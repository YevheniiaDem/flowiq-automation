#!/usr/bin/env bash
# Per-job teardown helper for non-shared-stack runners.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ARTIFACTS_DIR="${ARTIFACTS_DIR:-ci-artifacts}"
SUITE_ID="${1:-suite}"

"${SCRIPT_DIR}/collect-logs.sh"
"${SCRIPT_DIR}/ci-down.sh"

echo "Local stack teardown completed for '${SUITE_ID}'."
