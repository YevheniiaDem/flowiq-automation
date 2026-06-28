#!/usr/bin/env bash
# Maps workflow suite selection to Maven profiles executed sequentially.
set -euo pipefail

SUITE="${1:-full}"
TEST_ENV="${TEST_ENV:-ci}"

run_profile() {
  local profile="$1"
  echo "Running Maven profile '${profile}' with env '${TEST_ENV}'..."
  mvn clean test -P"${profile}" -Denv="${TEST_ENV}" -B
}

case "${SUITE}" in
  full)
    run_profile regression
    run_profile contract
    ;;
  smoke)
    run_profile api-smoke
    run_profile ui-smoke
    ;;
  api)
    run_profile api-regression
    ;;
  ui)
    run_profile ui-smoke
    ;;
  contract)
    run_profile contract
    ;;
  security)
    echo "::warning::Security suite is not implemented yet. Skipping."
    ;;
  *)
    echo "::error::Unknown test suite '${SUITE}'."
    exit 1
    ;;
esac

echo "Test suite '${SUITE}' completed."
