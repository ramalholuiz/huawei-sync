#!/usr/bin/env bash
set -u

if (( $# != 0 )); then
  echo "BLOCKED: scripts/check-gate1-baseline.sh does not accept arguments." >&2
  exit 2
fi

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
status=0

blocked() {
  echo "BLOCKED: $*" >&2
  status=1
}

require_file() {
  local relative_path=$1
  [[ -s "$repo_root/$relative_path" ]] || blocked "required checked-in file is missing or empty: $relative_path"
}

require_executable() {
  local relative_path=$1
  [[ -x "$repo_root/$relative_path" ]] || blocked "required checked-in script is not executable: $relative_path"
}

require_text() {
  local relative_path=$1
  local expected=$2
  local description=$3
  if [[ ! -f "$repo_root/$relative_path" ]] || ! grep -Fq -- "$expected" "$repo_root/$relative_path"; then
    blocked "$description ($relative_path)"
  fi
}

require_absent_text() {
  local relative_path=$1
  local forbidden=$2
  local description=$3
  if [[ -f "$repo_root/$relative_path" ]] && grep -Fq -- "$forbidden" "$repo_root/$relative_path"; then
    blocked "$description ($relative_path)"
  fi
}

require_file gradlew
require_file gradlew.bat
require_file gradle/wrapper/gradle-wrapper.jar
require_file gradle/wrapper/gradle-wrapper.properties
require_file scripts/verify.sh
require_file scripts/verify.ps1
require_file docs/gate1-runtime-validation.md
require_file app/src/main/AndroidManifest.xml
require_file app/src/main/java/dev/lui/huaweisync/MainActivity.kt
require_file app/src/main/java/dev/lui/huaweisync/domain/SyntheticWorkoutFactory.kt
require_file app/src/main/java/dev/lui/huaweisync/health/HealthWorkoutMapper.kt
require_file app/src/main/java/dev/lui/huaweisync/data/AppDatabase.kt
require_file app/src/main/java/dev/lui/huaweisync/data/SyncLedgerEntity.kt
require_file app/src/main/java/dev/lui/huaweisync/data/SyncLedgerStore.kt
require_file app/src/main/java/dev/lui/huaweisync/diagnostics/Gate1Diagnostics.kt
require_file app/src/main/java/dev/lui/huaweisync/diagnostics/Gate1RuntimeDiagnostics.kt
require_file app/src/main/java/dev/lui/huaweisync/health/HealthConnectWorkoutInspector.kt
require_file app/src/test/java/dev/lui/huaweisync/data/SyncLedgerStoreTest.kt
require_file app/src/test/java/dev/lui/huaweisync/health/Gate1SyncCoordinatorTest.kt
require_file app/src/test/java/dev/lui/huaweisync/health/HealthConnectWorkoutInspectorTest.kt
require_file app/src/test/java/dev/lui/huaweisync/diagnostics/Gate1DiagnosticsTest.kt
require_file app/src/test/java/dev/lui/huaweisync/diagnostics/Gate1RuntimeDiagnosticsTest.kt
require_file app/src/test/java/dev/lui/huaweisync/Gate1DiagnosticsScreenPolicyTest.kt
require_executable gradlew
require_executable scripts/verify.sh
require_executable scripts/check-gate1-baseline.sh

require_text scripts/verify.sh './gradlew clean test lint assembleDebug' \
  'POSIX verification must run the complete Gradle command'
require_text scripts/verify.ps1 '& $wrapper clean test lint assembleDebug' \
  'Windows verification must run the complete Gradle command through gradlew.bat'
require_text scripts/verify.ps1 '$repoRoot = Split-Path -Parent $PSScriptRoot' \
  'Windows verification must resolve the repository independently of the caller location'
require_text scripts/verify.ps1 'exit $gradleExitCode' \
  'Windows verification must propagate Gradle failures'
require_text docs/gate1-runtime-validation.md '## Windows PowerShell procedure' \
  'runtime guide must contain a Windows-native procedure'
require_text docs/gate1-runtime-validation.md '## POSIX shell procedure' \
  'runtime guide must contain a POSIX procedure'
require_text docs/gate1-runtime-validation.md 'write_attempt_count=1' \
  'runtime guide must define the three-run write-attempt invariant'
require_text docs/gate1-runtime-validation.md 'health_connect_match_count=1' \
  'runtime guide must define the authoritative Health Connect count invariant'
require_text docs/gate1-runtime-validation.md 'room_row_count=0' \
  'runtime guide must define the post-reinstall empty-ledger observation'
require_text docs/gate1-runtime-validation.md 'version_match=true' \
  'runtime guide must require the expected client-record version match'
require_text docs/gate1-runtime-validation.md 'Gate 1 remains **BLOCKED**' \
  'runtime guide must not convert a procedure into device evidence'
require_text docs/gates.md 'docs/gate1-runtime-validation.md' \
  'gate checklist must link its authoritative runtime procedure'
require_text docs/progress.md 'Runtime evidence recorded: no' \
  'progress must distinguish implementation from unrecorded device evidence'
require_text app/src/main/AndroidManifest.xml 'android.permission.health.READ_EXERCISE' \
  'manifest must declare exercise read permission'
require_text app/src/main/AndroidManifest.xml 'android.permission.health.WRITE_EXERCISE' \
  'manifest must declare exercise write permission'
require_text app/src/main/java/dev/lui/huaweisync/domain/SyntheticWorkoutFactory.kt \
  'WorkoutMetadataPolicy.clientRecordIdFor' \
  'synthetic record ID must come from the deterministic metadata policy'
require_text app/src/main/java/dev/lui/huaweisync/health/HealthWorkoutMapper.kt \
  'clientRecordId = metadata.clientRecordId' \
  'Health Connect metadata must receive the deterministic client record ID'
require_text app/src/main/java/dev/lui/huaweisync/health/HealthWorkoutMapper.kt \
  'clientRecordVersion = metadata.clientRecordVersion' \
  'Health Connect metadata must receive the client record version'
require_text app/src/main/java/dev/lui/huaweisync/data/SyncLedgerEntity.kt \
  'tableName = "sync_ledger"' \
  'Room ledger entity must remain present'
require_text app/src/main/java/dev/lui/huaweisync/diagnostics/Gate1Diagnostics.kt \
  'It never carries record IDs, payloads, or provider/exception text.' \
  'diagnostic export must document its privacy boundary'
require_text app/src/main/java/dev/lui/huaweisync/MainActivity.kt \
  'DiagnosticNextAction.NONE -> durableStatus == SyncStatus.VERIFIED' \
  'verified diagnostics must expose the explicit third idempotency run without opening other states'
require_absent_text docs/gates.md 'Status: PASS' \
  'Gate 1 must not be marked PASS without recorded device evidence'

if (( status != 0 )); then
  exit "$status"
fi

echo "PASS: Gate 1 checked-in baseline and cross-platform runtime contract are present."
