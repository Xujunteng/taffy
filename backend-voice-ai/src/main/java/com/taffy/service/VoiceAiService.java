package com.taffy.service;

import com.taffy.dto.*;
import com.taffy.entity.TtsTask;

/**
 * 语音AI核心服务接口
 * userId不再从Body取，由Controller通过JWT拦截器获取后传入
 */
public interface VoiceAiService {
    /** 创建声音训练任务 */
    VoiceTrainResponse trainVoice(Long userId, VoiceTrainRequest request);

    /** 文本转语音合成 */
    TtsConvertResponse convertTextToSpeech(Long userId, TtsConvertRequest request) throws Exception;

    /** 查询TTS任务状态 */
    TtsTask getTaskStatus(Long taskId);
}
