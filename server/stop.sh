#!/usr/bin/env bash
# Stops the Placement NIT backend started with ./run.sh
PID_FILE=/tmp/placement-nit.pid
if [ -f "$PID_FILE" ]; then
  PID=$(cat "$PID_FILE")
  kill "$PID" 2>/dev/null || true
  rm -f "$PID_FILE"
fi
# Belt and braces: clear anything still holding the port.
lsof -ti :8080 2>/dev/null | xargs kill -9 2>/dev/null || true
echo "Backend stopped."
