package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.dto.*;
import com.taffy.entity.TtsTask;
import com.taffy.entity.VoiceModel;
import com.taffy.mapper.TtsTaskMapper;
import com.taffy.mapper.VoiceModelMapper;
import com.taffy.service.TtsProvider;
import com.taffy.service.TtsResult;
import com.taffy.service.VoiceAiService;
import com.taffy.util.WavAudioTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 语音AI核心服务实现
 */
@Service
public class VoiceAiServiceImpl implements VoiceAiService {
    private static final Logger log = LoggerFactory.getLogger(VoiceAiServiceImpl.class);

    private final VoiceModelMapper voiceModelMapper;
    private final TtsTaskMapper ttsTaskMapper;
    private final TtsProvider ttsProvider;
    private final VoiceAiProperties properties;
    private final AliyunTtsService aliyunTtsService;

    public VoiceAiServiceImpl(VoiceModelMapper voiceModelMapper, TtsTaskMapper ttsTaskMapper,
                              TtsProvider ttsProvider, VoiceAiProperties properties,
                              AliyunTtsService aliyunTtsService) {
        this.voiceModelMapper = voiceModelMapper;
        this.ttsTaskMapper = ttsTaskMapper;
        this.ttsProvider = ttsProvider;
        this.properties = properties;
        this.aliyunTtsService = aliyunTtsService;
    }

    @Override
    public VoiceTrainResponse trainVoice(Long userId, VoiceTrainRequest request) {
        if (userId == null) {
            throw new IllegalStateException("登录状态异常，请重新登录");
        }
        if (request == null || request.getVoiceModelId() == null) {
            throw new IllegalArgumentException("voiceModelId不能为空");
        }

        // 查找已有的声音模型
        VoiceModel model = voiceModelMapper.findById(request.getVoiceModelId());
        if (model == null) {
            throw new IllegalArgumentException("声音模型不存在");
        }
        if (!userId.equals(model.getUserId())) {
            throw new IllegalArgumentException("无权操作该声音模型");
        }

        String audioPath = model.getAudioFilePath();
        if (audioPath == null || audioPath.isBlank()) {
            model.setStatus("失败");
            model.setModelParams("{\"error\":\"训练音频路径为空\"}");
            voiceModelMapper.updateStatus(model);
            throw new IllegalStateException("训练音频路径为空，请重新上传音频");
        }

        Path audioFile = Path.of(audioPath).toAbsolutePath().normalize();
        if (!Files.exists(audioFile) || !Files.isRegularFile(audioFile)) {
            model.setStatus("失败");
            model.setModelParams("{\"error\":\"训练音频文件不存在\",\"sourceAudio\":\"" + escapeJson(audioPath) + "\"}");
            voiceModelMapper.updateStatus(model);
            throw new IllegalStateException("训练音频文件不存在，请重新上传音频");
        }

        // 分析上传音频的基频/响度/明亮度，生成 voice profile
        // Java Sound API 原生不支持 MP3/M4A/AAC，需先用 ffmpeg 转为标准 WAV
        Path analysisFile = audioFile;
        Path tempWav = null;
        try {
            String ext = audioPath.contains(".") ? audioPath.substring(audioPath.lastIndexOf('.') + 1).toLowerCase() : "";
            if (!Set.of("wav").contains(ext)) {
                tempWav = Files.createTempFile("taffy-train-", ".wav");
                convertToWavForAnalysis(audioFile, tempWav);
                analysisFile = tempWav;
                log.info("已将 {} 音频转为 WAV 用于分析", ext.toUpperCase());
            }
        } catch (Exception e) {
            log.warn("ffmpeg 预处理音频失败，回退到直接分析: {}", e.getMessage());
        }

        WavAudioTools.AudioProfile profile = WavAudioTools.analyzeFile(analysisFile);

        // 清理临时 WAV
        if (tempWav != null) {
            try { Files.deleteIfExists(tempWav); } catch (java.io.IOException ignored) {}
        }

        String localParams = WavAudioTools.profileToJson(profile, audioFile.toString());
        model.setModelParams(localParams);
        model.setStatus("就绪");

        // 匹配最佳阿里云预设音色（无需 OSS，基于本地音色分析）
        String trainingMsg = profile.valid ? "训练完成：已提取音色特征" : "训练完成：音频已保存，未检测到有效音色特征，使用默认音色";
        try {
            String bestVoice = aliyunTtsService.selectBestVoice(localParams);
            if (bestVoice != null) {
                String aliyunParams = "{" +
                        "\"provider\":\"aliyun-voice-match\"," +
                        "\"recommendedVoice\":\"" + bestVoice + "\"," +
                        "\"profileApplied\":true" +
                        "}";
                model.setModelParams(mergeVoiceParams(localParams, aliyunParams));
                trainingMsg = "训练完成：已匹配阿里云音色「" + bestVoice + "」";
            }
        } catch (Exception e) {
            // 音色匹配失败，已用本地 profile，不阻塞
            log.warn("阿里云音色匹配失败：{}", e.getMessage());
        }
        voiceModelMapper.updateStatus(model);

        return new VoiceTrainResponse(model.getId(), model.getStatus(), trainingMsg);
    }

    @Override
    public TtsConvertResponse convertTextToSpeech(Long userId, TtsConvertRequest request) throws Exception {
        if (userId == null) {
            throw new IllegalStateException("登录状态异常，请重新登录");
        }
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("text不能为空");
        }

        VoiceModel model = null;
        if (request.getVoiceModelId() != null) {
            model = voiceModelMapper.findById(request.getVoiceModelId());
            if (model == null) {
                throw new IllegalArgumentException("声音模型不存在");
            }
            if (!userId.equals(model.getUserId())) {
                throw new IllegalArgumentException("无权使用该声音模型");
            }
            if (!"就绪".equals(model.getStatus())) {
                throw new IllegalStateException("声音模型尚未就绪，请先训练");
            }
        }

        TtsTask task = new TtsTask();
        task.setUserId(userId);
        task.setVoiceModelId(request.getVoiceModelId());
        task.setTextContent(request.getText().trim());
        task.setStatus("processing");
        task.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ttsTaskMapper.insert(task);

        try {
            String voiceParams = model == null ? "{}" : ensureVoiceProfile(model);
            TtsResult ttsResult = ttsProvider.synthesize(request, voiceParams);
            byte[] audio = ttsResult.getAudio();
            if (audio == null || audio.length < 44) {
                throw new IllegalStateException("TTS合成结果为空");
            }

            Path dir = resolveStorageRoot().resolve(String.valueOf(userId)).resolve("tts");
            Files.createDirectories(dir);
            Path output = dir.resolve("tts-" + task.getId() + "-" + UUID.randomUUID() + ".wav");
            Files.write(output, audio);

            ttsTaskMapper.updateStatusAndOutput(task.getId(), "success", output.toString());
            return new TtsConvertResponse(task.getId(), "success", output.toString(),
                    "/api/tts/audio/" + task.getId(),
                    ttsResult.getProvider(), ttsResult.getProviderNote());
        } catch (Exception e) {
            if (task.getId() != null) {
                ttsTaskMapper.updateStatusAndOutput(task.getId(), "failed", null);
            }
            throw e;
        }
    }

    @Override
    public TtsTask getTaskStatus(Long taskId) {
        TtsTask task = ttsTaskMapper.findById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("TTS任务不存在");
        }
        return task;
    }

    /**
     * 兼容旧版本已训练模型：如果数据库里还没有 voice profile，合成前自动从原始上传音频补提取一次。
     */
    private String ensureVoiceProfile(VoiceModel model) {
        String params = model.getModelParams();
        if (params != null && params.contains("\"profileApplied\":true")) {
            return params;
        }
        String audioPath = model.getAudioFilePath();
        if (audioPath == null || audioPath.isBlank()) {
            return params == null ? "{}" : params;
        }
        Path audioFile = Path.of(audioPath).toAbsolutePath().normalize();

        // 同样需要 ffmpeg 预处理，确保 Java Sound 能解析
        Path analysisFile = audioFile;
        Path tempWav = null;
        try {
            String ext = audioPath.contains(".") ? audioPath.substring(audioPath.lastIndexOf('.') + 1).toLowerCase() : "";
            if (!Set.of("wav").contains(ext)) {
                tempWav = Files.createTempFile("taffy-profile-", ".wav");
                convertToWavForAnalysis(audioFile, tempWav);
                analysisFile = tempWav;
            }
        } catch (Exception e) {
            log.warn("ensureVoiceProfile ffmpeg 预处理失败: {}", e.getMessage());
        }

        WavAudioTools.AudioProfile profile = WavAudioTools.analyzeFile(analysisFile);

        if (tempWav != null) {
            try { Files.deleteIfExists(tempWav); } catch (java.io.IOException ignored) {}
        }

        String newParams = WavAudioTools.profileToJson(profile, audioFile.toString());
        model.setModelParams(newParams);
        voiceModelMapper.updateStatus(model);
        return newParams;
    }

    /**
     * 用 ffmpeg 将任意音频转为标准 16kHz 单声道 WAV，供 Java Sound API 分析。
     * Java Sound 原生只支持 WAV/AIFF/AU，不支持 MP3/M4A/AAC/OGG。
     */
    private void convertToWavForAnalysis(Path input, Path output) throws Exception {
        List<String> args = List.of(
                "ffmpeg", "-y", "-i", input.toString(),
                "-acodec", "pcm_s16le", "-ac", "1", "-ar", "16000",
                output.toString()
        );
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try { process.getInputStream().transferTo(out); } catch (java.io.IOException ignored) {}
        }, "taffy-ffmpeg-reader");
        reader.setDaemon(true);
        reader.start();

        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("ffmpeg 音频转换超时");
        }
        reader.join(3000);

        if (process.exitValue() != 0) {
            String msg = new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8).trim();
            throw new IllegalStateException("ffmpeg 转换失败: " + (msg.isEmpty() ? "UNKNOWN" : msg.substring(0, Math.min(200, msg.length()))));
        }
    }

    private Path resolveStorageRoot() {
        Path root = Path.of(properties.getStorageRoot());
        if (!root.isAbsolute()) {
            root = Path.of(System.getProperty("user.dir")).resolve(root).toAbsolutePath().normalize();
        }
        return root;
    }

    /**
     * 合并阿里云 voice model params 和本地 profile params。
     * 保留阿里云的 voiceId + 本地的音色分析数据，生成合法的 JSON。
     *
     * 注意：当前阿里云声音复刻（OSS 上传）未集成，此方法预留供后续使用。
     */
    private String mergeVoiceParams(String localParams, String aliyunParams) {
        if (aliyunParams == null || aliyunParams.isBlank()) return localParams;
        if (localParams == null || localParams.isBlank()) return aliyunParams;

        // 去掉两边的 {}，合并，以 aliyun 为主，本地补充
        String local = localParams.trim();
        String aliyun = aliyunParams.trim();
        if (local.startsWith("{")) local = local.substring(1);
        if (local.endsWith("}")) local = local.substring(0, local.length() - 1);
        if (aliyun.startsWith("{")) aliyun = aliyun.substring(1);
        if (aliyun.endsWith("}")) aliyun = aliyun.substring(0, aliyun.length() - 1);
        // 确保不产生空字符串拼接
        if (local.isEmpty() && aliyun.isEmpty()) return "{}";
        if (local.isEmpty()) return "{" + aliyun + "}";
        if (aliyun.isEmpty()) return "{" + local + "}";
        return "{" + aliyun + "," + local + "}";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
