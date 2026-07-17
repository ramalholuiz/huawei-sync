# Cycle 4 — Motion system (app-wide)

**Cycle scope.** Extend the existing `HuaweiSyncMotionPolicy` into an app-wide motion vocabulary. No production animation ships in Cycle 4.
**Role.** Motion Designer + Accessibility Reviewer.
**Baseline.** `app/src/main/java/dev/lui/huaweisync/ui/components/MotionPolicy.kt` — `fast 160ms`, `standard 240ms`, `deliberate 480ms`, `syncRotation 1000ms`, plus `Reduced` (all zeros).
**Guardrails from intake.** No infinite animation without an active operation. No simulated progress. No third-party logo animation. No new dependencies (no Rive, no Lottie).

## 1. What motion is *for*

Motion in Huawei Sync must communicate one of six facts:

1. **Origin.** Where the flow started (a tap, a state change, a coordinator event).
2. **Destination.** Where the content is going (the sheet, the detail screen, the next state).
3. **Progress.** *Real* work happening — always keyed to an observed coordinator event.
4. **Verification.** A success moment tied to `realReadbackConfirmed = true`.
5. **Confirmation.** A user's action being acknowledged (button ripple, sheet appearance).
6. **Failure.** Something went wrong — a short, single-shot signal, never repeated.
7. **Next action.** A subtle nudge (chip appearance) after work completes.

Motion **must not**:

- Loop without an active coordinator phase.
- Simulate progress (no fake 30 % / 60 % / 90 %).
- Decorate a logo (containers may animate; logos do not).
- Delay the primary action's discoverability (the first tap-target must be reachable within `fast` from screen entry).

## 2. Duration and easing tokens

Extend the existing `HuaweiSyncMotionPolicy` with named easings (already-Compose-native `FastOutSlowInEasing`, `LinearEasing`, custom curves defined below).

| Token | Value | Purpose |
|---|---|---|
| `duration.fast` | 160 ms | Chip in/out; small UI reactions. |
| `duration.standard` | 240 ms | Hero cross-fade; card enter/exit. |
| `duration.deliberate` | 480 ms | Sheet enter; list-to-detail transition. |
| `duration.observed` | Variable (real observation window from coordinator) | Progress that is keyed to actual events. |
| `duration.syncRotation` | 1000 ms | Rotation on active-phase indicator. |
| `easing.enter` | `CubicBezier(0.05, 0.7, 0.1, 1.0)` — M3 "emphasized decelerate" | Content coming in. |
| `easing.exit` | `CubicBezier(0.3, 0.0, 0.8, 0.15)` — M3 "emphasized accelerate" | Content going out. |
| `easing.change` | `FastOutSlowInEasing` | State swaps inside a persistent surface. |
| `easing.rotate` | `LinearEasing` | Active-phase rotation. |

Reduced-motion:

- All `duration.*` values collapse to `0 ms`.
- `easing.*` values are ignored (no interpolation).
- Every animation call has an explicit `if (motion.reducedMotion) { … static … }` branch.

## 3. Motion signatures per interaction

### 3.1 Navigation between top-level tabs

- **Standard.** Cross-fade of the content region only. `duration.standard`. `easing.change`.
- **Reduced motion.** Instant swap.
- Never animates the tab bar itself.

### 3.2 List → detail (Activity → Activity detail)

- **Standard.** Shared-element transition on the row's identity band (accent strip + monogram). `duration.deliberate`. `easing.enter` on the detail, `easing.exit` on the list.
- **Reduced motion.** Instant swap.
- Requires Compose Navigation `SharedElement` (available in `androidx.compose.animation:1.7+`). If unavailable in the shipping version, fall back to a simple slide + fade.

### 3.3 Modal → bottom sheet (Sync now)

- **Standard.** Slide-in from bottom + fade-in. `duration.deliberate`. `easing.enter` on entry, `easing.exit` on dismiss.
- **Reduced motion.** Instant appearance and disappearance.
- Backdrop scrim fades in with the sheet.

### 3.4 Chip appearance (e.g. `Diagnose this` on error, `See details →` on success)

- **Standard.** Fade + short slide from below (`fadeIn + slideInVertically { height / 4 }`). `duration.fast`.
- **Reduced motion.** Instant, opaque.

### 3.5 State swap inside the same surface (Home hero)

- **Standard.** `AnimatedContent` with `fadeIn(standard) togetherWith fadeOut(fast)`. `easing.change`.
- **Reduced motion.** Instant swap.

### 3.6 List reordering (rare — only on Activity when a new row lands)

- **Standard.** `animateItemPlacement()` on the LazyColumn. `duration.deliberate`. `easing.enter`.
- **Reduced motion.** No placement animation.

### 3.7 Skeleton loading

- **Standard.** Static neutral blocks. Optional shimmer using `deliberate` cycle if the loading takes longer than 400 ms.
- **Reduced motion.** Static neutral blocks; no shimmer.

## 4. Sync progress motion (real-work animation)

The single place where motion is *the* signal, not decoration.

**Rules.**

- Motion happens only when `coordinatorBusy = true` and a phase transition has been observed.
- If a phase has been observed but the coordinator has not moved for `duration.observed` (see § 2), the indicator holds at the last observed state — **it does not simulate**.
- On Sync now sheet, the coordinator phase updates are the only source of forward motion. A new phase → the pipeline rail's active node shifts one position.
- Success (`realReadbackConfirmed = true`) triggers `SuccessMoment` (see visual system § 12.4) — a one-shot fade + a soft accent-wash on the hero.
- Failure triggers `ErrorStateSection` in place; no shake animation, no flash — the sanitised summary appears with an `easing.enter` fade.

**Pipeline rail active node.**

- Rotates its glyph at `duration.syncRotation` (linear) only when the current phase matches the node's role.
- All other nodes are static.

**Reduced motion.**

- The active glyph is still marked visually (colour + subtle 8dp inner ring) but does not rotate.
- Success moment is a static state (no fade-in).

## 5. Onboarding motion (one-time)

- The watch-to-phone motif line draws in once on first visit to the Onboarding screen (`duration.deliberate` × 2 for the drawing effect).
- On subsequent visits, the motif is static.
- Reduced motion: static from the first visit.

## 6. Charts motion

- **Bar chart.** Bars grow up from the baseline on first display (`duration.standard`, `easing.enter`, per-bar 40 ms stagger). On subsequent renders of the same data, no re-animation.
- **Sparkline.** Drawn once on entry (`duration.standard`). Not re-animated on data updates.
- **Stat tile.** Number cross-fades on data change (`duration.standard`, `easing.change`).
- **Reduced motion.** Bars appear at full height; sparkline appears fully drawn; number swaps instantly.

## 7. Motion budget per screen

To prevent the app from becoming a motion showcase, we cap moving elements per screen:

| Screen | Concurrent motion budget |
|---|---|
| Home (compact) | 1 hero state-change + 1 active-phase pulse. No more. |
| Home (wide) | Same, plus the trailing panel may animate its content on state change (`duration.standard`). |
| Activity | 1 list-item-placement animation on new row + 1 chip animation. |
| Activity detail | Timeline stagger (once) + one shared-element transition from list. |
| Connections | 1 tile state-change on status transition. |
| Sync now sheet | 1 pipeline rail animation + 1 success moment when applicable. |
| Diagnostics | 1 fact-section change animation. |

## 8. What is *not* animated

- Any third-party brand mark (Health Connect logo, Material Symbols icons carrying no state).
- Any monogram tile at rest.
- The theme toggle button.
- The overflow menu icon.
- Any secondary text or eyebrow.
- Idle FAB (moves when tapped; does not idle-pulse).

## 9. TalkBack behaviour

Motion never delays TalkBack announcements. Every state change fires its `stateDescription` update **before** the animation begins. TalkBack users hear the same state at the same time regardless of the reduced-motion setting.

## 10. Testing motion

Two required tests, not implemented in Cycle 4:

- **Reduced-motion regression.** Every screen renders identically to its motion-off counterpart when `LocalHuaweiSyncMotionPolicy provides HuaweiSyncMotionPolicy.Reduced`.
- **No-idle-motion regression.** A snapshot test that captures a screen after 2 seconds of composition with `coordinatorBusy = false` finds pixel-identical content across the interval (no ambient loop).

Both tests are scheduled in `cycle-04-implementation-roadmap.md`.

## 11. What each direction is free to change

- **Direction A — Calm Health Companion.** Uses `duration.standard` heavily, no `syncRotation` on Home (pipeline rail is not on Home for A; only inside Sync now sheet).
- **Direction B — Dynamic Fitness Product.** Uses `duration.fast` for chip / number swaps to feel snappy; success moment is slightly louder (accent-wash + a subtle number scale from 96 % → 100 %). Reduced-motion path is unchanged.
- **Direction C — Connected Health Ecosystem.** Uses `duration.deliberate` for edge draws (Watch → Sync → Health Connect → GymRats). Active-phase pulse is the primary motion; nothing else on Home moves.

Directions may **not** change:

- The reduced-motion contract.
- The no-simulated-progress rule.
- The no-logo-animation rule.
- The motion budget per screen.
