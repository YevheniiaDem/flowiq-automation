#!/usr/bin/env bash
# Resolves backend/frontend CI images: reuse local, pull GHCR by git SHA, or compose build fallback.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=retry.sh
source "${SCRIPT_DIR}/retry.sh"

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
BACKEND_CONTEXT="${BACKEND_CONTEXT:-./flowiq-backend}"
FRONTEND_CONTEXT="${FRONTEND_CONTEXT:-./flowiq-frontend}"
REGISTRY="${GHCR_REGISTRY:-ghcr.io}"
IMAGE_OWNER="${GHCR_IMAGE_OWNER:-}"
LOCAL_BACKEND_TAG="flowiq-backend:ci"
LOCAL_FRONTEND_TAG="flowiq-frontend:ci"

compose() {
  docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME:-flowiq-ci}" "$@"
}

images_present_locally() {
  docker image inspect "${LOCAL_BACKEND_TAG}" > /dev/null 2>&1 \
    && docker image inspect "${LOCAL_FRONTEND_TAG}" > /dev/null 2>&1
}

resolve_owner() {
  if [ -n "${IMAGE_OWNER}" ]; then
    echo "${IMAGE_OWNER}"
    return
  fi
  if [ -n "${GITHUB_REPOSITORY_OWNER:-}" ]; then
    echo "${GITHUB_REPOSITORY_OWNER,,}"
    return
  fi
  echo ""
}

resolve_sha() {
  local context="$1"
  if [ -d "${context}/.git" ]; then
    git -C "${context}" rev-parse HEAD
  else
    echo ""
  fi
}

build_remote_tags() {
  local owner="$1"
  local backend_sha="$2"
  local frontend_sha="$3"

  if [ -z "${owner}" ] || [ -z "${backend_sha}" ] || [ -z "${frontend_sha}" ]; then
    return 1
  fi

  BACKEND_IMAGE="${REGISTRY}/${owner}/flowiq-backend:ci-${backend_sha}"
  FRONTEND_IMAGE="${REGISTRY}/${owner}/flowiq-frontend:ci-${frontend_sha}"
  export BACKEND_IMAGE FRONTEND_IMAGE
}

echo "Resolving CI Docker images..."

if images_present_locally; then
  echo "Local tags ${LOCAL_BACKEND_TAG} and ${LOCAL_FRONTEND_TAG} already present — skipping pull/build."
  exit 0
fi

if [ -n "${BACKEND_IMAGE:-}" ] && [ -n "${FRONTEND_IMAGE:-}" ]; then
  echo "Using explicit image references from environment."
  if "${SCRIPT_DIR}/ci-pull-images.sh"; then
    exit 0
  fi
  echo "::warning::Explicit GHCR pull failed — falling back."
fi

if [ "${CI_SKIP_GHCR_PULL:-false}" != "true" ]; then
  owner="$(resolve_owner)"
  backend_sha="$(resolve_sha "${BACKEND_CONTEXT}")"
  frontend_sha="$(resolve_sha "${FRONTEND_CONTEXT}")"

  if build_remote_tags "${owner}" "${backend_sha}" "${frontend_sha}"; then
    echo "Attempting GHCR pull for backend=${BACKEND_IMAGE}"
    if "${SCRIPT_DIR}/ci-pull-images.sh"; then
      echo "Resolved images from GHCR (content-addressed by git SHA)."
      exit 0
    fi
    echo "::warning::GHCR pull failed — falling back to local compose build."
  fi
fi

if [ ! -d "${BACKEND_CONTEXT}" ] || [ ! -d "${FRONTEND_CONTEXT}" ]; then
  echo "::error::Cannot build locally — backend/frontend contexts missing and GHCR pull unavailable."
  exit 1
fi

echo "Building backend and frontend images locally (compose build fallback)..."
retry compose build flowiq-backend flowiq-frontend
echo "Local compose build completed."
