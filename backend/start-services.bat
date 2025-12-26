@echo off
REM Script de lancement des services backend CourierSync

echo ========================================
echo Lancement des services backend CourierSync
echo ========================================
echo.

REM Vérification des prérequis
echo Verification des pre-requis...
echo.

REM Démarrage des services dans l'ordre
echo 1. Installation des modules Maven (si necessaire)
cd %~dp0
call mvn clean install -DskipTests -q
if %errorlevel% neq 0 (
    echo Erreur lors de la construction Maven
    pause
    exit /b %errorlevel%
)

echo.
echo 2. Lancement du User Auth Service (port 8081)
start "User Auth Service" cmd /k "cd %~dp0user-auth-service ^&^& mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo 3. Lancement du Dispatch Service (port 8082)
start "Dispatch Service" cmd /k "cd %~dp0dispatch-service ^&^& mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo 4. Lancement du Tracking Service (port 8083)
start "Tracking Service" cmd /k "cd %~dp0tracking-service ^&^& mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo 5. Lancement du Billing Service (port 8084)
start "Billing Service" cmd /k "cd %~dp0billing-service ^&^& mvn spring-boot:run"
timeout /t 15 /nobreak > nul

echo 6. Lancement de l'API Gateway (port 8080)
start "API Gateway" cmd /k "cd %~dp0api-gateway ^&^& mvn spring-boot:run"

echo.
echo ========================================
echo Tous les services sont en cours de lancement
echo ========================================
echo.
echo Services:
echo - User Auth Service: http://localhost:8081
echo - Dispatch Service: http://localhost:8082
echo - Tracking Service: http://localhost:8083
echo - Billing Service: http://localhost:8084
echo - API Gateway: http://localhost:8080
echo.
echo Appuyez sur une touche pour quitter ce script (les services continueront de tourner)
pause > nul
