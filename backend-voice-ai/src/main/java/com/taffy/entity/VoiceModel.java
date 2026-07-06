package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoiceModel {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String status;
    private String audioFilePath;
    private String modelParams;
    private LocalDateTime createdAt;
}