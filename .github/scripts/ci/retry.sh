#!/usr/bin/env bash
# Retries flaky infrastructure commands (docker pull, compose up, etc.). Does NOT retry tests.
set -euo pipefail

MAX_ATTEMPTS="${RETRY_MAX_ATTEMPTS:-5}"
DELAY_SECONDS="${RETRY_DELAY_SECONDS:-10}"

if [ "$#" -lt 1 ]; then
  echo "Usage: retry.sh <command> [args...]"
  exit 1
fi

attempt=1
while true; do
  if "$@"; then
    exit 0
  fi
  exit_code=$?

  if [ "${attempt}" -ge "${MAX_ATTEMPTS}" ]; then
    echo "::error::Command failed after ${MAX_ATTEMPTS} attempts (exit ${exit_code}): $*"
    exit "${exit_code}"
  fi

  echo "Attempt ${attempt}/${MAX_ATTEMPTS} failed (exit ${exit_code}). Retrying in ${DELAY_SECONDS}s: $*"
  sleep "${DELAY_SECONDS}"
  attempt=$((attempt + 1))
done
