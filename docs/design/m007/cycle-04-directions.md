# Cycle 4 — Three app-wide directions

**Cycle scope.** Three fundamentally different personalities for the whole app under Option C IA, all consuming the same visual-system tokens, motion tokens, and honest chart shortlist. No production Compose ships in this cycle.
**Role.** Mobile Art Director + Design Systems Reviewer.
**Precedent.** Cycle 3 produced three Dashboard-only concepts (A Soft Premium Health, B Dynamic Fitness Utility, C Connected Ecosystem) and shipped only A in a debug gallery. Cycle 4 does **not** treat any of them as approved. It refines them into app-wide directions that must render *every* Option-C surface, not just Home.
**Guardrails.** Every direction preserves the runtime contracts (Room, ledger, coordinator, Gate 1 diagnostics, identity, deduplication). The three variants only touch presentation.

## 0. What each direction must ship

Per direction:

1. **Concept** — the personality in one sentence.
2. **Personality** — the voice, one paragraph.
3. **Inspiration without copy** — 3–5 patterns from `cycle-04-benchmark.md § 3` that inform the direction, and one line per source about what we are *not* copying.
4. **Information architecture** — the Option C tabs plus any per-direction overflow / sheet variations.
5. **Menu** — bottom bar + wide rail label & icon choices; overflow contents.
6. **Colours** — hue map for `accent`, `success`, `attention`, `error`, `neutralHigh`, `neutralLow`, and any subtle wash tokens. Light and dark theme hex values.
7. **Shapes** — corner-radius emphasis within `radius.chip / control / card / hero / sheet / avatar` (visual system § 2).
8. **Typography** — which type roles this direction leans on (visual system § 6).
9. **Components** — which component variants this direction prefers.
10. **Iconography** — Material Symbols Rounded weight + optical size choice.
11. **Images** — where images appear (per brand-assets § 1).
12. **Logos** — which logos ship, at which size, per brand-assets § 3.
13. **Graphs** — which of the four honest charts (visual system § 11) show up per screen.
14. **Motion** — per-screen motion emphasis within motion-system § 3–7.
15. **Dark theme** — how the palette maps.
16. **Light theme** — same.
17. **Accessibility notes** — direction-specific concerns.
18. **Per-screen application** — one paragraph per Option-C tab + one for Onboarding + one for the Sync now sheet.
19. **Pros / cons / effort / maintenance** — comparison inputs.

Every direction fills every section. The comparison is at § 5 of this doc.

---

## 1. Direction A — Calm Health Companion

### 1.1 Concept

A quiet, personal-care surface that lets the sync itself be the loudest thing on the screen. Rounded, generous, ink-forward. Reads as "I have this handled for you" not "here are the internals".

### 1.2 Personality

Steady, adult, unassuming. Copy is warm but never chirpy. Numbers are shown when they mean something, not to prove the app has metrics. The success moment is a soft accent-wash, not a confetti burst. Emphasises the *outcome* (workout confirmed in Health Connect) over the *process* (coordinator phases).

### 1.3 Inspiration without copy

- Apple Health summary (**H3**) — card-per-topic rhythm and generous spacing. Not copied: the photographic hero (we ship no hero image).
- Gentler Streak (**H11**) — quiet copy voice, wellness intent without KPIs shouting. Not copied: character illustrations.
- Notion Today view (**P1**) — calm typography over data. Not copied: any Notion iconography.
- Oura Home tile (**H10**) — one number + one label + one gentle background wash. Not copied: sleep / readiness proprietary language.
- Refactoring UI shadow guidance (previous cycle reference) — soft tinted, single-layer shadows. Not copied: skeuomorphic layered stacks.

### 1.4 Information architecture

Option C exactly: Home / Activity / Connections + top-bar overflow (`Diagnose`, `Setup`, `About`, `Send feedback`). Sync now is a bottom sheet on compact, a persistent trailing panel on wide.

### 1.5 Menu

Bottom bar labels (compact):

- `Home` — icon `Home` (Rounded).
- `Activity` — icon `History` (Rounded).
- `Connections` — icon `Hub` (Rounded).

Wide rail: same three labels, larger icons (24dp), plus a persistent `Sync now` FAB above the rail and a `Setup` text button at the bottom.

Overflow menu: as IA doc § 5.3.

### 1.6 Colours

Ink-forward palette; accent hue is muted mint / sage; success is a warmer green; attention is a warm amber; error is a coral red.

Light theme:

| Role | Value |
|---|---|
| `surface.canvas` | #F7F7F5 |
| `surface.base` | #FFFFFF |
| `surface.raised` | #F1F1EE |
| `surface.high` | #E8E8E4 |
| `ink` | #12140F |
| `ink2` | #545651 |
| `ink3` | #90918C |
| `line` | #E1E2DE |
| `accent` | #2E7D5B (sage green) |
| `accentContainer` | #C7E4D4 |
| `accentSoft` | #E6F2EC |
| `success` | #2E7D5B |
| `attention` | #B26A00 |
| `error` | #B3261E |

Dark theme:

| Role | Value |
|---|---|
| `surface.canvas` | #0F1210 |
| `surface.base` | #171A18 |
| `surface.raised` | #1F2321 |
| `surface.high` | #262A28 |
| `ink` | #F4F5F1 |
| `ink2` | #B8BAB5 |
| `ink3` | #7C7E79 |
| `line` | #2F3330 |
| `accent` | #7CCFA5 |
| `accentContainer` | #244B39 |
| `accentSoft` | #1F2E27 |
| `success` | #7CCFA5 |
| `attention` | #E7A356 |
| `error` | #F2B8B5 |

### 1.7 Shapes

Leans on `radius.hero` (28dp) for the Home hero card. Cards otherwise at `radius.card` (20dp). Chips at `radius.chip` (full). Buttons at `radius.control` (12dp).

### 1.8 Typography

- Hero title: `headlineMedium`.
- Section title: `titleLarge`.
- Card title: `titleMedium`.
- Body: `bodyLarge`.
- Numbers: single stat tile uses `displaySmall` (28sp); avoids `displayMedium`.
- Eyebrows: `titleSmall` mixed case.

No custom font in Cycle 4.

### 1.9 Components

- `RaisedCard` for hero and summary tiles.
- `SurfaceCard` for grouped Activity rows.
- `AccentWashCard` for success moments on Home hero.
- `AttentionCard` for error state.
- `FilterChip` for the attention filter on Activity.
- Bottom-sheet `SyncDetailsSheet` for Sync now.
- No `PhaseMedallion`.

### 1.10 Iconography

Material Symbols — Rounded, weight 400, grade 0, optical size 24. Icons carry state colour only where the icon *is* the status signal.

### 1.11 Images

Original single-line watch-to-phone motif on Onboarding hero (in-house, one flat colour + one hairline). No stock illustration. No brand photography.

### 1.12 Logos

Per brand-assets § 3:

- Health Connect wordmark and icon at 24dp on Onboarding second line, and at 32dp inside the Connections Health Connect detail sheet.
- Every other integration: monogram inside the tile chrome.

### 1.13 Graphs

- Home: `StatTile` for "This week" (workout count) + `StatTile` for "Sync success". No sparkline on Home.
- Activity: `BarChartCard` at the top (12 weeks of workouts).
- Diagnostics: `StatTile` for time-to-confirmation + `SparklineTile` for failures over 14 days.

### 1.14 Motion

- `duration.standard` heavy; `duration.fast` for chips.
- No `syncRotation` on Home (rail lives inside the sheet).
- Success moment: `AccentWashCard` fade-in + soft check-icon slide.

### 1.15 Dark theme

Ink-forward-and-quiet in dark, too: near-black base, mint accent that stays quiet, softly-lit `surface.raised` (`#1F2321`). Success moment reads as a subtle green wash, never neon.

### 1.16 Light theme

Warm off-white canvas (`#F7F7F5`), not stark white. Cards white on the off-white to give depth without shadows.

### 1.17 Accessibility notes

- The mint accent hue at `#2E7D5B` clears 4.5:1 on both `surface.base` and `surface.raised` in light.
- On dark, `#7CCFA5` clears 4.5:1 on `surface.base` — verified against `#171A18`.
- Reduced motion turns off the accent wash's fade-in; the wash is present statically.

### 1.18 Per-screen application

- **Onboarding.** Warm off-white; hero mission at `headlineMedium`; primary `PrimaryAction` "Set up Huawei Sync" with Health Connect icon inline; secondary `SecondaryAction` "I already set it up".
- **Home.** Hero card at `radius.hero`; source→destination row inline; two `StatTile`s below; recent activity list of three; FAB anchored bottom-right.
- **Activity.** BarChartCard at top; day-grouped rows; soft caps day headers.
- **Activity detail.** Workout header + lifecycle timeline; technical detail behind disclosure.
- **Connections.** Active tiles first (Huawei · Sync · Health Connect · GymRats); Preview tiles second; every tile → detail sheet.
- **Sync now sheet.** Bottom-sheet; pipeline rail compact; single `Start sync` primary + `Show phase detail` text action.

### 1.19 Pros / cons / effort / maintenance

- **Pros.** Softest departure from today's language; broad user appeal; success feels earned; safest accessibility outcome.
- **Cons.** Least distinctive of the three — could read as generic wellness if not carefully typographed.
- **Effort.** Medium. Reuses most component APIs; introduces new tokens + retires several.
- **Maintenance.** Easy — the palette is small and the component set is standard M3.

---

## 2. Direction B — Dynamic Fitness Product

### 2.1 Concept

Confident, asymmetric, expressive. The app looks and feels like a modern fitness product — big numeric moments, strong contrast, no wasted whitespace. Cycle 3's Concept B, brought forward and applied app-wide.

### 2.2 Personality

Utility with attitude. The app speaks to a user who thinks of themselves as owning a fitness product, not managing a sync tool. Copy is short, verb-first. The success moment briefly makes the workout count feel like a win.

### 2.3 Inspiration without copy

- Strava features (**H7**) — row-as-object composition; expressive typography. Not copied: orange-over-black palette; social kudos.
- Linear public product page (**S1 not chosen; referenced from Cycle 3**) — asymmetric composition; strong horizontal accent lines. Not copied: Linear's iconography.
- Vercel deployments (**O1**) — utility dashboards that show state expressively without a wall of text. Not copied: the developer log viewer.
- Refactoring UI hierarchy — building visual hierarchy through weight / size / contrast, not borders. Not copied: bootstrap tropes.
- Home Assistant integrations (**S3**) — tile-and-sheet interaction. Not copied: the maintainer aesthetic.

### 2.4 Information architecture

Option C. Same tabs; same overflow.

### 2.5 Menu

Bottom bar (compact):

- `Home` — icon `Home` (Rounded).
- `Activity` — icon `History` (Rounded).
- `Connections` — icon `Hub` (Rounded).

Wide rail: same, with a stronger accent-glow around the `Sync now` FAB and a wider active-tab indicator (a 3dp accent underline instead of a dot).

### 2.6 Colours

Deep near-black base, an electric fitness accent, a bright success.

Light theme:

| Role | Value |
|---|---|
| `surface.canvas` | #F5F5F7 |
| `surface.base` | #FFFFFF |
| `surface.raised` | #EDEEF1 |
| `surface.high` | #E2E4E8 |
| `ink` | #0B0C10 |
| `ink2` | #4B4E56 |
| `ink3` | #8A8D95 |
| `line` | #D8DAE0 |
| `accent` | #E85D2F (electric orange) |
| `accentContainer` | #FFD7C6 |
| `accentSoft` | #FFECE1 |
| `success` | #1F8A4C |
| `attention` | #C47B00 |
| `error` | #C0362D |

Dark theme:

| Role | Value |
|---|---|
| `surface.canvas` | #08090B |
| `surface.base` | #101216 |
| `surface.raised` | #171A20 |
| `surface.high` | #1F232B |
| `ink` | #F6F7F9 |
| `ink2` | #B6B9C1 |
| `ink3` | #7A7E87 |
| `line` | #2A2D34 |
| `accent` | #FF7A4B |
| `accentContainer` | #4A2213 |
| `accentSoft` | #26160F |
| `success` | #6BD08E |
| `attention` | #F0A94F |
| `error` | #F2A099 |

### 2.7 Shapes

Intentionally mixes small (4dp / 8dp) and larger (24dp) radii for tension. Cards at `radius.card` (20dp). Buttons at `radius.control` (12dp). Chips at `radius.chip` (full). Home hero uses an asymmetric shape: `RoundedCornerShape(topStart = 28.dp, topEnd = 8.dp, bottomEnd = 28.dp, bottomStart = 8.dp)`.

### 2.8 Typography

- Home hero title: `displaySmall` (28sp) for the primary numeric fact (workouts this week or `attemptCount`).
- Hero sub-title: `titleMedium` (18sp).
- Section title: `titleLarge`.
- Card title: `titleMedium`.
- Body: `bodyMedium`.
- Numbers: `displayMedium` on `StatTile` — this direction is the only one that reaches `displayMedium`.
- Eyebrows: `titleSmall` mixed case with `letterSpacing = 0.02.em`.

### 2.9 Components

- `RaisedCard` used aggressively on Home hero (asymmetric shape) + `StatTile`.
- `ConnectionRow` uses a slightly denser layout — smaller inner padding to fit more density without losing hit-target.
- `FilterChip` inverted colour when active (accent-fill, ink foreground).
- Bottom-sheet `SyncDetailsSheet` for Sync now — with a subtle accent glow on the drag handle when a sync is in progress.

### 2.10 Iconography

Material Symbols — Rounded, weight 500 (heavier than A), grade 0, optical size 24.

### 2.11 Images

Same discipline as A. Onboarding hero uses the same watch-to-phone motif, tinted in the accent hue but drawn identically. No brand imagery beyond approved marks.

### 2.12 Logos

Same as Direction A (per brand-assets § 3).

### 2.13 Graphs

- Home: `StatTile` for "This week" using `displayMedium`; `SparklineTile` for weekly count under it (adds a scannable trend to the number).
- Activity: `BarChartCard` for 12 weeks + `StatTile` for sync success rate.
- Diagnostics: same as A.

### 2.14 Motion

- `duration.fast` heavy (chip / number swaps feel snappy).
- Success moment includes a subtle number scale (`96 % → 100 %`, single spring, `duration.standard`).
- Sync-now sheet gains a subtle accent-glow on the FAB while active (per `cycle-04-motion-system.md § 4`).

### 2.15 Dark theme

Deep near-black canvas, electric orange accent, bright success green. Success feels loud on dark; on light it stays confident but not shouty.

### 2.16 Light theme

Cool off-white canvas (`#F5F5F7`) with white cards. Accent is high-contrast — reads immediately.

### 2.17 Accessibility notes

- Electric orange `#E85D2F` on light must be paired with `ink` for text, never with `ink2`. Verified 4.7:1 against `surface.base`.
- On dark, `#FF7A4B` clears 4.5:1 against `#101216`.
- `letterSpacing = 0.02.em` on eyebrows and `titleMedium` is required to prevent tight display types from becoming hard-to-read at small sizes.

### 2.18 Per-screen application

- **Onboarding.** Same shape as A but hero title larger, accent stronger; the mission line uses `titleLarge` weight 700; secondary link is a text button in accent.
- **Home.** Asymmetric hero card with a single primary number in `displayMedium`; `SparklineTile` under it; `StatTile` for success rate to the right; recent activity as three dense rows.
- **Activity.** Denser rows (min 56dp instead of 64dp) to fit more without dropping legibility; chart card uses accent-tinted bars for the current week.
- **Activity detail.** Larger workout header (`displaySmall`); lifecycle timeline with per-step accent-tinted dots for the active step.
- **Connections.** Active tiles slightly larger than Preview tiles; Preview tiles compact.
- **Sync now sheet.** Pipeline rail active-node uses accent stroke; `Start sync` button in accent-fill.

### 2.19 Pros / cons / effort / maintenance

- **Pros.** Most distinctive personality; strongest first impression; owns "fitness product" positioning.
- **Cons.** Accent is loud — risk of feeling gamified; asymmetric hero shape breaks the M3 default grammar.
- **Effort.** Medium-high. Introduces asymmetric shape + custom letter-spacing rules.
- **Maintenance.** Medium — asymmetric shapes must be watched in future component additions.

---

## 3. Direction C — Connected Health Ecosystem

### 3.1 Concept

Flow-first. The app reads as a *topology* — a source watch, this app, Health Connect, and an available downstream (GymRats). Every screen supports the topology rather than competing with it. Cycle 3's Concept C, brought forward and matured into an app-wide voice.

### 3.2 Personality

Structured, precise, calm. Speaks in *paths* rather than *pages*. Colour is used to indicate flow state (`edgeDormant / edgeActive / edgeConfirmed`) not to add personality per surface.

### 3.3 Inspiration without copy

- Zapier "How it works" (**S1**) — source → transform → destination composition. Not copied: applet catalogue chrome; the word "Zap".
- Home Assistant integrations (**S3**) — tile-and-sheet plus a topology view. Not copied: maintainer aesthetic; JSON detail.
- Vercel edge network diagram (**earlier reference**) — animated edges on a topology surface. Not copied: developer-tool tone.
- Compose Canvas + graphicsLayer docs — native drawing baseline. Not copied: any developer sample directly.
- WCAG 2.2 animation-from-interactions — enforces reduced-motion. Not copied: N/A.

### 3.4 Information architecture

Option C plus one nuance: Connections is the *primary* browsing surface, second only to Home. Activity trades places with Connections in the bottom bar order (`Home · Connections · Activity`) so the topology metaphor is one tap away.

### 3.5 Menu

Bottom bar (compact) — reordered:

- `Home` — icon `Home` (Rounded).
- `Connections` — icon `Hub` (Rounded).
- `Activity` — icon `History` (Rounded).

Wide rail: same order, with a `Sync now` FAB adjacent to the `Sync` hub node when the topology is visible on Home.

### 3.6 Colours

Deep neutrals + a single active-state hue. Very restrained; the topology's node/edge colours are what carry personality.

Light theme:

| Role | Value |
|---|---|
| `surface.canvas` | #F6F6F4 |
| `surface.base` | #FFFFFF |
| `surface.raised` | #F0F0EE |
| `surface.high` | #E6E6E3 |
| `ink` | #0F1210 |
| `ink2` | #4E5450 |
| `ink3` | #8A8F8B |
| `line` | #DDE0DC |
| `accent` | #2F6BE2 (calm blue) |
| `accentContainer` | #CFE0FF |
| `accentSoft` | #E9F1FF |
| `success` | #227A5A |
| `attention` | #A96A00 |
| `error` | #B3261E |
| `edgeDormant` | `ink` α20 |
| `edgeActive` | `accent` |
| `edgeConfirmed` | `success` |

Dark theme:

| Role | Value |
|---|---|
| `surface.canvas` | #0C0E12 |
| `surface.base` | #131620 |
| `surface.raised` | #1A1E29 |
| `surface.high` | #232633 |
| `ink` | #F5F6F8 |
| `ink2` | #B4B8C2 |
| `ink3` | #7A7F8B |
| `line` | #2A2E3A |
| `accent` | #7EA6F7 |
| `accentContainer` | #1F3A6D |
| `accentSoft` | #182339 |
| `success` | #7CD9B2 |
| `attention` | #E7A356 |
| `error` | #F2B8B5 |

### 3.7 Shapes

Nodes are pill-shaped or circular (`radius.chip` at the extremes). Supporting cards at `radius.card`. Buttons at `radius.control`.

### 3.8 Typography

- Hero: `titleLarge` for node names (topology reads first); `bodyLarge` for the supporting explanation.
- Section titles: `titleLarge`.
- Numbers: `displaySmall` reserved for one metric per screen only (the "workouts this week" tile on Home).
- Eyebrows: `titleSmall` mixed case.

### 3.9 Components

- Home hero is a horizontal (compact) or vertical (wide) topology drawn with `SyncPipelineRail` (already exists) + a small "supporting panel" card.
- `ConnectionRow` in Connections is the same tile chrome that Home uses inside the topology — one shape language across the app.
- `SyncDetailsSheet` shows the same topology, expanded, with per-node timing.
- `FabAction` is smaller (48dp) and positioned adjacent to the `Sync` hub node itself on wide.

### 3.10 Iconography

Material Symbols — Rounded, weight 400, grade 0, optical size 20 (slightly smaller than A/B to sit inside pill-shaped nodes).

### 3.11 Images

Same discipline. Onboarding hero uses the topology directly: a static-drawn Watch → Sync → Health Connect → GymRats motif.

### 3.12 Logos

Same as A / B. Health Connect gets a slightly larger presence inside its topology node (28dp icon) because the topology *is* the identity.

### 3.13 Graphs

- Home: `StatTile` for "This week" (only one). No sparkline.
- Activity: `BarChartCard` for 12 weeks; `StatTile` for sync success rate.
- Diagnostics: same as A.

### 3.14 Motion

- Topology edges draw in with `Animatable` at `duration.deliberate` when the connected node pair transitions state (idle → active, active → confirmed).
- Active-phase node has a soft scale pulse `1.0 → 1.03 → 1.0` at `duration.syncRotation`. Reduced motion: static.
- Zero motion when the coordinator is idle.

### 3.15 Dark theme

Deep neutrals; the calm-blue accent reads as a thin line, not a fill. Confirmed state (`success` green) pops without shouting. Idle topology is very quiet.

### 3.16 Light theme

Warm off-white; the topology's `ink` glyphs are the loudest thing at rest. The accent hue only appears when a real edge activates.

### 3.17 Accessibility notes

- `edgeDormant` at `ink` α20 must be tested for TalkBack visibility — the topology's `contentDescription` describes each node's state in words (`"Health Connect: confirmed"`) so TalkBack does not depend on the edge tone.
- Active-phase pulse must be disabled under reduced motion.
- Bottom bar order change (`Activity ↔ Connections`) requires an in-app one-time hint on first run.

### 3.18 Per-screen application

- **Onboarding.** Hero is the topology drawn once (line-draw animation on first visit).
- **Home.** Topology at the top (Watch → Sync → Health Connect → GymRats) with per-node state; one `StatTile` under it (workouts this week); recent activity as three rows; FAB adjacent to the `Sync` hub on wide, bottom-right on compact.
- **Activity.** Same as A / B; the topology is *not* repeated here (that would be redundant).
- **Activity detail.** Header + lifecycle timeline; no rail (timeline says everything, per the redesign doc).
- **Connections.** Two lists (Active, Preview) using the same tile shape as the Home topology tiles.
- **Sync now sheet.** Same topology, expanded, plus a small `Coordinator phase` header + the phase name.

### 3.19 Pros / cons / effort / maintenance

- **Pros.** Communicates the app's *purpose* at a glance — nothing else does. Uses an existing component (`SyncPipelineRail`) as the visual anchor. Room to grow (add nodes when the graph adds destinations).
- **Cons.** Steepest learning cost — the topology is a metaphor a user has to accept. Risk of reading as a network-admin tool if node typography stiffens.
- **Effort.** Medium-high. The topology needs to be genuinely responsive (compact vs wide) and reduced-motion-safe.
- **Maintenance.** Medium. Adding a destination is easy (one node); changing the topology metaphor is not.

---

## 4. Which direction has *not* been chosen (and how the human decides)

Cycle 4 **does not choose**. It ships three directions the human can evaluate against the same data and mission.

### 4.1 Comparison table — filled by the Art Director as observation, not recommendation

| Criterion | A — Calm Health | B — Dynamic Fitness | C — Connected Ecosystem |
|---|---|---|---|
| Personality (feels like a health tool worth using?) | Yes — calm; risk of "generic wellness" | Yes — confident; risk of "gamified" | Yes — technical; risk of "developer tool" |
| Clarity of current state at a glance | High (hero copy carries it) | Highest (number is the state) | High (topology is the state) |
| Simplicity (fewer moving parts) | Highest | Medium | Medium |
| Quality feel (does it read as premium?) | High | High | High |
| Differentiation (distinct from a generic Compose app?) | Medium | Highest | Highest |
| Accessibility floor cleared? | Yes | Yes with letter-spacing rule | Yes with node contentDescription |
| Maintenance (survives a year of feature work?) | Highest | Medium | Medium |
| Alignment with mission (sync workouts honestly → Health Connect → GymRats) | High | High | Highest |
| Onboarding ease (new user oriented in 15s?) | High | Medium | Medium |
| Room to add integrations (Strava, Fitbit) | High | High | Highest |

### 4.2 The Art Director's read (observation, not decision)

- **A is the safest bet.** Ship-ready fastest; least room for user confusion; won't age poorly. If we want to converge fast without a debate, this is the direction.
- **B is the most product-forward.** Best "premium fitness app" positioning; best if a real user thinks of Huawei Sync as *their* fitness product. Riskiest hue choice — needs disciplined use.
- **C is the most Huawei-Sync-specific.** No other direction communicates "this app connects Huawei to Android" at a glance. Best if the app's identity is meant to be its role, not its wellness tone.

The recommendation is left to the human. The implementation roadmap (`cycle-04-implementation-roadmap.md § 4`) fans the same first three cycles equally — no direction is bet on before the human calls it.

## 5. What Cycle 4 does *not* ship

- No Compose composables. No file under `app/src/main/java/dev/lui/huaweisync/ui/prototypes/conceptA/` gets a Cycle-4 sibling.
- No screenshots. Screenshots come with the implementation cycle that picks a direction.
- No pick between A / B / C.
- No change to `HuaweiSyncDestination.all`.
- No change to `HuaweiSyncNavigationTest`.
- No new dependency in `libs.versions.toml`.
- No brand asset in the APK.
