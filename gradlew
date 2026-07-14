#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.10.2"
DIST_DIR=".gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
GRADLE_BIN="${DIST_DIR}/gradle-${GRADLE_VERSION}/bin/gradle"

if [[ ! -x "${GRADLE_BIN}" ]]; then
  mkdir -p "${DIST_DIR}"
  ZIP="${DIST_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
  if [[ ! -f "${ZIP}" ]]; then
    if ! command -v curl >/dev/null 2>&1; then
      echo "BLOCKED: curl is required to bootstrap Gradle ${GRADLE_VERSION}." >&2
      exit 2
    fi
    curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "${ZIP}"
  fi
  unzip -q -o "${ZIP}" -d "${DIST_DIR}"
fi

exec "${GRADLE_BIN}" "$@"
