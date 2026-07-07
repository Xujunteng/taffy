# 直播助手网站 (Live Streaming Assistant)

基于 AI 语音合成技术的直播助手平台，支持用户上传音频进行声音训练，使用训练后的 AI 声音模型朗读任意文本，帮助直播者提高效率和直播质量

## 项目信息

| 项目 | 详情 |
|------|------|
| 团队人数 | 4 人 |
| 开发周期 | 4 天 |
| 技术栈 | Vue 3 + Spring Boot + MyBatis + SQLite |
| 交付形式 | 可运行系统 + 部署 + PPT + 演示 |

## 功能清单

### 必做功能 (MVP)

1. **用户注册和登录** — JWT 认证、BCrypt 密码加密
2. **音频文件上传和声音训练** — 对接阿里云语音合成 API，上传音频样本克隆声音
3. **文本到语音转换 (TTS)** — 输入文本，使用训练后的声音模型朗读
4. **个性化声音设置和管理** — 声音库 CRUD、训练状态追踪
5. **实时语音合成和直播支持** — WebSocket 实时 TTS、OBS 对接
6. **用户反馈和声音质量评价** — 评分（1-5 星）、评论
7. **直播脚本编辑和排练** — 脚本 CRUD、分类、排练计时
8. **数据安全和隐私保护** — BCrypt 加密、JWT 认证、音频隔离存储
9. **第三方直播平台集成** — OBS Studio WebSocket 对接
10. **语音调整和效果预览** — 语速、音调等参数调整
11. **声音库存储和管理** — 声音模型列表、删除管理
12. **用户教育和帮助中心** — 帮助文章、分类浏览、搜索

### 选做功能

- 多语言支持和自动翻译
- 直播数据分析和观众互动
- API 接口供高级用户使用

## 技术架构

### 前端

- **框架**: Vue 3 + Vite
- **UI 库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **音频**: MediaRecorder API / HTML5 Audio API

### 后端（三个独立服务）

| 服务 | 端口 | 职责 | 开发人员 |
|------|------|------|----------|
| 主后端服务 | 8081 | 用户认证、声音库管理、基础业务 | 成员 2 |
| 语音 AI 服务 | 8082 | 音频上传、TTS 合成、声音训练 | 成员 3 |
| 拓展功能服务 | 8083 | 评价系统、数据统计、脚本管理、帮助中心 | 成员 4 |

### 数据库

- **SQLite** — 零配置、即开即用、单文件存储
- 三服务共享同一数据库文件

### 外部依赖

- 阿里云语音合成 API（声音克隆 + TTS）
- OBS Studio WebSocket 插件

## 系统架构图

```
┌─────────────────────────────────────────────────┐
│                   前端 (Vue 3)                    │
│  登录 | 声音管理 | 脚本 | TTS | 直播 | 评价 | 帮助  │
└─────────────────┬───────────────────────────────┘
                  │  RESTful API (HTTP/JSON)
    ┌─────────────┼─────────────┐
    ▼             ▼             ▼
┌────────┐ ┌──────────┐ ┌──────────┐
│主后端   │ │语音AI    │ │拓展功能   │
│:8081   │ │:8082     │ │:8083     │
└───┬────┘ └────┬─────┘ └────┬─────┘
    └───────────┼─────────────┘
                ▼
    ┌─────────────────────┐
    │   SQLite 数据库      │
    │   音频文件存储       │
    └─────────────────────┘
```

## 页面路由

| 页面 | 路由 | 功能描述 |
|------|------|----------|
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |
| 仪表盘 | `/dashboard` | 快速入口、最近模型、统计概览 |
| 声音管理 | `/voices` | 声音列表、上传音频、训练状态 |
| TTS 转换 | `/tts` | 文本输入、声音选择、试听下载 |
| 直播面板 | `/live` | OBS 连接、实时 TTS、脚本切换 |
| 脚本编辑 | `/scripts` | 脚本 CRUD、分类、排练计时 |
| 声音评价 | `/feedback` | 评分、评论、历史评价 |
| 直播统计 | `/stats` | 观看数据、互动图表 |
| 帮助中心 | `/help` | 文章列表、搜索、分类 |

## 数据库表

| 表名 | 说明 |
|------|------|
| `users` | 用户表（id, username, password_hash, email, role, created_at, updated_at） |
| `voice_models` | 声音模型表（id, user_id, name, description, status, audio_file_path, model_params, created_at） |
| `tts_tasks` | TTS 任务表（id, user_id, voice_model_id, text_content, audio_output_path, status, created_at） |
| `scripts` | 脚本表（id, user_id, title, content, category, created_at, updated_at） |
| `feedbacks` | 评价表（id, user_id, voice_model_id, rating, comment, created_at） |
| `live_sessions` | 直播会话表（id, user_id, script_id, platform, start_time, end_time, stats_data） |
| `help_articles` | 帮助文章表（id, title, content, category, sort_order, created_at） |

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose (部署用)

### 本地开发

#### 1. 克隆项目

```bash
git clone <repo-url>
cd taffy
```

#### 2. 启动后端服务

```bash
# 主后端服务 (:8081)
cd backend-main
mvn spring-boot:run

# 语音 AI 服务 (:8082)
cd backend-voice-ai
mvn spring-boot:run

# 拓展功能服务 (:8083)
cd backend-extended
mvn spring-boot:run
```

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，API 请求通过 Vite 代理转发到对应后端服务。

### Docker Compose 部署

```bash
docker-compose up -d
```

部署后将启动以下服务：
- Nginx (端口 80/443) — 静态文件 + API 反向代理
- 主后端服务 (端口 8081)
- 语音 AI 服务 (端口 8082)
- 拓展功能服务 (端口 8083)

## 项目结构

```
taffy/
├── frontend/                # Vue 3 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 通用组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── api/             # API 请求封装
│   │   └── assets/          # 静态资源
│   ├── vite.config.js
│   └── package.json
├── backend-main/            # 主后端服务 (:8081)
│   ├── src/main/java/com/taffy/
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 业务逻辑
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── entity/          # 实体类
│   │   ├── config/          # 配置类
│   │   └── common/          # 通用类（JWT、异常等）
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/          # MyBatis XML
│   └── pom.xml
├── backend-voice-ai/        # 语音 AI 服务 (:8082)
│   ├── src/main/java/com/taffy/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── config/
│   │   └── common/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── backend-extended/        # 拓展功能服务 (:8083)
│   ├── src/main/java/com/taffy/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── config/
│   │   └── common/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── docker-compose.yml       # Docker Compose 编排
├── nginx.conf               # Nginx 配置
└── README.md
```

## 团队分工

| 角色 | 技术栈 | 职责范围 |
|------|--------|----------|
| 前端开发 | Vue 3 + Vite + Element Plus + Axios | 全部页面开发、音频录制/播放组件、API 对接 |
| 主后端开发 | Spring Boot + MyBatis + SQLite + JWT | 用户认证、数据库设计、声音库 CRUD |
| 语音 AI 开发 | Spring Boot + 阿里云 SDK + 文件处理 | 音频上传、TTS 合成、声音训练 |
| 拓展功能 + 部署 | Spring Boot + Docker + Nginx | 评价系统、数据统计、脚本管理、帮助中心、部署 |

## 开发计划（4 天）

| 天数 | 前端 | 主后端 | 语音 AI | 拓展 + 部署 |
|------|------|--------|---------|-------------|
| D1 | 项目初始化、路由布局、登录注册页 | 项目初始化、数据库建表、登录注册 API | 项目初始化、阿里云 SDK 集成、音频上传 API | 项目初始化、Git 仓库、Docker Compose 模板 |
| D2 | 声音管理页、TTS 页、脚本编辑页 | 声音库 CRUD、JWT 中间件、用户中心 | TTS 合成 API、音频处理、声音训练 | 帮助中心、脚本管理、评价系统 |
| D3 | 直播面板、评价统计页、帮助中心页 | API 文档、安全加固、三方联调 | 语音调整、效果预览、服务联调 | OBS 对接、WebSocket 配置 |
| D4 | 语音调整滑块、翻译功能、全页联调 | 开发者 API、Bug 修复、部署文档 | 效果预览 API、翻译 API、Bug 修复 | Docker Compose 部署、Nginx 配置、全流程测试 |

## 许可

本项目仅用于学术和教育目的。