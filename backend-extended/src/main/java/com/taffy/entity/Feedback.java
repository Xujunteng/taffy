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
    /** 是否公开姓名，默认 true */
    private Boolean showName;
    /** 评价人用户名（来自 users 表 JOIN，非 DB 列） */
    private String username;
    /** 声音模型名称（来自 voice_models 表 JOIN，非 DB 列） */
    private String modelName;
    private LocalDateTime createdAt;
}
