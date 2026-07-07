package com.liveassistant.voiceai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoiceTrainResponse {
    private Long voiceModelId;
    private String status;
    private String message;
}
