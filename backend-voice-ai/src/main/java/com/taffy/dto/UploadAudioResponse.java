package com.taffy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 音频上传响应
 */
@Data
@AllArgsConstructor
public class UploadAudioResponse {
    private String filePath;
    private String fileName;
    private long size;
}
