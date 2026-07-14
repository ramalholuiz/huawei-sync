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

Verification evidence:

- `git diff --check` passed locally.
- `./scripts/tooling-doctor.sh` passed for its current checks: node, python3, uv, graphify, gsd.
- `./scripts/verify.sh` is blocked in this environment before Gradle runs: `Unable to locate a Java Runtime`.

Runtime evidence still required before Gate 1 can be marked `PASS`:

- Run `./scripts/verify.sh` in an environment with Java Runtime and Android SDK.
- Install/run the debug app on an Android device with Health Connect.
- Record device model and Android version.
- Record Health Connect availability status shown by the app.
- Grant ExerciseSessionRecord read/write permissions and record permission state screenshots or notes.
- Run the synthetic sync three times.
- Record the synthetic `clientRecordId` and `clientRecordVersion` shown by the app.
- Record number of matching Health Connect exercise-session records before and after three write attempts.

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
