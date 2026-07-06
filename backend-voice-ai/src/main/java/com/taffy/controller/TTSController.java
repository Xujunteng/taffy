package com.taffy.controller;

import com.taffy.service.TTSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tts")
public class TTSController {

    @Autowired
    private TTSService ttsService;

    @PostMapping("/convert")
    public Map<String, Object> convert(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long voiceModelId = Long.valueOf(body.get("voiceModelId").toString());
        String text = body.get("text").toString();
        double speed = body.get("speed") != null ? Double.parseDouble(body.get("speed").toString()) : 1.0;
        double pitch = body.get("pitch") != null ? Double.parseDouble(body.get("pitch").toString()) : 1.0;
        return ttsService.convert(userId, voiceModelId, text, speed, pitch);
    }

    @GetMapping("/status/{taskId}")
    public Map<String, Object> getStatus(@PathVariable Long taskId) {
        return ttsService.getTaskStatus(taskId);
    }

    @GetMapping("/history")
    public Map<String, Object> getHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ttsService.getHistory(userId);
    }
}