package com.taffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 声音训练响应
 */
@Data
@AllArgsConstructor
public class VoiceTrainResponse {
    private Long voiceModelId;
    private String status;
    private String message;
}
