# AGENTS.md

## Project mission
Build a personal sideloaded Android app that syncs workouts from Huawei Health into Android Health Connect so apps such as GymRats can consume them.

Primary flow: Huawei Watch Fit 5 Pro → Huawei Health → this app → Health Connect → GymRats.

Optional secondary flow for later only: Huawei Health → Strava → this app → Health Connect. Do not implement Strava before Gate 1 and Gate 2 pass, and never import the same workout from both sources without deterministic deduplication.

## Hard rules
- Do not invent APIs, permission names, SDK names, record types, or undocumented Huawei/Strava fields.
- Confirm names, versions, permissions, and behavior in official documentation before implementation.
- Use only official Huawei APIs/SDKs for Huawei integration. No reverse engineering and no private app storage access.
- Keep Huawei and Strava SDK/data-transfer types outside the domain model.
- Every record written to Health Connect must have a deterministic identifier.
- Deduplication must continue to work after app reinstall.
- Every bug fix must include a regression test.
- Do not add heart rate, calories, distance, or other metrics before the vertical slice works.
- Do not mark work complete without running tests, lint, and build.

## Gates before full product
1. Gate 1: Health Connect synthetic ExerciseSessionRecord with deterministic clientRecordId, Room ledger, and no duplicate on repeated writes.
2. Gate 2: Manual GymRats validation that the synthetic ExerciseSessionRecord written to Health Connect is imported.
3. Gate 3: Huawei official SDK/API authorization and at least one real Huawei Health workout read, with raw payload and normalized model shown in diagnostics.
4. Gate 4: Real Huawei workout converted to domain model, written to Health Connect, visible in GymRats, and synced three times without duplication.

## MVP scope after gates
- Manual sync.
- Initial import of the last seven days.
- Room ledger.
- Temporal overlap window.
- Idempotent updates.
- Exportable logs.
- Permission and error status.
- Heart rate, calories, and distance only when available, and only after Gate 1 and Gate 2 pass.

## Out of scope for MVP
- Backend, Firebase, analytics, reverse engineering, private storage reads, sleep, SpO2, weight, body composition, sophisticated UI, and any direct GymRats integration.

## Verification
- Prefer `scripts/verify.sh` for local verification.
- The final definition of done requires `./gradlew clean test lint assembleDebug` and `scripts/verify.sh` to pass.
- Follow `docs/engineering-standards.md` for sync, Bluetooth, test, and release standards.
