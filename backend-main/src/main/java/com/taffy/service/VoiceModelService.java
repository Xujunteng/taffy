package com.taffy.service;

import com.taffy.entity.VoiceModel;
import com.taffy.mapper.VoiceModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoiceModelService {

    @Autowired
    private VoiceModelMapper voiceModelMapper;

    public List<VoiceModel> getVoiceList(Long userId) {
        return voiceModelMapper.findByUserId(userId);
    }

    public VoiceModel getVoiceById(Long id) {
        return voiceModelMapper.findById(id);
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

    public void deleteVoice(Long id) {
        voiceModelMapper.delete(id);
    }
}