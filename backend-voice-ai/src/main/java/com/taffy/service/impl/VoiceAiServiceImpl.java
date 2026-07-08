package com.taffy.service.impl;

import com.taffy.config.VoiceAiProperties;
import com.taffy.dto.*;
import com.taffy.entity.TtsTask;
import com.taffy.entity.VoiceModel;
import com.taffy.mapper.TtsTaskMapper;
import com.taffy.mapper.VoiceModelMapper;
import com.taffy.service.TtsProvider;
import com.taffy.service.VoiceAiService;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 语音AI核心服务实现
 */
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
    public VoiceTrainResponse trainVoice(Long userId, VoiceTrainRequest request) {
        // 查找已有的声音模型
        VoiceModel model = voiceModelMapper.findById(request.getVoiceModelId());
        if (model == null) throw new IllegalArgumentException("声音模型不存在");
        if (!model.getUserId().equals(userId)) throw new IllegalArgumentException("无权操作该声音模型");

        // 更新状态为就绪（演示模式，真实环境需对接阿里云训练API）
        model.setStatus("就绪");
        model.setModelParams("{\"provider\":\"aliyun-or-mock\",\"sourceAudio\":\"" + model.getAudioFilePath() + "\"}");
        voiceModelMapper.updateStatus(model);

        return new VoiceTrainResponse(model.getId(), model.getStatus(), "训练完成（演示模式）");
    }

    @Override
    public TtsConvertResponse convertTextToSpeech(Long userId, TtsConvertRequest request) throws Exception {
        VoiceModel model = null;
        if (request.getVoiceModelId() != null) {
            model = voiceModelMapper.findById(request.getVoiceModelId());
            if (model == null) throw new IllegalArgumentException("声音模型不存在");
            if (!"就绪".equals(model.getStatus())) throw new IllegalStateException("声音模型尚未就绪，请先训练");
        }

        TtsTask task = new TtsTask();
        task.setUserId(userId);
        task.setVoiceModelId(request.getVoiceModelId());
        task.setTextContent(request.getText());
        task.setStatus("PROCESSING");
        ttsTaskMapper.insert(task);

        byte[] audio = ttsProvider.synthesize(request, model == null ? "{}" : model.getModelParams());
        Path dir = Path.of(properties.getStorageRoot(), String.valueOf(userId), "tts");
        if (!dir.isAbsolute()) {
            dir = Path.of(System.getProperty("user.dir")).resolve(dir).toAbsolutePath().normalize();
        }
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
