# Cycle 2 — Implementation Report

**Cycle scope:** History screen, Activity Detail screen, and the list-to-detail transition.
**Chosen direction:** A — Timeline minimalista (see `cycle-02-directions.md`).
**Base commit for Cycle 2:** `da59c0f` (last Cycle 1 evidence commit on `milestone/M007`).
**Branch:** `milestone/M007`.
**Worktree:** `.gsd-worktrees/M007`.

## 1. What shipped

Cycle 2 commits, on top of `da59c0f`:

| Commit | Subject | Type |
|---|---|---|
| `0e47465` | `docs(m007-c02): research direction and motion spec for history and detail` | Research + directions + motion spec. |
| `59af732` | `feat(m007-c02): rework history and activity detail around Direction A` | First Compose pass: History day grouping + attention filter + row rewrite; Detail identity band + vertical rail + lifecycle timeline + technical toggle. Motion spec updated to describe the AnimatedVisibility identity arrival that shipped. |
| `b172d36` | `feat(m007-c02): polish history and detail after live critique` | Post-critique polish: 6dp row rule, filter-chip border, dropped duplicate top-bar microcopy, timeline neutral tone + connector coloring, section-label breathing space. |
| _(this doc)_ | `docs(m007-c02): capture cycle-02 evidence` | Includes an API-level portability fix (`Instant.atZone(zone).toLocal*()` instead of the API-31 `LocalTime/LocalDate.ofInstant`) uncovered by `lintDebug`. Screenshots, APK SHA-256, verification transcript. |

## 2. Files touched

Presentation only. Runtime packages (`data/`, `domain/`, `health/`, `diagnostics/`, `presentation/`) show **zero** changes vs. `719f70d` (Cycle 1 base) — confirmed by `git diff --stat 719f70d..HEAD` scoped to those directories.

- `app/src/main/java/dev/lui/huaweisync/ui/screens/history/HistoryScreen.kt` — full rewrite. New parts: `groupByDay` bucketing on `updatedAtEpochMillis` in `ZoneId.systemDefault()` with `TODAY`, `YESTERDAY`, `<EEE dd MMM>`, ISO fallback labels; a sticky counter row with `N DURABLE RECORD(S) [· FILTERED TO ATTENTION]`; a single `NEEDS ATTENTION ×N` filter chip that disables to `NO ATTENTION NEEDED` when nothing requires attention; a redesigned row (leading 6dp state color rule, `sourceProvider` microcopy, bold readback state text, right-aligned `HH:mm` local time, inline `×N` attempts chip when `attemptCount > 1`); progressive identity — the client-record identity string is removed from the row and lives only on the Detail's technical toggle.
- `app/src/main/java/dev/lui/huaweisync/ui/screens/detail/ActivityDetailScreen.kt` — full rewrite. New parts: a top bar with just the back arrow (no repeated microcopy); an `IdentityBand` with the state color rule, source-provider eyebrow, `Activity record` anchor, and a compact `ReadbackPill`; a vertical `SyncPipelineRail` (`HUAWEI → SYNC → HC → GYMRATS`) projected from *this workout's* ledger facts, entirely static (no ticker, no confirmed one-shot, no active flow); an `ActivityLifecycleTimeline` composable with staggered entrance via `AnimatedVisibility` and per-entry timeline connectors keyed off the entry tone; a "Show technical detail" toggle backed by `animateContentSize` that reveals `clientRecordId / clientRecordVersion / attemptCount / sourceProvider / safeErrorCode`.
- `docs/design/m007/cycle-02-{research,directions,motion-spec,implementation-report}.md` — cycle-scoped docs, plus the motion spec addendum explaining why `SharedTransitionLayout` was deferred and `AnimatedVisibility` shipped instead.
- `docs/milestones/M007/screenshots/cycle-02/` — captures listed in § 6.

Removed pre-existing helpers `LedgerStateLabel`, `stateColor`, and the `readbackLabel` moved from a private helper to an internal top-level function in `HistoryScreen.kt` so `ActivityDetailScreen.kt` can reuse it without a package split.

## 3. Motion catalog

Three motion families, all with explicit `HuaweiSyncMotion.current.reducedMotion` branches. Cycle 1's rail motion vocabulary is untouched.

| Family | Where | Standard motion | Reduced-motion fallback |
|---|---|---|---|
| **Identity arrival** | Detail's `IdentityBand`, on first composition of a non-null activity | `fadeIn(tween(standardMillis)) + slideInVertically(tween(standardMillis)) { height / 4 }`, initial state `false` → `true` on `LaunchedEffect` | `AnimatedVisibility` initial state `true`; static |
| **Timeline entrance** | Detail's `ActivityLifecycleTimeline`, per-entry | Same enter transition per entry, with `LaunchedEffect` `kotlinx.coroutines.delay(index * fastMillis / 2)` stagger | `visibleState.targetState = true` immediately; no delay |
| **Progressive reveal** | Detail's "Show technical detail" toggle | `Modifier.animateContentSize(tween(standardMillis))` on the container; toggle label `AnimatedContent(standardMillis in / fastMillis out)` | `animateContentSize` skipped; direct swap |

The rail on Detail is entirely static — `activeFlow = NONE`, `activeNodeIndex = null`, `confirmedAcknowledgement = false` on every node — because the workout is not live-syncing while the user reads Detail. Motion never runs on top of a past fact, and the rail never re-fires the confirmation halo shipped in Cycle 1.

`SharedTransitionLayout` was evaluated and deferred (see `cycle-02-motion-spec.md § 2`). The row's state color rule and the Detail identity band's rule paint the same color in the same shape, so identity is preserved visually across the boundary without the shared-element wiring.

## 4. Preserved contracts (verified)

- Runtime packages `data/`, `domain/`, `health/`, `diagnostics/`, `presentation/` — 0 changes vs `719f70d`.
- `ProductGymRatsStatus.READY_TO_READ.label` — Detail's rail keeps the GymRats node in `PENDING` regardless of readback state; the last timeline entry reads "Available for GymRats to import" and never claims delivery.
- `HuaweiSyncMotionPolicy.Reduced` branch — every new motion path (`AnimatedVisibility`, `LaunchedEffect` delay, `animateContentSize`) reads `HuaweiSyncMotion.current.reducedMotion` and returns a static path.
- Test tags preserved: `history-loading`, `history-empty`, `history-error`, `history-content`, `history-item-<clientRecordId>`, `activity-detail-back`, `activity-detail-<clientRecordId>`, `activity-detail-missing`.
- New test tags added: `history-attention-filter`, `history-day-header-<label>`, `history-filter-empty`, `activity-detail-identity`, `activity-detail-technical`, `activity-detail-technical-body`, `activity-lifecycle-<key>`.
- Honesty guardrails: `HonestyCopyGuardTest` still passes (no forbidden delivery strings introduced). `EyebrowBudgetTest` still passes (History uses 2 eyebrows, Detail uses 1 — both at budget).
- A11y: TalkBack reading order is preserved; the identity band has `stateDescription` on the pill; the row's state color rule carries a `contentDescription` "<label> state marker"; the attention filter chip is announced with role, selection state, and `NEEDS ATTENTION ×N` / `NO ATTENTION NEEDED` labels; timeline connectors are decorative (no separate content description).

## 5. Verification transcript

Run from the worktree root with `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` and `JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`.

```
./gradlew :app:testDebugUnitTest      # BUILD SUCCESSFUL
./gradlew :app:testReleaseUnitTest    # BUILD SUCCESSFUL
./gradlew clean test lint assembleDebug
                                      # BUILD SUCCESSFUL — the first attempt failed lintDebug with
                                      # two NewApi errors on LocalTime.ofInstant / LocalDate.ofInstant
                                      # (API 31, minSdk 26). Fixed inline by switching to
                                      # Instant.atZone(zone).toLocalTime()/toLocalDate(), then the
                                      # full pipeline was green.
scripts/verify.sh                     # BUILD SUCCESSFUL (clean test lint assembleDebug)
git diff --check                      # exit 0
git diff --stat 719f70d..HEAD -- app/src/main/java/dev/lui/huaweisync/{data,domain,health,diagnostics,presentation}
                                      # empty output — runtime packages untouched
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
Size: 18,532,320 bytes
SHA-256: `083dce28982d5a7bf42473437bebf035c3383f594f432e06c9f3c9feef393b98`

## 6. Emulator captures

All captures on `HuaweiSync_API_35` (Android 15 / API 35, 1080×2400 @ 420dpi).

Directory: `docs/milestones/M007/screenshots/cycle-02/`

| File | Purpose |
|---|---|
| `history-content-light.png` | History with 1 VERIFIED row, light theme, post-polish. |
| `history-content-dark.png` | Same, dark theme, post-polish. |
| `detail-verified-light.png` | Detail (VERIFIED workout), light theme, above the fold — identity band + vertical rail visible. |
| `detail-verified-dark.png` | Same, dark theme. |
| `detail-verified-scrolled-dark.png` | Detail scrolled to reveal the full lifecycle timeline and the honest disclaimer, dark theme. |
| `detail-technical-open-light.png` | Detail with the technical-detail toggle expanded, light theme. |
| `detail-technical-open-dark.png` | Same, dark theme. |
| `detail-technical-body-light.png` | Detail scrolled further to show the revealed technical body (`Client record ID`, `Record version`, `Attempt count`), light theme. |
| `cycle-01-dashboard-dark-regression.png` | Dashboard, dark theme — proves Cycle 1's rail, phase counter, ledger summary, and Connected services are unchanged. |
| `final-history-dark.png` | Final rest state — emulator left on History in dark theme. |

Reduced-motion path is exercised by `ReducedMotionDetailPreview` in `ActivityDetailScreen.kt`; on-device capture of the reduced-motion path is left for the tool tracked in Cycle 1 followup #3.

## 7. Non-goals reaffirmed

- No new record types.
- No new integrations.
- No new dependencies.
- No claim of GymRats delivery.
- No changes to Room, ledger, `Gate1SyncCoordinator`, deterministic identity, attempt count, deduplication, reconciliation, or claims policy.
- No changes to Dashboard, Sync-now, Pipeline, Integrations, Diagnostics, Automation, or Assistant.

## 8. Followups (Cycle 3+ candidates)

- Reduced-motion in-app toggle for on-device capture (already tracked from Cycle 1).
- Shared-element list-to-detail transition (`SharedTransitionLayout`) — evaluated and deferred in Cycle 2; the identity band's readback pill is the intended shared element.
- Multi-day History captures — the current emulator ledger has one row, so day-grouping is proven by unit-testable `groupByDay` and the `HistoryContentPreview` matrix rather than a live capture with 10+ workouts. When Gate 3 lands and real Huawei workouts flow, capture a real multi-day History.
- Attention-state Detail captures — the current ledger has only a `VERIFIED` row, so Detail's attention-branch (`ReconciliationDetailPreview`, `PendingDetailPreview`) captures use the preview matrix.
