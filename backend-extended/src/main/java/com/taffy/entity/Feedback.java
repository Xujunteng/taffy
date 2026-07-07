package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;
    private Long userId;
    private Long voiceModelId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
