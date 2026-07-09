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
     * @return 合成结果（包含音频数据和供应商信息）
     */
    TtsResult synthesize(TtsConvertRequest request, String voiceParams) throws Exception;
}
