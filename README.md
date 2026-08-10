# huawei-sync

Personal sideloaded Android app to sync workouts from Huawei Health into Android Health Connect so apps such as GymRats can consume them.

Primary flow:

```text
Huawei Watch Fit 5 Pro → Huawei Health → huawei-sync → Health Connect → GymRats
```

Optional later fallback flow, not before Gate 1 and Gate 2 pass:

```text
Huawei Health → Strava → huawei-sync → Health Connect
```

The project is gate-driven. Do not build the full product until Gate 1 Health Connect synthetic write, Gate 2 manual GymRats import validation, Gate 3 Huawei real workout read, and Gate 4 Huawei → Health Connect → GymRats vertical slice are proven with reproducible evidence. GymRats is validated manually through Health Connect only; this app must not implement any direct GymRats integration.

## Documentation

- `AGENTS.md` — permanent project rules.
- `docs/research.md` — official API references, limitations, and open questions.
- `docs/plan.md` — implementation phases, risks, and decisions.
- `docs/gates.md` — gate checklists and PASS/BLOCKED evidence.
- `docs/progress.md` — progress log and next steps.
- `docs/data-mapping.md` — Huawei/Strava → domain → Health Connect mapping.
- `docs/bluetooth-sync-infra.md` — Bluetooth source-ingestion checkpoint and boundary.
- `docs/bluetooth-desktop-validation.md` — desktop/device validation runbook for controlled Bluetooth loopback.
- `docs/sync-infra-loop.md` — current infrastructure loop and graph.
- `docs/mobile-engineering-audit.md` — current engineering audit and priority plan.
- `docs/engineering-standards.md` — definition of done, sync, Bluetooth, test, and release rules.
- `docs/github-issues-backlog.md` — exact issue drafts for missing GitHub work.
- `scripts/verify.sh` — verification entry point.

## Verified baseline

Commit `15e3fbd` and the supplied Windows evidence establish this `VERIFIED` build/install baseline:

- checksum-verified Gradle Wrapper 8.11.1 with AGP 8.10.1;
- `compileSdk 36`, `targetSdk 35`, and AndroidX Health Connect 1.1.0;
- a usable Java environment;
- passing `./gradlew clean test lint assembleDebug`, canonical `scripts/verify.sh`, and debug/release unit tests;
- a generated debug APK installed successfully on `emulator-5554`;
- `MainActivity` launched without an immediate crash and reported Health Connect `Available`.

Later Gate 1 emulator evidence promoted Gate 1 to `PASS`; see `docs/gates.md` for the authoritative current status.

## Verification

`scripts/verify.sh` is the canonical build-verification entry point:

```bash
scripts/verify.sh
```

It runs the required clean Gradle verification (`clean test lint assembleDebug`). The final definition of done still requires the canonical script and its underlying Gradle checks to pass in the target verification environment.
