package com.taffy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纯 Java 音频处理工具。
 *
 * 作用：
 * 1. 训练时从上传 WAV 中提取一个轻量 voice profile：平均基频、响度、明亮度。
 * 2. 合成后根据 voice profile 和前端 pitch 滑块做后处理，让音调真正发生变化。
 *
 * 注意：这不是神经网络声音克隆，只是本地无模型情况下的音色近似处理。
 */
public final class WavAudioTools {
    private static final Logger log = LoggerFactory.getLogger(WavAudioTools.class);

    private WavAudioTools() {
    }

    public static AudioProfile analyzeFile(Path path) {
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            log.warn("音频分析: 文件不存在或不可读 path={}", path);
            return AudioProfile.neutral("SOURCE_AUDIO_NOT_FOUND");
        }
        try {
            PcmAudio pcm = readAsPcm16Mono(path);
            return analyzePcm(pcm, "OK");
        } catch (Exception e) {
            log.warn("音频分析失败 path={} error={}: {}", path, e.getClass().getSimpleName(), e.getMessage());
            return AudioProfile.neutral("UNSUPPORTED_AUDIO_OR_ANALYZE_FAILED");
        }
    }

    public static byte[] applyVoiceProfileAndPitch(byte[] wavBytes, double pitch, String modelParams) {
        if (wavBytes == null || wavBytes.length <= 44) {
            return wavBytes;
        }
        try {
            PcmAudio pcm = readAsPcm16Mono(wavBytes);
            AudioProfile generated = analyzePcm(pcm, "GENERATED");
            AudioProfile trained = AudioProfile.fromJson(modelParams);

            double uiPitch = clamp(pitch <= 0 ? 1.0 : pitch, 0.5, 2.0);
            double modelPitchFactor = 1.0;
            if (trained.valid && trained.pitchHz > 50 && generated.pitchHz > 50) {
                modelPitchFactor = clamp(trained.pitchHz / generated.pitchHz, 0.72, 1.38);
            }

            double totalPitchFactor = clamp(uiPitch * modelPitchFactor, 0.45, 2.25);
            short[] samples = pcm.samples;

            if (Math.abs(totalPitchFactor - 1.0) > 0.025) {
                samples = resampleForPitch(samples, totalPitchFactor);
            }

            if (trained.valid) {
                samples = matchRms(samples, trained.rms);
                samples = matchBrightness(samples, pcm.sampleRate, trained.brightness, generated.brightness);
            }

            return writeMono16Wav(pcm.sampleRate, samples);
        } catch (Exception e) {
            log.error("WavAudioTools后处理失败，返回原始音频: {}", e.getMessage());
            return wavBytes;
        }
    }

    public static String profileToJson(AudioProfile profile, String sourceAudio) {
        if (profile == null) {
            profile = AudioProfile.neutral("EMPTY_PROFILE");
        }
        return "{" +
                "\"provider\":\"local-sapi-profile\"," +
                "\"profileApplied\":true," +
                "\"analysisStatus\":\"" + escapeJson(profile.status) + "\"," +
                "\"valid\":" + profile.valid + "," +
                "\"pitchHz\":" + round(profile.pitchHz) + "," +
                "\"rms\":" + round(profile.rms) + "," +
                "\"brightness\":" + round(profile.brightness) + "," +
                "\"maleRatio\":" + round(profile.maleRatio) + "," +
                "\"genderHint\":\"" + escapeJson(profile.genderHint()) + "\"," +
                "\"sourceAudio\":\"" + escapeJson(sourceAudio) + "\"" +
                "}";
    }

    public static String extractGenderHint(String json) {
        if (json == null || json.isBlank()) {
            return "NotSet";
        }
        // 先尝试直接读已缓存的 genderHint
        Pattern p = Pattern.compile("\\\"genderHint\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String value = m.group(1);
            if ("Male".equalsIgnoreCase(value)) {
                return "Male";
            }
            if ("Female".equalsIgnoreCase(value)) {
                return "Female";
            }
        }
        // 未缓存则用多特征重新计算
        AudioProfile profile = AudioProfile.fromJson(json);
        return profile.genderHint();
    }

    private static PcmAudio readAsPcm16Mono(Path path) throws Exception {
        try (AudioInputStream input = AudioSystem.getAudioInputStream(path.toFile())) {
            return readStreamAsPcm16Mono(input);
        }
    }

    private static PcmAudio readAsPcm16Mono(byte[] bytes) throws Exception {
        try (AudioInputStream input = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
            return readStreamAsPcm16Mono(input);
        }
    }

    private static PcmAudio readStreamAsPcm16Mono(AudioInputStream input) throws Exception {
        AudioFormat base = input.getFormat();
        AudioFormat target = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                Math.max(1, base.getChannels()),
                Math.max(1, base.getChannels()) * 2,
                base.getSampleRate(),
                false
        );
        try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(target, input)) {
            byte[] data = readAllBytes(pcmStream);
            int channels = Math.max(1, target.getChannels());
            int frameSize = channels * 2;
            int frames = data.length / frameSize;
            short[] mono = new short[frames];
            for (int i = 0; i < frames; i++) {
                int sum = 0;
                int offset = i * frameSize;
                for (int ch = 0; ch < channels; ch++) {
                    int idx = offset + ch * 2;
                    int lo = data[idx] & 0xff;
                    int hi = data[idx + 1];
                    sum += (short) ((hi << 8) | lo);
                }
                mono[i] = clampToShort(Math.round((float) sum / channels));
            }
            return new PcmAudio(Math.round(target.getSampleRate()), mono);
        }
    }

    private static byte[] readAllBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = stream.read(buffer)) >= 0) {
            if (n > 0) {
                out.write(buffer, 0, n);
            }
        }
        return out.toByteArray();
    }

    private static AudioProfile analyzePcm(PcmAudio pcm, String status) {
        if (pcm == null || pcm.samples == null || pcm.samples.length < 512 || pcm.sampleRate <= 0) {
            return AudioProfile.neutral("EMPTY_PCM");
        }
        short[] samples = pcm.samples;
        int limit = Math.min(samples.length, pcm.sampleRate * 20);

        double sumSquares = 0;
        int zeroCross = 0;
        short prev = samples[0];
        for (int i = 0; i < limit; i++) {
            double v = samples[i] / 32768.0;
            sumSquares += v * v;
            if (i > 0 && ((samples[i] >= 0 && prev < 0) || (samples[i] < 0 && prev >= 0))) {
                zeroCross++;
            }
            prev = samples[i];
        }
        double rms = Math.sqrt(sumSquares / Math.max(1, limit));
        double brightness = zeroCross / (double) Math.max(1, limit);
        PitchResult pitchResult = estimatePitchHz(samples, pcm.sampleRate, limit);
        boolean valid = pitchResult.pitchHz > 50 || rms > 0.005;
        return new AudioProfile(valid, pitchResult.pitchHz, rms, brightness, pitchResult.maleRatio, status);
    }

    private static PitchResult estimatePitchHz(short[] samples, int sampleRate, int limit) {
        int frame = Math.min(4096, Math.max(1024, Integer.highestOneBit(sampleRate / 8)));
        int hop = frame / 2;
        int minLag = Math.max(1, sampleRate / 360);
        int maxLag = Math.max(minLag + 1, sampleRate / 70);
        List<Double> pitches = new ArrayList<>();
        int maleFrames = 0;

        for (int start = 0; start + frame < limit && pitches.size() < 80; start += hop) {
            double energy = 0;
            for (int i = 0; i < frame; i++) {
                double v = samples[start + i] / 32768.0;
                energy += v * v;
            }
            double rms = Math.sqrt(energy / frame);
            if (rms < 0.01) {
                continue;
            }

            double bestCorr = 0;
            int bestLag = 0;
            for (int lag = minLag; lag <= maxLag && lag < frame; lag++) {
                double sum = 0;
                double a2 = 0;
                double b2 = 0;
                for (int i = 0; i < frame - lag; i++) {
                    double a = samples[start + i] / 32768.0;
                    double b = samples[start + i + lag] / 32768.0;
                    sum += a * b;
                    a2 += a * a;
                    b2 += b * b;
                }
                double corr = sum / Math.sqrt(Math.max(1e-9, a2 * b2));
                if (corr > bestCorr) {
                    bestCorr = corr;
                    bestLag = lag;
                }
            }
            if (bestLag > 0 && bestCorr > 0.32) {
                double hz = sampleRate / (double) bestLag;
                if (hz >= 70 && hz <= 360) {
                    pitches.add(hz);
                    if (hz < 165) maleFrames++;
                }
            }
        }

        if (pitches.isEmpty()) {
            return new PitchResult(0, 0);
        }
        Collections.sort(pitches);
        double pitch25 = pitches.get(Math.max(0, pitches.size() / 4));
        double maleRatio = pitches.size() > 0 ? (double) maleFrames / pitches.size() : 0;
        return new PitchResult(pitch25, maleRatio);
    }

    public static final class PitchResult {
        public final double pitchHz;
        public final double maleRatio;

        public PitchResult(double pitchHz, double maleRatio) {
            this.pitchHz = pitchHz;
            this.maleRatio = maleRatio;
        }
    }

    private static short[] resampleForPitch(short[] input, double factor) {
        if (input == null || input.length < 2 || Math.abs(factor - 1.0) < 0.01) {
            return input;
        }
        int newLength = Math.max(2, (int) Math.round(input.length / factor));
        short[] output = new short[newLength];
        for (int i = 0; i < newLength; i++) {
            double srcIndex = i * factor;
            int left = (int) Math.floor(srcIndex);
            if (left >= input.length - 1) {
                output[i] = input[input.length - 1];
                continue;
            }
            int right = left + 1;
            double frac = srcIndex - left;
            double value = input[left] * (1.0 - frac) + input[right] * frac;
            output[i] = clampToShort(Math.round(value));
        }
        return output;
    }

    private static short[] matchRms(short[] samples, double targetRms) {
        if (samples == null || samples.length == 0 || targetRms <= 0) {
            return samples;
        }
        double sumSquares = 0;
        for (short sample : samples) {
            double v = sample / 32768.0;
            sumSquares += v * v;
        }
        double current = Math.sqrt(sumSquares / samples.length);
        if (current <= 0.0001) {
            return samples;
        }
        double gain = clamp(targetRms / current, 0.72, 1.35);
        short[] out = new short[samples.length];
        for (int i = 0; i < samples.length; i++) {
            out[i] = clampToShort(Math.round(samples[i] * gain));
        }
        return out;
    }

    private static short[] matchBrightness(short[] samples, int sampleRate, double targetBrightness, double generatedBrightness) {
        if (samples == null || samples.length < 3 || targetBrightness <= 0 || generatedBrightness <= 0) {
            return samples;
        }
        double ratio = targetBrightness / generatedBrightness;
        short[] out = new short[samples.length];
        out[0] = samples[0];

        if (ratio < 0.86) {
            double strength = clamp((1.0 - ratio) * 0.7, 0.05, 0.45);
            double prev = samples[0];
            for (int i = 1; i < samples.length; i++) {
                double value = prev * strength + samples[i] * (1.0 - strength);
                out[i] = clampToShort(Math.round(value));
                prev = value;
            }
            return out;
        }

        if (ratio > 1.16) {
            double strength = clamp((ratio - 1.0) * 0.25, 0.03, 0.28);
            for (int i = 1; i < samples.length; i++) {
                double value = samples[i] + strength * (samples[i] - samples[i - 1]);
                out[i] = clampToShort(Math.round(value));
            }
            return out;
        }

        return samples;
    }

    private static byte[] writeMono16Wav(int sampleRate, short[] samples) throws IOException {
        if (sampleRate <= 0) {
            sampleRate = 16000;
        }
        if (samples == null) {
            samples = new short[0];
        }
        int dataSize = samples.length * 2;
        int byteRate = sampleRate * 2;
        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize);
        writeAscii(out, "RIFF");
        writeIntLE(out, 36 + dataSize);
        writeAscii(out, "WAVE");
        writeAscii(out, "fmt ");
        writeIntLE(out, 16);
        writeShortLE(out, 1);
        writeShortLE(out, 1);
        writeIntLE(out, sampleRate);
        writeIntLE(out, byteRate);
        writeShortLE(out, 2);
        writeShortLE(out, 16);
        writeAscii(out, "data");
        writeIntLE(out, dataSize);
        for (short sample : samples) {
            writeShortLE(out, sample);
        }
        return out.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
    }

    private static void writeIntLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xff);
        out.write((value >>> 8) & 0xff);
        out.write((value >>> 16) & 0xff);
        out.write((value >>> 24) & 0xff);
    }

    private static void writeShortLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xff);
        out.write((value >>> 8) & 0xff);
    }

    private static short clampToShort(long value) {
        if (value > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        if (value < Short.MIN_VALUE) {
            return Short.MIN_VALUE;
        }
        return (short) value;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String round(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return "0";
        }
        return String.format(Locale.US, "%.6f", v);
    }

    private static Double extractDouble(String json, String key) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return null;
        }
        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean extractBoolean(String json, String key) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return null;
        }
        return Boolean.parseBoolean(m.group(1));
    }

    public static final class PcmAudio {
        public final int sampleRate;
        public final short[] samples;

        private PcmAudio(int sampleRate, short[] samples) {
            this.sampleRate = sampleRate;
            this.samples = samples;
        }
    }

    public static final class AudioProfile {
        public final boolean valid;
        public final double pitchHz;
        public final double rms;
        public final double brightness;
        /** 男声音高帧占比 (< 165Hz 的帧 / 总有效帧)，0~1 */
        public final double maleRatio;
        public final String status;

        public AudioProfile(boolean valid, double pitchHz, double rms, double brightness, double maleRatio, String status) {
            this.valid = valid;
            this.pitchHz = pitchHz;
            this.rms = rms;
            this.brightness = brightness;
            this.maleRatio = maleRatio;
            this.status = status == null ? "OK" : status;
        }

        /** 兼容旧代码的构造函数（maleRatio=0） */
        public AudioProfile(boolean valid, double pitchHz, double rms, double brightness, String status) {
            this(valid, pitchHz, rms, brightness, 0, status);
        }

        public static AudioProfile neutral(String status) {
            return new AudioProfile(false, 0, 0, 0, 0, status);
        }

        public static AudioProfile fromJson(String json) {
            Boolean valid = extractBoolean(json, "valid");
            Double pitch = extractDouble(json, "pitchHz");
            Double rms = extractDouble(json, "rms");
            Double brightness = extractDouble(json, "brightness");
            Double maleRatio = extractDouble(json, "maleRatio");
            if (valid == null && pitch == null && rms == null && brightness == null && maleRatio == null) {
                return neutral("NO_PROFILE");
            }
            return new AudioProfile(
                    valid == null || valid,
                    pitch == null ? 0 : pitch,
                    rms == null ? 0 : rms,
                    brightness == null ? 0 : brightness,
                    maleRatio == null ? 0 : maleRatio,
                    "FROM_JSON"
            );
        }

        /**
         * 多特征联合性别判定：
         * 1. maleRatio >= 0.5 → 多数帧为男声音高 → Male
         * 2. maleRatio <= 0.3 → 多数帧为女声音高 → Female
         * 3. 中间区用 p25 兜底：p25Hz < 150 → Male，否则 Female
         */
        public String genderHint() {
            if (pitchHz <= 0) {
                return "NotSet";
            }
            if (maleRatio >= 0.5) return "Male";
            if (maleRatio <= 0.3) return "Female";
            // borderline: 低音底线低于 150Hz 偏向男声
            return pitchHz < 150 ? "Male" : "Female";
        }
    }
}
