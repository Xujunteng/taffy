package com.liveassistant.voiceai.service;

import com.liveassistant.voiceai.dto.TtsConvertRequest;

public interface TtsProvider {
    byte[] synthesize(TtsConvertRequest request, String voiceParams) throws Exception;
}
