@echo off
setlocal
echo =====================================================================
echo   Launching JavaFX Stock Exchange Trading Dashboard
echo =====================================================================

cd /d "%~dp0"

mvn javafx:run
