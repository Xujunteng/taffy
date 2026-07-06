package com.taffy.service;

import com.taffy.entity.VoiceModel;
import com.taffy.mapper.VoiceModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AudioService {

    @Autowired
    private VoiceModelMapper voiceModelMapper;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    public VoiceModel uploadAndCreate(MultipartFile file, String name, String description, Long userId) {
        try {
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            // 创建声音模型记录
            VoiceModel voice = new VoiceModel();
            voice.setUserId(userId);
            voice.setName(name);
            voice.setDescription(description);
            voice.setAudioFilePath(filePath.toString());
            voice.setStatus("训练中");
            voice.setCreatedAt(LocalDateTime.now());
            voiceModelMapper.insert(voice);
            return voice;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}