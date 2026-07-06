package com.taffy.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @GetMapping("/live")
    public Map<String, Object> getLiveStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> data = new HashMap<>();
        data.put("voiceCount", 0);
        data.put("ttsCount", 0);
        data.put("scriptCount", 0);
        data.put("liveCount", 0);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    @GetMapping("/sessions")
    public Map<String, Object> getSessions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", new ArrayList<>());
        return result;
    }
}