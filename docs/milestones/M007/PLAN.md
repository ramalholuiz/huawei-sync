# M007 — Visual and Motion Lab

**Status:** planning (docs-only; the central GSD tracker holds only M001 — no lifecycle row for M007)
**Branch:** `milestone/M007`
**Worktree:** `.gsd-worktrees/M007`
**Base commit:** `719f70d` (last M006 verification commit)
**Preserved invariants:** Room ledger, `Gate1SyncCoordinator`, deterministic identity, deduplication, reconciliation, claims policy, `ExerciseSessionRecord` as the only P0 record type. M007 is a presentation-only milestone; runtime packages (`data/`, `domain/`, `health/`, `diagnostics/`) MUST NOT change.

## 1. Milestone vision

M007 stands up an iterative visual/motion loop for Huawei Sync:

> visual research → brainstorm → art direction → Jetpack Compose implementation → real capture → critique → polish.

Each cycle ends with actual improvements installed on the emulator (`HuaweiSync_API_35`, Android 15, 1080×2400 @ 420dpi). This is not a documentation audit — the deliverable is code and evidence.

Explicit non-goals for the milestone as a whole:

- No new record types, no new integrations, no new metrics, no backend.
- No Rive / Lottie / new motion libraries in Cycle 1.
- No Strava, no reverse-engineered Huawei access.
- No claim of GymRats confirmation before Gate 2 passes.

## 2. Process (per cycle)

Every cycle follows the same four roles executed in sequence in the same session:

1. **Visual Researcher.** Web research on current references (health, fitness, sync/transfer, wearables, dashboards, observability, motion & micro-interactions). Record URL, owner, access date, purpose. Classify every reference as one of: visual reference only / reusable asset with compatible license / prohibited from APK.
2. **Product Art Director (Impeccable).** Vocabulary: critique, distill, clarify, layout, typeset, animate, polish, bolder, quieter. Produces three compact concepts:
   - **A — Conservative refinement**
   - **B — Premium utility**
   - **C — Expressive motion**

   Compares on: clarity · simplicity · personality · cognitive load · accessibility · technical effort · risk · Huawei Sync alignment. Chooses **one** direction with a written justification. Does *not* implement all three.
3. **Jetpack Compose Engineer.** Implements only the chosen direction. Native APIs only: `AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `updateTransition`, `Animatable`, `Canvas`, shared-element transitions if warranted. No new dependencies. Motion must:
   - communicate state or causality,
   - respect the reduced-motion policy in `HuaweiSyncMotionPolicy`,
   - not block interaction,
   - not simulate progress the coordinator has not emitted,
   - not show success before evidence,
   - not loop except during a real operation.
4. **Visual QA.** Runs `./gradlew clean test lint assembleDebug`, installs on `HuaweiSync_API_35`, captures before/after, dark/light, compact + wide viewport, long-content case, touch-target and contrast checks, motion frame sequences. Verifies no functional regression. Writes a critique and a second polish pass.

The Impeccable skill (`/Users/luiz/.claude/skills/impeccable/SKILL.md`) is the methodology reference for role 2. Live-mode / web-detector tools are not treated as Compose validators.

## 3. Cycle 1 — scope

Cycle 1 works **only** on these three surfaces:

1. **Dashboard** — `app/src/main/java/dev/lui/huaweisync/ui/screens/dashboard/DashboardScreen.kt`
2. **Sync now modal** — `app/src/main/java/dev/lui/huaweisync/ui/screens/sync/SyncNowModal.kt`
3. **Pipeline** — `app/src/main/java/dev/lui/huaweisync/ui/screens/pipeline/PipelineScreen.kt` (+ `PipelinePresentation.kt`)

Shared surfaces that Cycle 1 may touch presentationally:

- `app/src/main/java/dev/lui/huaweisync/ui/components/ModernistComponents.kt` (SyncFab, ModernistSurface, ModernistProgress, StatusLabel, StraightEdgeButton, SectionHeader, TechnicalMicrocopy) — refinement only, no signature breakage.
- `app/src/main/java/dev/lui/huaweisync/ui/components/MotionPolicy.kt` — additions only (never remove the reduced-motion branch).
- `app/src/main/java/dev/lui/huaweisync/ui/theme/{Color,Type,Tokens,Theme}.kt` — token/palette refinement is allowed; new tokens must be documented in the motion/spec docs.

Explicitly out of scope for Cycle 1 (do not open until the user approves): History, Activity Detail, Integrations, Diagnostics, Assistant, Automation, Detail, Onboarding.

### 3.1 Cycle 1 objectives

- Improve hierarchy, composition, and visual personality of the three surfaces.
- Reduce Dashboard staticness — motion communicating live coordinator state, not decorative loops.
- Make the sync flow legible and pleasant: represent **Huawei → Huawei Sync → Health Connect → GymRats** as a first-class pipeline model.
- Distinguish five states clearly: **waiting · writing · verifying · confirmed · error**.
- Never claim GymRats confirmation before Gate 2. The GymRats endpoint remains "workout is available for GymRats to import; delivery has not been claimed" until Gate 2 evidence exists.
- Fewer buttons; one primary action per surface.
- Useful motion only.

### 3.2 Coordinator states → visual states mapping (source of truth)

| Coordinator phase / status | Visual state | Motion allowed |
| --- | --- | --- |
| `IDLE` + `READY_TO_SYNC` | **waiting** | none / breathe-once on mount |
| `PREFLIGHT` | **waiting → writing** transition | one directional transition |
| `WRITE` / `ACCEPTANCE` / `WRITE_IN_PROGRESS` | **writing** | active loop bound to `syncRotationMillis` |
| `ACCEPTED_AWAITING_READBACK` / `VERIFICATION` | **verifying** | secondary active loop, distinct from writing |
| `CONFIRMED_IN_HEALTH_CONNECT` | **confirmed** | one-shot acknowledgement, then static |
| `RECONCILIATION_REQUIRED`, `RETRY_REQUIRED`, `ACTION_REQUIRED`, `PERMISSION_REQUIRED`, `UPDATE_REQUIRED`, `UNAVAILABLE`, `FAILED` | **error / attention** | static; no ambient motion |

The GymRats endpoint stays labelled by `ProductGymRatsStatus.label` from the current runtime — no visual affordance may upgrade this to "delivered".

### 3.3 Preserved contracts (do not break)

- `Gate1SyncCoordinator` and every runtime package it depends on.
- `PipelinePresentation.numericProgress == null` invariant — no fake percentages.
- `HuaweiSyncMotionPolicy.Reduced` branch and every `if (motion.reducedMotion)` check.
- `dashboard-sync-fab`, `dashboard-sync-hero`, `dashboard-failure-summary`, `sync-now-overlay`, `sync-modal-status`, `sync-modal-explanation`, `sync-modal-phase-checks`, `sync-check-*`, `pipeline-screen`, `pipeline-phases`, `pipeline-status`, `pipeline-awaiting-evidence`, `pipeline-step-*`, `pipeline-failure-summary`, `bottom-nav-*` test tags — keep or replace with an equivalent tag that existing tests reference.
- Existing `contentDescription` / `stateDescription` / `paneTitle` semantics (a11y regressions are treated as blockers).

## 4. Assets policy

- References may be downloaded for internal documentation, stored under `docs/design/m007/references/` if kept.
- Nothing pulled from the web enters the APK automatically. Every candidate asset is logged in `docs/design/assets/ASSET-MANIFEST.md` with: name, owner, official URL, license/terms, attribution, local file, `production-approved: yes/no`.
- APK-shipped assets are limited to: officially authorized assets, assets with compatible licenses, open-source icons with license preserved, original illustrations produced in-house.
- No copied promotional imagery or illustrations from other apps. The Huawei logo is not used as primary branding without later approval.

## 5. Cycle 1 deliverables

Documents (under `docs/design/m007/`):

- `cycle-01-research.md` — visual research findings, references table, three-concept brainstorm inputs.
- `cycle-01-directions.md` — Directions A/B/C, comparison matrix, chosen direction + rationale.
- `cycle-01-motion-spec.md` — per-state motion spec (duration, easing, trigger, reduced-motion fallback) for the chosen direction.
- `cycle-01-asset-manifest.md` — Cycle-1-scoped subset of the asset manifest (owned assets, sources, approval status).
- `cycle-01-implementation-report.md` — what changed, files touched, before/after captures, motion frames, verification evidence, APK SHA-256.

Code changes: only inside the surfaces listed in §3, respecting §3.3.

Evidence: captured on `HuaweiSync_API_35`, stored under `docs/milestones/M007/screenshots/cycle-01/` (before / after, dark / light, viewport variants, motion frames).

## 6. Commit sequence

Five commits, each self-contained and small enough to review:

1. `docs(m007): plan visual and motion lab` — this PLAN.md + empty `docs/design/m007/` directory scaffolding + manifest skeleton.
2. `docs(m007-c01): visual research and chosen direction` — research + directions + motion spec + asset manifest for Cycle 1.
3. `feat(m007-c01): implement chosen direction on dashboard, sync-now, pipeline` — first Compose pass on the three surfaces.
4. `feat(m007-c01): post-critique polish` — refinements from Visual QA critique.
5. `docs(m007-c01): capture cycle-01 evidence` — screenshots, motion frames, `cycle-01-implementation-report.md`, APK SHA-256, verification transcript.

No merge, push, PR, reset, rebase, or force-push. Central GSD tracker is not updated (only M001 lives there).

## 7. Verification (definition of done for Cycle 1)

Run in this order from the worktree root, all must pass:

```
./gradlew testDebugUnitTest
./gradlew testReleaseUnitTest
./gradlew clean test lint assembleDebug
scripts/verify.sh
git diff --check
```

Additional required checks:

- Runtime packages unchanged: `git diff --stat 719f70d..HEAD -- app/src/main/java/dev/lui/huaweisync/{data,domain,health,diagnostics,presentation}` shows zero changes.
- APK SHA-256 recorded in `cycle-01-implementation-report.md`.
- Emulator screenshots for each visual state (waiting, writing, verifying, confirmed, error) on Dashboard, Sync-now modal, and Pipeline, in both dark and light themes.
- One motion sequence (frame series or short capture) per state that has motion.
- TalkBack / accessibility content descriptions unchanged for the FAB and the sync modal pane title.
- Reduced-motion path exercised (a preview or capture with `HuaweiSyncMotionProvider(reducedMotion = true)`).

When all of the above pass:

- leave the emulator open, Huawei Sync in the foreground on the Dashboard,
- present the main references,
- explain the chosen direction,
- list the changes and motions implemented,
- report the final commit SHA and the APK SHA-256,
- stop for human visual evaluation.

Do not advance to History, Activity Detail, or any other surface until the user approves Cycle 1.

## 8. Persistence rules

- Every design decision made during Cycle 1 must land in `docs/design/m007/cycle-01-directions.md` (rationale) or `cycle-01-motion-spec.md` (motion contract) so a future session can reconstruct the reasoning without this chat.
- Every reference cited in `cycle-01-research.md` includes URL, owner, access date, and classification (visual reference / reusable asset / prohibited).
- Any deviation from this PLAN.md is recorded as a short `## Cycle 1 addendum` section at the bottom of this file with the commit SHA that introduced the deviation.
