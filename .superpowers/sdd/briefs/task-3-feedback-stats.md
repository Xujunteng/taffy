# Task 3: 评价系统 + 数据统计模块

**目标**: 创建 feedbacks 和 live_sessions 表的完整实现

## 数据库表

### feedbacks
```sql
CREATE TABLE IF NOT EXISTS feedbacks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    voice_model_id INTEGER,
    rating INTEGER CHECK(rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (voice_model_id) REFERENCES voice_models(id)
);
```

### live_sessions
```sql
CREATE TABLE IF NOT EXISTS live_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    script_id INTEGER,
    platform VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    stats_data TEXT
);
```

## 需要创建/完善的文件

### A. 评价模块

#### entity/Feedback.java
字段: id, userId, voiceModelId, rating(1-5), comment, createdAt

#### mapper/FeedbackMapper.java
- findByUserId(Long userId) — 按用户查评价
- findByVoiceModelId(Long voiceModelId) — 按声音模型查评价
- getAvgRating(Long voiceModelId) — SELECT COALESCE(AVG(rating), 0) FROM feedbacks WHERE voice_model_id=?
- getCountByVoiceModelId(Long voiceModelId) — SELECT COUNT(*)
- insert(Feedback) — @Options(useGeneratedKeys=true)

#### service/FeedbackService.java
- getFeedbackList(userId, voiceModelId): voiceModelId不为空→findByVoiceModelId，否则→findByUserId
- submitFeedback(feedback): setCreatedAt, insert
- getVoiceRating(voiceModelId): 返回 Map {avgRating, count}

#### controller/FeedbackController.java（完善现有）
- GET /api/feedback?voiceModelId=xxx → {code:200, data:[...]}
- POST /api/feedback → {code:200, message:"评价提交成功"}
- GET /api/feedback/rating/{voiceModelId} → {code:200, data:{avgRating, count}}

### B. 数据统计模块

#### entity/LiveSession.java
字段: id, userId, scriptId, platform, startTime, endTime, statsData

#### mapper/LiveSessionMapper.java
- findByUserId(Long userId) — ORDER BY start_time DESC
- findById(Long id)
- countByUserId(Long userId) — SELECT COUNT(*)
- insert(LiveSession) — @Options(useGeneratedKeys=true)
- update(LiveSession) — SET end_time/stats_data

#### service/StatsService.java
- @Autowired LiveSessionMapper + JdbcTemplate
- getOverview(userId): JdbcTemplate跨表查 voice_models/tts_tasks/scripts/live_sessions 的COUNT → 返回 {voiceCount, ttsCount, scriptCount, liveCount}
- getLiveStats(userId): 获取sessions列表，计算总时长(秒)
- getSessions(userId): 返回LiveSession列表
- startSession(userId, scriptId, platform): 创建设startTime=now()的session
- endSession(sessionId, statsData): 设endTime=now(),更新

#### controller/StatsController.java（完善现有）
- GET /api/stats/overview → {code:200, data:{voiceCount, ttsCount, scriptCount, liveCount}}
- GET /api/stats/live → {code:200, data:{totalSessions, totalDurationSeconds}}
- GET /api/stats/sessions → {code:200, data:[LiveSession数组]}
- POST /api/stats/sessions → body:{scriptId?, platform?} → {code:200, message:"直播已开始", data:session}

## 全局约束
- com.taffy 包，Map<String,Object> 返回
- MyBatis 注解SQL，StatsService用JdbcTemplate做跨表聚合
- 从 request.getAttribute("userId") 获取用户ID
