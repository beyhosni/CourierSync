@echo off
REM Script d'arrêt des services backend CourierSync

echo ========================================
echo Arrêt des services backend CourierSync
echo ========================================
echo.

REM Arrêt des services par port
echo Arrêt du User Auth Service (port 8081)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8081" ^| find "LISTENING"') do (
    taskkill /F /PID %%a
)

echo Arrêt du Dispatch Service (port 8082)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8082" ^| find "LISTENING"') do (
    taskkill /F /PID %%a
)

echo Arrêt du Tracking Service (port 8083)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8083" ^| find "LISTENING"') do (
    taskkill /F /PID %%a
)

echo Arrêt du Billing Service (port 8084)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8084" ^| find "LISTENING"') do (
    taskkill /F /PID %%a
)

echo Arrêt de l'API Gateway (port 8080)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8080" ^| find "LISTENING"') do (
    taskkill /F /PID %%a
)

echo.
echo ========================================
echo Tous les services ont été arrêtés
echo ========================================
pause
