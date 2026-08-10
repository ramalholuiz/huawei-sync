# Cycle 2 — Visual research (History & Activity Detail)

**Cycle scope:** History screen, Activity Detail screen, and the transition between them.
**Author role:** Visual Researcher.
**Purpose:** collect the smallest useful set of current references, then hand them to the Product Art Director step. Every reference below is **visual reference only** unless the classification column says otherwise — nothing enters the APK by default.

Access date for every row below: 2026-07-17.

## 1. What Cycle 2 needs

Cycle 2 works on two surfaces the user actually returns to over time. The job is not "more design" — it is:

- Let the user find a specific past workout fast, without scanning every row.
- Show the *real* state of each workout: prepared, in flight, verified in Health Connect, or awaiting an action.
- Explain what happened in a workout's life without exposing SHA-256 identity strings in the first fold.
- Extend the Huawei → Huawei Sync → Health Connect → GymRats vocabulary from Cycle 1's rail into the detail view — one workout at a time.
- Never claim delivery to GymRats. Gate 2 has not passed. `ProductGymRatsStatus.READY_TO_READ.label` remains the only allowable phrasing.

The reference hunt therefore focuses on:

- Activity lists that group by day and let the user skim by state.
- Timeline / audit-log detail views that read as a story, not a table.
- Empty and error states that are honest about the absence of data.
- List-to-detail transitions that carry an identity forward without motion theater.

Explicit exclusions: social feeds, marketing pages, gamified fitness dashboards, chat UI, generic Material 3 tutorial pages.

## 2. Reference table

Classifications:

- **REF** — visual reference only, does not enter the APK.
- **ASSET** — candidate for the APK, requires a row in the asset manifest before shipping.
- **BLOCKED** — must not be copied, imitated 1:1, or shipped.

### 2.1 Activity lists grouped by day

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| H1 | Strava | https://support.strava.com/hc/en-us/articles/216917947-Activity-Feed-and-Following | Day-bucketed activity feed with per-item state and one-line summary; teaches the "sticky day header + calm row" reading rhythm. | REF |
| H2 | Apple Health | https://support.apple.com/en-us/HT203037 | Reference for how a health app groups records by day without inventing a summary the source does not provide. | REF |
| H3 | GitHub commit history | https://docs.github.com/en/repositories/viewing-activity-and-data-for-your-repository/viewing-your-repositorys-commit-history | Time-ordered ledger with per-row status glyph; useful precedent for surfacing identity when needed but not by default. | REF |
| H4 | Stripe dashboard payments list | https://docs.stripe.com/dashboard | State-first list (succeeded / requires action / failed) with a one-tap "needs attention" filter — the closest peer to what our ledger surfaces. | REF |

### 2.2 Timeline / audit-log detail views

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| T1 | Stripe event timeline | https://docs.stripe.com/webhooks/dashboard | Per-object timeline where every step has a timestamp and a plain-language line — model for the Activity Detail timeline. | REF |
| T2 | Vercel deployment detail | https://vercel.com/docs/deployments/managing-deployments | "Ready · Building · Error" per-step progression with progressive-disclosure metadata (technical IDs revealed on tap). | REF |
| T3 | GitHub Actions job run | https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows/using-workflow-run-logs | Collapsed step list expanded into evidence; teaches how to keep technical detail out of first fold. | REF |
| T4 | Linear issue activity | https://linear.app/docs | Timeline of state transitions in a single object where each step is annotated by what caused it — matches our attempt-count semantics. | REF |

### 2.3 Empty & error states

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| E1 | Slack empty state library | https://slack.design/articles/empty-states-in-slack/ | Framework for empty states that describe what will fill them without simulating fake content. | REF |
| E2 | Nielsen Norman Group | https://www.nngroup.com/articles/error-message-guidelines/ | Rules for error copy that stay actionable without blame; used to shape retryable-error copy. | REF |
| E3 | Material 3 empty state | https://m3.material.io/components/dialogs/guidelines | Reduced-attention empty state for utility surfaces — argues for vertically centered, one-line-plus-CTA layout (already what our empty state does). | REF |

### 2.4 List-to-detail transitions & micro-interaction

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| L1 | Android Compose shared elements | https://developer.android.com/develop/ui/compose/animation/shared-elements | Native API baseline. Confirms Cycle 2 does not need Lottie/Rive to carry identity across the transition. | REF |
| L2 | Material 3 motion | https://m3.material.io/styles/motion/transitions/transition-patterns | Reference for list-to-detail patterns and their reduced-motion fallbacks. | REF |
| L3 | Apple Human Interface Guidelines — Motion | https://developer.apple.com/design/human-interface-guidelines/motion | Cross-platform sanity check on "motion should communicate causality" — informs the timeline expansion. | REF |
| L4 | WCAG 2.2 animation-from-interactions | https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html | Reduced-motion accessibility requirements — reinforces `HuaweiSyncMotionPolicy.Reduced` for all new motion. | REF |

**No third-party fitness brand assets, no Huawei brand marks, no GymRats logo, no Strava logo are referenced for inclusion.** They would move to BLOCKED if proposed.

## 3. What the research settles

- **Day grouping is the right first-order affordance.** Every peer that lets a user scan an activity list quickly (Strava, Apple Health, GitHub, Stripe) groups by day. A sticky day header + calm rows is the smallest thing that makes a longer list navigable.
- **State beats metric.** For a ledger where we intentionally do not surface metrics (heart rate, calories, distance) until Gate 1/2 pass, the row's primary information is the readback state, not any body-facing metric. This matches Stripe's "state-first" list.
- **Timelines beat fact tables.** Vercel, Stripe and GitHub Actions all show per-step timelines instead of raw property tables. That is a better model for Activity Detail than the current fact-block layout.
- **Progressive disclosure of identity.** Long hashes (our `clientRecordId`) belong behind a tap, not in the first fold. GitHub's "copy commit SHA" and Stripe's "copy event ID" both hide identity by default and surface it on demand.
- **One meaningful filter, not many.** Stripe teaches that a single "needs attention" filter is worth its keep. More than one filter multiplies choices and hurts the reading path — Cycle 2 will resist the temptation to add more.
- **Empty state is a promise, not a placeholder.** The Slack and Material 3 guides both frame empty states as *"here is what will fill this once the ledger records a fact"* — the current copy already does this and should not regress.
- **No motion for its own sake.** Every reference argues motion must communicate causality. Cycle 2 will move identity across the transition and animate the timeline into view, and stop.

## 4. What Cycle 2 will *not* do based on the research

- No **weekly / monthly aggregates**. We do not have the workout metrics to aggregate.
- No **chart or sparkline**. Same reason.
- No **map view** (some fitness apps do this; we do not have GPS data).
- No **social affordances**. Not our audience, not our product.
- No **multi-select or bulk action**. The ledger row is not a mutable object from the UI's perspective; deletion and reconciliation are coordinator concerns.
- No **third-party sync services logos** in the row or the detail. Text labels only, matching the Cycle 1 rail.

## 5. Notes for the Art Director step

- Reuse the Cycle 1 rail vocabulary (`SyncPipelineRail`, `SyncRailState`, `SyncRailFlow`) so the four-endpoint story is the same object on Detail as on Dashboard, Sync-now, and (implicit) Pipeline.
- Keep the existing `history-item-<id>`, `history-empty`, `history-error`, `history-loading`, `history-content`, `activity-detail-back`, `activity-detail-missing`, `activity-detail-<id>` test tags. Every regression test in `HistorySelectionTest`, `HistoryStateMapperTest`, `EyebrowBudgetTest`, `HonestyCopyGuardTest`, and `AccessibilityResponsiveRegressionTest` still passes.
- Do not add new eyebrows to satisfy hierarchy. `EyebrowBudgetTest` caps History at 2 and Detail at 1. Hierarchy must come from typography, weight, and spacing.
- Any new motion must have an explicit `if (motion.reducedMotion)` branch and must be re-runnable — no ambient loops when the coordinator is idle.
