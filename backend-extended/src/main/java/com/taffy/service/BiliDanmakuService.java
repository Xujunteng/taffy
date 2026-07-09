package com.taffy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.zip.Inflater;

/**
 * B站直播弹幕 WebSocket 客户端。
 * 连接 wss://broadcastlv.chat.bilibili.com/sub，解析二进制协议。
 */
@Service
public class BiliDanmakuService {
    private static final Logger log = LoggerFactory.getLogger(BiliDanmakuService.class);
    private static final String BILI_WS = "wss://broadcastlv.chat.bilibili.com/sub";

    private WebSocket ws;
    private ScheduledExecutorService heartbeat;
    private String roomId;
    private volatile boolean connected;
    private final CopyOnWriteArrayList<DanmakuListener> listeners = new CopyOnWriteArrayList<>();
    private final java.util.Set<String> recentDanmaku = ConcurrentHashMap.newKeySet();

    @FunctionalInterface
    public interface DanmakuListener {
        void onMessage(String type, String user, String content, Map<String, Object> raw);
    }

    public void addListener(DanmakuListener l) { listeners.add(l); }
    public void removeListener(DanmakuListener l) { listeners.remove(l); }
    private void fire(String type, String user, String content, Map<String, Object> raw) {
        String dedup = user + "|" + content;
        if (recentDanmaku.contains(dedup)) return;
        recentDanmaku.add(dedup);
        if (recentDanmaku.size() > 200) recentDanmaku.clear();
        for (DanmakuListener l : listeners) {
            try { l.onMessage(type, user, content, raw); } catch (Exception ignored) {}
        }
    }

    public boolean isConnected() { return connected; }
    public String getRoomId() { return roomId; }

    public synchronized void connect(String targetRoomId) {
        disconnect();
        this.roomId = targetRoomId;
        try {
            HttpClient client = HttpClient.newHttpClient();
            ws = client.newWebSocketBuilder()
                    .buildAsync(URI.create(BILI_WS), new WsListener())
                    .get(5, TimeUnit.SECONDS);
            heartbeat = Executors.newSingleThreadScheduledExecutor();
            heartbeat.scheduleAtFixedRate(this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
            log.info("B站弹幕 WebSocket 已连接, roomId={}", roomId);
        } catch (Exception e) {
            log.error("B站 WebSocket 连接失败: {}", e.getMessage());
            connected = false;
        }
    }

    public synchronized void disconnect() {
        connected = false;
        roomId = null; // 防止 onClose 自动重连
        if (heartbeat != null) { heartbeat.shutdownNow(); heartbeat = null; }
        if (ws != null) {
            try { ws.sendClose(1000, "bye"); } catch (Exception ignored) {}
            ws = null;
        }
    }

    // === 二进制协议 ===

    private void sendAuth() {
        if (ws == null || roomId == null) return;
        String json = "{\"uid\":0,\"roomid\":" + roomId +
                ",\"protover\":2,\"platform\":\"web\",\"clientver\":\"2.6.0\",\"type\":2}";
        byte[] body = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        sendPacket(7, body); // OP_AUTH = 7
    }

    private void sendHeartbeat() {
        if (!connected || ws == null) return;
        sendPacket(2, new byte[0]); // OP_HEARTBEAT = 2
    }

    private void sendPacket(int operation, byte[] body) {
        if (ws == null) return;
        try {
            ByteBuffer buf = ByteBuffer.allocate(16 + body.length);
            buf.putInt(16 + body.length); // total_len
            buf.putShort((short) 16);     // header_len
            buf.putShort((short) 1);      // proto_ver
            buf.putInt(operation);        // operation
            buf.putInt(1);                // sequence
            buf.put(body);
            buf.flip();
            ws.sendBinary(buf, true);
        } catch (Exception e) {
            log.warn("B站发送包失败: {}", e.getMessage());
        }
    }

    private void onBinaryMessage(ByteBuffer buf) {
        try {
            buf.position(0);
            int totalLen = buf.getInt();
            int headerLen = buf.getShort() & 0xFFFF;
            int protoVer = buf.getShort() & 0xFFFF;
            int op = buf.getInt();
            buf.getInt(); // sequence, unused

            int bodyLen = totalLen - headerLen;
            if (bodyLen <= 0) return;

            byte[] body = new byte[bodyLen];
            buf.get(body);

            if (op == 8) { // AUTH_REPLY
                connected = true;
                log.info("B站弹幕认证成功, roomId={}", roomId);
                return;
            }
            if (op == 3) return; // HEARTBEAT_REPLY

            if (op == 5) { // MESSAGE
                byte[] decompressed = decompress(body, protoVer);
                String text = new String(decompressed, java.nio.charset.StandardCharsets.UTF_8);
                // 可能包含多条 JSON
                for (String part : text.split("(?<=\\})\\s*(?=\\{)")) {
                    parseDanmakuMessage(part.trim());
                }
            }
        } catch (Exception e) {
            log.warn("B站数据包解析错误: {}", e.getMessage());
        }
    }

    private byte[] decompress(byte[] data, int protoVer) throws Exception {
        if (protoVer == 0 || protoVer == 1) return data;
        if (protoVer == 2) {
            // B站 protover=2 是 zlib (deflate)，部分服务器用 raw deflate
            try {
                return inflate(data, false); // 先尝试标准 zlib
            } catch (Exception e) {
                return inflate(data, true);  // 失败则尝试 raw deflate
            }
        }
        return data; // protoVer 3 (brotli) fallback
    }
    private byte[] inflate(byte[] data, boolean nowrap) throws Exception {
        Inflater inflater = new Inflater(nowrap);
        inflater.setInput(data);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        while (!inflater.finished()) {
            int n = inflater.inflate(buf);
            if (n > 0) out.write(buf, 0, n);
        }
        inflater.end();
        return out.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private void parseDanmakuMessage(String json) {
        try {
            Map<String, Object> msg = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
            String cmd = (String) msg.get("cmd");
            if (cmd == null) return;

            if (cmd.contains("DANMU_MSG")) {
                List<Object> info = msg.get("info") instanceof List ? (List<Object>) msg.get("info") : null;
                if (info != null && info.size() > 3) {
                    String content = String.valueOf(info.get(1));
                    List<Object> userInfo = info.get(2) instanceof List ? (List<Object>) info.get(2) : List.of();
                    String username = userInfo.size() > 0 ? String.valueOf(userInfo.get(0)) : "匿名";
                    fire("danmaku", username, content, msg);
                }
            } else if (cmd.contains("SEND_GIFT")) {
                Map<String, Object> data = (Map<String, Object>) msg.get("data");
                if (data != null) {
                    String user = String.valueOf(data.getOrDefault("uname", "匿名"));
                    String gift = String.valueOf(data.getOrDefault("giftName", "礼物"));
                    int num = data.get("num") instanceof Number ? ((Number) data.get("num")).intValue() : 1;
                    fire("gift", user, gift + " x" + num, msg);
                }
            } else if (cmd.contains("WELCOME")) {
                Map<String, Object> data = (Map<String, Object>) msg.get("data");
                if (data != null) {
                    String user = String.valueOf(data.getOrDefault("uname", "用户"));
                    fire("enter", user, "进入了直播间", msg);
                }
            }
        } catch (Exception ignored) {}
    }

    // === WebSocket Listener ===
    private class WsListener implements WebSocket.Listener {
        private ByteBuffer pending;

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
            sendAuth();
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (pending != null) {
                ByteBuffer merged = ByteBuffer.allocate(pending.remaining() + data.remaining());
                merged.put(pending); merged.put(data); merged.flip();
                pending = null;
                onBinaryMessage(merged);
            } else if (last) {
                onBinaryMessage(data);
            } else {
                pending = data;
            }
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int code, String reason) {
            connected = false;
            log.info("B站 WebSocket 断开: {} {}", code, reason);
            if (roomId != null) {
                // 用 heartbeat 线程池调度重连，不创建新线程
                if (heartbeat != null && !heartbeat.isShutdown()) {
                    heartbeat.schedule(() -> connect(roomId), 5, TimeUnit.SECONDS);
                }
            }
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            connected = false;
            log.warn("B站 WebSocket 错误: {}", error.getMessage());
        }
    }
}
