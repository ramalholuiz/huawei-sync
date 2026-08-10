# Cycle 3 — Asset manifest

Cycle-3-scoped subset of `docs/design/assets/ASSET-MANIFEST.md`. Only assets touched or shipped in Cycle 3 are listed here.

Access date for every row: 2026-07-17 (session date).

## 1. Cycle 3 shipping policy

Cycle 3 delivers three prototype Dashboards. All visuals are drawn with Compose primitives (`Box`, `Row`, `Column`, `Canvas`, `Modifier.drawBehind`, `RoundedCornerShape`) or Material Icons that are already bundled via the existing `androidx.compose.material:material-icons-extended` dependency.

**Cycle 3 ships no new bitmap or vector asset, no brand mark, no logo, no font file, and no new library.**

- No Huawei brand mark.
- No Health Connect brand mark.
- No GymRats brand mark.
- No Strava, Fitbit, WHOOP, Nike or any other fitness or health company mark.
- No image sourced from the internet is embedded, referenced, or loaded at runtime.
- No custom font. `HuaweiSyncTheme.typography` continues to use system `FontFamily.SansSerif` and `FontFamily.Monospace`.
- No new icon set. Concept-A/B/C composables reuse the existing `material-icons-extended` set.

Monogram characters used in Concept A/B/C (`H`, `HC`, `GR`, `W`) are ASCII typography, not logos, and remain unchanged from Cycle 1's stance.

## 2. Iconography (Cycle 3)

Cycle 3 continues to draw icons from the already-bundled `androidx.compose.material:material-icons-extended` (Apache-2.0, Google). New icon uses introduced by the Cycle 3 concepts are limited to icons already present in the set:

- `Icons.Rounded.Sync` — reused across all three concepts on the sync trigger surface.
- `Icons.Rounded.Check` / `Icons.Rounded.CheckCircle` — reused on success-confirmed states.
- `Icons.Rounded.ErrorOutline` — reused on error states.
- `Icons.Rounded.HourglassEmpty` — new **within our codebase** but already part of the Material Icons Extended set — used on waiting states in Concepts A and B.
- `Icons.Rounded.CloudSync` — new within our codebase but already part of Material Icons Extended — used on the topology hub node in Concept C.
- `Icons.Rounded.RadioButtonChecked` / `Icons.Rounded.RadioButtonUnchecked` — reused on the gallery state selector.

No new icon library is added and no icon SVG is bundled in `res/drawable/` or `res/raw/`.

## 3. Typography (Cycle 3)

- `HuaweiSyncTheme.typography.productSans` remains `FontFamily.SansSerif` (system default).
- `HuaweiSyncTheme.technicalTypography` remains `FontFamily.Monospace` (system default).
- Concept B defines `ConceptBTypography.displayHero` locally, but it is a local `TextStyle` built from the existing `MaterialTheme.typography.displayMedium` with a larger `fontSize` and `letterSpacing` — no new font family, no new font file, no new dependency.
- No `.ttf`, `.otf`, or `.woff` file is added to `app/src/main/res/font/`.

## 4. Colors and brand marks

- The single production accent (`HuaweiSyncColors.accent`) is unchanged. Concept B defines a local warm-shifted derivative (`pulseAccent`) inside `ConceptBTokens.kt` as an in-code `Color(...)`; it does not modify `HuaweiSyncColors`.
- Concept C defines local edge colors (`edgeDormant`, `edgeActive`, `edgeConfirmed`) inside `ConceptCTokens.kt` — all derived from `HuaweiSyncTheme.colors.ink`, `.accent`, and `.ok` via `.copy(alpha = …)`.
- No new material asset is added to `res/`.

## 5. Illustrations

Cycle 3 ships no bitmap illustration and no vector illustration. Every visual is Compose-drawn:

- Concept A's soft-tinted card backgrounds are `Modifier.background(brush = Brush.verticalGradient(...))` on `RoundedCornerShape` surfaces.
- Concept B's asymmetric plates are `Modifier.background(color, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 4.dp))` — no bitmap.
- Concept C's topology nodes and edges are drawn with `Canvas { … }` — no bitmap.
- Concept C's `surfaceLattice` dotted grid is drawn with `Modifier.drawBehind { drawCircle(…) }` in a loop — no bitmap tile.

## 6. Manifest table (Cycle 3)

| Name | Owner | Official URL | License / terms | Attribution | Local file | Production-approved |
| --- | --- | --- | --- | --- | --- | --- |
| material-icons-extended (Rounded set — Sync, Check, CheckCircle, ErrorOutline, HourglassEmpty, CloudSync, RadioButtonChecked, RadioButtonUnchecked) | Google | https://fonts.google.com/icons | Apache-2.0 | © Google, Apache-2.0 (already declared via Gradle dependency) | dependency: `androidx.compose.material:material-icons-extended` | yes |

No other Cycle-3 asset is shipped. The root `docs/design/assets/ASSET-MANIFEST.md` remains the single source of truth for future cycles.
