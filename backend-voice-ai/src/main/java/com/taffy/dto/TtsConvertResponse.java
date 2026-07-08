package com.taffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TTS合成响应
 */
@Data
@AllArgsConstructor
public class TtsConvertResponse {
    private Long taskId;
    private String status;
    private String audioOutputPath;
    private String audioUrl;
}
