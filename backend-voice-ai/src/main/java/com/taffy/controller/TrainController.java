package com.taffy.controller;

import com.taffy.dto.VoiceTrainRequest;
import com.taffy.dto.VoiceTrainResponse;
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
            result.put("message", e.getMessage());
        }
        return result;
    }
}
