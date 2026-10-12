#!/bin/bash
set -e
echo "====================================================================="
echo "  Launching JavaFX Stock Exchange Trading Dashboard"
echo "====================================================================="

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

mvn javafx:run
