package com.taffy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * TTS合成请求 — userId由拦截器注入，不从body传入
 */
@Data
public class TtsConvertRequest {

    /** 声音模型ID（可选，不传则使用默认声音） */
    private Long voiceModelId;

    @NotBlank(message = "text不能为空")
    @Size(max = 2000, message = "文本长度不能超过2000字")
    private String text;

    /** 语言，默认中文普通话 */
    private String language = "zh-CN";

    /** 语速，1.0为正常 */
    private Double speed = 1.0;

    /** 音调，1.0为正常 */
    private Double pitch = 1.0;
}
