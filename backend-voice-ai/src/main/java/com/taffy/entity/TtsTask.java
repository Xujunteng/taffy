package com.taffy.entity;

import lombok.Data;

/**
 * TTS合成任务实体
 */
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
