# Cycle 4 — Screen redesign wireframes

**Cycle scope.** ASCII wireframes for the six most-changed screens under Option C IA + the new visual/motion systems. No pixel mockups; no Compose; no assets. Intended to be read next to `cycle-04-directions.md` for the hue/typography variants.
**Role.** Mobile Art Director.
**Convention.** Rectangles are cards; `─`/`│` are visual dividers; `▤` is an active state; `○` is a status dot; `→` is a tap-through affordance. `[Verb]` is a button. `⋯` is the top-bar overflow.

## 1. Home (compact, replaces `Dashboard`)

```
┌───────────────────────────────────────────────┐
│  Huawei Sync                    ⟳ Sync   ⋯   │  ← top bar (sync icon opens sheet)
├───────────────────────────────────────────────┤
│                                               │
│  Today                                        │  ← eyebrow · titleSmall mixed case
│  1 workout confirmed in Health Connect        │  ← headlineMedium (hero title)
│  Available for GymRats to import              │  ← bodyLarge (hero supporting)
│                                               │
│  ┌─── source → destination row (28dp) ────┐  │  ← small compositional strip
│  │  ○ Huawei  →  ▤ Sync  →  ○ Health Connect │
│  │  Watch not paired · Ready · Confirmed    │
│  └───────────────────────────────────────────┘
│                                               │
│  ┌───────────────────┬───────────────────┐   │
│  │  This week        │  Sync success     │   │  ← two StatTile cards
│  │                   │                   │   │
│  │  3                │  100 %            │   │
│  │  workouts         │  last 3 syncs     │   │
│  └───────────────────┴───────────────────┘   │
│                                               │
│  Recent activity                              │  ← titleLarge section header
│  ┌───────────────────────────────────────────┐│
│  │ ▤ TODAY 08:12  Confirmed              →   ││  ← ActivityRow (tap → detail)
│  │ ○ YESTERDAY 07:03  Confirmed          →   ││
│  │ ○ THU 10 JUL 06:44  Pending readback  →   ││
│  └───────────────────────────────────────────┘│
│  See all →                                     │  ← TextAction (jumps to Activity)
│                                                │
│  ┌───────────────────────────────────────────┐│
│  │       [ Sync now ]        FabAction 56dp   ││  ← anchored bottom-right
│  └───────────────────────────────────────────┘│
└───────────────────────────────────────────────┘
```

Content rules:

- Hero card holds the *most-recent-outcome* copy. On error state it becomes an `ErrorStateSection` (see § 6).
- The source → destination row is small; it renders inside the hero card and is the *only* place that topology lives on Home.
- Two-tile stat strip only when at least one of the two has data. Otherwise a single `Sync your first workout` primary action.
- The "Recent activity" list shows at most 3 rows; deeper drill goes through `See all →` to the Activity tab.
- FAB is the single call to action; long-press on the FAB opens the debug gallery in `BuildConfig.DEBUG` builds (preserves existing gesture) — the current `H`-logo long-press retires.

## 2. Home (wide, ≥ 840 dp)

```
┌──────────────┬────────────────────────────────────────────────┬─────────────────┐
│              │  Huawei Sync                    ⟳ Sync   ⋯    │                 │
│  ▤ Home      ├────────────────────────────────────────────────┤  Sync details   │
│  ○ Activity  │                                                │                 │
│  ○ Connections│  Today                                        │  ○ Preflight     │
│              │  1 workout confirmed in Health Connect         │  ▤ Write         │
│  [Sync now]  │  Available for GymRats to import               │  ○ Acceptance    │
│  Setup       │                                                │  ○ Verification  │
│              │  [ source → destination row ]                   │                 │
│              │                                                │  Sanitized       │
│              │  ┌─────────────┬─────────────┐                 │  failure summary │
│              │  │  This week  │  Success    │                 │  when present    │
│              │  └─────────────┴─────────────┘                 │                 │
│              │                                                │                 │
│              │  Recent activity                               │                 │
│              │  · · · rows · · ·                              │                 │
│              │                                                │                 │
└──────────────┴────────────────────────────────────────────────┴─────────────────┘
```

The wide layout gains a trailing 320dp panel that continually shows the coordinator's `Sync details` — no need to open a sheet on wide. On compact, the same content lives inside the sheet.

## 3. Activity (replaces `History`)

```
┌───────────────────────────────────────────────┐
│  Your activity                  ⟳ Sync   ⋯   │
├───────────────────────────────────────────────┤
│                                               │
│  ┌──────────────────────────────────────────┐│
│  │  12                       [Attention (2)]││  ← summary + FilterChip
│  │  workouts this month                     ││
│  └──────────────────────────────────────────┘│
│                                               │
│  Bars: workouts / week (last 12 weeks)        │
│  ┌───────────────────────────────────────────┐│
│  │  ▁▂▂▃▄▄▅▆▆▇█▇                              ││  ← BarChartCard
│  │  12 weeks ago                    This week││
│  └───────────────────────────────────────────┘│
│                                               │
│  TODAY                                        │  ← soft caps day header
│  ┌───────────────────────────────────────────┐│
│  │ ▤ 08:12  Confirmed                    →   ││  ← ActivityRow
│  ├───────────────────────────────────────────┤│
│  │ ○ 07:03  Pending readback             →   ││
│  └───────────────────────────────────────────┘│
│                                               │
│  YESTERDAY                                    │
│  ┌───────────────────────────────────────────┐│
│  │ ○ 07:12  Confirmed                    →   ││
│  └───────────────────────────────────────────┘│
│                                               │
│  · · ·                                        │
└───────────────────────────────────────────────┘
```

Row rules:

- Left accent strip carries the state colour.
- Primary title = start-of-workout time (until we have workout name post-Gate 3, then it becomes the workout title).
- One-line supporting = state label ("Confirmed", "Pending readback", "Reconciliation required", "Retry available", "Action required").
- Right-aligned attempt count when > 1 ("×2").
- Whole row is a tap target ≥ 64dp.
- Day header uses soft caps + hairline; not the current shouty pill.

## 4. Connections (replaces `Integrations`)

```
┌───────────────────────────────────────────────┐
│  Connections                    ⟳ Sync   ⋯   │
├───────────────────────────────────────────────┤
│                                               │
│  Active                                       │  ← titleLarge
│                                               │
│  ┌───────────────────────────────────────────┐│
│  │ [H] Huawei Health                     →   ││  ← ConnectionRow
│  │  Source · Watch not paired yet            ││
│  ├───────────────────────────────────────────┤│
│  │ [◈] Health Connect                    →   ││  ← official mark (approved)
│  │  Confirmed · exercise session write       ││
│  ├───────────────────────────────────────────┤│
│  │ [G] GymRats                           →   ││
│  │  Available for GymRats to import          ││
│  └───────────────────────────────────────────┘│
│                                               │
│  Preview                                      │
│  ┌───────────────────────────────────────────┐│
│  │ [S] Strava           Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [G] Garmin Connect   Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [F] Fitbit           Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [O] Oura             Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [W] WHOOP            Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [S] Samsung Health   Coming soon      →   ││
│  ├───────────────────────────────────────────┤│
│  │ [G] Google Fit       Coming soon      →   ││
│  └───────────────────────────────────────────┘│
└───────────────────────────────────────────────┘
```

- Only the Health Connect tile carries a real brand mark (approved). Every other tile shows the monogram inside the tile chrome.
- Each row is tappable and opens a `ConnectionDetailSheet` with (a) provider name, (b) current status, (c) a `Learn more` link out to the official app / page, (d) an action button (`Manage in Health Connect`, `Open Huawei Health`, etc.) where legal.

## 5. Sync now (bottom sheet, replaces the current dialog)

```
      (page scrim, dismiss on tap)
┌───────────────────────────────────────────────┐
│                    ▁▁▁▁▁                       │  ← drag handle
│                                                │
│  Syncing                                       │  ← titleLarge (or "Ready to sync")
│  Reading your watch · then writing to          │  ← bodyMedium (context)
│  Health Connect                                │
│                                                │
│  ┌───────────────────────────────────────────┐│
│  │  ○ Huawei  →  ▤ Sync  →  ○ Health Connect ││  ← pipeline rail (compact)
│  └───────────────────────────────────────────┘│
│                                                │
│  Coordinator phase                             │  ← titleSmall mixed case
│  Write in progress                             │  ← titleMedium (current phase)
│                                                │
│  [ Sanitized failure summary card if present ]│
│                                                │
│  [ Start sync ] (or [ Cancel this run ] )      │  ← primary action
│  [ Show phase detail ]                         │  ← TextAction (opens SyncDetailsSheet)
│  [ Close ]                                     │  ← secondary
└───────────────────────────────────────────────┘
```

Rules:

- Opening the sheet does not trigger `onSync`. The `Start sync` button does. Separates *watching* from *triggering* (see IA doc § 5.2).
- No letter medallion.
- The failure summary card appears only when `sanitizedFailureSummary != null`.
- Sheet can be dismissed while a sync is in progress; the coordinator continues.

## 6. Sync failure (Home state) — the error hero

```
┌───────────────────────────────────────────────┐
│  Huawei Sync                    ⟳ Sync   ⋯   │
├───────────────────────────────────────────────┤
│                                               │
│  Today                                        │
│                                               │
│  ┌───────────────────────────────────────────┐│
│  │ ✕  Sync couldn't complete                 ││  ← AttentionCard variant
│  │                                           ││
│  │  Health Connect write failed. Retry after ││
│  │  reviewing availability and permission.   ││
│  │                                           ││
│  │  [Retry sync]      [Diagnose this]        ││
│  └───────────────────────────────────────────┘│
│                                               │
│  Last confirmed 2 hours ago                   │  ← bodySmall (still shows the truth)
│  · · · recent activity (same as Home) · · ·   │
│                                               │
└───────────────────────────────────────────────┘
```

- Error hero uses the `AttentionCard` variant + `surface.attentionWash` overlay.
- Both `Retry sync` and `Diagnose this` are prominent; `Retry sync` re-opens the Sync now sheet with the button primed.
- The rest of Home stays intact — history and success moments are still visible so a failed sync does not black out the app.

## 7. Activity detail (post-redesign)

```
┌───────────────────────────────────────────────┐
│  ← Your workout                          ⋯   │
├───────────────────────────────────────────────┤
│                                               │
│  TUE 15 JUL, 07:03  ·  4 min                  │  ← titleLarge (workout header)
│  Confirmed in Health Connect                  │  ← bodyLarge
│                                               │
│  ┌───────────────────────────────────────────┐│
│  │  Lifecycle                                ││  ← titleMedium
│  │                                           ││
│  │  ●  Prepared                              ││
│  │  │  15 Jul 07:03 UTC                      ││
│  │  ●  Health Connect accepted               ││
│  │  │  15 Jul 07:03 UTC                      ││
│  │  ●  Readback verified                     ││
│  │  │  15 Jul 07:03 UTC                      ││
│  │  ●  Available for GymRats to import       ││
│  │                                           ││
│  └───────────────────────────────────────────┘│
│                                               │
│  [ Show technical detail ]                    │  ← disclosure toggle
│                                               │
│  When expanded:                               │
│  ┌───────────────────────────────────────────┐│
│  │  Client record ID  hs:v1:9f17…c73eed     ││
│  │  Record version    2                      ││
│  │  Attempt count     3                      ││
│  │  Source provider   synthetic              ││
│  │  Safe error code   —                      ││
│  └───────────────────────────────────────────┘│
└───────────────────────────────────────────────┘
```

- The sync-path rail retires from this screen — the lifecycle timeline says everything the rail said.
- Technical detail stays behind a disclosure.
- Header will get real workout title + type + duration post-Gate 3.

## 8. Behaviour matrix per state (Home hero)

| State | Hero card | Primary action | Motion |
|---|---|---|---|
| Ready (empty) | `Sync your first workout` · `Bring your watch nearby` | Sync now | None. |
| Ready (returning) | `All caught up` · `Last confirmed <human time>` | Sync now | None. |
| Waiting | `Waiting to sync` · `Bring your watch nearby` | Sync now (still available) | Info-role dot at rest. |
| Syncing | `Syncing your workout` · `Writing to Health Connect` | Cancel this run (secondary) | Pipeline rail active-node rotation (only inside the sheet). |
| Verified | `Workout confirmed in Health Connect` · `Available for GymRats to import` | See details → | One-shot success moment. |
| Error | `Sync couldn't complete` · sanitized summary | Retry sync · Diagnose this | None (short fade in of the summary). |

## 9. What the wireframes do not show yet

- **First-run behaviour.** Onboarding is auto-launched if the user has never granted Health Connect. Onboarding wireframe is unchanged in shape from today (mission line + permission-grant primary + `I already set it up` secondary) — its redesign lives inside `cycle-04-directions.md § 2 / § 3 / § 4`.
- **Overflow menu.** Contains `Diagnose`, `Setup`, `About`, `Send feedback` — see IA doc § 5.3.
- **Diagnostics.** Content-identical to today; opens as a full page via `Overflow → Diagnose`.
- **Automation / AI Assistant.** Not reachable in the redesigned app until relit.
