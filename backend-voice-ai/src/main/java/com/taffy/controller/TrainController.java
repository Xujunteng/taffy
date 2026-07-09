package com.taffy.controller;

import com.taffy.dto.VoiceTrainRequest;
import com.taffy.dto.VoiceTrainResponse;
import com.taffy.entity.VoiceModel;
import com.taffy.mapper.VoiceModelMapper;
import com.taffy.service.VoiceAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 声音训练控制器
 */
@RestController
@RequestMapping("/api/voice")
public class TrainController {

    @Autowired
    private VoiceAiService voiceAiService;

    @Autowired
    private VoiceModelMapper voiceModelMapper;

    /**
     * 提交声音训练任务
     */
    @PostMapping("/train")
    public Map<String, Object> train(@Valid @RequestBody VoiceTrainRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        try {
            VoiceTrainResponse response = voiceAiService.trainVoice(userId, request);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", response);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage() == null || e.getMessage().isBlank() ? "服务器内部错误，请稍后再试" : e.getMessage());
        }
        return result;
    }

    /**
     * 查询训练状态 — 训练完成后直接查声音模型状态即可
     */
    @GetMapping("/train/status/{voiceModelId}")
    public Map<String, Object> trainStatus(@PathVariable Long voiceModelId) {
        Map<String, Object> result = new HashMap<>();
        try {
            VoiceModel model = voiceModelMapper.findById(voiceModelId);
            if (model == null) {
                result.put("code", 404);
                result.put("message", "声音模型不存在");
                return result;
            }
            result.put("code", 200);
            result.put("message", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("status", model.getStatus());
            data.put("voiceModelId", model.getId());
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage() == null || e.getMessage().isBlank() ? "服务器内部错误" : e.getMessage());
        }
        return result;
    }
}
