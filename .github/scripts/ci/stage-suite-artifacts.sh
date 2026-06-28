#!/usr/bin/env bash
# Stages suite test artifacts for compressed CI upload.
# Resolves Playwright paths under target/ (hardcoded in test runtime) and extracts
# failure-only screenshots/videos from Allure results without rerunning tests.
set -euo pipefail

TARGET_DIR="${1:?Maven target directory required}"
STAGE_DIR="${2:?Staging directory required}"
INCLUDE_PLAYWRIGHT="${3:-false}"

mkdir -p "${STAGE_DIR}"/{surefire,allure-results,allure-report,traces,screenshots,videos,logs}

copy_dir_contents() {
  local src="$1"
  local dest="$2"
  if [ -d "${src}" ] && [ -n "$(find "${src}" -mindepth 1 -print -quit 2>/dev/null)" ]; then
    mkdir -p "${dest}"
    cp -a "${src}/." "${dest}/"
    echo "Staged ${src} -> ${dest}"
  fi
}

copy_dir_contents "${TARGET_DIR}/surefire-reports" "${STAGE_DIR}/surefire"
copy_dir_contents "${TARGET_DIR}/allure-results" "${STAGE_DIR}/allure-results"
copy_dir_contents "${TARGET_DIR}/site/allure-maven-plugin" "${STAGE_DIR}/allure-report"
copy_dir_contents "${TARGET_DIR}/logs" "${STAGE_DIR}/logs"
copy_dir_contents "target/logs" "${STAGE_DIR}/logs"

if [ "${INCLUDE_PLAYWRIGHT}" = "true" ]; then
  copy_dir_contents "${TARGET_DIR}/traces" "${STAGE_DIR}/traces"
  copy_dir_contents "target/traces" "${STAGE_DIR}/traces"
  copy_dir_contents "${TARGET_DIR}/screenshots" "${STAGE_DIR}/screenshots"
  copy_dir_contents "target/screenshots" "${STAGE_DIR}/screenshots"
  copy_dir_contents "${TARGET_DIR}/test-output" "${STAGE_DIR}/screenshots"

  ALLURE_DIR="${TARGET_DIR}/allure-results"
  if [ -d "${ALLURE_DIR}" ]; then
    echo "Extracting failure attachments from Allure results in ${ALLURE_DIR}..."
    shopt -s nullglob
    for result_file in "${ALLURE_DIR}"/*-result.json; do
      status="$(jq -r '.status // "unknown"' "${result_file}")"
      if [ "${status}" != "failed" ] && [ "${status}" != "broken" ]; then
        continue
      fi

      while IFS= read -r source; do
        [ -z "${source}" ] && continue
        attachment="${ALLURE_DIR}/${source}"
        if [ ! -f "${attachment}" ]; then
          attachment="${ALLURE_DIR}/$(basename "${source}")"
        fi
        if [ ! -f "${attachment}" ]; then
          continue
        fi

        att_type="$(jq -r --arg src "${source}" '.attachments[]? | select(.source == $src) | .type // ""' "${result_file}")"
        att_name="$(jq -r --arg src "${source}" '.attachments[]? | select(.source == $src) | .name // ""' "${result_file}")"
        base="$(basename "${attachment}")"

        if [[ "${att_type}" == video/* ]] \
          || [[ "${att_name}" == *[Vv]ideo* ]] \
          || [[ "${base}" == *.webm ]] \
          || [[ "${base}" == *.mp4 ]]; then
          cp -f "${attachment}" "${STAGE_DIR}/videos/${base}"
          echo "Staged failed-test video: ${base}"
        elif [[ "${att_type}" == image/* ]] \
          || [[ "${att_name}" == *[Ss]creenshot* ]] \
          || [[ "${base}" == *.png ]] \
          || [[ "${base}" == *.jpg ]]; then
          cp -f "${attachment}" "${STAGE_DIR}/screenshots/${base}"
          echo "Staged failed-test screenshot: ${base}"
        elif [[ "${att_type}" == application/zip ]] \
          || [[ "${att_name}" == *[Tt]race* ]] \
          || [[ "${base}" == *.zip ]]; then
          cp -f "${attachment}" "${STAGE_DIR}/traces/${base}"
          echo "Staged failed-test trace attachment: ${base}"
        fi
      done < <(jq -r '.attachments[]? | .source // empty' "${result_file}")
    done
    shopt -u nullglob
  fi
fi

echo "Artifact staging complete: ${STAGE_DIR}"
