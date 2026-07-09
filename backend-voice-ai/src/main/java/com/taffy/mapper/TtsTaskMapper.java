package com.taffy.mapper;

import com.taffy.entity.TtsTask;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

/**
 * TTS任务 Mapper — 全部使用注解SQL
 */
@Mapper
public interface TtsTaskMapper {

    @Insert("INSERT INTO tts_tasks (user_id, voice_model_id, text_content, audio_output_path, status, created_at) " +
            "VALUES (#{userId}, #{voiceModelId}, #{textContent}, #{audioOutputPath}, #{status}, #{createdAt})")
    @SelectKey(statement = "SELECT last_insert_rowid()", keyProperty = "id", before = false, resultType = Long.class)
    int insert(TtsTask task);

    @Select("SELECT * FROM tts_tasks WHERE id = #{id}")
    TtsTask findById(@Param("id") Long id);

    @Select("SELECT * FROM tts_tasks WHERE user_id = #{userId} ORDER BY created_at DESC, id DESC LIMIT 50")
    List<TtsTask> findByUserId(@Param("userId") Long userId);

    @Update("UPDATE tts_tasks SET status = #{status}, audio_output_path = #{audioOutputPath} WHERE id = #{id}")
    int updateStatusAndOutput(@Param("id") Long id,
                              @Param("status") String status,
                              @Param("audioOutputPath") String audioOutputPath);
}
