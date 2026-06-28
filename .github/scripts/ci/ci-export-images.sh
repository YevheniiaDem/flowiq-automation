#!/usr/bin/env bash
# Exports pre-built backend/frontend images for parallel test jobs.
set -euo pipefail

OUTPUT="${1:-ci-images.tar}"

if ! docker image inspect flowiq-backend:ci > /dev/null 2>&1; then
  echo "::error::Image flowiq-backend:ci not found. Run ci-up.sh first."
  exit 1
fi

if ! docker image inspect flowiq-frontend:ci > /dev/null 2>&1; then
  echo "::error::Image flowiq-frontend:ci not found. Run ci-up.sh first."
  exit 1
fi

echo "Exporting CI images to ${OUTPUT}..."
docker save flowiq-backend:ci flowiq-frontend:ci -o "${OUTPUT}"
echo "Image export complete ($(du -h "${OUTPUT}" | cut -f1))."
