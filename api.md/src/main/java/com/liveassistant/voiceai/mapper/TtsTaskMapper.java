package com.liveassistant.voiceai.mapper;

import com.liveassistant.voiceai.entity.TtsTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TtsTaskMapper {
    int insert(TtsTask task);
    TtsTask findById(@Param("id") Long id);
    int updateStatusAndOutput(@Param("id") Long id, @Param("status") String status, @Param("audioOutputPath") String audioOutputPath);
}
