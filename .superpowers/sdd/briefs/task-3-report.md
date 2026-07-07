# Task 3 完成报告: 评价系统 + 数据统计模块

## 完成时间
2026-07-07

## 创建的文件 (6个)

### 实体层 (Entity)
| 文件 | 路径 | 说明 |
|------|------|------|
| Feedback.java | `entity/Feedback.java` | 评价实体: id, userId, voiceModelId, rating(1-5), comment, createdAt |
| LiveSession.java | `entity/LiveSession.java` | 直播会话实体: id, userId, scriptId, platform, startTime, endTime, statsData |

### 数据访问层 (Mapper)
| 文件 | 路径 | 说明 |
|------|------|------|
| FeedbackMapper.java | `mapper/FeedbackMapper.java` | MyBatis注解SQL: findByUserId, findByVoiceModelId, getAvgRating (COALESCE AVG), getCountByVoiceModelId, insert (@Options useGeneratedKeys) |
| LiveSessionMapper.java | `mapper/LiveSessionMapper.java` | MyBatis注解SQL: findByUserId (ORDER BY start_time DESC), findById, countByUserId, insert, update (SET end_time/stats_data) |

### 业务层 (Service)
| 文件 | 路径 | 说明 |
|------|------|------|
| FeedbackService.java | `service/FeedbackService.java` | getFeedbackList (voiceModelId非空→按模型查,否则→按用户查), submitFeedback (setCreatedAt+insert), getVoiceRating (返回Map{avgRating, count}) |
| StatsService.java | `service/StatsService.java` | @Autowired JdbcTemplate + LiveSessionMapper; getOverview (JdbcTemplate跨表查voice_models/tts_tasks/scripts/live_sessions的COUNT), getLiveStats (计算总时长秒数), getSessions, startSession, endSession |

## 完善的文件 (2个)

### 控制器层 (Controller)
| 文件 | 变更 |
|------|------|
| FeedbackController.java | 从空壳框架完善为完整实现: 注入FeedbackService, GET /api/feedback?voiceModelId=xxx, POST /api/feedback (body解析voiceModelId/rating/comment), GET /api/feedback/rating/{voiceModelId} |
| StatsController.java | 从空壳框架完善为完整实现: 注入StatsService, GET /api/stats/overview, GET /api/stats/live, GET /api/stats/sessions, POST /api/stats/sessions (body解析scriptId/platform) |

## 关键技术决策

1. **跨表聚合**: StatsService.getOverview() 使用 JdbcTemplate.queryForObject 执行4张表的 COUNT 查询 (voice_models, tts_tasks, scripts, live_sessions)，符合需求文档的 JdbcTemplate 要求
2. **总时长计算**: getLiveStats() 遍历 LiveSession 列表，使用 Duration.between(startTime, endTime) 累加秒数
3. **用户ID获取**: 所有接口统一从 request.getAttribute("userId") 获取，与现有 AuthInterceptor 的认证流程一致
4. **返回格式**: 所有接口返回 Map<String, Object>，格式为 {code:200, data:...} 或 {code:200, message:"...", data:...}

## 未完成项 (需Maven环境)
- 编译验证: 环境中未安装Maven，无法运行 `mvn compile`。建议在开发机上执行编译验证。
- 集成测试: 需启动Spring Boot服务后测试各API端点。

## 涉及的表
- feedbacks (评价表) — 新建表，已在 schema.sql 中定义
- live_sessions (直播会话表) — 新建表，已在 schema.sql 中定义
- voice_models (跨表查询) — StatsService 中 COUNT 查询
- tts_tasks (跨表查询) — StatsService 中 COUNT 查询
- scripts (跨表查询) — StatsService 中 COUNT 查询
