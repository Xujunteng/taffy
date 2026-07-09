package com.taffy.service;

import com.taffy.entity.LiveSession;
import com.taffy.mapper.LiveSessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    @Autowired
    private LiveSessionMapper liveSessionMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getOverview(Long userId) {
        Map<String, Object> data = new HashMap<>();

        Integer voiceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voice_models WHERE user_id = ?", Integer.class, userId);
        Integer ttsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tts_tasks WHERE user_id = ?", Integer.class, userId);
        Integer scriptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scripts WHERE user_id = ?", Integer.class, userId);
        Integer liveCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM live_sessions WHERE user_id = ?", Integer.class, userId);

        data.put("voiceCount", voiceCount != null ? voiceCount : 0);
        data.put("ttsCount", ttsCount != null ? ttsCount : 0);
        data.put("scriptCount", scriptCount != null ? scriptCount : 0);
        data.put("liveCount", liveCount != null ? liveCount : 0);
        return data;
    }

    public Map<String, Object> getLiveStats(Long userId) {
        List<LiveSession> sessions = liveSessionMapper.findByUserId(userId);
        long totalDurationSeconds = 0;
        for (LiveSession session : sessions) {
            if (session.getStartTime() != null && session.getEndTime() != null) {
                totalDurationSeconds += Duration.between(session.getStartTime(), session.getEndTime()).getSeconds();
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("totalSessions", sessions.size());
        data.put("totalDurationSeconds", totalDurationSeconds);
        return data;
    }

    public List<LiveSession> getSessions(Long userId) {
        return liveSessionMapper.findByUserId(userId);
    }

    public LiveSession startSession(Long userId, Long scriptId, String platform) {
        LiveSession session = new LiveSession();
        session.setUserId(userId);
        session.setScriptId(scriptId);
        session.setPlatform(platform);
        session.setStartTime(LocalDateTime.now());
        liveSessionMapper.insert(session);
        return session;
    }

    /** 返回最近 7 天的 TTS 合成趋势数据 */
    public List<Map<String, Object>> getTtsTrend(Long userId) {
        return jdbcTemplate.queryForList(
            "SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM tts_tasks WHERE user_id = ? " +
            "AND created_at >= DATE('now', '-7 days') " +
            "GROUP BY DATE(created_at) ORDER BY date", userId);
    }

    /** 返回最近活动（TTS + 评价） */
    public List<Map<String, Object>> getRecentActivity(Long userId) {
        List<Map<String, Object>> list = new java.util.ArrayList<>();

        // 最近 5 条 TTS 记录
        List<Map<String, Object>> ttsList = jdbcTemplate.queryForList(
            "SELECT 'tts' as type, text_content as detail, created_at as time " +
            "FROM tts_tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT 5", userId);
        list.addAll(ttsList);

        // 最近 5 条评价
        List<Map<String, Object>> fbList = jdbcTemplate.queryForList(
            "SELECT 'feedback' as type, comment as detail, created_at as time " +
            "FROM feedbacks WHERE user_id = ? ORDER BY created_at DESC LIMIT 5", userId);
        list.addAll(fbList);

        // 按时间排序取前 10 条
        list.sort((a, b) -> String.valueOf(b.getOrDefault("time", ""))
                .compareTo(String.valueOf(a.getOrDefault("time", ""))));
        return list.size() > 10 ? list.subList(0, 10) : list;
    }

    public LiveSession endSession(Long sessionId, String statsData) {
        LiveSession session = liveSessionMapper.findById(sessionId);
        if (session != null) {
            session.setEndTime(LocalDateTime.now());
            session.setStatsData(statsData);
            liveSessionMapper.update(session);
        }
        return session;
    }
}
