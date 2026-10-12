@echo off
setlocal
echo =====================================================================
echo   Launching JavaFX Stock Exchange Trading Dashboard
echo =====================================================================

cd /d "%~dp0"

set "TARGET_URL=%~1"
if "%TARGET_URL%"=="" set "TARGET_URL=%EXCHANGE_URL%"
if "%TARGET_URL%"=="" set "TARGET_URL=http://localhost:8085"

echo Connecting to Exchange Backend at: %TARGET_URL%
echo.

mvn javafx:run -Dexchange.url="%TARGET_URL%"
