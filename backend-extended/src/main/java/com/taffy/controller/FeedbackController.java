package com.taffy.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }

    @PostMapping
    public Map<String, Object> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "评价提交成功");
        return result;
    }

    @GetMapping("/rating/{voiceModelId}")
    public Map<String, Object> getRating(@PathVariable Long voiceModelId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", Map.of("avgRating", 4.5, "count", 10));
        return result;
    }
}