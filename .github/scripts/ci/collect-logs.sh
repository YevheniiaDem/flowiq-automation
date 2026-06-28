#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-flowiq-ci}"
COMPOSE_PROFILES="${COMPOSE_PROFILES:-}"
ARTIFACTS_DIR="${ARTIFACTS_DIR:-ci-artifacts}"

mkdir -p "${ARTIFACTS_DIR}/inspect"

compose() {
  if [ -n "${COMPOSE_PROFILES}" ]; then
    docker compose --profile "${COMPOSE_PROFILES}" -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  else
    docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
  fi
}

echo "Collecting Docker and Compose diagnostics into ${ARTIFACTS_DIR}..."

docker ps -a > "${ARTIFACTS_DIR}/docker-ps.txt" 2>&1 || true
docker images > "${ARTIFACTS_DIR}/docker-images.txt" 2>&1 || true
docker stats --no-stream --no-trunc > "${ARTIFACTS_DIR}/docker-stats.txt" 2>&1 || true

if compose ps -q > /dev/null 2>&1; then
  compose logs --no-color > "${ARTIFACTS_DIR}/compose.log" 2>&1 || true

  for service in postgres flowiq-backend flowiq-frontend mailhog minio redis; do
    if compose ps --status running "${service}" --quiet 2>/dev/null | grep -q .; then
      compose logs --no-color "${service}" > "${ARTIFACTS_DIR}/${service}.log" 2>&1 || true
    fi
  done

  compose ps -a > "${ARTIFACTS_DIR}/compose-ps.txt" 2>&1 || true

  while IFS= read -r container_id; do
    [ -z "${container_id}" ] && continue
    name="$(docker inspect --format '{{.Name}}' "${container_id}" 2>/dev/null | sed 's|^/||' || echo "${container_id}")"
    safe_name="$(echo "${name}" | tr '/:' '__')"
    docker inspect "${container_id}" > "${ARTIFACTS_DIR}/inspect/${safe_name}.json" 2>&1 || true
  done < <(compose ps -aq 2>/dev/null || true)
else
  echo "No compose project '${COMPOSE_PROJECT_NAME}' running — skipping compose log collection." \
    > "${ARTIFACTS_DIR}/compose.log"
fi

cat > "${ARTIFACTS_DIR}/README.txt" <<EOF
Docker diagnostics bundle
=========================
compose.log          - All Compose service logs
postgres.log         - PostgreSQL service logs
flowiq-backend.log   - Backend service logs
flowiq-frontend.log  - Frontend service logs
compose-ps.txt       - Compose container list
docker-ps.txt        - All Docker containers
docker-images.txt    - Local Docker images
docker-stats.txt     - Container resource usage snapshot
inspect/*.json       - docker inspect output per container
EOF

echo "Docker diagnostics collection finished."
