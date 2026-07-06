package com.taffy.controller;

import com.taffy.entity.VoiceModel;
import com.taffy.service.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AudioService audioService;

    @PostMapping("/upload")
    public Map<String, Object> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        VoiceModel voice = audioService.uploadAndCreate(file, name, description, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", voice);
        return result;
    }
}