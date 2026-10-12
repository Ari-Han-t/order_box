#!/bin/bash
set -e
echo "====================================================================="
echo "  Running Scripted Presentation Demo Scenario (CLI Headless)"
echo "====================================================================="

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

mvn exec:java@demo
