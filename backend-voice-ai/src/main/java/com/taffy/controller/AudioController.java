package com.taffy.controller;

import com.taffy.service.AudioStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 音频上传控制器
 */
@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AudioStorageService audioStorageService;

    /**
     * 上传音频文件
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        try {
            String path = audioStorageService.saveAudio(file, userId);
            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("filePath", path);
            data.put("fileName", file.getOriginalFilename());
            data.put("size", file.getSize());
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
