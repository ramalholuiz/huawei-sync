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
| material-icons-extended (Rounded set — full app-wide standard: Home, History, Hub, Info, Build, Timeline, List, Settings, AccessTime, Refresh, Bolt, Wifi, CloudOff, AutoAwesome, LockClock, ArrowForward, ArrowBack) | Google | https://fonts.google.com/icons | Apache-2.0 | © Google, Apache-2.0 (declared via Gradle dependency) | dependency: `androidx.compose.material:material-icons-extended` | yes |
| health-connect-wordmark (light) | Google | https://developer.android.com/health-and-fitness/guides/health-connect/develop/build-brand-experience | Third-party product identification per the source page; no separate SPDX; usage bound by the source rules. | "Health Connect" (paired with the icon per the source page's clear-space and colour rules). | *(not yet in-repo — landing with Cycle 10 per `docs/design/m007/cycle-04-implementation-roadmap.md § 4.6`; when landed, expected path `app/src/main/res/drawable/health_connect_wordmark_light.xml`)* | yes (approved for future shipping; not yet shipped) |
| health-connect-wordmark (dark) | Google | https://developer.android.com/health-and-fitness/guides/health-connect/develop/build-brand-experience | Same as light variant. | Same as light variant. | *(not yet in-repo — landing with Cycle 10; expected path `app/src/main/res/drawable/health_connect_wordmark_dark.xml`)* | yes (approved for future shipping; not yet shipped) |
| health-connect-icon | Google | https://developer.android.com/health-and-fitness/guides/health-connect/develop/build-brand-experience | Same as wordmark. | "Health Connect" text-label required alongside the icon per the source page. | *(not yet in-repo — landing with Cycle 10; expected path `app/src/main/res/drawable/health_connect_icon.xml`)* | yes (approved for future shipping; not yet shipped) |
| huawei-health-name-only | Huawei | https://consumer.huawei.com/en/support/brand/ | Text mention only per Huawei brand guidelines. | N/A for text mention. | text-only reference in copy | text: yes / mark: no |
| gymrats-name-only | GymRats | https://gymrats.app | No developer brand kit surfaced as of 2026-07-17. Default posture: text mention only. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires personal outreach + written permission) |
| samsung-health-name-only | Samsung | https://developer.samsung.com/health | Text mention only. Samsung Health SDK licence required for integration. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires Samsung brand approval + SDK licence) |
| strava-name-only | Strava, Inc. | https://developers.strava.com/guidelines/ | Text mention permitted. "Powered by Strava" lockup and badges require an approved OAuth app registration. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (post Gate 2 + Strava OAuth registration) |
| garmin-connect-name-only | Garmin Ltd. | https://developer.garmin.com/gc-developer-program/ | Text mention permitted. Marks require Garmin Connect developer program acceptance. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires Garmin developer program) |
| google-fit-name-only | Google | https://about.google/brand-resource-center/ | Text mention permitted under Google brand guidance. Product logos require Google brand approval. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires Google brand approval) |
| fitbit-name-only | Fitbit / Google | https://web.developer.fitbit.com/community-terms/ | Text mention only. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires Fitbit brand approval) |
| oura-name-only | Oura Health Ltd. | https://ouraring.com/press | Text mention only; no third-party developer usage rules surfaced. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires direct outreach + permission) |
| whoop-name-only | WHOOP, Inc. | https://www.whoop.com/us/en/press/ | Text mention only; no third-party developer usage rules surfaced. | N/A for text mention. | text-only reference in copy | text: yes / mark: no (requires direct outreach + permission) |
| huawei-sync-source-monogram (H) | in-house (this repo) | N/A | Original — first-party visual identity of Huawei Sync. | N/A. | Compose-drawn (`Text("H")` inside a tile chrome); no separate raster / vector asset ships in Cycle 4. | yes |
| watch-to-phone-motif | in-house (this repo, planned) | N/A | Original — commissioned in-house for Onboarding hero. | N/A. | *(not yet in-repo — planned for Cycle 15 per `docs/design/m007/cycle-04-implementation-roadmap.md § 4.11`)* | yes (approved for future shipping; not yet shipped) |

## Cycle 4 assessment (2026-07-17)

Cycle 4 is a **research + direction** pass. No new binary asset ships with it. The rows above marked *approved for future shipping; not yet shipped* record the approval decision made in `docs/design/m007/cycle-04-brand-assets.md` so a later implementation cycle can land the assets under the same terms.

Every row is traceable to a single source URL. Any future addition must:

1. Live in this manifest with `production-approved: yes` before landing in the APK.
2. Sit next to an adjacent `LICENSE` / `NOTICE` file when the source requires attribution beyond the mark itself.
3. Follow the size / colour / clear-space rules from the source URL — no re-tinting, no distortion.
4. Be traceable to the Cycle-4 brand-assets doc that approved it.
