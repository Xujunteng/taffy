#!/usr/bin/env bash
set -e

# ============================================================
# Taffy 直播助手 — Linux 启动脚本
# ============================================================
# 用法: ./start.sh [start|stop|restart|status]
# 默认: start
#
# 目录结构要求:
#   taffy/
#   ├── data/                  # SQLite DB + 音频文件
#   ├── backend-main.jar
#   ├── backend-voice-ai.jar
#   ├── backend-extended.jar
#   └── start.sh
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

DATA_DIR="$SCRIPT_DIR/data"
LOG_DIR="$SCRIPT_DIR/logs"
PID_DIR="$SCRIPT_DIR/pids"

# ---- 可配置项 (也可通过环境变量覆盖) ----
export JWT_SECRET="${JWT_SECRET:-taffy-live-assistant-jwt-secret-key-2026}"
export JWT_EXPIRATION="${JWT_EXPIRATION:-86400000}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:sqlite:./data/taffy.db}"
export VOICE_AI_STORAGE_ROOT="${VOICE_AI_STORAGE_ROOT:-./data/audio}"

# 阿里云 TTS（可选，不配置则使用本地 espeak-ng）
# export ALIYUN_ACCESS_KEY_ID="your-key"
# export ALIYUN_ACCESS_KEY_SECRET="your-secret"
# export ALIYUN_APP_KEY="your-app-key"

JAVA_OPTS="${JAVA_OPTS:--Xms128m -Xmx512m}"

# ---- 函数 ----
check_java() {
    if ! command -v java &>/dev/null; then
        echo "[ERROR] 未找到 Java，请安装 JDK 17+: sudo apt-get install -y openjdk-17-jre"
        exit 1
    fi
    echo "[OK] Java: $(java -version 2>&1 | head -1)"
}

check_espeak() {
    if command -v espeak-ng &>/dev/null; then
        echo "[OK] espeak-ng 已安装"
    elif command -v espeak &>/dev/null; then
        echo "[OK] espeak 已安装"
    else
        echo "[WARN] espeak-ng 未安装 — 阿里云 TTS 失败时无法兜底"
        echo "      安装: sudo apt-get install -y espeak-ng"
    fi
}

ensure_dirs() {
    mkdir -p "$DATA_DIR" "$DATA_DIR/audio" "$LOG_DIR" "$PID_DIR"
}

start_service() {
    local name="$1"
    local jar="$2"
    local port="$3"
    local pid_file="$PID_DIR/$name.pid"
    local log_file="$LOG_DIR/$name.log"

    if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
        echo "[SKIP] $name 已在运行 (pid=$(cat "$pid_file"))"
        return
    fi

    if [ ! -f "$jar" ]; then
        echo "[ERROR] 找不到 $jar"
        return 1
    fi

    echo "[START] $name (port=$port)..."
    nohup java $JAVA_OPTS -jar "$jar" > "$log_file" 2>&1 &
    echo $! > "$pid_file"

    # 等待启动
    for i in $(seq 1 30); do
        if curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port/api/auth/login" 2>/dev/null | grep -q '200\|401\|405'; then
            echo "[OK] $name 已启动 (pid=$(cat "$pid_file"))"
            return 0
        fi
        sleep 1
    done
    echo "[WARN] $name 启动超时，请检查日志: $log_file"
}

stop_service() {
    local name="$1"
    local pid_file="$PID_DIR/$name.pid"

    if [ ! -f "$pid_file" ]; then
        echo "[SKIP] $name 未运行"
        return
    fi

    local pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
        echo "[STOP] $name (pid=$pid)..."
        kill "$pid"
        for i in $(seq 1 15); do
            if ! kill -0 "$pid" 2>/dev/null; then
                echo "[OK] $name 已停止"
                rm -f "$pid_file"
                return 0
            fi
            sleep 1
        done
        echo "[WARN] 强制停止 $name"
        kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
}

# ---- 主逻辑 ----
CMD="${1:-start}"

case "$CMD" in
    start)
        echo "===== Taffy 启动 ====="
        check_java
        check_espeak
        ensure_dirs

        # 1. 先启动 backend-main（初始化数据库表）
        start_service "backend-main" "backend-main.jar" "8081"
        sleep 2

        # 2. 启动 voice-ai 和 extended（依赖 main 建表）
        start_service "backend-voice-ai" "backend-voice-ai.jar" "8082"
        start_service "backend-extended" "backend-extended.jar" "8083"

        echo ""
        echo "===== 服务状态 ====="
        for s in backend-main backend-voice-ai backend-extended; do
            pid_file="$PID_DIR/$s.pid"
            if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
                echo "  $s: RUNNING (pid=$(cat "$pid_file"))"
            else
                echo "  $s: STOPPED"
            fi
        done
        echo ""
        echo "日志目录: $LOG_DIR"
        echo "数据目录: $DATA_DIR"
        ;;

    stop)
        echo "===== Taffy 停止 ====="
        stop_service "backend-extended"
        stop_service "backend-voice-ai"
        stop_service "backend-main"
        echo "所有服务已停止"
        ;;

    restart)
        "$0" stop
        sleep 2
        "$0" start
        ;;

    status)
        echo "===== Taffy 状态 ====="
        for s in backend-main backend-voice-ai backend-extended; do
            pid_file="$PID_DIR/$s.pid"
            if [ -f "$pid_file" ] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
                echo "  $s: RUNNING (pid=$(cat "$pid_file"))"
            else
                echo "  $s: STOPPED"
            fi
        done
        ;;

    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
