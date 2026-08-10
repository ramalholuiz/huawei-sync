# Mobile engineering audit

Date: 2026-08-10

Scope: current Android repository, including the Bluetooth sync direction requested after the BitChat reference review. BitChat is a transport reference only: its useful ideas for this project are Bluetooth-first delivery, peer discovery, compact payloads, offline operation, and explicit privacy boundaries. This app must not become a chat app, a mesh network product, or a reverse-engineered Huawei client.

Status values:

- `Present`: implemented in the repo with tests or clear evidence.
- `Partial`: implemented only for the current gate, not production-complete.
- `Missing`: not implemented.
- `Blocked`: needs external account, device, official API, or store/release access.

## Executive verdict

The project already has a solid Android/Health Connect vertical foundation: Kotlin, Compose, Room ledger, Health Connect write/readback contracts, deterministic ids, safe diagnostics, migrations, and a useful unit-test suite. The previous UI work went beyond the core sync path, but it did not destroy the infrastructure.

The production gaps are not another screen. The gaps are: real Bluetooth transfer, real Huawei data source decision, GitHub workflow, CI, release discipline, device validation, and partner-app validation through Health Connect.

## Inventory

| Area | Status | Evidence |
| --- | --- | --- |
| Android project | Present | `settings.gradle.kts`, `app/build.gradle.kts`, Gradle wrapper, Kotlin/Compose app module |
| Source control hygiene | Partial | `.gitignore` excludes local config, keystores, secrets, build output; `.github` was missing before this audit |
| Health Connect write path | Present | `HealthConnectWorkoutWriter`, `HealthWorkoutMapper`, Gate 1 evidence |
| Health Connect readback/reconciliation | Present | `HealthConnectWorkoutInspector`, confirmation and reconciliation tests |
| Room ledger | Present | `AppDatabase`, `SyncLedgerStore`, unique source/provider index, migration test |
| Deterministic dedupe | Present | deterministic `clientRecordId`, content hash/version policy, coordinator tests |
| UI state handling | Present | loading, empty, retryable error, busy states in dashboard/history/diagnostics |
| Privacy-safe diagnostics | Present | sanitized diagnostics/export tests and safe error-code policy |
| Bluetooth permissions/preflight | Partial | permission declarations and preflight contract exist; no scan/connect service yet |
| Bluetooth payload ingestion | Partial | controlled parser/source seam exists; no real device transport yet |
| Huawei source integration | Blocked | official Huawei SDK/API authorization and real workout read are not implemented |
| Samsung Health / Apple Health / Strava / GymRats | Partial | Health Connect can bridge compatible Android apps; direct integrations are not implemented |
| Offline/background sync | Missing | no WorkManager/background sync; manual sync only |
| Observability | Partial | local diagnostics/export only; no crash reporting/analytics by design yet |
| Accessibility/responsive checks | Partial | regression tests exist; no device/screenshot matrix in CI |
| Test coverage | Partial | 41 unit-test files; no `androidTest`, no coverage threshold |
| CI | Missing before this audit | no `.github/workflows` existed |
| Release/app store readiness | Missing | no release keystore, signing workflow, privacy/release checklist |

## Priority plan

### P0 - Make the repo hard to break

1. Add CI that runs the same Gradle verification path used locally.
2. Add issue and PR templates so every change carries scope, tests, risk, and evidence.
3. Keep `scripts/verify.sh` as the canonical local gate.
4. Do not mark local verification complete on this macOS host until Java is installed.

### P1 - Finish sync infrastructure before more UI

1. Build a minimal Bluetooth scan/connect/read path behind `WorkoutSourceReader`.
2. Start with controlled loopback payloads, then move to a real source only after the protocol/source is legal and documented.
3. Feed Bluetooth workouts through the existing Room ledger and Health Connect writer.
4. Prove three transfers do not duplicate Room or Health Connect records.

### P2 - Validate real ecosystem sync

1. Gate 2: confirm GymRats imports the synthetic Health Connect exercise session.
2. Gate 3: use official Huawei SDK/API or another documented legal source path to read one real workout.
3. Gate 4: write one real Huawei workout to Health Connect and validate GymRats import.
4. Treat Samsung Health, Strava, and similar apps as Health Connect consumers/providers first; add direct APIs only when Health Connect cannot cover the use case.

### P3 - Production readiness

1. Add physical-device validation matrix.
2. Add release signing and privacy policy workflow.
3. Decide whether crash reporting is acceptable for a personal sideloaded health app.
4. Add background sync only after manual real-data sync is reliable.

## Current gaps by topic

| Topic | Audit result | Next action |
| --- | --- | --- |
| Loading/error/empty states | Present | Keep tests when changing state mappers |
| Motion/performance | Partial | Avoid new animation until sync is proven |
| Offline | Partial | Room gives local durability; no queued Bluetooth/background sync yet |
| Lifecycle | Partial | ViewModel uses flows and exclusive actions; no long-running worker yet |
| Permissions | Partial | Health Connect and Bluetooth declared; Bluetooth runtime flow not built |
| Persistence | Present | Room ledger and migration tests exist |
| APIs | Partial | Health Connect present; Huawei official API still blocked |
| Observability | Partial | Safe diagnostics only |
| Privacy/security | Partial | no secrets found in tracked config; health payload export is sanitized |
| Deep links | Missing | not needed until a real share/open flow exists |
| Accessibility | Partial | regression tests exist; no instrumented device matrix |
| Responsiveness | Partial | UI tests exist; no screenshot CI |
| Lint/tests/coverage | Partial | Gradle lint/tests configured; no coverage gate |
| Dependencies | Present | version catalog, no new dependency needed for this audit |
| Env/build-release | Partial | debug verification exists; release signing missing |
| Rollout/app store | Missing | not needed before real-data vertical slice |
| Architecture | Partial | gate-driven architecture is good; source layer needs real Bluetooth implementation |

## Decisions

- Health Connect remains the default bridge to GymRats, Samsung Health, Strava-compatible Android flows, and other health apps.
- Direct partner integrations stay out until a Health Connect limitation is proven.
- No reverse engineering of Huawei Health private storage or undocumented watch protocol.
- Bluetooth work proceeds as a controlled source-ingestion layer, not a BitChat fork.

