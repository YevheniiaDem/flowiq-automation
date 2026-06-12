#!/usr/bin/env bash
set -euo pipefail

BACKEND_DIR="${1:-flowiq-backend}"
LOG_FILE="${2:-backend.log}"
PID_FILE="${3:-backend.pid}"

if [ ! -d "${BACKEND_DIR}" ]; then
  echo "::error::Backend directory '${BACKEND_DIR}' not found."
  exit 1
fi

cd "${BACKEND_DIR}"

echo "Building backend JAR..."
mvn -B -DskipTests package -q

JAR="$(find target -maxdepth 1 -name 'flowiq-backend-*.jar' ! -name '*-sources.jar' | head -n 1)"
if [ -z "${JAR}" ]; then
  echo "::error::Backend JAR not found under ${BACKEND_DIR}/target"
  exit 1
fi

echo "Starting backend: ${JAR}"
nohup java -jar "${JAR}" \
  --spring.docker.compose.enabled=false \
  --spring.datasource.url="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/flowiq}" \
  --spring.datasource.username="${SPRING_DATASOURCE_USERNAME:-flowiq}" \
  --spring.datasource.password="${SPRING_DATASOURCE_PASSWORD:-flowiq123}" \
  > "../${LOG_FILE}" 2>&1 &

echo $! > "../${PID_FILE}"
echo "Backend PID: $(cat "../${PID_FILE}")"
