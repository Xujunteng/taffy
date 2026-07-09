FROM nginx:alpine

# 安装 JDK + ffmpeg + Python + edge-tts（从 Alpine 源，不依赖 Docker Hub）
RUN apk add --no-cache openjdk17-jre-headless ffmpeg python3 py3-pip supervisor && \
    pip3 install --no-cache-dir --break-system-packages edge-tts

# Nginx 配置（单容器用 localhost）
COPY nginx-docker.conf /etc/nginx/nginx.conf

# 前端
COPY taffy-frontend-v2/dist /usr/share/nginx/html

# 后端 JAR（已本地编译）
COPY backend-main/target/backend-main-1.0.0.jar /app/backend-main.jar
COPY backend-voice-ai/target/backend-voice-ai-1.0.0.jar /app/backend-voice-ai.jar
COPY backend-extended/target/backend-extended-1.0.0.jar /app/backend-extended.jar

# 数据和音频目录
RUN mkdir -p /app/data /data/audio
COPY data/taffy.db /app/data/taffy.db

# Supervisor 配置
COPY supervisord.conf /etc/supervisord.conf

EXPOSE 80
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
