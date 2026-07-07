package com.liveassistant.voiceai.entity;

import lombok.Data;

@Data
public class VoiceModel {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String status;
    private String audioFilePath;
    private String modelParams;
    private String createdAt;
}
