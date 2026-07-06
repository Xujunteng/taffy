package com.taffy.mapper;

import com.taffy.entity.VoiceModel;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface VoiceModelMapper {

    @Select("SELECT * FROM voice_models WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<VoiceModel> findByUserId(Long userId);

    @Select("SELECT * FROM voice_models WHERE id = #{id}")
    VoiceModel findById(Long id);

    @Insert("INSERT INTO voice_models (user_id, name, description, status, audio_file_path, model_params, created_at) " +
            "VALUES (#{userId}, #{name}, #{description}, #{status}, #{audioFilePath}, #{modelParams}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VoiceModel voiceModel);

    @Update("UPDATE voice_models SET status=#{status}, model_params=#{modelParams} WHERE id=#{id}")
    int updateStatus(VoiceModel voiceModel);
}