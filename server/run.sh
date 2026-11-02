#!/usr/bin/env bash
# Starts the Placement NIT backend on port 8080.
#
#   ./run.sh          — start (detached; survives closing the terminal)
#   ./stop.sh         — stop it again
#   tail -f /tmp/placement-nit.log
#
# Uses the JDK 21 bundled in the project (../.tools), so Maven's noisy
# "restricted method / Unsafe" warnings from newer JDKs are avoided.
set -euo pipefail
cd "$(dirname "$0")"

PORT=8080
LOG=/tmp/placement-nit.log
PID_FILE=/tmp/placement-nit.pid

ROOT="$(cd .. && pwd)"
if [ -d "$ROOT/.tools/jdk21/Contents/Home" ]; then
  export JAVA_HOME="$ROOT/.tools/jdk21/Contents/Home"
fi

# 1. Free the port from a stale previous run, if any.
if lsof -ti :$PORT >/dev/null 2>&1; then
  echo "Port $PORT busy — stopping previous instance…"
  lsof -ti :$PORT | xargs kill -9 2>/dev/null || true
  sleep 1
fi

# 2. Start detached — closing this terminal will NOT kill the server.
nohup ./mvnw spring-boot:run > "$LOG" 2>&1 &
SERVER_PID=$!
echo "$SERVER_PID" > "$PID_FILE"

echo "Starting backend (pid $SERVER_PID) — first start can take ~30 s…"
for _ in $(seq 1 90); do
  if curl -s -o /dev/null "http://localhost:$PORT/"; then
    break
  fi
  sleep 1
done

if lsof -ti :$PORT >/dev/null 2>&1; then
  echo "✅ Backend is up on http://localhost:$PORT"
  echo "   Login:  curl -X POST http://localhost:$PORT/api/auth/login -H 'Content-Type: application/json' -d '{\"email\":\"student@nit.edu\",\"password\":\"Student@123\"}'"
  echo "   Logs:   tail -f $LOG"
  echo "   Stop:   ./stop.sh"
else
  echo "❌ Backend did not come up — check the log:"
  tail -25 "$LOG"
  exit 1
fi
