# Implementation plan

## Goal
Build a personal sideloaded Android app that syncs Huawei Health workouts into Android Health Connect so GymRats can consume them.

GymRats is **not** an integration target for this app. GymRats already consumes Health Connect directly, so validation is exclusively manual: write an `ExerciseSessionRecord` to Health Connect and confirm GymRats imports it. Do not add any direct GymRats integration or app-to-GymRats communication path.

## Priorities

- P0: `ExerciseSessionRecord` only.
- P1 after Gate 1 and Gate 2 pass: calories, distance, and heart rate associated with a workout, only when available from the source.
- Outside the initial MVP: steps, sleep, SpO2, weight, and body composition.

Daily steps are intentionally excluded from the vertical slice because GymRats treats workouts, not daily step totals, as the relevant import target.

## Phase 0: repository foundation

Deliverables:

- Permanent project rules in `AGENTS.md`.
- API research and limitations in `docs/research.md`.
- Gate plan and evidence log in `docs/gates.md`.
- Progress log in `docs/progress.md`.
- Data mapping plan in `docs/data-mapping.md`.
- Verification entry point in `scripts/verify.sh`.

Exit criteria:

- Documentation names the gates, blockers, and official references.
- Verification script exists and is executable.

## Gate 1: Health Connect synthetic write

Purpose: prove this app can write one synthetic workout record to Health Connect idempotently.

Deliverables:

- Android project in Kotlin and Jetpack Compose.
- Health Connect availability detection.
- Runtime permission request flow for writing/reading exercise sessions.
- Synthetic strength-training `ExerciseSessionRecord` insertion.
- Deterministic and reinstall-reproducible `metadata.clientRecordId` for the synthetic record.
- Room-backed sync ledger present from this gate onward.
- Repeat insertion/update without creating duplicates.

Implementation notes:

- Use `androidx.health.connect:connect-client` with an officially confirmed version.
- The Gate 1 record must use a deterministic ID such as `huawei-sync:synthetic:gate1-strength-fixed`.
- Do not depend on the local database to prevent duplicates. The local ledger is for audit, reconciliation, status, retries, and diagnostics; the `clientRecordId` must be reproducible after reinstall.
- Use `metadata.clientRecordVersion` so updates are explicit and idempotent.
- Use Room for the sync ledger from the vertical slice onward.
- Use DataStore only for preferences and simple checkpoints.

Room ledger fields required from the start:

- `sourceProvider`
- `sourceRecordId`
- `deterministicClientRecordId`
- `sourceModifiedAt` or `sourceVersion`
- `contentHash`
- `healthConnectRecordId`
- `syncStatus`
- `attemptCount`
- `lastError`
- `createdAt`
- `updatedAt`

Exit criteria:

- Three repeated Gate 1 writes leave one logical workout in Health Connect.
- The ledger records each attempt/status without being the source of deduplication truth.
- Tests, lint, and build run through `scripts/verify.sh` once the Android project exists.

## Gate 2: GymRats manual import validation

Purpose: prove GymRats consumes the synthetic Health Connect workout written by this app.

Deliverables:

- Manual checklist for validating GymRats import from Health Connect.
- Evidence that GymRats has Health Connect permission to read exercise sessions.
- Evidence that the synthetic Gate 1 `ExerciseSessionRecord` appears in GymRats, or a documented `BLOCKED` result.

Explicit non-goal:

- No direct GymRats integration; validation is manual through Health Connect only.

Exit criteria:

- GymRats imports/displays the synthetic `ExerciseSessionRecord` from Health Connect, or the project is blocked/pivoted with evidence.

## Gate 3: Huawei real workout read

Purpose: prove the app can use official Huawei APIs/SDKs to authorize the owner and read one real Huawei Health workout.

Deliverables:

- Documented AppGallery Connect setup.
- Permanent package name.
- Permanent release keystore stored outside the repository.
- Official Huawei SDK/API integration only.
- User authorization for Huawei Health data.
- At least one real workout read from Huawei Health.
- Diagnostic screen showing the raw Huawei payload and normalized domain model.

Exit criteria:

- A real Huawei Health workout from Huawei Watch Fit 5 Pro is visible in diagnostics.
- Required Huawei permissions/scopes, SDK dependencies, versions, and approval state are documented from official sources.

## Gate 4: Huawei to Health Connect to GymRats vertical slice

Purpose: prove the real end-to-end flow without any direct GymRats integration.

Deliverables:

- Huawei workout to source-independent domain conversion.
- Domain to Health Connect `ExerciseSessionRecord` conversion.
- Deterministic and reinstall-reproducible Health Connect `clientRecordId`.
- Room ledger reconciliation for attempts, status, hash, source version, and Health Connect record ID.
- Manual GymRats confirmation that the real Huawei workout written to Health Connect is imported.
- Three sync runs without duplicate Health Connect or GymRats entries.

Exit criteria:

- One real Huawei workout appears in Health Connect and GymRats.
- Three manual sync runs do not create duplicates.
- Changed workout data reconciles via deterministic ID/version behavior and ledger update flow.

## MVP after Gates 1-4 pass

Deliverables:

- Manual sync.
- Initial import of last seven days.
- Room ledger local state.
- Temporal overlap window.
- Idempotent updates.
- Exportable logs.
- Status for permissions and errors.
- Calories, distance, and heart rate associated with workouts, only when available from source.

Do not implement WorkManager, Strava, steps, or additional metrics before Gate 1 and Gate 2 pass. WorkManager remains a later best-effort enhancement after the core vertical slice is proven.

## Blocking criteria

Stop or pivot if:

- Health Connect cannot store the required workout representation.
- GymRats does not consume this app's `ExerciseSessionRecord` records from Health Connect.
- Huawei approvals or APIs do not expose real workouts.
- Deterministic deduplication cannot survive reinstall.
- The owner cannot install a signed APK on the target device.
