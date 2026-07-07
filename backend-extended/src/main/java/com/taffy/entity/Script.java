package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Script {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
