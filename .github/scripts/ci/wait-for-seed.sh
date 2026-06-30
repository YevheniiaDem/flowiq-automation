#!/usr/bin/env bash
# Verifies demo seed data is present by logging in via the API.
set -euo pipefail

API_URL="${1:-http://localhost:8080/api}"
EMAIL="${TEST_USER_EMAIL:-demo@flowiq.ai}"
PASSWORD="${TEST_USER_PASSWORD:-demo123}"
MAX_ATTEMPTS="${2:-30}"
INTERVAL_SECONDS="${3:-3}"

LOGIN_URL="${API_URL%/}/auth/login"

echo "Waiting for demo seed user '${EMAIL}' at ${LOGIN_URL} (max ${MAX_ATTEMPTS} attempts)..."

payload=$(printf '{"email":"%s","password":"%s"}' "${EMAIL}" "${PASSWORD}")

for ((i = 1; i <= MAX_ATTEMPTS; i++)); do
  http_code="$(curl -s -o /tmp/flowiq-seed-response.json -w "%{http_code}" \
    -X POST "${LOGIN_URL}" \
    -H "Content-Type: application/json" \
    -d "${payload}" || true)"

  if [ "${http_code}" = "200" ] && grep -qE '"(token|accessToken)"' /tmp/flowiq-seed-response.json 2>/dev/null; then
    echo "Demo seed verified after ${i} attempt(s)."
    exit 0
  fi

  echo "Attempt ${i}/${MAX_ATTEMPTS} — seed not ready (HTTP ${http_code})."
  sleep "${INTERVAL_SECONDS}"
done

echo "::error::Demo seed data was not available in time."
exit 1
