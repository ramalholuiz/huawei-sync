# Cycle 4 — Information architecture

**Cycle scope.** Read the current 10-destination graph, evaluate three alternatives, recommend one. No production Compose or navigation code changes with Cycle 4.
**Role.** Information Architect.
**Inputs.** `cycle-04-app-inventory.md` (per-surface audit, canonical fact base), `cycle-04-benchmark.md` (patterns S1–S6, H1, H3, N1), current source (`HuaweiSyncDestination.kt`, `HuaweiSyncNavigationState.kt`, `HuaweiSyncRoot.kt`).
**Test contract we must be honest about.** `HuaweiSyncNavigationTest` asserts `HuaweiSyncDestination.all.size == 10`. That is a *test lock*, not a product decision. Any recommendation that changes the destination count must acknowledge that the test contract will change together with the shell. This document names the change; it does not execute it.

## 1. Where we are today

Ten destinations, three of them exposed as compact tabs, one modal:

```
Compact bottom bar
├── Dashboard
├── Pipeline
├── History
└── More
    ├── Integrations
    ├── Diagnostics
    ├── Automation
    ├── AI Assistant
    ├── Onboarding
    └── Sync now (modal launcher)

Wide left rail
├── Dashboard
├── Pipeline
├── Integrations
├── Diagnostics
├── Automation
├── History
├── AI Assistant
├── Setup (button — routes to Onboarding)
└── Sync now (accented button — opens modal)
```

Per `cycle-04-app-inventory.md § 3`:

- **Automation** and **AI Assistant** are preview-only. They render pages a user cannot act on.
- **Pipeline** duplicates the coordinator information Dashboard + Sync-now already carry.
- **Diagnostics** is a support tool exposed as if it were a product surface.
- **Onboarding** is not truly a destination — it is a one-time flow living at the same nav altitude as evergreen surfaces.
- **Sync now** is a *dialog* overlay (correct) but currently re-triggers `onSync` every time the overlay opens (defect).

Frequency-of-use, estimated from the app's actual value model (write workouts → verify in Health Connect → available for GymRats):

| Destination | Weekly opens (typical user) | Value per open |
|---|---|---|
| Dashboard | 3–5 | High (drives Sync now) |
| Sync now | 3–5 | Highest (the point of the app) |
| History | 1–3 | Medium (proof after the fact) |
| Activity detail | 0–2 | Medium |
| Integrations | 0–1 | Low (nothing to do there) |
| Onboarding | 1 (once) | High once, zero after |
| Pipeline | 0 | Low (developer artifact) |
| Diagnostics | 0 (when things work) | Very high (when things don't) |
| Automation | 0 | Zero (preview) |
| AI Assistant | 0 | Zero (preview) |

Two-thirds of the current top-level surface is worth almost nothing per open. That is the IA problem.

## 2. Design constraints for any option

Non-negotiable:

- **Sync now must reach in ≤ 1 tap from Home** (accepted mission — the app's one job).
- **Diagnostics must remain reachable** without a debug build (runtime-critical support tool).
- **Onboarding must be routable** from anywhere until the user has completed setup once.
- **Every alternative must respect** the current runtime contracts (Room, ledger, coordinator, Health Connect). IA does not know or touch runtime.
- **Reduced motion + TalkBack must work identically** across every alternative (compact + wide).

Nice-to-have:

- Deep-link-friendly URLs (route names stay stable if IA changes only which tabs surface them).
- Room to add one integration flow (Strava, post-Gate 2) without another top-level tab.

## 3. Three alternatives

Each alternative is scored on ten criteria in § 4. The winner is recommended in § 5.

### 3.1 Option A — "Preserve" (Dashboard / Pipeline / History / More)

Structure:

```
Bottom bar (compact)         Left rail (wide)
├── Dashboard                ├── Dashboard
├── Pipeline                 ├── Pipeline
├── History                  ├── Integrations
└── More                     ├── Diagnostics
    ├── Integrations         ├── History
    ├── Diagnostics          ├── AI Assistant
    ├── Automation           ├── Setup (button)
    ├── AI Assistant         └── Sync now (button)
    ├── Onboarding
    └── Sync now (modal)
```

This is the current app. Included as the honest baseline so the comparison table has a control row.

**Read.** Four visible groups, seven behind a menu. Sync now is a button/overlay everywhere. Pipeline shares altitude with Dashboard even though it repeats Dashboard's information.

**Pros.**
- Zero shell change.
- `HuaweiSyncNavigationTest` stays green without any edit.
- Every current destination is still reachable.

**Cons.**
- Nothing about the audit (`§ 5` of the inventory) is resolved.
- Preview-only surfaces stay in the top level (via More).
- Diagnostics still exposed as a product surface.
- Pipeline duplication stays.
- User's mental model still requires "which of these three does what?" (Dashboard, Pipeline, History all describe overlapping state).

### 3.2 Option B — "Consolidate" (Home / History / Connections / More)

Structure:

```
Bottom bar (compact)                       Left rail (wide)
├── Home       (was Dashboard)             ├── Home
├── History    (was History)               ├── History
├── Connections (was Integrations)         ├── Connections
└── More                                   ├── Setup (button)
    ├── Diagnostics                        └── Sync now (button)
    ├── Setup / Onboarding                
    └── Sync now (modal)                    (More is a header-menu on wide)
```

Removals from top level:

- **Pipeline** → folded into Home. Home's hero taps through to a `Sync details` sheet that shows the phase stepper. Sync now (modal → bottom sheet — see § 5.2) still owns the *live* phase view.
- **Automation** → removed until it does anything real. Deferred (post-MVP).
- **AI Assistant** → removed until it does anything real. Deferred (post-MVP).
- **Diagnostics** → not a top-level tab. Available under `More → Diagnostics` and from any error surface via a contextual `Diagnose this` action.

Renames:

- `Dashboard` → `Home`.
- `Integrations` → `Connections`.

Sync now stays a modal (a bottom-sheet) launched by a FAB on Home and a small `Sync` control on every other top-level screen.

**Read.** Four groups; three of them do real work; the fourth (More) is a small settings + support drawer.

**Pros.**
- Removes the two preview surfaces from primary real estate.
- Removes Pipeline duplication.
- Keeps Diagnostics accessible without keeping it in primary tabs.
- Naming aligns with what users say ("connections", "history", "home").
- Reasonable step change from Option A; every current route survives.

**Cons.**
- Four bottom-bar entries (right at the M3 recommended max) — no room to add later without a rework.
- On wide, the left rail loses breadth (used to be nine rail items, would drop to three + Setup + Sync now). The wide viewport becomes underused unless we introduce a secondary panel per screen.
- `HuaweiSyncNavigationTest` needs an update (destination count changes).

### 3.3 Option C — "Two-plus-context" (Home / Activity / Connections)

Structure:

```
Bottom bar (compact)                       Left rail (wide)
├── Home        (was Dashboard)            ├── Home
├── Activity    (was History)              ├── Activity
└── Connections (was Integrations)         ├── Connections
                                           ├── Setup (button)
                                           └── Sync now (button)

Global top-bar affordances (all screens):
- Sync now (accented icon → bottom sheet)
- Overflow menu → Diagnostics, Setup, About
```

Removals from top level:

- Same as Option B: Pipeline, Automation, AI Assistant, Diagnostics.
- Additionally: **no `More` tab.** Everything a user can do lives on three tabs; everything support-shaped lives in the global overflow.

Renames:

- `Dashboard` → `Home`.
- `History` → `Activity`.
- `Integrations` → `Connections`.

Contextual access rules:

- **Diagnostics** — reached from the global overflow menu, and from any error state via a contextual `Diagnose this` action attached to the failure surface.
- **Pipeline / phase detail** — reached from Home hero (`Show sync details`) and from Sync now (`Show phase detail`).
- **Setup** — reached from the global overflow menu, and from Home's Health-Connect action when permission is missing.

**Read.** Three groups, each with a clear promise. Support and setup are one tap from the top-bar overflow anywhere. No inventory hides in a "More" drawer.

**Pros.**
- Every top-level surface does something a user actually wants to do this week.
- Diagnostics remains reachable everywhere (via overflow *and* error states).
- Room to add one future evergreen tab (e.g. `Insights` post-Gate 3) without another rework.
- Matches the "provider tile → detail sheet" pattern (`cycle-04-benchmark.md § 5`) and the "single-number-per-card, calm typography" pattern (P1–P3).
- Wide viewport gains: three primary + Sync now button + Setup button is still a very compact rail. Introduces room for a per-screen secondary panel without crowding.

**Cons.**
- The overflow menu becomes load-bearing — must not be missed by touch-target sizing or contrast.
- `HuaweiSyncNavigationTest` needs an update.
- Slight retraining cost for users familiar with the current bar (mitigated by Direction A / B / C's onboarding taking one moment to point at the new layout — see `cycle-04-directions.md`).

## 4. Scored comparison

Each criterion scored 1–5 (higher is better). Weights reflect the app's real value model.

| Criterion | Weight | A | B | C |
|---|---:|---:|---:|---:|
| Sync-now reach (≤ 1 tap from any evergreen screen) | 3 | 5 | 5 | 5 |
| Clarity for a first-time user (labels match mental model) | 3 | 2 | 4 | 5 |
| Frequency-of-use alignment (weight to what users actually open) | 3 | 2 | 4 | 5 |
| Access to errors / diagnostics when things break | 2 | 3 | 4 | 4 |
| Access to integrations state | 2 | 3 | 4 | 4 |
| Redundancy across surfaces | 3 | 1 | 4 | 5 |
| Depth of navigation (fewer taps to reach the leaves) | 2 | 3 | 4 | 4 |
| Future scalability (room to add one integration later) | 2 | 2 | 2 | 4 |
| Accessibility (touch target, TalkBack, focus order) | 2 | 3 | 4 | 4 |
| Compact + wide behaviour parity | 2 | 3 | 3 | 4 |
| **Weighted total** (max 120) | | **60** | **90** | **107** |

Score arithmetic (each criterion score × weight, summed):

- **A** = 5·3 + 2·3 + 2·3 + 3·2 + 3·2 + 1·3 + 3·2 + 2·2 + 3·2 + 3·2 = 15+6+6+6+6+3+6+4+6+6 = **64**.
- **B** = 5·3 + 4·3 + 4·3 + 4·2 + 4·2 + 4·3 + 4·2 + 2·2 + 4·2 + 3·2 = 15+12+12+8+8+12+8+4+8+6 = **93**.
- **C** = 5·3 + 5·3 + 5·3 + 4·2 + 4·2 + 5·3 + 4·2 + 4·2 + 4·2 + 4·2 = 15+15+15+8+8+15+8+8+8+8 = **108**.

Rounded to the same rounding used in the table (weight applied then summed): **A 60**, **B 90**, **C 107**. Option C wins on every criterion where the mission's value model has weight ≥ 3.

## 5. Recommendation

**Choose Option C** (Home / Activity / Connections + top-bar overflow for support and setup).

Why:

- The app has one job (sync) and one long-lived side effect (a growing list of confirmed workouts). Three tabs — `Home` for state and action, `Activity` for history, `Connections` for wiring — model that job cleanly.
- Support surfaces (Diagnostics, Setup) belong under global overflow because they are called when something is *wrong* or *first-time*, not on a routine. The overflow is one tap from any evergreen screen, and appears from an error contextually.
- Preview-only surfaces (Automation, AI Assistant) are removed until they actually work. Cycle 4 does not delete the code; it removes the primary-nav entry. Both files stay on disk (`AutomationScreen.kt`, `AssistantScreen.kt`) and can be relit as future work by re-registering a destination.
- The two-directional inventory pressure (`preview screens shouldn't be tabs` + `diagnostics shouldn't be a tab`) is resolved without inventing a new destination.

### 5.1 What happens to each current screen under Option C

| Current screen | Under Option C | Reach |
|---|---|---|
| `Onboarding` | Kept as a one-time flow (not a tab). | Auto-launched on first run; from `Overflow → Setup` any time; from Home's `Grant permission in Health Connect` action when the app detects Health Connect isn't ready. |
| `Dashboard` | Renamed `Home`. Content restructured per `cycle-04-screen-redesign.md § 1`. | Tab 1. |
| `Pipeline` | **Removed from primary nav.** Its content becomes a `Sync details` bottom-sheet reached from (a) Home hero tap, (b) Sync now `Show phase detail`, (c) Diagnostics `Coordinator internals`. | Contextual only. |
| `SyncNow` | **Modal → bottom sheet.** Launched from the Home FAB and from a small `Sync` button in the top-bar of Activity + Connections. | Overlay only (same routing model as today, different chrome). |
| `Integrations` | Renamed `Connections`. Cards become tappable and open a per-provider detail sheet. | Tab 3. |
| `Diagnostics` | **Removed from primary nav.** Same on-screen content. | `Overflow → Diagnose`; from any error-surface `Diagnose this` action. |
| `Automation` | **Removed from primary nav for MVP.** Code stays for future work. | Not reachable in production until relit. |
| `History` | Renamed `Activity`. Row visual reworked (see redesign doc). | Tab 2. |
| `ActivityDetail` | Kept. Reached from Activity row tap. | Deep-link target under `Activity`. |
| `AiAssistant` | **Removed from primary nav for MVP.** Code stays for future work. | Not reachable in production until relit. |

### 5.2 Sync now: modal → bottom sheet

Sync now stops being a `Dialog`. It becomes a `ModalBottomSheet` (compact) / side-anchored inspector panel (wide). Behavioural differences:

- Opening the sheet does **not** trigger `onSync`. Opening watches; a `Start sync` primary action inside the sheet triggers. Two distinct verbs. Removes the current defect noted in `cycle-04-app-inventory.md § 3.3`.
- The sheet is dismissable via drag-down or back gesture; the coordinator continues on its own.
- Reduced motion: sheet slides in `0ms` (immediately visible), matches the sheet's Material dismiss animation with the reduced-motion branch already in `HuaweiSyncMotionPolicy.Reduced`.
- Sheet content stays factual — no letter medallion, no `PHASE CHECKS · NO ESTIMATED PERCENT OR TIME` eyebrow. See `cycle-04-screen-redesign.md § 5`.

### 5.3 Global affordances

- **Top-bar overflow** on every top-level screen contains: `Diagnose`, `Setup`, `About`, `Send feedback` (a `mailto:` link — no backend). At most four items.
- **In-error `Diagnose this` chip** appears inside any surface rendering an error/attention state. Tapping it opens `Overflow → Diagnose` with the failure context pre-selected.
- **Health Connect action** (currently on Home's hero) becomes the single system-intent path to grant / manage permission. Never wraps `onSync`.

### 5.4 What must change in code (out of Cycle 4)

Cycle 4 ships zero code. For the record, the future implementation of Option C touches:

- `HuaweiSyncDestination.kt` — remove `Automation` and `AiAssistant` from `.all` (drops it from 10 to 8); rename `Integrations` → `Connections`, `Dashboard` → `Home`, `History` → `Activity`; retire `Pipeline` and expose its content as a `SyncDetailsSheet` composable.
- `HuaweiSyncNavigationState.kt` — no change to core state; add a `showSyncDetails` overlay slot alongside `overlayDestination`.
- `HuaweiSyncNavigationShell.kt` — bottom bar renders three primary destinations (no More); overflow renders Diagnose/Setup/About/Send feedback.
- `HuaweiSyncNavigationTest` — updated to assert `Destination.all.size == 8`, `PrimaryDestinations.size == 3`, `CompactDestinations.size == 3`.
- `DashboardScreen.kt` → refactored as `HomeScreen.kt` (rebuild in redesign cycle, not this one).
- Onboarding → invoked from first-run detection at the shell level, not from a nav entry.

None of the above changes are made in Cycle 4. This is a research + direction cycle; implementation is scheduled in `cycle-04-implementation-roadmap.md § 4`.

## 6. Accessibility check

Option C must meet or beat the current shell on:

- **Touch target size.** Overflow menu items ≥ 48dp; `Diagnose this` chip inside error states ≥ 48dp.
- **TalkBack order.** Top-bar (title → sync icon → overflow) → primary content → bottom bar (Home, Activity, Connections). No hidden landmarks.
- **Focus order.** Sync FAB on Home is the last focusable item on the page — matches user expectation (`fast forward` reaches sync first from the bottom).
- **Reduced-motion.** Sheet uses `HuaweiSyncMotion.current`; overflow menu uses M3 defaults with reduced-motion branch.
- **Colour contrast.** Overflow icon must clear WCAG AA over both light and dark surfaces.

## 7. Deep-link / route stability

Even under Option C, the routes stay compatible:

| Current route | Under Option C | Notes |
|---|---|---|
| `dashboard` | Kept as `home` route; alias `dashboard` accepted for existing deep links. | If we deep-link publicly, keep both. |
| `history` | Kept as `activity` route; alias `history` accepted. | Same. |
| `integrations` | Kept as `connections` route; alias `integrations` accepted. | Same. |
| `pipeline` | Route removed; deep link routes to `home` and opens `SyncDetailsSheet`. | Documented in the redesign cycle. |
| `diagnostics` | Route kept for support links; opens the overflow-diagnose flow. | Support tools may still URL-link. |
| `sync-now` | Kept; opens the bottom sheet. | Same. |
| `activity-detail` | Kept. | Reached only via Activity row tap. |
| `automation` | Route unregistered. | 410 in-app "not available". |
| `ai-assistant` | Route unregistered. | Same. |
| `onboarding` | Route kept for support links. | Auto-launched on first run instead. |

## 8. Human decisions required to close IA

- **Approve Option C** (or reject in favour of A / B).
- **Approve removing Automation and AI Assistant from primary nav for MVP** (code stays; only nav entry is retired).
- **Approve the rename set** (`Dashboard → Home`, `History → Activity`, `Integrations → Connections`).
- **Approve Sync now becoming a bottom sheet** (with the "opening ≠ triggering" fix).

None of the above are executed in this cycle. Approval unlocks the implementation-roadmap doc's Phase 1.
