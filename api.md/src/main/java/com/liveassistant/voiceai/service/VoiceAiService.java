package com.liveassistant.voiceai.service;

import com.liveassistant.voiceai.dto.*;
import com.liveassistant.voiceai.entity.TtsTask;

public interface VoiceAiService {
    VoiceTrainResponse trainVoice(VoiceTrainRequest request);
    TtsConvertResponse convertTextToSpeech(TtsConvertRequest request) throws Exception;
    TtsTask getTaskStatus(Long taskId);
}
