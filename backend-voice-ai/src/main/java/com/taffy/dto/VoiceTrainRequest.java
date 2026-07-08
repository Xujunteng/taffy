package com.taffy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 声音训练请求 — 传入已创建的声音模型ID开始训练
 */
@Data
public class VoiceTrainRequest {

    @NotNull(message = "voiceModelId不能为空")
    private Long voiceModelId;
}
