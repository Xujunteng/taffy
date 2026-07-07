package com.liveassistant.voiceai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoiceTrainRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;

    @NotBlank(message = "name不能为空")
    private String name;

    private String description;

    @NotBlank(message = "audioFilePath不能为空")
    private String audioFilePath;
}
