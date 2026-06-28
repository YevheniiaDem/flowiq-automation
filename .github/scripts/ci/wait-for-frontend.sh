#!/usr/bin/env bash
set -euo pipefail

URL="${1:-http://localhost:3000/}"
MAX_ATTEMPTS="${2:-60}"
INTERVAL_SECONDS="${3:-3}"

echo "Waiting for frontend at ${URL} (max ${MAX_ATTEMPTS} attempts)..."

for ((i = 1; i <= MAX_ATTEMPTS; i++)); do
  if curl -sf "${URL}" > /dev/null; then
    echo "Frontend is reachable after ${i} attempt(s)."
    exit 0
  fi
  echo "Attempt ${i}/${MAX_ATTEMPTS} — frontend not reachable yet."
  sleep "${INTERVAL_SECONDS}"
done

echo "::error::Frontend did not become reachable in time."
exit 1
