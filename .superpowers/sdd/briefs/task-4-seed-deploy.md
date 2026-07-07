# Task 4: 种子数据 + 开发日志 + 提交

**目标**: 预置帮助文章数据，验证部署配置，提交全部代码

## 1. config/DataInitializer.java
- 实现 CommandLineRunner
- @Autowired JdbcTemplate
- run(): 检查 help_articles 表是否为空(COUNT)，为空则插入6篇中文帮助文章
- 6篇文章内容（使用中文）：

```
1. "快速入门", category="入门指南", sort_order=1
   内容: 本平台使用流程——注册→创建声音→上传音频→训练→TTS合成→直播

2. "如何录制优质音频样本", category="入门指南", sort_order=2
   内容: 安静环境、好麦克风、自然语速、5-15分钟、WAV/MP3/M4A

3. "TTS参数调整说明", category="功能说明", sort_order=3
   内容: 语速0.5-2.0、音调0.5-2.0、音量0-100

4. "OBS直播配置教程", category="功能说明", sort_order=4
   内容: OBS开启WebSocket→设置端口密码→平台输入信息→连接→发送文本

5. "如何进行声音评价", category="入门指南", sort_order=5
   内容: 选择声音→打分1-5星→写评论→提交

6. "常见问题FAQ", category="常见问题", sort_order=6
   内容: 训练5-15分钟/支持中文/文件限50MB/多直播独立会话
```

## 2. 创建 docs/开发日志.md（在taffy仓库根目录下）
记录从Day1到Day4的开发过程，包含：
- 技术选型决策
- 每天完成的任务
- 遇到的问题和解决方案

格式参考已提供的内容（成员4视角）。

## 3. 验证部署配置
- 确认 docker-compose.yml 中 backend-extended 配置正确（端口8083, 卷挂载 ./data:/app/data）
- 确认 nginx.conf 中 /api/feedback/, /api/stats/, /api/scripts/, /api/help/ 都代理到 backend-extended:8083
- 确认 backend-extended/Dockerfile 存在且正确

## 4. Git提交
```bash
git add backend-extended/ docs/
git commit -m "feat(extended): 完成拓展功能服务全部实现

- 新增JWT认证配置(JwtUtil+AuthInterceptor+WebConfig)
- 新增4个Entity(Script/Feedback/LiveSession/HelpArticle)
- 新增4个MyBatis Mapper(注解SQL)
- 新增4个Service(业务逻辑)
- 完善4个Controller(14个API端点)
- 种子数据: 6篇帮助文章
- Docker Compose + Nginx配置验证通过"
```

## 全局约束
- com.taffy 包
- 只用INSERT语句，不用XML
- DataInitializer 要幂等（COUNT>0时跳过插入）
