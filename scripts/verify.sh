#!/usr/bin/env bash
set -euo pipefail

if [[ ! -x ./gradlew ]]; then
  echo "BLOCKED: ./gradlew not found. Create the Android project before full verification." >&2
  exit 2
fi

./gradlew clean test lint assembleDebug
