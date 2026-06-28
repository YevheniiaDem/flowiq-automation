#!/usr/bin/env bash
# Starts a pre-built stack (no docker build). Used by parallel test jobs after build-environment.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=retry.sh
source "${SCRIPT_DIR}/retry.sh"

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-flowiq-ci}"
COMPOSE_PROFILES="${COMPOSE_PROFILES:-}"
CI_IMAGES_TAR="${CI_IMAGES_TAR:-ci-images.tar}"
STACK_ENV_FILE="${STACK_ENV_FILE:-stack.env}"
export COMPOSE_FILE COMPOSE_PROJECT_NAME

compose() {
  if [ -n "${COMPOSE_PROFILES}" ]; then
    docker compose --profile "${COMPOSE_PROFILES}" -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  else
    docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  fi
}

load_images() {
  if [ -f "${STACK_ENV_FILE}" ]; then
    # shellcheck disable=SC1090
    set -a
    source "${STACK_ENV_FILE}"
    set +a
  fi

  if [ -n "${BACKEND_IMAGE:-}" ] && [ -n "${FRONTEND_IMAGE:-}" ]; then
    echo "Attempting GHCR pull from stack metadata..."
    if "${SCRIPT_DIR}/ci-pull-images.sh"; then
      return 0
    fi
    echo "::warning::GHCR pull failed — trying tarball fallback."
  fi

  if [ -f "${CI_IMAGES_TAR}" ]; then
    echo "Loading pre-built CI images from ${CI_IMAGES_TAR}..."
    retry docker load -i "${CI_IMAGES_TAR}"
    return 0
  fi

  echo "::error::No pre-built images available (GHCR or ci-images.tar)."
  return 1
}

load_images

echo "Starting pre-built CI stack (project: ${COMPOSE_PROJECT_NAME})..."

retry docker pull postgres:15-alpine
retry compose up -d --no-build postgres

"${SCRIPT_DIR}/wait-for-postgres.sh" postgres

retry compose up -d --no-build flowiq-backend
"${SCRIPT_DIR}/wait-for-backend-health.sh" \
  "http://localhost:${BACKEND_PORT:-8080}/api/health"

retry compose up -d --no-build flowiq-frontend
"${SCRIPT_DIR}/wait-for-frontend.sh" \
  "http://localhost:${FRONTEND_PORT:-3000}/"

"${SCRIPT_DIR}/wait-for-seed.sh" \
  "http://localhost:${BACKEND_PORT:-8080}/api"

if [ -n "${COMPOSE_PROFILES}" ]; then
  case "${COMPOSE_PROFILES}" in
    mailhog) retry compose up -d --no-build mailhog ;;
    minio) retry compose up -d --no-build minio ;;
    redis) retry compose up -d --no-build redis ;;
  esac
fi

echo "Pre-built CI stack is ready."
