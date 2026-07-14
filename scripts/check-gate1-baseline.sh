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
    blocked "$relative_path is missing $description"
  fi
}

required_files=(
  settings.gradle.kts
  build.gradle.kts
  app/build.gradle.kts
  gradle.properties
  gradle/libs.versions.toml
  gradle/wrapper/gradle-wrapper.properties
  gradle/wrapper/gradle-wrapper.jar
  gradlew
  gradlew.bat
  app/src/main/AndroidManifest.xml
  app/src/main/java/dev/lui/huaweisync/MainActivity.kt
  app/src/main/java/dev/lui/huaweisync/data/AppDatabase.kt
  app/src/main/java/dev/lui/huaweisync/data/SyncLedgerDao.kt
  app/src/main/java/dev/lui/huaweisync/data/SyncLedgerEntity.kt
  app/src/main/java/dev/lui/huaweisync/domain/SyntheticWorkoutFactory.kt
  app/src/main/java/dev/lui/huaweisync/health/Gate1SyncCoordinator.kt
  app/src/main/java/dev/lui/huaweisync/health/HealthConnectWorkoutWriter.kt
  app/src/main/java/dev/lui/huaweisync/health/HealthWorkoutMapper.kt
  app/src/test/java/dev/lui/huaweisync/domain/SyntheticWorkoutFactoryTest.kt
  app/src/test/java/dev/lui/huaweisync/health/Gate1SyncCoordinatorTest.kt
  app/src/test/java/dev/lui/huaweisync/health/HealthWorkoutMapperTest.kt
  README.md
  docs/gate1-static-audit.md
  docs/gates.md
  docs/progress.md
)

for relative_path in "${required_files[@]}"; do
  require_file "$relative_path"
done
require_executable gradlew
require_executable scripts/check-gate1-baseline.sh

require_text settings.gradle.kts 'include(":app")' 'the :app module declaration'
require_text app/build.gradle.kts 'compileSdk = 36' 'the statically audited compile SDK value'
require_text app/build.gradle.kts 'targetSdk = 35' 'the statically audited target SDK value'
require_text app/build.gradle.kts 'JavaVersion.VERSION_17' 'the Java 17 compile contract'
require_text gradle/libs.versions.toml 'healthConnect = "1.1.0"' 'the audited Health Connect version'
require_text gradle/libs.versions.toml 'room = "2.6.1"' 'the audited Room version'
require_text gradle/wrapper/gradle-wrapper.properties 'gradle-8.11.1-bin.zip' 'the Gradle 8.11.1 distribution URL'
require_text gradle/wrapper/gradle-wrapper.properties 'distributionSha256Sum=' 'a pinned distribution checksum'
require_text gradlew 'org.gradle.wrapper.GradleWrapperMain' 'the wrapper main class invocation'
require_text gradlew.bat 'org.gradle.wrapper.GradleWrapperMain' 'the Windows wrapper main class invocation'
require_text app/src/main/AndroidManifest.xml 'android.permission.health.READ_EXERCISE' 'the exercise read permission declaration'
require_text app/src/main/AndroidManifest.xml 'android.permission.health.WRITE_EXERCISE' 'the exercise write permission declaration'
require_text app/src/main/java/dev/lui/huaweisync/domain/SyntheticWorkoutFactory.kt 'CLIENT_RECORD_ID' 'the deterministic client record identifier'
require_text app/src/main/java/dev/lui/huaweisync/health/HealthWorkoutMapper.kt 'Metadata.manualEntry' 'Health Connect metadata mapping'
require_text app/src/main/java/dev/lui/huaweisync/data/SyncLedgerEntity.kt '@Entity' 'the Room ledger entity'
require_text app/src/test/java/dev/lui/huaweisync/health/Gate1SyncCoordinatorTest.kt 'repeat(3)' 'the three-call fake-writer contract case'

if [[ -s "$repo_root/gradle/wrapper/gradle-wrapper.jar" ]] && ! grep -aFq 'org/gradle/wrapper/GradleWrapperMain.class' "$repo_root/gradle/wrapper/gradle-wrapper.jar"; then
  blocked "gradle/wrapper/gradle-wrapper.jar does not contain the expected GradleWrapperMain entry"
fi

for label in STATIC BLOCKED DEFERRED; do
  require_text docs/gate1-static-audit.md "$label" "the $label evidence classification"
done
require_text docs/gates.md 'Status: BLOCKED' 'an honest Gate 1 status'
require_text docs/gates.md 'Static evidence only' 'the static-versus-runtime evidence boundary'
require_text docs/progress.md 'Gate 1 remains BLOCKED' 'the corrected current status'
require_text README.md 'Gate 1 remains `BLOCKED`' 'the current Gate 1 status'
require_text README.md 'docs/gate1-static-audit.md' 'the baseline audit link'

if (( status != 0 )); then
  echo "DEFERRED: Gradle, tests, lint, APK assembly, installation, Health Connect, device, and reinstall proof were not run."
  exit "$status"
fi

echo "STATIC: checked-in Gate 1 configuration, complete wrapper surface, source/test seams, and evidence documents match the audited baseline."
echo "BLOCKED: Gate 1 has no desktop build/lint/APK evidence and no real Health Connect or reinstall evidence."
echo "DEFERRED: run the desktop and device procedures supplied by later validation work; this checker did not invoke Gradle."
