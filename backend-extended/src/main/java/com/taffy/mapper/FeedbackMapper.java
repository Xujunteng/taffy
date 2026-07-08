package com.taffy.mapper;

import com.taffy.entity.Feedback;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Select("SELECT * FROM feedbacks WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Feedback> findByUserId(Long userId);

    @Select("SELECT * FROM feedbacks WHERE voice_model_id = #{voiceModelId} ORDER BY created_at DESC")
    List<Feedback> findByVoiceModelId(Long voiceModelId);

    @Select("SELECT COALESCE(AVG(rating), 0) FROM feedbacks WHERE voice_model_id = #{voiceModelId}")
    Double getAvgRating(Long voiceModelId);

    @Select("SELECT COUNT(*) FROM feedbacks WHERE voice_model_id = #{voiceModelId}")
    Integer getCountByVoiceModelId(Long voiceModelId);

    @Insert("INSERT INTO feedbacks (user_id, voice_model_id, rating, comment, created_at) " +
            "VALUES (#{userId}, #{voiceModelId}, #{rating}, #{comment}, #{createdAt})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(Feedback feedback);
}
