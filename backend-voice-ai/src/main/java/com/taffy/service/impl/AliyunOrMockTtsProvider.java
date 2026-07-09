package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.dto.TtsConvertRequest;
import com.taffy.service.TtsProvider;
import com.taffy.service.TtsResult;
import com.taffy.util.WavAudioTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * TTS 实现 —— 使用微软 Edge TTS 神经网络语音，免费高质量。
 */
@Service
public class AliyunOrMockTtsProvider implements TtsProvider {
    private static final Logger log = LoggerFactory.getLogger(AliyunOrMockTtsProvider.class);

    // edge-tts 中文声音库 —— 根据上传音频音高自动匹配
    // 女声（edge-tts 7.2.8 可用标准女声）
    private static final String[] FEMALE_VOICES = {
        "zh-CN-XiaoyiNeural",    // 晓伊 - 活泼（高音）
        "zh-CN-XiaoxiaoNeural",  // 晓晓 - 温暖（低音）
    };
    // 男声（edge-tts 7.2.8 可用标准男声，按音高降序）
    private static final String[] MALE_VOICES = {
        "zh-CN-YunxiNeural",    // 云希 - 活力阳光（高音）
        "zh-CN-YunxiaNeural",   // 云夏 - 可爱（中高音）
        "zh-CN-YunyangNeural",  // 云扬 - 专业可靠（中音）
        "zh-CN-YunjianNeural",  // 云健 - 激情（低音）
    };

    private final VoiceAiProperties properties;

    public AliyunOrMockTtsProvider(VoiceAiProperties properties, AliyunTtsService aliyunTtsService) {
        this.properties = properties;
    }

    @Override
    public TtsResult synthesize(TtsConvertRequest request, String voiceParams) {
        byte[] wav = synthesizeWithEdgeTts(request, voiceParams);
        return new TtsResult(wav, "edge-tts", "微软Edge TTS (神经网络语音)");
    }

    // ==================== edge-tts ====================

    private byte[] synthesizeWithEdgeTts(TtsConvertRequest request, String voiceParams) {
        String text = requireText(request);
        String voice = resolveEdgeVoice(voiceParams);
        String rate = resolveEdgeRate(request == null ? null : request.getSpeed());
        String pitchHz = resolveEdgePitch(request == null ? null : request.getPitch());

        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("taffy-edge-tts-");
            Path mp3Path = workDir.resolve("output.mp3");
            Path wavPath = workDir.resolve("output.wav");

            // Step 1: edge-tts → MP3
            List<String> edgeArgs = new ArrayList<>();
            edgeArgs.add(edgeTtsCommand());
            edgeArgs.add("--voice");
            edgeArgs.add(voice);
            edgeArgs.add("--rate");
            edgeArgs.add(rate);
            edgeArgs.add("--pitch");
            edgeArgs.add(pitchHz);
            edgeArgs.add("--text");
            edgeArgs.add(text);
            edgeArgs.add("--write-media");
            edgeArgs.add(mp3Path.toString());

            log.info("edge-tts: voice={} rate={} pitch={} textLen={}", voice, rate, pitchHz, text.length());
            runProcess(edgeArgs, 60, "edge-tts合成超时", "edge-tts合成失败");

            if (!Files.exists(mp3Path) || Files.size(mp3Path) < 100) {
                throw new IllegalStateException("edge-tts未生成有效音频文件");
            }

            // Step 2: ffmpeg MP3 → 16kHz mono WAV
            List<String> ffmpegArgs = List.of(
                    "ffmpeg", "-y", "-i", mp3Path.toString(),
                    "-acodec", "pcm_s16le", "-ac", "1", "-ar", "16000",
                    wavPath.toString()
            );
            runProcess(ffmpegArgs, 30, "ffmpeg转换超时", "ffmpeg转换失败");

            return readValidWav(wavPath, "edge-tts合成失败");
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("edge-tts合成失败：" + safeMessage(e), e);
        } finally {
            deleteQuietly(workDir);
        }
    }

    // ---- edge-tts 参数映射 ----

    /** 根据上传音频的性别+音高匹配最相似的声音 */
    private String resolveEdgeVoice(String voiceParams) {
        String configured = getEdgeTtsConfig("voice");
        if (configured != null && !"auto".equalsIgnoreCase(configured)) return configured;

        String genderHint = WavAudioTools.extractGenderHint(voiceParams);
        double pitchHz = extractPitchHz(voiceParams);
        boolean isMale = "Male".equalsIgnoreCase(genderHint);

        if (isMale) {
            return selectByPitch(pitchHz, MALE_VOICES, new double[]{160, 135, 110});
        } else {
            return selectByPitch(pitchHz, FEMALE_VOICES, new double[]{220});
        }
    }

    /** 根据音高在声音列表中选最接近的 */
    private String selectByPitch(double pitchHz, String[] voices, double[] thresholds) {
        if (pitchHz <= 0) return voices[0]; // 无 profile 数据用默认
        for (int i = 0; i < thresholds.length; i++) {
            if (pitchHz >= thresholds[i]) return voices[i];
        }
        return voices[voices.length - 1];
    }

    /** 从 voice profile JSON 提取基频 */
    private double extractPitchHz(String voiceParams) {
        if (voiceParams == null || voiceParams.isBlank()) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"pitchHz\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
                .matcher(voiceParams);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    /** speed 0.5~2.0 → edge-tts rate "-50%"~"+100%" */
    private String resolveEdgeRate(Double speed) {
        double value = speed != null ? Math.max(0.5, Math.min(2.0, speed)) : 1.0;
        int rate = (int) Math.round((value - 1.0) * 100);
        return (rate >= 0 ? "+" : "") + rate + "%";
    }

    /** pitch 0.5~2.0 → edge-tts pitch "-10Hz"~"+20Hz" */
    private String resolveEdgePitch(Double pitch) {
        double value = pitch != null ? Math.max(0.5, Math.min(2.0, pitch)) : 1.0;
        int hz = (int) Math.round((value - 1.0) * 20);
        return (hz >= 0 ? "+" : "") + hz + "Hz";
    }

    private String edgeTtsCommand() {
        String configured = getEdgeTtsConfig("command");
        return (configured != null && !"auto".equalsIgnoreCase(configured)) ? configured : "edge-tts";
    }

    private String getEdgeTtsConfig(String field) {
        if (properties == null || properties.getEdgeTts() == null) return null;
        var cfg = properties.getEdgeTts();
        if ("voice".equals(field)) return cfg.getVoice();
        if ("command".equals(field)) return cfg.getCommand();
        return null;
    }

    // ==================== 公共工具 ====================

    private String requireText(TtsConvertRequest request) {
        String text = request == null || request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("text不能为空");
        return text;
    }

    private void runProcess(List<String> args, long timeoutSeconds, String timeoutMessage, String failurePrefix) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        ByteArrayOutputStream processOutput = new ByteArrayOutputStream();
        Thread outputThread = new Thread(() -> {
            try { process.getInputStream().transferTo(processOutput); } catch (IOException ignored) {}
        }, "taffy-tts-output-reader");
        outputThread.setDaemon(true);
        outputThread.start();

        boolean finished = process.waitFor(Math.max(1, timeoutSeconds), TimeUnit.SECONDS);
        if (!finished) { process.destroyForcibly(); throw new IllegalStateException(timeoutMessage); }
        outputThread.join(3000);

        String console = decodeConsoleOutput(processOutput.toByteArray());
        if (process.exitValue() != 0) {
            throw new IllegalStateException(failurePrefix + "：" + compactConsoleMessage(console));
        }
    }

    private byte[] readValidWav(Path outputPath, String failurePrefix) throws IOException {
        if (!Files.exists(outputPath) || !Files.isRegularFile(outputPath)) {
            throw new IllegalStateException(failurePrefix + "：NO_OUTPUT_FILE");
        }
        byte[] wav = Files.readAllBytes(outputPath);
        if (wav.length <= 44) throw new IllegalStateException(failurePrefix + "：INVALID_OUTPUT_FILE");
        return wav;
    }

    private String decodeConsoleOutput(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        return new String(bytes, StandardCharsets.UTF_8).replace('\0', ' ').trim();
    }

    private String compactConsoleMessage(String message) {
        if (message == null || message.isBlank()) return "UNKNOWN_ERROR";
        String compact = message.replace('\r', ' ').replace('\n', ' ').replaceAll("\\s+", " ").trim();
        return compact.length() > 300 ? compact.substring(0, 300) + "..." : compact;
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? e.getClass().getSimpleName() : e.getMessage();
    }

    private void deleteQuietly(Path dir) {
        if (dir == null || !Files.exists(dir)) return;
        try (Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}
