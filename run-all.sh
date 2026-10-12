#!/usr/bin/env bash
set -e

echo "====================================================================="
echo "  STOCK EXCHANGE SIMULATION - ALL-IN-ONE LAUNCHER (Linux/macOS)"
echo "  1. Starts Backend Server (Jetty REST API + Matching Engine :8085)"
echo "  2. Executes Live Demo Scenario (Bob SELL vs Alice BUY verification)"
echo "  3. Opens JavaFX Trading Dashboard GUI Terminal"
echo "====================================================================="
echo ""

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

# Step 1: Start Server if not already active
if curl -s -f http://localhost:8085/api/health > /dev/null 2>&1; then
    echo "[INFO] Exchange Server already running on port 8085."
else
    echo "[INFO] Starting Exchange Server in background..."
    if [ -f "target/stock-exchange-simulation-1.0.0.jar" ]; then
        java -jar target/stock-exchange-simulation-1.0.0.jar &
    else
        mvn exec:java@server &
    fi
    SERVER_PID=$!
    echo "[INFO] Waiting for Server to start on port 8085..."
    for i in {1..30}; do
        if curl -s -f http://localhost:8085/api/health > /dev/null 2>&1; then
            echo "[OK] Exchange Server is active and healthy!"
            break
        fi
        sleep 0.5
    done
fi

echo ""
# Step 2: Run Demo Scenario
echo "[2/3] Running Presentation Demo Scenario..."
if [ -f "target/stock-exchange-simulation-1.0.0.jar" ]; then
    java -cp target/stock-exchange-simulation-1.0.0.jar com.exchange.demo.LiveDemoRunner
else
    mvn exec:java@demo
fi

echo ""
# Step 3: Launch JavaFX GUI
echo "[3/3] Launching JavaFX Client..."
mvn javafx:run
