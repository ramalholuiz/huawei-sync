# Cycle 2 — Motion spec (Direction A)

**Author role:** Product Art Director + Compose Engineer handoff.
**Scope:** History screen, Activity Detail screen, and the transition between them.
**Motion policy source:** `dev.lui.huaweisync.ui.components.HuaweiSyncMotionPolicy`. Every entry below MUST specify its reduced-motion behavior.

## 1. Vocabulary

Cycle 2 introduces three motion families. They are additive to Cycle 1's rail motion vocabulary; they do not modify any existing motion.

| Family | Purpose | Where | Reduced-motion fallback |
|---|---|---|---|
| **shared identity** | Carry the workout's identity across the list-to-detail boundary | History row → Detail identity band | Disabled — instant swap; both surfaces render their static layout |
| **timeline entrance** | Communicate temporal ordering when the detail first mounts | Detail's `ActivityLifecycleTimeline` | All items render at once, no fade, no stagger |
| **progressive reveal** | Expand technical detail without a page jump | Detail's "Show technical detail" toggle | `animateContentSize` disabled — content appears or disappears instantly |

Cross-cutting rules that hold across all three:

- No infinite loop is allowed. All Cycle 2 motion is one-shot, fires exactly once per state entry, and terminates.
- No motion communicates state the ledger has not already recorded. In particular, the timeline items animate their *entrance*, not their *arrival* — the arrival was persisted at write time.
- No motion runs on top of an ongoing sync. Cycle 2 motion is entirely about *reading* past facts, not orchestrating live ones.
- Every family respects `HuaweiSyncMotion.current.reducedMotion` with an explicit `if (motion.reducedMotion)` branch.
- Motion does not block input. The technical-detail toggle is tappable during its expansion; back navigation is honored the whole time.

## 2. Motion tokens

Reuse `HuaweiSyncMotionPolicy` tokens as they are. No new companion fields are added in Cycle 2.

| Purpose | Token | Value (Standard) | Value (Reduced) |
|---|---|---|---|
| Shared-element enter/exit | `standardMillis` | 240 ms | 0 ms (disabled) |
| Timeline item entrance | `standardMillis` for total window; per-item offset `fastMillis / 2` | 240 ms window, ~80 ms stagger between items | 0 ms — all items composed static |
| Technical detail `animateContentSize` | `standardMillis` | 240 ms | 0 ms (instant) |

## 3. Per-family motion detail

### 3.1 Shared identity — list-to-detail

- The shared element is the **leading 4 dp state color rule** on the History row and the **compact readback pill** on the Detail identity band.
- Compose API: `SharedTransitionLayout` at the `HuaweiSyncNavigationShell` boundary, with `Modifier.sharedElement(key = "activity-identity-${clientRecordId}")` on both the row rule + pill and the Detail pill. Distinct keys per workout so a wrong-key drop-in never blends unrelated activities.
- Enter transition: `fadeIn(tween(policy.standardMillis))`; exit: `fadeOut(tween(policy.fastMillis))`. No scale, no translation beyond the natural bounds change.
- Only the state color and pill participate in the shared element. The bulk of the Detail surface (rail, timeline, disclaimer, technical toggle) enters through Compose's default composition, not through the shared element.
- **Reduced motion:** `SharedTransitionLayout` is not composed. Both surfaces render their static layout; back navigation still works. The row rule and the Detail pill still visually match (same color, same shape) — the only difference is the identity does not *travel*, it just *is*.

### 3.2 Timeline entrance

- On the first composition of `ActivityLifecycleTimeline`, each item mounts with `AnimatedVisibility(visible = true)` wrapped around a `LaunchedEffect` that flips `visible` from `false` to `true` after `index * (policy.fastMillis / 2)` ms.
- Item's own transition: `fadeIn(tween(policy.standardMillis)) + slideInVertically(tween(policy.standardMillis)) { it / 4 }`. The slide is small (~1/4 of item height) so no item ever appears from off-screen.
- Total window: `standardMillis + (n_items - 1) * (fastMillis / 2)`. With 4-5 items and current tokens that is < 500 ms.
- The stagger is fired once per Detail entry — leaving Detail and coming back re-fires it (which is desired: it re-communicates temporal ordering after a real navigation).
- **Reduced motion:** `AnimatedVisibility` is not used; the timeline items compose static in their final position. No stagger. TalkBack reads the timeline in order regardless.

### 3.3 Progressive reveal

- The "Show technical detail" toggle expands a hidden `Column` under it. The container carries `Modifier.animateContentSize(animationSpec = tween(policy.standardMillis))`.
- No cross-fade — the content is either present or absent; the container's height animates.
- The toggle label switches between `SHOW TECHNICAL DETAIL` and `HIDE TECHNICAL DETAIL` via `AnimatedContent` with `standardMillis` in / `fastMillis` out; reduced motion → direct swap.
- **Reduced motion:** `Modifier.animateContentSize` is not applied. Content appears or disappears instantly, layout jumps in one frame.

## 4. Rail on Detail — motion contract

The vertical `SyncPipelineRail` on Detail is *entirely static*. Concretely:

- `activeFlow = SyncRailFlow.NONE`. No ticker segment ever animates on Detail.
- `activeNodeIndex = null`. No node is highlighted as "in progress" on Detail.
- `SyncRailNode.confirmedAcknowledgement = false` on every node. The one-shot confirmation halo does **not** fire on Detail, even for a `VERIFIED` workout — the acknowledgement communicated a live moment on Dashboard/Sync-now/Pipeline, and re-firing it here would misrepresent a past fact as a present event.
- All state (`CONFIRMED`, `ATTENTION`, `PENDING`, `WAITING`) is communicated by border color and label only, exactly as in the reduced-motion Cycle 1 path.

This keeps the Detail view faithful to the ledger and avoids reinventing live motion in a place that reads history.

## 5. Accessibility

- Every motion above has an explicit reduced-motion branch (Section 3).
- TalkBack reading order is the same in both branches — motion never reorders content.
- Focus does not shift because of the shared element or the timeline stagger. Focus lands on the identity band on Detail entry so a screen reader user can start with "which workout" before hearing the timeline.
- No color-only encoding is introduced. Every state has label text next to its color (rail label, timeline row copy, state pill).
- The disclaimer "THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP." is always rendered — never behind a toggle, never below the fold on a compact viewport.

## 6. Non-motion decisions bundled with this spec

Bundled here because the engineer will hit them the moment the spec is applied:

- **Day header string.** Compute `LocalDate` from `updatedAtEpochMillis` in `ZoneId.systemDefault()`. Label priorities: `TODAY`, `YESTERDAY`, `<UPPERCASE 3-LETTER WEEKDAY> <DD MON>` (e.g. `MON 14 JUL`), fallback `YYYY-MM-DD`. All-caps for consistency with the `TechnicalMicrocopy` register.
- **Row time.** Local `HH:mm` from `updatedAtEpochMillis`. UTC full timestamp remains available in Detail.
- **Attempts chip.** Rendered only when `attemptCount > 1`. Format: `×N` in the `technicalTypography.label` register. Not a pill — just a compact rule-less inline element to avoid competing with the state pill.
- **Filter chip label.** `NEEDS ATTENTION` (uppercase, `technicalTypography.label`). Disabled state uses `ink3`; enabled uses `accentForeground` border and `accentSoft` background.
- **`EvidenceTransition` reuse.** The pipeline module already exposes `EvidenceTransition` for status-label crossfades. Use it on the Detail identity band's state pill so a coordinator-driven state change (which will only happen if the user re-enters Detail after a new sync) crossfades gracefully. Skip it elsewhere.
- **One accent per surface.** History: the `NEEDS ATTENTION` chip (when active) owns the accent. Detail: the rail's per-node border tone (state-derived) owns the accent; the technical-detail toggle is neutral.
