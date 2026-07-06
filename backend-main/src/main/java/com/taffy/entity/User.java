package com.taffy.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}