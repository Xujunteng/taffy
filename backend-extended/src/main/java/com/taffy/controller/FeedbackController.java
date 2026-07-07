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

        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setVoiceModelId(body.get("voiceModelId") != null ?
                Long.valueOf(body.get("voiceModelId").toString()) : null);
        feedback.setRating(body.get("rating") != null ?
                Integer.valueOf(body.get("rating").toString()) : null);
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
