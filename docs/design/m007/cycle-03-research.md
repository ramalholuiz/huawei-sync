# Cycle 3 — Visual research (Dashboard visual-language break)

**Cycle scope:** three parallel, real, executable Compose prototypes of the Dashboard, evaluated side-by-side. No production Dashboard change. No propagation to other screens.
**Author role:** Visual Researcher.
**Purpose:** collect the smallest useful set of visual references for three deliberately different personalities the Dashboard *could* have, then hand them to the Product Art Director step. Every reference below is **visual reference only** — nothing enters the APK.

Access date for every row below: 2026-07-17.

## 1. What Cycle 3 answers

Cycles 1 and 2 rebuilt the sync rail and the ledger reading experience honestly, but the app still reads as a **rigid, square, developer-technical** surface. Motion polish and copy honesty are correct; personality is missing. Cycle 3's job is not "add polish to the current Dashboard" — the code already has polish. It is to **stop propagating the current visual language automatically** and evaluate three fundamentally different Dashboard personalities against the same real data and states, before deciding whether any of them earns a rollout.

Constraints that shape the research:

- Preserve every runtime contract: Room, ledger, `Gate1SyncCoordinator`, deterministic identity, deduplication, reconciliation, claims policy, `ExerciseSessionRecord`.
- No new dependencies (no Rive, no Lottie, no image libraries beyond what is already shipping).
- No third-party logos, brand marks, or web-sourced imagery inside the APK. Original assets or explicitly-labelled placeholders only, tracked in `cycle-03-asset-manifest.md`.
- Do not choose one direction. Do not apply one direction to the rest of the app. Deliver the three functioning, capture the evidence, stop for human evaluation.
- The three concepts must render the same seven states from real fixtures: empty, waiting, syncing, success confirmed in Health Connect, error, GymRats available to import, and one representative composed state. All seven must render in light and dark themes.
- Each concept has **its own local design tokens** (surfaces, shapes, elevations, gradients, motion timing). It is not allowed to inherit `HuaweiSyncShapes` or the shared `HuaweiSyncTheme.geometry` values, so the concepts diverge visually where it matters.

Explicit exclusions: no changes to History, Activity Detail, Pipeline, Sync-now, Diagnostics, Automation, Integrations, Assistant, Onboarding, or the navigation shell. No new record types, no new integrations, no metric fabrication.

## 2. Reference table

Classifications:

- **REF** — visual reference only, does not enter the APK.
- **BLOCKED** — must not be copied, imitated 1:1, or shipped.

### 2.1 Concept A — Soft Premium Health (rounded, calm, wellness-forward)

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| A1 | Apple Health app overview | https://support.apple.com/en-us/HT203037 | Reference for a premium health surface that reads as care rather than instrumentation. Rounded cards, generous inner spacing, ink-forward palette, restrained accent. |
| A2 | Nike Training Club onboarding page | https://www.nike.com/ntc-app | Reference for warm approachable typography and generous corner radii on health-adjacent content. Do **not** copy the marketing imagery. |
| A3 | WHOOP membership overview | https://www.whoop.com/us/en/membership/ | Reference for calm night-black + soft mint palette on a data-heavy surface without feeling clinical. |
| A4 | Material 3 elevation & tonal surfaces | https://m3.material.io/styles/color/the-color-system/color-roles | Baseline for tonal surface families and subtle elevation — the API vocabulary we already have and can lean into for depth without shadows. |
| A5 | Refactoring UI shadow guidance | https://www.refactoringui.com/previews/shadow | Sanity check on soft, tinted, layered shadows vs harsh flat drops. |

### 2.2 Concept B — Dynamic Fitness Utility (asymmetric, expressive, energetic)

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| B1 | Linear public product page | https://linear.app/homepage | Reference for asymmetric composition, strong horizontal accent lines, and confident dark theming on a utility product. |
| B2 | Strava Summit dashboard | https://www.strava.com/subscribe | Reference for high-contrast fitness accents (electric orange over deep neutral) applied without being loud. Do **not** copy marketing hero imagery. |
| B3 | Vercel deployments dashboard | https://vercel.com/docs/deployments | Reference for utility dashboards that show state with expressive typography and asymmetric grids, no wasted whitespace. |
| B4 | Material 3 large-display expressive typography | https://m3.material.io/styles/typography/type-scale-tokens | Baseline for using display-scale typography to carry personality without illustration. |
| B5 | Refactoring UI hierarchy guidance | https://www.refactoringui.com/previews/hierarchy | Rules for building visual hierarchy through weight/size/contrast rather than borders and dividers. |

### 2.3 Concept C — Connected Ecosystem (flow-first, node-and-edge, technological)

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| C1 | Zapier Zap editor overview | https://zapier.com/how-it-works | Reference for a "source → transformation → destinations" node model that reads as connectedness, not tooling. |
| C2 | Home Assistant integrations map | https://www.home-assistant.io/integrations/ | Reference for a device/service node topology where each node has state and one clear connection line. |
| C3 | Vercel edge network diagram | https://vercel.com/docs/edge-network/overview | Reference for animated connection edges on a topology surface without turning it into a developer diagram. |
| C4 | Compose Canvas + graphicsLayer docs | https://developer.android.com/develop/ui/compose/graphics/draw/modifiers | Native-API baseline for the animated edges. Confirms Concept C does not need Rive/Lottie. |
| C5 | WCAG 2.2 animation-from-interactions | https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html | Reduced-motion accessibility requirements — reinforces `HuaweiSyncMotionPolicy.Reduced` for the flow animation. |

**No Huawei brand marks, no GymRats logo, no Strava logo, no Health Connect brand assets are referenced for inclusion.** They would move to BLOCKED if proposed.

## 3. What the research settles

- **The current visual language is *one* choice, not the only one.** The rectangular ModernistSurface + straight borders + technical microcopy budget is a valid personality for a developer-facing gate demo. It is not the only valid personality for a personal health-adjacent tool. Cycles 1 and 2 improved the current language; they did not evaluate alternatives.
- **Three different personalities can render the same honest facts.** Nothing about deterministic identity, ledger truth, or GymRats-availability wording forces a specific visual language. Soft-round, expressive-utility, and connected-flow can each render the same seven states without inventing metrics or claiming delivery.
- **Motion is a personality signal, not just a polish signal.** Cycle 1 gave the rail a motion vocabulary and Cycle 2 gave the timeline one; Cycle 3 must give each concept its own motion timing without introducing loops on idle state or bypassing `HuaweiSyncMotion.current.reducedMotion`.
- **Depth ≠ shadows.** Two of three concepts want depth. Material 3 tonal surfaces (`surface`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`) and small tinted shadows are enough. No new elevation system.
- **No image-based shortcut.** The temptation to "fix personality by adding a hero illustration" is exactly what the mission forbids. Personality comes from the composition, typography, color, radius, and motion of the surface itself. Illustrations may join later when they exist as original assets tracked in the asset manifest.
- **Local tokens beat theme edits.** Editing shared `HuaweiSyncShapes` or `HuaweiSyncTheme` would leak Cycle 3's exploration into every other screen. Each concept defines its own tokens (`ConceptATokens`, `ConceptBTokens`, `ConceptCTokens`) and reads them only from within its own composable tree.

## 4. What Cycle 3 will *not* do based on the research

- **No shared-token edits.** `HuaweiSyncShapes`, `HuaweiSyncGeometry`, `HuaweiSyncSpacing`, `HuaweiSyncTheme.colors` remain untouched. Cycle 3 adds concept-local tokens; it does not modify the production tokens or their consumers.
- **No propagation.** None of the concepts is wired into `HuaweiSyncNavigationState`, `HuaweiSyncDestination.all`, `PrimaryDestinations`, or `CompactDestinations`. The production Dashboard is unchanged and `HuaweiSyncNavigationTest` (which enforces exactly 10 destinations) still passes.
- **No new dependency.** No Rive, no Lottie, no image-decoding library beyond the ones already in `libs.versions.toml`.
- **No brand imagery.** No Huawei mark, no GymRats logo, no Health Connect logo, no fitness brand hero. Placeholder tiles use text or Compose-drawn shapes only.
- **No metric fabrication.** No sparkline, no chart, no "you did X today" — Gate 3 has not passed. Concept B may use expressive numeric typography on `ledgerWorkoutCount` and `attemptCount`, which are real facts; it may not invent metrics.
- **No claim of GymRats delivery.** All three concepts render the GymRats surface using `ProductGymRatsStatus.READY_TO_READ.label` verbatim ("Available for GymRats to import"). `HonestyCopyGuardTest` still passes.
- **No overwrite of the production Dashboard for evaluation.** The three concepts are reachable through a debug-only, dialog-based `PrototypeGallery` surface that leaves the production navigation untouched.

## 5. Notes for the Art Director step

- The three concepts are **not** three refinements of the current Dashboard. They are three fundamentally different personalities. If a concept feels like "current Dashboard with a nicer shadow", it is not the concept — go further.
- Each concept must render the same seven fixtures: `EMPTY`, `WAITING`, `SYNCING`, `SUCCESS_CONFIRMED`, `ERROR`, `GYMRATS_AVAILABLE`, `COMPOSED_REPRESENTATIVE`. All from real `ProductSyncState` values, no bespoke fake states.
- Each concept must ship its own token file. `ConceptATokens.kt`, `ConceptBTokens.kt`, `ConceptCTokens.kt`. Do not read `HuaweiSyncShapes` from concept code; every corner-radius, elevation, and spacing decision is local.
- Reuse of composables from `ui/components/` is only allowed when the composable is genuinely style-agnostic (e.g. `SyncPipelineRail` as a data-driven visual). If a component (`ModernistSurface`, `StatusLabel`, `TechnicalMicrocopy`) carries the current visual language into the concept, the concept must **not** use it — it must build its own equivalent locally.
- Every motion path has an explicit `if (motion.reducedMotion)` branch and is re-runnable without loops on idle state.
- No test regression is acceptable. `HonestyCopyGuardTest`, `EyebrowBudgetTest`, `AccessibilityResponsiveRegressionTest`, `HuaweiSyncNavigationTest`, and every runtime test must still pass.
- Deliverable per concept: (a) Composable renders all seven states in light and dark; (b) 14 emulator screenshots at `HuaweiSync_API_35`; (c) motion frames documented in `cycle-03-motion-spec.md`; (d) comparison entry in `cycle-03-directions.md`.
- The comparison at the end is **descriptive, not prescriptive.** The Art Director does not recommend one; the human evaluates on the emulator and decides.
