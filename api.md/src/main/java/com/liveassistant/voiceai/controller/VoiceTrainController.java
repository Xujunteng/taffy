package com.liveassistant.voiceai.controller;

import com.liveassistant.voiceai.common.ApiResponse;
import com.liveassistant.voiceai.dto.VoiceTrainRequest;
import com.liveassistant.voiceai.dto.VoiceTrainResponse;
import com.liveassistant.voiceai.service.VoiceAiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/voice")
@CrossOrigin
public class VoiceTrainController {
    private final VoiceAiService voiceAiService;

    public VoiceTrainController(VoiceAiService voiceAiService) {
        this.voiceAiService = voiceAiService;
    }

    @PostMapping("/train")
    public ApiResponse<VoiceTrainResponse> train(@Valid @RequestBody VoiceTrainRequest request) {
        return ApiResponse.ok(voiceAiService.trainVoice(request));
    }
}
