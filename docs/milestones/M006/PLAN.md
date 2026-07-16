# M006 — Product Design Refinement (Direction A)

**Status:** planning (docs-only; no lifecycle DB row yet — see "Lifecycle recovery" below)
**Branch:** `milestone/M006`
**Worktree:** `.gsd-worktrees/M006`
**Base commit:** `1bd7126` (M005 UX docs)
**Approved direction:** A — Refinement (`docs/design/m005-design-directions.md § Direction A`)
**Preserved invariants:** Room ledger, coordinator, deterministic identity, deduplication, reconciliation, claims policy, `ExerciseSessionRecord` as the only P0 record type. This milestone touches presentation only.

## 1. Vision

Close every P0 usability/honesty/accessibility gap and the P1 refinement moves that make Direction A shippable, without altering runtime semantics. When M006 ships, the user opens Huawei Sync, taps sync once, sees calm progress, and reads a truthful "Confirmed in Health Connect — available for GymRats to import." Everything below that headline is craft: typography that actually loads, hierarchy that is inverted the right way round, contrast that meets AA, one acknowledgement moment on completion, one CTA hierarchy, one accent color, one success color.

Explicit non-goals: no new record types, no new integrations, no metric surfaces, no backend, no Kotlin/Compose/Gradle/resources changes in *this* planning artifact.

## 2. Source inputs

- `docs/design/m005-design-audit.md` — findings, protected strengths, dimension list.
- `docs/design/m005-screen-inventory.md` — screen ↔ screenshot map.
- `docs/design/m005-design-directions.md` — Direction A specification.
- `docs/design/m005-prioritized-backlog.md` — P0 (10 rows), P1 (12 rows), P2 (opt-in), R- (rejected).

Every slice below cites the backlog rows it closes.

## 3. Slices

Ten thin, independently-verifiable slices. Ordered so each slice ships an observable user-visible change, and so slices with the highest honesty/accessibility risk land first. All slices target the same branch `milestone/M006`.

### S01 — Sync-now visual feedback (P0-01, P0-08)
**Closes:** F-C1, F-A3.
**Change:** wire the Dashboard `Sync now` FAB to open `SyncNowModal` on tap (or, if the no-modal design is intentional, show inline progress on Dashboard so state visibly changes on tap); collapse the FAB's a11y semantics so TalkBack reads a single state-aware `contentDescription` (`Sync now` / `Sync in progress` / `Synced — sync again`), not `Sync now, Sync complete, Button`.
**Files (planning only):** `DashboardScreen.kt`, `HuaweiSyncRoot.kt`, `HuaweiSyncNavigationState.kt`, `ModernistComponents.kt::SyncFab`.
**Verify:** manual — tap FAB with sync idle / mid-flight / complete → visible state changes each time; TalkBack reads exactly one label per state; instrumentation test asserts the composed content description.
**Risk:** low. No runtime coupling changes.

### S02 — FAB clipping & content padding (P0-03, P1-11)
**Closes:** F-S1, F-S2.
**Change:** raise Dashboard `LazyColumn` bottom padding from 112dp → 144dp (or move FAB to `Scaffold(floatingActionButton = …)`); bump Dashboard section spacing `xl 24dp` → `xxl 32dp`; invert card padding ratio to outer 16dp / inner 24dp.
**Files:** `DashboardScreen.kt`, `HuaweiSyncRoot.kt`, `ModernistSurface.kt`.
**Verify:** screenshot regression against Pixel 5 / Pixel 7 Pro emulators — Verification card fully visible above the FAB in both viewports; card breathing matches Direction A spec.
**Risk:** low.

### S03 — Sync-truth copy for Health Connect & GymRats (P0-04, P0-05)
**Closes:** F-CO1, F-CO2, F-CO3.
**Change:** rewrite the Dashboard hero body from `A matching deterministic record was read back from Health Connect.` → `Confirmed in Health Connect. GymRats can import it on next open.`; rewrite GymRats service row from `READY FOR GYMRATS TO READ` → `Available for GymRats to import`; delete the `08 PROVIDERS` claim on Integrations (or bind it to the actual runtime count).
**Files:** `DashboardScreen.kt` (`supportingText` map), `IntegrationsScreen.kt`, `ProductSyncState.kt` label constants.
**Verify:** string-resource unit tests assert no reference remains to the old copy; snapshot test on Dashboard hero / GymRats row / Integrations header matches new copy; audit checklist confirms honesty invariants from `m005-design-directions.md § Shared constraints` still hold (nothing says `Synced!`).
**Risk:** medium — copy touches the honesty moat. Copy review required before merge.

### S04 — Settings → Integrations label unification (P0-06)
**Closes:** F-N1.
**Change:** keep `Integrations` as the destination label everywhere; delete the `SETTINGS` relabel in the More menu at `HuaweiSyncRoot.kt:356`.
**Files:** `HuaweiSyncRoot.kt`.
**Verify:** grep asserts no `SETTINGS` string remains in navigation code; UI test navigates via bottom-nav, wide-nav rail, and More menu → all three routes land on the same `Integrations` destination with the same title.
**Risk:** low. No route table changes.

### S05 — Remove metrics without a real source (P0-05 continuation, and Integrations claims audit)
**Closes:** F-CO3 and any residual `08 PROVIDERS` / stub counts on Onboarding (`STEP 01/04` when there are only two steps — R-10 says the fake steps stay rejected, so the fix is to drop `01/04`).
**Change:** audit `IntegrationsScreen.kt`, `OnboardingScreen.kt`, and any status-chip site that renders a number the runtime cannot back; either bind to a real source or delete the line. No new fields are read from the runtime — deletion is the default.
**Files:** `IntegrationsScreen.kt`, `OnboardingScreen.kt`, string tables.
**Verify:** manual walk of every screen with the runtime in three states (fresh install, permission denied, sync complete); no numeric or count label appears whose value is a constant unrelated to observed state.
**Risk:** low. Deletions only; no runtime changes.

### S06 — Dark-theme contrast for disclaimers (P0-07)
**Closes:** F-A2.
**Change:** raise `ink3` on dark from 38% → 52% alpha so `bodySmall` disclaimers hit 4.5:1 AA against `#0B0A09`; retire `ink4` for text roles (leave for decorative rules only).
**Files:** `Color.kt`, any composables using `ink3`/`ink4` for text.
**Verify:** unit test computes WCAG contrast for every `ink*` × background pair used for text and asserts ≥ 4.5:1 for `bodySmall` and up; manual walk on Pixel 5 emulator in dark mode confirms disclaimers readable.
**Risk:** low.

### S07 — History empty-state centering (P0-09)
**Closes:** F-E2, F-S3.
**Change:** wrap the empty-state card in `Column(verticalArrangement = Arrangement.Center, modifier = fillMaxSize)` (or equivalent) so it centers vertically instead of floating at ~60% height.
**Files:** `HistoryScreen.kt`.
**Verify:** snapshot test on empty History for compact and wide viewports; card center-y within ±8dp of viewport center after subtracting top bar + bottom nav insets.
**Risk:** low.

### S08 — Dashboard failure surfacing (P0-10)
**Closes:** F-E4.
**Change:** reorder the Dashboard `LazyColumn` so `SANITIZED FAILURE SUMMARY` renders at the top when the runtime exposes a non-null failure, replacing the ready-to-sync hero rather than living below the fold.
**Files:** `DashboardScreen.kt` (item ordering only — no new data model, no changes to `ProductSyncState` shape).
**Verify:** compose test with a synthesized failure state asserts the failure card is the first non-topbar item; with a null failure it does not render; snapshot test confirms the hero returns when failure clears.
**Risk:** medium — the reordering must not accidentally suppress the hero when both are absent, nor duplicate it when both are present. Add a two-state fixture.

### S09 — Reduce eyebrows & jargon (P1-01)
**Closes:** F-H2, F-T5.
**Change:** enforce Direction A's "one eyebrow per screen, at the page top only" rule. `SectionHeader.kt` gets an `eyebrow` param that defaults to `null`; every existing call-site with a section-level eyebrow (`DURABLE LEDGER`, `CURRENT FACTS`, `HEALTH CONNECT READBACK`, etc.) drops the eyebrow. Also demote the "three separate uses of *deterministic*" pattern noted in the audit — sentence-case one, keep one uppercase chip role.
**Files:** `SectionHeader.kt`, every screen that composes `SectionHeader` today.
**Verify:** grep test — no more than one `eyebrow = ` param per screen file; manual walk asserts each screen has exactly zero or one eyebrow at the top.
**Risk:** medium — high touch count. Land after S03 so copy work reviews together.

### S10 — Typography scale & font licenses (P0-02, P1-02)
**Closes:** F-T1, F-T2, F-H1.
**Change:** bundle Archivo (400/600/800) and JetBrains Mono (400/600) as `app/src/main/res/font/` assets with their SIL OFL licenses; point `ProductSans` / `TechnicalMono` at bundled families instead of `FontFamily.SansSerif` / `FontFamily.Monospace`; raise Dashboard page title from `titleMedium 15sp` to `headlineLarge 28sp` (or a new `pageTitle` role in `Type.kt`) per Direction A's H1 spec.
**Files:** `app/src/main/res/font/**`, `Type.kt`, `DashboardScreen.kt::DashboardTopBar`, `app/build.gradle.kts` if a font resource is registered there.
**Verify:** APK inspection confirms the four font files are shipped; runtime log confirms `ProductSans` resolves to Archivo (not the platform fallback); snapshot test on Dashboard top bar shows the 28sp title; license files present at repo path required for OFL redistribution.
**Risk:** medium — license diligence is the load-bearing part. Confirm SIL OFL 1.1 requirements before merge (attribution, no reserved font name conflicts, license file bundled inside the APK).

## 4. Dependency order

```
S03 ─┐
S04 ─┼─→ S09  (copy + eyebrow rules land together)
S05 ─┘
S01 ─→ S08   (failure surfacing sits on the reordered LazyColumn; do S01 first so tap-to-open is already wired)
S02, S06, S07  independent, can ship anytime after S01
S10  independent, but land last so typographic changes review against the finished copy
```

No slice depends on Kotlin, Compose, Gradle, or resource *changes* being made in this planning artifact — this doc is the plan, not the code.

## 5. Acceptance criteria (milestone-level)

M006 ships when all of the following hold on `milestone/M006`:

1. Every backlog row cited above (P0-01 through P0-10, P1-01, P1-02, P1-11) has a slice marked complete with an evidence link.
2. `scripts/verify.sh` and `./gradlew clean test lint assembleDebug` pass on the M006 worktree.
3. A manual regression pass on Pixel 5 (compact) and Pixel 7 Pro (wide) emulators, in both light and dark themes, confirms:
   - FAB tap always produces visible state change.
   - Verification card visible above FAB (no clipping).
   - Every screen has ≤ 1 eyebrow, at the top.
   - Every `bodySmall` disclaimer on dark passes 4.5:1 AA.
   - History empty state renders centered.
   - Dashboard renders the sanitized failure summary at the top when a failure exists.
   - Copy audit: no screen says `Synced!`; every honesty invariant from `m005-design-directions.md § Shared constraints` holds.
4. Runtime semantics unchanged — Room schema, ledger writes, coordinator phases, deterministic identity, deduplication, reconciliation, claims policy, and `ExerciseSessionRecord` shape are byte-identical to the M006 base commit `1bd7126` (verify by diffing the relevant packages).
5. Font licenses: Archivo + JetBrains Mono SIL OFL 1.1 files bundled per OFL redistribution terms.

## 6. Risks & mitigations

| # | Risk | Mitigation |
|---|---|---|
| R-M6-1 | Copy edits in S03 accidentally overpromise (e.g. someone rewrites `Available for GymRats to import` back to `Synced!`). | Copy review is a required gate for S03. Add a lint-style test that fails the build if forbidden strings appear (`Synced!`, `Confirmed everywhere`, etc.). |
| R-M6-2 | S09 (eyebrow reduction) touches many files, high merge-conflict surface. | Land after S03 so the same reviewer covers both; keep the `SectionHeader` param backward-compatible (default `null`) so call-site cleanup can be split into smaller commits. |
| R-M6-3 | S10 font bundling changes app size and could regress cold-start rendering. | Measure APK size delta and cold-start-to-first-frame on the same emulator before/after; if size regresses > 1 MB or cold start > 5%, revisit downloadable-fonts path. |
| R-M6-4 | S08 reorder hides the hero card when failure is stale. | Two-state fixture in the compose test: (a) failure=null → hero present, failure card absent; (b) failure=set → failure card first, hero absent. |
| R-M6-5 | Contrast fix in S06 (`ink3` alpha bump) affects decorative rules that reuse `ink3` for lines. | Audit every `ink3` use before the change; migrate line-only uses to a dedicated `line3` token if needed. |
| R-M6-6 | Lifecycle blocker (see §7) means auto-mode cannot drive M006. | This plan is intentionally executable manually — every slice states its files and its verify step so a human can walk them. Auto-mode adoption is a separate track once §7 is resolved. |

## 7. Lifecycle recovery — why this plan is docs-only

The GSD MCP server's project root for this session is `.gsd-worktrees/M006`. That directory has no `.gsd/` (project preferences use `git.isolation: worktree`, so the state store lives only in the parent repo). From the server's viewpoint the M006 worktree is "no project" — that is why the console shows both `milestone/M006` and `No active GSD project` simultaneously.

The parent repo's `.gsd/gsd.db` + `.gsd/ROADMAP.md` register only M001 (`🔄 M001: Gate 1 static preparation`). M002, M003, M004, and M005 were completed as branches/worktrees *outside* the lifecycle tracker — an established, if implicit, project pattern. `/gsd auto` therefore reads the parent state and correctly reports "M001 is complete but not merged"; it has no row for M006 to schedule.

Continuing M006 through the lifecycle without violating the user's constraints (no merge of M001, no re-init, no recreation of M006, no history rewrite) requires one of:

- **Option A (recommended, needs explicit approval):** register M006 in the parent-repo DB with `mcp__gsd-workflow__gsd_plan_milestone`, passing the ten slices below, so the lifecycle tracker has a row it can advance. This is a DB write only; no source code, no merges. It does not resolve M001 — M001 stays `🔄` — but it lets `/gsd auto` schedule M006 in parallel. This is safe if the M002–M005 pattern (unregistered milestones) is acceptable to continue.
- **Option B:** skip lifecycle registration entirely and execute M006 by hand, driving each slice's verify step through `verify-before-complete`. This plan document is the durable artifact; slice completion is tracked in commits and PR checklists, not in `.gsd/gsd.db`. This is fully consistent with what M002–M005 already did.
- **Option C:** rewrite the parent-repo roadmap to backfill M002–M005 as historical milestones and add M006 fresh. This *is* history editing of the tracker (though not of git), so it needs explicit approval and is not proposed here.

Both A and B are non-destructive. B is the smallest step; A adds tracking without altering M001. C is not recommended unless the user wants a clean audit trail across all six milestones.

Awaiting approval before proceeding to any implementation or lifecycle write.
