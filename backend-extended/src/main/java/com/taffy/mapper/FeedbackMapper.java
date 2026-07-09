package com.taffy.mapper;

import com.taffy.entity.Feedback;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Select("SELECT * FROM feedbacks WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Feedback> findByUserId(Long userId);

    /** 公开列表：JOIN users + voice_models 获取用户名和模型名 */
    @Select("SELECT f.*, u.username, v.name AS modelName FROM feedbacks f " +
            "LEFT JOIN users u ON f.user_id = u.id " +
            "LEFT JOIN voice_models v ON f.voice_model_id = v.id " +
            "ORDER BY f.created_at DESC")
    List<Feedback> findAllPublic();

    /** 按声音模型筛选的公开列表 */
    @Select("SELECT f.*, u.username, v.name AS modelName FROM feedbacks f " +
            "LEFT JOIN users u ON f.user_id = u.id " +
            "LEFT JOIN voice_models v ON f.voice_model_id = v.id " +
            "WHERE f.voice_model_id = #{voiceModelId} ORDER BY f.created_at DESC")
    List<Feedback> findByVoiceModelIdPublic(Long voiceModelId);

    @Select("SELECT * FROM feedbacks WHERE voice_model_id = #{voiceModelId} ORDER BY created_at DESC")
    List<Feedback> findByVoiceModelId(Long voiceModelId);

    @Select("SELECT COALESCE(AVG(rating), 0) FROM feedbacks WHERE voice_model_id = #{voiceModelId}")
    Double getAvgRating(Long voiceModelId);

    @Select("SELECT COUNT(*) FROM feedbacks WHERE voice_model_id = #{voiceModelId}")
    Integer getCountByVoiceModelId(Long voiceModelId);

    @Insert("INSERT INTO feedbacks (user_id, voice_model_id, rating, comment, show_name, created_at) " +
            "VALUES (#{userId}, #{voiceModelId}, #{rating}, #{comment}, " +
            "#{showName, jdbcType=INTEGER}, #{createdAt})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(Feedback feedback);
}
