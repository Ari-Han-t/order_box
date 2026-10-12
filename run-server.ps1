Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "  Starting Electronic Stock Exchange Backend Server (Port 8080)" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

if (Test-Path "target\stock-exchange-simulation-1.0.0.jar") {
    java -jar "target\stock-exchange-simulation-1.0.0.jar"
} else {
    mvn exec:java@server
}
