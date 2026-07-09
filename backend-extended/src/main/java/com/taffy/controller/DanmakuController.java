package com.taffy.controller;

import com.taffy.service.BiliDanmakuService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/live")
public class DanmakuController {

    @Autowired
    private BiliDanmakuService biliService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        biliService.addListener((type, user, content, raw) -> {
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("type", type);
            msg.put("user", user);
            msg.put("content", content);
            msg.put("time", java.time.LocalTime.now().toString().substring(0, 8));
            String json = toJson(msg);
            for (SseEmitter e : sseEmitters) {
                try { e.send(SseEmitter.event().name("danmaku").data(json)); } catch (Exception ex) { sseEmitters.remove(e); }
            }
        });
    }

    /** SSE 弹幕流 — 前端 EventSource 连接 */
    @GetMapping(value = "/danmaku/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L);
        sseEmitters.add(emitter);
        emitter.onCompletion(() -> sseEmitters.remove(emitter));
        emitter.onTimeout(() -> sseEmitters.remove(emitter));
        return emitter;
    }

    /** 连接 B站直播间弹幕 */
    @PostMapping("/danmaku/connect")
    public Map<String, Object> connect(@RequestBody Map<String, String> body) {
        String roomId = body.get("roomId");
        if (roomId == null || roomId.isBlank()) {
            return error("请输入B站直播间ID");
        }
        biliService.connect(roomId.trim());
        return ok(Map.of("roomId", roomId, "status", "connecting"));
    }

    /** 断开 B站弹幕 */
    @PostMapping("/danmaku/disconnect")
    public Map<String, Object> disconnectDanmaku() {
        biliService.disconnect();
        return ok(Map.of("status", "disconnected"));
    }

    /** 弹幕连接状态 */
    @GetMapping("/danmaku/status")
    public Map<String, Object> status() {
        return ok(Map.of(
            "connected", biliService.isConnected(),
            "roomId", biliService.getRoomId() != null ? biliService.getRoomId() : ""
        ));
    }

    /** 模拟弹幕（用于演示/测试） */
    @PostMapping("/danmaku/mock")
    public Map<String, Object> mock(@RequestBody Map<String, Object> body) {
        String user = String.valueOf(body.getOrDefault("user", "模拟用户"));
        String content = String.valueOf(body.getOrDefault("content", "666"));
        for (SseEmitter e : sseEmitters) {
            try {
                Map<String, Object> msg = Map.of("type", "danmaku", "user", user,
                        "content", content, "time", java.time.LocalTime.now().toString().substring(0, 8));
                e.send(SseEmitter.event().name("danmaku").data(toJson(msg)));
            } catch (Exception ex) { sseEmitters.remove(e); }
        }
        return ok(Map.of("sent", true));
    }

    /** 开放 API — 推送文本到 TTS 合成队列。鉴权：X-API-Key 请求头 */
    @PostMapping("/push")
    public Map<String, Object> push(@RequestBody Map<String, Object> body,
                                    @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return error(401, "请提供 X-API-Key 请求头，密钥可在个人中心获取");
        }
        // 查 users 表验证 API Key
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, username FROM users WHERE api_key = ?", apiKey);
        if (rows.isEmpty()) return error(401, "API Key 无效");
        Long userId = ((Number) rows.get(0).get("id")).longValue();
        String text = String.valueOf(body.getOrDefault("text", ""));
        if (text.isBlank()) return error("text 不能为空");

        // 返回成功，实际 TTS 由前端轮询或通过 SSE 推送
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("text", text);
        data.put("voiceModelId", body.getOrDefault("voiceModelId", null));
        data.put("speed", body.getOrDefault("speed", 1.0));
        data.put("pitch", body.getOrDefault("pitch", 1.0));
        data.put("status", "queued");
        data.put("timestamp", System.currentTimeMillis());

        // 通过 SSE 推送给前端
        for (SseEmitter e : sseEmitters) {
            try {
                Map<String, Object> pushMsg = new LinkedHashMap<>(data);
                pushMsg.put("type", "push");
                e.send(SseEmitter.event().name("push").data(toJson(pushMsg)));
            } catch (Exception ex) { sseEmitters.remove(e); }
        }

        return ok(data);
    }

    private static Map<String, Object> ok(Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", 200); r.put("message", "success"); r.put("data", data); return r;
    }
    private static Map<String, Object> error(String msg) { return error(400, msg); }
    private static Map<String, Object> error(int code, String msg) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code); r.put("message", msg); return r;
    }
    private static String toJson(Object obj) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj); }
        catch (Exception e) { return "{}"; }
    }
}
