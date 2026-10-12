Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host "  STOCK EXCHANGE SIMULATION - ALL-IN-ONE LAUNCHER (PowerShell)" -ForegroundColor Cyan
Write-Host "  1. Starts Backend Server (Jetty REST API + Matching Engine :8085)" -ForegroundColor Yellow
Write-Host "  2. Executes Live Demo Scenario (Bob SELL vs Alice BUY verification)" -ForegroundColor Yellow
Write-Host "  3. Opens JavaFX Trading Dashboard GUI Terminal" -ForegroundColor Yellow
Write-Host "=====================================================================" -ForegroundColor Cyan
Write-Host ""

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# Step 1: Start Server if not already running on port 8085
Write-Host "[1/3] Checking if Exchange Server is running on port 8085..." -ForegroundColor Cyan
$serverRunning = $false
try {
    $resp = Invoke-WebRequest -Uri "http://localhost:8085/api/health" -UseBasicParsing -TimeoutSec 1
    if ($resp.StatusCode -eq 200) { $serverRunning = $true }
} catch {}

if ($serverRunning) {
    Write-Host "[INFO] Exchange Server is already running on http://localhost:8085!" -ForegroundColor Green
} else {
    Write-Host "[INFO] Starting Exchange Server in separate window..." -ForegroundColor Yellow
    if (Test-Path "target\stock-exchange-simulation-1.0.0.jar") {
        Start-Process cmd -ArgumentList "/k title Exchange Server [Port 8085] && cd /d `"$scriptDir`" && java -jar target\stock-exchange-simulation-1.0.0.jar"
    } else {
        Start-Process cmd -ArgumentList "/k title Exchange Server [Port 8085] && cd /d `"$scriptDir`" && mvn exec:java@server"
    }
    Write-Host "[INFO] Waiting for Server to initialize on port 8085..." -ForegroundColor Cyan
    $healthy = $false
    for ($i = 0; $i -lt 30; $i++) {
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:8085/api/health" -UseBasicParsing -TimeoutSec 1
            if ($resp.StatusCode -eq 200) { $healthy = $true; break }
        } catch {}
        Start-Sleep -Milliseconds 500
    }
    if ($healthy) {
        Write-Host "[OK] Exchange Server is active and healthy!" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Health check timed out, proceeding anyway..." -ForegroundColor Yellow
    }
}

Write-Host ""
# Step 2: Run Presentation Demo Scenario
Write-Host "[2/3] Running Presentation Demo Scenario (Slides 07 & 11)..." -ForegroundColor Cyan
if (Test-Path "target\stock-exchange-simulation-1.0.0.jar") {
    java -cp "target\stock-exchange-simulation-1.0.0.jar" com.exchange.demo.LiveDemoRunner
} else {
    mvn exec:java@demo
}

Write-Host ""
# Step 3: Launch JavaFX Trading Terminal GUI
Write-Host "[3/3] Launching JavaFX Trading Terminal UI..." -ForegroundColor Cyan
mvn javafx:run

Write-Host ""
Write-Host "JavaFX Terminal session ended." -ForegroundColor Green
Pause
