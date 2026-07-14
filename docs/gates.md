# Gates and evidence

Status values: `TODO`, `PASS`, `BLOCKED`.

## Gate 1: Health Connect synthetic write

Status: BLOCKED

Purpose: write one synthetic strength-training `ExerciseSessionRecord` to Health Connect idempotently.

Checklist:

- [x] Android Kotlin + Jetpack Compose project exists.
- [x] Health Connect availability is detected.
- [x] Required Health Connect permissions for exercise sessions are requested.
- [x] Synthetic strength `ExerciseSessionRecord` write path is implemented.
- [x] Synthetic record uses deterministic `metadata.clientRecordId` reproducible after reinstall.
- [x] `metadata.clientRecordVersion` strategy is defined.
- [x] Room sync ledger exists from this gate onward.
- [x] Repeated insert/update is covered by a three-run idempotency test against the Room ledger.
- [x] No `StepsRecord`, WorkManager, Strava, or additional workout metrics are implemented in Gate 1 app code.

Ledger fields required from Gate 1:

- [x] `clientRecordId`
- [x] source
- [x] source workout id
- [x] dedupe key
- [x] client record version
- [x] Health Connect record id when available
- [x] last synced timestamp
- [x] successful write count

Implementation evidence added:

- Android project: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `gradlew`.
- Compose entry point: `app/src/main/java/dev/lui/huaweisync/MainActivity.kt`.
- Health Connect availability and permission handling: `HealthConnectAvailabilityChecker`, `HealthConnectPermissions`.
- Synthetic strength session mapping: `SyntheticWorkoutFactory`, `HealthWorkoutMapper`.
- Deterministic ID: `huawei-sync:synthetic:gate1-strength-training`.
- Client record version strategy: baseline semantic payload uses `clientRecordVersion = 1`; increment only when the same deterministic record's semantic payload changes.
- Room ledger: `AppDatabase`, `SyncLedgerEntity`, `SyncLedgerDao` with unique primary `clientRecordId`.
- Idempotency test: `Gate1SyncCoordinatorTest` runs sync three times and asserts one ledger row for the deterministic client record id.

Build, install, and launch evidence (`VERIFIED`):

- Commit `15e3fbd` provides the checksum-verified Gradle Wrapper 8.11.1, AGP 8.10.1, `compileSdk 36`, `targetSdk 35`, and Health Connect 1.1.0 baseline.
- Windows verification had usable Java and passed `clean test lint assembleDebug` through the canonical `scripts/verify.sh` entry point.
- Debug and release unit tests passed, and the debug APK was generated.
- `adb` installed the debug APK successfully on `emulator-5554`.
- `MainActivity` launched without an immediate crash.
- The app reported Health Connect `Available`.

This settles the build/install/launch baseline; it does not settle the Health Connect runtime contract. Gate 1 therefore remains `BLOCKED` on exactly these five runtime proofs:

- actual Health Connect permission grant for the exercise-session read/write permissions;
- a real synthetic `ExerciseSessionRecord` write;
- real Health Connect readback of the synthetic record;
- three-run idempotency evidence against both Health Connect and the Room ledger;
- reinstall behavior confirming that deterministic deduplication survives app reinstall.

This list is exhaustive for Gate 1. Manual GymRats validation belongs to Gate 2 and is not a Gate 1 blocker.

Remaining milestone ownership is frozen as follows:

- S02 implements deterministic identity, the canonical content hash, stable semantic versioning, and the complete Room ledger schema and transitions.
- S03 implements and tests the coordinator write, finalize, confirm, and reconcile contracts.
- S04 implements diagnostics and the runtime validation procedure that collects the five Gate 1 proofs above.

Huawei and Strava integrations, direct GymRats APIs, WorkManager, `StepsRecord`, and P1 metrics remain out of scope for Gate 1. `scripts/verify.sh` remains unchanged as the canonical build-verification entry point.

## Gate 2: GymRats manual import validation

Status: TODO

Purpose: manually confirm GymRats imports the synthetic workout from Health Connect.

Checklist:

- [ ] GymRats is installed on the owner's device.
- [ ] GymRats has Health Connect permission to read exercise sessions.
- [ ] The Gate 1 synthetic workout is visible in Health Connect.
- [ ] GymRats imports/displays the Gate 1 synthetic workout.
- [ ] Result is recorded as `PASS` or `BLOCKED` with evidence.
- [ ] No direct GymRats integration exists.

Evidence to record:

- GymRats app version.
- Health Connect permission state for GymRats.
- Synthetic `clientRecordId` tested.
- Health Connect observation.
- GymRats observation.
- Screenshots or written reproduction notes.

## Gate 3: Huawei real workout read

Status: TODO

Purpose: use official Huawei SDKs/APIs to authorize the owner and read one real Huawei Health workout.

Checklist:

- [ ] Huawei Developer account created.
- [ ] AppGallery Connect project created.
- [ ] Permanent package name selected.
- [ ] Release keystore created and stored outside repository.
- [ ] Signing certificate configured in Huawei console.
- [ ] Health Service Kit / Health Kit enabled.
- [ ] Required official SDK/API dependencies documented.
- [ ] User authorization works.
- [ ] At least one real Huawei Health workout is read.
- [ ] Raw payload is shown on diagnostics screen.
- [ ] Normalized domain model is shown on diagnostics screen.
- [ ] No Health Connect write of Huawei data is considered complete until Gate 4.

Evidence to record:

- AppGallery Connect configuration notes.
- SDK dependency names and versions.
- Permission/scope names from official docs.
- Approval status.
- Redacted raw payload sample.
- Normalized workout sample.

## Gate 4: Huawei to Health Connect to GymRats vertical slice

Status: TODO

Purpose: convert one real Huawei workout to the domain model, write it as an `ExerciseSessionRecord`, and manually confirm GymRats imports it from Health Connect.

Checklist:

- [ ] Real Huawei workout converts to source-independent domain model.
- [ ] Domain model converts to `ExerciseSessionRecord`.
- [ ] Workout writes to Health Connect with deterministic `metadata.clientRecordId` reproducible after reinstall.
- [ ] Room ledger records source identity, deterministic ID, content hash, Health Connect ID, status, attempts, and errors.
- [ ] GymRats displays/imports the workout from Health Connect.
- [ ] Three sync executions do not create duplicates.
- [ ] Changed workout data reconciles.
- [ ] No direct GymRats integration exists.

Evidence to record:

- Source Huawei workout identifier.
- Deterministic domain sync key.
- Health Connect `clientRecordId` and `clientRecordVersion`.
- Ledger row before and after each sync.
- Before/after Health Connect record counts.
- GymRats validation notes.
- Failure and retry notes, if any.
