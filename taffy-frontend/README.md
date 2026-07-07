# Taffy 直播助手前端

按照开发指南为成员1生成的 Vue 3 前端项目。

## 技术栈
Vue 3 + Vite + Element Plus + Pinia + Axios + Vue Router

## 启动
```bash
cd frontend
npm install
npm run dev
```

访问：`http://localhost:5173`

## 对接说明
前端统一请求 `/api/*`，由 `vite.config.js` 代理到：

- `/api/auth`、`/api/voices` → `http://localhost:8081`
- `/api/audio`、`/api/tts`、`/api/voice` → `http://localhost:8082`
- `/api/scripts`、`/api/feedback`、`/api/stats`、`/api/help` → `http://localhost:8083`

## 页面
- 登录 / 注册
- 仪表盘
- 声音管理
- TTS 转换
- 直播面板
- 脚本编辑
- 声音评价
- 直播统计
- 帮助中心
