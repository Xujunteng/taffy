package com.taffy.controller;

import com.taffy.common.Result;
import com.taffy.entity.VoiceModel;
import com.taffy.service.VoiceModelService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voices")
public class VoiceController {

    @Autowired
    private VoiceModelService voiceModelService;

    @GetMapping
    public Result<List<VoiceModel>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(voiceModelService.getVoiceList(userId));
    }

    @GetMapping("/{id}")
    public Result<VoiceModel> getById(@PathVariable Long id) {
        VoiceModel voice = voiceModelService.getVoiceById(id);
        if (voice == null) {
            return Result.error(404, "声音模型不存在");
        }
        return Result.success(voice);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody VoiceModel voice) {
        voice.setId(id);
        voiceModelService.updateVoice(voice);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        voiceModelService.deleteVoice(id);
        return Result.success(null);
    }
}