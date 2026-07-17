# Cycle 4 — Visual system (app-wide)

**Cycle scope.** Define one coherent app-wide visual system to replace the current all-square, all-hairline `HuaweiSyncGeometry` + `HuaweiSyncSpacing` + `ModernistSurface` grammar. The system is direction-agnostic — the three directions in `cycle-04-directions.md` each choose *within* this system.
**Role.** Design Systems Reviewer + Mobile Art Director + Accessibility Reviewer.
**Guardrails.** No code ships in this cycle. Every proposal is stated as tokens to introduce and existing tokens to retire; the *when* is in `cycle-04-implementation-roadmap.md`.

## 1. What the current system does today

From `app/src/main/java/dev/lui/huaweisync/ui/theme/Tokens.kt`:

- `HuaweiSyncGeometry.cornerRadius = 0.dp` — every card is a right-angled rectangle.
- `HuaweiSyncGeometry.borderThin = 1.dp`, `borderStrong = 2.dp` — depth is expressed exclusively through borders.
- `HuaweiSyncShapes` maps every M3 shape slot to `RoundedCornerShape(0.dp)` — even chips, buttons, and text fields end up square.
- `HuaweiSyncElevation.none / small(1) / medium(3) / large(12)` exists but almost nothing uses `medium` or `large` — the app draws with `none` and adds a border instead.
- Spacing scale (`HuaweiSyncSpacing`): `xs 4`, `sm 8`, `md 12`, `lg 16`, `xl 24`, `xxl 32`. Fine as a scale; used inconsistently (some surfaces open with `xl`, others with `lg`).
- Palette (from `Color.kt` / `Theme.kt`): `ink / ink2 / ink3 / line / lineStrong / accent / accentSoft / accentContainer / accentForeground / surface1 / surface2 / surface3 / canvasBackground / background / ok / info / warning`. Correct as roles; small; no depth variant, no tint variant.
- Typography (`Type.kt`): Material 3 baseline; `HuaweiSyncTheme.technicalTypography` adds `label` / `microcopy` / `value` — the "technical eyebrow" style leaks into every screen.
- Motion (`MotionPolicy.kt`): `fast 160`, `standard 240`, `deliberate 480`, `syncRotation 1000`, and a `Reduced` variant. Correct scaffold — the motion doc keeps it as the baseline.

**System-level critique.** The token surface is small, safe, and enforces one *look* (developer console). It does not enforce hierarchy — every screen ends up flat because there is no reason to elevate anything.

## 2. Shapes and corner radii

Replace the single-value `HuaweiSyncGeometry.cornerRadius = 0.dp` with a rounded-scale keyed to the size of the container.

| Token | Value | Where it applies | Not to use for |
|---|---|---|---|
| `radius.chip` | 999.dp (fully rounded) | Chips, pills, tag-like affordances | Cards, sheets. |
| `radius.control` | 12.dp | Buttons, text fields, small inputs | Cards larger than 48dp min height. |
| `radius.card` | 20.dp | Card-level surfaces (Home summary cards, Connections cards, Activity rows) | Full-viewport hero. |
| `radius.hero` | 28.dp | Home hero, empty-state hero | Chips or buttons. |
| `radius.sheet` | 28.dp top-only | Bottom sheets (top corners rounded, bottom flush with screen) | Anywhere else. |
| `radius.chart` | 8.dp | Chart plot backgrounds and per-bar caps | Card outer edges. |
| `radius.avatar` | 999.dp | Circular tokens (source badge, single-letter mark) | Non-circular content. |

Additional rule:

- **Do not apply** `radius.chip` to a full-width surface (a chip's roundedness only reads at pill widths).
- **Do not compose** `radius.hero` + `radius.card` next to each other with less than 8.dp of space between — the two radii start reading as the same shape at close proximity.

Retire `HuaweiSyncGeometry.cornerRadius`. Retire the mapping of `HuaweiSyncShapes` to 0.dp; use `Shapes(extraSmall = radius.control, small = radius.control, medium = radius.card, large = radius.card, extraLarge = radius.hero)`.

Border rules under the new shapes:

- Cards and hero **do not** carry a visible border by default. Depth carries their edge.
- Chips, controls, and dividers may carry hairlines (`1.dp`) using `line` — used sparingly.
- Attention states (error border, warning border) use `2.dp` borders **inside** the card (`Modifier.border` applied over an inner padding) so the border does not visually enlarge the card.

## 3. Spacing and grid

Extend the existing scale with two clarifying values:

| Token | Value | Purpose |
|---|---|---|
| `space.xs` | 4.dp | Micro-gap between glyph and label. |
| `space.sm` | 8.dp | Chip padding; inner row gaps. |
| `space.md` | 12.dp | Grouped-row spacing; icon-label rows. |
| `space.lg` | 16.dp | Card inner padding, list item padding. |
| `space.xl` | 24.dp | Card-to-card vertical gap; hero inner padding. |
| `space.xxl` | 32.dp | Section-break vertical gap. |
| `space.section` (new) | 40.dp | Between semantically-independent sections on Home. |
| `space.gutter` (new) | 20.dp | Between the screen edge and the outer edge of first-line content on compact viewports. |

Grid:

- **Compact (< 600dp).** Single-column at `space.gutter` from each edge. All content fits in a 4dp base unit.
- **Medium (600–839dp).** Single-column at `space.xxl` from each edge; hero may span two-thirds width with a supporting card on the trailing side.
- **Wide (≥ 840dp).** Two-column layout: main content column max 640dp; trailing panel column max 320dp for `Sync details`, `Diagnose this`, `Activity detail` context.

## 4. Elevation and tonal surfaces

Depth comes from tonal roles, not shadows. This uses Material 3 tonal surfaces (`cycle-04-benchmark.md D1`) but keeps existing role names alive.

Add tonal surface roles (light theme; dark theme derives with matching L*):

| Token | Purpose | Approximate role |
|---|---|---|
| `surface.canvas` | App background under content. | Existing `background`. |
| `surface.base` | Baseline card fill (Connections cards, Activity rows). | Existing `surface1`. |
| `surface.raised` | Home summary cards, hero. | Existing `surface2`. |
| `surface.high` | Bottom-sheet interior above other content. | Existing `surface3`. |
| `surface.accentWash` | Success wash on hero (soft vertical gradient from `accent` α8 → transparent). | New — carried over from Concept A. |
| `surface.attentionWash` | Error / attention wash on hero (soft vertical gradient from `accent` α12 → transparent). | New. |

Shadow rules:

- **Zero** shadow on `surface.base`, `surface.raised`.
- **One** soft, tinted shadow on `surface.high` (`elevation = 3dp`, `ambientColor = ink α60%`, `spotColor = accent α15%`) — reserved for sheets and modals only.
- **Never** compose shadow + border on the same surface (they compete).

## 5. Colour system (roles, not hues)

Keep the current role vocabulary — expand where the current system runs out.

Existing roles (from `Color.kt`, preserved):

- `ink`, `ink2`, `ink3` — text tones (highest to lowest emphasis).
- `line`, `lineStrong` — dividers.
- `accent`, `accentSoft`, `accentContainer`, `accentForeground` — primary accent family.
- `surface1`, `surface2`, `surface3`, `canvasBackground`, `background` — base surfaces.
- `ok`, `info`, `warning` — status colours.

New roles to add:

| Role | Purpose |
|---|---|
| `success` | Verified / confirmed states. Distinct from `ok` — `ok` reads as "healthy state", `success` reads as "just happened successfully". |
| `attention` | Reconciliation / retry states. Distinct from `warning`. |
| `error` | Failure states. Distinct from `accent`. |
| `neutralHigh`, `neutralLow` | High-emphasis and low-emphasis neutral tones for chip fills, empty-state backgrounds. |
| `accentSubtle` | 8 % alpha of `accent` for washes. |

Rules:

- **Direction A**, **B**, **C** each map these roles to *different* hues (see `cycle-04-directions.md`). The role names stay the same across directions; only the hue map changes.
- **Do not** use the accent hue for warning, error, or attention states — they carry their own hue set.
- **Do not** re-tint any third-party logo (`cycle-04-brand-assets.md § 2`).
- **Do** run every hue map through the accessibility check in § 13 before adoption.

## 6. Typography

Reuse the Material 3 type scale; retire the "technical eyebrow" as an app-wide feature.

Kept:

- `display*`, `headline*`, `title*`, `body*`, `label*` from `HuaweiSyncTheme.typography`.

Retired (or heavily reduced):

- `HuaweiSyncTheme.technicalTypography.microcopy` and `.label` in caps — these become allowed **only** on the Diagnostics surface, not app-wide.
- Every `TechnicalMicrocopy("SOMETHING CAPS")` call outside Diagnostics is removed.

New patterns:

- **Eyebrow.** `titleSmall` mixed case, `space.sm` from the following title. Not uppercase.
- **Section header.** `titleLarge` for section titles inside a screen.
- **Screen title.** `headlineMedium` (compact) / `headlineLarge` (wide).
- **Numeric emphasis.** `displaySmall` (28sp) on Home summary tile for the single primary count (workouts this week). `displayMedium` (36sp) reserved for direction-specific hero cases.
- **Body copy.** `bodyLarge` for hero supporting text; `bodyMedium` for card supporting text; `bodySmall` for footnotes and evidence-only content.
- **Icon-adjacent labels.** `labelLarge` mixed case with a `letterSpacing = 0.02.em` — no uppercase transform.

Line-height stays at the M3 defaults. Font family stays at the platform default — introducing a custom font is out of scope for Cycle 4 and is scheduled explicitly in `cycle-04-implementation-roadmap.md`.

## 7. Iconography

Standardise on **Material Symbols — Rounded weight**, already shipping through `androidx.compose.material:material-icons-extended`. No mixing weights.

Sizes:

- `size.icon.sm` = 16.dp — inside chips, inline with `bodyMedium`.
- `size.icon.md` = 20.dp — inline with `titleMedium`, list-row indicators.
- `size.icon.lg` = 24.dp — top-bar actions.
- `size.icon.xl` = 32.dp — Connections detail sheet header.

Tinting:

- Icons default to `ink` for informative use, `ink2` for secondary, and the semantic role colour (`success`, `attention`, `error`) when the icon *is* the status signal.
- **Do not** tint icons to the direction's accent hue purely for decoration.

## 8. Illustrations and imagery

Cycle 4 does not commission any illustration. What the system prescribes:

- **Onboarding hero.** A single line-drawn watch-to-phone motif (original, in-house, one flat colour + one hairline). Placeholder text label + monogram until it exists.
- **Empty states.** No illustration. One line of copy + one primary action (`cycle-04-visual-system.md § 12`).
- **Success states.** No illustration; use `surface.accentWash` + one line of copy + one small icon.
- **Brand marks.** Only Health Connect (approved) + Material Symbols today. See `cycle-04-brand-assets.md`.

## 9. Component vocabulary

The system defines these components; each direction re-skins within the tokens above but preserves the API.

### 9.1 Card families

- **`SurfaceCard`.** Base card. Padding `space.lg`. Radius `radius.card`. Fill `surface.base`. No border. Elevation 0.
- **`RaisedCard`.** Home summary cards, hero. Padding `space.xl`. Radius `radius.card` or `radius.hero`. Fill `surface.raised`. Elevation 0 (depth from tone).
- **`AttentionCard`.** For error / retry surfaces. Same as `SurfaceCard` plus a `2.dp` inner border in `error` (or `attention`).
- **`AccentWashCard`.** For success moments on Home hero. `SurfaceCard` + a `surface.accentWash` overlay.
- **`ChipStrip`.** Horizontal scrollable row of chips (`Filter`, `Attention only`, `Verified only`). Fully rounded.

### 9.2 Buttons

- **`PrimaryAction`.** Filled `accent` background, `accentForeground` text, radius `radius.control`, min height 48dp, min width 96dp.
- **`SecondaryAction`.** `surface.base` background with hairline `line` border, `ink` text, same shape as primary.
- **`TextAction`.** Text-only, `accent` colour, `titleMedium`, no chrome — used for "Show phase detail", "Diagnose this".
- **`FabAction`.** 56×56dp circular; `accent` background; drops one soft tinted shadow (`elevation = 3.dp`).

Replace `StraightEdgeButton` with `PrimaryAction` / `SecondaryAction`. Retire `StraightEdgeButton` after the roadmap's shell cycle.

### 9.3 Chips

- **`StatusChip`.** Small rounded pill with an icon + label; height 24dp; padding `space.sm`. Semantic role colour for both text and border (both at low alpha for fill).
- **`FilterChip`.** M3 filter chip semantics; height 32dp; fill `surface.raised` when inactive, `accentSoft` when active.
- **`SourceChip`.** For Activity row (post-Gate 3): small chip with a source glyph + text name; height 20dp.

Retire the current `AttentionFilterChip` bespoke implementation once `FilterChip` exists.

### 9.4 Navigation

- **`BottomBar`.** M3 `NavigationBar` at `surface.base` with three destinations (Home / Activity / Connections). Selected item shows both icon and label; unselected items show icon only (labels appear on active or on TalkBack).
- **`WideRail`.** M3 `NavigationRail` at `surface.raised` with three destinations + `FabAction` at the top (`Sync now`) + `TextAction` at the bottom (`Setup`).
- **`TopBar`.** M3 `CenterAlignedTopAppBar` on compact; `TopAppBar` on wide. Actions: sync icon (opens the sheet) + overflow (`Diagnose`, `Setup`, `About`, `Send feedback`).

### 9.5 Lists

- **`ActivityRow`.** Full-width tap target ≥ 64dp. Left: `space.gutter`. Content: primary title (`titleMedium`), one-line supporting (`bodySmall`), right-aligned time + attempt count. Right accent strip (4dp) coloured by readback state.
- **`ConnectionRow`.** Full-width tap target. Left: 40dp mark (logo or monogram). Content: name (`titleMedium`) + one-line status (`bodySmall`) + `StatusChip`. Right chevron for "opens detail sheet".
- **`FactRow`.** Diagnostics-only. Label + value in two columns; monospaced value; SelectionContainer.

### 9.6 Sheets and modals

- **`SyncDetailsSheet`.** `ModalBottomSheet` (compact) / side panel (wide). Contains the coordinator's phase view, pipeline rail, sanitized failure summary. See `cycle-04-screen-redesign.md § 5`.
- **`ConnectionDetailSheet`.** Per-provider detail; includes brand mark (when approved), status, contextual action (open the provider's Android app, if installed), and one-liner about what Huawei Sync does with this provider.
- **`DiagnoseSheet`.** From "Diagnose this" chips; jumps into the Diagnostics surface content in a sheet, so the user can inspect without losing context.

### 9.7 Charts (see § 11 for spec)

- **`StatTile`.** Single-number card. Number in `displaySmall`; label in `labelLarge` mixed case; optional trend chip.
- **`SparklineTile`.** 40dp-tall sparkline over 7 buckets; no scale labels.
- **`BarChartCard`.** 7-bar horizontal or 12-bar vertical; uniform width; single colour keyed to the semantic role of what is being measured.

### 9.8 Dividers

- **`Divider.hairline`.** 1dp `line`. Only inside cards or between grouped rows.
- **`Divider.space`.** Zero-height layout gap of `space.section`. Preferred over visible dividers between sections.

## 10. States

App-wide state vocabulary, uniform across screens.

- **Ready.** Neutral tone. Card renders normally. No adornment.
- **Attention.** `attention` role. `AttentionCard` variant; `StatusChip` renders `attention` colour.
- **Error.** `error` role. `AttentionCard` variant + `error` border; hero surface may take `surface.attentionWash`.
- **Success.** `success` role. `AccentWashCard` on hero for the moment; `StatusChip` in `success` colour on cards.
- **Pending.** `info` role. `StatusChip` in `info` colour with a soft ambient pulse only when a coordinator action is *actually running* (see `cycle-04-motion-system.md § 4`).
- **Empty.** Uses `EmptyStateSection` (§ 12).
- **Loading.** Uses skeleton blocks (§ 12).

## 11. Charts (honest list only)

Per `cycle-04-benchmark.md § 4`, four charts are honest with the P0 data we have today and after Gate 1. All others are rejected.

### 11.1 Workouts per week (bar chart)

- Rendered as `BarChartCard` with 12 vertical bars, one per week for the past 12 weeks.
- Bar colour: `accent` at 100 % (each bar the same).
- Y-axis: no numeric labels; instead, a single `Max: N` label above the tallest bar.
- X-axis: no per-bar labels; instead, `12 weeks ago` and `This week` at the extremes.
- Empty state: renders a flat baseline with the copy "Sync your first workout — it appears here".
- Reduced motion: bars appear immediately, no bar-grow animation.

### 11.2 Sync success rate (stat tile)

- Rendered as `StatTile`. Number is percent (`displaySmall`), label is `Sync success` (`labelLarge`).
- When sample size is `< 3`, the tile renders `—` and the copy "Not enough data yet".
- Colour: `success` for ≥ 95 %, `attention` for 80–95 %, `error` for < 80 %. Roles taken from § 5.

### 11.3 Time-to-confirmation (stat tile — Diagnostics only)

- `StatTile` with the median time from `acceptedAt` to `confirmedAt` across the past 20 rows.
- Only on Diagnostics; users do not see this on Home or Activity.

### 11.4 Failures over time (sparkline — Diagnostics only)

- `SparklineTile` of failure count per day over the past 14 days.
- On Diagnostics only.

**Rejected charts** (from the benchmark shortlist): calories, heart rate, distance, sleep, SpO2, workout type distribution (until Gate 3), duplication-avoided count (not persisted today).

## 12. Empty, error, loading, success — shared components

### 12.1 `EmptyStateSection`

- Icon (24dp) at the top — semantic to the section (e.g. a `Sync` icon for empty Activity).
- Title `titleLarge` — one sentence.
- Supporting `bodyMedium` — one sentence.
- Optional `PrimaryAction`.

Per-surface content:

- Activity empty: "Sync your first workout" · "Confirmed workouts appear here." · action `Sync now`.
- Connections active-path empty (shouldn't happen — always at least three tiles): N/A.
- Diagnostics empty: "Ready when you are" · "No diagnostic run yet." · action `Refresh`.

### 12.2 `ErrorStateSection`

- `AttentionCard` variant. Icon (24dp) in `error` role.
- Title one sentence describing what did not work in user terms.
- Sanitized failure summary as `bodyMedium` (unchanged content, honest).
- Primary action `Retry` where a retry is legal per the coordinator; secondary `Diagnose this`.
- Never re-triggers `onSync` on retry silently — it goes back through the same Sync now sheet flow.

### 12.3 `LoadingSkeleton`

- Rectangular skeleton blocks matching the shape of the content that will replace them (hero-shaped block, list-row-shaped blocks).
- Shimmer is **not** required — a flat neutral tone at `surface.raised` α80 is enough. If shimmer ships, it uses a `HuaweiSyncMotion.current.deliberateMillis` cycle and is gated on reduced-motion.

### 12.4 `SuccessMoment`

- Applied inside Home hero only.
- `AccentWashCard` + a small check icon in `success` role animating in with a `fadeIn + slideInVertically` (see `cycle-04-motion-system.md § 4`).
- Copy: "Confirmed in Health Connect" + one small `TextAction`: "See your workouts →" (jumps to Activity).

## 13. Accessibility

Every direction must clear:

- **Contrast.** Text against its background ≥ 4.5:1 (`bodyMedium` and smaller) or ≥ 3:1 (larger than 18pt / 14pt bold).
- **Icons that carry state.** ≥ 3:1 against background; non-decorative icons carry a `contentDescription`.
- **Touch target.** Every interactive element ≥ 48×48dp effective hit box, even if the visual is smaller.
- **Focus order.** Top-bar → screen content → bottom bar. Overflow menu opens as a system dropdown.
- **Reduced motion.** All motion in `cycle-04-motion-system.md` has an explicit reduced-motion branch.
- **Font scaling.** Every text element uses `MaterialTheme.typography` and scales with system font size up to `largest` without truncation. Lists that would truncate switch to two-line layout at ≥ 130 %.
- **TalkBack.** Card composite semantics collapse the whole card into one node with a merged description ("Health Connect. Confirmed. Available for GymRats to import.").

## 14. What each direction is free to change

Direction A / B / C in `cycle-04-directions.md` each choose:

- **Hue map** (`accent`, `success`, `attention`, `error`, `neutralHigh`, `neutralLow`).
- **Radius emphasis** (A leans on `hero`, B intentionally mixes 4 and 24, C uses `chip`-style pill nodes).
- **Motion timing** (all within § 2 of the motion doc).
- **Emphasis on typography** (A prefers `headlineMedium`; B pushes `displayMedium`; C prefers `titleLarge` on the topology and reserves display type for one metric only).
- **Iconography weight** — but all three stay on Rounded (see `cycle-04-brand-assets.md § 3.11`).

Directions **do not** change:

- Component names, APIs, or roles.
- Elevation model.
- Chart contract (honest list in § 11).
- Accessibility floor in § 13.

## 15. What must retire from the current codebase

Cycle 4 lists these as retirements for the implementation roadmap:

- `HuaweiSyncGeometry.cornerRadius = 0.dp` and its `RoundedCornerShape(0)` mapping in `HuaweiSyncShapes`.
- `StraightEdgeButton` (replaced by `PrimaryAction` / `SecondaryAction`).
- `ModernistSurface` (replaced by `SurfaceCard` / `RaisedCard` / `AttentionCard` / `AccentWashCard`).
- `TechnicalMicrocopy` outside Diagnostics.
- `SectionHeader` with uppercase eyebrow outside Diagnostics.
- `AttentionFilterChip` bespoke look (replaced by `FilterChip`).
- `PhaseMedallion` letter block on Sync now (replaced by the pipeline rail — sheet redesign in the redesign doc).

None of these retirements happen in Cycle 4 — they are the deletion list for the implementation cycles.
