# M005 · Design Directions

Three fully-fleshed directions for Huawei Sync's next design pass. Each one starts from the same fixed constraints and answers the same core question — "how fast can the owner sync a workout and know it made it into GymRats?" — with a different set of trade-offs.

## Shared constraints (do not violate)

- Runtime honesty is inviolable. `Confirmed in Health Connect` never becomes `Synced`. `Ready for GymRats to read` may be rewritten, but its truth (workout is available for consumers to pick up, not that GymRats has done so) is preserved.
- Deterministic identity remains first-class where it is currently first-class: `Diagnostics`, `Activity detail`. It may become a drawer on Dashboard/History rather than a headline.
- Preview surfaces (`Automation`, `AI Assistant`) stay labeled preview.
- No new integrations, no new record types, no new backends.
- All directions target Android 13+ (Health Connect gate). Compose stack unchanged.
- Zero-radius rectangles stay the geometric grammar for every direction (the Modernist stance is the app's identity). Directions differ on typography, chromatic strategy, motion, and hierarchy — not on shape.

Each direction below is a coherent design language, not a menu of options. Pick one, refine one.

---

## Direction A — **Refinement**

> "The current design is right; the execution is 60% there. Ship the 100%."

### Concept

The Modernist voice, the runtime honesty, and the flat geometry are already an unusual, valuable stance. Direction A does not redesign — it *finishes* the current design. Every visual issue in `m005-design-audit.md § 3` is addressed as craft work: typography that actually loads, hierarchy that inverts back to correct, contrast that meets AA, one acknowledgement moment on completion, one CTA hierarchy. The user opens the app, sees a clear "Ready to sync", taps once, watches a calm progress line, and sees a clear "Done, and available to GymRats". Nothing more.

### Typography

- **Bundle Archivo** (variable font, weights 400/600/800) and **JetBrains Mono** (400/600) as local assets under `app/src/main/res/font/`. Google Fonts SIL OFL licensed. No CDN.
- Rebalance the scale:
  - H1 (page titles): Archivo 800, 28sp, tracking -0.02em (currently `titleMedium` 15sp — biggest single fix).
  - H2 (section titles): Archivo 700, 20sp, tracking -0.015em (currently `headlineSmall` 22sp — closer, tighten).
  - Body: Archivo 400, 16sp, line-height 24sp (currently `bodyLarge` 18sp/28sp — reduce, breathes more).
  - Eyebrow: JetBrains Mono 600, **9sp** with 1.2sp tracking (currently 10sp / 1.4sp — smaller, tighter, less shouty).
  - Value / metric: JetBrains Mono 400, 13sp.
- Eyebrow rule: **one eyebrow per screen, at the top only.** Section titles no longer get eyebrows. This alone removes ~60% of the "AI grammar" reflex.
- Uppercase-tracked labels are reserved for: (1) primary status chips (`READY` / `COMPLETE` / `ATTENTION` / `ERROR`), (2) tab labels, (3) buttons, (4) the single page-level eyebrow. Everywhere else is sentence case.

### Color

Keep the two palettes. Tighten:

- **Neutrals stay** (`background = #F3F2F2` light, `#0B0A09` dark). The prototype's warm-gray family is unusual and correct — do not swap for a pure white.
- **Accent duality goes**. One accent role, one color: **`#DE2A0E`** (a hair darker than `#EC3013`) works as both text and rules against both bg and dark bg (contrast 5.6:1 on `#F3F2F2`, 5.1:1 on `#0B0A09`). Retire `accentForeground`. The `DASHBOARD` selected tab and the FAB are now the same red.
- Add one **success role** used consistently: `#1D8A3D` on light (chip fill + text), `#4ADE80` on dark. Not two greens for the same fact.
- `ink3` on dark moves from 38% → 52% alpha to hit 4.5:1 for `bodySmall` disclaimers. `ink4` becomes decoration-only.
- **Delete `canvasBackground` from surfaces**. It only appears in the wide-nav left rail edge and creates a visual seam.

### Components

- The `H` mark moves out of the top bar. Onboarding only. Everywhere else the page title carries the app.
- Bottom-nav: change 4 → 3 destinations (Dashboard, History, More) — Pipeline becomes a Dashboard drawer. The FAB is the fourth "slot" visually. Rationale: for a personal utility, the daily job is sync + look at yesterday. The pipeline is a "prove it" surface, not a daily surface.
- More menu: replace the modal takeover with a **48dp-tall bottom sheet peek** that expands on drag. Destinations are single-line rows with icon + label + short subtitle (`Automation · Preview`, `Diagnostics · Gate 1 evidence`, `AI Assistant · Local only`), not full-width red buttons of equal weight.
- The `Sync now` FAB becomes an **inline primary CTA** on Dashboard when idle (a full-width red button that says "Sync now" above the pipeline drawer), and a floating circular status when syncing (progress ring around a circular FAB, replacing the current filled red square).
- The `SANITIZED FAILURE SUMMARY` card floats to the top of Dashboard when non-null, replacing the ready-to-sync hero.
- Wide-nav rail shrinks from 224dp to 200dp; the "Setup" button drops (Setup is not a permanent destination); primary CTA becomes the same red inline button that the compact layout uses.

### Density

- Card outer padding drops from `xl = 24dp` → `lg = 16dp`. Inner spacing rises from `lg = 16dp` → `xl = 24dp`. Reverses the current inverted ratio.
- Section spacing on Dashboard: `xl = 24dp` → `xxl = 32dp`. Breathes.
- The FAB's safe-zone: content bottom-padding rises from 112dp → **144dp**. FAB no longer clips Verification.
- Empty states get a `Column(verticalArrangement = Arrangement.Center)` wrapper so cards center vertically, not float mid-screen.

### Charts / diagrams

- The Integrations `CURRENT ROUTE` diagram (`HW → HC → APPS`) stays but drops color-implying-state. All nodes are outlined; a small live-dot next to the active node ("dot pulses on the HC node when a sync is in flight") indicates progress.
- Onboarding orbit diagram (`H → HC → APPS`) stays as-is. It is the strongest visual moment in the app.
- No new charts, no sparklines, no metric cards. This is a utility, not a dashboard.

### Motion

- **One acknowledgement moment**: on sync complete, the hero card border-flashes green once (300ms ease-out), and the phase eyebrow slides from `VERIFICATION` to `COMPLETE` with a 200ms crossfade. No confetti.
- **One progress moment**: the phase progress rule animates from previous phase to current phase over 400ms `ease-out-quart`; the label crossfades.
- **One incoming moment**: on Dashboard mount, the hero card slides up 6dp with `hs-rise` equivalent (200ms) and the ledger/services/verification blocks stagger in 40ms apart. Only on mount, not on every recomposition.
- `@media (prefers-reduced-motion: reduce)` equivalent: all four animations become instant.

### Advantages

- Fastest to implement. No new components; no new palette; no new fonts beyond bundling. Estimated M005-execution effort: ~4-6 dev days.
- Preserves the runtime honesty and Modernist voice that already differentiate the app.
- All P0 problems from the audit close under this direction.

### Risks

- The app still looks like a niche technical utility. It doesn't invite non-technical Huawei watch owners. If the goal is "share with a friend who has a Huawei Watch Fit 5", this direction may not be inviting enough.
- The Sync FAB → inline primary CTA change removes the floating action, which some Android users expect on a "one main action" surface.

### Per-screen change examples (compact layout)

- **Dashboard**: kill top-bar eyebrow. H1 becomes 28sp Archivo `Ready to sync` / `Syncing…` / `Confirmed in Health Connect`. Below H1 a single 16sp body sentence explaining the state. Hero card contains only the state + a red inline `Sync now` button (idle) or progress rule (syncing) or `Available for GymRats to import` chip (complete). Below the hero, a "What just happened" **collapsed pipeline drawer** (tap to expand into current Pipeline content). Below that: `Recent activity` (last 3), `Verification` (short 2-line summary + "See Diagnostics"). Full audit trail moves into Diagnostics.
- **History**: card content unchanged; empty-state centered; the giant SHA-256 collapses into a `⋯` chevron with tap-to-reveal.
- **Diagnostics**: unchanged, protected as the ops surface.
- **Onboarding**: kill the redundant `DARK` toggle here; single `Start setup` primary CTA becomes the only red thing; body copy shortens.
- **Sync modal**: wired to the FAB. Shows the 5 pipeline phases as a live checklist with the coordinator's phase highlighted, and closes on complete after a 400ms hold.

---

## Direction B — **Premium Utility**

> "A calm, high-craft technical instrument. Like a Nomos watch or a Teenage Engineering unit — quiet on the surface, precise underneath."

### Concept

If Direction A is "finish the current design", Direction B is "raise its ceiling". Same runtime honesty, same modernist geometry, but the surface reads as a precision instrument, not an engineering console. Type is smaller, the palette is bone-white and near-black, red is used sparingly and only for state, and every screen has a single ink-level heading that reads at reading distance. Motion is minimal but exact. Data is displayed with the discipline of a technical manual: labels flush-left in mono, values flush-right, hair rules between rows. The Modernist voice sharpens.

### Typography

- **Two families with real contrast**: **Söhne** (or Inter Display if we want free) for prose and headings; **Berkeley Mono** (paid) or **JetBrains Mono** (free) for values, labels, and timestamps. Söhne + Berkeley is the pairing used by Vercel, Linear, Superhuman — it works because both are geometric humanist and share tracking values.
- Free-license alternative: **Inter Display 800** for H1 + **Inter 400/500** for body + **JetBrains Mono 400** for values. Ships without a font license bill.
- Scale is smaller than Direction A:
  - H1: 24sp Inter Display 800, -0.02em.
  - H2: 16sp Inter 600, 0em.
  - Body: 15sp Inter 400, line-height 22sp.
  - Value: 12sp JetBrains Mono 400, tracking 0em.
  - Eyebrow: 9sp Inter 600 sentence-case (not uppercase), tracking 0.06em.
- **The uppercase-tracked mono microcopy is retired everywhere except status chips.** Eyebrows become sentence-case Inter (`Live product state` becomes `Live product state`, then… delete it, we don't need it — the page title carries this).
- Values (numbers, timestamps, IDs) stay monospace so tabular columns align. Everything else is proportional.

### Color

- **Bone-white bg**: `#F5F2ED` (warm off-white, OKLCH 0.95 0.005 80). Not cream — cream is the current AI reflex (see Impeccable slop test). Bone reads as photograph paper, not brand.
- **Near-black ink**: `#1A1917` (OKLCH 0.16 0.002 60). 12.9:1 contrast on bone.
- **One accent, sparingly**: `#B41E12` deep-red — used only for (a) active state chips, (b) primary CTAs, (c) the H mark. Never for progress rules (which use ink), never for chart lines.
- **Muted greens for success**: `#4A6B4E` (light), `#7DA383` (dark). Not `#4ADE80` — that's a notification color, not a state color.
- **Dark theme**: near-black bg `#0F0E0D`, ink `#EBE7DF`. The dark theme is the exception, used when the user's system is dark; not the default (per audit's F-CL1, dark and light must both be first-class).
- Grays: `#B8B3AB` (line strong), `#DED9D0` (line thin), `#E8E4DB` (surface2). Warmer than the current cool-tinted grays.

### Components

- **The hero card disappears.** Dashboard's opening screen is a single H1 (`Ready to sync`) + a single 15sp body sentence + a single primary CTA (`Sync now`). Below it, three anchored blocks: `Recent activity` (list, no card border), `Connected services` (two-row table with mono labels + values), `Verification` (2-line summary + link). Nothing floats.
- **Tables replace cards** for tabular data (Connected services, Ledger metrics). Row: `Workouts tracked` (Inter 15sp ink2) → `1` (Berkeley Mono 12sp ink). Hair rule (0.5dp) between rows. This is the direction's most identity-carrying move.
- **The FAB goes away.** Primary action is an inline red button under the H1 (idle) or a progress line + phase name (syncing). Sync completion becomes a single sentence + a small green dot next to `Available for GymRats to import`.
- **Diagrams become schematic.** Integrations `CURRENT ROUTE` renders as `HW ─→ HC ─→ APPS` in monospace, with a small red dot indicating active node. Onboarding orbit becomes a horizontal 3-node schematic: `Huawei Health   Health Connect   Other apps` with two hair-line connectors. Loses the animated ring but gains legibility.
- Bottom nav: same 3-item shrink as Direction A, but rendered as small mono labels 10sp (no icons) — Teenage Engineering / Braun. Icons are visual noise for a 3-tab shell.

### Density

- **Denser overall**. 12dp base spacing unit. H1 padding: 24dp top / 8dp below. Section spacing: 32dp.
- Line-height on body drops from 28 → 22 (Direction A) → **22 sp fixed**. Body copy runs at 15/22 which is book-density.
- Cards / borders get thinner (0.5dp instead of 1dp) which reads as engraved rather than framed. Requires anti-aliasing check on the emulator (some 0.5dp lines dissolve at 420dpi).

### Charts / diagrams

- Introduce **one** chart: on the History detail (Activity detail), a small horizontal timeline showing "written → accepted → readback → last update" with three ticks and the exact timestamps below. Small, monochrome, informative. Not a hero visualization — a footnote diagram.
- No pie, no bar, no sparkline. This is not a body-data app; it is a sync log.

### Motion

- **Fewer, sharper moves than Direction A.** No stagger, no confetti, no border flashes.
- Sync progress: the phase name crossfades in place (150ms). The horizontal progress line does not animate — it snaps to the new phase, because a snap reads as "definitive". This is a difficult-to-defend choice but the whole direction is about definitiveness.
- Complete: the primary red button turns into a small green tag + `Available for GymRats to import` sentence. 200ms crossfade.
- Reduced motion: everything snaps.

### Advantages

- Reads as a serious, high-craft product. Comfortable to leave on the home screen. Non-technical Huawei owners see something calm; technical ones see the discipline.
- Removes the "engineering console" perception issue in one move.
- The Table pattern for tabular data is the right primitive for a ledger-first product.
- Fonts and palette are (a) not "AI 2026 cream" and (b) not "SaaS cliché navy-and-white". Impeccable slop test passes: from category alone (Android health utility), no one would predict bone-and-near-black with mono values.

### Risks

- Free typography needs a decision: Inter is safe but appears everywhere; Söhne + Berkeley reads better but costs money. If we pick Inter, we need to distinguish via layout and color, not typeface identity.
- Larger visual departure from prototype — the prototype's assertive red-on-black is replaced by restraint. If we later want to hero the app in marketing screenshots, this direction is quieter than the prototype planned for.
- Some Android users will miss the FAB. This is a first-party pattern the direction consciously drops.

### Per-screen change examples

- **Dashboard idle**: page-heading `Ready to sync` (24sp Inter 800, ink) → subtitle `Health Connect is available and write is granted.` (15sp Inter 400, ink2) → red `Sync now` button (full-width, 48dp). Below: table `Connected services` (2 rows). Below: `Recent activity` (linked list of 3 rows). Below: `Verification` (1 sentence + link to Diagnostics). No hero card. No eyebrows.
- **Dashboard syncing**: `Syncing…` (H1) → `Writing to Health Connect · phase 2 of 5` (subtitle) → single hair-line progress bar with phase ticks below (`Preflight ✓ · Write ● · Acceptance · Readback · Reconcile`). No FAB, no modal.
- **Dashboard complete**: `Available for GymRats to import` (H1, ink) → `Written to Health Connect · 12 seconds ago` (subtitle) → outlined `Sync again` button + text link `See how we know` → Diagnostics.
- **History**: full-width table. Columns: `When` (mono date), `Kind` (Synthetic/Real), `Verified` (green dot / red dot), `ID` (mono ellipsis, tap to expand).
- **Onboarding**: single-color type-only hero. `Move workouts from Huawei Health to every app.` (36sp H1) → `Health Connect is the bridge. Huawei Sync is the postman.` (18sp body) → `Start setup` (red button) → `Restore existing` (link). No radial orbit; kill the illustration.

---

## Direction C — **Bold Health Dashboard**

> "This is a fitness product. Behave like one. Charts, motion, color."

### Concept

Directions A and B assume the app's soul is "sync utility". Direction C bets differently: even if the user only ever syncs 3 workouts a week, the moments when they open the app should feel like opening a fitness product they're proud to have on their phone. Bring back the prototype's motion, hero the workout data that HC exposes, and let the identity red carry weight. Runtime honesty stays (per shared constraints), but the surface amplifies. Diagnostics still exists and still tells the truth; it just is no longer the app's front page.

### Typography

- **Archivo bundled** as Direction A. Add **Instrument Serif Italic** as a *display accent* for hero moments only (numbers, occasional headlines). This is a controversial choice — see risks.
- Scale is bolder than Directions A/B:
  - H1 hero (Dashboard "Ready to sync" or "You're synced"): Archivo 900, 42sp, tracking -0.03em. Above the "6rem cap" from Impeccable but only used on Dashboard hero and Onboarding — the two moments where hero size is earned.
  - Numbers in the hero (e.g. workout count, minutes): Instrument Serif Italic 400, 56sp. The serif italic against a bold sans is the Direction's most identity-carrying pairing.
  - Body: Archivo 400, 16sp.
  - Eyebrow: JetBrains Mono 600, 10sp, 1.4sp tracking, red.
- The uppercase-tracked eyebrow returns as identity in this direction (it is the one place the design commits to a bold decoration rather than distills away from it).

### Color

- **Two palettes, both saturated.**
- Light: `#F8F5F0` bg (warm cream — chosen here deliberately because the direction is confident enough that "AI cream" reads as intention rather than fallback), `#141210` ink, red `#EC3013` (unchanged from prototype), success `#0F8B3E`, warning `#D97706`, info `#2563EB`.
- Dark: `#0B0A09` bg (unchanged), `#F3F2F2` ink, red `#FF5540`, success `#4ADE80`, warning `#F5A524`, info `#6EA8FF`.
- **Data-viz palette** (introduced for this direction only): `#EC3013` primary, `#F59E0B` secondary, `#0EA5E9` tertiary, `#7C3AED` quaternary, `#22C55E` success. Not for state — state uses the accent + success + warning above. Data viz is for chart series only.
- The `H` mark becomes a **gradient-free solid**. No aging clichés.

### Components

- **Hero moment is a real block.** Dashboard opens with a full-bleed dark red band (100dp tall) containing the H mark + one word status (`Synced` / `Syncing` / `Ready` / `Blocked`) + a giant serif italic number (workouts synced this week, or seconds since last sync). This is the visual choice that most differentiates this direction.
- Below the band: a horizontal **weekly workout strip** (7 columns, one per day, filled by workouts synced) — the app's first real "chart". Reads instantly. Tap a column to open History filtered by day.
- Below: `Live pipeline` accordion (default collapsed, currently the full Pipeline screen), `Verification`, `Connected services`.
- FAB stays as a **72dp circle** (not a square) with animated ring on progress. When idle it says `SYNC` in mono; when running it becomes a determinate ring; when complete it flashes green then returns to `SYNC`.
- Charts: History gets a **monthly view** with dot-per-day (green dot = verified, red dot = failed, empty circle = no sync). No numbers, no percentages — a calendar strip.
- Bottom nav stays at 4 items but gains a **long-press affordance**: long-press Dashboard to see a peek of last sync's diagnostics. Progressive disclosure.

### Density

- Dashboard is denser than A but less dense than B — the hero band is 100dp, then everything else runs at 16dp padding with 20dp between sections.
- Cards return (rectangles, still zero-radius) but with color-tinted backgrounds: `Recent activity` has a very subtle warm-tint bg (5% accent); `Verification` has a very subtle green-tint bg (5% success) when confirmed.
- Onboarding regains breathing room — 40dp between blocks — because it is a first-impression screen.

### Charts / diagrams

- **Weekly workout strip** (bar chart, no axis labels, dots for tap targets).
- **Monthly verification calendar** (30-40 dot grid).
- Both use only the state palette, not the data-viz palette. Data-viz palette is reserved for future work (heart-rate over time when we can source it — post-Gate 4 territory).

### Motion

- **Bring back the prototype's motion vocabulary, carefully**:
  - Hero band `hs-rise` on Dashboard mount (300ms).
  - Sync complete: green ring pulse from the FAB, one wave outward at 400ms with reduced opacity (subtle, single wave — not the prototype's confetti).
  - Weekly strip: on mount, each column staggers in 30ms apart (fills bottom-up).
  - Pipeline accordion: 250ms ease-out expansion.
  - Onboarding orbit: keep the concentric ring rotation from the prototype (`hs-orbit` @ 40s and `hs-orbit-rev` @ 24s).
- All motion honors `Settings.Global.ANIMATOR_DURATION_SCALE` and reduced-motion.
- The single deliberate loud motion: **sync success**. A user must feel it worked.

### Advantages

- The app becomes shareable. A friend sees this on your phone and asks what it is.
- Real charts (weekly strip, monthly calendar) turn passive ledger data into something a user might glance at daily even without syncing — the app becomes a *personal training log*, not a sync utility.
- Recovers the prototype's ambition without inventing new integrations.

### Risks

- **The AI slop test warning is real.** A cream-bg fitness dashboard with an italic serif for numbers and a red band across the top is not far from generic "premium fitness" tropes. This direction leans into that risk deliberately; if we execute poorly, we look like every workout app Dribbble ever produced.
- Instrument Serif Italic is a strong choice. If it renders wrong on a low-end device or in Chinese-locale users, the whole identity moment collapses.
- The most implementation risk: the hero band + charts require new Compose surfaces (`WeeklyStrip`, `MonthlyCalendar`, `HeroBand`) and layout work.
- Runtime evidence is de-emphasized. If we ever have to prove "no, really, we're honest", the user has to tap through to Diagnostics. Some product owners find this a regression.

### Per-screen change examples

- **Dashboard idle (hero band)**: dark-red 100dp band, `H` mark left, mono `READY` eyebrow, serif italic `0` (workouts synced today) with `today` sublabel, then `Sync now` link inside the band (white on red). Below: weekly strip (7 columns, today outlined). Below: `Recent activity` (last workout name if any, else empty state). Below: `Verification` tint-card. FAB present, bottom-right.
- **Dashboard syncing**: hero band flips to `SYNCING`, serif italic morphs to elapsed seconds (`14s`), a horizontal red bar underneath shows phase progress. Below the band, the weekly strip is unchanged. `Live pipeline` accordion auto-expands during sync, collapses on complete.
- **Dashboard complete**: hero band flips to green-outlined-on-red `SYNCED`, serif italic to `1` (workout confirmed today). Two-line body under the band: `Available in Health Connect · Waiting for GymRats to pick up.` Weekly strip today-column fills.
- **History**: monthly calendar at top, tap a day to filter list below.
- **Onboarding**: the orbit diagram returns to full motion, the H1 is `Your watch, in every app you already use.` (from prototype), primary CTA `Set up in 60 seconds.`
- **Sync modal (wired to FAB)**: prototype-style phase list with animated fill on the active phase and a large red `Cancel` at bottom. Auto-dismisses on complete after a 500ms hold.

---

## How to pick between them

| Question | Direction A | Direction B | Direction C |
|---|---|---|---|
| How much design work? | 4-6 dev days | 10-14 dev days | 14-20 dev days |
| How much risk of regression? | Very low | Low | Medium |
| How different does it look from today? | Same app, sharper | Recognizably different, quieter | Recognizably different, louder |
| Who is the target user? | "I know what I'm doing, don't fuss." | "I care about craft; don't shout at me." | "I want it to feel like a real app." |
| Which prototype ambition survives? | Voice + geometry | Geometry + honesty | Motion + brand |
| Which audit findings does it close? | All P0s + most P1s | All P0s + all P1s | All P0s + P1s that are not about "less color" |
| Does it pass the Impeccable slop test? | Yes — Modernist niche, not a category default | Yes — bone-and-near-black w/ mono values is unusual for the health category | Only if executed well; the trope risk is real |

## Recommendation

The recommendation is a **hybrid**: implement Direction A first (as an M005a slice), then use the calibrated foundation to attempt Direction C (as M005b or M006) after Gate 2 is closed and the user has actually shared the app with a friend.

Direction A alone is enough to ship. Direction B is the safest but most restrained. Direction C is the most exciting but assumes the app has an audience to impress — and today its audience is one Huawei watch owner.

See `m005-prioritized-backlog.md` for the specific P0/P1/P2 backlog derived from these directions.
