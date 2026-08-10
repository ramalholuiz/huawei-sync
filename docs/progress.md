# Progress log

## 2026-07-14 — Planning baseline

Changed:

- Created permanent project rules.
- Added research notes for Health Connect, Huawei Health Service Kit, GymRats validation, and optional Strava fallback.
- Revised implementation plan into four gates: Health Connect synthetic write, manual GymRats validation, Huawei real workout read, and final vertical slice.
- Made GymRats validation manual-only and the Room ledger mandatory from Gate 1.
- Added the data mapping strategy and canonical `scripts/verify.sh` entry point.

## 2026-07-14 — Gate 1 implementation

Changed:

- Added the Android Kotlin + Jetpack Compose Gate 1 project.
- Added Health Connect availability detection and `ExerciseSessionRecord` read/write permission request flow.
- Added a synthetic strength-training `ExerciseSessionRecord` write path with deterministic `metadata.clientRecordId`.
- Defined `clientRecordVersion = 1` as the baseline semantic payload version strategy.
- Added the Room ledger with a unique `clientRecordId` row and sync write count.
- Added local tests for deterministic ID/version, Health Connect mapping, and three-run Room ledger idempotency.
- Kept Huawei, Strava, WorkManager, StepsRecord, direct GymRats APIs, and P1 metrics out of Gate 1 app code.

## 2026-07-14 — Verified Windows baseline reconciliation

Build, install, and launch evidence (`VERIFIED`):

- Commit `15e3fbd` contains the checksum-verified Gradle Wrapper 8.11.1, AGP 8.10.1, `compileSdk 36`, `targetSdk 35`, and Health Connect 1.1.0 baseline.
- The Windows environment provided usable Java.
- `clean test lint assembleDebug` passed on the supplied Windows baseline; the repository now exposes equivalent POSIX and Windows verification scripts.
- Debug and release unit tests passed, and the debug APK was generated.
- `adb` installed the debug APK successfully on `emulator-5554`.
- `MainActivity` launched without an immediate crash.
- Health Connect availability reported `Available`.

Gate status (`BLOCKED`):

The remaining Gate 1 blockers are frozen as exactly these five runtime proofs:

1. Actual Health Connect permission grant for the exercise-session read/write permissions.
2. A real synthetic `ExerciseSessionRecord` write.
3. Real Health Connect readback of the synthetic record.
4. Three-run idempotency against both Health Connect and the Room ledger.
5. Reinstall behavior proving deterministic deduplication survives app reinstall.

This list is exhaustive. Manual GymRats validation belongs to Gate 2 and is not a Gate 1 blocker.

Slice ownership:

- S02 implements deterministic identity, the canonical content hash, stable semantic versioning, and the complete Room ledger schema and transitions.
- S03 implements and tests the coordinator write, finalize, confirm, and reconcile contracts.
- S04 implements diagnostics and the runtime validation procedure that produces the remaining Gate 1 evidence.

Huawei and Strava integrations, direct GymRats APIs, WorkManager, `StepsRecord`, and P1 metrics remain out of scope for Gate 1.

## 2026-07-15 — Cross-platform Gate 1 runtime contract

Implemented:

- Added `scripts/verify.ps1`, a Windows-native `gradlew.bat clean test lint assembleDebug` entry point with caller-location independence and failure propagation.
- Extended `scripts/check-gate1-baseline.sh` to audit both verification entry points, the diagnostics/inspector surfaces, exact runtime invariants, and continued `BLOCKED` wording.
- Added the authoritative [`docs/gate1-runtime-validation.md`](gate1-runtime-validation.md) permission, write, bounded readback, three-action, Room/Health Connect comparison, and reinstall procedure.
- Kept evidence export privacy-safe: no payloads, raw identifiers, provider messages, or exception text.

Runtime evidence recorded: no

Gate 1 remains `BLOCKED`. No permission, real write/readback, three-action device count, or reinstall evidence was produced while writing this procedure.

Next steps:

1. Run the platform verification command and exact device procedure in `docs/gate1-runtime-validation.md`.
2. Record the six sanitized checkpoint reports and environment metadata in this file.
3. Change Gate 1 to `PASS` only when every expected invariant has objective device evidence.
4. Keep manual GymRats validation in Gate 2.

## 2026-07-15 — Gate 1 physical-device attempt blocked by host prerequisites

Checkpoint outcome (`BLOCKED`):

- `scripts/verify.sh` stopped before Gradle execution because this macOS host has no usable Java runtime; no test, lint, APK build, or APK hash evidence was produced.
- `adb devices -l` could not run because `adb` is not installed or discoverable on this host.
- A bounded check of the standard Android Studio JBR, user Android SDK platform-tools, Java home registry, command path, and Spotlight index found no alternate usable JDK or `adb` installation.
- Therefore exactly-one-authorized-device, APK install/launch, Health Connect availability, and clean zero/zero Room and Health Connect baseline facts remain unproven.
- No device, Health Connect, Room, permission, or application state was changed. No serial number, health data, raw identifier, or provider payload was collected.

Required recovery:

1. Resume on a host with JDK 17, Android SDK platform 36, and `adb` available.
2. Connect exactly one authorized physical Android device with Health Connect available.
3. Restart the authoritative procedure from desktop verification and the clean baseline; do not infer any runtime checkpoint from this failed attempt.

Gate 1 remains `BLOCKED`.

## 2026-07-15 — Gate 1 authoritative Windows emulator proof

Runtime evidence recorded: yes

Final evidence (`VERIFIED`):

- Validated exact commit `2a61059955a2670949693204db3fd09d464a1e4e` in the clean detached Windows worktree on `DESKTOP-3CTKCGT`.
- Windows used OpenJDK 21.0.10, Android SDK platform 36, adb 37.0.0, and boot-complete `emulator-5554`.
- `gradlew.bat testDebugUnitTest`, `gradlew.bat testReleaseUnitTest`, `gradlew.bat clean test lint assembleDebug`, Windows Git Bash `scripts/verify.sh`, and `git diff --check` all passed.
- Debug and release each ran 103 tests with zero failures/errors. Lint reported zero errors and 15 warnings.
- Produced `app-debug.apk` at 10,927,037 bytes with SHA-256 `cac3ae07aa96de8657ecc540eeabbc863d3fbbe65faf543862ccd2eab4e9145e`.
- Fixed the real Health Connect controller prerequisite by declaring the official rationale activity and Android 14+ permission-usage alias; its regression test passed on Windows.
- Granted real exercise-session permissions through the official Health Connect UI.
- A real synthetic `ExerciseSessionRecord` write and bounded official readback produced exactly one Health Connect match, one expected-version match, one Room row, one attempt, client record version 1, and `version_match=true`.
- Fixed verified reruns so the third action is an `ALREADY_VERIFIED` no-op; the Windows regression test and real emulator action both preserved Health Connect 1, Room 1, and attempts 1.
- Uninstalling the app reset Room to 0 while Health Connect remained 1. Reinstalling the same APK and syncing reconstructed Room 1 with one attempt and no second Health Connect record.
- Sanitized exports recorded `VERIFIED`, permission `GRANTED`, exact count/version facts, closed reconciliation, and finalized local state. No record ID, provider payload, or health payload was exported.

Gate 1 status: `PASS`.

Physical-device resilience validation is deferred. Gate 2 and manual GymRats validation have not started.

## 2026-08-10 — Bluetooth infrastructure refocus

Changed:

- Added `docs/bluetooth-sync-infra.md` to freeze the next direction around source ingestion instead of more UI work.
- Added a Bluetooth infrastructure checkpoint to the plan and gate checklist.
- Recorded the BitChat Android boundary: use it as a transport reference, not as app code or chat scope.

Next steps:

1. Extract the synthetic Gate 1 source behind a source-agnostic workout reader seam.
2. Add Bluetooth capability/preflight checks before any scan/connect code.
3. Prove a controlled Bluetooth loopback with one workout-shaped payload.
4. Decide the real source path from evidence: official Huawei API, official/documented Bluetooth protocol, or controlled watch-side software.

## 2026-08-10 — Bluetooth source seam implementation

Changed:

- Added `WorkoutSourceReader` and kept `SyntheticWorkoutSource` as the default source for existing Gate 1 UI actions.
- Added `WorkoutSource.BLUETOOTH` and proved a Bluetooth-shaped workout can flow through the existing ledger and Health Connect writer boundary.
- Added a small Android Bluetooth capability reader and preflight contract with closed blocker codes.
- Added a bounded `huawei-sync-workout-v1` payload parser for controlled Bluetooth loopback tests.
- Declared the Bluetooth permissions needed before transport work: scan, connect, advertise for controlled loopback, and pre-Android-12 location compatibility.

Still not claimed:

- No Huawei Watch protocol integration exists yet.
- No scan/connect/GATT service exists yet.
- No Samsung Health, Strava, GymRats, or other app-specific direct write exists; Health Connect remains the current app-to-app sync bridge.

## 2026-08-10 — Engineering audit and repository workflow

Changed:

- Added `docs/mobile-engineering-audit.md` with the current state, gaps, and P0-P3 plan.
- Added `docs/engineering-standards.md` as the canonical definition of done for sync, Bluetooth, tests, and release work.
- Added exact issue drafts in `docs/github-issues-backlog.md`.
- Added GitHub issue templates, a PR template, and Android CI using the existing Gradle verification command.

Still blocked:

- Local Gradle verification on this macOS host still requires a Java runtime.
- GitHub issues were drafted locally, not created through GitHub.

## 2026-08-10 — Bluetooth source adapter

Changed:

- Added `BluetoothWorkoutSource`, a small adapter from a controlled Bluetooth payload reader to the existing `WorkoutSourceReader` pipeline.
- Added a unit test proving decoded Bluetooth payloads reach the source seam and malformed payloads stop before ledger writes.

Still not claimed:

- No physical Android scan/connect/read execution has been proven yet.
- No real Huawei Watch transfer has been proven.

## 2026-08-10 — Minimal Android BLE central reader

Changed:

- Added a central-side BLE reader that scans for the controlled workout service UUID, stops scanning on the first match, connects over LE GATT, discovers services, and reads the workout summary characteristic.
- Added legacy pre-Android-12 Bluetooth permissions alongside Android 12+ scan/connect permissions.
- Added legacy location permission checking to the Bluetooth preflight for Android versions before 12.

Still not claimed:

- No physical Bluetooth loopback has been executed on this host.

## 2026-08-10 — Controlled BLE loopback peripheral

Changed:

- Added a minimal GATT peripheral harness that advertises the controlled workout service UUID and serves one workout summary characteristic.
- Declared `BLUETOOTH_ADVERTISE` because the app can now act as the loopback peripheral on a second Android device.

Still not claimed:

- The loopback has not been executed on physical devices from this host.
- The harness is single-read and limited to 512-byte payloads; chunking waits until real payload size requires it.

## 2026-08-10 — Desktop validation wiring

Changed:

- Added `AndroidBluetoothSyncWiring` so the desktop build can assemble the Bluetooth source, preflight, Health Connect writer/inspector, and existing coordinator without UI changes.
- Added `docs/bluetooth-desktop-validation.md` with the exact desktop/device evidence required before marking loopback `PASS`.

Still blocked:

- Desktop Gradle/device execution is required for compile, lint, and physical loopback evidence.

## 2026-08-10 — Infrastructure loop graph

Changed:

- Added `docs/sync-infra-loop.md` to track the Bluetooth sync loop as a graph: implement, verify, record evidence, fix/block, and repeat.
- Marked the current active node as central reader complete in code, with peripheral loopback and physical evidence still open.
