package com.taffy.controller;

import com.taffy.dto.TtsConvertRequest;
import com.taffy.dto.TtsConvertResponse;
import com.taffy.entity.TtsTask;
import com.taffy.mapper.TtsTaskMapper;
import com.taffy.service.VoiceAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TTS语音合成控制器
 */
@RestController
@RequestMapping("/api/tts")
public class TTSController {

    @Autowired
    private VoiceAiService voiceAiService;

    @Autowired
    private TtsTaskMapper ttsTaskMapper;

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
            result.put("message", safeMessage(e, "TTS合成失败"));
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
            result.put("message", "success");
            result.put("data", task);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", safeMessage(e, "查询TTS任务失败"));
        }
        return result;
    }

    /**
     * 当前用户的TTS历史记录
     */
    @GetMapping("/history")
    public Map<String, Object> history(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        try {
            List<TtsTask> tasks = ttsTaskMapper.findByUserId(userId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", tasks);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", safeMessage(e, "查询TTS历史失败"));
        }
        return result;
    }

    /**
     * 播放TTS合成的音频文件（免登录，供audio标签直接访问）
     */
    @GetMapping("/audio/{taskId}")
    public ResponseEntity<FileSystemResource> audio(@PathVariable Long taskId) {
        return buildAudioResponse(taskId, false);
    }

    /**
     * 下载TTS合成的音频文件
     */
    @GetMapping("/download/{taskId}")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long taskId) {
        return buildAudioResponse(taskId, true);
    }

    private ResponseEntity<FileSystemResource> buildAudioResponse(Long taskId, boolean attachment) {
        TtsTask task = voiceAiService.getTaskStatus(taskId);
        if (task.getAudioOutputPath() == null || task.getAudioOutputPath().isBlank()) {
            String msg = "failed".equals(task.getStatus()) ? "TTS合成失败，音频文件未生成" : "音频文件尚未生成";
            throw new IllegalStateException(msg);
        }
        File file = new File(task.getAudioOutputPath());
        if (!file.exists() || !file.isFile()) {
            throw new IllegalStateException("音频文件不存在");
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .contentLength(file.length());
        if (attachment) {
            builder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tts-output-" + taskId + ".wav");
        }
        return builder.body(new FileSystemResource(file));
    }

    private String safeMessage(Exception e, String fallback) {
        return e.getMessage() == null || e.getMessage().isBlank() ? fallback : e.getMessage();
    }
}
