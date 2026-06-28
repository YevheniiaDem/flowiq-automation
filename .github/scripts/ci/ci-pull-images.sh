#!/usr/bin/env bash
# Pulls immutable CI images from GHCR and tags them for docker-compose.ci.yml.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=retry.sh
source "${SCRIPT_DIR}/retry.sh"

BACKEND_IMAGE="${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
FRONTEND_IMAGE="${FRONTEND_IMAGE:?FRONTEND_IMAGE is required}"
LOCAL_BACKEND_TAG="${LOCAL_BACKEND_TAG:-flowiq-backend:ci}"
LOCAL_FRONTEND_TAG="${LOCAL_FRONTEND_TAG:-flowiq-frontend:ci}"

echo "Pulling backend image: ${BACKEND_IMAGE}"
retry docker pull "${BACKEND_IMAGE}"

echo "Pulling frontend image: ${FRONTEND_IMAGE}"
retry docker pull "${FRONTEND_IMAGE}"

docker tag "${BACKEND_IMAGE}" "${LOCAL_BACKEND_TAG}"
docker tag "${FRONTEND_IMAGE}" "${LOCAL_FRONTEND_TAG}"

echo "GHCR images tagged as ${LOCAL_BACKEND_TAG} and ${LOCAL_FRONTEND_TAG}."
