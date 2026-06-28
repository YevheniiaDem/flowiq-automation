#!/usr/bin/env bash
# Merges Allure result files from multiple downloaded CI artifacts into one directory.
# Supports raw directories and gzip-compressed suite artifacts (*.tar.gz).
set -euo pipefail

INPUT_DIR="${1:-allure-input}"
OUTPUT_DIR="${2:-merged-allure-results}"

if [ ! -d "${INPUT_DIR}" ]; then
  echo "::warning::Allure input directory '${INPUT_DIR}' not found — creating empty output."
  mkdir -p "${OUTPUT_DIR}"
  exit 0
fi

mkdir -p "${OUTPUT_DIR}"

extract_dir="${INPUT_DIR}/._extracted"
mkdir -p "${extract_dir}"

while IFS= read -r -d '' archive; do
  case "$(basename "${archive}")" in
    *allure-results*.tar.gz)
      name="$(basename "${archive}" .tar.gz)"
      dest="${extract_dir}/${name}"
      mkdir -p "${dest}"
      echo "Extracting Allure archive ${archive}..."
      tar -xzf "${archive}" -C "${dest}" 2>/dev/null || true
      ;;
  esac
done < <(find "${INPUT_DIR}" -type f -name '*allure-results*.tar.gz' -print0 2>/dev/null || true)

file_count=0
while IFS= read -r -d '' file; do
  base="$(basename "${file}")"
  case "${base}" in
    *.tar.gz|README.txt|.gitkeep)
      continue
      ;;
  esac
  case "${base}" in
    *-result.json|*-container.json|*-attachment*|*.png|*.webm|*.mp4|*.zip)
      cp -f "${file}" "${OUTPUT_DIR}/"
      file_count=$((file_count + 1))
      ;;
  esac
done < <(find "${INPUT_DIR}" "${extract_dir}" -type f -print0 2>/dev/null || true)

echo "Merged ${file_count} Allure result file(s) into ${OUTPUT_DIR}."

if [ "${file_count}" -eq 0 ]; then
  echo "::warning::No Allure result files found to merge."
fi
