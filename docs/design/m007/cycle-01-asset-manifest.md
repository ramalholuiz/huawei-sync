# Cycle 1 — Asset manifest

Cycle-1-scoped subset of `docs/design/assets/ASSET-MANIFEST.md`. Only assets touched or shipped in Cycle 1 are listed here.

Access date for every row: 2026-07-16 (session date).

## 1. Iconography

Cycle 1 continues to draw icons from the icon set already bundled via the `androidx.compose.material:material-icons-extended` dependency (Apache-2.0, Google). New icon uses in Cycle 1:

- `Icons.Rounded.Sync` — reused on the rail active-node medallion (already used in `SyncNowModal`, `PipelineScreen`, `ModernistComponents`).
- `Icons.Rounded.Check` — reused on the confirmed one-shot (already used in the same files).
- `Icons.Rounded.ErrorOutline` — reused on error state (already used in `PipelineScreen`).
- `Icons.Rounded.MoreHoriz` — reused on pending state (already used in `PipelineScreen`).

No new icon library is added.

## 2. Typography

- `ProductSans` remains `androidx.compose.ui.text.font.FontFamily.SansSerif` (system default). No font file is bundled.
- `TechnicalMono` remains `androidx.compose.ui.text.font.FontFamily.Monospace` (system default). No font file is bundled.
- Cycle 1 does not add a custom font. If a future cycle adds one, it lands here first with a `production-approved: yes` row and a matching SPDX license.

## 3. Colors and brand marks

- No third-party brand mark (Huawei, Health Connect, GymRats, Fitbit, Whoop, or any other) is used as a visual asset in the APK.
- The monogram characters `H`, `S`, `HC`, `G` in the rail are ASCII typography, not logos.
- The single accent color `HuaweiSyncColors.accent` remains unchanged.

## 4. Illustrations

Cycle 1 ships no bitmap or vector illustration. All rail geometry is drawn with Compose primitives (`Box`, `Row`, `Column`, `Canvas`).

## 5. Manifest table (Cycle 1)

| Name | Owner | Official URL | License / terms | Attribution | Local file | Production-approved |
| --- | --- | --- | --- | --- | --- | --- |
| material-icons-extended (Rounded set — Sync, Check, ErrorOutline, MoreHoriz) | Google | https://fonts.google.com/icons | Apache-2.0 | © Google, Apache-2.0 (already declared via Gradle dependency) | dependency: `androidx.compose.material:material-icons-extended` | yes |

No other Cycle-1 asset requires shipping. The root `docs/design/assets/ASSET-MANIFEST.md` remains the single source of truth for future cycles.
