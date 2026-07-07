package com.liveassistant.voiceai.controller;

import com.liveassistant.voiceai.common.ApiResponse;
import com.liveassistant.voiceai.dto.TtsConvertRequest;
import com.liveassistant.voiceai.dto.TtsConvertResponse;
import com.liveassistant.voiceai.entity.TtsTask;
import com.liveassistant.voiceai.service.VoiceAiService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/tts")
@CrossOrigin
public class TtsController {
    private final VoiceAiService voiceAiService;

    public TtsController(VoiceAiService voiceAiService) {
        this.voiceAiService = voiceAiService;
    }

    @PostMapping("/convert")
    public ApiResponse<TtsConvertResponse> convert(@Valid @RequestBody TtsConvertRequest request) throws Exception {
        return ApiResponse.ok(voiceAiService.convertTextToSpeech(request));
    }

    @GetMapping("/status/{taskId}")
    public ApiResponse<TtsTask> status(@PathVariable Long taskId) {
        return ApiResponse.ok(voiceAiService.getTaskStatus(taskId));
    }

    @GetMapping("/audio/{taskId}")
    public ResponseEntity<FileSystemResource> audio(@PathVariable Long taskId) {
        TtsTask task = voiceAiService.getTaskStatus(taskId);
        File file = new File(task.getAudioOutputPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
