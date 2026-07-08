package com.taffy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 声音训练请求 — userId由拦截器注入，不从body传入
 */
@Data
public class VoiceTrainRequest {

    @NotBlank(message = "name不能为空")
    private String name;

    private String description;

    @NotBlank(message = "audioFilePath不能为空")
    private String audioFilePath;
}
