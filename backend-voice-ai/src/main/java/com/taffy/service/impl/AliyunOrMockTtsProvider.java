package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.dto.TtsConvertRequest;
import com.taffy.service.TtsProvider;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 阿里云TTS实现 — 优先使用真实API，未配置时使用Mock占位
 */
@Service
public class AliyunOrMockTtsProvider implements TtsProvider {
    private final VoiceAiProperties properties;

    public AliyunOrMockTtsProvider(VoiceAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public byte[] synthesize(TtsConvertRequest request, String voiceParams) {
        if (!properties.getAliyun().isEnabled()) {
            String mock = "MOCK_AUDIO_FOR_TEXT=" + request.getText() + "\nVOICE_PARAMS=" + voiceParams;
            return mock.getBytes(StandardCharsets.UTF_8);
        }

        // TODO: 接入阿里云智能语音交互/语音合成 SDK 或 REST API。
        // 当前保留可运行占位实现，方便无密钥环境下完成联调和课程演示。
        // 开启 voice-ai.aliyun.enabled=true 后，可用 appKey/accessKey 调用真实 TTS。
        String placeholder = "ALIYUN_TTS_PLACEHOLDER=" + request.getText();
        return placeholder.getBytes(StandardCharsets.UTF_8);
    }
}
