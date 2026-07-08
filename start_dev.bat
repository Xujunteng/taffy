@echo off
echo ============================================
echo  Taffy Dev Environment Launcher
echo ============================================

set SCRIPT_DIR=%~dp0
set MVN=e:\maven\maven-mvnd-1.0.6-windows-amd64\mvn\bin\mvn

echo [1/4] Starting backend-main (port 8081)...
start "backend-main" /d "%SCRIPT_DIR%backend-main" cmd /k "%MVN% spring-boot:run"

echo [2/4] Starting backend-voice-ai (port 8082)...
start "backend-voice-ai" /d "%SCRIPT_DIR%backend-voice-ai" cmd /k "%MVN% spring-boot:run"

echo [3/4] Starting backend-extended (port 8083)...
start "backend-extended" /d "%SCRIPT_DIR%backend-extended" cmd /k "%MVN% spring-boot:run"

echo [4/4] Starting v2 frontend (port 5173)...
start "v2-frontend" /d "%SCRIPT_DIR%taffy-frontend-v2" cmd /k "npm run dev"

echo.
echo ============================================
echo  All services started!
echo  backend-main      : http://localhost:8081
echo  backend-voice-ai  : http://localhost:8082
echo  backend-extended  : http://localhost:8083
echo  v2 frontend       : http://localhost:5173
echo ============================================
echo.
pause