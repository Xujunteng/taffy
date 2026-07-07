package com.liveassistant.voiceai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TtsConvertRequest {
    @NotNull(message = "userId不能为空")
    private Long userId;

    private Long voiceModelId;

    @NotBlank(message = "text不能为空")
    @Size(max = 2000, message = "文本长度不能超过2000字")
    private String text;

    private String language = "zh-CN";
    private Double speed = 1.0;
    private Double pitch = 1.0;
}
