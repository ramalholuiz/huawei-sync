# Cycle 3 — Directions (Dashboard visual-language break)

**Author role:** Product Art Director (Impeccable).
**Vocabulary:** design · redesign · shape · critique · distill · animate · colorize · compose.
**Inputs:** `cycle-03-research.md`, current Compose sources (`DashboardScreen.kt`, `ProductSyncState.kt`, `SyncPipelineRail.kt`, `PipelinePresentation.kt`, `ModernistComponents.kt`, `HuaweiSyncTheme.kt`, `Tokens.kt`, `MotionPolicy.kt`).
**Guardrails:** every runtime contract preserved (see `docs/milestones/M007/PLAN.md § 3.3`). `HonestyCopyGuardTest`, `EyebrowBudgetTest`, `AccessibilityResponsiveRegressionTest`, `HuaweiSyncNavigationTest` must still pass. `HuaweiSyncDestination.all` stays at 10 destinations exactly.

## 1. Critique of the current Dashboard

Before proposing three directions, distill what the current Dashboard code does and where the *personality* — not the correctness — hurts.

### 1.1 Kept

- **Honesty.** Every string is derivable from a real fact. `sanitizedFailureSummary`, `ProductGymRatsStatus.READY_TO_READ.label`, `ProductHealthConnectStatus` labels — none of them lie. `HonestyCopyGuardTest` and `EyebrowBudgetTest` guardrails are correct and non-negotiable.
- **Data flow.** `DashboardScreenState.Loading | Content(sync)` is exhaustive. `PipelinePresentation.from(sync, coordinatorBusy)` is a clean pure projection. `SyncPipelineRail` is a genuinely re-usable data-driven visual and can survive into any of the three concepts.
- **Layout regions.** SyncHero → LedgerSummary → ConnectedServices → VerificationSummary is the right order of information. The critique is *how* they read, not the sequence.

### 1.2 Hurts

- **Rectangles everywhere.** `HuaweiSyncGeometry.borderThin` on every surface, `HuaweiSyncTheme.colors.line` bordering every card, `HuaweiSyncShapes` biased to right angles. The overall impression is "developer console", not "personal health tool".
- **Depth is a border, not a surface.** There is no meaningful use of tonal surface families or shadow. Every card sits at the same visual altitude, which flattens hierarchy.
- **`TechnicalMicrocopy` dominates.** `LIVE PRODUCT STATE`, `IDLE`, `PREFLIGHT`, `SANITIZED FAILURE SUMMARY`, `PHASE 1 OF 5 — PREFLIGHT` — every region opens with an uppercase technical eyebrow. Correct as truth; heavy as personality.
- **Monogram tiles.** The `HC` / `GR` monogram squares in `ConnectedServices` read as placeholder chrome, not as identity. They are neither logos (we can't use those) nor a compositional element that carries meaning.
- **Metric row is a printf.** `MetricRow(label, value)` renders "Workouts tracked  0" with `bodyMedium` label and `technicalTypography.value` value. Utilitarian, uninteresting, and does nothing for personality.
- **One-note motion.** Cycle 1's rail motion is the only motion on Dashboard. The FAB, the surfaces, the ledger row — none of them have per-concept motion signatures.
- **Palette is small and cold.** `ink / ink2 / line / lineStrong / accent / accentContainer / surface1 / surface2 / surface3 / warning / ok` covers correctness but does not build personality. There is no soft tint, no depth gradient, no accent-that-warms.

## 2. Three directions

Each concept is a **complete Dashboard replacement inside its own composable tree**. It reads the same `ProductSyncState` fixtures the production Dashboard reads. It uses its own local `Concept{X}Tokens` — no reference to `HuaweiSyncShapes`, `HuaweiSyncGeometry`, `ModernistSurface`, `ModernistStatus`, `StatusLabel`, `TechnicalMicrocopy`, or `SectionHeader`. Shared production tokens (`HuaweiSyncTheme.colors`) are readable but each concept re-maps them into its own local palette (`ConceptAColors`, `ConceptBColors`, `ConceptCColors`) with optional additions.

### Direction A — Soft Premium Health

Warm, rounded, calm. The Dashboard reads as a personal-care surface, not a control panel.

- **Personality.** Wellness-forward. Every surface is a rounded container with subtle tinted depth. Typography leans on `MaterialTheme.typography.headlineMedium` and `titleLarge` for warmth; `technicalTypography.microcopy` is banned from Concept A entirely (state is communicated through composition, not eyebrows).
- **Local tokens (`ConceptATokens`).**
  - Corner radii: `xs = 12dp`, `sm = 16dp`, `md = 20dp`, `lg = 24dp`, `xl = 28dp`.
  - Surface families: `surfaceCanvas` (bg), `surfacePanel` (elevated card), `surfaceInset` (nested content), `surfaceAccentWash` (readback-confirmed hero band with a discreet vertical gradient from `HuaweiSyncTheme.colors.ok.copy(alpha = 0.08f)` to `Color.Transparent`).
  - Shadows: `Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp), ambientColor = HuaweiSyncTheme.colors.ink.copy(alpha = 0.6f), spotColor = HuaweiSyncTheme.colors.accent.copy(alpha = 0.15f))` — soft, single-layer, tinted.
  - Spacing scale: `xs = 6dp`, `sm = 12dp`, `md = 16dp`, `lg = 20dp`, `xl = 28dp`, `xxl = 40dp` — deliberately more generous than the production `HuaweiSyncSpacing`.
- **Layout.** A single-column stack of large rounded cards, each with generous inner padding (`24dp` all around). The SyncHero card uses the accent-wash gradient when `phase == SUCCESS_CONFIRMED`; otherwise it uses `surfacePanel` unbordered. The FAB is a `56dp` circle with a soft tinted shadow, no square edges anywhere.
- **State language.**
  - `EMPTY` — hero card "Ready to sync your first workout"; ledger card "No workouts yet. Sync when your watch is nearby."
  - `WAITING` — hero card "Waiting to sync" with a soft pulsing dot; the ok color at low alpha behind it.
  - `SYNCING` — hero card "Syncing your workout" with the rail below as a horizontal progress arc; the pulse quickens.
  - `SUCCESS_CONFIRMED` — hero card wears the accent-wash gradient; a checkmark drifts in from below; ledger card shows `1 workout tracked`.
  - `ERROR` — hero card border becomes `HuaweiSyncTheme.colors.accent` at low alpha; a rounded chip below shows the sanitized failure summary.
  - `GYMRATS_AVAILABLE` — a discrete card at the bottom "Ready for GymRats" using the honesty label verbatim, calm not celebratory.
  - `COMPOSED_REPRESENTATIVE` — SYNCING hero + 3-workout ledger + GymRats-available card + Health Connect confirmed pill.
- **Motion (spec in `cycle-03-motion-spec.md § 2`).** A single ambient pulse on the state indicator during `WAITING`/`SYNCING` (700ms sine, reduced-motion path = static). Hero card cross-fades between state variants over `standardMillis`. Ledger card animates count changes with `animateContentSize`. No animation on the FAB idle. Reduced-motion path is enforced on all three.

### Direction B — Dynamic Fitness Utility

Confident, asymmetric, energetic. The Dashboard reads as a modern fitness product — big numbers, strong contrast, expressive typography, no wasted whitespace.

- **Personality.** Utility with attitude. Display-scale typography (`displayMedium` / `displayLarge`) carries the primary numeric fact (`ledgerWorkoutCount` or `attemptCount`), and the surrounding composition asymmetrically leans on it. Rectangles are allowed but they are not right-angled boxes — they are asymmetric plates.
- **Local tokens (`ConceptBTokens`).**
  - Corner radii: `xs = 4dp`, `sm = 8dp` for chips; **plates** use `RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 4.dp)` — an intentional diagonal-mirror shape that reads as motion at rest.
  - Surface families: `surfaceDeep` (near-black in dark theme, near-white in light), `surfaceCharged` (accent-tinted for the primary hero plate), `surfaceInset`.
  - Palette additions: `pulseAccent` (electric orange derived from `HuaweiSyncTheme.colors.accent` shifted 20° toward warm), `pulseAccentMuted`, `hairline` (`ink.copy(alpha = 0.15f)`).
  - Typography: local `ConceptBTypography` upsizes `displayMedium` to 56sp for the primary metric; `titleMedium` becomes `18sp` with `letterSpacing = 0.02.em`; microcopy stays at production size for accessibility.
  - Spacing scale: production `HuaweiSyncSpacing` for consistency, but `xxl` is `56dp` for the hero band.
- **Layout.** Asymmetric two-plate composition at the top: a large `surfaceCharged` plate on the left with the primary numeric fact (`ledgerWorkoutCount` or `phase.displayLabel()` as a compact caps stamp), and a narrow `surfaceInset` plate on the right showing the readback state and next action. Below, a horizontal `SyncPipelineRail` on a `surfaceDeep` band. Below that, a two-column grid of small plates for `Verification`, `Attempts`, `GymRats-available`. FAB is a **pill**, not a circle — 56dp × 128dp — anchored bottom-right with a subtle accent glow when the phase is active.
- **State language.**
  - `EMPTY` — hero plate reads "0 · READY" in `displayMedium`; right plate reads "Ready to sync".
  - `WAITING` — hero plate reads "· HOLDING" and pulses on the accent border.
  - `SYNCING` — hero plate reads the phase label (`PREFLIGHT` / `WRITE` / etc) in display type; rail below shows the active flow.
  - `SUCCESS_CONFIRMED` — hero plate switches to `pulseAccent` background with the count in display type; right plate reads "Confirmed in Health Connect".
  - `ERROR` — hero plate switches to a hairline treatment (no fill) with the accent color as border only; the sanitized failure summary lives on the right plate.
  - `GYMRATS_AVAILABLE` — small plate reads "Available for GymRats to import" using the honesty label verbatim; no celebration.
  - `COMPOSED_REPRESENTATIVE` — SYNCING hero + rail active + attempts=1 + GymRats plate.
- **Motion (spec in `cycle-03-motion-spec.md § 3`).** The hero-plate numeric changes with `AnimatedContent` using a slide-and-fade transition (fast). The active phase label enters from below with a `slideInVertically`. The pill FAB glows via a `Modifier.drawBehind` gradient that pulses at 1400ms sine when the phase is not IDLE (reduced-motion = static glow at 50%). No idle motion when phase is IDLE.

### Direction C — Connected Ecosystem

Node-and-edge topology. The Dashboard reads as a flow between three real endpoints — the watch, this app, Health Connect — with GymRats shown as an available downstream consumer.

- **Personality.** Flow-first. The primary composition is a **topology** drawn in Compose Canvas: three source/hub/destination nodes on a horizontal (compact) or vertical (wide) axis, with animated edges when the coordinator is not IDLE. Everything else on the Dashboard supports that topology, not competes with it.
- **Local tokens (`ConceptCTokens`).**
  - Corner radii: `nodeRadius = 20dp` (nodes are pill-shaped or circular depending on role), `panelRadius = 12dp` for supporting cards.
  - Surface families: `surfaceLattice` (bg with a very faint dotted grid drawn behind the topology at 2% ink alpha), `surfaceNode` (near-surface with 1dp hairline), `surfaceNodeActive` (accent-tinted).
  - Edge colors: `edgeDormant = ink.copy(alpha = 0.20f)`, `edgeActive = accent`, `edgeConfirmed = ok`. Edge width `2.dp` dormant, `3.dp` active.
  - Motion palette: `pulseDuration = 1600ms`, `edgeDrawDuration = standardMillis`.
- **Layout.** Compact viewport (< 720dp width): topology is a **horizontal row** at the top — `[WATCH] — [HUAWEI SYNC] — [HEALTH CONNECT] → (GYMRATS)`. Wide viewport (≥ 720dp): topology is a **vertical column** on the leading side and a supporting panel is on the trailing side (auto-adapts). Below the topology (or beside it, on wide): a compact panel with `Verification`, `Attempts`, `Sanitized failure summary`. The FAB is a **circular sync button** placed adjacent to the `HUAWEI SYNC` hub node itself (not floating in the corner), making the composition read as "press the hub to sync".
- **State language.**
  - `EMPTY` — all edges dormant; nodes show their name and state ("Watch: not paired · Sync: ready · Health Connect: ready · GymRats: available").
  - `WAITING` — Watch and Sync nodes light up softly; the edge between them draws a slow dashed animation.
  - `SYNCING` — the edge from Sync to Health Connect draws in with a moving-dash pattern; Health Connect node pulses ready.
  - `SUCCESS_CONFIRMED` — Health Connect node fills with the ok color; the edge from Sync to Health Connect turns solid `edgeConfirmed`; GymRats node lights up with the honesty label as its state text.
  - `ERROR` — the last active edge switches to `accent` color and dash pattern; the offending node wears a `2.dp` accent border; sanitized failure summary lives in the supporting panel.
  - `GYMRATS_AVAILABLE` — GymRats node shows "Available for GymRats to import" verbatim, using `edgeDormant` between HC and GymRats (never `edgeConfirmed` — Gate 2 has not passed).
  - `COMPOSED_REPRESENTATIVE` — three edges active with different states (Watch→Sync confirmed, Sync→HC active-syncing, HC→GymRats dormant-available).
- **Motion (spec in `cycle-03-motion-spec.md § 4`).** Edge dash-draw uses `Animatable` at `pulseDuration` cadence when the connected node pair is in an active state. Node pulse is a `scale = 1f → 1.03f → 1f` at 1200ms during `WAITING`/`SYNCING`. Reduced-motion path = static dash pattern (no motion), static scale (no pulse). Motion **never** runs on `IDLE` state.

## 3. Comparison framework

The comparison table below is the **framework** the human will use on the emulator to evaluate the three concepts side by side. It is **not** filled in by the Art Director — Cycle 3 does not choose. It ships intentionally blank for the human to complete during evaluation.

| Criterion | A — Soft Premium Health | B — Dynamic Fitness Utility | C — Connected Ecosystem |
|---|---|---|---|
| Personality (does it feel like a personal health tool?) | _(human)_ | _(human)_ | _(human)_ |
| Clarity (can you read the current state at a glance?) | _(human)_ | _(human)_ | _(human)_ |
| Simplicity (fewer moving parts is better) | _(human)_ | _(human)_ | _(human)_ |
| Quality feel (does it read as premium?) | _(human)_ | _(human)_ | _(human)_ |
| Differentiation (does it look distinctly different from a generic Compose app?) | _(human)_ | _(human)_ | _(human)_ |
| Accessibility (contrast, TalkBack order, reduced-motion) | _(human)_ | _(human)_ | _(human)_ |
| Maintenance (would this survive a year of feature work?) | _(human)_ | _(human)_ | _(human)_ |
| Alignment with Huawei Sync's purpose (sync workouts honestly into Health Connect for GymRats) | _(human)_ | _(human)_ | _(human)_ |

The Art Director's own read is captured in § 4 below, but as observation, not as a recommendation.

## 4. Art Director's observations (not a recommendation)

- **A is the safest departure.** Rounded surfaces + subtle depth is the shortest visual step away from the current developer-console feel. Risk: if it goes too far into "generic wellness app", it loses the app's technical character. Mitigation: keep the sync rail and the honest labels visible; only the container language softens.
- **B is the boldest personality.** Display typography and asymmetric plates communicate the most personality per screen, and match users who think of themselves as owning a fitness product. Risk: display typography and asymmetric plates hurt on small screens; the two-plate hero fights the sync rail for attention. Mitigation: enforce a single primary numeric fact per hero, keep the sync rail visually secondary.
- **C is the most concept-forward.** The topology reads as "this is what Huawei Sync does" at a glance — no other concept communicates the app's purpose so directly. Risk: the flow diagram can slip into looking like a developer tool if the nodes read as boxes with labels rather than as places. Mitigation: node typography is warm and rounded, not caps + monospace; edges use color for state, not for tech.

A, B, C are not versions of the same idea. They are three different answers to "what personality should the primary Dashboard have?", and each answers "how do we render the seven states honestly?" internally consistent to its personality.

## 5. What Cycle 3 ships

- Three self-contained Compose composables — `ConceptADashboard.kt`, `ConceptBDashboard.kt`, `ConceptCDashboard.kt` — each with its own tokens file.
- One debug-only `PrototypeGallery.kt` composable rendered in a fullscreen `Dialog`, launched from a long-press on the Dashboard top-bar logo when `BuildConfig.DEBUG` is true. The dialog offers three tabs (A / B / C) and a state-selector row (EMPTY / WAITING / SYNCING / SUCCESS / ERROR / GYMRATS / COMPOSED) plus a theme toggle.
- One shared fixtures file (`PrototypeGalleryFixtures.kt`) that maps the seven states to real `ProductSyncState` values built from `ProductSyncStateMapper.mapAvailability`/`mapLedgerStatus`/`mapPermission`. No bespoke fake state.
- Per-concept motion spec in `cycle-03-motion-spec.md`, including the reduced-motion fallback for each animation.
- 14 emulator captures per concept (7 states × 2 themes) in `docs/milestones/M007/screenshots/cycle-03/concept-a|b|c/`.
- One comparison entry per concept in this document.

## 6. What Cycle 3 does NOT ship

- No change to the production Dashboard.
- No change to `HuaweiSyncNavigationState`, `HuaweiSyncDestination`, `PrimaryDestinations`, `CompactDestinations`. `HuaweiSyncNavigationTest` still asserts exactly 10 destinations.
- No change to `HuaweiSyncShapes`, `HuaweiSyncGeometry`, `HuaweiSyncSpacing`, `HuaweiSyncTheme` production tokens. Every token added by Cycle 3 lives in a concept-local file that only the concept's own composables import.
- No new dependency in `libs.versions.toml` or `app/build.gradle.kts` beyond `buildConfig = true` on the existing `buildFeatures` block (required for `BuildConfig.DEBUG`).
- No claim of GymRats delivery. `ProductGymRatsStatus.READY_TO_READ.label` verbatim, in every concept.
- No brand asset in the APK. `cycle-03-asset-manifest.md` declares that Cycle 3 ships no bitmap or vector asset — only Compose-drawn shapes and text.
- No choice between A, B, C. The comparison table in § 3 is left for the human to fill on the emulator.
- No propagation to History, Detail, Sync-now, Pipeline, Integrations, Diagnostics, Automation, Assistant, or Onboarding. None of them are touched.
