Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "  Running Scripted Presentation Demo Scenario (CLI Headless)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

mvn exec:java@demo
