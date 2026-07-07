package com.liveassistant.voiceai.controller;

import com.liveassistant.voiceai.common.ApiResponse;
import com.liveassistant.voiceai.dto.UploadAudioResponse;
import com.liveassistant.voiceai.service.AudioStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin
public class AudioController {
    private final AudioStorageService audioStorageService;

    public AudioController(AudioStorageService audioStorageService) {
        this.audioStorageService = audioStorageService;
    }

    @PostMapping("/upload")
    public ApiResponse<UploadAudioResponse> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("userId") Long userId) throws Exception {
        String path = audioStorageService.saveAudio(file, userId);
        return ApiResponse.ok(new UploadAudioResponse(path, file.getOriginalFilename(), file.getSize()));
    }
}
