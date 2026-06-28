#!/usr/bin/env bash
# Runs a single Maven profile for parallel CI jobs (isolated target directory).
set -euo pipefail

PROFILE="${1:?Maven profile required}"
TEST_ENV="${TEST_ENV:-ci}"
TARGET_DIR="${TARGET_DIR:-target/${PROFILE}}"

mkdir -p "${TARGET_DIR}"

echo "Running profile '${PROFILE}' (env=${TEST_ENV}, target=${TARGET_DIR})..."
mvn clean test \
  -P"${PROFILE}" \
  -Denv="${TEST_ENV}" \
  -Dproject.build.directory="${TARGET_DIR}" \
  -Dallure.results.directory="${TARGET_DIR}/allure-results" \
  -B

echo "Profile '${PROFILE}' completed."
