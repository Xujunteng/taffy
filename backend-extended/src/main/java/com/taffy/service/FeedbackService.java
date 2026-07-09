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

    /** 获取公开评价列表（所有人可见），voiceModelId 为 null 时返回全部 */
    public List<Feedback> getFeedbackList(Long voiceModelId) {
        List<Feedback> list;
        if (voiceModelId != null) {
            list = feedbackMapper.findByVoiceModelIdPublic(voiceModelId);
        } else {
            list = feedbackMapper.findAllPublic();
        }
        // 匿名处理：showName=false 时隐藏用户名
        for (Feedback f : list) {
            if (!Boolean.TRUE.equals(f.getShowName())) {
                f.setUsername("匿名用户");
            }
        }
        return list;
    }

    public Feedback submitFeedback(Feedback feedback) {
        feedback.setCreatedAt(LocalDateTime.now());
        // showName 默认为 true
        if (feedback.getShowName() == null) {
            feedback.setShowName(true);
        }
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
