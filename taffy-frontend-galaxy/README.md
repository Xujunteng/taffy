# Taffy 星河直播助手前端 Galaxy 版

这是按开发指南重做的完整前端工程。技术栈：Vue 3 + Vite + Element Plus + Pinia + Axios + Vue Router。

## 启动

```bash
cd taffy-frontend-galaxy
npm install
npm run dev
```

访问：`http://localhost:5173`

## 构建

```bash
npm run build
```

## 页面

- 登录：`/login`
- 注册：`/register`
- 星河主页/仪表盘：`/dashboard`
- 声音管理：`/voices`
- TTS 转换：`/tts`
- 直播面板：`/live`
- 脚本编辑：`/scripts`
- 声音评价：`/feedback`
- 直播统计：`/stats`
- 帮助中心：`/help`

## 功能完成点

- JWT 登录状态管理、路由守卫、退出登录
- Axios 统一请求封装、错误处理、401 自动清理登录态
- Vite 开发代理：8081 / 8082 / 8083 三类后端服务
- 声音模型上传、编辑、删除、训练提交、训练状态轮询
- TTS 合成、状态查询、播放、历史、Blob 下载
- 直播控制台：声音选择、脚本选择、快捷话术、发送记录
- 脚本 CRUD、本地检索、分类筛选、复制内容
- 声音评价：提交评分、评价列表、评分概览
- 直播统计：概览卡片、会话表格、趋势图
- 帮助中心：文章列表、分类筛选、关键词搜索、详情弹窗
- 全局星河主题、主页星辰大海 Hero、星河光带、玻璃拟态卡片、加载/空状态/错误状态、基础响应式

## API 代理说明

- `/api/auth/*`、`/api/voices/*` → `http://localhost:8081`
- `/api/audio/*`、`/api/tts/*`、`/api/voice/*` → `http://localhost:8082`
- `/api/scripts/*`、`/api/feedback/*`、`/api/stats/*`、`/api/help/*` → `http://localhost:8083`
