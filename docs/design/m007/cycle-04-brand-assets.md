# Cycle 4 — Brand assets

**Cycle scope.** Research third-party brand assets Huawei Sync references (source watch, destination platforms, potential integrations). Establish per-asset usage rights, per-surface placement, and per-artifact production-approval verdicts. No asset ships in this cycle; all decisions are proposals for human sign-off.
**Role.** Brand Asset Researcher.
**Access date for every source below.** 2026-07-17.
**Hard rules re-stated from intake.**

- Only official sources: brand kits, developer documentation, press pages, official trademark pages, official repositories.
- Blocked sources: Icons8, Pinterest, blogs of logos, screenshots-extracted-to-SVG, manually vectorised logos.
- Do not alter colours or proportions of any third-party logo.
- Do not animate a logo when the brand prohibits it. Animate connections, containers, and states — not the mark itself.
- Do not make Huawei Sync look sponsored / endorsed / developed by any of these companies.
- Huawei Sync must own its own identity.
- **No asset researched here enters the APK during Cycle 4.**

## 1. Where logos are worth using in Huawei Sync (proposal)

Before we enumerate assets, we agree on where a logo would add value versus where it would only add noise. From the inventory (`cycle-04-app-inventory.md § 3.5`) and Option C in `cycle-04-information-architecture.md § 5`, logos help in exactly these surfaces:

| Surface | Would a logo help? | Rationale |
|---|---|---|
| Onboarding — mission line ("Huawei Health → Health Connect") | Yes, sparingly | A user identifying the two apps by their real marks understands what Huawei Sync connects. |
| Connections — Active path (Huawei Health · Sync · Health Connect · GymRats) | Yes | Provider tiles must be identifiable at a glance; monograms fail this test today. |
| Connections — Preview providers (Strava, Fitbit, etc.) | Yes if permitted | Same reason as active path. |
| Home — hero source/destination row | **No** | Home should read as *state*, not as a marketing wall. A monogram (single letter) is enough at the compact hero size (28dp). |
| Sync now sheet | **No** | The sheet is about the *coordinator*, not the brands. Text names only. |
| Activity / History rows | **No** for now | Rows are workouts, not brands. Once Huawei Health integration lands, a single subtle source badge is fine (see § 6). |
| Activity Detail — identity band | **No** | Same reason. |
| Diagnostics | **No** | Support tool; text names only. |

The pattern: **logos live where identification is the primary job** (Onboarding, Connections). Everywhere else, the state or the workout is the point, and monograms / text names are enough.

## 2. Rules for how logos can be shipped

If we ship any of the logos below, all of these must hold:

1. Source is an official brand kit or developer-brand documentation page (linked in § 3).
2. Local copy lives under `app/src/main/res/drawable/` with an adjacent `LICENSE` / `NOTICE` note referencing the source URL and the date of download.
3. Colour, proportion, and clear-space rules from the brand kit are respected — never re-tinted, never distorted, never composited on top of arbitrary backgrounds.
4. Minimum size, spacing, and background contrast requirements from the brand kit are respected. If the kit does not spell them out, Huawei Sync uses a 24dp minimum height with 8dp clear space on all sides.
5. No implication of endorsement in copy — never "Powered by X", "In partnership with X", or "Official X app" unless X has approved that specific text.
6. The APK strings include the required attribution string as documented (e.g. Health Connect wordmark usage rules — see § 3.1).
7. Logo assets are not animated. Motion happens around them (containers, connections, states — see `cycle-04-motion-system.md § 8`).

Any asset that cannot meet all seven remains **`production-approved: no`** until Cycle 4's follow-up gets an explicit yes from the source or a human decision to ship anyway.

## 3. Per-brand assessment

Columns per row:

- **Brand / product.** Legal owner and the specific product surface Huawei Sync would reference.
- **Official name to use.** The spelling required by the brand kit; matches copy verbatim.
- **Official source.** URL of the brand kit / developer-brand page consulted.
- **Format(s).** File formats the brand kit distributes.
- **Variants.** Colour, mono, wordmark, symbol-only, dark/light.
- **Allowed usage.** What the kit explicitly permits for a third-party developer.
- **Restrictions.** What the kit explicitly prohibits.
- **Required attribution.** Exact string, when required.
- **Ship in internal prototype?** May we render it inside the debug prototype gallery on our own devices, not distributed.
- **Ship in a distributed APK?** May we render it in the debug or release APK we sideload for personal use — no Play Store distribution.
- **Extra authorisation needed?** Whether a human still needs to reach out or confirm.
- **`production-approved`.** `yes` / `no` for a Play-Store-distributed APK.

### 3.1 Health Connect (Google) — the destination platform

| Field | Value |
|---|---|
| Brand / product | Google · **Health Connect** (Android) |
| Official name to use | "Health Connect" (spaced, both words capitalised) |
| Official source | https://developer.android.com/health-and-fitness/guides/health-connect/develop/build-brand-experience |
| Format(s) | SVG + PNG (@1x/@2x/@3x) — provided in the brand-experience page |
| Variants | Colour (default), monochrome light-on-dark, monochrome dark-on-light. Wordmark ("Health Connect") + icon (heart-in-badge). |
| Allowed usage | Third-party apps that read from or write to Health Connect may show the Health Connect icon + wordmark in their in-app "Connections" / "Providers" surface and on Onboarding, following the size / clear-space / colour rules on the source page. |
| Restrictions | No re-colouring; no composition with the app's own logo to form a new lockup; no implication that the app is Google or an official Google product; no use in advertising without explicit permission. |
| Required attribution | Per the source page: text credit "Health Connect" must be spelled correctly and paired with the icon. No small-print attribution string required in the app for standard in-product identification, but the source page must be cited in-repo (see § 2 rule 2). |
| Ship in internal prototype? | Yes. |
| Ship in a distributed APK? | Yes — sideloaded personal APK is inside the terms of "in-app product identification" documented at the source page. |
| Extra authorisation needed? | No, provided the size / clear-space rules on the source page are followed. |
| `production-approved` | **yes** |

Notes: this is the single strongest asset case. Health Connect is a first-party platform we integrate against; Google explicitly maintains a brand-experience page to make this correct. Placement: Onboarding hero, Connections `Active path` tile, Connections detail sheet for the Health Connect provider.

### 3.2 Huawei Health — the source app

| Field | Value |
|---|---|
| Brand / product | Huawei · **Huawei Health** (Android app) |
| Official name to use | "Huawei Health" |
| Official source | https://consumer.huawei.com/en/support/brand/ (Huawei brand guidelines — restrictive) |
| Format(s) | Not distributed for third-party developer use. |
| Variants | N/A. |
| Allowed usage | Text mention only, per Huawei's brand guidelines. |
| Restrictions | Third-party use of Huawei's marks (including "Huawei" wordmark, device photography, and app-icon copies) is not permitted outside authorised programs. |
| Required attribution | N/A when only the text name is used. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — to use the Huawei Health app icon we would need Huawei brand approval, which is not warranted for a personal sideloaded utility. |
| `production-approved` | **no** (for the mark). Text name is fine. |

Notes: this is the source app; users will identify it by name. We use the name in copy and represent it visually with **our own** source glyph — a small watch mark inside a rounded tile, not the Huawei mark. The Cycle 4 assumption is that we do not ship the Huawei watch photo or logo. This is important: the app's whole positioning is "yours, not Huawei's".

### 3.3 GymRats — the currently-targeted downstream consumer

| Field | Value |
|---|---|
| Brand / product | GymRats |
| Official name to use | "GymRats" |
| Official source | https://gymrats.app (product site) |
| Format(s) | Not published for third-party developer use as of access date. |
| Variants | Unknown. |
| Allowed usage | No developer brand kit surfaced. Default posture: **text mention only**. |
| Restrictions | Cannot ship the logo without written permission. |
| Required attribution | N/A when only the text name is used. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — a personal email to the GymRats team requesting permission to render their icon inside the Connections tile is warranted if we want a real logo. Until we ship a logo, our own "G" monogram remains the placeholder. |
| `production-approved` | **no** (for the mark). Text name is fine. |

Notes: text-only, until a human explicitly asks and receives permission.

### 3.4 Samsung Health

| Field | Value |
|---|---|
| Brand / product | Samsung · **Samsung Health** |
| Official name to use | "Samsung Health" |
| Official source | https://developer.samsung.com/health (Samsung Health developer portal) |
| Format(s) | Not distributed as a third-party-usable brand kit. |
| Variants | Unknown. |
| Allowed usage | Text name only; SDK usage requires the Samsung Health SDK licence. |
| Restrictions | Samsung branding is under Samsung's brand guidelines; logo usage requires approval. |
| Required attribution | N/A for the name. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — Samsung Health SDK access requires their approval; that is out of Cycle 4 scope. |
| `production-approved` | **no** (for the mark). |

### 3.5 Strava

| Field | Value |
|---|---|
| Brand / product | Strava, Inc. |
| Official name to use | "Strava" |
| Official source | https://developers.strava.com/guidelines/ |
| Format(s) | Strava distributes an official brand kit including SVG marks. |
| Variants | Full-colour wordmark, monochrome, orange square, "Powered by Strava" lockup. |
| Allowed usage | Third-party apps that integrate Strava can use the "Powered by Strava" lockup and specific badges once the app has an approved OAuth app registration. |
| Restrictions | No modifications to the marks; the "Powered by Strava" lockup must be shown next to Strava-sourced content; strict rules on where and when the mark can appear. |
| Required attribution | The badges themselves include "Strava" attribution; the docs specify their required minimum sizes. |
| Ship in internal prototype? | Yes, once OAuth app registration is filed (a real prerequisite even for prototyping post-Gate-2). Until then, text only. |
| Ship in a distributed APK? | Yes, once OAuth app registration is approved and Gate 2 has passed. |
| Extra authorisation needed? | Yes — Strava OAuth app registration. |
| `production-approved` | **no** for Cycle 4 (Gate 2 has not passed). |

### 3.6 Garmin Connect

| Field | Value |
|---|---|
| Brand / product | Garmin Ltd. · **Garmin Connect** |
| Official name to use | "Garmin Connect" |
| Official source | https://developer.garmin.com/gc-developer-program/ |
| Format(s) | Marketing marks distributed to approved developer-program members. |
| Variants | Wordmark + icon. |
| Allowed usage | Requires acceptance into the Garmin Connect developer program. |
| Restrictions | Extensive; text mention permitted for reference. |
| Required attribution | Per Garmin developer program agreement — text mention does not require it. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — the Garmin Connect developer program. |
| `production-approved` | **no** for Cycle 4. |

### 3.7 Google Fit

| Field | Value |
|---|---|
| Brand / product | Google · **Google Fit** |
| Official name to use | "Google Fit" |
| Official source | https://about.google/brand-resource-center/ |
| Format(s) | Not published for third-party developer use as a standalone product mark. |
| Variants | N/A. |
| Allowed usage | Text mention only under Google's general brand guidance. |
| Restrictions | The Google logo and product logos require explicit permission for reproduction in third-party apps. |
| Required attribution | Per Google brand guidance. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — Google brand approval. |
| `production-approved` | **no** for the mark. |

### 3.8 Fitbit

| Field | Value |
|---|---|
| Brand / product | Fitbit / Google |
| Official name to use | "Fitbit" |
| Official source | https://web.developer.fitbit.com/community-terms/ |
| Format(s) | Not distributed for third-party in-app product identification. |
| Variants | N/A. |
| Allowed usage | Text name only. Fitbit's community terms restrict logo use. |
| Restrictions | Extensive. |
| Required attribution | N/A for text. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes. |
| `production-approved` | **no** for the mark. |

### 3.9 Oura

| Field | Value |
|---|---|
| Brand / product | Oura Health Ltd. · **Oura Ring** |
| Official name to use | "Oura" |
| Official source | https://ouraring.com/press (press page — no developer usage rules discovered from public sources as of access date) |
| Format(s) | Press-download images (marketing assets), not product-brand kit. |
| Variants | Marketing photography. |
| Allowed usage | Not established; text mention safe. |
| Restrictions | Marketing photography is not intended for in-app third-party use. |
| Required attribution | N/A for text. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes — direct outreach if we want to ship anything more than the name. |
| `production-approved` | **no** for the mark. |

### 3.10 WHOOP

| Field | Value |
|---|---|
| Brand / product | WHOOP, Inc. |
| Official name to use | "WHOOP" (all caps) |
| Official source | https://www.whoop.com/us/en/press/ (press page — no developer usage rules discovered from public sources) |
| Format(s) | Press-download images. |
| Variants | N/A. |
| Allowed usage | Not established; text mention safe. |
| Restrictions | Same as Oura. |
| Required attribution | N/A for text. |
| Ship in internal prototype? | Text name only. |
| Ship in a distributed APK? | Text name only. |
| Extra authorisation needed? | Yes. |
| `production-approved` | **no** for the mark. |

### 3.11 Icons that *are* production-approved

We already ship one open-source icon set:

| Asset | Owner | Source | Licence | Attribution | Ship location | `production-approved` |
|---|---|---|---|---|---|---|
| Material Symbols (Rounded weight) — `Sync`, `Check`, `ErrorOutline`, `MoreHoriz`, `Home`, `Timeline`, `Settings`, `Info`, `Build`, `List`, `History`, `AccessTime`, `Refresh`, `Bolt`, `Wifi`, `Hub`, `ArrowForward`, `ArrowBack`, `AutoAwesome`, `LockClock`, `CloudOff` | Google | https://fonts.google.com/icons | Apache-2.0 | © Google, Apache-2.0 — declared via the `androidx.compose.material:material-icons-extended` dependency in `libs.versions.toml`. | App-wide. | **yes** |

Standardise on the **Rounded** weight app-wide (`cycle-04-visual-system.md § 8`). Retire the mixed use of `Filled` / `Outlined` variants.

## 4. Rollup table

| Brand / mark | `production-approved` | Blocker to move to `yes` |
|---|---|---|
| Health Connect (Google) — icon + wordmark | **yes** | — |
| Material Symbols (Rounded) | **yes** | — |
| Huawei Health — text | yes (text only) | — |
| Huawei Health — mark | no | Huawei brand approval (not warranted for MVP). |
| GymRats — text | yes (text only) | — |
| GymRats — mark | no | Personal outreach + permission. |
| Samsung Health — text | yes (text only) | — |
| Samsung Health — mark | no | Samsung brand approval + SDK licence. |
| Strava — text | yes (text only) | — |
| Strava — "Powered by" lockup | no | Gate 2 pass + Strava OAuth app registration. |
| Garmin Connect — text | yes (text only) | — |
| Garmin Connect — mark | no | Garmin developer program acceptance. |
| Google Fit — text | yes (text only) | — |
| Google Fit — mark | no | Google brand approval. |
| Fitbit — text | yes (text only) | — |
| Fitbit — mark | no | Fitbit brand approval. |
| Oura — text | yes (text only) | — |
| Oura — mark | no | Direct outreach + permission. |
| WHOOP — text | yes (text only) | — |
| WHOOP — mark | no | Direct outreach + permission. |

## 5. What Cycle 4 will drop into `docs/design/assets/ASSET-MANIFEST.md`

Cycle 4 does not ship any new binary asset. The manifest update in this cycle:

- Records that the Health Connect icon + wordmark are cleared for future shipping *conditionally on human approval to proceed*.
- Records that no other third-party mark is production-approved.
- Preserves the existing `material-icons-extended` row.

Any bitmap or SVG that later ships will be added to the manifest with the rules in § 2, in a separate implementation cycle.

## 6. Where the marks live *if* Cycle 5+ ships them

Assuming human approval in § 8:

- **Onboarding hero, second line.** "Send workouts from Huawei Health into Health Connect." Beside the "Health Connect" text: the Health Connect wordmark **or** icon (single-choice, not both at 24dp).
- **Connections → Active path.** Each of the four tiles (Huawei · Sync · Health Connect · GymRats) shows: our own tile chrome + the provider's mark (when approved) *inside* the tile. Huawei Health, GymRats, and Sync itself remain monograms today.
- **Connections → per-provider detail sheet.** The provider's official mark at 32dp at the top of the sheet, with the mark's minimum clear space around it. Only rendered for providers with `production-approved: yes` on the mark.
- **Post-Gate-3, Activity rows.** A `SOURCE` glyph at 16dp on the left of each row, keyed to which provider originated the workout. Huawei Health starts here as a monogram, upgrades to a mark only after Huawei brand approval.

## 7. What we deliberately do not do

- We do not ship the Huawei mark, colour palette, or watch photography.
- We do not build a lockup that composites Huawei Sync's own mark with any third-party mark.
- We do not tint any third-party logo to fit our palette.
- We do not animate any third-party logo (containers and connections may animate, per the motion doc).
- We do not "Powered by X" any surface.
- We do not ship Undraw / Icons8 / Pinterest-sourced illustration.
- We do not import Rive / Lottie for logo motion.

## 8. Human decisions required to close brand assets

1. **Approve Health Connect wordmark + icon for shipping in Onboarding and Connections** (rules per § 3.1 must be followed). Blocked otherwise.
2. **Approve text-only treatment for every other third-party product** for MVP (Huawei Health, GymRats, Samsung Health, Strava, Garmin Connect, Google Fit, Fitbit, Oura, WHOOP).
3. **Decide whether to send a permission request to GymRats** for the mark. If yes, someone owns the outreach and reports back the outcome.
4. **Confirm Huawei Sync ships its own mark** — text + monogram is fine for MVP; a proper logo mark is out of Cycle 4 scope but tracked as a follow-up.
5. **Standardise Material Symbols Rounded** app-wide — not a legal question, but a system decision that affects every screen in the visual system doc.
