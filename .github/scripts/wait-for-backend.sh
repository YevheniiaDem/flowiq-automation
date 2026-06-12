#!/usr/bin/env bash
set -euo pipefail

URL="${1:-http://localhost:8080/api/health}"
MAX_ATTEMPTS="${2:-60}"
SLEEP_SECONDS="${3:-5}"

echo "Waiting for backend at ${URL} (max ${MAX_ATTEMPTS} attempts)..."

for ((i = 1; i <= MAX_ATTEMPTS; i++)); do
  if curl -sf "${URL}" > /dev/null; then
    echo "Backend is healthy after ${i} attempt(s)."
    exit 0
  fi
  echo "Attempt ${i}/${MAX_ATTEMPTS} — backend not ready, sleeping ${SLEEP_SECONDS}s..."
  sleep "${SLEEP_SECONDS}"
done

echo "::error::Backend did not become healthy in time."
exit 1
