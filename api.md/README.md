# voice-ai-service

直播助手网站成员3（语音AI开发）独立后端服务，端口 `8082`。

## 已实现接口

- `POST /api/audio/upload`：音频上传处理
- `POST /api/voice/train`：声音训练流程编排（演示模式直接生成 READY 模型）
- `POST /api/tts/convert`：文本转语音核心功能
- `GET /api/tts/status/{taskId}`：查询 TTS 任务状态
- `GET /api/tts/audio/{taskId}`：下载/试听生成音频

## 技术栈

Spring Boot 3 + MyBatis + SQLite + 本地文件存储 + 阿里云 TTS 预留适配层。

## 本地运行

```bash
mvn spring-boot:run
```

默认数据库：`./data/live_assistant.db`  
默认音频目录：`./data/audio`  
默认端口：`8082`

## 环境变量

```bash
SQLITE_DB_PATH=./data/live_assistant.db
VOICE_AI_STORAGE_ROOT=./data/audio
ALIYUN_TTS_ENABLED=false
ALIYUN_ACCESS_KEY_ID=your-key-id
ALIYUN_ACCESS_KEY_SECRET=your-key-secret
ALIYUN_TTS_APP_KEY=your-app-key
```

未配置阿里云密钥时，服务会使用 mock 音频内容写入 `.wav` 文件，保证接口流程可以跑通，方便前后端联调与 GitHub 提交。

## 快速测试

```bash
# 1. 上传音频
curl -F "file=@sample.wav" -F "userId=1" http://localhost:8082/api/audio/upload

# 2. 创建声音训练任务
curl -X POST http://localhost:8082/api/voice/train \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"name":"我的声音","description":"demo","audioFilePath":"./data/audio/1/uploads/sample.wav"}'

# 3. 文本转语音
curl -X POST http://localhost:8082/api/tts/convert \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"voiceModelId":1,"text":"欢迎来到直播间","language":"zh-CN","speed":1.0,"pitch":1.0}'
```

## GitHub 提交

```bash
git init
git add .
git commit -m "feat: add voice ai service"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```
