@echo off
setlocal
echo =====================================================================
echo   Starting Electronic Stock Exchange Backend Server (Port 8080)
echo =====================================================================

cd /d "%~dp0"

if exist "target\stock-exchange-simulation-1.0.0.jar" (
    java -jar "target\stock-exchange-simulation-1.0.0.jar"
) else (
    mvn exec:java@server
)
