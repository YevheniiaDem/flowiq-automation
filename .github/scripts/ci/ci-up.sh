#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=retry.sh
source "${SCRIPT_DIR}/retry.sh"

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-flowiq-ci}"
BACKEND_CONTEXT="${BACKEND_CONTEXT:-./flowiq-backend}"
FRONTEND_CONTEXT="${FRONTEND_CONTEXT:-./flowiq-frontend}"
COMPOSE_PROFILES="${COMPOSE_PROFILES:-}"
export COMPOSE_FILE COMPOSE_PROJECT_NAME BACKEND_CONTEXT FRONTEND_CONTEXT

if [ ! -d "${BACKEND_CONTEXT}" ]; then
  echo "::error::Backend context '${BACKEND_CONTEXT}' not found."
  exit 1
fi

if [ ! -d "${FRONTEND_CONTEXT}" ]; then
  echo "::error::Frontend context '${FRONTEND_CONTEXT}' not found."
  exit 1
fi

compose() {
  if [ -n "${COMPOSE_PROFILES}" ]; then
    docker compose --profile "${COMPOSE_PROFILES}" -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  else
    docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  fi
}

echo "Starting ephemeral CI stack (project: ${COMPOSE_PROJECT_NAME})..."

retry docker pull postgres:15-alpine

"${SCRIPT_DIR}/ci-resolve-images.sh"

echo "Starting infrastructure services..."
retry compose up -d postgres

"${SCRIPT_DIR}/wait-for-postgres.sh" postgres

echo "Starting backend (Flyway migrations run on startup)..."
retry compose up -d flowiq-backend

"${SCRIPT_DIR}/wait-for-backend-health.sh" \
  "http://localhost:${BACKEND_PORT:-8080}/api/health"

echo "Starting frontend..."
retry compose up -d flowiq-frontend

"${SCRIPT_DIR}/wait-for-frontend.sh" \
  "http://localhost:${FRONTEND_PORT:-3000}/"

"${SCRIPT_DIR}/wait-for-seed.sh" \
  "http://localhost:${BACKEND_PORT:-8080}/api"

if [ -n "${COMPOSE_PROFILES}" ]; then
  echo "Starting optional Compose profile services: ${COMPOSE_PROFILES}"
  case "${COMPOSE_PROFILES}" in
    mailhog) retry compose up -d mailhog ;;
    minio) retry compose up -d minio ;;
    redis) retry compose up -d redis ;;
    *)
      echo "::warning::Unknown optional profile '${COMPOSE_PROFILES}' — skipping optional services."
      ;;
  esac
fi

echo "Ephemeral CI stack is ready."
