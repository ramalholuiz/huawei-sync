# Cycle 1 — Implementation Report

**Cycle scope:** Dashboard, Sync-now modal, Pipeline.
**Chosen direction:** B — Premium utility (see `cycle-01-directions.md`).
**Base commit:** `719f70d` (last M006 verification).
**Branch:** `milestone/M007`.
**Worktree:** `.gsd-worktrees/M007`.

## 1. What shipped

Four consecutive commits on `milestone/M007`, plus this evidence commit:

| Commit | Subject | Type |
|---|---|---|
| `a962985` | `docs(m007): plan visual and motion lab` | Milestone plan + doc scaffolding + asset manifest skeleton. |
| `cdfa76d` | `docs(m007-c01): visual research and chosen direction` | Research + directions + motion spec + asset manifest (cycle-scoped). |
| `dc20260` | `feat(m007-c01): install pipeline rail on dashboard and sync-now` | First Compose pass: `SyncPipelineRail`, model projection, dashboard hero rail, sync-now rail + phase medallion, pipeline shape encoding + phase counter. |
| `36ec687` | `feat(m007-c01): polish rail label row and modal density` | Post-QA refinement: weighted rail label row (fixes label truncation), shorter GymRats supporting text, hides rail supporting text inside the narrower modal. |
| _(this doc)_ | `docs(m007-c01): capture cycle-01 evidence` | Screenshots, motion frames, APK SHA-256, verification transcript. |

## 2. Files touched

Presentation only. Runtime packages (`data/`, `domain/`, `health/`, `diagnostics/`, `presentation/`) show **zero** changes vs. `719f70d`.

- `app/src/main/java/dev/lui/huaweisync/ui/components/SyncPipelineRail.kt` (new) — horizontal + vertical rail with per-node state, per-segment ticker, confirmed one-shot with halo, and an explicit reduced-motion branch on every entry point.
- `app/src/main/java/dev/lui/huaweisync/ui/screens/pipeline/PipelinePresentation.kt` — projects `SyncPipelineRailModel` from `ProductSyncState` + `coordinatorBusy`, exposes `phaseIndex` / `phaseTotal` for the phase counter. `numericProgress == null` invariant preserved.
- `app/src/main/java/dev/lui/huaweisync/ui/screens/dashboard/DashboardScreen.kt` — inserts the horizontal rail inside `SyncHero`. Progress label swapped from "80% · PHASE: ..." to "80% · PHASE 4 OF 5 — VERIFICATION" (fraction is still deterministic; wording no longer asserts a measured percent).
- `app/src/main/java/dev/lui/huaweisync/ui/screens/sync/SyncNowModal.kt` — replaces the rotating-square `PhaseSignal` and the three-endpoint strip with `SyncPipelineRail` (compact, no supporting text) plus a 72dp phase medallion showing the current step letterform (P/W/A/V/R). Original TalkBack strings preserved.
- `app/src/main/java/dev/lui/huaweisync/ui/screens/pipeline/PipelineScreen.kt` — per-step glyph gains a `Canvas` shape backdrop (filled / faint / half / X). `PhaseCard` microcopy is now `COORDINATOR PHASE i OF 5` (falls back to `COORDINATOR PHASE EVIDENCE` while awaiting evidence). The rail itself is not repeated on this screen — see `cycle-01-motion-spec.md § 6` for why.

## 3. Motion catalog

Implemented, one-to-one with the motion spec:

| Product state | Coordinator source | Motion | Reduced-motion fallback |
|---|---|---|---|
| **waiting** | `IDLE` + `READY_TO_SYNC` | static | identical |
| **writing** | `WRITE`, `ACCEPTANCE`, `PREFLIGHT`, `WRITE_IN_PROGRESS` | forward `Canvas` ticker on the segment upstream of the HC node (or Sync node during preflight) | solid 2dp bar in accent-foreground |
| **verifying** | `VERIFICATION`, `ACCEPTED_AWAITING_READBACK` | reverse-direction ticker at 55% alpha on the same segment | solid 2dp bar in `info` at 55% alpha |
| **confirmed** | `CONFIRMED_IN_HEALTH_CONNECT` | one-shot `Animatable` on the HC node: scale 1.00→1.08→1.00 + halo outline stroke 0→80%→0, total 480 ms | node border simply switches to `ok`, no scale, no halo |
| **attention / error** | `RECONCILIATION_REQUIRED`, `RETRY_REQUIRED`, `ACTION_REQUIRED`, `PERMISSION_REQUIRED`, `UPDATE_REQUIRED`, `UNAVAILABLE`, `FAILED` | static; node ring `accent` or `warning` | identical |

Sync-now medallion letter transitions use `AnimatedContent(standardMillis in / fastMillis out)`; reduced motion collapses to `tween(0)`. No infinite loop runs while the coordinator is idle. Confirmed acknowledgement fires exactly once per state entry (keyed on `node.state` / `node.confirmedAcknowledgement` / `node.id` / `policy.reducedMotion` inside a `LaunchedEffect`).

## 4. Preserved contracts (verified)

- Runtime packages `data/`, `domain/`, `health/`, `diagnostics/`, `presentation/` — 0 changes vs base commit.
- `PipelinePresentation.numericProgress == null` invariant — enforced by the same `require` block; no percentage is claimed anywhere.
- GymRats node — never enters `CONFIRMED`. Its state maps to `PENDING` unconditionally in the rail projection until Gate 2.
- `HuaweiSyncMotionPolicy.Reduced` branch — respected by every new motion path (`RailNode` LaunchedEffect, `RailSegment` static fallback, medallion `AnimatedContent`).
- Test tags — `dashboard-sync-fab`, `dashboard-sync-hero`, `dashboard-failure-summary`, `sync-now-overlay`, `sync-modal-status`, `sync-modal-explanation`, `sync-modal-phase-checks`, `sync-check-*`, `pipeline-screen`, `pipeline-phases`, `pipeline-status`, `pipeline-awaiting-evidence`, `pipeline-step-*`, `pipeline-failure-summary` — all preserved.
- A11y descriptions — sync-now medallion still emits `Readback confirmed` / `Coordinator phase active` / `Coordinator phase active, static reduced-motion indicator` / `No active coordinator phase observed`, verified by `PipelineScreenTest > reduced motion modal stays static while awaiting real phase evidence`.

## 5. Verification transcript

Run from the worktree root with `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` and `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`.

```
./gradlew testDebugUnitTest      # BUILD SUCCESSFUL — 204 tests, 0 failed
./gradlew testReleaseUnitTest    # BUILD SUCCESSFUL
scripts/verify.sh                # BUILD SUCCESSFUL (clean test lint assembleDebug)
git diff --check                 # exit 0
git diff --stat 719f70d..HEAD -- app/src/main/java/dev/lui/huaweisync/{data,domain,health,diagnostics,presentation}
                                 # (empty output — runtime packages untouched)
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
Size: 18,499,552 bytes
SHA-256: `0222b9ccf26251aefb44f6d9a2249494faa08bad0ff5b1a83157c27f9c80cd0e`

## 6. Emulator captures

All captures taken on `HuaweiSync_API_35` (Android 15 / API 35, 1080×2400 @ 420dpi).

Directory: `docs/milestones/M007/screenshots/cycle-01/`

- `before-dashboard-dark.png` — dashboard in dark, first Compose pass (labels truncated to `HUAWE / HEALT / GYMRA`).
- `before-sync-modal-dark.png` — sync-now modal, first pass (supporting text truncated to `COORDINATO / CONFIRME / AWAITING`).
- `dashboard-dark.png` — dashboard, dark, after polish. Labels full, halo one-shot around HC node visible, phase counter reads `80% · PHASE 4 OF 5 — VERIFICATION`.
- `dashboard-light.png` — dashboard, light, after polish. Same rail treatment against the light palette.
- `sync-modal-dark.png` — sync-now modal, dark, after polish. Rail with clean labels, phase medallion `V` in green, phase checks bar green (four of five complete).
- `pipeline-dark.png` — pipeline screen, dark. Per-step shape encoding visible: filled tinted squares for `COMPLETE`, faint hollow for `PENDING`, phase counter `COORDINATOR PHASE 4 OF 5`.
- `pipeline-light.png` — same, light theme.

## 7. Non-goals reaffirmed

- No new record types.
- No new integrations (Huawei official SDK is still pending Gate 3).
- No new dependencies (all animation via native Compose APIs).
- No GymRats delivery claim.
- No changes to the FAB semantics, bottom nav, or destinations.
- No Rive / Lottie.

## 8. Followups (Cycle 2 candidates)

Not landed in Cycle 1, tracked here so they are not forgotten:

- Dashboard FAB icon: `SyncFabState` selection when `state = CONFIRMED_IN_HEALTH_CONNECT + syncInProgress=false` currently paints the accent square with an off-center `Check` — worth a visual pass in Cycle 2.
- Pipeline screen: candidate for a **vertical** `SyncPipelineRail` alongside the per-step list once the phase-card layout is reflowed to keep the HC endpoint above the fold.
- Reduced-motion capture: add an in-app toggle (or dev-menu affordance) so QA can capture the reduced-motion path visually. Today the reduced-motion branch is only exercised in Robolectric tests.
- Explore Direction C ideas (canvas-drawn traveling packet during writing) once the rail is validated live.
