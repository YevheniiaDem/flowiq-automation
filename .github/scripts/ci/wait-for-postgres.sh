#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.ci.yml}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-flowiq-ci}"
SERVICE="${1:-postgres}"
MAX_ATTEMPTS="${2:-60}"
INTERVAL_SECONDS="${3:-2}"

compose() {
  docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT_NAME}" "$@"
}

echo "Waiting for PostgreSQL service '${SERVICE}' (max ${MAX_ATTEMPTS} attempts)..."

for ((i = 1; i <= MAX_ATTEMPTS; i++)); do
  if compose exec -T "${SERVICE}" pg_isready -U "${POSTGRES_USER:-flowiq}" -d "${POSTGRES_DB:-flowiq}" > /dev/null 2>&1; then
    echo "PostgreSQL is ready after ${i} attempt(s)."
    exit 0
  fi
  echo "Attempt ${i}/${MAX_ATTEMPTS} — PostgreSQL not ready yet."
  sleep "${INTERVAL_SECONDS}"
done

echo "::error::PostgreSQL did not become ready in time."
exit 1
