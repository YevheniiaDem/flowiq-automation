#!/usr/bin/env bash
# Generates an Allure HTML report with history (trends, duration, retries, flaky) preserved.
set -euo pipefail

RESULTS_DIR="${1:-merged-allure-results}"
REPORT_DIR="${2:-allure-report}"
GH_PAGES_DIR="${3:-gh-pages-cache}"
ARTIFACT_HISTORY_DIR="${4:-allure-history-cache}"
PROJECT_DIR="${5:-.}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="${PROJECT_DIR}/target/allure-publish-build"

"${SCRIPT_DIR}/allure-restore-history.sh" \
  "${RESULTS_DIR}" \
  "${GH_PAGES_DIR}" \
  "${ARTIFACT_HISTORY_DIR}"

mkdir -p "${REPORT_DIR}" "${BUILD_DIR}"

echo "Generating Allure report from ${RESULTS_DIR}..."
(
  cd "${PROJECT_DIR}"
  mvn -B allure:report \
    -Dallure.results.directory="${RESULTS_DIR}" \
    -Dproject.build.directory="${BUILD_DIR}" || true
)

GENERATED="${BUILD_DIR}/site/allure-maven-plugin"
if [ -d "${GENERATED}" ]; then
  cp -r "${GENERATED}/." "${REPORT_DIR}/"
fi

if [ ! -f "${REPORT_DIR}/index.html" ]; then
  echo "::warning::Allure index.html not found at ${REPORT_DIR} — report may be empty."
  exit 0
fi

history_count=0
if [ -d "${REPORT_DIR}/history" ]; then
  history_count="$(find "${REPORT_DIR}/history" -type f | wc -l | tr -d ' ')"
fi

echo "Allure report ready at ${REPORT_DIR} (history files: ${history_count})."
