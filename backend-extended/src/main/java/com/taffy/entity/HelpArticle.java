package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HelpArticle {
    private Long id;
    private String title;
    private String content;
    private String category;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
