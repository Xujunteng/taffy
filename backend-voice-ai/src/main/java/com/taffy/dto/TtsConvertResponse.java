package com.taffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * TTS合成响应
 */
@Data
@AllArgsConstructor
public class TtsConvertResponse {
    private Long taskId;
    private String status;
    private String audioOutputPath;
    private String audioUrl;
    /** TTS 供应商: "aliyun" / "edge-tts" */
    private String provider;
    /** 供应商备注，如 "阿里云TTS不可用，已使用本地TTS兜底" */
    private String providerNote;
}
