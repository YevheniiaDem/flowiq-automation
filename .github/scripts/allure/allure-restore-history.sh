#!/usr/bin/env bash
# Copies Allure history from a previous GitHub Pages deployment or history artifact.
set -euo pipefail

RESULTS_DIR="${1:?Results directory required}"
GH_PAGES_DIR="${2:-gh-pages-cache}"
ARTIFACT_HISTORY_DIR="${3:-allure-history-cache}"

restore_from() {
  local source="$1"
  if [ -d "${source}" ]; then
    mkdir -p "${RESULTS_DIR}/history"
    cp -r "${source}/." "${RESULTS_DIR}/history/"
    local count
    count="$(find "${RESULTS_DIR}/history" -type f | wc -l | tr -d ' ')"
    echo "Restored Allure history from ${source} (${count} file(s))."
    return 0
  fi
  return 1
}

echo "Restoring Allure history into ${RESULTS_DIR}..."

for candidate in \
  "${GH_PAGES_DIR}/history" \
  "${ARTIFACT_HISTORY_DIR}/history" \
  "${ARTIFACT_HISTORY_DIR}"; do
  if restore_from "${candidate}"; then
    exit 0
  fi
done

echo "No previous Allure history found — trends will start from this run."
