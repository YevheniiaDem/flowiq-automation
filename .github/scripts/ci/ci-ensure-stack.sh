#!/usr/bin/env bash
# Ensures a healthy stack exists: reuse running stack or load pre-built images and start.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if "${SCRIPT_DIR}/ci-verify-stack.sh"; then
  exit 0
fi

echo "Stack not reusable on this runner — starting from pre-built images..."
"${SCRIPT_DIR}/ci-up-fast.sh"
