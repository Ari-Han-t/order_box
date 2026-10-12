@echo off
setlocal
title Stock Exchange - Master Launcher
echo =====================================================================
echo   STOCK EXCHANGE SIMULATION - ALL-IN-ONE LAUNCHER
echo   1. Starts Backend Server (Jetty REST API + Matching Engine :8085)
echo   2. Executes Live Demo Scenario (Bob SELL vs Alice BUY verification)
echo   3. Opens JavaFX Trading Dashboard GUI Terminal
echo =====================================================================
echo.

cd /d "%~dp0"

:: Step 1: Start Server in its own window if not already running on port 8085
echo [1/3] Checking if Exchange Server is running on port 8085...
powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8085/api/health' -UseBasicParsing -TimeoutSec 1; if ($r.StatusCode -eq 200) { exit 0 } } catch {}; exit 1"
if %ERRORLEVEL% equ 0 (
    echo [INFO] Exchange Server is already running on http://localhost:8085!
) else (
    echo [INFO] Starting Exchange Server in separate window...
    if exist "target\stock-exchange-simulation-1.0.0.jar" (
        start "Exchange Server [Port 8085]" cmd /k "title Exchange Server [Port 8085] && cd /d "%~dp0" && java -jar target\stock-exchange-simulation-1.0.0.jar"
    ) else (
        start "Exchange Server [Port 8085]" cmd /k "title Exchange Server [Port 8085] && cd /d "%~dp0" && mvn exec:java@server"
    )
    echo [INFO] Waiting for Server to initialize on port 8085...
    powershell -NoProfile -Command "for ($i=0; $i -lt 30; $i++) { try { $r = Invoke-WebRequest -Uri 'http://localhost:8085/api/health' -UseBasicParsing -TimeoutSec 1; if ($r.StatusCode -eq 200) { exit 0 } } catch {}; Start-Sleep -Milliseconds 500 }; exit 1"
    if %ERRORLEVEL% neq 0 (
        echo [WARNING] Server health check timed out, proceeding anyway...
    ) else (
        echo [OK] Exchange Server is active and healthy!
    )
)

echo.
:: Step 2: Run Presentation Demo Scenario
echo [2/3] Running Presentation Demo Scenario (Slides 07 & 11)...
if exist "target\stock-exchange-simulation-1.0.0.jar" (
    java -cp "target\stock-exchange-simulation-1.0.0.jar" com.exchange.demo.LiveDemoRunner
) else (
    call mvn exec:java@demo
)

echo.
:: Step 3: Launch JavaFX Trading Terminal GUI
echo [3/3] Launching JavaFX Trading Terminal UI...
call mvn javafx:run

echo.
echo JavaFX Terminal session ended.
pause
