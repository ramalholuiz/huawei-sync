# Cycle 3 — Implementation Report (Concept A only)

**Cycle scope:** three visual-language-break prototypes of the Dashboard reachable from a debug-only entry point, without touching production runtime, production navigation, or any other screen. This report covers **Concept A only**. Concepts B and C are placeholders (WIP), gated behind human validation of A.
**Chosen direction:** A — Soft Premium Health (see `cycle-03-directions.md § 2 Direction A`).
**Base commit for Cycle 3:** `e28cee0` (last Cycle 2 evidence commit on `milestone/M007`).
**Branch:** `milestone/M007`.
**Worktree:** `.gsd-worktrees/M007`.

## 1. What shipped

Cycle 3 delivers three artifacts on top of `e28cee0`:

| Artifact | Type |
|---|---|
| `docs/design/m007/cycle-03-{research,directions,asset-manifest,motion-spec}.md` | Design research + three-direction shape doc + explicit "no ship assets this cycle" manifest + per-concept motion spec. |
| `app/src/main/java/dev/lui/huaweisync/ui/prototypes/` + `conceptA/` | Debug-only `PrototypeGalleryDialog` reachable via long-press on the Dashboard `H` logo when `BuildConfig.DEBUG`. Contains Concept A dashboard, seven canonical fixtures, and A/B/C tab scaffolding (B & C render a "Coming next" placeholder). |
| `docs/design/m007/screenshots/cycle-03/concept-a/*.png` | 14 emulator captures — 7 canonical fixtures × 2 themes. |

Concepts B and C are **explicitly WIP placeholders** inside the gallery. They exist so the delivery shape stays "one slice per concept with pause for evaluation" (chosen at cycle start via structured intake) and so a follow-up commit for B or C does not need to re-wire the gallery.

## 2. Files touched

Presentation only. Runtime packages (`data/`, `domain/`, `health/`, `diagnostics/`, `presentation/`) show **zero** changes vs. `e28cee0` — confirmed by `git diff --stat e28cee0..HEAD` scoped to those directories (empty output).

Ten files changed / added:

| Path | Kind | Purpose |
|---|---|---|
| `app/build.gradle.kts` | edit (+1 line) | Enables `buildFeatures.buildConfig = true` so `BuildConfig.DEBUG` is available for the gallery gate. |
| `app/src/main/java/dev/lui/huaweisync/ui/screens/dashboard/DashboardScreen.kt` | edit (+43 / −5) | Optional `onLongPressLogo: (() -> Unit)?` parameter (default `null`, so production callers are unchanged). When `BuildConfig.DEBUG`, the top-bar `H` logo gets `combinedClickable(onLongClick = …)` that opens `PrototypeGalleryDialog`. On release builds the branch is dead-stripped. Test tag added: `dashboard-prototype-gallery-trigger`. |
| `app/src/main/java/dev/lui/huaweisync/ui/prototypes/PrototypeGallery.kt` | new (259 lines) | Fullscreen `Dialog` (`usePlatformDefaultWidth = false`, `dismissOnBackPress = true`) with three concept tabs, a chip-row state selector (7 fixtures), a Light/Dark toggle, and a Close chip. |
| `app/src/main/java/dev/lui/huaweisync/ui/prototypes/PrototypeGalleryFixtures.kt` | new (95 lines) | Seven `ProductSyncState` fixtures derived directly from production `ProductSyncState` / `ProductHealthConnectStatus` / `ProductSyncPhase` — no invented fields. |
| `app/src/main/java/dev/lui/huaweisync/ui/prototypes/conceptA/ConceptATokens.kt` | new (67 lines) | Local tokens: rounded shapes (16–28 dp), soft shadow, per-state accent wash brushes (accent, waiting, error), inner padding constants. **No reference to `HuaweiSyncShapes` or `HuaweiSyncGeometry`.** |
| `app/src/main/java/dev/lui/huaweisync/ui/prototypes/conceptA/ConceptADashboard.kt` | new (489 lines) | Concept A Dashboard: `Column` hero + section cards. Reuses `SyncPipelineRail` (a genuinely reusable, data-driven component) and `HuaweiSyncMotion.current` for motion policy. Does not import `ModernistSurface`, `StatusLabel`, `TechnicalMicrocopy`, or `SectionHeader`. |
| `docs/design/m007/cycle-03-research.md` | new | Reference sweep for warm/rounded/health-forward surfaces, dashboards, and audit timelines that inform the three directions. |
| `docs/design/m007/cycle-03-directions.md` | new | Three-direction critique + shape doc: A Soft Premium Health, B Dynamic Fitness Utility, C Connected Ecosystem — with per-concept local tokens, motion signature, and honesty guardrails. |
| `docs/design/m007/cycle-03-asset-manifest.md` | new | Declares no third-party assets ship in Cycle 3. Placeholder monograms only. |
| `docs/design/m007/cycle-03-motion-spec.md` | new | Per-concept motion families with explicit `HuaweiSyncMotion.current.reducedMotion` fallbacks. |
| `docs/design/m007/screenshots/cycle-03/concept-a/*.png` | new | 14 emulator captures — § 6. |

## 3. Concept A — how it reads

Concept A is a Dashboard replacement rendered inside its own composable tree, driven by the same seven `ProductSyncState` fixtures the real Dashboard reads.

**Shape.**

- Rounded surfaces: `24 dp` hero, `20 dp` cards, `16 dp` inset chips. No square borders.
- One soft shadow layer per hero (`elevation = 1.dp`, tinted ambient + spot). Cards are surface-tonal, no borders.
- Hero has a per-state vertical wash: green over surface-1 for confirmed, blue over surface-1 for waiting/syncing, amber over surface-1 for error, none for ready.

**Copy.**

- Hero title changes per state — "Ready to sync your first workout" / "Bring your watch nearby" / "Syncing your workout" / "Workout confirmed in Health Connect" / "Sync couldn't complete". None of these introduce a forbidden claim; `HonestyCopyGuardTest` remains green.
- `TechnicalMicrocopy` is not used anywhere in Concept A. Eyebrows exist but are `titleSmall`-cased (e.g. "Today", "Your workouts", "Your history", "Sync path").
- GymRats status remains `ProductGymRatsStatus.READY_TO_READ.label` — "Available for GymRats to import". No delivery claim.

**Motion.**

- Hero content swap on state change: `AnimatedContent` `fadeIn(standardMillis) + fadeOut(fastMillis)`, with a `reducedMotion` branch collapsing to `tween(0)`.
- History card count uses `animateContentSize(standardMillis)` when the count transitions, `reducedMotion` disables it.
- The four-endpoint `SyncPipelineRail` retains Cycle 1's motion vocabulary — Concept A reuses the component untouched.

**What Concept A does *not* do.**

- No Rive / Lottie / vector illustrations.
- No third-party monograms — `H`, `S`, `HC`, `G`, `GR` are the same placeholder monograms Cycle 1 shipped.
- No changes to `HuaweiSyncTheme.colors` or `HuaweiSyncTypography` — Concept A only re-maps existing tokens through local shape / padding / motion tokens.

## 4. Access

The gallery is unreachable in a release APK: the branch inside `DashboardScreen.kt` is gated on `BuildConfig.DEBUG` and R8 strips it. In a debug build, it is reached by:

1. Launch the app to Dashboard.
2. Long-press the `H` logo in the top-left of the Dashboard top-bar (~500 ms).
3. Fullscreen dialog opens with three tabs (A / B / C), a state chip row, and a Light/Dark toggle.
4. Back button or Close chip dismisses the dialog without affecting production nav state.

The gallery does **not** register a new destination in `HuaweiSyncDestination.all` (which `HuaweiSyncNavigationTest` locks at exactly 10 entries) and does **not** appear in `PrimaryDestinations` or the More menu.

## 5. Preserved contracts (verified)

- **Runtime packages** `data/`, `domain/`, `health/`, `diagnostics/`, `presentation/` — 0 changes vs `e28cee0`. Verified: `git diff --stat e28cee0..HEAD` scoped to those directories returns empty output.
- **`HuaweiSyncDestination.all.size == 10`** — Concept A adds zero destinations; gallery lives outside the nav graph as a `Dialog`. `HuaweiSyncNavigationTest` still passes.
- **`HonestyCopyGuardTest`** — every string in Concept A's dashboard resolves to a real fact; no forbidden delivery claims were introduced.
- **`EyebrowBudgetTest`** — file-scoped test, unchanged files (`DashboardScreen.kt` unchanged eyebrows; new `conceptA/` files not in the test scope).
- **`AccessibilityResponsiveRegressionTest`** — unchanged existing screens, so no regression surface.
- **`HuaweiSyncMotionPolicy.Reduced`** — every new motion path (`AnimatedContent`, `animateContentSize`) reads `HuaweiSyncMotion.current` and returns a static `tween(0)` branch when reducedMotion is true.
- **`ProductGymRatsStatus.READY_TO_READ.label`** — Concept A's Sync path rail keeps GymRats node in `PENDING` regardless of readback state; the GymRats card reads exactly `Available for GymRats to import`.
- **Zero new dependencies** — no Rive, no Lottie, no font libraries, no image assets. Confirmed by `git diff app/build.gradle.kts gradle/libs.versions.toml`.

## 6. Verification transcript

Run from the worktree root with `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools` and `JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home`.

```
./gradlew clean test lint assembleDebug   # BUILD SUCCESSFUL in 3m 55s — 94 tasks executed
scripts/verify.sh                          # BUILD SUCCESSFUL in 2m 33s
git diff --stat e28cee0..HEAD -- app/src/main/java/dev/lui/huaweisync/{data,domain,health,diagnostics,presentation}
                                           # empty output — runtime packages untouched
```

The single pre-existing `SyncLedgerStoreTest.kt:435` warning is not introduced by Cycle 3 and is present on `e28cee0`.

APK: `app/build/outputs/apk/debug/app-debug.apk`
Size: 18,581,472 bytes
SHA-256: `6a41fe191d6747b12fea7f5d6cf0ea5727bcf32a95837f18836cd5d96b0eff74`

## 7. Emulator captures

All captures on `HuaweiSync_API_35` (Android 15 / API 35, 1080 × 2400 @ 420 dpi). Captured via `adb exec-out screencap -p` after driving the gallery with `adb shell input tap` — see § 4 for the entry gesture.

Directory: `docs/design/m007/screenshots/cycle-03/concept-a/`

| Fixture | Dark | Light |
|---|---|---|
| `EMPTY` (`READY_TO_SYNC`, `IDLE`, 0 workouts) | `concept-a-empty-dark.png` | `concept-a-empty-light.png` |
| `WAITING` (`READY_TO_SYNC`, `PREFLIGHT`) | `concept-a-waiting-dark.png` | `concept-a-waiting-light.png` |
| `SYNCING` (`WRITE_IN_PROGRESS`, `WRITE`, 1 attempt) | `concept-a-syncing-dark.png` | `concept-a-syncing-light.png` |
| `SUCCESS_CONFIRMED` (`CONFIRMED_IN_HEALTH_CONNECT`, 1 workout, verified) | `concept-a-success-dark.png` | `concept-a-success-light.png` |
| `ERROR` (`FAILED`, sanitized failure summary) | `concept-a-error-dark.png` | `concept-a-error-light.png` |
| `GYMRATS_AVAILABLE` (same shape as `SUCCESS_CONFIRMED` — see note) | `concept-a-gymrats-dark.png` | `concept-a-gymrats-light.png` |
| `COMPOSED_REPRESENTATIVE` (`WRITE_IN_PROGRESS`, `ACCEPTANCE`, 3 workouts, 2 attempts) | `concept-a-composed-dark.png` | `concept-a-composed-light.png` |

**Note on GYMRATS_AVAILABLE fixture:** by construction it is `ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT` + `ProductGymRatsStatus.READY_TO_READ`, which is currently indistinguishable from the `SUCCESS_CONFIRMED` fixture. This is not a bug — under Gate 1, "GymRats available" is a *view* on the same confirmed state, not a new state. The two screenshots verify that both fixtures render honestly; a later cycle (post Gate 2) can split them if delivery becomes a first-class fact.

Animation-frame sequences are deliberately not captured this cycle — Concept A's motion is intentionally quiet (`fadeIn(240ms) + fadeOut(160ms)` on state swap; `animateContentSize` on the count). The intake scope confirmed "42 shots (7 × 3 × 2)"; per-concept frame sequences ship only after direction selection to keep repository binary weight down. Reduced-motion path is provable by unit-toggling `HuaweiSyncMotion.current` — a headless capture harness is a Cycle 4 candidate.

## 8. Non-goals reaffirmed

- No new record types.
- No new integrations.
- No new dependencies.
- No claim of GymRats delivery in any Concept A string.
- No changes to Room, ledger, `Gate1SyncCoordinator`, deterministic identity, attempt count, deduplication, reconciliation, or claims policy.
- No changes to Onboarding, Pipeline, Sync-now, Integrations, Diagnostics, Automation, History, Activity Detail, or AI Assistant.
- No changes to `HuaweiSyncDestination`, `PrimaryDestinations`, `CompactDestinations`, or the More menu inventory.
- No changes to `HonestyCopyGuardTest`, `EyebrowBudgetTest`, `HuaweiSyncNavigationTest`, or `AccessibilityResponsiveRegressionTest`.
- No choice made between Concepts A, B, C — the intake explicitly instructed "não escolha nem aplique uma alternativa ao restante do aplicativo".

## 9. Stop for evaluation

Concept A is complete and stops here for human review. Concept B (Dynamic Fitness Utility) and Concept C (Connected Ecosystem) are gated behind approval of A. When you validate A on-device via the long-press gesture in a debug APK, respond with either:

- "Concept A approved for reference — proceed to B", or
- "Concept A needs rework — [feedback]", or
- "Concept A is enough — stop Cycle 3 here".

## 10. Followups (Cycle 3 continuation candidates)

- **Concept B slice** — Dynamic Fitness Utility as its own file tree (`conceptB/`), following the same debug-gated pattern. Motion signature is more energetic; per `cycle-03-directions.md § 2 Direction B`.
- **Concept C slice** — Connected Ecosystem with animated node connections; per `cycle-03-directions.md § 2 Direction C`.
- **Direction comparison doc** — after A/B/C ship, a comparison doc across the eight criteria (personality, clarity, simplicity, quality feel, differentiation, accessibility, maintenance, product-fit) with no binding recommendation.
- **Animation frame captures** — deferred to the chosen direction only, to avoid inflating the repo with speculative binary assets.
- **Reduced-motion on-device capture** — same followup already tracked in Cycle 2.
