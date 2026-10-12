param(
    [string]$Url = $env:EXCHANGE_URL
)

if (-not $Url) {
    $Url = "http://localhost:8085"
}

Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "  Launching JavaFX Stock Exchange Trading Dashboard" -ForegroundColor Cyan
Write-Host "  Connecting to: $Url" -ForegroundColor Yellow
Write-Host "=====================================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

mvn javafx:run -Dexchange.url="$Url"
