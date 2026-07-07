package com.liveassistant.voiceai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadAudioResponse {
    private String filePath;
    private String fileName;
    private long size;
}
