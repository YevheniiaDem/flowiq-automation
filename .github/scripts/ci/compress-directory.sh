#!/usr/bin/env bash
# Compresses a directory into a gzip tarball. Skips when the directory is empty or missing.
set -euo pipefail

SOURCE_DIR="${1:?Source directory required}"
OUTPUT_FILE="${2:?Output .tar.gz path required}"

if [ ! -d "${SOURCE_DIR}" ]; then
  echo "Directory '${SOURCE_DIR}' does not exist — skipping compression."
  exit 0
fi

if [ -z "$(find "${SOURCE_DIR}" -mindepth 1 -print -quit 2>/dev/null)" ]; then
  echo "Directory '${SOURCE_DIR}' is empty — skipping compression."
  exit 0
fi

mkdir -p "$(dirname "${OUTPUT_FILE}")"
tar -czf "${OUTPUT_FILE}" -C "$(dirname "${SOURCE_DIR}")" "$(basename "${SOURCE_DIR}")"
size="$(du -h "${OUTPUT_FILE}" | cut -f1)"
echo "Created ${OUTPUT_FILE} (${size})"
