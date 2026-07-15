# Local development tooling

## Android build baseline

Commit `15e3fbd` and the supplied Windows run establish the following `VERIFIED` toolchain:

- checksum-verified Gradle Wrapper 8.11.1;
- Android Gradle Plugin 8.10.1;
- `compileSdk 36` and `targetSdk 35`;
- AndroidX Health Connect 1.1.0;
- a usable Windows Java environment.

The supplied Windows baseline passed `clean test lint assembleDebug`, debug/release unit tests, and generated the debug APK. The APK installed successfully on `emulator-5554`; `MainActivity` launched without an immediate crash and Health Connect reported `Available`.

This toolchain and install baseline is `VERIFIED`, while Gate 1 remains `BLOCKED` on real Health Connect permission, write/readback, three-run idempotency, and reinstall evidence. Use `scripts/verify.sh` on POSIX hosts and `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify.ps1` on Windows; both resolve the repository root and run `clean test lint assembleDebug`.

## Ponytail

Ponytail is already installed in Codex, and its hooks have been reviewed and approved. Use it to review changes for simplicity. It must not remove security requirements, Room persistence, idempotency, or tests.

## Graphify

- Version: 0.9.15
- Project skill: `.agents/skills/graphify`
- Purpose: codebase navigation and analysis
- Gate: run it only after Gate 1 passes
- Future command: `/graphify .`

The graph has not been generated yet.

## GSD Pi

- Version: 1.11.0
- Purpose: planning and local project memory

Do not run GSD auto while Codex has an active `/goal`. Do not allow GSD and Codex to edit the same branch or worktree at the same time. Authentication and onboarding are manual owner actions.

## Instruction hierarchy

1. Explicit owner instruction and active `/goal`
2. `AGENTS.md`
3. `docs/gates.md` and `docs/plan.md`
4. Tests, `scripts/verify.sh`, and `scripts/verify.ps1`
5. Ponytail
6. Graphify
7. GSD, until promoted to the primary orchestrator
