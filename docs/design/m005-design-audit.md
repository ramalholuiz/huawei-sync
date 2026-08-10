# M005 · Design Audit

**Product:** Huawei Sync — a personal sideloaded utility whose one job is to let a Huawei Watch owner move workouts from Huawei Health into Android Health Connect so apps like GymRats can read them. The user-value equation is small: **minimum effort in → confident answer out**.

**Method:** Critique the shipped Compose surface at commit `4538c47` against (a) the interactive prototype `docs/design/interactive-prototype/HuaweiSync.dc.html`, (b) real emulator screenshots (see `m005-screen-inventory.md`), (c) the product mission and gate structure in `AGENTS.md`, and (d) the Impeccable design vocabulary (critique, layout, typeset, distill, clarify, onboard, adapt, animate, polish). No app code changed.

---

## 1. Strengths — what to protect at all costs

The current app has real design integrity in five places. Any redesign must not throw these away.

1. **Runtime honesty.** Every screen tells the truth about what has and has not happened. `Diagnostics` shows the actual SHA-256 client-record ID (`huawei-sync:v1:6604c3f9…`). `Automation` labels itself `PREVIEW ONLY` and clearly says `Nothing here is saved`. `AI Assistant` says `No model is connected` and enforces canned rules. `Activity detail` closes with `THIS VIEW REPORTS LEDGER AND READBACK FACTS ONLY. IT DOES NOT CLAIM DELIVERY TO ANOTHER APP.` This is unusual for an app of this class — most sync utilities lie about integrations. Protect this.
2. **Deterministic identity is visible.** The full client-record ID is not hidden behind a dev toggle; it is shown as first-class product content in `Diagnostics`, `History`, and `Activity detail`. That is the difference between a personal utility and a black box.
3. **Modernist voice.** Zero-radius rectangles, 1dp/2dp rules, tracked-uppercase mono eyebrows, ExtraBold display sans, semantic status chips with letter-outline. This is a coherent editorial voice that reads as "engineering-first, non-consumer". It looks nothing like a generic Material Health app. Keep the voice; sharpen its execution.
4. **The onboarding orbit diagram (`H → HC → APPS`)** is the app's single most identity-carrying visual moment. It works because it is diagrammatic, not decorative.
5. **The coordinator phase evidence timeline (`Preflight → Write → Acceptance → Readback → Reconciliation`) on Pipeline** is a genuinely novel product idea. Most apps show a spinner; this app shows the state machine. That is more honest, and — after M005 — potentially more calming.

Rule of thumb for the redesign: if a change forces "Confirmed in Health Connect" to become "Synced!", it is wrong.

---

## 2. The primary problem — the app forgot who it is for

Huawei Sync is a **daily-driver personal utility for the owner**, not an admin console for an SRE. The runtime evidence shipped for Gate 1 has bled into the everyday product surface. The user who wants to run a sync and check that GymRats can read it is confronted with:

- three separate places that use the word *"deterministic"* on the first two screens;
- an eyebrow of `LIVE PRODUCT STATE` on the Dashboard;
- a subtitle of `EVIDENCE-BASED CONNECTIONS / 08 PROVIDERS` on Integrations;
- a monospace 64-character SHA-256 on the first History card;
- and no simple, human answer to "did it work?".

The user asked "did my workout make it?" The app answered "the deterministic record's semantic content hash matched the accepted client identity at version 1 with 1 readback match." Both are true. Only one is a product.

**The audit's central recommendation is not to hide evidence — it is to move it out of the user's primary path and into a "prove it to me" drawer.** The runtime honesty is the moat; the product doesn't need to sell it every second.

---

## 3. Findings by dimension

Where a finding maps to a specific screenshot in `docs/design/m005-screenshots/`, the filename is cited.

### 3.1 Information hierarchy — critique

**F-H1 · The Dashboard title is smaller than the sync-hero body copy.** The top-bar reads `Dashboard` (15sp `titleMedium` ExtraBold) but the hero H1 `Confirmed in Health Connect` renders at ~30sp `headlineMedium`. Result: the top bar reads as a browser tab, not a product surface. The `H` mark reads as more important than the app's page title. See `01-dashboard-default.png`.

**F-H2 · Every dashboard section leads with a monospace eyebrow.** `DURABLE LEDGER` above "Recent activity"; `CURRENT FACTS` above "Connected services"; `HEALTH CONNECT READBACK` above "Verification". Eyebrows on every section is the equivalent of the AI-slop reflex from web brand pages: a rhythm that reads as scaffold rather than voice. One deliberate eyebrow is voice; four eyebrows in a row is grammar. See `01-dashboard-default.png`, `01b-dashboard-scrolled.png`.

**F-H3 · The status chip and the phase eyebrow duplicate.** The hero shows `COMPLETE` (green outlined chip, top-left) and `VERIFICATION` (mono microcopy, top-right). Then the progress rule label repeats `80% · SYNC PHASE: VERIFICATION`. Three labels for one fact.

**F-H4 · Dashboard's `LIVE PRODUCT STATE` eyebrow is meaningless to a user.** It's a phrase we invented for ourselves.

### 3.2 CTAs and primary actions — critique

**F-C1 · The single most important control (Sync now FAB) may not do what the screen implies.** On the seeded permission-granted state we observed the FAB tap fall through to a Dashboard re-render without opening any modal or advancing the phase. In `HuaweiSyncRoot.kt` line 218 the sync overlay is triggered by a `LaunchedEffect` on `navigationState.overlayDestination != null`, and `showSyncOverlay()` is only called from side-rail and More menu buttons; the FAB directly calls `onSync`. This is a **P0 bug** — the FAB claims to sync but the modal that visualizes progress never appears in the compact layout. Even if `onSync` is doing the right thing behind the scenes, the user's mental model breaks: pressing "Sync now" should show progress or a result.

**F-C2 · Primary CTAs and outlined CTAs read at the same visual weight.** `→ START SETUP` (red filled) and `CONTINUE EXISTING SETUP` (outlined) are both full-width, both ~48dp tall, both uppercase-tracked. The outlined variant is nearly invisible against the light bg because `HuaweiSyncTheme.colors.lineStrong` is `0x38201E1D` (22% alpha) — see `09-onboarding.png`.

**F-C3 · The Dashboard theme toggle occupies the same weight as a page action.** `DARK` sits in the top-right at the same size as any `StraightEdgeButton`, but it is decoration. It should be a subtle icon or move into Settings.

**F-C4 · No primary CTA on Dashboard.** After the user hits the app, the most-visible red thing is the theme toggle, then the FAB in the corner. In the permission-required state, the red `REVIEW PERMISSION` inside the hero card is correct — but in the ready-to-sync state, the visual weight collapses. If a user does nothing, nothing happens. That's fine for an admin console; not for a "did it work?" utility.

### 3.3 Navigation and information architecture — critique

**F-N1 · Same destination, two labels.** `Integrations` is the primary label everywhere except the More menu, where it is `SETTINGS`. `HuaweiSyncRoot.kt` line 356. Users will not know these are the same thing. See `04-more-menu.png`, `05-integrations.png`.

**F-N2 · Two different information architectures ship in the same binary.** Compact shows `Dashboard · Pipeline · History · More` (4 items). Wide shows `Dashboard · Pipeline · Integrations · Diagnostics · Automation · History · AI Assistant` (7 items) plus a footer `Setup` + `Sync now`. Which set of destinations is primary is not defined in the design. On a personal phone the answer is compact-4; on a tablet the answer is wide-7. There is no rule about what belongs on the "front page" vs "utility drawer".

**F-N3 · `Diagnostics`, `Automation`, `AI Assistant`, and `Setup` are all buried in More.** For an app whose thesis is honest evidence, `Diagnostics` should not require three taps. `Setup` in the More menu is odd — that's a first-run surface, not a permanent destination.

**F-N4 · The compact More menu is a modal takeover, not a menu.** It is a bottom sheet that covers 90% of screen. See `04-more-menu.png`. All items are full-width red-outline uppercase buttons of equal weight, ending in a bright red `SYNC NOW`. This is a strong opinion but not the right one — it demands the user process 6 equal choices to reach one destination.

**F-N5 · No back-affordance on Dashboard's `H` mark, but there is on Activity detail.** Users learn the `H` is decorative on the Dashboard, then unlearn it on child screens.

### 3.4 Typography — typeset

**F-T1 · Archivo is not bundled.** `app/src/main/java/dev/lui/huaweisync/ui/theme/Type.kt` lines 11-16 explicitly fall back to `FontFamily.SansSerif` (Roboto on the emulator). The prototype's most visible identity carrier is missing at runtime. All screens are shipped in a font the design never intended. **P0.**

**F-T2 · JetBrains Mono is not bundled either.** Same failure. All "technical microcopy" is rendered in Android's monospace fallback (Cousine/Droid Sans Mono depending on the API level). The technical voice is *approximately* right, but not the intended voice.

**F-T3 · Body copy is 18sp `bodyLarge` on Dashboard hero.** That's the correct size for the H1 body, but the app uses it for hero subtext and long paragraphs. On onboarding (`09-onboarding.png`), the paragraph "Move workout sessions from Huawei Health into Android Health Connect..." runs 3 lines at 18sp — dense for a first-run explainer.

**F-T4 · The `titleMedium` for section titles ("Health Connect", "GymRats" in Connected services) is 15sp ExtraBold — smaller than the surrounding `bodyLarge` at 18sp.** Titles smaller than body copy invert hierarchy.

**F-T5 · Uppercase tracked labels are everywhere.** Eyebrows, chip labels, button labels, tab labels, footer disclaimers, even service statuses (`READY FOR GYMRATS TO READ`). When everything shouts, nothing shouts. See any screen.

**F-T6 · The client-record ID overflows the container.** `huawei-sync:v1:6604c3f9d89372ea30aa64aa553a8f…` truncates mid-hash in History but wraps to 2 lines in Diagnostics — the same string, two treatments. See `03-history.png` vs `06-diagnostics.png`. Neither is calm.

### 3.5 Color and contrast — critique

**F-CL1 · The palette is deliberately narrow and this is a strength**, but the accent has to do too much work — everything red means "primary", from the Sync FAB to the eyebrow color of a `PREVIEW ONLY` warning to the disabled `COMING SOON` scheduler icon. Users cannot tell a call-to-action from a warning from a decoration.

**F-CL2 · The `accentForeground` split is a good decision but it's leaking into the visual grammar.** Text-safe accent (`#A61B12` light, `#FF6B55` dark) is a different color than the rule accent (`#EC3013`), and both appear on the same screen. See the History tab label (dark red `HISTORY`) vs the FAB (bright red) in `03-history.png`. The eye reads them as "two similar reds" and asks "why?". Solutions in design directions.

**F-CL3 · Success is green outline chip (`OK 0xFF4ADE80`) but not enforced.** The `Recent activity` card in the empty state has zero color at all; `Readback confirmed` uses green text; `VERIFIED` uses green chip. Same fact, three treatments.

**F-CL4 · The neutrals are correct but the light theme's `background` is `#F3F2F2` and the `surface2` used inside the hero is `#EAE9E9` — two near-identical warm-grays.** The hero card floats but does not read as a card, because its border (line at 10% alpha ink) also nearly disappears against the tinted surround. See `01-dashboard-default.png`.

**F-CL5 · Dark theme's FAB in "Complete" state is a red-orange filled square with only a subtle broken circle inside** (see `01-dashboard-dark.png` bottom-right). It reads as "loading" more than "ready to re-sync". The prototype used a green pulse for verified; the runtime uses a monochrome red rectangle.

### 3.6 Spacing and density — layout

**F-S1 · The FAB clips the last content card.** Bottom padding on Dashboard content is 112dp (see `DashboardScreen.kt` line 157) but the FAB is 72dp with 24dp margin (`ModernistComponentMetrics.syncFabSize = 72.dp` + `HuaweiSyncSpacing.xl = 24.dp` = 96dp). In practice the FAB visibly overlaps the top of the "Verification" section — see the cropped `Verification` heading and clipped Verification card in `01-dashboard-default.png`. The bottom padding is not enough for a right-aligned FAB, and there is no content-inset for the last card.

**F-S2 · Cards are aligned edge-to-edge with `xl = 24.dp` padding** but their inner rhythm is `lg = 16.dp` — this makes cards feel cramped inside and spaced outside. The prototype used more air between elements inside a card and less padding outside; the ratio is inverted.

**F-S3 · The empty-history card floats mid-screen at ~60% height.** `13b-history-empty.png`. Neither centered (top-anchored) nor bottom-tabbed. This happens because the LazyColumn's only child is the card and it is not vertically distributed.

**F-S4 · The wide layout uses a 224dp fixed rail on a landscape phone (914dp wide).** That is 24% of screen for a left rail. See `12-landscape-wide-nav.png`. The rail is empty below the fold. Rail width should scale to the shortest destination label, not to a fixed 224dp.

**F-S5 · Onboarding CTAs are pinned to the bottom edge with no safe-area buffer** — visually the buttons run against the gesture bar. See `09-onboarding.png`.

### 3.7 Iconography and imagery — critique

**F-I1 · Bottom-nav icons are stock Material rounded** (`Home`, `Timeline`, `History`, `MoreHoriz`). They neither reinforce nor contradict the Modernist grammar; they read as "any Material app". The prototype used bespoke inline SVGs with a 2.4 stroke that matched the 2dp rule weight.

**F-I2 · The `H` monogram is a red square with a white bold H.** Distinctive, but only appears on Dashboard top-bar and Onboarding step header, then never again. The user learns a brand mark then loses it — inconsistent.

**F-I3 · Provider monograms (`HC`, `GR`, `HW`, `APP`) are 44dp rectangles with a border and a mono-tracked label inside.** These are fine, but the `HC` in the Integrations "current route" diagram is filled red while the `HW` and `APP` are outlined — the diagram implies HC is the active/live node while others are dormant. Users may read this as "HW is disabled". See `05-integrations.png`.

### 3.8 Motion — animate

**F-M1 · The app is nearly motionless.** Only `SyncFab` `CircularProgressIndicator` (when Syncing) and `animateFloatAsState` on the progress rule move. The prototype specified orbit rings, confetti on complete, hs-pulse dots, hs-flow-down flow lines, hs-glow pulses; **none survived** to Compose. See `Type.kt`, `ModernistComponents.kt`, prototype `keyframes`.
**F-M2 · There is no acknowledgement moment.** When sync completes, no transition happens. The user is expected to notice a status label change from `SYNCING` to `COMPLETE`. That is not enough feedback for the app's core loop.
**F-M3 · The syncing spinner in the Verification section is red on red — barely visible.** See the "Verification" card FAB overlap in `01-dashboard-default.png` and `01-dashboard-dark.png`.

### 3.9 Accessibility — audit

**F-A1 · Uppercase tracked mono at 10sp for eyebrows.** `HuaweiSyncTechnicalTypography.label` is 10sp SemiBold with 1.4sp tracking. Under low-vision conditions this is illegible. The label style is used for the FAB "SYNCING" state description and for chip labels; enlarging it via font-scale would break layout because `SectionHeader` only stacks-trailing at `fontScale >= 1.3f`.
**F-A2 · Contrast risks:**
 - `ink3 = 0x61F3F2F2` (dark) — that's 38% alpha white — used for `bodySmall` disclaimers. Fails WCAG AA 4.5:1 on dark bg for anything under 18sp.
 - `ink4 = 0x2EF3F2F2` (dark, 18% alpha) — used for `line` accents. Fails.
 - The `READY FOR GYMRATS TO READ` monospace status on light theme (`Connected services` card) is `ink2 = 0xFF5B5755` on `surface1 = 0xFFFFFFFF` — passes (`~7.3:1`). Good.
 - The `LIVE PRODUCT STATE` microcopy on the Dashboard top bar is `ink2` on the background — passes.
 - However the tab-bar `DASHBOARD` selected color is `accentForeground = 0xFFA61B12` on `background = 0xFFF3F2F2`. Contrast ≈ 6.3:1. Passes AA for 10sp small text, but only just.
**F-A3 · The FAB in `Complete` state uses `Icons.Rounded.Check` — content description is set** (`stateDescription = state.spokenState`), but the semantics duplicate `contentDescription = "Sync now"` and `stateDescription = "Sync complete"`. Talkback reads `Sync now, Sync complete, Button`. Ambiguous.
**F-A4 · No haptic on primary actions**. Sync-start, sync-complete, permission-granted — none produce haptic feedback. Not a violation, but a missed opportunity for a utility whose feedback loop is often eye-off-phone.

### 3.10 Empty, loading, error, permission states — critique

**F-E1 · Loading is a single card with `SYNCING` chip + `0%` progress rule + copy `Reading Health Connect availability, permissions, and the durable ledger.`** Static, not animated, and the copy names 3 subsystems most users don't care about.
**F-E2 · The empty-history state (`13b-history-empty.png`) is orphaned mid-screen.** No illustration, no CTA back to Dashboard, no explanation of *when* something will appear.
**F-E3 · The permission-required state is genuinely strong** — `ATTENTION` yellow chip, `Health Connect permission required` heading, `REVIEW PERMISSION` red primary button. `01c-dashboard-empty-permission.png`. Keep this pattern for other blockers.
**F-E4 · Failed / error states not observed live** but per `DashboardScreen.kt` line 294-303 render as a `SANITIZED FAILURE SUMMARY` red-bordered card at the bottom of the LazyColumn. Buried under everything else; a failure should surface at the top, not scroll-below-the-fold.

### 3.11 Honesty of sync messages — clarify

**F-CO1 · The hero H1 `Confirmed in Health Connect` is exactly right.** But it does not answer the user's actual question, which is "and did GymRats get it?". Health Connect confirmation is a necessary but not sufficient step. The current copy pretends the last mile is done because we cannot verify GymRats without Gate 2. This is a boundary the app respects — but the visual language ("Everything green!") does not match the boundary ("but not verified downstream").
**F-CO2 · `READY FOR GYMRATS TO READ` on the GymRats service row is one of the most important pieces of copy in the app**. It is technically honest — the workout is available in HC for GymRats to consume. But it reads to a user as "GymRats is ready" (i.e., "GymRats has it"). The user is not distinguishing "the record has been posted to a shared bulletin board" from "GymRats has picked it up". Needs a rewrite. Candidate: `Available for GymRats to import` or `Waiting for GymRats to pick up`.
**F-CO3 · Integrations `08 PROVIDERS` is false at runtime.** Two providers show. The number is decoration from the prototype's ambition. Wire to a real count or delete.
**F-CO4 · `AI Assistant` copy `No model is connected. Prompts run through canned, deterministic rules and never leave this app.` is exemplary.** Frame the rest of the app this way.

### 3.12 Prototype vs Compose — the delta

See `m005-screen-inventory.md § Divergences` for the full table. The single largest reversal: the prototype was a **confident brand product** ("A sync utility that behaves like a product"); the runtime is a **truth-first engineering surface**. Both are legitimate. The redesign should keep the runtime's truth and rebuild the prototype's confidence around it.

---

## 4. The five most important problems (ranked)

1. **The Sync FAB may not open the sync visualization in the compact layout.** Users tap the most-visible red control and nothing seems to happen. If the underlying sync did run, the user has no feedback loop. `F-C1`. Blocks the app's core promise: "did it work?".
2. **Archivo and JetBrains Mono are not bundled.** The app ships in fallback fonts. Every screen reads slightly wrong. `F-T1`, `F-T2`. Ship-blocker for M005's design intent.
3. **Runtime evidence has bled into every product surface.** The Dashboard reads as an ops dashboard, not a personal utility. The user's primary loop is buried under `LIVE PRODUCT STATE`, `DURABLE LEDGER`, `CURRENT FACTS`, `HEALTH CONNECT READBACK`, and a raw SHA-256. `F-H1`, `F-H2`, `F-H4`, `F-T5`, `F-T6`, `§2`. The truth stays; the presentation moves.
4. **The FAB clips the last card, and the sync progress + Verification section fights the FAB visually.** `F-S1`. Every seeded-state screenshot shows a red square covering "Verification". Any user seeing that will pause.
5. **The single central design question — "did it work?" — is answered in monospaced technical language.** `Confirmed in Health Connect` is correct but does not close the loop with GymRats. `Ready for GymRats to read` is honest but reads as delivered. `F-CO1`, `F-CO2`. This is the copy problem the redesign has to solve.

Runners-up worth naming: `F-N1` (Settings/Integrations dual label), `F-N2` (compact vs wide IA divergence), `F-M2` (no acknowledgement moment on completion), `F-A2` (contrast risk on dark ink3/ink4).

---

## 5. What is out-of-scope for this audit

Per the M005 mandate: no Kotlin, Compose, Gradle, or resource changes. No Huawei Health, Samsung Health, GymRats API, WorkManager, or new record-type work. Preview screens (Automation, AI Assistant) are not to be turned into shipped features. The ledger, coordinator, and deterministic identity are not to be re-modeled. Everything in this audit is a proposal, not an implementation.

Physical-device Gate 2 belongs to M004 and is BLOCKED on a real Android phone. It is not an M005 concern.

---

Continue in `m005-design-directions.md` for three fully-fleshed directions (Refinement / Premium Utility / Bold Health Dashboard) and `m005-prioritized-backlog.md` for the P0/P1/P2/Rejected backlog.
