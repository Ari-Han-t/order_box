Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "  Launching JavaFX Stock Exchange Trading Dashboard" -ForegroundColor Cyan
Write-Host "=====================================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

mvn javafx:run
