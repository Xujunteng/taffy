package com.taffy.controller;

import com.taffy.entity.VoiceModel;
import com.taffy.mapper.VoiceModelMapper;
import com.taffy.service.AudioStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频上传控制器 — 上传音频并创建声音模型记录
 */
@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AudioStorageService audioStorageService;

    @Autowired
    private VoiceModelMapper voiceModelMapper;

    /**
     * 上传音频文件，同时创建声音模型记录
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam("name") String name,
                                       @RequestParam(value = "description", required = false) String description,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        try {
            String path = audioStorageService.saveAudio(file, userId);

            // 创建声音模型记录
            VoiceModel voice = new VoiceModel();
            voice.setUserId(userId);
            voice.setName(name != null && !name.isEmpty() ? name : file.getOriginalFilename());
            voice.setDescription(description != null ? description : "");
            voice.setAudioFilePath(path);
            voice.setStatus("训练中");
            voice.setCreatedAt(LocalDateTime.now());
            voiceModelMapper.insert(voice);

            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("id", voice.getId());
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
