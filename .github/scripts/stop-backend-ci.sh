#!/usr/bin/env bash
set -euo pipefail

PID_FILE="${1:-backend.pid}"

if [ -f "${PID_FILE}" ]; then
  PID="$(cat "${PID_FILE}")"
  if kill -0 "${PID}" 2>/dev/null; then
    echo "Stopping backend (PID ${PID})..."
    kill "${PID}" || true
    wait "${PID}" 2>/dev/null || true
  fi
  rm -f "${PID_FILE}"
fi
