# Cycle 1 — Directions

**Author role:** Product Art Director (Impeccable).
**Vocabulary:** critique · distill · clarify · layout · typeset · animate · polish · bolder · quieter.
**Inputs:** `cycle-01-research.md`, current Compose sources (Dashboard, SyncNowModal, PipelineScreen, ModernistComponents, HuaweiSyncMotionPolicy, HuaweiSyncTheme).
**Guardrails:** preserve every runtime contract and semantic listed in `docs/milestones/M007/PLAN.md § 3.3`.

## 1. Critique of the current state

Before proposing directions, distill what the code already does well and where it hurts.

### 1.1 Kept
- Flat geometry (`HuaweiSyncGeometry.cornerRadius = 0.dp`) is a genuine personality choice — do not soften into generic rounded Material.
- Two-typeface system (`ProductSans` + `TechnicalMono`) already gives us a hierarchy tool.
- `PipelinePresentation` is the right domain model — clean separation of state from view.
- `HuaweiSyncMotionPolicy.Reduced` branch is exemplary; every motion component checks it.
- `TechnicalMicrocopy` gives us a signature "instrument-panel" voice.

### 1.2 Hurts
- **Dashboard is a stack of equal-weight cards.** `SyncHero` competes with `LedgerSummary`, `ConnectedServices`, `VerificationSummary` — no single answer to "what am I supposed to look at now?".
- **Sync-now modal's central signal is a rotating square icon.** It reads as a spinner, not as a state signifier. The endpoint strip below is decorative (`G1` / `HC` / `G` monograms), not causal.
- **Pipeline collapses the source.** "Gate 1 synthetic workout" as an endpoint card is honest but visually thin; there is no shape for Huawei as an inert source, no shape for GymRats as an implicit downstream.
- **Progress bar in `ModernistProgress` shows `xx%`.** Comes from a deterministic phase mapping (`ProductSyncPhase.progressFraction`) — not a real percentage. That contradicts our own `numericProgress == null` invariant on the pipeline model and mildly lies to the user. Cycle 1 should replace the labelled percent with a phase count ("Phase 2 of 5 · WRITE").
- **Ambient rotation in `PhaseSignal` and `PipelineStepGlyph` is a loop that runs while any phase is active.** It communicates "something is happening", but does not distinguish writing from verifying. Two different motions would carry more meaning.
- **StatusLabel color-only encoding.** `READY` and `COMPLETE` share the same green pill. TalkBack distinguishes them via `stateDescription`, but a sighted user cannot without reading the whole line. Direction should add shape or letterform to the pill.
- **Endpoint strip cognitive load.** Three tiny cards, three monograms, three tiny detail texts — that is more chrome than signal for a 412dp-wide modal.

## 2. Three directions

### Direction A — Conservative refinement

Distill and clarify what exists. No new motion vocabulary, no new visual metaphors.

- Dashboard: reorder to a single hero + secondary strip; hide the ledger/verification cards behind a "Details" toggle when the coordinator is idle so the hero owns the viewport.
- Sync-now: replace the rotating square with a static phase medallion; keep the endpoint strip but drop `G1` (rename to `HUAWEI` / `SYNC` / `HC` / `GYMRATS`, 4-card row on wide viewports, 2×2 on compact).
- Pipeline: same list layout, but recolor the step glyph to encode state by shape too — filled square = complete, hollow square = pending, half-filled square (drawn on `Canvas`) = active, X = error.
- Motion: only refactor the phase transition — no ambient loops.

Pros: lowest technical risk; smallest diff; easiest to review.
Cons: Dashboard still reads as a card stack; personality gain is small; barely moves the needle on "less static".

### Direction B — Premium utility

Rebuild the hierarchy around the pipeline metaphor. Everything else supports the pipeline.

- Dashboard promotes the pipeline strip **to the hero position**: a horizontal HUAWEI → SYNC → HC → GYMRATS rail with per-node state, sits under the title bar. Existing SyncHero (permission/action CTA) collapses to a slim action row directly under the rail. Ledger and Verification sit as a two-column strip below. Failure summary lives above the rail when present (unchanged).
- Sync-now modal reuses the same rail — the modal becomes an amplified view of the same object, keyed by the same test tags. Central signal is a large phase medallion showing the current step's letterform (P / W / A / V / R) and its state color.
- Pipeline screen becomes a vertical version of the same rail plus per-step evidence.
- Motion: state transitions via `AnimatedContent`, `updateTransition`. Writing state gets a directional "flow" ticker under the arrow between two nodes; Verifying state gets a distinct "return-arrow" ticker. Confirmed state fires a one-shot `Animatable` scale-and-fade on the destination node. Reduced-motion shows all endpoints static with a state pill.
- Type: bolder Dashboard title (already `headlineLarge`), tighter tracking on state pills, reserve `technicalTypography.value` for numeric metrics only.

Pros: same rail across three surfaces = predictable, memorable, matches the Airbyte / P4 reference; clarifies the Huawei → GymRats flow; motion carries semantic weight.
Cons: touches the most shared component (rail must be extracted into a reusable composable); Dashboard hero is a new invention, needs care to remain calm; extraction has moderate refactor cost.

### Direction C — Expressive motion

Push the personality dial. Motion becomes the identity.

- Dashboard: same hero as B, but the rail is drawn on `Canvas` with hand-tuned strokes; state transitions animate the stroke width.
- Sync-now: central signal is a full-height canvas that draws a phase-specific glyph (e.g. traveling packet during writing, echo pulse during verifying, expanding square during confirmed).
- Pipeline: canvas rendition of the rail rotated vertical; each step draws its own micro-icon.
- Motion: layered `updateTransition`, multiple `Animatable` timelines coordinated per phase; requires careful reduced-motion fallback for every canvas.

Pros: strongest personality; genuine "expressive" identity aligned with Whoop-style boldness.
Cons: highest technical effort, highest risk of drift from the coordinator-truth invariant, hardest reduced-motion path (each canvas needs its own explicit static fallback), longest QA cycle, likely spills over Cycle 1.

## 3. Comparison

| Criterion | A | B | C |
|---|---|---|---|
| Clarity of state (waiting/writing/verifying/confirmed/error) | 3 | 5 | 4 |
| Simplicity (single mental model across surfaces) | 3 | 5 | 3 |
| Personality (Huawei Sync feels distinct) | 2 | 4 | 5 |
| Cognitive load (fewer buttons, one primary action) | 4 | 5 | 3 |
| Accessibility (reduced-motion, TalkBack, contrast) | 5 | 4 | 3 |
| Technical effort in one cycle | 5 | 4 | 2 |
| Risk to preserved contracts | 5 | 4 | 2 |
| Alignment with Huawei Sync mission (honest, calm, correct) | 4 | 5 | 3 |

Scores are relative within Cycle 1, not absolute. Higher is better.

## 4. Chosen direction — **B (Premium utility)**

Rationale:

1. **Distinct states** — the rail is the strongest device for making waiting, writing, verifying, confirmed, and error visually distinguishable, and it enforces the same encoding across all three surfaces we own in Cycle 1.
2. **One mental model** — the same rail on Dashboard, Sync-now, and Pipeline means a user learns the visual language once. That is a bigger simplicity win than any local refinement in A.
3. **Personality without risk** — B upgrades personality (bolder typography, coordinated motion, single accent) without inventing a canvas language we cannot QA in one cycle. C's personality is stronger, but the reduced-motion fallback burden and coordinator-truth risk make it wrong for Cycle 1.
4. **Motion earns its place** — B assigns each motion a semantic job (write-flow, verify-echo, confirmed-arrival). Every motion has an obvious reduced-motion fallback (endpoint pill, state label) already validated by the current codebase.
5. **Alignment with mission** — B keeps the GymRats endpoint labelled by `ProductGymRatsStatus.label` (no "delivered" affordance), the numeric-progress invariant intact, and the Room ledger evidence surfaced under the rail.

C is the natural target for Cycle 2 if Cycle 1 ships. A is a strictly weaker version of B.

## 5. What Direction B ships in Cycle 1

Concrete change list handed to the Compose Engineer:

- **New shared composable** `SyncPipelineRail` in `ui/components/`. Props: ordered list of endpoint models (label, monogram, state, optional support text), a motion policy, an optional highlighted transition (source → destination) for the active phase.
- **Endpoints modelled as four fixed nodes:** `HUAWEI`, `SYNC`, `HC`, `GYMRATS`. State enum reused from `PipelineStepState` semantics: `pending`, `active`, `complete`, `needs_attention`. GymRats stays labelled by `ProductGymRatsStatus.label`; no state may upgrade beyond "available to import" without Gate 2 evidence.
- **Dashboard** — new hero: title bar (unchanged), then `SyncPipelineRail`, then a compact action row (permission CTA or "Sync now") wired through the existing `onShowSyncOverlay` / `onResolveHealthConnect`. Ledger + Connected services + Verification move below in a tighter block; each becomes a single-line row instead of a card unless the coordinator surfaces new information. Failure summary stays above the rail when present.
- **Sync-now modal** — replaces `PhaseSignal` (rotating square) with an amplified `SyncPipelineRail` plus a phase-medallion (letterform of the current step). Endpoint strip is removed (its content is now the rail). Existing `EvidenceTransition` for the status label stays. Phase checks bar stays (existing test tags: `sync-modal-phase-checks`, `sync-check-*`).
- **Pipeline screen** — the vertical layout stays, but each endpoint card and the phase card get a leading pipeline dot that visually matches the rail; per-step glyph gains a shape encoding (filled/hollow/half-filled/X drawn on `Canvas`). Ledger row and failure summary unchanged in structure.
- **Motion vocabulary** (spec detail lives in `cycle-01-motion-spec.md`):
  - waiting: static.
  - writing: single directional ticker between the two arrows immediately around the active node.
  - verifying: reverse-direction echo ticker on the same segment, half-opacity.
  - confirmed: one-shot scale-and-fade on the confirmed node, ≤400ms, then static.
  - error: static, red border on the affected node.
- **Typography moves:** Dashboard title stays `headlineLarge`; state pills get monospace label styling from `technicalTypography.label` for consistency; `ModernistProgress` percent label is replaced with a phase counter ("PHASE 2 OF 5 · WRITE") because the underlying value is deterministic, not measured.
- **Accent discipline:** exactly one `accent` on each surface (the active-node ring). `ok`, `warning`, `info` colors reserved for state pills. No secondary accent additions.

Cycle 1 does not:

- change any runtime package,
- add dependencies,
- introduce new record types,
- claim GymRats delivery,
- animate anything the coordinator has not evidenced.
