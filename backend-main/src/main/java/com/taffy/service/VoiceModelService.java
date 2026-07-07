package com.taffy.service;

import com.taffy.entity.VoiceModel;
import com.taffy.mapper.VoiceModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoiceModelService {

    private static final Logger log = LoggerFactory.getLogger(VoiceModelService.class);

    @Autowired
    private VoiceModelMapper voiceModelMapper;

    @Value("${app.file.upload-dir:./data/audio}")
    private String uploadDir;

    public List<VoiceModel> getVoiceList(Long userId) {
        return voiceModelMapper.findByUserId(userId);
    }

    public VoiceModel getVoiceById(Long id) {
        return voiceModelMapper.findById(id);
    }

    /**
     * 验证声音模型是否属于指定用户
     */
    public boolean belongsToUser(Long voiceId, Long userId) {
        VoiceModel voice = voiceModelMapper.findById(voiceId);
        return voice != null && voice.getUserId().equals(userId);
    }

    public VoiceModel createVoice(VoiceModel voice) {
        voice.setCreatedAt(LocalDateTime.now());
        voice.setStatus("训练中");
        voiceModelMapper.insert(voice);
        return voice;
    }

    public void updateVoice(VoiceModel voice) {
        voiceModelMapper.update(voice);
    }

    /**
     * 删除声音模型，同时删除关联的音频文件
     */
    public void deleteVoice(Long id) {
        VoiceModel voice = voiceModelMapper.findById(id);
        if (voice != null && voice.getAudioFilePath() != null) {
            // 删除音频文件
            String filePath = voice.getAudioFilePath();
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("已删除音频文件: {}", filePath);
                } else {
                    log.warn("音频文件删除失败: {}", filePath);
                }
            }
        }
        voiceModelMapper.delete(id);
    }
}
