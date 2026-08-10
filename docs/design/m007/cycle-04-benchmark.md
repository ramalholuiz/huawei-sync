# Cycle 4 — Visual benchmark

**Cycle scope.** App-wide product research for Huawei Sync. This document collects only *transferable patterns* from official product surfaces of adjacent apps. Nothing here is copied. Nothing here becomes an asset.
**Role.** Mobile Product Researcher + Brand Asset Researcher (references only; assets live in `cycle-04-brand-assets.md`).
**Access date for every row.** 2026-07-17.
**Sources.** Official product pages, official developer / documentation pages, official app-store listings, official brand kits. No blogs, no Pinterest, no Icons8, no third-party mirrors. When only a promotional / marketing surface exists, we call that out — those references teach positioning, not visual patterns to copy.

## 1. Reference classification

- **REF** — visual reference only. Nothing enters the APK.
- **BLOCKED** — must not be copied 1:1, imitated pixel-for-pixel, or shipped.
- **PATTERN** — a transferable interaction / composition pattern (not a look). These are the only rows that inform the visual system and directions.

Every row is one of the three. A row that only says "looks nice" is not enough — we record what the pattern *does* and what we would refuse to copy.

## 2. Categories and shortlist

Cycle 3 covered Dashboard-shaped references (Apple Health, WHOOP, Linear, Strava, Vercel, Zapier, Home Assistant). Cycle 4 explicitly does **not** repeat those rows verbatim — it broadens the sweep into the shapes the app actually has: history lists, activity detail, integrations, onboarding, error recovery, and honest observability.

## 3. Full reference table

### 3.1 Health platforms and syncs (the closest peers)

| # | Product | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| H1 | Health Connect (Google) | https://health.google/health-connect-android/ | REF · PATTERN | Consent explainer + provider-permission chooser (Android system). Every third-party read/write shows one line per permission with a per-record `On/Off`. | Making system-level permissions explicit and revocable per data type. | Adopt a single "What Huawei Sync writes" sheet with one line per record type (today: only `ExerciseSession`) and a jump-out link to the Health Connect permission screen. | The system's utility-grey chrome. |
| H2 | Health Connect API docs — data types page | https://developer.android.com/reference/androidx/health/connect/client/records/ExerciseSessionRecord | REF | Authoritative field list for `ExerciseSessionRecord` and related types. | Prevents us from inventing fields when we design the History / Detail surfaces. | Every field we render on Activity Detail must appear here — if it doesn't, we cannot show it as fact. | N/A — this is a factual source, not a look. |
| H3 | Apple Health — Summary tab (product overview) | https://www.apple.com/health/ | REF · PATTERN | Card-per-topic vertical scroll. "Highlights" strip at the top. Each card is generously spaced, single-colour, and answers one question. | Preventing dashboard density from turning into a wall. | A single-column Home with fewer, larger cards, each answering one question ("What just synced?", "How's this week going?"). | The photographic hero — Apple can afford a hero image; we cannot. |
| H4 | Samsung Health — main screen product page | https://www.samsung.com/global/galaxy/apps/samsung-health/ | REF · PATTERN | Big-number-per-tile (steps, active minutes, sleep) in a two-column grid. Marketing surface, not the app itself. | Communicating a KPI at a glance. | On History, a two-tile summary strip (workouts this week, verified count) works well *if we only render numbers we can prove*. | Marketing-only imagery; per-metric colour theming (which mixes signals). |
| H5 | Huawei Health — feature overview | https://consumer.huawei.com/en/wearables/health/ | REF · BLOCKED | Marketing hero + segmented feature list. | N/A — nothing here is transferable to Huawei Sync's own product surface without implying endorsement. | Only reminds us what the source-of-truth app looks like so we do not accidentally imitate its ring-and-metric aesthetic. | Any Huawei art direction or branding. Huawei Sync must remain visually independent. |
| H6 | Garmin Connect — feature overview | https://www.garmin.com/en-US/garmin-connect/ | REF · PATTERN | Two-line row lists for each activity: title + duration + type icon; secondary line for stats. | Making history rows readable at a glance without turning them into a table. | On our History, promote workout title (or day+time until type is known) to primary and demote state to a small right-aligned chip. | The stat-per-column grid — we don't have most of those stats honestly yet. |
| H7 | Strava — activity list (feed) | https://www.strava.com/features | REF · PATTERN | Card per activity with map thumbnail, title, and stat bar. | Turning each row into an object rather than a log line. | The row-as-object pattern is right; the map thumbnail is not applicable (no GPS today). | Kudos / social affordances. Strava's palette (orange over dark). |
| H8 | Strava API — Activities endpoint | https://developers.strava.com/docs/reference/#api-Activities | REF | Documented field set that a third-party sync can populate. | Prevents us from inventing Strava fields for the future Strava flow. | Consulted only if / when the Strava secondary flow becomes real (after Gate 2). | N/A. |
| H9 | Fitbit — dashboard overview | https://www.fitbit.com/global/us/technology/health-metrics | REF | Metric-per-card with color-coded status pill (in range / low / high). | Communicating state via a pill without over-alarming. | State pill vocabulary maps onto our readback states (Verified / Pending / Attention). | Metric-per-color palette that leaks health verdicts we cannot make. |
| H10 | Oura — Home reference | https://ouraring.com/features | REF · PATTERN | Single-number-per-card with a soft brand gradient behind the number. | Elevating a single number so it reads as personal. | On History week-summary, the "one number, one label, one background wash" composition is transferable. | The proprietary readiness / sleep score visual language. |
| H11 | Gentler Streak — App Store listing | https://apps.apple.com/us/app/gentler-streak-workout-tracker/id1554772045 | REF · PATTERN | Warm, calm tone; "How are you feeling?" prompts instead of raw KPIs. | Signalling wellness intent without turning the app into a chart. | Directions A copy voice — quieter, human, not celebratory. | Character illustrations (we ship no mascot). |
| H12 | HealthFit — App Store listing | https://apps.apple.com/us/app/healthfit/id1202650514 | REF · PATTERN | Utility app with dense list of past workouts, per-row source-of-truth badge. | Making the "which app / which watch produced this row" fact readable. | On History, a small `SOURCE` chip next to each row (Huawei Health today; future providers later). | HealthFit's system-grey utility look. |

### 3.2 Cross-service sync / connector products (the shape closest to our IA)

| # | Product | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| S1 | Zapier — "How it works" | https://zapier.com/how-it-works | REF · PATTERN | Source → transformation → destination composition rendered as three named tiles with an animated connector. | Making the *flow* the primary visual metaphor. | Home hero as a source → destination row with real state per node (Huawei → Sync → Health Connect → GymRats). Small — not the whole screen. | Zapier's app-catalogue grid; naming Huawei Sync a "Zap". |
| S2 | Zapier — Task history detail | https://help.zapier.com/hc/en-us/articles/8496053689101 | REF · PATTERN | Per-run detail page with a stepper of stages and per-stage input/output. | Making a debug-friendly run detail without engineering vocabulary. | Activity detail lifecycle timeline (already close to this). | Zapier's inspector tone. |
| S3 | Home Assistant — Integrations page | https://www.home-assistant.io/integrations/ | REF · PATTERN | Grid of provider tiles with real logos; each tile opens a device-and-services detail sheet. | Making a "connections" surface where each tile is a real integration, not a label. | The **tile → sheet** interaction on our Connections screen. Sheets keep the top level browsable. | The maintainer-tool aesthetic (grey chrome, JSON-style detail). |
| S4 | Home Assistant — Add integration flow | https://www.home-assistant.io/docs/installation/ | REF · PATTERN | Wizard steps with an evidence-based readiness check per step. | Making setup honest — each step tells the user what evidence is required. | Onboarding as 2–3 steps (see below for Health Connect / Health Connect permission / first sync), each with a proof line. | The utility wizard chrome. |
| S5 | IFTTT — Applet detail | https://ifttt.com/explore | REF · BLOCKED | Marketing surface for their applet catalogue. | N/A — pattern is already covered by Zapier row. | — | The paid tiering scaffolding. |
| S6 | 1Password Connect / integrations | https://developer.1password.com/docs/connect/ | REF · PATTERN | Per-integration status card with `Connected`, `Needs re-auth`, `Error` states and a one-line reason. | Making integration status readable in one card. | Connections screen card layout: title, one-line state, one-line reason, primary contextual action. | The developer-console styling. |

### 3.3 Deployment / observability dashboards (for honest per-run detail)

| # | Product | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| O1 | Vercel — deployment detail | https://vercel.com/docs/deployments | REF · PATTERN | Header row with commit + status; body with a stepper of build stages; per-step logs behind a disclosure. | Rendering a lifecycle with facts, not spinners. | The stepper + optional per-step disclosure on Activity Detail. | The developer log viewer. |
| O2 | GitHub — Actions run summary | https://docs.github.com/en/actions/monitoring-and-troubleshooting-workflows/monitoring-workflows | REF · PATTERN | Colour-per-status + per-step timing on a single vertical rail. | Turning many small stages into a scannable single column. | Activity detail timeline (already close). | The developer-only microcopy. |
| O3 | Sentry — issue detail | https://docs.sentry.io/product/issues/ | REF · PATTERN | Persistent header (title, level, first seen, last seen) + tabs for stacktrace / events / breadcrumbs. | Keeping the identity visible while the user drills into events. | Activity Detail identity band kept sticky as the user scrolls into the lifecycle. | Developer tone. |
| O4 | Linear — issue view | https://linear.app/method | REF · PATTERN | Sidebar of properties + main body of activity. Every property has a value or an explicit `–`. | Making "unknown yet" a first-class value rather than absent chrome. | On Activity Detail, unknown facts get an explicit `Not yet known — waiting for readback`, not a missing line. | Sidebar patterns don't apply on mobile. |
| O5 | Grafana — panel description | https://grafana.com/docs/grafana/latest/panels-visualizations/ | REF | Documented panel types (stat, sparkline, bar, heatmap). | Prevents us from inventing chart types. | Chart shortlist (`cycle-04-benchmark.md § 4`) chosen from stat / bar / sparkline only. | Any operator-console aesthetic. |

### 3.4 Personal dashboards / notion-style productivity (for calm typography)

| # | Product | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| P1 | Notion — home / today view | https://www.notion.so/help/customize-and-manage-views | REF · PATTERN | Left-aligned large heading, small subtitle, list of blocks. Very calm. | Bringing calm typography into a data surface. | Home eyebrow → title → supporting text hierarchy on Direction A. | Any Notion iconography. |
| P2 | Todoist — Today view | https://todoist.com/features | REF · PATTERN | One primary count ("3 tasks today") + a list. | Reducing dashboard clutter to one number. | Home summary strip: "1 workout confirmed today" + a small verb ("Sync") button. | Todoist's category colours. |
| P3 | Things 3 — Today view (App Store) | https://apps.apple.com/us/app/things-3/id904280696 | REF · PATTERN | Row height, generous top padding, per-day dividers with a soft label. | Making a list feel less like a table. | On History, use soft day separators (small caps + hairline of very low alpha) instead of the current uppercase pill. | Playful stylistic flourishes (paper corner, etc.). |

### 3.5 Design system baselines (for tokens, motion, accessibility)

| # | Source | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| D1 | Material 3 — colour roles | https://m3.material.io/styles/color/roles | REF | Documented tonal roles (`surface`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, `surfaceContainerHighest`). | Depth without shadows. | Direct baseline for `cycle-04-visual-system.md § 4` (elevation & tonal surfaces). | The default M3 palette hues; the Material chip look. |
| D2 | Material 3 — shape tokens | https://m3.material.io/styles/shape/overview | REF | Documented corner radius scale (`extra-small` 4, `small` 8, `medium` 12, `large` 16, `extra-large` 28, `full`). | A ready-made grammar for corners. | Baseline for `cycle-04-visual-system.md § 2` (shapes and radii). | The default `Shapes()` mapping. |
| D3 | Material 3 — motion | https://m3.material.io/styles/motion/overview | REF | Documented duration and easing families (short/medium/long × emphasized/standard/decelerate/accelerate). | A shared vocabulary the whole app can use. | Direct input to `cycle-04-motion-system.md § 2` (durations and easings). | Any single "signature curve" that turns Huawei Sync into a stock M3 app. |
| D4 | Material 3 — typography | https://m3.material.io/styles/typography/type-scale-tokens | REF | Documented type scale (display / headline / title / body / label × sm/md/lg). | Prevents typography sprawl. | Baseline for `cycle-04-visual-system.md § 6`. | Material's default font pairing. |
| D5 | WCAG 2.2 — success criteria | https://www.w3.org/TR/WCAG22/ | REF | Contrast, target size, motion, focus rules. | Non-negotiable accessibility floor. | Applied to every direction and to the visual system. | N/A — this is a standard. |
| D6 | WCAG — Animation from interactions | https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html | REF | Reduced-motion requirement. | Enforces the `HuaweiSyncMotionPolicy.Reduced` guard we already have. | Motion system doc adopts explicit reduced-motion branches per component. | N/A. |
| D7 | Apple HIG — Motion | https://developer.apple.com/design/human-interface-guidelines/motion | REF | Purpose-driven motion guidance ("motion clarifies, motion doesn't decorate"). | Confirms we should not decorate. | Explicit rule in motion doc: no idle motion. | Any iOS-specific idioms. |
| D8 | Compose canvas & drawing | https://developer.android.com/develop/ui/compose/graphics/draw/modifiers | REF | Native drawing APIs. | Prevents introducing Rive/Lottie for anything drawable. | Confirms directions can ship without new deps. | N/A. |

### 3.6 Illustrations, imagery and iconography (what we *could* use without brand hijack)

| # | Source | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| I1 | Material Symbols (Rounded) | https://fonts.google.com/icons | REF · PATTERN | The rounded weight of the Material Symbols family. | A single visually-coherent icon set already shipping via `androidx.compose.material:material-icons-extended`. | Standardise on `Rounded` weight app-wide; forbid mixing with `Filled` or `Outlined` in the same product surface. | Random icon-per-screen choices we currently have (`Home`, `Timeline`, `Settings`, `Info`, `Build`, `History`, `List`). |
| I2 | Material Symbols variable-font info | https://developers.google.com/fonts/docs/material_symbols | REF | Variable-axis metadata (weight, grade, optical size). | Lets us tune icon weight to match the visual system without shipping multiple fonts. | Fixed weight/grade choice per direction. | Runtime axis animation (too clever). |
| I3 | Lucide — open-source icon set | https://lucide.dev | REF | 1000+ open-source (ISC-licensed) icons with a consistent line weight. | Backup if a specific product idea has no Material Symbol. | Reference only — Huawei Sync stays on Material Symbols today. | N/A. |
| I4 | Undraw — open illustrations (MIT) | https://undraw.co/license | REF · BLOCKED | Free customisable illustrations. | Would be a shortcut, but the aesthetic is unmistakable and would strip Huawei Sync of identity. | Do **not** ship any Undraw illustration in the APK. | Everything. |
| I5 | Google Photos "moments" API concept (design language) | https://blog.google/products/photos/ | REF · BLOCKED | Warm brand illustrations. | N/A. | — | Direct use. |
| I6 | Original abstract watch → phone motif | (to be designed in-house) | REF | A single line-drawn illustration for Onboarding hero. | Replacing the current H/HC/APPS orbit. | Directions A / C will call for this; production is out of Cycle 4 scope. | Any recognisable third-party watch. |

### 3.7 Empty / error / loading (state patterns)

| # | Source | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| E1 | Superhuman — "Inbox zero" empty state | https://blog.superhuman.com/inbox-zero/ | REF · PATTERN | Empty state as a compliment, not a void. | Making empty a milestone. | On History empty: "Sync when your watch is nearby — this is where confirmed workouts will land". | The Superhuman brand voice. |
| E2 | GitHub — 404 pattern | https://github.com/404 | REF · PATTERN | Playful but sober error page with a clear primary action. | Making errors readable at a glance. | Sync failure hero: single sentence + primary `Retry` + secondary `Diagnose`. | The octocat mascot. |
| E3 | Skeleton loading — Facebook engineering write-up | https://engineering.fb.com/2016/02/22/android/facebook-tools-open-source-android-shimmer/ | REF · PATTERN | Skeleton blocks that match the shape of the loaded content. | Preventing spinner-only loading states. | Home loading state: skeleton blocks in the shape of the hero + summary cards; History loading: skeleton rows. Deferred to the visual-system doc; not implemented this cycle. | Facebook's shimmer animation (imported dependency; not needed). |

### 3.8 Onboarding patterns (personal-first)

| # | Source | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| N1 | Signal — onboarding | https://signal.org/download/android/ | REF · PATTERN | Two-step onboarding (privacy explainer + permission) with real Android system intent as step 2. | Turning permission requests into an inline step, not a modal. | Split Onboarding into two steps: mission + permission-grant intent. Both live inside the onboarding flow. | The messaging-app tone. |
| N2 | Pocket Casts — onboarding | https://support.pocketcasts.com/knowledge-base/getting-started-with-pocket-casts/ | REF · PATTERN | Short skippable pages introducing one concept each. | Not overwhelming new users. | Onboarding as at most two full pages (mission → permission), never a carousel. | Multiple pages of promotion. |

### 3.9 Brand pages consulted for logo *usage rules* (asset details in `cycle-04-brand-assets.md`)

| # | Source | URL | Class | Screen / pattern observed | Problem it solves | What can inspire | What we will NOT copy |
|---|---|---|---|---|---|---|---|
| B1 | Health Connect brand guidelines | https://developer.android.com/health-and-fitness/guides/health-connect/develop/build-brand-experience | REF | Official rules for referring to "Health Connect" and using its icon in third-party apps. | Prevents us from misusing the mark. | Direct source of truth for Connections + Onboarding usage. | Any usage outside the documented rules. |
| B2 | Google brand guidance | https://about.google/brand-resource-center/ | REF | Rules for referring to Google products. | Prevents "Google Fit" from being used casually. | Only mention Google Fit in copy; do not carry the mark until legal review. | Any use of the Google logo. |
| B3 | Strava brand guidelines | https://developers.strava.com/guidelines/ | REF | Explicit rules on Strava logo usage and "Powered by Strava". | Prevents misuse if / when the Strava flow ships. | Reserve mark for post-Gate 2 use. | Any pre-authorisation shipping. |
| B4 | Fitbit brand assets policy | https://web.developer.fitbit.com/community-terms/ | REF | Rules for Fitbit developer terms and asset usage. | Prevents shipping the Fitbit mark. | Text mention only. | Any logo use without permission. |
| B5 | Samsung Health developer program | https://developer.samsung.com/health | REF | Documentation for the Samsung Health SDK / developer terms. | Confirms Samsung Health is post-approval territory. | Reference only. | Logo usage. |
| B6 | Garmin developer program terms | https://developer.garmin.com/gc-developer-program/ | REF | Documentation for Garmin developer program. | Confirms Garmin Connect is post-approval territory. | Reference only. | Logo usage. |
| B7 | Oura brand guidelines | https://ouraring.com/blog/press | REF · BLOCKED | Press page — no developer guidelines discovered from public sources. | Documents lack of clear usage rules. | Text mention only. | Any logo use. |
| B8 | WHOOP brand | https://www.whoop.com/us/en/press/ | REF · BLOCKED | Press page — no permissive developer usage discovered. | Same as Oura. | Text mention only. | Any logo use. |
| B9 | Huawei brand guidelines | https://consumer.huawei.com/en/support/brand/ | REF · BLOCKED | Prohibits third-party use of Huawei's marks outside authorised programs. | Reinforces "Huawei Sync must own its own visual identity". | No Huawei logo inside the app. | The Huawei mark, colours, or wordmark. |
| B10 | GymRats — public app pages | https://gymrats.app | REF · BLOCKED | Product website; no developer / brand kit surfaced. | Confirms no green-lit third-party use of the GymRats mark. | Text mention only. | Any use of the GymRats logo. |

**Full brand-asset detail** — usage rights, restrictions, `production-approved: yes/no`, per-provider decision — lives in `cycle-04-brand-assets.md`. This benchmark table only records the *page consulted*, so we can trace each decision back to the source.

## 4. Honest-chart shortlist (fed into per-screen doc)

Cycle 4 audits which charts are **honest with the P0 data we already have**, given Gate 1 has passed and Gate 3 has not.

| Chart | Data source | Honest today | Honest post-Gate 3 | Notes |
|---|---|---|---|---|
| Workouts per week (bar) | Ledger count over 7-day windows | Yes | Yes | Sits at the top of Activity / History. Use uniform bars, no scale labels smaller than 12sp. |
| Sync success rate (bar or stat tile) | `verified` / `attempted` from the ledger | Yes | Yes | Sits on the Home summary strip. Renders `100 %` when the sample is 0/0, so include a `no data yet` state. |
| Failures over time (sparkline) | Ledger rows with a safe error code | Yes | Yes | Useful on Diagnostics; not on Home. |
| Time-to-confirmation (stat tile) | `confirmedAt − acceptedAt` avg from ledger | Yes | Yes | Useful on Diagnostics only — a user does not care about milliseconds. |
| Duplication avoided count (stat tile) | Not currently persisted; would need a counter | No | No (would need a new fact) | Reject for MVP. Do not fabricate. |
| Workout-type distribution (donut) | Requires Huawei Health workout type | No | Yes | Belongs on History post-Gate 3. |
| Calories / heart-rate / distance (any chart) | Not read today | No | Only if Gate 3 confirms availability | Explicitly forbidden by the mission and confirmed here. |
| Sleep / SpO2 / body-comp | Not in scope | No | No | Never in MVP. |

The visual specification for the honest charts lives in `cycle-04-visual-system.md § 11`. The screen placement lives in `cycle-04-screen-redesign.md`.

## 5. Patterns that appear in > 3 references (the ones that repeat = the ones to prioritise)

Cross-tabulating § 3, five patterns recur:

1. **Source → destination row-of-tiles** (S1, S2, H3, O1). Direction C's topology already models this; Cycle 4 keeps it small (Home hero only).
2. **Provider tile → detail sheet** (S3, S6, H1). Adopted for Connections screen in `cycle-04-directions.md`.
3. **Lifecycle stepper with per-step evidence** (O1, O2, S2, S6). Adopted for Activity Detail (we already have this; polish only).
4. **Empty state as a milestone, not a void** (E1, E2). Adopted for the shared `EmptyState` component in `cycle-04-visual-system.md § 12`.
5. **State pill next to a title (not above it)** (H9, H12, S6). Adopted on History rows.

## 6. What we intentionally did *not* research

To keep the sweep honest and this document usable, we intentionally excluded:

- **Gym / social apps.** Peloton, Strava social feed, Nike Run Club: the app is not a social product.
- **Wearable hardware marketing pages.** Apple Watch, Galaxy Watch, WHOOP band: the app is not selling a device.
- **AI-first assistants** (ChatGPT, Perplexity, Anthropic Claude): the Assistant surface is preview-only and being *removed* from the top-level nav; no need to import their aesthetic.
- **Growth / dashboards for creators** (Substack, Beehiiv, YouTube Studio): different problem shape.
- **Pinterest / Behance / Dribbble references.** All rejected by intake as unreliable sources; the intake said explicitly no.

## 7. What we cannot resolve without a decision

Two sources were consulted and require human judgment to close:

- **Health Connect brand usage.** The Health Connect brand-experience page (B1) permits the icon and name in third-party apps *when following the rules*. `cycle-04-brand-assets.md § 3` proposes we ship the Health Connect wordmark **and** icon in `Connections` and `Onboarding`, at documented sizes and with the required attribution. Needs human confirmation before we schedule the asset drop.
- **GymRats.** No developer program surfaced. `cycle-04-brand-assets.md § 3` proposes text-only usage until we reach out and receive written permission. Needs a "yes, keep text-only" or "yes, initiate the ask" decision.
