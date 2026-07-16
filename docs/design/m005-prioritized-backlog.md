# M005 · Prioritized Design Backlog

Derived from `m005-design-audit.md` and `m005-design-directions.md`. Each item cites the audit finding it closes, the screenshot(s) it maps to (see `docs/design/m005-screenshots/`), the source file(s) it would touch, and rough effort.

## P0 — Objective usability, accessibility, or honesty problems

These must be closed regardless of which design direction we pick. They are not aesthetic; they are the app not doing what it says it does.

| # | Item | Closes | Screens | Files (proposed, not implemented) | Effort |
|---|---|---|---|---|---|
| P0-01 | **Wire the Dashboard `Sync now` FAB to open the sync visualization.** Either open `SyncNowModal` on tap or, if the current no-modal design is intentional, show progress inline on Dashboard so the user sees state change. | F-C1 | 01a-d | `DashboardScreen.kt`, `HuaweiSyncRoot.kt`, `HuaweiSyncNavigationState.kt` | 1 dev day |
| P0-02 | **Bundle Archivo (400/600/800) and JetBrains Mono (400/600) as local `res/font/` assets.** Point `ProductSans` / `TechnicalMono` at the bundled families instead of `FontFamily.SansSerif` / `FontFamily.Monospace`. Ship SIL OFL licenses. | F-T1, F-T2 | all | `app/src/main/res/font/`, `Type.kt`, `app/build.gradle.kts` (Google Fonts downloadable-fonts is one option; local bundling is safer for a sideloaded app that may run offline first-time) | 0.5 day + license diligence |
| P0-03 | **Fix FAB clipping the last content card.** Raise `DashboardScreen.kt` LazyColumn bottom-padding from 112dp to ≥ 144dp, or move the FAB into a `Scaffold(floatingActionButton = …)` slot. | F-S1 | 01a-d | `DashboardScreen.kt`, `HuaweiSyncRoot.kt` | 0.25 day |
| P0-04 | **Rewrite the two sync-truth headlines to close the loop with the user's mental model:** Dashboard hero body: `A matching deterministic record was read back from Health Connect.` → `Confirmed in Health Connect. GymRats can import it on next open.` GymRats service row: `READY FOR GYMRATS TO READ` → `Available for GymRats to import` (or `Waiting for GymRats to pick up`). | F-CO1, F-CO2 | 01a-d, 05 | `DashboardScreen.kt` (`supportingText` map), `IntegrationsScreen.kt`, `ProductSyncState.kt` labels | 0.5 day (copy) |
| P0-05 | **Delete the `08 PROVIDERS` claim on Integrations, or bind it to the actual provider count.** Two providers render at runtime; the number is prototype ambition. | F-CO3 | 05 | `IntegrationsScreen.kt` | 0.25 day |
| P0-06 | **Rename `Integrations` OR `Settings` consistently — one label for one destination.** Recommendation: keep `Integrations` everywhere (it is the accurate name) and delete the `SETTINGS` relabel in the More menu. | F-N1 | 04 | `HuaweiSyncRoot.kt` line 356 | 0.1 day |
| P0-07 | **Raise `ink3` on dark from 38% → 52% alpha** so `bodySmall` disclaimers hit 4.5:1 AA. Retire `ink4` for text roles (leave for decorative lines only). | F-A2 | all dark | `Color.kt` | 0.25 day |
| P0-08 | **De-duplicate the FAB's a11y semantics.** Current `contentDescription = "Sync now"` + `stateDescription = "Sync complete"` reads as `Sync now, Sync complete, Button` on TalkBack. Change to a single `contentDescription` that flexes by state (`Sync now`, `Sync in progress`, `Synced — sync again`). | F-A3 | all | `ModernistComponents.kt` `SyncFab` | 0.25 day |
| P0-09 | **Center the empty-state card in History** using `Column(verticalArrangement = Arrangement.Center, modifier = fillMaxSize)` or an equivalent. Currently floats at ~60% height. | F-E2, F-S3 | 13b | `HistoryScreen.kt` | 0.25 day |
| P0-10 | **Surface the `SANITIZED FAILURE SUMMARY` card at the top of Dashboard when non-null**, not buried at the bottom. Failure must not scroll below the fold. | F-E4 | (not captured — failure state) | `DashboardScreen.kt` (reorder LazyColumn items) | 0.25 day |

**P0 total effort:** ~3.5 dev days. All are pre-conditions for any of the three design directions.

---

## P1 — High-impact visual improvements

Ship these to make the app read as polished. Each aligns with all three directions unless noted.

| # | Item | Closes | Screens | Files | Effort |
|---|---|---|---|---|---|
| P1-01 | **Reduce eyebrow use to one per screen, at the page top.** Section headers stop leading with `DURABLE LEDGER`, `CURRENT FACTS`, `HEALTH CONNECT READBACK`, etc. | F-H2, F-T5 | all | `SectionHeader.kt` — new `eyebrow` param drops to null default; call-sites updated | 1 day |
| P1-02 | **Raise Dashboard page title from `titleMedium` (15sp) to `headlineLarge` (28sp) or a new `pageTitle` role.** The top bar becomes a real page heading, not a browser tab. | F-H1 | 01a-d | `DashboardScreen.kt` `DashboardTopBar`, `Type.kt` (add `pageTitle`) | 0.5 day |
| P1-03 | **Kill the redundant `DARK` toggle on Onboarding.** One toggle in the app (Dashboard, or better: move theme selection to a Settings section under Integrations). | F-C3 | 09 | `OnboardingScreen.kt` | 0.25 day |
| P1-04 | **Downgrade the `DARK` toggle on Dashboard to an icon-only affordance** or move to a Settings surface. It is currently the same weight as a primary action. | F-C3 | 01a-d | `DashboardScreen.kt` `DashboardTopBar` | 0.25 day |
| P1-05 | **Unify accent color to a single ink-safe value** (`#DE2A0E` per Direction A). Retire `accentForeground` split. FAB, selected tab, primary CTA all read as the same red. | F-CL2 | all | `Color.kt`, `Theme.kt` | 0.5 day + regression pass |
| P1-06 | **Introduce one success color** (`#1D8A3D` light / `#4ADE80` dark) and use it consistently across `Recent activity` ok state, `Readback confirmed`, `VERIFIED` chip, and any future "green" surfaces. | F-CL3 | all | `Color.kt`, chip/label components | 0.5 day |
| P1-07 | **Add one acknowledgement moment on sync completion.** Border-flash + phase eyebrow crossfade + optional soft haptic. Respects reduced-motion. | F-M2, F-A4 | 01a-d | `DashboardScreen.kt`, `MotionPolicy.kt` | 1 day |
| P1-08 | **Collapse the SHA-256 client-record ID by default** on History and Dashboard. Chevron reveals full hash. Diagnostics keeps it expanded. | F-T6 | 03, 01a-d | `HistoryScreen.kt`, `Dashboard*` | 0.5 day |
| P1-09 | **Shrink the wide-nav rail from 224dp to ~180-200dp** and move `Setup` out of the footer (Setup is not a permanent destination). Sync now button stays. | F-S4 | 12 | `HuaweiSyncRoot.kt` `WidePrimaryNavigation` | 0.5 day |
| P1-10 | **Replace the compact More modal with a small drag-up bottom sheet** with iconized rows and short subtitles. Items are no longer full-width red buttons. | F-N4 | 04 | `HuaweiSyncRoot.kt` `CompactMoreMenu`, new sheet component | 1.5 days |
| P1-11 | **Increase Dashboard section spacing from `xl` (24dp) to `xxl` (32dp) and invert card inner/outer padding ratio** (outer 16dp, inner 24dp). Cards breathe. | F-S2 | 01a-d | `DashboardScreen.kt`, `ModernistSurface.kt` | 0.5 day |
| P1-12 | **Add safe-area padding to Onboarding CTAs** — currently render against the gesture bar. | F-S5 | 09 | `OnboardingScreen.kt` | 0.25 day |

**P1 total effort:** ~7 dev days. Combined with P0: ~10.5 dev days for the "Refinement" direction (Direction A) shippable outcome.

---

## P2 — Experiments and refinements

Interesting-but-optional work. Do these after P0/P1 lands. Some are direction-specific.

| # | Item | Applies to | Rationale | Effort |
|---|---|---|---|---|
| P2-01 | Collapse compact bottom-nav from 4 items → 3 (Dashboard / History / More) and make Pipeline a Dashboard drawer. | A, B | Compact IA today mixes daily-driver destinations with proof destinations. | 2 days |
| P2-02 | Replace `Card` container for tabular data (Connected services, Ledger metrics) with `Table` primitive: label flush-left mono, value flush-right, hair rule between rows. | B (identity), A (optional) | Right primitive for a ledger-first product. | 2 days |
| P2-03 | Add a weekly workout strip on Dashboard (7 columns, one per day, fill per synced workout). | C | Turns passive ledger into a daily-open surface. | 3 days |
| P2-04 | Add a monthly verification calendar on History (dot per day, green/red/empty). | C | Same rationale as P2-03 for History. | 3 days |
| P2-05 | Hero band on Dashboard (100dp dark-red block with H mark + status + big serif number). | C | The identity moment for Direction C. | 2 days |
| P2-06 | Add a small horizontal timeline chart on Activity detail (`written → accepted → readback → last update`). | B, C | Turns a fact list into a story. | 1 day |
| P2-07 | Add haptic feedback to sync-start / sync-complete / permission-granted. | all | Utility whose feedback often happens eye-off-phone. | 0.5 day |
| P2-08 | Ship a per-provider status color scheme (HC red-active, GR blue-outline, HW dashed-outline) that is stateful — provider shows disconnected via dashed line, live via solid. | all | Current Integrations diagram implies HW is disabled because of fill/outline mix. | 1 day |
| P2-09 | Localize eyebrow / status labels so they honor system locale (currently English uppercase in code). | all | Portuguese / Spanish uppercase looks aggressive; may need sentence-case fallback per locale. | 1 day + strings review |
| P2-10 | Replace stock Material bottom-nav icons with a bespoke 2px-stroke SVG set that echoes the 2dp rule weight from the prototype. | A, C | Restores the prototype's iconographic voice. | 2 days |
| P2-11 | Add a "See how we know" text link on Dashboard completion state that opens Diagnostics filtered to the most-recent sync. | all | Preserves the "prove it to me" drawer story. | 0.5 day |
| P2-12 | Bring back the Onboarding orbit's actual concentric-ring rotation from the prototype (`hs-orbit` 40s / `hs-orbit-rev` 24s equivalents in Compose). | A, C | The one place the prototype's motion identity is worth restoring literally. | 1 day |
| P2-13 | Introduce Instrument Serif Italic (or free alternative) as a display-accent font for numeric hero moments only. | C only | The Direction C identity move. | 0.5 day font work + 1 day rollout |
| P2-14 | Progressive disclosure on the Automation and AI Assistant preview screens: introduce an explicit `Preview` overlay label at the top of each screen (persistent, not just an eyebrow) so no one mistakes them for shipped features. | all | Automation and Assistant look shipped enough that a user might try to enable them and get confused. | 0.5 day |
| P2-15 | Tune the light-theme palette to solve the "hero card blends into background" issue: either raise the hero card's contrast (surface2 → surface3) or drop the card and use a horizontal rule above/below. | all | Follow-on for F-CL4 once P1-05 lands. | 0.5 day |

**P2 total effort:** ~20+ dev days, but P2 is opt-in per direction.

---

## Rejected — ideas incompatible with product or architecture

Recording the ideas we *don't* do, and why. These are the tempting moves someone will re-suggest in 3 weeks.

| # | Rejected item | Why not |
|---|---|---|
| R-01 | Move the client-record ID off the app entirely, replace with a friendly name. | Deterministic identity is the moat. Users of a personal sideloaded utility want to see the hash — that is why they picked this app over a black-box sync service. Collapse it (P1-08), don't delete it. |
| R-02 | Add a real dashboard of workouts synced (line chart of workouts/week, cumulative counts). | Feature scope past Gate 4. Requires reading fields we don't sync yet (dates over time as a series, categories). Would also invert the app's identity from "utility" to "log viewer" without user demand evidence. |
| R-03 | Auto-open Health Connect when app launches without permissions. | Auto-launching a system dialog on cold start is hostile. Current pattern (permission-required hero card with explicit `REVIEW PERMISSION` button) is correct. |
| R-04 | Replace the Modernist zero-radius rectangles with rounded Material 3 shapes. | Would erase the app's most distinctive stance. Every direction explicitly preserves zero-radius. |
| R-05 | Ship a light green "success" hero background on Dashboard once verified. | Overpromises. Health Connect confirmation is a necessary step; GymRats confirmation is unknown until Gate 2. Green hero implies delivered. |
| R-06 | Introduce a chat-style AI assistant with a remote LLM. | Product mission is local-only, deterministic. AI Assistant screen explicitly promises `no network`. Reversing this is a mission change, not a design change. |
| R-07 | Move Diagnostics into a hidden dev menu. | Diagnostics is the trust surface for personal sideloaded apps. It stays reachable in ≤2 taps. |
| R-08 | Introduce marketing-copy CTAs like "Sync now to see your gains!" | Wrong voice. The app's voice is precise and honest. Cheer-copy would break it. |
| R-09 | Replace the Sync FAB with a Material 3 `ExtendedFloatingActionButton` labeled "Sync now" always visible. | Extended FAB is 56dp tall + label; on a small phone with a bottom nav it eats the visual budget of the last card *worse* than the current FAB. If we go inline (Direction A/B), we go all the way. |
| R-10 | Add Onboarding steps 02-04 (there are only 2 CTAs today, but the header says `STEP 01/04`). | Direction A/B propose deleting the `STEP 01/04` claim instead. Adding fake steps to match invented copy would be dishonest. |
| R-11 | Introduce a "Sync history heatmap" (GitHub-style contribution grid). | Second-order AI-slop trope for fitness apps. If we do a calendar (P2-04), it is dot-per-day binary, not intensity-shaded. |
| R-12 | Store user-editable notes per synced workout. | Post-Gate 4 feature scope. Ledger is authoritative; editing it breaks reproducibility of the deterministic identity guarantees. |
| R-13 | Add a widget to the Android home screen. | Reasonable idea long-term, but requires a running sync scheduler, which is post-Gate 4 (WorkManager). Explicitly out of M005 scope. |
| R-14 | Make dark theme the default. | The Compose entry uses `isSystemInDarkTheme()` — that respects the user's system-wide choice. Overriding that would be prescriptive. |
| R-15 | Rename the app to something friendlier. | "Huawei Sync" is accurate. Boring is a feature for a personal utility. |

---

## Suggested execution order (recommendation)

If we accept the audit's recommendation (Direction A first, C later):

1. **M005a slice** (~4 dev days): P0-01, P0-02, P0-03, P0-04, P0-05, P0-06, P0-07, P0-08, P0-09, P0-10. Ship the "the app now does what it claims" pass.
2. **M005b slice** (~7 dev days): all P1 items. Ship the "Refinement" direction end-to-end.
3. **M005c slice or M006** (choose your own adventure): P2 items grouped by direction. If we go Direction B, run P2-01, 02, 06, 09, 15. If we go Direction C, run P2-03, 04, 05, 07, 10, 12, 13.

Human decision required: **choose one of Directions A / B / C** (or explicitly pick a hybrid), then M005a is unblocked to start.

No code changes will be made until that decision is explicit.
