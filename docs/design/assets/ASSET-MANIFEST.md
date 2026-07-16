# Asset manifest

Every visual asset that is a candidate for the APK is logged here. References that stay in documentation only are not required to appear in this file, but references that could plausibly be shipped must be listed and marked.

Columns:

- **Name** — short slug used in code / docs.
- **Owner** — the entity that holds rights to the asset.
- **Official URL** — the canonical source (documentation page, official brand kit, license repo).
- **License / terms** — SPDX identifier when available, or a short summary + link.
- **Attribution** — the exact attribution string that must ship if the asset ships.
- **Local file** — path inside this repo where the asset (or a documentation copy) lives.
- **Production-approved** — `yes` if the asset is cleared to ship in the APK, otherwise `no`.

Rules:

- Nothing pulled from the web enters the APK automatically. A candidate asset only becomes shippable after appearing here with `production-approved: yes`.
- No copied promotional imagery or illustrations from other apps.
- The Huawei logo is not used as primary Huawei Sync branding without later approval.
- Icons must be open source with the license preserved next to the asset (`LICENSE` sibling file).
- Illustrations that ship must be original, produced in-house.

| Name | Owner | Official URL | License / terms | Attribution | Local file | Production-approved |
| --- | --- | --- | --- | --- | --- | --- |
| material-icons-extended (Rounded set — Sync, Check, ErrorOutline, MoreHoriz) | Google | https://fonts.google.com/icons | Apache-2.0 | © Google, Apache-2.0 (declared via Gradle dependency) | dependency: `androidx.compose.material:material-icons-extended` | yes |
