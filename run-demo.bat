@echo off
setlocal
echo =====================================================================
echo   Running Scripted Presentation Demo Scenario (CLI Headless)
echo =====================================================================

cd /d "%~dp0"

mvn exec:java@demo
