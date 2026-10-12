#!/bin/bash
set -e
echo "====================================================================="
echo "  Starting Electronic Stock Exchange Backend Server (Port 8080)"
echo "====================================================================="

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

if [ -f "target/stock-exchange-simulation-1.0.0.jar" ]; then
    java -jar "target/stock-exchange-simulation-1.0.0.jar"
else
    mvn exec:java@server
fi
