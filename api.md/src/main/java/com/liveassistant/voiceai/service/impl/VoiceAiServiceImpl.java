package com.liveassistant.voiceai.service.impl;

import com.liveassistant.voiceai.config.VoiceAiProperties;
import com.liveassistant.voiceai.dto.*;
import com.liveassistant.voiceai.entity.TtsTask;
import com.liveassistant.voiceai.entity.VoiceModel;
import com.liveassistant.voiceai.mapper.TtsTaskMapper;
import com.liveassistant.voiceai.mapper.VoiceModelMapper;
import com.liveassistant.voiceai.service.TtsProvider;
import com.liveassistant.voiceai.service.VoiceAiService;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class VoiceAiServiceImpl implements VoiceAiService {
    private final VoiceModelMapper voiceModelMapper;
    private final TtsTaskMapper ttsTaskMapper;
    private final TtsProvider ttsProvider;
    private final VoiceAiProperties properties;

    public VoiceAiServiceImpl(VoiceModelMapper voiceModelMapper, TtsTaskMapper ttsTaskMapper,
                              TtsProvider ttsProvider, VoiceAiProperties properties) {
        this.voiceModelMapper = voiceModelMapper;
        this.ttsTaskMapper = ttsTaskMapper;
        this.ttsProvider = ttsProvider;
        this.properties = properties;
    }

    @Override
    public VoiceTrainResponse trainVoice(VoiceTrainRequest request) {
        VoiceModel model = new VoiceModel();
        model.setUserId(request.getUserId());
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setAudioFilePath(request.getAudioFilePath());
        model.setStatus("READY");
        model.setModelParams("{\"provider\":\"aliyun-or-mock\",\"sourceAudio\":\"" + request.getAudioFilePath() + "\"}");
        voiceModelMapper.insert(model);
        return new VoiceTrainResponse(model.getId(), model.getStatus(), "声音训练任务已创建，当前演示模式直接置为READY");
    }

    @Override
    public TtsConvertResponse convertTextToSpeech(TtsConvertRequest request) throws Exception {
        VoiceModel model = null;
        if (request.getVoiceModelId() != null) {
            model = voiceModelMapper.findById(request.getVoiceModelId());
            if (model == null) throw new IllegalArgumentException("声音模型不存在");
            if (!"READY".equalsIgnoreCase(model.getStatus())) throw new IllegalStateException("声音模型尚未就绪");
        }

        TtsTask task = new TtsTask();
        task.setUserId(request.getUserId());
        task.setVoiceModelId(request.getVoiceModelId());
        task.setTextContent(request.getText());
        task.setStatus("PROCESSING");
        ttsTaskMapper.insert(task);

        byte[] audio = ttsProvider.synthesize(request, model == null ? "{}" : model.getModelParams());
        Path dir = Path.of(properties.getStorageRoot(), String.valueOf(request.getUserId()), "tts");
        Files.createDirectories(dir);
        Path output = dir.resolve("tts-" + task.getId() + "-" + UUID.randomUUID() + ".wav");
        Files.write(output, audio);

        ttsTaskMapper.updateStatusAndOutput(task.getId(), "SUCCESS", output.toString());
        return new TtsConvertResponse(task.getId(), "SUCCESS", output.toString(), "/api/tts/audio/" + task.getId());
    }

    @Override
    public TtsTask getTaskStatus(Long taskId) {
        TtsTask task = ttsTaskMapper.findById(taskId);
        if (task == null) throw new IllegalArgumentException("TTS任务不存在");
        return task;
    }
}
