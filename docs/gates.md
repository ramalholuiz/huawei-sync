# Gates and evidence

Status values: `TODO`, `PASS`, `BLOCKED`.

## Gate 1: Health Connect synthetic write

Status: TODO

Purpose: write one synthetic strength-training `ExerciseSessionRecord` to Health Connect idempotently.

Checklist:

- [ ] Android Kotlin + Jetpack Compose project exists.
- [ ] Health Connect availability is detected.
- [ ] Required Health Connect permissions for exercise sessions are requested.
- [ ] Synthetic strength `ExerciseSessionRecord` is inserted.
- [ ] Synthetic record uses deterministic `metadata.clientRecordId` reproducible after reinstall.
- [ ] `metadata.clientRecordVersion` strategy is defined.
- [ ] Room sync ledger exists from this gate onward.
- [ ] Repeated insert/update does not create duplicates.
- [ ] No `StepsRecord`, WorkManager, Strava, or additional metrics are implemented in this gate.

Required ledger fields:

- [ ] `sourceProvider`
- [ ] `sourceRecordId`
- [ ] `deterministicClientRecordId`
- [ ] `sourceModifiedAt` or `sourceVersion`
- [ ] `contentHash`
- [ ] `healthConnectRecordId`
- [ ] `syncStatus`
- [ ] `attemptCount`
- [ ] `lastError`
- [ ] `createdAt`
- [ ] `updatedAt`

Evidence to record:

- Device model and Android version.
- Health Connect availability status.
- Permission state screenshots or notes.
- Synthetic `clientRecordId`.
- `clientRecordVersion` behavior.
- Number of records before and after three write attempts.
- Ledger rows after each attempt.

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
