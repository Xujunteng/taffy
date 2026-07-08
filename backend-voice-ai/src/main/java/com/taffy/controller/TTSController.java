package com.taffy.controller;

import com.taffy.dto.TtsConvertRequest;
import com.taffy.dto.TtsConvertResponse;
import com.taffy.entity.TtsTask;
import com.taffy.service.VoiceAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TTS语音合成控制器
 */
@RestController
@RequestMapping("/api/tts")
public class TTSController {

    @Autowired
    private VoiceAiService voiceAiService;

    /**
     * 文本转语音合成
     */
    @PostMapping("/convert")
    public Map<String, Object> convert(@Valid @RequestBody TtsConvertRequest ttsRequest,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        try {
            TtsConvertResponse response = voiceAiService.convertTextToSpeech(userId, ttsRequest);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", response);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 查询TTS任务状态
     */
    @GetMapping("/status/{taskId}")
    public Map<String, Object> status(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TtsTask task = voiceAiService.getTaskStatus(taskId);
            result.put("code", 200);
            result.put("data", task);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 下载TTS合成的音频文件（免登录）
     */
    @GetMapping("/audio/{taskId}")
    public ResponseEntity<FileSystemResource> audio(@PathVariable Long taskId) {
        TtsTask task = voiceAiService.getTaskStatus(taskId);
        File file = new File(task.getAudioOutputPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
