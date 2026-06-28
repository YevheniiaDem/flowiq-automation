#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-flowiq-ci}"
COMPOSE_PROFILES="${COMPOSE_PROFILES:-}"
TEMP_DIR="${CI_TEMP_DIR:-/tmp/flowiq-ci}"

compose() {
  if [ -n "${COMPOSE_PROFILES}" ]; then
    docker compose --profile "${COMPOSE_PROFILES}" -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  else
    docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  fi
}

echo "Tearing down ephemeral CI stack (project: ${COMPOSE_PROJECT_NAME})..."

if compose ps -q > /dev/null 2>&1; then
  compose down -v --remove-orphans || true
else
  echo "Compose project not found — nothing to stop."
fi

if [ -d "${TEMP_DIR}" ]; then
  rm -rf "${TEMP_DIR}" || true
fi

echo "Cleanup finished."
