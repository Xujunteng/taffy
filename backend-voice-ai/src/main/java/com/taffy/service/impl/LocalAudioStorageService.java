package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.service.AudioStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * 本地文件系统音频存储实现
 */
@Service
public class LocalAudioStorageService implements AudioStorageService {
    private static final Set<String> ALLOWED_EXT = Set.of("wav", "mp3", "m4a", "aac", "ogg");
    private final VoiceAiProperties properties;

    public LocalAudioStorageService(VoiceAiProperties properties) {
        this.properties = properties;
    }

    @Override
    public String saveAudio(MultipartFile file, Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }
        String original = file.getOriginalFilename() == null ? "audio.wav" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "wav";
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("仅支持 wav/mp3/m4a/aac/ogg 音频格式");
        }
        Path dir = Path.of(properties.getStorageRoot(), String.valueOf(userId), "uploads");
        // 相对路径时基于项目目录解析，避免被Tomcat临时目录影响
        if (!dir.isAbsolute()) {
            dir = Path.of(System.getProperty("user.dir")).resolve(dir).toAbsolutePath().normalize();
        }
        Files.createDirectories(dir);
        Path target = dir.resolve(UUID.randomUUID() + "." + ext);
        file.transferTo(target.toFile());
        return target.toString();
    }
}
