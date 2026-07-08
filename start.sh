#!/bin/bash
# 直播助手网站 - 一键启动脚本
# 队友用浏览器打开 http://10.135.4.128:8080 即可

echo "=== 直播助手网站 启动中 ==="

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# 1. 确保数据库目录存在并建表
mkdir -p data
if [ ! -f data/taffy.db ]; then
  echo "初始化数据库..."
fi

# 2. 启动主后端 (必须第一个，会建表)
echo "启动主后端 :8081..."
cd backend-main
nohup mvn spring-boot:run > /tmp/taffy-main.log 2>&1 &
cd "$PROJECT_DIR"
sleep 15

# 3. 启动语音AI
echo "启动语音AI :8082..."
cd backend-voice-ai
nohup mvn spring-boot:run > /tmp/taffy-voiceai.log 2>&1 &
cd "$PROJECT_DIR"
sleep 12

# 4. 启动拓展功能
echo "启动拓展功能 :8083..."
cd backend-extended
nohup mvn spring-boot:run > /tmp/taffy-extend.log 2>&1 &
cd "$PROJECT_DIR"
sleep 12

# 5. 构建并启动前端
echo "构建前端..."
cd frontend
npm run build 2>/dev/null
echo "启动前端 :8080..."
nohup npx vite preview --host 0.0.0.0 --port 8080 > /tmp/taffy-frontend.log 2>&1 &
cd "$PROJECT_DIR"

sleep 3

echo ""
echo "=============================="
echo "  网站已启动!"
echo "  内网地址: http://10.135.4.128:8080"
echo "=============================="
