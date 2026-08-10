# GitHub issues backlog

GitHub issues were not created from this environment. Use these as exact issue drafts.

## P0 - Add CI verification

Labels: `p0`, `infra`, `ci`

Implement GitHub Actions for Android verification using JDK 17 and the existing Gradle flow.

Acceptance:

- CI runs on pull request and push to `main`.
- CI executes `./gradlew clean test lint assembleDebug`.
- The workflow does not require signing secrets.

## P0 - Keep Bluetooth source behind sync pipeline

Labels: `p0`, `bluetooth`, `sync`

Finish the Bluetooth source reader path without changing UI scope.

Acceptance:

- Controlled Bluetooth payload reaches `WorkoutSourceReader`.
- The payload maps to `DomainWorkout`.
- Three identical transfers produce one ledger row and no duplicate Health Connect record.
- Failure states use safe error codes.

## P1 - Implement minimal BLE scan/connect loopback

Labels: `p1`, `bluetooth`

Implement the smallest real Android BLE loopback needed to transfer one bounded workout payload.

Acceptance:

- Runtime Bluetooth permission flow exists.
- Adapter disabled, permission denied, and no paired/available source are testable blockers.
- Transfer result is sanitized and does not expose raw health payload by default.

## P1 - Validate GymRats import through Health Connect

Labels: `p1`, `validation`, `health-connect`

Run Gate 2 manually on the owner's device.

Acceptance:

- GymRats version is recorded.
- Health Connect permission state for GymRats is recorded.
- Synthetic workout is visible in Health Connect and GymRats.
- Evidence is added to `docs/gates.md`.

## P1 - Decide legal Huawei source path

Labels: `p1`, `huawei`, `research`

Decide whether the real source is official Huawei API/SDK, documented Bluetooth protocol, or controlled watch-side software.

Acceptance:

- Official docs or blocker evidence are linked in `docs/research.md`.
- No private-storage or reverse-engineered path is selected.
- Gate 3 checklist is updated.

## P2 - Add release checklist

Labels: `p2`, `release`

Document release signing, privacy review, artifact hash, and sideload/store rollout steps.

Acceptance:

- Keystore location policy is documented without committing secrets.
- Release command and artifact hash procedure are documented.
- Privacy review checklist exists before any public distribution.

