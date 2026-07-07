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

    /**
     * 获取当前用户的声音模型列表
     */
    @GetMapping
    public Result<List<VoiceModel>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(voiceModelService.getVoiceList(userId));
    }

    /**
     * 获取单个声音模型详情
     * 需要验证所有权
     */
    @GetMapping("/{id}")
    public Result<VoiceModel> getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        VoiceModel voice = voiceModelService.getVoiceById(id);
        if (voice == null) {
            return Result.error(404, "声音模型不存在");
        }
        // 验证所有权
        if (!voice.getUserId().equals(userId)) {
            return Result.error(403, "无权访问该声音模型");
        }
        return Result.success(voice);
    }

    /**
     * 更新声音模型信息（名称、描述、状态）
     * 需要验证所有权
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody VoiceModel voice, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        VoiceModel existing = voiceModelService.getVoiceById(id);
        if (existing == null) {
            return Result.error(404, "声音模型不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error(403, "无权修改该声音模型");
        }
        voice.setId(id);
        voiceModelService.updateVoice(voice);
        return Result.success(null);
    }

    /**
     * 删除声音模型（同时删除关联音频文件）
     * 需要验证所有权
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        VoiceModel voice = voiceModelService.getVoiceById(id);
        if (voice == null) {
            return Result.error(404, "声音模型不存在");
        }
        if (!voice.getUserId().equals(userId)) {
            return Result.error(403, "无权删除该声音模型");
        }
        voiceModelService.deleteVoice(id);
        return Result.success(null);
    }
}
