package com.liveassistant.voiceai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TtsConvertResponse {
    private Long taskId;
    private String status;
    private String audioOutputPath;
    private String audioUrl;
}
