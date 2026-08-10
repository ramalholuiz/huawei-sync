# Cycle 3 — Motion spec (three concepts)

**Author role:** Product Art Director + Compose Engineer handoff.
**Scope:** three Dashboard prototypes (A / B / C) rendered inside a debug-only PrototypeGallery. No production motion is modified.
**Motion policy source:** `dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy`. Every entry below MUST specify its reduced-motion behavior.

## 1. Cross-cutting rules

The following hold for all three concepts. Non-negotiable.

- **No motion on `IDLE`.** If `ProductSyncState.phase == ProductSyncPhase.IDLE` and the state is not `SUCCESS_CONFIRMED`, no ambient motion runs. This is stricter than Cycle 1's rail policy and matches Cycle 2's "no ambient motion on read-only surfaces" rule.
- **No claim through motion.** No animation celebrates a GymRats delivery. The GymRats surface never runs a confirmation animation; it only carries the honesty label.
- **Every animation has a `HuaweiSyncMotion.current.reducedMotion` branch.** The branch is not "faster" — it is "static". Reduced motion renders the final visual state at frame zero.
- **No `while(true)` or `repeatable(iterations = infinite)`** in concept code. The single exception is Concept B's pill FAB glow, which is a `rememberInfiniteTransition` bounded by a `LaunchedEffect(phase)` that stops the transition when `phase == IDLE`. Same rule applies to Concept C's edge pulse.
- **Reuse `HuaweiSyncMotionPolicy` tokens.** No concept defines its own `standardMillis` or `fastMillis`. Where a concept needs its own timing name (e.g. Concept C's `pulseDuration = 1600ms`), it is declared as a `val` inside the concept's tokens file and its value is derived from `HuaweiSyncMotionPolicy.current.standardMillis * 6` (or similar), so a global motion policy change still cascades.

## 2. Concept A — Soft Premium Health

Warm and calm. Three motion families, none of them loud.

| Family | Where | Standard motion | Reduced-motion fallback |
|---|---|---|---|
| **State cross-fade** | Hero card between `EMPTY` / `WAITING` / `SYNCING` / `SUCCESS_CONFIRMED` / `ERROR` | `AnimatedContent(targetState = sync.dominantState, transitionSpec = { fadeIn(tween(standardMillis)) with fadeOut(tween(fastMillis)) })` | `AnimatedContent` bypassed — direct swap of the hero content |
| **Soft pulse** | Small status dot on hero card during `WAITING` and `SYNCING` | `rememberInfiniteTransition` on `alpha = 0.4f..1f` at 700 ms sine, tied to a `LaunchedEffect(phase)` that cancels on `IDLE`/`SUCCESS_CONFIRMED` | Dot renders at `alpha = 0.8f` static; no pulse |
| **Count reveal** | Ledger card metric row when `ledgerWorkoutCount` changes | `Modifier.animateContentSize(tween(standardMillis))` around the count `Text` | `animateContentSize` skipped; instant layout |

Wash gradient on `SUCCESS_CONFIRMED` is not animated — it is a static `Brush.verticalGradient` applied to the hero card's background. The switch into and out of the wash is carried by the state cross-fade above.

## 3. Concept B — Dynamic Fitness Utility

Confident and expressive. Two motion families.

| Family | Where | Standard motion | Reduced-motion fallback |
|---|---|---|---|
| **Display swap** | Hero plate numeric / phase label when `sync.phase` changes | `AnimatedContent` with `slideInVertically(tween(fastMillis)) { it / 2 } + fadeIn(tween(fastMillis))` as enter and `slideOutVertically(tween(fastMillis)) { -it / 2 } + fadeOut(tween(fastMillis))` as exit | `AnimatedContent` bypassed — direct swap |
| **Pill glow** | Pill FAB when `phase != IDLE` | `rememberInfiniteTransition` on `alpha = 0.4f..0.9f` at 1400 ms sine, applied to a `drawBehind` gradient border. Cancelled by `LaunchedEffect(phase)` when `phase == IDLE` | Static glow at `alpha = 0.65f`; no pulse |

Plate hairlines and internal spacing do not animate. The asymmetric shape is the personality; motion does not need to reinforce it.

## 4. Concept C — Connected Ecosystem

Flow-first. Two motion families, both tied to real coordinator activity.

| Family | Where | Standard motion | Reduced-motion fallback |
|---|---|---|---|
| **Edge dash draw** | Any topology edge whose two endpoints are in a `WAITING` or `SYNCING`-like state | `Animatable(0f..1f)` cycled at `pulseDuration = 1600 ms` using `tween(pulseDuration, easing = LinearEasing)`, driving a `PathEffect.dashPathEffect(floatArrayOf(dashLen, gapLen), phase = value * (dashLen + gapLen))`. Tied to a `LaunchedEffect(edgeState)` that cancels on `dormant` and `confirmed`. | Edge renders as a static dashed line for `WAITING` / `SYNCING`, static solid line for `confirmed`, static thin line for `dormant`. No animation. |
| **Node pulse** | Node whose local state is `active` (currently syncing) | `rememberInfiniteTransition` on `scale = 1f..1.03f` at 1200 ms sine, applied to `Modifier.graphicsLayer { scaleX = value; scaleY = value }`. Cancelled by `LaunchedEffect(nodeState)` on `dormant`/`confirmed`. | Node renders at `scale = 1f` static; no pulse. |

Nodes themselves do not fade in on first composition — they render at their final position at frame zero. The topology is a **static** graph; only its edges and the currently-active node animate.

## 5. PrototypeGallery motion

The gallery itself is a debug-only fullscreen `Dialog`. Motion is minimal:

- Dialog fade-in on open: Compose's default `Dialog` transition. No override.
- Tab switch between A / B / C: `AnimatedContent` with `slideInHorizontally(tween(fastMillis))` + `fadeIn(tween(fastMillis))`. Reduced motion → direct swap.
- State selector row: state changes swap the underlying `ProductSyncState` fixture. Each concept's own motion above handles the resulting transition.
- Theme toggle: `HuaweiSyncTheme` re-composes; no explicit motion is added on top.

## 6. Accessibility

- Every animation above has an explicit reduced-motion branch (Section 2, 3, 4).
- TalkBack reading order is stable across concepts. Nothing is added to the DOM by a motion; the composition tree is the same whether motion is standard or reduced.
- No focus shift is triggered by any animation. Focus lands where it would in the reduced-motion path.
- No color-only encoding. Every state (waiting, syncing, success, error, gymrats-available) has a text label next to its color/shape signal.
- The honesty disclaimer required on the production Dashboard ("This view reports ledger and readback facts only…") is preserved verbatim in all three concepts and is not behind any toggle in any state.

## 7. Non-motion decisions bundled with this spec

Bundled here because the engineer will hit them the moment the spec is applied:

- **Local motion tokens.** Concept A/B/C each declare motion timing derived from `HuaweiSyncMotionPolicy.current` inside their own tokens file. No concept re-declares `standardMillis` or `fastMillis`.
- **`LaunchedEffect(phase)` gating.** Every `rememberInfiniteTransition` in Cycle 3 is guarded by a `LaunchedEffect` on the phase (or edge state / node state) so animation stops the frame the underlying state exits its "active" range. No animation continues running when the sync is IDLE.
- **State-selector fixtures.** The seven fixtures (EMPTY / WAITING / SYNCING / SUCCESS_CONFIRMED / ERROR / GYMRATS_AVAILABLE / COMPOSED) are constructed in `PrototypeGalleryFixtures.kt` from real `ProductSyncState` values, using `ProductSyncStateMapper` where possible. Fixture builder functions never invent metrics; only fields the ledger and coordinator can legally produce are set.
- **One accent per concept per surface.** Concept A: the hero-card accent wash (only on `SUCCESS_CONFIRMED`). Concept B: the pill FAB glow (only when `phase != IDLE`). Concept C: the active edge color (only on the currently active edge). Never more than one accent-driven element on the same surface at the same time.
- **No test regression.** `HonestyCopyGuardTest`, `EyebrowBudgetTest`, `AccessibilityResponsiveRegressionTest`, `HuaweiSyncNavigationTest`, and every runtime test must still pass after Cycle 3 lands. `EyebrowBudgetTest` currently caps eyebrows per production screen; Cycle 3 concept composables are not measured by that test today (they are not one of the production screens), but each concept must not introduce more than one `TechnicalMicrocopy`-equivalent eyebrow per surface, matching the production budget as a self-imposed rule.
