package com.taffy.service;

import com.taffy.dto.TtsConvertRequest;

/**
 * TTS语音合成抽象接口
 * 可切换阿里云真实TTS或Mock占位实现
 */
public interface TtsProvider {
    /**
     * 合成语音
     * @param request TTS合成请求参数
     * @param voiceParams 声音模型参数（JSON格式）
     * @return 合成后的音频字节数组
     */
    byte[] synthesize(TtsConvertRequest request, String voiceParams) throws Exception;
}
