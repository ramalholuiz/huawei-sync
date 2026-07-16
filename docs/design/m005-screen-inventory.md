# M005 · Screen Inventory

Captured 2026-07-16 against commit `4538c47` on branch `milestone/M005`, worktree `~/Projetos/huawei/huawei-sync/.gsd-worktrees/M005`, running the debug APK `app/build/outputs/apk/debug/app-debug.apk` (18,466,784 bytes, SHA-256 `18a7b0ab961ee2d2da41770a5d9d1dca96d3823fe8a04194b71a00be85f7986c`) on emulator `HuaweiSync_API_35` (`emulator-5554`, Android 15, 1080×2400 @ 420dpi, `sys.boot_completed=1`). No app code was modified.

Screenshots live in `docs/design/m005-screenshots/` and are referenced below by filename.

The Compose destinations enumerated in `HuaweiSyncDestination.kt` are: `Onboarding`, `Dashboard`, `Pipeline`, `SyncNow` (modal), `Integrations` (labeled "Settings" in the compact menu), `Diagnostics`, `Automation`, `History`, `ActivityDetail`, `AiAssistant`.

## 01. Dashboard

- Route: `dashboard`. Default landing surface. Compact bottom-nav tab 1.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/dashboard/DashboardScreen.kt`.
- Composition: `DashboardTopBar` (H mark, title "Dashboard" / eyebrow `LIVE PRODUCT STATE`, `DARK`/`LIGHT` toggle) → `SyncHero` (status label + verbose sync-state phrase + phase microcopy + red rule progress) → `Recent activity` ledger metrics → `Connected services` monogram list (HC + GR) → `Verification` readback summary → SyncFab overlaid bottom-right.
- Captured variants:
  - `01-dashboard-default.png` — seeded state, phase `VERIFICATION` at 80%, "Confirmed in Health Connect", 1 workout, GR ready.
  - `01b-dashboard-scrolled.png` — Verification card + spinner + attempt facts.
  - `01-dashboard-dark.png` — same seeded state on the dark palette (`Light` toggle label).
  - `01c-dashboard-empty-permission.png` — first-run state after `pm clear`: yellow `ATTENTION` hero with `Health Connect permission required` + red `REVIEW PERMISSION` button, empty ledger, HC row status `HEALTH CONNECT PERMISSION REQUIRED`.
  - `01d-dashboard-empty-scrolled.png` — same, scrolled to Verification (`Readback not yet confirmed`).

## 02. Pipeline

- Route: `pipeline`. Compact bottom-nav tab 2. Read-only.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/pipeline/PipelineScreen.kt`.
- Composition: title "Live sync pipeline" + eyebrow `LIVE · READ-ONLY` → SOURCE card ("Gate 1 synthetic workout" / "Huawei Health import is not connected yet.") → coordinator phase evidence timeline (`Preflight`, `Write`, `Acceptance`, `Readback verification`, `Reconciliation`) with COMPLETE/PENDING chips → `ANDROID HUB` card (Health Connect / Confirmed in Health Connect) → `DESTINATION CLAIM` card (GymRats).
- Captured variants:
  - `02-pipeline.png` — light theme, verified state.
  - `02b-pipeline-scrolled.png` — GymRats destination claim card.
  - `02-pipeline-dark.png` — dark theme.

## 03. History

- Route: `history`. Compact bottom-nav tab 3. Read-only ledger.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/history/HistoryScreen.kt`.
- Composition: title "Activity history" + eyebrow `READ-ONLY LEDGER` → `1 DURABLE RECORD` count → card list (per-row: `SYNTHETIC`, ledger acceptance state `Verified`, `VERIFIED` chip, UTC timestamp, monospaced client-record ID truncated with ellipsis, `ATTEMPTS 1 · RECORD VERSION 1`).
- Captured variants:
  - `03-history.png` — one verified synthetic record.
  - `03-history-dark.png` — dark theme.
  - `13b-history-empty.png` — post-`pm clear` empty state: `No ledger activity / A workout appears here only after a durable ledger row exists.` The empty card floats mid-screen at ~y=60%, not centered.

## 04. More menu (compact-only overlay)

- Route: overlay from `MoreNavigationKey`. Compact bottom-nav tab 4.
- Composition: dim scrim (94% opacity) + bottom sheet with `NAVIGATION / All destinations` header + stacked full-width `SETTINGS`, `DIAGNOSTICS`, `AUTOMATION`, `AI ASSISTANT`, `SETUP` buttons (all uppercase, mono-tracked) + accent-filled `SYNC NOW` at bottom.
- Captured variants:
  - `04-more-menu.png` — light theme.
  - `04-more-menu-dark.png` — dark theme.
- Note: `Integrations` is relabeled `Settings` only in this menu (`HuaweiSyncRoot.kt` line 356). The primary label is `Integrations` everywhere else, so the same destination is called two things.

## 05. Integrations (a.k.a. Settings)

- Route: `integrations`. Reached via More → Settings, or wide-nav "Integrations".
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/integrations/IntegrationsScreen.kt`.
- Composition: title "Integrations" + eyebrow `EVIDENCE-BASED CONNECTIONS` + subtitle "08 PROVIDERS" → red-outlined honesty callout (`STATUS FOLLOWS EVIDENCE / Health Connect confirmation requires official readback. Consumer apps remain ready-to-read until separately validated.`) → `CURRENT ROUTE` diagram (`HW → HC → APPS`, HC solid red, others outlined) → `Android health exchange / READBACK GATED` section → provider cards (Health Connect green-outlined `CONFIRMED IN HEALTH CONNECT`, GymRats blue-outlined `READY FOR GYMRATS TO READ`, ...).
- Captured variants:
  - `05-integrations.png` — light theme.
  - `05-integrations-dark.png` — dark theme.
- Note: subtitle claims "08 PROVIDERS" but only 2 are meaningfully instantiated at runtime. The number needs a source of truth.

## 06. Diagnostics

- Route: `diagnostics`. This is the proven Gate 1 runtime surface (see M001 evidence).
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/diagnostics/DiagnosticsScreen.kt` rendered inside `HuaweiSyncRoot.kt` line 427-434 (`Diagnostics center` wrapper + delegated `gate1Entry`).
- Composition: outer title "Diagnostics center" + eyebrow `PROVEN GATE 1 SURFACE` → inner Gate 1 diagnostics: title "Diagnostics" + eyebrow `GATE 1 / RUNTIME EVIDENCE` + explainer → `HEALTH CONNECT ENVIRONMENT` metric card (Availability / Exercise session permission) → outlined `CURRENT RESULT` card (`VERIFIED` chip + phrase + `Next action: NONE`) → `DETERMINISTIC METADATA / Record identity` card with full monospaced 64-char SHA-256 client-record ID wrapped over two lines.
- Captured variants:
  - `06-diagnostics.png`
  - `06b-diagnostics-scrolled.png` — attempts + readback timestamps.
  - `06-diagnostics-dark.png`

## 07. Automation

- Route: `automation`.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/automation/AutomationScreen.kt`.
- Composition: title "Automation" + eyebrow `PREVIEW · LOCAL SESSION` + subtitle `NO BACKGROUND JOB` → red-outlined `PREVIEW ONLY` honesty callout (`Explore the intended automation experience without enabling automation. Nothing here is saved. No background job is created. Leaving this screen resets the preview.`) → `MASTER CONTROL / Automatic sync / Unavailable in this preview` row with `COMING SOON` chip and disabled switch → `TRIGGER LIBRARY / Choose how sync could start / 04 CONCEPTS` list: `Scheduled sync`, `After a Huawei Health update`, `While charging`, `On Wi-Fi at home` (visible on scroll) — each with a red icon, description, and `COMING SOON` chip.
- Captured variants:
  - `07-automation.png`, `07b-automation-scrolled.png`, `07-automation-dark.png`.

## 08. AI Assistant

- Route: `ai-assistant`.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/assistant/AssistantScreen.kt`.
- Composition: title "AI Assistant" + eyebrow `EXPERIMENTAL · DETERMINISTIC LOCAL` + subtitle `NO NETWORK` → red-outlined honesty callout (`EXPERIMENTAL · LOCAL ONLY / No model is connected. Prompts run through canned, deterministic rules and never leave this app.`) → `ASK · LOCAL CONCEPT / Sync assistant` block explaining the local-rules boundary → `TRY A SAFE PROMPT` list of three canned questions each with a `RUN →` action → disabled free-form input placeholder → footer disclaimer.
- Captured variants:
  - `08-assistant.png`, `08b-assistant-scrolled.png`, `08-assistant-dark.png`.

## 09. Onboarding (Setup)

- Route: `onboarding`. Labeled `Setup` in the More menu and wide-nav CTA.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/onboarding/OnboardingScreen.kt`.
- Composition: top bar (H mark + "Huawei Sync" + step `STEP 01 / 04` + `DARK` toggle) → concentric-ring radial diagram (`H → HC → APPS`) — the app's single most identity-carrying visual moment → eyebrow `FOR HUAWEI WATCH OWNERS` + hero heading `Your workouts, available through Health Connect.` → body copy → `HEALTH CONNECT / READY / Confirmed in Health Connect` status card → red primary `→ START SETUP` → outlined `CONTINUE EXISTING SETUP` → footer honesty note (`Huawei Sync writes only the records you explicitly sync. Health Connect permissions remain under Android control.`).
- Captured variants:
  - `09-onboarding.png`, `09b-onboarding-scrolled.png`, `09-onboarding-dark.png`.
- Note: The Onboarding surface hosts its own theme toggle, duplicating the Dashboard one; step `01 / 04` is a claim (4 steps) but flows have no next-step UI beyond the two CTAs.

## 10. Activity detail

- Route: `activity-detail`. Reached by tapping a History card.
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/detail/ActivityDetailScreen.kt`.
- Composition: top bar (`←` back + eyebrow `DETERMINISTIC LEDGER DETAIL`) → eyebrow `SYNTHETIC` + title `Activity record` → `READBACK STATE / Verified / VERIFIED` chip + explanation card → `DETERMINISTIC IDENTITY` card (`Client record ID` full 64-char SHA-256, `Record version 1`) → `WRITE ATTEMPTS` card (`Attempt count 1`, `Acceptance Recorded · 2026-07-16 16:14:08 UTC`) → `READBACK VERIFICATION` card (`Verification Verified · timestamp`, `Last ledger update timestamp`) → uppercase-mono footer disclaimer (`THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP.`).
- Captured variants:
  - `11-activity-detail.png`, `11b-activity-detail-scrolled.png`.
- Note: This is the cleanest, most trustworthy screen in the app. All facts are labeled, timestamped, and honest.

## 11. Sync Now modal (`SyncNow`)

- Route: `sync-now`. Modal-only destination (see `HuaweiSyncDestination.kt` line 58).
- Source: `app/src/main/java/dev/lui/huaweisync/ui/screens/sync/SyncNowModal.kt`.
- Wiring: shown by `HuaweiSyncNavigationState.showSyncOverlay()`; reachable from wide-nav side-rail "Sync now" button, More menu bottom "SYNC NOW" red button, and — via `LaunchedEffect` in `HuaweiSyncRoot.kt` line 218-222 — reacts to `overlayDestination` being set.
- Runtime observation: **the SyncFab on Dashboard does not open this modal**. `DashboardScreen.kt` line 77-84 wires `onClick = onSync`, which in `HuaweiSyncRoot.kt` runs the coordinator directly. Only side-rail and More menu paths call `showSyncOverlay()`. Attempts to capture the modal via the wide-nav button and the More menu both returned to the Dashboard within the screenshot latency window — the overlay either dismisses immediately in this build or the wire-up is not asserting overlay presence. **Filed as a P0 finding in the audit.**
- Not captured: no confirmed screenshot exists. `10-sync-modal-landscape.png` shows the wide-nav shell after tapping SYNC NOW without an overlay visible. The scrolled variant `10b-sync-modal-scrolled.png` is the Dashboard scrolled (the tap chain resolved to Dashboard).
- Composition per code (`SyncNowModal.kt` — read but not verified visually): full-screen surface with dim scrim, `SectionHeader`, phase list, dismiss control.

## Bonus surfaces captured

- `12-landscape-wide-nav.png` — landscape (2400×1080). `WideNavigationBreakpoint = 760.dp`; landscape @ 420dpi is ~914dp so the shell switches to the wide layout: 224dp left rail with `HUAWEI SYNC` monospaced kicker, `Workout bridge` display heading, stacked destination tiles (`Dashboard` selected pink), and two right-aligned CTAs (`Setup`, red `Sync now`). Right column shows Dashboard content.
- `14-hc-permission-request.png` — Android Health Connect system permission dialog surfaced when a permission-less user taps the FAB. Design-owned by the system; we cannot style it, but its entry needs to be predictable and warned.

## Cross-cutting notes captured live

- The `DARK` / `LIGHT` toggle is present on Dashboard *and* Onboarding, styled as an outlined `StraightEdgeButton` — visually the same weight as primary CTAs.
- `SectionHeader` eyebrows are always UPPERCASE MONOSPACE with tracked letter-spacing. Every screen except History and Activity detail (which promote the eyebrow above the H1) leads with an eyebrow.
- Bottom-nav is 4 items (`Dashboard · Pipeline · History · More`); the wide-nav rail shows 7 items (`Dashboard · Pipeline · Integrations · Diagnostics · Automation · History · AI Assistant`) — so compact and wide expose different information architecture.
- The red primary-accent runs at OKLCH-equivalent `#EC3013` (rules/borders) but text-safe `#A61B12` (light) / `#FF6B55` (dark) for foreground. This is why the `DASHBOARD` tab label under the selected FAB icon reads as a darker red than the FAB itself.
- Archivo & JetBrains Mono are declared but not bundled (`Type.kt` lines 11-16); the runtime uses `FontFamily.SansSerif` and `FontFamily.Monospace`. The prototype's distinctive Archivo voice is missing on emulator/device.

## Divergences: prototype vs Compose

The `docs/design/interactive-prototype/HuaweiSync.dc.html` prototype (v0.1, 10 screens) diverges from the shipped Compose surface in ways that matter:

| Aspect | Prototype (HTML) | Compose (runtime) |
|---|---|---|
| Default theme | Dark-first | Light-first (`isSystemInDarkTheme()`, but emulator is light by default) |
| Onboarding H1 | "Your watch, in every app you already use." (product-marketing voice) | "Your workouts, available through Health Connect." (accurate, flat) |
| Dashboard H1 | "Good morning, Léo." + date | "Dashboard" (title 15sp) + `LIVE PRODUCT STATE` (eyebrow) |
| Hero state | Green pulse `EVERYTHING SYNCED` + confetti + 32px H1 | Green chip `COMPLETE` + `Confirmed in Health Connect` + 6dp red progress rule |
| Integrations | 8 destinations rendered (Strava, Fit, Samsung, Garmin, Fitbit, Oura + HC + GR) | 2 destinations rendered (HC + GR), subtitle still claims `08 PROVIDERS` |
| Motion | orbit rings, pulse dots, confetti, ring pulses, flow-down animations | Only FAB `CircularProgressIndicator` + `animateFloatAsState` progress rule |
| Font | Archivo (Google Fonts CDN) + JetBrains Mono (CDN) | System sans-serif + system monospace (font files not bundled) |
| Header CTA | Full-width `Set up in 60 seconds` promise | `→ START SETUP` (accurate) + `CONTINUE EXISTING SETUP` |
| Accent | Single `#ec3013` used throughout | `#EC3013` for rules; `#A61B12`/`#FF6B55` for text/icons (contrast-safe) |
| Iconography | Bespoke inline SVGs (retina-clean, 2.4 stroke) | `Icons.Rounded.*` from Material — Home, Timeline, Settings, Info, Build, History, MoreHoriz — visually generic |
| Loading | shimmer bars | Blank surface with `Loading sync status` copy + 0% progress rule |
| Empty | prototype does not show empty state | `13b-history-empty.png` shows `No ledger activity` card floating mid-screen |

The prototype was drawn as a brand exercise; the Compose surface was implemented as a truth-first engineering exercise. Neither is wrong. M005's job is to reconcile the two: keep the runtime's honesty and add back the prototype's confidence.
