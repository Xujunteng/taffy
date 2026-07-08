# ── 前端构建 ──
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY taffy-frontend-v2/package*.json ./
RUN npm install
COPY taffy-frontend-v2/ ./
RUN npm run build

# ── 主后端构建 ──
FROM maven:3.9-eclipse-temurin-17 AS main-build
WORKDIR /app
COPY backend-main/pom.xml ./pom.xml
COPY backend-main/src ./src
RUN mvn package -DskipTests -q

# ── 语音AI构建 ──
FROM maven:3.9-eclipse-temurin-17 AS voice-build
WORKDIR /app
COPY backend-voice-ai/pom.xml ./pom.xml
COPY backend-voice-ai/src ./src
RUN mvn package -DskipTests -q

# ── 拓展功能构建 ──
FROM maven:3.9-eclipse-temurin-17 AS extra-build
WORKDIR /app
COPY backend-extended/pom.xml ./pom.xml
COPY backend-extended/src ./src
RUN mvn package -DskipTests -q

# ── 最终运行镜像 ──
FROM nginx:alpine

# Nginx配置
COPY nginx.conf /etc/nginx/nginx.conf

# 前端静态文件
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# 音频文件目录
RUN mkdir -p /data/audio

# 后端JAR
COPY --from=main-build /app/target/*.jar /app/backend-main.jar
COPY --from=voice-build /app/target/*.jar /app/backend-voice-ai.jar
COPY --from=extra-build /app/target/*.jar /app/backend-extended.jar

# 安装JRE + supervisor
RUN apk add --no-cache openjdk17-jre supervisor

# Supervisor配置：同时运行nginx + 3个后端
RUN echo $'[supervisord]\nnodaemon=true\n\n[program:nginx]\ncommand=nginx -g "daemon off;"\n\n[program:main]\ncommand=java -jar /app/backend-main.jar\n\n[program:voice]\ncommand=java -jar /app/backend-voice-ai.jar\n\n[program:extra]\ncommand=java -jar /app/backend-extended.jar' > /etc/supervisord.conf

# 数据库文件
COPY data/taffy.db /app/data/taffy.db

EXPOSE 80
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
