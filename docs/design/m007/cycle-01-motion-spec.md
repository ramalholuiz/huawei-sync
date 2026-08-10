# Cycle 1 — Motion spec (Direction B)

**Author role:** Product Art Director + Compose Engineer handoff.
**Scope:** Dashboard hero rail, Sync-now modal, Pipeline screen.
**Motion policy source:** `dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy` — every entry below MUST specify its reduced-motion behavior.

## 1. State vocabulary

Five product states, mapped from coordinator evidence exactly as in `PLAN.md § 3.2`:

| State | Coordinator source | Motion | Reduced-motion fallback |
|---|---|---|---|
| **waiting** | `IDLE` + `READY_TO_SYNC` | none (static) | none |
| **writing** | `PREFLIGHT`, `WRITE`, `ACCEPTANCE`, `WRITE_IN_PROGRESS` | rail-segment ticker, forward direction | segment renders a filled bar, no motion |
| **verifying** | `ACCEPTED_AWAITING_READBACK`, `VERIFICATION` | rail-segment ticker, reverse direction, half-opacity | segment renders a half-opacity filled bar |
| **confirmed** | `CONFIRMED_IN_HEALTH_CONNECT` | one-shot `Animatable` on the confirmed node (scale 1.0→1.08→1.0, fade-in halo 0→80%→0) over 400 ms, then static | node ring switches to `ok` color, no scale, no halo |
| **error / attention** | `RECONCILIATION_REQUIRED`, `RETRY_REQUIRED`, `ACTION_REQUIRED`, `PERMISSION_REQUIRED`, `UPDATE_REQUIRED`, `UNAVAILABLE`, `FAILED` | static; node ring uses `accent` (red) or `warning` (amber) per severity | identical |

Rules that hold across every motion below:

- No infinite loop is allowed when the coordinator is not busy. All ambient motion terminates when `PipelinePresentation.motion.active == false`.
- No motion may target the GymRats node into a "complete" state before Gate 2. GymRats visual state is bounded by `ProductGymRatsStatus.label`; the rail exposes only `pending` / `active` / `needs_attention` for that node.
- Every motion respects `HuaweiSyncMotion.current.reducedMotion` and has an explicit branch (existing pattern in `PipelineStepGlyph`).
- Motion does not block input. `AnimatedContent` transitions never gate interactive controls (buttons, dismiss, permission CTAs).

## 2. Rail geometry

`SyncPipelineRail` composable renders four nodes in the fixed order `HUAWEI → SYNC → HC → GYMRATS`, separated by three segments.

- Node: 40 dp square (Dashboard hero), 32 dp square (Sync-now, Pipeline rail). Border `HuaweiSyncGeometry.borderThin` in the state color.
- Segment: 2 dp height rule between nodes, drawn on `Canvas` for the ticker treatment; falls back to a plain colored rule when reduced motion.
- Vertical variant (Pipeline): same nodes rotated 90°, segments become vertical rules.
- Labels: monogram inside the node (`H`, `S`, `HC`, `G`); text label directly under the node in `technicalTypography.label`.

## 3. Motion tokens

Reuse existing `HuaweiSyncMotionPolicy` tokens whenever possible. No new companion values are added in Cycle 1.

| Purpose | Token | Value (Standard) | Value (Reduced) |
|---|---|---|---|
| State label crossfade (`AnimatedContent`) | `standardMillis` in / `fastMillis` out | 240 in / 160 out | 0 / 0 |
| Rail ticker cycle (writing/verifying) | `syncRotationMillis` interpreted as segment-length duration | 1000 ms | segment renders static |
| Confirmed acknowledgement (`Animatable`) | `deliberateMillis` | 480 ms | 0 ms (static color swap) |
| Node border tone crossfade | `fastMillis` | 160 ms | 0 ms |

No new fields are added to `HuaweiSyncMotionPolicy`. If a future cycle needs a distinct token for verifying (currently reuses `syncRotationMillis`), it is added there with the same reduced-motion invariant.

## 4. Per-state motion detail

### 4.1 Waiting
- Rail is static.
- All node borders in `line` (surface strong). Monograms in `ink`.
- No ticker.
- `AnimatedContent` crossfades any label change (e.g. permission granted → ready) using `standardMillis`.

### 4.2 Writing
- Segment between the two nodes immediately adjacent to the active node runs a forward ticker: a 12 dp bright dot travels along the 2 dp rule from left to right in `syncRotationMillis` (or top to bottom in the vertical rail), then restarts.
- Only one segment tickers at a time (the segment upstream of the active node when writing to HC, downstream when reading back from HC — see 4.3).
- Active node border uses `accentForeground`. Non-active nodes stay in `line`.
- Reduced motion: segment renders a solid 2 dp bar in `accentForeground` for the full length. Node still uses `accentForeground` border.

### 4.3 Verifying
- Ticker direction reverses (right → left / bottom → top). Ticker dot uses `info` color at half opacity to visibly differ from writing.
- Active node border uses `info`.
- Reduced motion: solid 2 dp bar in `info` at half opacity. No motion.

### 4.4 Confirmed
- One-shot `Animatable`:
  - Scale on the confirmed node from 1.0 → 1.08 → 1.0 over 480 ms (easing: `FastOutSlowInEasing`).
  - Halo (soft square outline outside the node border, expands from node bounds outward by 6 dp, opacity 0 → 80% → 0) synchronized with the scale.
- After the one-shot, the confirmed node's border stays in `ok` color, static.
- The rail's segments return to static; no ambient motion.
- Reduced motion: node border switches to `ok`, no scale, no halo. The one-shot is never scheduled.
- **Gate 2 constraint:** the confirmed animation only fires on the HC node. It never fires on the GymRats node in Cycle 1.

### 4.5 Error / attention
- Static. Node border uses `accent` for hard failures (`FAILED`, `UNAVAILABLE`, `PERMISSION_REQUIRED`, `UPDATE_REQUIRED`, `ACTION_REQUIRED`, `RETRY_REQUIRED`) and `warning` for soft flags (`RECONCILIATION_REQUIRED`).
- Any ambient motion running when the transition happens terminates within one `fastMillis` frame.
- Reduced motion: identical (nothing changes).

## 5. Sync-now modal amplification

Central signal in the modal is a rail identical to the Dashboard rail, at 40 dp nodes to occupy the modal's width. Under the rail, a **phase medallion**:

- 72 dp square, border in the state color.
- Contains a single letter for the current step: `P` (Preflight), `W` (Write), `A` (Acceptance), `V` (Verification), `R` (Reconciliation). Falls back to the previous letter (unchanged) when awaiting evidence.
- Letter transitions use `AnimatedContent` with `standardMillis` in / `fastMillis` out.
- Reduced motion: no crossfade, direct swap.

Existing `sync-modal-status`, `sync-modal-explanation`, `sync-modal-phase-checks`, `sync-check-*` test tags stay. The old rotating square (`PhaseSignal`) is removed; any semantics on it move to the medallion. The old three-endpoint strip (`Endpoint("G1", …)` etc.) is removed; its content is now the rail.

## 6. Pipeline screen refinement

The `SyncPipelineRail` is **not** repeated on `PipelineScreen`. That screen already lists five per-step rows that read as the pipeline; adding the rail would duplicate the same object in the same viewport and (measured with Robolectric at `w411dp-h891dp`) pushes the Health Connect endpoint card below the fold, breaking one existing regression test. Direction B still applies here through:

- Per-step glyph shape encoding (drawn on `Canvas`, backing the existing icon):
  - `COMPLETE` — filled square.
  - `PENDING` — faint hollow square.
  - `ACTIVE` — half-filled square (left half filled).
  - `NEEDS_ATTENTION` — hollow square with a diagonal cross.
- Phase counter microcopy: `PhaseCard`'s header changes from `COORDINATOR PHASE EVIDENCE` to `COORDINATOR PHASE i OF 5` when a phase is known, falling back to `COORDINATOR PHASE EVIDENCE` while awaiting evidence.
- Existing `pipeline-step-*` and `pipeline-status` test tags stay. The existing `EvidenceTransition` on the glyph is preserved.
- The reduced-motion branch already exists in `PipelineStepGlyph` — no change.

## 7. Non-motion decisions bundled with this spec

Bundled because the Compose engineer will hit them the moment the spec is applied:

- `ModernistProgress` label changes from `"${percent}% · ${LABEL}"` to `"PHASE ${index} OF ${count} · ${LABEL}"`. The visual bar keeps the current fraction fill (deterministic phase progress), but the label stops asserting a percentage.
- The Dashboard title bar keeps `headlineLarge` (post-M006-S10a).
- One accent per surface: the active rail-node color owns the accent. Buttons remain neutral except the accent action button when explicitly required (permission CTA), matching the existing `StraightEdgeButton(accent = true)` pattern.
- The bottom-nav (`ModernistBottomNavigation`) is untouched in Cycle 1.
