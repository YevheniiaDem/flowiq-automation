#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=retry.sh
source "${SCRIPT_DIR}/retry.sh"

URL="${1:-http://localhost:8080/api/health}"
MAX_ATTEMPTS="${2:-90}"
INTERVAL_SECONDS="${3:-3}"

echo "Waiting for backend health at ${URL} (max ${MAX_ATTEMPTS} attempts)..."

wait_once() {
  local response
  if ! response="$(curl -sf "${URL}")"; then
    return 1
  fi

  if echo "${response}" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"'; then
    return 0
  fi
  if echo "${response}" | grep -qi 'UP'; then
    return 0
  fi
  return 1
}

for ((i = 1; i <= MAX_ATTEMPTS; i++)); do
  if wait_once; then
    echo "Backend is healthy after ${i} attempt(s)."
    exit 0
  fi
  echo "Attempt ${i}/${MAX_ATTEMPTS} — backend not healthy yet."
  sleep "${INTERVAL_SECONDS}"
done

echo "::error::Backend did not become healthy in time."
exit 1
