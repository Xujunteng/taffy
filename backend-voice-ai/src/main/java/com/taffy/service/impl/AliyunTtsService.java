package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.util.WavAudioTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阿里云智能语音交互 REST API 封装。
 *
 * 支持：
 * - Token 管理（HMAC-SHA1 签名，缓存复用）
 * - TTS 语音合成
 * - 声音复刻训练
 */
@Service
public class AliyunTtsService {

    private static final Logger log = LoggerFactory.getLogger(AliyunTtsService.class);

    private static final String DEFAULT_TOKEN_URL = "https://nls-meta.cn-shanghai.aliyuncs.com/pop/2018-05-18/tokens";
    private static final String DEFAULT_TTS_URL = "https://nls-gateway.cn-shanghai.aliyuncs.com/stream/v1/tts";
    private static String rfc822Date() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("GMT"));
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        return now.format(fmt);
    }

    private final VoiceAiProperties properties;

    private volatile String cachedToken;
    private volatile long tokenExpireTime;
    private final ReentrantLock tokenLock = new ReentrantLock();

    public AliyunTtsService(VoiceAiProperties properties) {
        this.properties = properties;
    }

    // ==================== Token 管理 ====================

    public String getToken() throws Exception {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpireTime - 60_000) {
            return cachedToken;
        }
        tokenLock.lock();
        try {
            if (cachedToken != null && now < tokenExpireTime - 60_000) {
                return cachedToken;
            }
            VoiceAiProperties.Aliyun cfg = properties.getAliyun();
            String accessKeyId = requireConfig(cfg.getAccessKeyId(), "access-key-id");
            String accessKeySecret = requireConfig(cfg.getAccessKeySecret(), "access-key-secret");

            String date = rfc822Date();
            String nonce = UUID.randomUUID().toString();
            String canonicalHeaders = "x-acs-signature-nonce:" + nonce + "\n";

            String stringToSign = "POST\napplication/json\n\napplication/json\n"
                    + date + "\n" + canonicalHeaders + "/pop/2018-05-18/tokens";

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            String signature = Base64.getEncoder().encodeToString(
                    mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));

            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Authorization", "acs " + accessKeyId + ":" + signature);
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            headers.put("Date", date);
            headers.put("x-acs-signature-nonce", nonce);

            String resp = httpPostString(resolveTokenUrl(), "{}", headers);
            String token = extractJsonString(resp, "Id");
            if (token == null || token.isEmpty()) {
                throw new IllegalStateException("阿里云Token获取失败: " + resp.substring(0, Math.min(200, resp.length())));
            }
            String expireStr = extractJsonString(resp, "ExpireTime");
            cachedToken = token;
            tokenExpireTime = expireStr != null ? Long.parseLong(expireStr) * 1000 : now + 3600_000;
            log.info("阿里云Token已刷新, 过期: {}", new Date(tokenExpireTime));
            return cachedToken;
        } finally {
            tokenLock.unlock();
        }
    }

    // ==================== TTS 语音合成 ====================

    /**
     * 调用阿里云 TTS 合成语音，返回 WAV 字节数组。
     * @param text 文本内容
     * @param customVoiceId 自定义声音模型ID（声音复刻训练后获得），null则用默认发音人
     */
    public byte[] synthesize(String text, String customVoiceId, Double speed, Double pitch) throws Exception {
        VoiceAiProperties.Aliyun cfg = properties.getAliyun();
        String token = getToken();
        String appKey = requireConfig(cfg.getAppKey(), "app-key");

        // 优先使用训练后的自定义声音，否则用配置的默认发音人
        String resolvedVoice = (customVoiceId != null && !customVoiceId.isBlank())
                ? customVoiceId : cfg.getVoiceName();
        if (resolvedVoice == null || resolvedVoice.isBlank()) resolvedVoice = "xiaoyun";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("appkey", appKey);
        params.put("token", token);
        params.put("text", text);
        params.put("format", cfg.getFormat() != null ? cfg.getFormat() : "wav");
        params.put("sample_rate", String.valueOf(cfg.getSampleRate() > 0 ? cfg.getSampleRate() : 16000));
        params.put("voice", resolvedVoice);
        params.put("volume", "50");

        double spd = speed != null ? Math.max(0.5, Math.min(2.0, speed)) : 1.0;
        params.put("speech_rate", String.valueOf((int) (spd * 100 - 100)));

        double pt = pitch != null ? Math.max(0.5, Math.min(2.0, pitch)) : 1.0;
        params.put("pitch_rate", String.valueOf((int) (pt * 100 - 100)));

        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (body.length() > 0) body.append("&");
            body.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }

        byte[] audio = httpPost(resolveTtsUrl(), body.toString(),
                Map.of("X-NLS-Token", token, "Content-Type", "application/x-www-form-urlencoded"));

        if (audio == null || audio.length < 44) {
            throw new IllegalStateException("阿里云TTS返回音频为空");
        }
        return audio;
    }

    // ==================== 音色匹配 ====================

    /**
     * 根据本地 voice profile 匹配最佳阿里云预设音色。
     *
     * 阿里云预设音色参考：
     * 女声 — xiaoyun(标准女声) / ruoxi(温柔女声) / sitong(活泼女声) / aiqi(甜美女声)
     * 男声 — xiaogang(标准男声) / sicheng(成熟男声)
     *
     * @param voiceProfileJson 本地音色分析 JSON
     * @return 推荐音色名，未配置阿里云时返回 null
     */
    public String selectBestVoice(String voiceProfileJson) {
        if (!isConfigured()) return null;

        String genderHint = WavAudioTools.extractGenderHint(voiceProfileJson);
        if ("Male".equalsIgnoreCase(genderHint)) {
            return "xiaogang";   // 标准男声
        }
        return "xiaoyun";         // 标准女声（默认）
    }

    // ==================== 声音训练 ====================

    /**
     * 阿里云声音复刻训练。
     *
     * 注意：完整的阿里云声音复刻需要先将音频上传到 OSS 再提交训练任务。
     * 当前未集成 OSS，此方法返回 null。音色匹配请使用 {@link #selectBestVoice(String)}。
     *
     * @param audioFilePath 音频文件路径
     * @param modelName 模型名称
     * @return 始终返回 null
     */
    public String trainVoice(String audioFilePath, String modelName) {
        if (!isConfigured()) {
            log.info("阿里云未配置，跳过远程训练: audioPath={}", audioFilePath);
            return null;
        }
        log.info("阿里云声音复刻需要 OSS 上传（未集成），使用本地分析+音色匹配: name={}", modelName);
        return null;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Token 使用固定的 meta 服务端点，不受 endpoint 配置影响 */
    private String resolveTokenUrl() {
        return DEFAULT_TOKEN_URL;
    }

    /** TTS 使用配置的 endpoint，默认 cn-shanghai gateway */
    private String resolveTtsUrl() {
        VoiceAiProperties.Aliyun cfg = properties.getAliyun();
        String endpoint = cfg != null ? cfg.getEndpoint() : null;
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.replaceFirst("/+$", "") + "/stream/v1/tts";
        }
        return DEFAULT_TTS_URL;
    }

    public boolean isConfigured() {
        VoiceAiProperties.Aliyun cfg = properties.getAliyun();
        return cfg != null && cfg.isEnabled()
                && cfg.getAccessKeyId() != null && !cfg.getAccessKeyId().isBlank()
                && !cfg.getAccessKeyId().startsWith("your-")
                && cfg.getAccessKeySecret() != null && !cfg.getAccessKeySecret().isBlank()
                && !cfg.getAccessKeySecret().startsWith("your-")
                && cfg.getAppKey() != null && !cfg.getAppKey().isBlank()
                && !cfg.getAppKey().startsWith("your-");
    }

    // ==================== HTTP 工具 ====================

    private String httpPostString(String urlStr, String body, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = openConnection(urlStr, "POST", headers);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private byte[] httpPost(String urlStr, String body, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = openConnection(urlStr, "POST", headers);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return readResponseBytes(conn);
    }

    private HttpURLConnection openConnection(String urlStr, String method, Map<String, String> headers) throws Exception {
        URL url = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(30000);
        if (headers != null) {
            headers.forEach(conn::setRequestProperty);
        }
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String body = new String(readAllBytes(is), StandardCharsets.UTF_8);
        if (code >= 400) {
            throw new IllegalStateException("阿里云API HTTP " + code + ": " + body.substring(0, Math.min(200, body.length())));
        }
        return body;
    }

    private byte[] readResponseBytes(HttpURLConnection conn) throws Exception {
        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            String contentType = conn.getContentType();
            byte[] body = readAllBytes(conn.getInputStream());
            // 阿里云流式TTS可能返回HTTP 200 + JSON错误体
            if (contentType != null && contentType.contains("application/json") && body.length > 0) {
                String json = new String(body, StandardCharsets.UTF_8);
                String errMsg = extractJsonString(json, "error_message");
                if (errMsg == null) errMsg = extractJsonString(json, "Message");
                if (errMsg == null) errMsg = json.substring(0, Math.min(200, json.length()));
                throw new IllegalStateException("阿里云TTS API错误: " + errMsg);
            }
            return body;
        }
        String err = conn.getErrorStream() != null
                ? new String(readAllBytes(conn.getErrorStream()), StandardCharsets.UTF_8)
                : "无错误详情";
        throw new IllegalStateException("阿里云TTS HTTP " + code + ": " + err.substring(0, Math.min(200, err.length())));
    }

    private byte[] readAllBytes(InputStream is) throws Exception {
        if (is == null) return new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) out.write(buf, 0, n);
        is.close();
        return out.toByteArray();
    }

    // ==================== JSON 工具 ====================

    private String extractJsonString(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx < 0) return null;
        int colon = json.indexOf(':', keyIdx);
        if (colon < 0) return null;
        int start = json.indexOf('"', colon);
        if (start < 0) return null;
        int end = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    private String requireConfig(String value, String name) {
        if (value == null || value.isBlank() || value.startsWith("your-")) {
            throw new IllegalStateException("阿里云" + name + "未配置，请在 application.yml 或环境变量中设置 voice-ai.aliyun." + name);
        }
        return value;
    }
}
