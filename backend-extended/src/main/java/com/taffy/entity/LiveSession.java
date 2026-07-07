package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LiveSession {
    private Long id;
    private Long userId;
    private Long scriptId;
    private String platform;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String statsData;
}
