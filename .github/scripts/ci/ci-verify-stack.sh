#!/usr/bin/env bash
# Returns 0 when the ephemeral stack is already healthy on this runner (reuse path).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-3000}"

if "${SCRIPT_DIR}/wait-for-backend-health.sh" \
  "http://localhost:${BACKEND_PORT}/api/health" 3 2; then
  if "${SCRIPT_DIR}/wait-for-frontend.sh" \
    "http://localhost:${FRONTEND_PORT}/" 3 2; then
    if "${SCRIPT_DIR}/wait-for-seed.sh" \
      "http://localhost:${BACKEND_PORT}/api" 3 2; then
      echo "Existing CI stack is healthy — reusing without rebuild."
      exit 0
    fi
  fi
fi

echo "CI stack is not healthy on this runner."
exit 1
