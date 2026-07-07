package com.taffy.mapper;

import com.taffy.entity.LiveSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LiveSessionMapper {

    @Select("SELECT * FROM live_sessions WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<LiveSession> findByUserId(Long userId);

    @Select("SELECT * FROM live_sessions WHERE id = #{id}")
    LiveSession findById(Long id);

    @Select("SELECT COUNT(*) FROM live_sessions WHERE user_id = #{userId}")
    Integer countByUserId(Long userId);

    @Insert("INSERT INTO live_sessions (user_id, script_id, platform, start_time, end_time, stats_data) " +
            "VALUES (#{userId}, #{scriptId}, #{platform}, #{startTime}, #{endTime}, #{statsData})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveSession session);

    @Update("UPDATE live_sessions SET end_time = #{endTime}, stats_data = #{statsData} WHERE id = #{id}")
    int update(LiveSession session);
}
