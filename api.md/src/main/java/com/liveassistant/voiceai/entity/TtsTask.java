package com.liveassistant.voiceai.entity;

import lombok.Data;

@Data
public class TtsTask {
    private Long id;
    private Long userId;
    private Long voiceModelId;
    private String textContent;
    private String audioOutputPath;
    private String status;
    private String createdAt;
}
