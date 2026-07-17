# Cycle 4 — Implementation roadmap

**Cycle scope.** Sequence the app-wide redesign work Cycle 4 recommends into small, independently-shippable cycles behind a human checkpoint at each hand-off. No production Compose ships in this cycle — the roadmap only names the cycles that would.
**Role.** Mobile Art Director + Design Systems Reviewer.
**Guardrails.** Runtime is untouched at every step (Room, ledger, coordinator, Health Connect, deterministic identity, deduplication, reconciliation, Gate 1 runtime). No metric fabrication. No brand animation. Every cycle keeps `HonestyCopyGuardTest`, `EyebrowBudgetTest`, and `AccessibilityResponsiveRegressionTest` green.

## 1. Sequencing principles

- **Change chrome before content.** The visual system + IA land first so downstream cycles use the new tokens from day one.
- **One direction at a time.** A/B/C are three options; the roadmap does not fan out — the human picks one, and that direction propagates through every cycle.
- **Small vertical cycles.** Each cycle owns 1 screen or 1 cross-cutting system, sized to fit inside a normal implementation session with tests.
- **Every cycle ends with human evaluation.** No cycle auto-triggers the next.
- **Runtime tests stay untouched.** Every cycle touches only presentation packages (`ui/`, `presentation/`, `theme/`).

## 2. Assumed prior sign-off before this roadmap runs

The roadmap assumes the human has approved:

1. **Option C IA** (Home / Activity / Connections + overflow) — `cycle-04-information-architecture.md § 5`.
2. **One of A / B / C** as the app-wide direction — `cycle-04-directions.md § 4`.
3. **Health Connect wordmark + icon** for shipping, following the source rules — `cycle-04-brand-assets.md § 3.1`.
4. **Standardising Material Symbols Rounded** app-wide — `cycle-04-brand-assets.md § 3.11`.
5. **Removing Automation and AI Assistant from primary nav for MVP** — `cycle-04-information-architecture.md § 8`.
6. **Sync now becoming a bottom sheet with `opening ≠ triggering`** — same doc.

If any of the six is not approved, the roadmap for the affected cycle re-scopes down to what remains legal.

## 3. Crosswalk — problem → cycle

Each cross-cutting problem in `cycle-04-app-inventory.md § 5` is addressed in a named cycle below.

| Problem (inventory § 5) | Addressed in |
|---|---|
| 1. Everything is a rectangle at the same altitude | **Cycle 5 — Visual system foundation** |
| 2. Engineering vocabulary leaks into every screen | **Cycle 5** + per-screen cycles for copy |
| 3. Diagnostics is a top-level tab | **Cycle 6 — Navigation shell (IA Option C)** |
| 4. Preview-only screens hold primary real estate | **Cycle 6** |
| 5. Sync now / Pipeline / Dashboard duplicate each other | **Cycle 6** + **Cycle 8 — Sync now sheet** |
| 6. Nothing has a real identity | **Cycle 10 — Connections + brand marks** |
| 7. Copy repeats across sections | Every per-screen cycle |
| 8. No chart today | **Cycle 9 — Activity redesign + charts** |
| 9. Motion is one-note | **Cycle 12 — Motion polish + reduced-motion regression** |
| 10. Onboarding does not actually onboard | **Cycle 7 — Home redesign** touches the Home permission handoff; **Cycle 13 — Onboarding rework** finishes it |

## 4. The cycles

Each cycle lists:

- **Objective.** What is different at the end.
- **Screens / components touched.**
- **Dependencies.** What must have shipped first.
- **Risks.**
- **Tests to add / preserve.**
- **Visual criteria (must-hold rules).**
- **Human checkpoint.** What the human evaluates at end-of-cycle before Cycle N+1 starts.
- **Estimated session count.** Rough order.

### 4.1 Cycle 5 — Visual system foundation

- **Objective.** Introduce the new tokens (radii, spacing, tonal surfaces, hue map for the *chosen* direction, elevation model). Retire `HuaweiSyncGeometry.cornerRadius`, `HuaweiSyncShapes(0.dp)`, `StraightEdgeButton`, `ModernistSurface`, `TechnicalMicrocopy` app-wide (Diagnostics retains uppercase eyebrow allowlist).
- **Screens touched.** None visually final; all screens are re-skinned against the new tokens but keep their current shape. Chrome-only cycle.
- **Components touched.** `Tokens.kt`, `Color.kt`, `Theme.kt`, `Type.kt`, new `Shapes.kt`, new component `SurfaceCard` / `RaisedCard` / `AttentionCard` / `AccentWashCard` / `PrimaryAction` / `SecondaryAction` / `TextAction` / `FabAction` / `StatusChip` / `FilterChip` / `EmptyStateSection` / `ErrorStateSection` / `LoadingSkeleton`.
- **Dependencies.** Direction chosen.
- **Risks.** `HonestyCopyGuardTest` and `EyebrowBudgetTest` may need updated allowlists (Diagnostics-only for uppercase eyebrow). Test-tag references may break if `ModernistSurface` is removed without a compat shim.
- **Tests.** Add `VisualTokensContractTest` — assert the token bag exposes every role named in visual-system § 5. Preserve existing runtime tests.
- **Visual criteria.** No visible border on `RaisedCard`. No `RoundedCornerShape(0)` in a shipping component. Every M3 default shape resolves to `radius.card` or `radius.hero`.
- **Human checkpoint.** Screenshot pass: every current screen re-skinned. No regressions in navigation flow.
- **Session count.** 2–3.

### 4.2 Cycle 6 — Navigation shell (Option C)

- **Objective.** Ship the three-tab shell (Home / Activity / Connections), the top-bar overflow (`Diagnose`, `Setup`, `About`, `Send feedback`), and the "opening ≠ triggering" fix for Sync now. Rename `Dashboard → Home`, `History → Activity`, `Integrations → Connections`. Retire `Pipeline`, `Automation`, `AI Assistant`, `Diagnostics` from primary nav.
- **Screens touched.** `HuaweiSyncRoot.kt`, `HuaweiSyncNavigationShell.kt`, `HuaweiSyncDestination.kt`, `HuaweiSyncNavigationState.kt`, `HuaweiSyncNavigationTest`.
- **Components touched.** Bottom bar (M3 `NavigationBar`), wide rail (`NavigationRail`), top bar (`CenterAlignedTopAppBar` on compact / `TopAppBar` on wide), overflow menu.
- **Dependencies.** Cycle 5.
- **Risks.** `HuaweiSyncNavigationTest` currently asserts `Destination.all.size == 10` — must change to `8`. Legacy deep links (`dashboard`, `history`, `integrations`, `pipeline`) need aliases per IA § 7.
- **Tests.** Update `HuaweiSyncNavigationTest` (destination count 8; primary destinations 3; compact destinations 3; overflow contents in order). Add a new `SyncNowOpenDoesNotTriggerTest` — asserts opening the sheet does not fire `onSync`.
- **Visual criteria.** Three tabs at bottom on compact; three items on wide rail. Overflow menu opens on tap. Sync now sheet slides in from bottom; drag-down dismisses.
- **Human checkpoint.** Flow: open the app → tap Sync now → sheet opens without firing sync → tap `Start sync` → sync runs. Confirm accessibility with TalkBack on the bottom bar and overflow menu.
- **Session count.** 2.

### 4.3 Cycle 7 — Home redesign

- **Objective.** Build the new Home screen per `cycle-04-screen-redesign.md § 1` and § 2 (compact + wide) using the direction's palette and typography. `Dashboard.kt` becomes `HomeScreen.kt`. Retire the debug long-press-on-logo gesture in favour of a long-press on the FAB.
- **Screens touched.** New `HomeScreen.kt`. Retire `DashboardScreen.kt`. Refactor `SyncFab` if needed. Update `HuaweiSyncNavigationShell` `home` destination.
- **Components touched.** `RaisedCard` (hero), `StatTile`, `AccentWashCard`, `AttentionCard`, `SourceDestinationRow` (new small component for the source→destination strip).
- **Dependencies.** Cycles 5–6.
- **Risks.** Wide-layout parity: the trailing `Sync details` panel must not double-render coordinator state (single source of truth: `PipelinePresentation.from(state, coordinatorBusy)`).
- **Tests.** `HomeHeroStateTest` — asserts the six hero states from `cycle-04-screen-redesign.md § 8` render honest copy. Screenshot pass at compact + wide.
- **Visual criteria.** Hero card at `radius.hero`. Two-tile stat strip only when data exists. FAB anchored bottom-right on compact; adjacent to the sync hub node on wide (Direction C only) or below the rail (A/B).
- **Human checkpoint.** Emulator screenshot pass at compact (412 × 892) and wide (1000 × 700), light + dark, six states.
- **Session count.** 2.

### 4.4 Cycle 8 — Sync now sheet

- **Objective.** Replace `SyncNowModal` (dialog) with `SyncNowSheet` (bottom sheet). Introduce `SyncDetailsSheet` reachable from Home hero, from the Sync now sheet's `Show phase detail`, and from Diagnostics.
- **Screens touched.** New `SyncNowSheet.kt`, new `SyncDetailsSheet.kt`. Retire `SyncNowModal.kt`.
- **Components touched.** `ModalBottomSheet`, `SyncPipelineRail` (reused verbatim), no letter medallion.
- **Dependencies.** Cycle 6 (nav shell rewrites the overlay slot).
- **Risks.** Wide viewport doesn't use a sheet — it uses a persistent trailing panel. Both must render the same content from `PipelinePresentation.from(state, coordinatorBusy)`.
- **Tests.** `SyncSheetContractTest` — asserts (a) opening ≠ triggering, (b) `Start sync` is the trigger, (c) dismissing while running does not cancel the coordinator, (d) reduced-motion path collapses to instant.
- **Visual criteria.** No letter medallion. `Coordinator phase` label appears in mixed case. Only `Start sync` triggers a new run.
- **Human checkpoint.** Flow: mid-sync, dismiss the sheet; confirm the coordinator continues. Reopen; sheet reflects current phase.
- **Session count.** 2.

### 4.5 Cycle 9 — Activity redesign + charts

- **Objective.** Build the new Activity screen per `cycle-04-screen-redesign.md § 3` with row visual + honest charts (`BarChartCard` for 12-week workouts and `StatTile` for sync success).
- **Screens touched.** New `ActivityScreen.kt`. Retire `HistoryScreen.kt`.
- **Components touched.** `BarChartCard`, `StatTile`, `ActivityRow`, day-header (soft caps).
- **Dependencies.** Cycles 5–6.
- **Risks.** Charts must render honestly at empty / low-sample states (see visual system § 11.1 / § 11.2). The bar chart's `Max: N` label must render only when `N > 0`.
- **Tests.** `ActivityChartHonestyTest` — asserts empty state renders `Sync your first workout`, `< 3` samples renders `— Not enough data yet` on the success tile.
- **Visual criteria.** Row primary title = start-of-workout time (until Gate 3). Attention filter is `FilterChip`.
- **Human checkpoint.** Screenshot pass at compact + wide, empty + populated, day-grouped and filtered.
- **Session count.** 2.

### 4.6 Cycle 10 — Connections + brand marks

- **Objective.** Build the new Connections screen per `cycle-04-screen-redesign.md § 4` with the Health Connect brand mark and every other integration as monogram + text. Introduce `ConnectionDetailSheet`.
- **Screens touched.** New `ConnectionsScreen.kt`. Retire `IntegrationsScreen.kt`.
- **Components touched.** `ConnectionRow`, `ConnectionDetailSheet`, `StatusChip`. Add `res/drawable/health_connect_wordmark_light.xml` and `_dark.xml` from the official Google source per `cycle-04-brand-assets.md § 3.1`.
- **Dependencies.** Cycles 5–6, human approval of brand-assets § 3.1.
- **Risks.** Health Connect asset must ship with the exact filename, size, and clear space documented at the source. `ASSET-MANIFEST.md` gets one new row per shipped file.
- **Tests.** `ConnectionsSurfaceContractTest` — asserts Active tiles render Health Connect at 24dp with the official wordmark asset, every other Active tile renders a monogram, Preview tiles are text + monogram. `ConnectionsAssetPresenceTest` — asserts the Health Connect wordmark drawable exists at the documented path.
- **Visual criteria.** No monogram in place of the Health Connect wordmark. No brand mark for GymRats, Huawei Health, or any other provider until brand approval lands.
- **Human checkpoint.** Screenshot pass at compact + wide. Verify the Health Connect wordmark's clear space vs the source rules.
- **Session count.** 2.

### 4.7 Cycle 11 — Activity detail redesign

- **Objective.** Rework Activity detail per `cycle-04-screen-redesign.md § 7`. Retire the sync-path rail from this screen; the lifecycle timeline is the single source of truth.
- **Screens touched.** `ActivityDetailScreen.kt`.
- **Components touched.** New identity header (workout header once real data lands; day + time until then), lifecycle timeline (already close), disclosure toggle.
- **Dependencies.** Cycles 5, 9.
- **Risks.** Post-Gate 3, header content type changes — the layout must accommodate a real workout name without redesign.
- **Tests.** `ActivityDetailHeaderTest` — asserts header renders honest copy in all readback states.
- **Visual criteria.** No sync-path rail on this screen.
- **Human checkpoint.** Screenshot pass + read-through with a human noting whether the timeline reads as a *story* or a *table*.
- **Session count.** 1.

### 4.8 Cycle 12 — Motion polish + reduced-motion regression

- **Objective.** Ship all motion signatures per `cycle-04-motion-system.md`. Add the reduced-motion regression tests.
- **Screens touched.** All.
- **Components touched.** `AnimatedContent`, `animateContentSize`, `ModalBottomSheet` slide-in.
- **Dependencies.** Every prior cycle (motion is polish).
- **Risks.** Motion budget per screen (motion-system § 7) must not be exceeded. `HuaweiSyncMotionPolicy.Reduced` path must be exercised.
- **Tests.** `ReducedMotionSnapshotTest` — every screen renders pixel-identical output with reduced motion on. `NoIdleMotionTest` — a screen at rest for 2 s is pixel-identical.
- **Visual criteria.** No looped animation without coordinator activity. No motion on any brand mark.
- **Human checkpoint.** Screen-record pass with reduced motion on and off.
- **Session count.** 1–2.

### 4.9 Cycle 13 — Onboarding rework

- **Objective.** Rework Onboarding into a two-step flow (mission + Health Connect permission grant) per direction. Auto-launch on first run when the user has not granted Health Connect.
- **Screens touched.** `OnboardingScreen.kt`. Add `FirstRunDetector` inside `HuaweiSyncNavigationShell`.
- **Components touched.** `RaisedCard`, `PrimaryAction`, `SecondaryAction`, watch-to-phone motif (placeholder until in-house illustration lands).
- **Dependencies.** Every prior cycle (Onboarding uses the mature system).
- **Risks.** First-run detection must survive process death; store the flag in `SharedPreferences` scoped to the app.
- **Tests.** `OnboardingFirstRunTest` — asserts the shell auto-launches Onboarding on first run and never on subsequent runs.
- **Visual criteria.** No duplicate primary action. Health Connect permission grant is a real Android intent, not `onSync`.
- **Human checkpoint.** Fresh-install flow: install debug APK → app opens on Onboarding → grant Health Connect → land on Home.
- **Session count.** 2.

### 4.10 Cycle 14 — Global states polish

- **Objective.** Ship the shared `EmptyStateSection`, `ErrorStateSection`, `LoadingSkeleton` app-wide. Replace bespoke empty / error / loading composables inside every screen.
- **Screens touched.** All.
- **Dependencies.** Cycle 5.
- **Risks.** `HistoryEmptyStateSurface`, `LoadingDashboard`, `LoadingDiagnostics` are unique; migration must preserve their existing test tags.
- **Tests.** `SharedStateComponentContractTest` — asserts every screen uses one of the three shared components for its empty / error / loading state.
- **Visual criteria.** No bespoke skeleton composable outside the shared component.
- **Human checkpoint.** Screenshot pass across every screen in every state.
- **Session count.** 1.

### 4.11 Cycle 15 — Illustration + logo polish

- **Objective.** Commission the in-house watch-to-phone motif illustration. Replace placeholder monograms in Onboarding with the finished asset. If GymRats has granted permission (see brand-assets § 8.3), ship the GymRats mark.
- **Screens touched.** Onboarding, Connections.
- **Dependencies.** Cycle 13.
- **Risks.** Illustration production is human work; block the cycle on the asset landing.
- **Tests.** `AssetManifestContractTest` — asserts every asset shipped in `res/drawable/` has an entry in `docs/design/assets/ASSET-MANIFEST.md`.
- **Visual criteria.** No placeholder text where the illustration should be.
- **Human checkpoint.** Review the illustration in-flow, on-device.
- **Session count.** 1.

## 5. Sequence at a glance

```
Cycle 5 — Visual system foundation      (chrome only, no screen final)
   └── HUMAN CHECKPOINT ──
Cycle 6 — Navigation shell (Option C)   (renames, retirements, overflow, sheet-vs-modal)
   └── HUMAN CHECKPOINT ──
Cycle 7 — Home redesign                  (hero + stat tiles + recent + FAB)
   └── HUMAN CHECKPOINT ──
Cycle 8 — Sync now sheet                 (opening ≠ triggering, no medallion)
   └── HUMAN CHECKPOINT ──
Cycle 9 — Activity redesign + charts     (bar chart + stat tile + rows)
   └── HUMAN CHECKPOINT ──
Cycle 10 — Connections + brand marks     (Health Connect asset lands)
   └── HUMAN CHECKPOINT ──
Cycle 11 — Activity detail redesign      (workout header + timeline only)
   └── HUMAN CHECKPOINT ──
Cycle 12 — Motion polish                 (reduced-motion + no-idle regression)
   └── HUMAN CHECKPOINT ──
Cycle 13 — Onboarding rework             (two-step flow, real permission grant)
   └── HUMAN CHECKPOINT ──
Cycle 14 — Global states polish          (shared empty/error/loading)
   └── HUMAN CHECKPOINT ──
Cycle 15 — Illustration + logo polish    (in-house motif + optional GymRats mark)
```

## 6. What each cycle intentionally does *not* do

- **No runtime change.** No cycle above touches Room, ledger, coordinator, deterministic identity, deduplication, reconciliation, Gate 1 runtime, `ExerciseSessionRecord`.
- **No new record types.** Every chart and every screen uses fields the ledger already stores.
- **No new integrations.** Strava / Fitbit / Garmin / Samsung Health / etc. remain Preview tiles until Gate 2 + brand approval.
- **No fabricated metrics.** Calories, HR, distance, sleep, SpO2 remain outside scope.
- **No new dependency.** No Rive, no Lottie, no font library, no image library. Any illustration ships as an original SVG / vector drawable.
- **No brand animation.** Containers and connections may animate; logos do not.
- **No claim of GymRats delivery.** `ProductGymRatsStatus.READY_TO_READ.label` remains verbatim on every surface until Gate 2 passes and the app has real proof of delivery.

## 7. Risk register

| Risk | Cycle | Mitigation |
|---|---|---|
| `HuaweiSyncNavigationTest` fails when we drop to 8 destinations | 6 | Update the test contract in the same PR that changes the shell. |
| Health Connect brand asset shipped without the required clear space | 10 | Cycle-10 checklist references `cycle-04-brand-assets.md § 3.1` verbatim; test asserts drawable presence + size. |
| Motion violates the reduced-motion contract | 12 | `ReducedMotionSnapshotTest` blocks the cycle unless every screen matches motion-off pixel-identically. |
| Illustration delay blocks Cycle 15 | 15 | Cycles 5–14 ship with placeholder monogram; 15 is unblocking only if the asset lands. |
| Wide-viewport `Sync details` panel and Sync now sheet fall out of sync | 7 / 8 | Both consume the same `PipelinePresentation.from(state, coordinatorBusy)`; contract test asserts single source of truth. |
| `HonestyCopyGuardTest` breaks when copy rewrites land | 7 / 9 / 11 / 13 | Every rewrite runs against the guard before landing; if a guard fails, either the copy is wrong or the guard is stale — no shipping without resolution. |

## 8. What "done" means for the redesign as a whole

At the end of Cycle 15:

- Home / Activity / Connections tabs are the only primary destinations.
- Sync now is a bottom sheet, opening does not trigger.
- Health Connect mark ships in Onboarding + Connections.
- Activity has an honest bar chart and success-rate tile.
- Activity detail leads with the workout, not with technical identity.
- Diagnostics is reachable from the overflow menu and from every error state.
- Motion is reduced-motion-safe and never idle-loops.
- The visual system is single-source-of-truth (`Tokens.kt`, `Shapes.kt`, `Type.kt`, `Color.kt`, component library).
- No production regression on Gate 1 (all runtime tests green).
