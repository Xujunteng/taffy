package com.liveassistant.voiceai.mapper;

import com.liveassistant.voiceai.entity.VoiceModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VoiceModelMapper {
    int insert(VoiceModel voiceModel);
    VoiceModel findById(@Param("id") Long id);
    int updateStatusAndParams(@Param("id") Long id, @Param("status") String status, @Param("modelParams") String modelParams);
}
