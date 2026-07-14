# Gate 1 checked-in baseline audit

Audited: 2026-07-14. Scope: repository files only.

## Evidence vocabulary

- **STATIC** — observed in checked-in text or binary structure; no Gradle, Android, or Health Connect behavior is implied.
- **BLOCKED** — required Gate 1 proof is missing or the checked-in implementation does not yet meet its contract.
- **DEFERRED** — an exact desktop or device procedure must be run later in a suitable environment.

Gate 1 remains **BLOCKED**. This audit is not build, test, lint, APK, installation, Health Connect, deduplication, or reinstall evidence.

## Inspected surfaces

| Surface | Files inspected | Classification | Observation |
| --- | --- | --- | --- |
| Gradle project | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml` | STATIC | One `:app` module declares compile SDK 36, target SDK 35, Java/Kotlin 17, AGP 8.10.1, Kotlin 1.9.25, Health Connect 1.1.0, and Room 2.6.1. Compatibility has not been executed. |
| Official wrapper surface | `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/wrapper/gradle-wrapper.jar` | STATIC | The complete standard Gradle wrapper surface is checked in. The JAR is a readable ZIP containing `GradleWrapperMain`; properties pin Gradle 8.11.1 and a distribution SHA-256. No wrapper download or Gradle command was run. |
| Android declaration | `app/src/main/AndroidManifest.xml`, `app/src/main/res/` | STATIC | Application/activity, provider query, and exercise read/write permissions are declared; backup is disabled. Manifest merge, lint, platform behavior, and provider compatibility remain unproved. |
| Domain and mapping | `domain/DomainWorkout.kt`, `domain/SyntheticWorkoutFactory.kt`, `health/HealthWorkoutMapper.kt` | STATIC | A synthetic strength session maps to `ExerciseSessionRecord` metadata with a constant client ID and version. The factory's times and offsets are environment-dependent. |
| Persistence | `data/AppDatabase.kt`, `data/SyncLedgerEntity.kt`, `data/SyncLedgerDao.kt`, `HuaweiSyncApp.kt` | STATIC / BLOCKED | Room database version 1 and a unique primary `clientRecordId` row exist. The ledger contract is incomplete and no migration/reinstall proof exists. |
| Orchestration | `health/Gate1SyncCoordinator.kt`, `health/HealthConnectWorkoutWriter.kt`, availability/permission helpers | STATIC / BLOCKED | A direct insert-then-upsert path exists. It has no persisted attempt state, accepted-write uncertainty handling, readback, confirmation, or reconciliation. |
| Diagnostics | `MainActivity.kt` | STATIC / BLOCKED | Availability, permission request, and a manual trigger exist. Success text reports local write count, not Health Connect record count; exceptions may expose raw messages. |
| Tests | all three files under `app/src/test/` | STATIC / BLOCKED | Pure mapping/identity checks and a Robolectric Room test are checked in. They were not run here and no instrumented/device tests exist. |
| Documentation and scripts | `README.md`, `docs/*.md`, `scripts/*.sh`, `.gitignore`, `AGENTS.md`, `skills-lock.json` | STATIC | Scope rules and verification entrypoints exist. This audit corrects earlier evidence wording; desktop validation remains deferred. |
| Auxiliary files | root entries, resource XML, ignore rules, wrapper binary | STATIC | No checked-in secret/config file was found. `local.properties` is intentionally ignored and absent, so Android SDK discovery is a desktop prerequisite. |

## Structure preserved

The existing single-module Compose app, source-independent domain seam, AndroidX adapter seam, Room database, deterministic client record metadata, availability/permission helpers, and preliminary unit tests are useful foundations. This task does not replace them, upgrade dependencies, or add Huawei, Strava, GymRats, WorkManager, steps, or extra metrics.

## Concrete defects and disposition

| # | Defect observed in checked-in code | Why it blocks proof | Disposition |
| --- | --- | --- | --- |
| 1 | `SyncLedgerEntity` lacks logical `(sourceProvider, sourceRecordId)` uniqueness, canonical `contentHash`, attempt count, explicit state, safe error details, created/updated timestamps, accepted-write facts, confirmation, and reconciliation state. `@Upsert` is the only transition primitive. | One row and a successful-write counter cannot represent retries or partial failure safely. | **S02:** deterministic identity/version and durable ledger state contract. |
| 2 | `SyntheticWorkoutFactory.create()` derives payload times from the current clock and zone while always using version 1. | Calls at different times can change semantic content without changing version. | **S02:** controlled fixture content, canonical hash, and stable semantic version rules. |
| 3 | `Gate1SyncCoordinator` calls the writer on every invocation. Its three-run fake test explicitly records three writer calls and proves only one local Room row. | A fake writer and Room count do not prove one real Health Connect record or idempotent AndroidX behavior. | **S03:** write/finalize/confirm/reconcile ports and negative orchestration tests; **DEFERRED device proof:** real three-run record counts. |
| 4 | No `readRecords`/readback path exists. An external success followed by Room failure is not represented, and a retry blindly inserts again. | Accepted-write uncertainty and reinstall recovery are unresolved. | **S03:** confirmation/reconciliation flow; **DEFERRED device proof:** real readback and reinstall scenario. |
| 5 | `MainActivity` labels the result “Synced” after insert/upsert and may display raw `Exception.message`. | The UI overstates proof and can disclose uncontrolled provider/database details. | **S04:** explicit privacy-safe states, codes, actions, and exportable diagnostics. |
| 6 | No checked-in run proves Gradle dependency resolution, compilation, unit tests, lint, APK assembly, manifest merge, install, permission grant, or Health Connect behavior. | Static inspection cannot establish executability or runtime behavior. | **DEFERRED desktop/device proof:** use the validation procedure introduced separately; keep Gate 1 BLOCKED. |
| 7 | No instrumented tests or persistent-after-reinstall evidence exist. Room data is removed by reinstall. | Reinstall deduplication cannot be inferred from local persistence or constant metadata alone. | **DEFERRED device proof** after S02-S04 implement reconciliation. |

## Static checker

Run from the repository root:

```sh
bash scripts/check-gate1-baseline.sh
```

The checker reads a fixed set of local files, verifies the complete wrapper/config/source/test/documentation surface, and emits `STATIC`, `BLOCKED`, and `DEFERRED` classifications. It never invokes Gradle, accesses a network, handles secrets, or treats a missing prerequisite as success.

## Deferred proof boundary

The minimum later desktop command remains:

```sh
./gradlew clean test lint assembleDebug
```

A successful desktop run still cannot prove installation, permissions, Health Connect write/read behavior, one-record three-run behavior, or reinstall continuity. Those require the target Android environment and recorded evidence. Gate 2 must not start until Gate 1 has real runtime proof.
