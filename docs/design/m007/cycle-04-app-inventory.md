# Cycle 4 — App inventory

**Cycle scope.** Full app-wide research pass for Huawei Sync. Read-only audit of every production surface. No Compose, Kotlin, Gradle, Room, ledger, coordinator, or Health Connect change ships with Cycle 4.
**Role.** Mobile Product Researcher.
**Base commit for the audit.** `74bc032`. Every surface below was read directly from source in that commit; nothing here is derived from screenshots.
**Sources.** `app/src/main/java/dev/lui/huaweisync/ui/**` — every screen, `HuaweiSyncRoot.kt`, `HuaweiSyncNavigationState.kt`, `HuaweiSyncDestination.kt`, `Tokens.kt`, `MotionPolicy.kt`, plus the state/mapper files under `ui/state/`.
**Access date.** 2026-07-17.

## 1. What this inventory is for

Cycles 1–3 improved individual surfaces (sync rail, ledger reading, one Dashboard concept in a debug gallery). No cycle has looked at the app as one product. Cycle 4 opens with that missing pass: describe what is actually on the phone today, per screen, then hand a factual base to the other Cycle 4 roles (IA, Art Direction, Motion, Assets).

The audit is **strictly descriptive**. Every recommendation in the last column of § 3 is a *proposal* — nothing here is decided. Choices land in `cycle-04-information-architecture.md`, `cycle-04-directions.md`, and `cycle-04-implementation-roadmap.md`.

## 2. Global navigation map (as shipped in `74bc032`)

Formal destinations declared in `HuaweiSyncDestination.all`:

| # | Route | Label | Role |
|---|---|---|---|
| 1 | `onboarding` | Setup | Screen |
| 2 | `dashboard` | Dashboard | Screen |
| 3 | `pipeline` | Pipeline | Screen |
| 4 | `sync-now` | Sync now | Modal (never replaces the current screen) |
| 5 | `integrations` | Integrations | Screen |
| 6 | `diagnostics` | Diagnostics | Screen |
| 7 | `automation` | Automation | Screen |
| 8 | `history` | History | Screen |
| 9 | `activity-detail` | Activity detail | Screen (reached only from History) |
| 10 | `ai-assistant` | AI Assistant | Screen |

Exposed as tabs / entries:

- **Wide primary rail** (`WidePrimaryNavigation`, ≥ 760dp): Dashboard, Pipeline, Integrations, Diagnostics, Automation, History, AI Assistant, plus a bottom `Setup` button and an accented `Sync now` button.
- **Compact bottom bar** (`CompactPrimaryNavigation`, < 760dp): Dashboard, Pipeline, History, `More`.
- **Compact "More" menu**: every primary destination not on the bottom bar plus `Onboarding` and `Sync now`.
- **Sync now**: always a `Dialog` overlay owned by `HuaweiSyncNavigationState.overlayDestination`. Never a full-screen destination.

`HuaweiSyncNavigationTest` locks `HuaweiSyncDestination.all.size == 10` — the inventory below does not assume that number is inviolable. It is a test contract; it is not a product decision.

Preserved runtime dependency: `Diagnostics` embeds the proven Gate 1 runtime surface. Any IA change must keep that entry alive (its contents can be rethemed but the surface itself cannot be dropped without touching runtime tests — out of scope for Cycle 4).

## 3. Per-surface inventory

Columns per screen:

- **Purpose (real).** What the screen actually does today, from the source.
- **Primary action.** The one thing a user is meant to do here.
- **Secondary action(s).** Everything else offered on the same surface.
- **Must-have information.** Facts the screen cannot honestly omit.
- **Nice-to-have / dispensable information.** Content that carries little value today.
- **Problems today.** Concrete UX issues from the current source, no speculation.
- **Technical-language load.** Where copy leaks internal vocabulary.
- **Visual rigidity.** Where the flat-square language hurts.
- **Density.** Load per fold on a 412dp × 892dp compact viewport.
- **Hierarchy issues.** Where the eye is uncertain what to read first.
- **Chart opportunity.** Honest chart possibilities *given P0 data only* (see `cycle-04-benchmark.md § 4` for the honest-chart shortlist).
- **Image opportunity.** Where an image (original, not brand asset) would help.
- **Motion opportunity.** Where movement would clarify state, per `cycle-04-motion-system.md`.
- **Simplification opportunity.** What could be cut, merged, or moved off the screen without losing evidence.
- **Real functional dependencies.** What has to remain wired for this screen to work correctly.
- **Preview / Coming-soon content.** Whether the surface is proven or preview-only today.
- **Recommendation.** `KEEP` / `COMBINE` / `MOVE` / `RENAME` / `REMOVE` / `CONTEXTUALIZE` (accessible from context only, not a permanent tab).

### 3.1 Onboarding (`OnboardingScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Explain what Huawei Sync does (write workout sessions from Huawei Health into Health Connect) and offer two entry buttons: `Start setup` and `Continue existing setup`. Both currently trigger `onSync()` and land the user on the Dashboard. |
| Primary action | `Start setup`. |
| Secondary action(s) | `Continue existing setup`, theme toggle in the top-bar. |
| Must-have information | Product mission statement; Health Connect availability strip; disclaimer that Huawei Sync only writes records the user explicitly syncs. |
| Dispensable information | `TechnicalMicrocopy("FOR HUAWEI WATCH OWNERS")` eyebrow duplicates the mission statement two lines below; `OnboardingHeader` `SETUP` eyebrow adds no new fact. |
| Problems today | Both primary buttons execute the *same* action (`onSync` + navigate to Dashboard) — the second button offers zero differentiation. No permission grant happens on Onboarding itself; the "start" is opaque. The Canvas orbit (H — HC — APPS) is a diagram, not an interaction, and does not carry state. |
| Technical-language load | Medium. `FOR HUAWEI WATCH OWNERS` eyebrow, `HEALTH CONNECT` eyebrow, disclaimer paragraph reads like copy tested for legal, not for a user's first minute. |
| Visual rigidity | High. Zero corner radius, straight-edge buttons, hairline borders — a corporate splash, not a personal onboarding. |
| Density | Low (mission + orbit + strip + 2 buttons + disclaimer fits one fold on 412×892). |
| Hierarchy issues | The display heading fights the `ConnectionOrbit` illustration for attention because both are competing for the top-third. |
| Chart opportunity | None on Onboarding. |
| Image opportunity | An original watch → phone illustration (not the H/HC/APPS orbit) would humanise the moment. Cycle 4 does not commission it; `cycle-04-visual-system.md § 7` scopes the illustration slot. |
| Motion opportunity | A one-time entry animation on the orbit (draw the connection line) marks setup as beginning — not looped, per `cycle-04-motion-system.md § 5`. |
| Simplification opportunity | Collapse the two buttons into one `Set up Huawei Sync` primary CTA that triggers the *actual* permission grant, plus a small `I already set it up` textlink to dismiss. |
| Real functional dependencies | Reads `productSyncState?.healthConnectStatus` to render the availability strip. Wires `onSync` and Dashboard navigation on button press. |
| Preview / Coming-soon | Proven — the two buttons currently both call `onSync` and navigate, but the screen itself is production. |
| Recommendation | **KEEP** (rewrite copy + reduce to one primary CTA). Onboarding is the only surface framed for a first-time user; removing it would break the setup story. |

### 3.2 Dashboard (`DashboardScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Render the current `ProductSyncState` as: (a) optional failure summary, (b) `SyncHero` with status label, phase eyebrow, pipeline rail, health-connect action, (c) `LedgerSummary` metric row, (d) `ConnectedServices` (Health Connect + GymRats), (e) `VerificationSummary`. FAB launches `SyncNow` modal. Long-press on the `H` logo opens the debug `PrototypeGalleryDialog` (`BuildConfig.DEBUG` only). |
| Primary action | Trigger `Sync now` via the FAB. |
| Secondary action(s) | `Review permission` / `Open Health Connect update` / `Review reconciliation` etc. via the contextual button in `SyncHero`; theme toggle in top-bar. |
| Must-have information | Current phase, sync status, sanitized failure summary when present, ledger count, Health Connect + GymRats status, verification counts. |
| Dispensable information | `TechnicalMicrocopy("LIVE PRODUCT STATE")` under the Dashboard title (redundant with everything below). `SANITIZED FAILURE SUMMARY` eyebrow above the sanitized failure summary (the surface already scopes it). `MetricRow` "Current record attempts" — attempts per-record is an internal diagnostic, not something a user needs on Home. |
| Problems today | Every section is a same-altitude bordered rectangle → hierarchy collapses; the eye has no anchor. The `H` logo is also a debug gesture — undiscoverable. GymRats row shows a status but has no path forward (no button to open GymRats). `VerificationSummary` reads as engineering QA, not user reassurance. |
| Technical-language load | High. `LIVE PRODUCT STATE`, `IDLE`, `PREFLIGHT`, `WRITE`, `ACCEPTANCE`, `VERIFICATION`, `RECONCILIATION`, `SANITIZED FAILURE SUMMARY`, `PHASE 1 OF 5 — PREFLIGHT`, `Deterministic ID matches`, `Expected version matches`. Correct as truth; heavy as personality (see `cycle-03-directions.md § 1.2` for the earlier read). |
| Visual rigidity | Maximum. Every card is a `ModernistSurface` with a hairline `line` border on a `surface2` fill. |
| Density | Medium (~one fold above the fold once failure summary + hero + ledger row are in — the FAB overlaps the ledger row on smaller devices because of the fixed 144dp bottom pad). |
| Hierarchy issues | Sync hero is *not* visually the biggest thing — it competes with the fixed FAB and the border chrome. The most important sentence on the screen (health-connect status label) shares altitude with a MetricRow row that just says "Workouts tracked 0". |
| Chart opportunity | Weekly workout count sparkline based on `ledgerWorkoutCount` history (needs a rolling window that the ledger already supports). Sync-success rate (accepted vs. attempted) from the ledger. No calories, no HR, no distance — see `cycle-04-benchmark.md § 4`. |
| Image opportunity | Minimal on Home. The `H` monogram logo can become a real product mark; that is out of Cycle 4 scope for creation but tracked in `cycle-04-brand-assets.md § 6`. |
| Motion opportunity | Hero state-change cross-fade (already in Concept A, Cycle 3); ledger-count `animateContentSize`; phase pulse *only when active*. Reduced-motion has to short-circuit every path. |
| Simplification opportunity | Merge `LedgerSummary` + `VerificationSummary` into one "Your workouts" card (one number + one one-liner "N confirmed in Health Connect"). Remove the `ConnectedServices` card from Home and move it to a `Connections` destination (see IA doc). Move `sanitizedFailureSummary` into the hero itself as an error state, not a separate card above the hero. |
| Real functional dependencies | Reads full `ProductSyncState`; drives `onSync`, `onShowSyncOverlay`, `onResolveHealthConnect`. Hosts the debug `PrototypeGalleryDialog` gate. |
| Preview / Coming-soon | Proven. Every fact is derived from production state; the debug gallery is `BuildConfig.DEBUG` only and is dead-stripped in release. |
| Recommendation | **KEEP** (rebuild as "Home"). Home is the app's front door and the only place the user does a sync; it stays. What changes is the composition — see `cycle-04-directions.md` and `cycle-04-screen-redesign.md § 1`. |

### 3.3 Sync now (`SyncNowModal.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Fullscreen `Dialog` overlay showing the coordinator's live phase — `PhaseMedallion` letter, horizontal `SyncPipelineRail`, `statusLabel`, contextual explanation, `PHASE CHECKS` strip (5 slots for PREFLIGHT/WRITE/ACCEPTANCE/VERIFICATION), close button. |
| Primary action | Dismiss when done. |
| Secondary action(s) | None. |
| Must-have information | Current phase label; per-phase check state; readback-confirmed flag; explanation of what is still happening. |
| Dispensable information | The `PHASE CHECKS · NO ESTIMATED PERCENT OR TIME` eyebrow (correct policy, but as user copy it reads as engineering apology). |
| Problems today | Modal opens on FAB tap *and* on every navigation to Sync now (a `LaunchedEffect` re-fires `onSync()` on every overlay show). If the user opens Sync now to *watch* an already-running sync, the app kicks a new one. The `SyncPipelineRail` also appears on Dashboard, so the modal duplicates Dashboard visuals. |
| Technical-language load | High. `SYNC STATUS`, `SYNC IN PROGRESS`, `P/W/A/V/R` letter medallion, `PHASE CHECKS · NO ESTIMATED PERCENT OR TIME`. |
| Visual rigidity | High. Rectangle-inside-rectangle dialog with a 72dp letter box in the middle. |
| Density | Low (single-screen dialog). |
| Hierarchy issues | The letter medallion (P/W/A/V/R) fights the status label + explanation paragraph. |
| Chart opportunity | Not needed on this modal. |
| Image opportunity | None. |
| Motion opportunity | This is the moment motion should carry meaning: a real progress indication that follows *observed* phase changes, not simulated fractions. See `cycle-04-motion-system.md § 4`. |
| Simplification opportunity | Move Sync now from `Dialog` overlay to a bottom-sheet (compact) / side panel (wide) so it can co-exist with the current screen rather than blocking it. Remove the letter medallion once the pipeline rail carries the same information. Kill the auto-`onSync` on overlay open — separate `Watch` from `Trigger`. |
| Real functional dependencies | Reads `ProductSyncState`; wires `coordinatorBusy`; triggers `onSync()` (currently on every open — a defect flagged for the roadmap). |
| Preview / Coming-soon | Proven. |
| Recommendation | **KEEP** (rework as a sheet, fix the auto-trigger, delete the letter medallion). |

### 3.4 Pipeline (`PipelineScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Longer-form live view of the same coordinator/ledger state: endpoint cards (Huawei Health import "not connected yet", Health Connect, GymRats), `PipelinePhaseCard` with the 5 coordinator steps, and an `ACTUAL LEDGER EVIDENCE` panel with ledger workouts + write attempts + optional sanitized failure. |
| Primary action | None (read-only). |
| Secondary action(s) | None. |
| Must-have information | Coordinator phase details, ledger evidence counts, sanitized failure summary. |
| Dispensable information | Duplicates Dashboard's `SyncHero` almost entirely — the horizontal rail on Dashboard already communicates node states; the `PipelinePhaseCard` here adds one more vertical rail; the endpoint cards repeat what `ConnectedServices` shows on Dashboard. |
| Problems today | Redundant with Dashboard + Sync-now for anyone not debugging a failed run. Very technical (phase names, evidence eyebrows). Its own eyebrow `LIVE · READ-ONLY` labels it as a report, not a tool. |
| Technical-language load | Maximum. `LIVE · READ-ONLY`, `ACTUAL LEDGER EVIDENCE`, `COORDINATOR PHASE 1 OF 5`, `NEEDS ATTENTION`, `AWAITING G2`. |
| Visual rigidity | High (same `ModernistSurface` cards). |
| Density | High for what it teaches — the entire screen is metadata. |
| Hierarchy issues | Every card wears the same weight. The vertical pipeline steps are the only novel content and they are visually the same size as the endpoint cards. |
| Chart opportunity | Sync-success-rate over time, phase-duration distribution. Not needed for MVP. |
| Image opportunity | None. |
| Motion opportunity | Reuse the same active-phase pulse from Sync-now; do not invent a new one. |
| Simplification opportunity | Fold everything worth keeping into a **contextual "See sync details"** action on Home / Sync now / Diagnostics. Do not keep it as a permanent tab. |
| Real functional dependencies | Reads `ProductSyncState`, `coordinatorBusy`; renders the shared pipeline presentation. |
| Preview / Coming-soon | Proven. |
| Recommendation | **CONTEXTUALIZE** (remove from the primary navigation; reach it from Home's hero on tap, from Sync-now's "Show details" button, from Diagnostics' "Coordinator internals" link). |

### 3.5 Integrations (`IntegrationsScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Show every integration provider Huawei Sync knows about, grouped as `activePath` (Huawei Health, Health Connect, GymRats) and `futureProviders` (Samsung Health, Strava, Garmin, Fitbit, Google Fit, Oura, etc.). Each card shows a monogram, name, status chip, one-line detail, and an evidence-level microcopy (`EVIDENCE / OFFICIAL READBACK` / `CONFIGURATION` / `LOCAL STATE` / `ROADMAP ONLY`). |
| Primary action | None. No card is tappable to open, resolve, or connect anything. |
| Secondary action(s) | None. |
| Must-have information | Which providers the app talks to, the current status of each, whether it is available today. |
| Dispensable information | The `EvidenceBanner` above the route diagram (`STATUS FOLLOWS EVIDENCE …`) is a policy statement, not user information. The `ROADMAP ONLY` evidence microcopy under every future card is a legal footnote applied per row. |
| Problems today | Everything is a monogram tile (`HW`, `HC`, `APP`, `GR`, `HR`, `ST`, `GC`, `FB`, `GF`, `OR` etc.) — no logo, no icon, no identity. Cards look identical; the eye cannot separate Strava from Garmin from Fitbit. No card resolves to an action, so the screen is a table with no rows to open. Route strip (`HUAWEI → ANDROID HUB → CONSUMERS`) duplicates the topology sketch on Onboarding. |
| Technical-language load | Very high. `EVIDENCE-BASED CONNECTIONS`, `READBACK GATED`, `SOURCE BOUNDARY`, `EVIDENCE / OFFICIAL READBACK`. |
| Visual rigidity | Maximum (grid of identical bordered rectangles). |
| Density | High. Full app-wide integration list + evidence banner + route strip on one scroll. |
| Hierarchy issues | Featured Health Connect card and non-featured cards use the same shape; the featured accent is a colored border, not a size or elevation change. |
| Chart opportunity | None. |
| Image opportunity | This is the surface where **real brand logos** would carry the most value if usage terms allow (see `cycle-04-brand-assets.md`). Health Connect, GymRats and the future providers each own a mark; using text monograms in place of official marks reads as unfinished. |
| Motion opportunity | Small state-change tint animation when a provider transitions status; no ambient motion. |
| Simplification opportunity | Rename to **Connections**. Split into two sections — *Active* (Huawei Health → Sync → Health Connect → GymRats) and *Preview* (everything else). Cards become tappable: tap opens a detail sheet with the provider's status, permission action, and a link out to the official app. |
| Real functional dependencies | Reads full `ProductSyncState` and derives `IntegrationState` from it. |
| Preview / Coming-soon | Mostly Preview / Coming-soon. Only Huawei Health (source, not connected via SDK yet), Health Connect, and GymRats have any evidence. |
| Recommendation | **KEEP + RENAME to "Connections"** + rework cards. Move to Home's second tab if IA option C is chosen (see IA doc). |

### 3.6 Diagnostics (`DiagnosticsScreen.kt` + embed)

| Field | Content |
|---|---|
| Purpose (real) | Show the current Gate 1 runtime diagnostic: environment (availability + permission), status summary (evidence code, message, next action), Room ledger facts, Health Connect readback facts, classification facts, and closed next actions (Check availability, Request permission, Run sync, Confirm, Reconcile, Refresh, Export). The `HuaweiSyncNavigationShell` wraps it with a `SectionHeader("Diagnostics center", eyebrow = "PROVEN GATE 1 SURFACE")`. |
| Primary action | Depends on the current diagnostic — e.g. `Request permission`, `Run sync`, `Reconcile`. |
| Secondary action(s) | `Refresh`, `Export`. |
| Must-have information | The current safe status code and its next-action guidance; the runtime environment; the ledger facts that prove or refute Gate 1 today. |
| Dispensable information | The `PROVEN GATE 1 SURFACE` eyebrow is engineering vocabulary. The `GATE 1 / RUNTIME EVIDENCE` header repeats it. |
| Problems today | Diagnostics is exposed as a top-level tab on the compact bottom bar via `More`, and as a first-class rail item on wide. It is a *support tool*, not something a user pulls up in normal flow. Its presence at the top level pushes real product surfaces (History, Connections) down. |
| Technical-language load | Maximum, and correctly so — this is the debug surface. But the vocabulary bleeds into everything else in the app because the same tokens are shared. |
| Visual rigidity | Maximum. |
| Density | Very high — 4 fact sections + status + environment + action row on one screen. |
| Hierarchy issues | Actions row sits at the bottom; the primary action (varies per state) is not clearly the first thing to reach. |
| Chart opportunity | None (this is a facts surface). |
| Image opportunity | None. |
| Motion opportunity | State-badge tint transitions when a fact section changes. |
| Simplification opportunity | Move Diagnostics out of the top navigation. Reach it from a **`Help & diagnostics`** entry under a More/Settings destination, or from any error surface via a "Diagnose this" contextual button. Keep the on-screen content identical; only the entry point changes. |
| Real functional dependencies | Wraps `HuaweiSyncNavigationShell`'s `gate1Entry: @Composable () -> Unit` slot. The Gate 1 runtime binding stays intact. |
| Preview / Coming-soon | Proven. |
| Recommendation | **MOVE** — keep the screen (runtime-critical), remove it from primary tabs; reach it contextually. |

### 3.7 Automation (`AutomationScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Preview-only screen that describes future automation concepts (Scheduled sync, After a Huawei Health update, While charging, etc.) with every switch disabled and stamped `COMING SOON`. Includes a "Run local preview check" button that toggles a purely visual `checked` state. |
| Primary action | None real. `Run local preview check` is a stylistic self-test. |
| Secondary action(s) | Reset preview. |
| Must-have information | For a user: nothing — the entire feature is not shipping. |
| Dispensable information | Everything on the screen is a promise for future work. |
| Problems today | Occupies a top-level tab that *does not do anything*. Explicitly labels itself `PREVIEW ONLY · Nothing here is saved. No background job is created.` Cognitive cost is high; user reward is zero. |
| Technical-language load | High. `MASTER CONTROL`, `LOCAL SESSION`, `LOCAL INTERACTION · NOT A SYNC`. |
| Visual rigidity | Maximum. |
| Density | High (large list of concept cards + preview flow card). |
| Hierarchy issues | The preview flow card fights the trigger cards for attention when nothing on the screen is real. |
| Chart opportunity | None. |
| Image opportunity | None. |
| Motion opportunity | None until automation exists. |
| Simplification opportunity | Remove it from the primary navigation until at least one automation trigger is actually implemented. Keep the design intent in `docs/design/m007/cycle-04-directions.md § 4` (post-MVP scope), not in the running app. |
| Real functional dependencies | Reads `AutomationScreenState` (a static preview state). Does not wire coordinator, permissions, or work-manager. |
| Preview / Coming-soon | 100 % Preview. |
| Recommendation | **REMOVE** from top-level navigation. Reintroduce when the first real automation trigger ships. Not a Cycle 4 change — a follow-up. |

### 3.8 History (`HistoryScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Read-only ledger listing grouped by day, with a `NEEDS ATTENTION ×N` filter chip and per-row readback state (`Prepared` / `Pending readback` / `Verified` / `Reconciliation required` / `Retry available` / `Action required`). Tap a row → Activity detail. |
| Primary action | Open an activity via tap. |
| Secondary action(s) | Toggle "Needs attention" filter, retry the read if the ledger read failed. |
| Must-have information | Every durable ledger row; per-row source provider + readback state + timestamp + attempt count. |
| Dispensable information | `TechnicalMicrocopy("READ-ONLY LEDGER")` eyebrow duplicates the surface title. `DURABLE RECORDS` counter uses shouty caps for a factual count. |
| Problems today | Rows are hairline-bordered rectangles with a 6dp accent strip on the left — visually a table, not a list of workouts. The workout is described as `SYNTHETIC · Reconciliation required` rather than in terms a user recognises (workout name, duration, when it happened, what it was). The day labels use `TODAY`/`YESTERDAY`/`FRI 17 JUL` uppercase — reads as log output. |
| Technical-language load | Medium-high. State labels are already user-safe; group headers and counters are shouty. |
| Visual rigidity | High. |
| Density | Low-medium (grouped list, 8-10 rows/fold). |
| Hierarchy issues | The eye reads the readback-state label first, not the workout itself. The time is right-aligned and small; the attempt count `×N` is a footnote. |
| Chart opportunity | Workouts-per-week bar chart at the top of History; success-rate over time. Both derivable from ledger data today, so **honest** (see `cycle-04-benchmark.md § 4`). |
| Image opportunity | A single small activity glyph per row (walk / run / ride / other) *once Huawei Health integration lands and workout type becomes known* — until then, use an abstract mark. |
| Motion opportunity | List-to-detail shared element (row → detail identity band). Grouped-header sticky animation. Reduced-motion path required. |
| Simplification opportunity | Reframe: **"Your workouts"** with duration, day/time, and a small state pill on the right. Move the readback-state chip below the workout title, not above it. Keep the attention filter; consider adding a search when N ≥ 30. |
| Real functional dependencies | Reads `HistoryState`; wires `onRetry`, `onSelectActivity`. |
| Preview / Coming-soon | Proven. |
| Recommendation | **KEEP + RENAME** to "Activity" / "Your workouts". |

### 3.9 Activity detail (`ActivityDetailScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Full lifecycle of one ledger row: identity band, sync-path vertical rail, lifecycle timeline (`Prepared` → `Health Connect accepted` → `Readback verified` → `Available for GymRats to import`), and an expandable technical detail block with the deterministic client-record ID, version, attempt count, provider, and safe error code. |
| Primary action | Back. |
| Secondary action(s) | Show / Hide technical detail. |
| Must-have information | Every real fact about the record: identity, acceptance, verification, GymRats-availability, safe error code if any. |
| Dispensable information | `THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP.` — correct as policy, unfriendly as user copy. |
| Problems today | The identity band identifies the row by `SYNTHETIC · Activity record` — not by workout type, duration, or start time. The vertical `SyncPipelineRail` reuses the same nodes shown on Home, so a user re-reads the same topology. Timeline entries are internally consistent but again very technical. |
| Technical-language load | High. `SYNC PATH`, `LIFECYCLE`, `AWAITING G2`, `SAFE ERROR CODE HC_WRITE_RETRYABLE`. |
| Visual rigidity | High. |
| Density | Medium. |
| Hierarchy issues | Identity band + sync path + lifecycle + tech detail toggle — four sections at the same visual weight. The timeline is the interesting content; today it is the third thing on the page. |
| Chart opportunity | None (single-record). |
| Image opportunity | A subtle background wash keyed to the readback state (see Direction A). |
| Motion opportunity | Timeline entries stagger-in (already in Cycle 2). Shared-element transition from the History row (deferred; needs Compose Navigation change). |
| Simplification opportunity | Lead with the workout (name, duration, start time) once real data exists. Until then, lead with the day + time and treat the state pill as secondary. Fold the sync-path rail into the timeline (or drop it — the timeline says everything the rail does). |
| Real functional dependencies | Reads a selected `ActivityHistoryItem` via `HistoryStateMapper.select`. |
| Preview / Coming-soon | Proven. |
| Recommendation | **KEEP + REDESIGN** — deep-link target from History; content stays; visual language must soften. |

### 3.10 AI Assistant (`AssistantScreen.kt`)

| Field | Content |
|---|---|
| Purpose (real) | Preview-only assistant. `AssistantLocalContext` is derived from `ProductSyncState`; `AssistantReducer` runs canned deterministic rules on a small set of `AssistantPrompt` entries. Composer is disabled (`AssistantState.INPUT_DISABLED_REASON`). No model is loaded; no network is used. |
| Primary action | Tap a canned prompt. |
| Secondary action(s) | Clear conversation. |
| Must-have information | Whether the assistant can genuinely help (today: no). |
| Dispensable information | The whole notice + tile stack signals "we thought about AI"; that is not user value. |
| Problems today | Top-level tab that runs on canned answers. `EXPERIMENTAL · LOCAL ONLY` and `NO MODEL IS CONNECTED` are honest, but a top-level surface promising something the app cannot yet do is a bad first impression. |
| Technical-language load | High. |
| Visual rigidity | High (message bubbles are `ModernistSurface` rectangles). |
| Density | Low. |
| Hierarchy issues | Composer is disabled and prompts are the actual actions — the prompt list should be top; instead the notice + section header push it down. |
| Chart opportunity | None. |
| Image opportunity | None. |
| Motion opportunity | Message enter/exit only, and only if the assistant becomes real. |
| Simplification opportunity | Remove from top-level navigation; reintroduce when a real assistant (even fully local) can answer non-canned questions. Keep the state machine and the honest scoping copy for that future work. |
| Real functional dependencies | Reads `ProductSyncState` via `AssistantLocalContext`; entirely local; no network, no permission touch. |
| Preview / Coming-soon | 100 % Preview. |
| Recommendation | **REMOVE** from top-level navigation. Reintroduce only when the assistant can inspect at least one real signal (e.g. ledger state) and act on it (e.g. offer a fix). |

## 4. Global states (per-screen behaviour)

| State | Current behaviour | Problems today | Recommendation |
|---|---|---|---|
| Permission required | Dashboard hero renders a `Review permission` button; Onboarding shows availability strip. | The button opens `onResolveHealthConnect` which today equals `onSync` — a "review" that immediately kicks a sync is misleading. There is no in-app explanation of *what* permission is required and *why*. | Split `Review permission` into `Grant permission in Health Connect` (opens the Health Connect Android intent) and `Learn what Huawei Sync writes` (opens a short in-app explainer sheet). Never re-run `onSync` from a "review" button. |
| Error | Failure summary appears as a bordered card *above* the hero (`FailureSummary`) and as a supporting line inside `PipelineScreen`. Retry lives inside the coordinator, not the UI. | Two places to see the same failure. No direct retry action in the hero. Sanitized code is not surfaced to a user in plain language. | One error surface per screen: on Home, the hero itself takes an error tone with a `Retry sync` primary + `Diagnose` secondary. Remove the separate `FailureSummary` card. |
| Empty | Dashboard's `LedgerSummary` shows "No workouts recorded yet"; History has its own empty state; Onboarding has its own strip. | Copy varies by surface; no consistent illustration or wording; empty state doesn't tell the user what to *do* next. | One shared `EmptyState` composable in the visual system (see `cycle-04-visual-system.md § 12`) that pairs one short line + one primary action. |
| Loading | Dashboard `LoadingDashboard`, History `HistoryEmptyStateSurface(loading = true)`, Diagnostics `LoadingDiagnostics`. | Inconsistent styling and label. Some show a circular indicator, some don't. | One shared skeleton pattern with a consistent "Reading …" line and a small progress indicator (see visual-system doc). |
| Success | Concept A is the only surface today that shifts the hero into a subtle celebratory tone via a color wash. Production Dashboard reuses the same status border colors. | Success does not feel like success. GymRats-availability is treated as neutral evidence, not a satisfying end-of-flow. | Treat "Confirmed in Health Connect" as the actual success moment on Home; add a small "GymRats can import this now" note with a link to open GymRats. |

## 5. Cross-cutting problems (short list)

Repeated observations from § 3, distilled:

1. **Everything is a rectangle at the same altitude.** Hierarchy is carried by borders, not by size, weight, spacing, or elevation.
2. **The vocabulary of engineering leaks into every screen.** Eyebrows (`LIVE PRODUCT STATE`, `SANITIZED FAILURE SUMMARY`, `EVIDENCE-BASED CONNECTIONS`) are truth-preserving but user-hostile.
3. **Diagnostics is a top-level tab.** It should not be. It is a support tool that displaces real product surfaces.
4. **Preview-only screens hold primary real estate.** Automation and AI Assistant each own a bottom-bar entry (via More) despite having no working feature.
5. **Sync now, Pipeline and Dashboard duplicate each other.** All three show the same coordinator phase in the same rail form.
6. **Nothing has a real identity.** Every third-party service is a monogram tile — even the ones with permissible logo use.
7. **Copy repeats itself across sections.** GymRats-availability, Health Connect status, and phase labels each appear on 3–4 screens.
8. **No chart today anywhere.** Even honest, evidence-only charts (workouts/week, success rate) are absent.
9. **Motion is one-note.** Cycle 1 gave the rail a signature; nothing else has one. No list-to-detail transition, no state-swap on the hero, no success moment.
10. **Onboarding does not actually onboard.** Both buttons trigger the same `onSync` + Dashboard navigation; no permission is granted, no first-sync is walked through.

Each of these is addressed by one or more of the follow-up Cycle 4 docs; the crosswalk is in `cycle-04-implementation-roadmap.md § 3`.
