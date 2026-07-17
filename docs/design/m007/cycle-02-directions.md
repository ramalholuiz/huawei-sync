# Cycle 2 — Directions (History & Activity Detail)

**Author role:** Product Art Director (Impeccable).
**Vocabulary:** critique · distill · clarify · layout · typeset · animate · polish.
**Inputs:** `cycle-02-research.md`, current Compose sources (`HistoryScreen.kt`, `ActivityDetailScreen.kt`, `HistoryState.kt`, `SyncPipelineRail.kt`, `PipelinePresentation.kt`, `SyncNowModal.kt`, `HuaweiSyncMotionPolicy`).
**Guardrails:** preserve every runtime contract from `docs/milestones/M007/PLAN.md § 3.3`. `EyebrowBudgetTest` caps History at 2 eyebrows and Detail at 1. `HonestyCopyGuardTest` forbids any claim of delivery to GymRats. `HistorySelectionTest` requires `history-empty`, `history-error`, `history-content`, and `history-item-<id>` tags, and the empty surface must stay near the vertical center. `AccessibilityResponsiveRegressionTest` requires `HistoryContentPreview` and `VerifiedDetailPreview` to keep the `@HuaweiSyncScreenshotPreviews` matrix annotation.

## 1. Critique of the current state

Before proposing directions, distill what the code already does and where it hurts.

### 1.1 Kept

- **Read-only ledger framing.** `HistoryState.Empty / Loading / RetryableError / Content` is the right domain shape. No inference, no fabricated rows.
- **Deterministic identity as primary key of a row.** `history-item-<clientRecordId>` and `HistoryStateMapper.select` mean selection is reinstall-stable and unambiguous.
- **State projection.** `ActivityReadbackState` (PREPARED / RECONCILIATION_REQUIRED / PENDING_READBACK / VERIFIED / RETRYABLE_ERROR / ACTION_REQUIRED) is exhaustive and honest.
- **Empty state discipline.** The empty state describes what will fill it, not a fake row. Vertical centering rules and the "no ledger activity" copy are already right.
- **Honest disclaimer in Detail.** "This view reports ledger and readback facts only. It does not claim delivery to another app." is the strongest single sentence in the current UI. Keep it.

### 1.2 Hurts

- **The list has no scan rhythm.** Every row looks the same — same border, same block of technical text, same `ATTEMPTS N · RECORD VERSION N`. A user cannot triage "which need attention" without reading each row end to end.
- **No temporal grouping.** Rows are chronological but not bucketed. When the ledger holds ten to twenty entries the eye has to reconstruct dates from the `yyyy-MM-dd HH:mm 'UTC'` line on every row. This will get worse over time.
- **Identity is louder than state.** The `clientRecordId` (`huawei-sync:v1:… :67b91d2d4a1f`) sits above the fold in every row. That is technical noise for the primary reading, and it dominates the state pill by weight.
- **Detail is a fact table, not a story.** `DETERMINISTIC IDENTITY / WRITE ATTEMPTS / READBACK VERIFICATION` are three sibling blocks. They read like `printf("%-30s: %s\n")`, which is honest but does not explain what happened in this workout's life.
- **The Huawei → Sync → HC → GymRats story is missing on Detail.** The Cycle 1 rail vocabulary owns the sync flow on Dashboard, Sync-now, and Pipeline. Detail should extend that vocabulary to *one* workout's story, not invent a parallel language.
- **No triage affordance.** With five or six rows the user might scroll; with twenty they will search. There is no filter, no chip, no jump-to affordance for "which need my attention".
- **No cross-screen identity carry.** The list-to-detail transition is a hard cut; the identity (monogram, state color) does not travel with the tap. That is a missed causality signal, and shared-element transitions are cheap on native Compose.
- **Detail back-nav microcopy is `DETERMINISTIC LEDGER DETAIL`.** That is *what the view is*, not *what workout you are looking at*. The user has no anchor to which activity they are inspecting.

## 2. Three directions

### Direction A — Timeline minimalista

Keep the row-per-workout list; upgrade its scan rhythm and make the detail a lifecycle timeline.

- **History.** Sticky day header ("TODAY", "YESTERDAY", "MON 14 JUL"). Row shrinks to: state color rule on the left edge (4 dp), source-provider label, readback state text, right-aligned time (`HH:mm`). Identity string is hidden by default and revealed via a "Show identity" affordance in Detail only. `ATTEMPTS` chip stays inline but only when `> 1`. Adds one filter chip `NEEDS ATTENTION` at the top of the content that filters to `RECONCILIATION_REQUIRED / RETRYABLE_ERROR / ACTION_REQUIRED`.
- **Activity Detail.** Header becomes a compact identity band ("Synthetic workout · Prepared 09:03 UTC"). Below it, a **vertical `SyncPipelineRail` (Huawei → Sync → HC → GymRats)** keyed off *this workout's* ledger facts (not the current live coordinator state). Below the rail, a **lifecycle timeline** with one row per persisted state transition (Prepared → Written → Verified / or Prepared → Retryable error), each with a timestamp. Technical identity (`clientRecordId`, `clientRecordVersion`, `safeErrorCode`, `sourceProvider`) sits behind a "Show technical detail" toggle at the bottom of the screen, collapsed by default.
- **Motion.** List-to-detail uses a Compose shared-element transition on the state color rule and the state pill. Timeline items enter with a per-item stagger via `AnimatedVisibility` on the first composition (280 ms window total). Reduced motion: no stagger, no shared element — instant swap, identical layout.

Pros: highest clarity per glance; hierarchy problem solved by removing noise, not by adding chrome; the vertical rail extends the Cycle 1 vocabulary; timeline reads like Vercel/Stripe (best-in-class peers for our domain); no new eyebrows required.
Cons: shared-element wiring across a `HuaweiSyncNavigationState`-driven `when` is a new pattern for this codebase — has to be evaluated for correctness under back-nav.

### Direction B — Activity ledger premium

Preserve the "read the whole ledger row" grid but upgrade its density, and turn the detail into a definitive audit report.

- **History.** Two-column dense grid on wide viewports (≥ 720 dp) with each cell showing state, source, time, attempts; single-column on compact. Sticky segmented header "ALL · ATTENTION · VERIFIED" driving the filter. Rows keep the identity fragment but demoted to a bottom microcopy row, not headline weight.
- **Activity Detail.** Grid of fact blocks reordered: `WORKOUT (source, prepared time) / SYNC STATUS (rail + timeline) / VERIFICATION (readback state, timestamps) / ATTEMPTS (attempt count, last error code)`. Rail stays but sits *inside* the sync-status block. Adds a copy-to-clipboard affordance on the identity block for support flows.
- **Motion.** `AnimatedContent` on filter chip changes with `standardMillis` in / `fastMillis` out. Timeline entries fade in. No shared element.

Pros: extends the current fact-table language rather than replacing it; premium feel; solid on wide viewports; three-way segmented filter is more visible than a single chip.
Cons: more chrome on already dense rows; two-column grid works for our test viewport (`411dp`) only as a single column, so most users see the same rows they see today; three-way segmented control adds a UI element with real cognitive cost when the ledger is small; does not solve the "identity dominates state" critique in the row.

### Direction C — Motion-first history

Push the motion dial. Everything is a transition; identity carries across screens with a hero move; timeline draws itself.

- **History.** Day-bucketed rows with a Canvas-drawn per-row micro-state glyph (a 1-frame draw that matches the rail node's shape). Sticky day header animates in on scroll. Filter chip becomes a segmented pill that morphs.
- **Activity Detail.** Rail draws itself in with a segment-by-segment reveal (280 ms segment, 200 ms overlap). Timeline items expand from a single line to their full detail with `animateContentSize`. Shared-element on the state color rule AND the readback state text.
- **Motion.** Multiple `Animatable` timelines. Requires an explicit reduced-motion fallback on every stage.

Pros: strongest personality; best "wow" moment; fully within native Compose APIs.
Cons: highest reduced-motion burden — every stage needs a matching static path; the motion catalog is larger than the visual improvement it buys on History/Detail specifically; the rail is *already* a Cycle 1 asset and does not need a new draw-in treatment; risk of drifting from "motion communicates causality" into "motion for its own sake"; Cycle 1 spent its motion budget on the sync flow — a bigger motion catalog on Detail risks making the app feel imbalanced (Detail more animated than Dashboard).

## 3. Comparison

| Criterion | A | B | C |
|---|---|---|---|
| Clarity (find and triage a workout) | 5 | 4 | 4 |
| Simplicity (one mental model with Cycle 1) | 5 | 3 | 3 |
| Density (fits ledger without waste) | 4 | 3 | 3 |
| Accessibility (reduced-motion, TalkBack, contrast) | 5 | 4 | 3 |
| Honesty (no invented metrics, no delivery claim) | 5 | 5 | 5 |
| Technical effort in one cycle | 4 | 4 | 2 |
| Consistency with Cycle 1 (rail vocabulary, one accent) | 5 | 4 | 3 |

Scores are relative within Cycle 2, not absolute. Higher is better.

## 4. Chosen direction — **A (Timeline minimalista)**

Rationale:

1. **Clarity by removal, not by addition.** The primary History problem is noise — same weight per row, identity above state. A shrinks each row until state is loudest, which is what the ledger actually holds. B and C solve less of that.
2. **One mental model.** The vertical rail on Detail reuses the same `SyncPipelineRail` / `SyncRailState` / `SyncRailFlow` we shipped in Cycle 1. A user learns the rail once. Reusing it here is the biggest simplicity multiplier available.
3. **Timeline earns its place.** Stripe, Vercel, Linear all prove that per-object timelines beat fact tables for state-driven objects. Our Activity is a state-driven object. This is the strongest single change.
4. **Progressive identity.** Identity is not deleted (deterministic identity is a project invariant); it moves behind a tap. That resolves the "noise > state" critique while keeping the identity visible whenever support/reconciliation actually needs it.
5. **Motion earns its place too.** Shared-element on the state color rule and pill *communicates* the identity being carried across the boundary. Timeline stagger *communicates* the temporal ordering. Nothing loops. Both are trivially disable-able via `HuaweiSyncMotionPolicy`.
6. **Consistent with Cycle 1 restraint.** One accent per surface, no new eyebrows, no new dependencies, no Canvas invention beyond what the rail already draws.

C is a natural target for a future motion polish cycle if we ever feel Detail should draw itself in. B is a strictly heavier version of A that trades scan speed for chrome, which is the wrong trade for this ledger's size.

## 5. What Direction A ships in Cycle 2

Concrete change list handed to the Compose Engineer:

### 5.1 History

- **Day grouping.** Group `ActivityHistoryItem.updatedAtEpochMillis` in the device's local zone into buckets: `TODAY`, `YESTERDAY`, `<WEEKDAY DD MON>` (fallback: `YYYY-MM-DD`). Sticky day header uses `technicalTypography.microcopy`. Grouping is a pure function of the input list; it does not fabricate any state.
- **Row layout (single column, compact & wide).**
  - 4 dp state color rule on the leading edge.
  - `sourceProvider.uppercase()` in `technicalTypography.microcopy`, `ink2`.
  - Readback state text in `technicalTypography.value`, `ink`, bold.
  - Right column: `HH:mm` local time + attempts-chip `×N` only when `attemptCount > 1`.
  - Identity fragment removed from the row. Full identity remains available on Detail (behind a toggle).
- **Filter chip.** Single `NEEDS ATTENTION` chip at the top of the content, sticky under the section header. When pressed, filters to `RECONCILIATION_REQUIRED / RETRYABLE_ERROR / ACTION_REQUIRED`. When no attention-requiring rows exist, the chip renders in a "no attention needed" muted state and is disabled — no fake affordance.
- **Section header.** Keeps the existing `SectionHeader(eyebrow = "READ-ONLY LEDGER", title = "Activity history")`. No new eyebrow.
- **Counter microcopy.** Keep `${size} DURABLE RECORD(S)` line — moved directly under the section header so it always renders above the filter chip. When a filter is active, appends `· FILTERED TO ATTENTION`.
- **Empty / Loading / Error.** Unchanged structurally: `history-empty`, `history-loading`, `history-error` tags stay, and the empty surface stays vertically centered so `HistorySelectionTest > empty history surface renders near the vertical center of the viewport` still passes.

### 5.2 Activity Detail

- **Identity band header.** Replaces `DETERMINISTIC LEDGER DETAIL` microcopy with a header that reads as "which workout am I looking at": `<SOURCE PROVIDER>` microcopy + `Activity record` title (unchanged text, so the existing single-eyebrow budget stays intact) + a compact readback pill on the trailing edge for at-a-glance state.
- **Vertical rail.** Insert a vertical `SyncPipelineRail` sized to Detail's column, projected from the *ledger row's* facts (not `ProductSyncState`), so the story is per-workout. Rail nodes: `HUAWEI` (source), `SYNC` (this app's ledger), `HC` (readback), `GYMRATS` (available to import). Mapping:
  - `HUAWEI` — always `PENDING` (source of truth is not the phone).
  - `SYNC` — `CONFIRMED` when the ledger has recorded acceptance (`acceptedAtEpochMillis != null` OR `readbackState in {VERIFIED, PENDING_READBACK}`), otherwise `PENDING`, `ATTENTION` on error states.
  - `HC` — `CONFIRMED` when `readbackState == VERIFIED`, `ACTIVE_VERIFY` when `PENDING_READBACK`, `ATTENTION` when `RECONCILIATION_REQUIRED / RETRYABLE_ERROR / ACTION_REQUIRED`, `WAITING` for `PREPARED`. Fires the confirmed one-shot only when `VERIFIED` is a fresh entry — passing `confirmedAcknowledgement = false` in Detail because the confirmation happened in the past, not right now.
  - `GYMRATS` — always `PENDING`. Never `CONFIRMED`. Supporting text stays `AWAITING G2`.
  - Since the workout is not live-syncing while the user reads Detail, `activeFlow = NONE` and `activeNodeIndex = null` in all cases. The rail is entirely static; motion never loops.
- **Lifecycle timeline.** New composable `ActivityLifecycleTimeline` renders one entry per persisted transition, in the order the ledger recorded them:
  1. `Prepared` — always present; timestamp = the earliest known ledger touch (`updatedAtEpochMillis` when `acceptedAtEpochMillis == null`; otherwise still shown as "Prepared" but derived from the fact that a durable row exists).
  2. `Written to Health Connect` — shown when `acceptedAtEpochMillis != null`. Copy: "Health Connect accepted the write".
  3. `Readback verified` — shown when `confirmedAtEpochMillis != null`. Copy: "Health Connect readback matched the deterministic identity".
  4. `Retry required` / `Reconciliation required` / `Action required` — shown when the current state is one of the attention states, with the `safeErrorCode` (if any) as a technical microcopy line.
  5. `Available for GymRats to import` — always shown at the tail (no timestamp), never marked complete, matching the honest disclaimer.
- **Technical detail (progressive).** Below the timeline, a "Show technical detail" toggle expands: `clientRecordId`, `clientRecordVersion`, `attemptCount`, `sourceProvider`, `safeErrorCode`. `animateContentSize` gives the reveal a graceful expansion under `standardMillis`; reduced motion swaps instantly.
- **Honest disclaimer.** Keep verbatim: "THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP." Move it just below the timeline (before the technical toggle) so it is visible before the user considers copying identity for support.

### 5.3 Transition

- **Shared identity.** Wrap the leading state color rule on the History row and the identity-band pill on Detail in `SharedTransitionLayout` + `sharedElement`, keyed on `clientRecordId`. Compose 1.8+ APIs; no new dependency.
- **Reduced-motion path.** When `HuaweiSyncMotion.current.reducedMotion` is true, disable the shared-element wrapper and render both surfaces without transition — identical visual layout, instant swap.

## 6. What Cycle 2 does NOT ship

- No new record types.
- No metric (heart rate, duration, calories, distance) invented for the row or the detail.
- No new dependencies. `androidx.compose.animation` shared-element APIs are already available.
- No claim of GymRats delivery. The label stays `Available for GymRats to import` and the GymRats node in the rail is never `CONFIRMED`.
- No changes to Room, ledger, `Gate1SyncCoordinator`, deterministic identity, attempt count, deduplication, reconciliation, or claims policy.
- No changes to Dashboard, Sync-now, Pipeline, Integrations, Diagnostics, Automation, or Assistant. Navigation state is unchanged.
