package com.taffy.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voice")
public class TrainController {

    @PostMapping("/train")
    public Map<String, Object> train(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long voiceModelId = Long.valueOf(body.get("voiceModelId").toString());
        // TODO: 对接阿里云声音训练API
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "训练任务已提交");
        result.put("voiceModelId", voiceModelId);
        return result;
    }

    @GetMapping("/train/status/{taskId}")
    public Map<String, Object> getTrainStatus(@PathVariable Long taskId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("status", "training");
        return result;
    }
}