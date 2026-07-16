# Interactive prototype implementation notes

## Source and provenance

- Visual source of truth: `HuaweiSync.dc.html` and its bundled `_ds/` design-system assets. The HTML is an implementation reference only; it must not be embedded or shipped as the Android runtime UI.
- Preserved archive: `Interactive prototype.zip`, copied from `~/Downloads/Interactive prototype.zip`.
- SHA-256 (both source and preserved copy): `29f0bdebd056197a1981f723bb52055897b61a9bf4e550f461880e5a4d9ce534`.
- The preserved archive is 67,556 bytes. Keep the ZIP and extracted source unchanged so future visual comparisons remain reproducible.

## Ten-screen inventory

1. **Onboarding — welcome:** four-step setup entry, Huawei-to-Health Connect-to-destination story, primary setup action, and restore affordance.
2. **Dashboard:** sync summary, connection cards, health tiles, recent activity, issue card, bottom navigation, and the primary red sync action. The prototype includes live copy changes for syncing and completion.
3. **Live sync pipeline:** read-only source-to-hub pipeline with throughput, four processing stages, destinations, and recent events.
4. **Sync now — particle sheet:** modal progress presentation with destination particles, item count, progress percentage, remaining-time estimate, and completion celebration.
5. **Integrations marketplace:** Health Connect as the required hub plus connected, available, paused, and coming-soon destination cards.
6. **Diagnostics center:** prioritized critical, warning, and informational issue cards with explanations and contextual recovery actions.
7. **Automation — rules as cards:** active, paused, and suggested schedule/condition cards expressed as sentences rather than a switch table.
8. **History timeline:** time-range filters, daily totals, delivery outcomes, retries, queued items, and replay affordance.
9. **Activity detail — running:** map treatment, workout summary, heart-rate zones, per-destination delivery state, and queued-item push action.
10. **AI Assistant:** prompt chips, conversational answers, and contextual actions based on claimed access to data, destinations, and logs.

## Color tokens

Values below are copied exactly from the HTML. Unoverridden semantic colors remain identical in both themes.

| Token | Dark | Light |
|---|---|---|
| `bg` | `#0b0a09` | `#f3f2f2` |
| `sf1` | `#131211` | `#ffffff` |
| `sf2` | `#1c1a18` | `#eae9e9` |
| `sf3` | `#26231f` | `#d7d3d3` |
| `ink` | `#f3f2f2` | `#201e1d` |
| `ink2` | `rgba(243,242,242,0.62)` | `rgba(32,30,29,0.62)` |
| `ink3` | `rgba(243,242,242,0.38)` | `rgba(32,30,29,0.42)` |
| `ink4` | `rgba(243,242,242,0.18)` | `rgba(32,30,29,0.20)` |
| `line` | `rgba(243,242,242,0.10)` | `rgba(32,30,29,0.10)` |
| `line2` | `rgba(243,242,242,0.22)` | `rgba(32,30,29,0.22)` |
| `canvas-bg` | `#050403` | `#e7e3de` |
| `accent` | `#ec3013` | `#ec3013` (inherited) |
| `accent-soft` | `rgba(236,48,19,0.14)` | same (inherited) |
| `ok` | `#4ade80` | same (inherited) |
| `warn` | `#f5a524` | same (inherited) |
| `info` | `#6ea8ff` | same (inherited) |

## Typography

- Primary family: **Archivo**, requested at weights 400, 500, 600, 700, 800, and 900; CSS fallback is exactly `system-ui, sans-serif`.
- Technical labels and numeric/data treatments use **JetBrains Mono**, requested at weights 400 and 600; native implementation should fall back to the platform monospace family.
- Preserve the prototype's hierarchy: heavy, tightly tracked display headings; compact uppercase labels with wide positive tracking; tabular-looking operational values; and restrained body copy. Do not rely on downloading Google Fonts at runtime. Bundle an approved font resource if licensing and APK policy allow it, otherwise use the documented platform fallbacks and accept metric differences.

## Reusable components

- App scaffold: top status/header region, bottom destination navigation, and floating primary sync action.
- `StatusPill`/badge: connected, hub, paused, queued, live, critical, warning, info, and coming-soon variants.
- Surface primitives: elevated card, outlined card, section header, metric tile, icon/avatar tile, divider, progress bar, and modal sheet.
- Domain presentations: connection card, pipeline stage, event row, diagnostic issue card, automation rule card, history event, destination delivery row, workout statistic, heart-zone bar, prompt chip, and assistant message.
- Components should consume app/domain presentation models. Never let HTML, Huawei transfer objects, or Health Connect records become UI models.

## Navigation, interactions, and animation

- Bottom navigation concepts are Home, History, Apps, Rules, and More. Cards drill into pipeline, diagnostics, integrations, history, and activity detail; back navigation must preserve the prior screen state.
- Primary interactions shown are setup/restore, manual sync, re-scan, contextual recovery actions, range filters, destination push/retry, rule activation, suggested-rule acceptance, prompt selection, and theme switching.
- The source defines `hs-pulse`, `hs-flow-down`, `hs-flow-h`, `hs-orbit`, `hs-orbit-rev`, `hs-ring`, `hs-rise`, `hs-bar`, `hs-shimmer`, `hs-blink`, `hs-fadein`, `hs-slide`, `hs-confetti`, `hs-tick`, and `hs-glow`. Use these as motion intent, not a requirement to reproduce web CSS literally.
- Motion must communicate state: flow for active transfer, rise/fade for entry, bar/ring for bounded progress, tick/confetti for confirmed completion, pulse/blink for live state, and glow for emphasis. Never animate a successful delivery before the coordinator has durable evidence. Respect reduced-motion by replacing orbit/flow/confetti and repeated pulsing with immediate state changes or a static indicator.

## Responsive and accessibility notes

- Treat the phone compositions as a narrow-screen reference, not fixed coordinates. Use safe-area/system-bar insets, scrollable content, adaptive spacing, and width constraints so cards remain readable on compact phones and do not stretch excessively on tablets.
- At large font scales, allow labels and actions to wrap; do not clip counts, error explanations, or recovery actions. Avoid encoding status solely through red/green/orange color—pair every state with text and/or an icon.
- Provide semantic headings, meaningful content descriptions where icons convey information, grouped card semantics, announced progress updates, and minimum 48 dp touch targets. Decorative map/particle/confetti elements should be hidden from accessibility services.
- Maintain contrast in both themes, especially translucent `ink3`/`ink4` and `line` tokens; promote text to a stronger token when the exact decorative token cannot meet the required contrast.
- Keyboard/D-pad focus order should follow visual order, sheets must trap focus and restore it when dismissed, and all actions need stable accessible labels independent of animation.

## Real-state sources

The native UI must render facts from existing application seams rather than prototype timers or invented sample data:

- Health Connect availability and permission state: existing availability, permissions, and preflight policies.
- Manual sync phase and outcome: `Gate1SyncCoordinator`/`Gate1SyncResult` typed states.
- Durable attempt, acceptance, confirmation, reconciliation, timestamps, and errors: Room ledger through `SyncLedgerStore`; UI must not reconstruct ledger transitions.
- Health Connect record count/version confirmation: `HealthConnectWorkoutInspector` typed inspection results.
- User-facing diagnostics/export copy and recovery guidance: `Gate1Diagnostics` and `Gate1RuntimeDiagnostics` exhaustive models.
- The current vertical slice supports the synthetic `ExerciseSessionRecord` Gate 1 flow. Empty, unavailable, permission-blocked, writing, accepted-but-unconfirmed, confirmed, reconciliation-pending, and failed states must remain distinguishable.

## Prohibited and preview-only claims

- **Prohibited as real behavior now:** Huawei Health authorization or workout reads; Strava, GymRats, Google Fit, Samsung Health, Oura, or Apple Health connections/delivery; background or 30-minute sync; post-workout triggers; Wi-Fi/charging rules; replay; destination push; AI diagnosis; learned suggestions; throughput/remaining-time promises; and two-way sync.
- **Prohibited metrics now:** steps, heart rate, sleep, calories, SpO₂, weight, hydration, distance, pace, heart zones, and map data. Project gates explicitly defer extra metrics until Gate 1 and Gate 2 pass.
- **Preview/sample only:** all named apps beyond Health Connect, item totals, timestamps, success percentages, battery-savings estimates, workout details, destination statuses, issue causes, assistant answers, and animation progress in the HTML. Preview UI must be labeled clearly and must not resemble live evidence.
- Do not claim GymRats visibility until manual Gate 2 validation exists. Do not claim Huawei connectivity until official authorization and a real read satisfy Gate 3. Do not add Strava before Gates 1 and 2 pass, and never import the same workout from multiple sources without deterministic deduplication.
- Never expose raw health data or sensitive payloads in general logs. Diagnostic output must stay privacy-safe and derive from the existing typed diagnostics model.

## Native Compose token translation

- `DarkHuaweiSyncColors` and `LightHuaweiSyncColors` preserve every audited HTML palette value. Material roles are adapters only; prototype-only roles (`sf3`, translucent inks and rules, canvas, and status colors) remain available through `HuaweiSyncTheme.colors`.
- The bundled Modernist scale maps `4/8/12/16/24/32 px` to density-independent `dp`. Its zero-radius shape tokens stay zero in every Material shape slot; 1 dp and 2 dp rules provide the primary hierarchy.
- CSS shadow y-offsets map to 1, 3, and 12 dp elevation tokens. CSS blur and mixed shadow color have no exact Material elevation equivalent, so components must apply them explicitly when required; structural surfaces should prefer rules and zero elevation.
- No trustworthy licensed Archivo or JetBrains Mono files exist in the preserved source or repository, and no fonts were downloaded. Archivo therefore maps to Android `FontFamily.SansSerif` (normally the device system sans), while JetBrains Mono maps to `FontFamily.Monospace`. The resulting glyph widths, line breaks, and weight interpolation can differ by device and must be checked in screen-level previews.
- The Material type scale follows audited HTML values: 56/32/30 dp display styles, 28/26/22 dp headlines, compact 20/15/13 dp titles, and 18/13.5/12 dp body copy. Prototype technical labels and values remain a separate monospace scale through `HuaweiSyncTheme.technicalTypography`.
- Dynamic color is intentionally disabled: replacing the audited palette with wallpaper-derived colors would violate the approved visual source. Theme selection follows the Android system setting unless a preview or test explicitly selects dark or light.

## S07 accessibility and responsive audit

- The audited inventory is exactly the ten prototype destinations: onboarding, dashboard, pipeline, sync modal, integrations, diagnostics, automation, history, activity detail, and local assistant. Each destination's representative state now opts into `HuaweiSyncScreenshotPreviews`, producing compact light (390 × 844), compact dark at 1.3× font scale, expanded light (1000 × 700), and expanded dark at 2× font scale. The sync modal and activity detail are explicit matrix members rather than being inferred from parent screens.
- Responsive fallback: the shell keeps the 760 dp compact/expanded navigation breakpoint. Shared section headers stack trailing actions below the heading below 420 dp or from 1.3× font scale, preserving flush-left straight-edge geometry while avoiding heading/action overlap. Long content remains vertically scrollable; the prototype does not invent a landscape-only information hierarchy.
- TalkBack and focus fallback: interactive rows expose button/tab roles and names, selected navigation exposes selected state, progress exposes range semantics, the sync dialog announces a pane title and traps focus through Compose `Dialog`, and declaration order remains the reading/focus order. The compact-menu scrim is explicitly named as a dismiss action. Decorative icons remain silent so labels are not announced twice.
- Target-size fallback: shared buttons, bottom navigation, the sync FAB, wide navigation rows, assistant prompts, and the assistant clear action enforce at least 48 dp. Disabled preview controls remain disabled and are described as unavailable; they are not made focusable by claiming a runtime capability.
- Contrast fallback: the raw HTML `accent`, `prototypeInk2`, `ink3`, and `ink4` tokens remain preserved for provenance, borders, and decoration. Runtime `ink2` is the accessible secondary-content fallback (`#AAA8A8` dark, `#5B5755` light); accent text/icons use theme-aware `accentForeground` (`#FF6B55` dark, `#A61B12` light); white-on-red controls use `accentContainer` (`#DF2B10`). These are deliberate accessibility substitutions because the imported red with white is 4.20:1 and the translucent secondary/tertiary inks fall below 4.5:1 on supported surfaces. Regression tests calculate contrast rather than relying on token names.
- Reduced motion is centralized in `HuaweiSyncMotionProvider`: Android animator scale zero selects zero-duration policies, suppresses sync rotation, and snaps progress. Only pipeline/sync primitives animate, so their dedicated reduced-motion previews are the truthful visual checks; static destinations do not receive fabricated motion variants.
- Verified fallback limits: Compose previews validate deterministic layout inputs but are not a replacement for TalkBack audio order, OEM font metrics, or screenshot pixel comparison on hardware. Those remain manual/runtime checks for T03; regression coverage guards the 48 dp prompt target, large-font header stacking, palette contrast, and all-ten preview matrix membership without claiming runtime GymRats or Huawei behavior.

## T02 static and build verification

Verification was run locally on 2026-07-16. This section records build and static evidence only; no APK was installed and no emulator, TalkBack, GymRats, Huawei authorization, or Health Connect runtime evidence was produced.

- `./gradlew testDebugUnitTest`: 184 tests, 0 failures, 0 errors, 0 skipped across 33 suites.
- `./gradlew testReleaseUnitTest`: 184 tests, 0 failures, 0 errors, 0 skipped across 33 suites.
- `./gradlew clean test lint assembleDebug`: passed; 368 unit-test executions across debug and release, 0 failures, 0 errors, 0 skipped.
- `scripts/verify.sh`: passed; its clean test/lint/debug-assemble pipeline executed the same 368 debug/release unit tests with 0 failures, 0 errors, and 0 skipped.
- Android lint: 0 errors and 18 warnings (`AndroidGradlePluginVersion` 2, `GradleDependency` 10, `KaptUsageInsteadOfKsp` 1, `MissingApplicationIcon` 1, `ModifierParameter` 2, `OldTargetApi` 1, `UnusedResources` 1).
- `git diff --check`: passed with no whitespace errors.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk` (18,466,784 bytes), SHA-256 `f34c77b79238fc6dbe7eb269f64ae81a047793c9f552b9981af114fa410f8b37`.

Static scope audit:

- All ten destinations opt into `HuaweiSyncScreenshotPreviews`, which provides compact light, compact dark at 1.3x font scale, expanded light, and expanded dark at 2x font scale. Unit coverage asserts exactly ten unique typed destinations and all-ten matrix membership. This is preview/build evidence, not a claim of runtime visual inspection.
- The claims policy remains fail-closed: Health Connect confirmation requires exact readback evidence; write acceptance alone cannot claim confirmation; GymRats copy cannot imply Gate 2 completion; the assistant is local, canned, free-form-disabled, and declares that it contacts no remote service.
- Dependency and source audits found no WorkManager dependency or usage, no remote-AI SDK or endpoint, and no Huawei, Strava, or other external integration SDK added by this prototype.
- No coordinator source file is changed. Presentation routing continues to call the existing coordinator/runtime boundaries; T02 does not rewrite coordinator logic.

## T03 provisional runtime evidence

Provisional runtime evidence was produced on `emulator-5554` (API 35) from tested HEAD `6082796c8d888db69a755545a47a0875caf420d6` using an installed APK with SHA-256 `fab5ee651e09900dd4eae036c58b184de2bed1020e5fdaf46079e9279b48f50b`. Health Connect permission was granted. The first action was accepted, the second was confirmed by official Health Connect readback, and the third returned `ALREADY_VERIFIED`.

Refreshed diagnostics reported durable status `VERIFIED`, 1 Room row, 1 attempt, 1 Health Connect match, 1 expected-version match, and exact version `true`. GymRats remains **Ready for GymRats to read**; Gate 2 has not started.

This evidence remains provisional pending a repeat on the documentation commit.
