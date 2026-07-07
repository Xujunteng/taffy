package com.taffy.service;

import com.taffy.entity.Feedback;
import com.taffy.mapper.FeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    public List<Feedback> getFeedbackList(Long userId, Long voiceModelId) {
        if (voiceModelId != null) {
            return feedbackMapper.findByVoiceModelId(voiceModelId);
        }
        return feedbackMapper.findByUserId(userId);
    }

    public Feedback submitFeedback(Feedback feedback) {
        feedback.setCreatedAt(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        return feedback;
    }

    public Map<String, Object> getVoiceRating(Long voiceModelId) {
        Double avgRating = feedbackMapper.getAvgRating(voiceModelId);
        Integer count = feedbackMapper.getCountByVoiceModelId(voiceModelId);
        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", avgRating != null ? avgRating : 0.0);
        result.put("count", count != null ? count : 0);
        return result;
    }
}
