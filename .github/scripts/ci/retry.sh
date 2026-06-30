#!/usr/bin/env bash
# Retries flaky infrastructure commands (docker pull, compose up, etc.). Does NOT retry tests.
# Source this file to define retry(), or execute directly: retry.sh <command> [args...]
set -euo pipefail

retry() {
  local max_attempts="${RETRY_MAX_ATTEMPTS:-5}"
  local delay_seconds="${RETRY_DELAY_SECONDS:-10}"

  if [ "$#" -lt 1 ]; then
    echo "Usage: retry <command> [args...]"
    return 1
  fi

  local attempt=1
  while true; do
    if "$@"; then
      return 0
    fi
    local exit_code=$?

    if [ "${attempt}" -ge "${max_attempts}" ]; then
      echo "::error::Command failed after ${max_attempts} attempts (exit ${exit_code}): $*"
      return "${exit_code}"
    fi

    echo "Attempt ${attempt}/${max_attempts} failed (exit ${exit_code}). Retrying in ${delay_seconds}s: $*"
    sleep "${delay_seconds}"
    attempt=$((attempt + 1))
  done
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  retry "$@"
fi
