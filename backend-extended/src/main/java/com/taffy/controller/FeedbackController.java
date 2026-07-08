package com.taffy.controller;

import com.taffy.entity.Feedback;
import com.taffy.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Long voiceModelId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();
        List<Feedback> feedbacks = feedbackService.getFeedbackList(userId, voiceModelId);
        result.put("code", 200);
        result.put("data", feedbacks);
        return result;
    }

    @PostMapping
    public Map<String, Object> submit(@RequestBody Map<String, Object> body,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = new HashMap<>();

        // 参数校验
        if (body.get("voiceModelId") == null) {
            result.put("code", 400);
            result.put("message", "声音模型ID不能为空");
            return result;
        }
        Integer rating = body.get("rating") != null ?
                Integer.valueOf(body.get("rating").toString()) : null;
        if (rating == null || rating < 1 || rating > 5) {
            result.put("code", 400);
            result.put("message", "评分必须在1-5之间");
            return result;
        }

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setVoiceModelId(Long.valueOf(body.get("voiceModelId").toString()));
        feedback.setRating(rating);
        feedback.setComment((String) body.get("comment"));

        feedbackService.submitFeedback(feedback);
        result.put("code", 200);
        result.put("message", "评价提交成功");
        return result;
    }

    @GetMapping("/rating/{voiceModelId}")
    public Map<String, Object> getRating(@PathVariable Long voiceModelId) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> ratingData = feedbackService.getVoiceRating(voiceModelId);
        result.put("code", 200);
        result.put("data", ratingData);
        return result;
    }
}
