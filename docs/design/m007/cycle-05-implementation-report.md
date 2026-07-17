# Cycle 5 — Visual System Foundation Implementation Report

**Cycle scope:** app-wide visual tokens, semantic colours, rounded Material shapes, foundational components, temporary compatibility aliases, contrast regression coverage, and emulator QA. No navigation or screen-structure redesign.
**Chosen direction:** A — Calm Health Companion (`cycle-04-directions.md § 1`).
**Base commit:** `0ec86e8`.
**Branch:** `milestone/M007`.
**Worktree:** `.gsd-worktrees/M007`.

## 1. Commits

| Commit | Subject | Scope |
|---|---|---|
| `57ce81b` | `feat(m007-c05): establish calm visual tokens` | Radius, spacing, sparse elevation, Direction A light/dark semantic colours, Material shape mapping, typography roles, token and contrast tests. |
| `67d62f7` | `feat(m007-c05): add foundational visual components` | Card/action/chip/state primitives plus source-compatible wrappers for legacy component names and component contract tests. |
| _(this document and captures)_ | `docs(m007-c05): capture visual foundation evidence` | Emulator screenshot matrix and verification evidence only. |

## 2. Foundation delivered

### Tokens and theme

- Radius roles: `chart 8dp`, `control 12dp`, `card 20dp`, `hero/sheet 28dp`, `chip 999dp`.
- Spacing roles extend the existing scale with `gutter 20dp` and `section 40dp`.
- Cards use tonal depth; structural elevation remains zero and overlay/FAB elevation is limited to `3dp`.
- Material defaults resolve to control, card, or hero shapes. No default shape resolves to a square corner.
- Direction A semantic roles cover tonal surfaces, accent, success, attention, error, info, neutral fills, and subtle washes in light and dark themes.
- Typography keeps platform-local fonts, mixed-case product labels, a `28sp` numeric emphasis role, and a `36sp` reserved hero role. Technical monospace remains available for Diagnostics compatibility only.

### Canonical components

- Cards: `SurfaceCard`, `RaisedCard`, `AttentionCard`, `AccentWashCard`.
- Actions: `PrimaryAction`, `SecondaryAction`, `TextAction`, `FabAction`.
- Compact controls: `StatusChip`, `FilterChip`, `Eyebrow`.
- Shared states: `EmptyStateSection`, `ErrorStateSection`, `LoadingSkeleton`.
- `RaisedCard` is borderless by construction; its depth comes from `surface2`.
- Interactive actions and filters preserve mixed-case labels and a minimum `48dp` touch height.

### Temporary compatibility layer

`ModernistSurface`, `StatusLabel`, `TechnicalMicrocopy`, and `StraightEdgeButton` remain as source-compatible wrappers so Cycle 5 can re-skin the current app without changing navigation or screen information architecture. New code uses the canonical components. Removing wrapper call sites belongs to the later per-screen cycles.

## 3. Scope guard verification

The two functional commits change only UI theme, UI components, and UI regression tests. They do not change:

- navigation destinations or navigation state;
- Home information architecture;
- `ModalBottomSheet` usage;
- external logos or assets;
- Room, ledger, coordinator, Health Connect runtime, deterministic identity, or deduplication;
- Cycle 6 or Cycle 7 implementation.

The existing four-destination shell remains intentionally unchanged for the human checkpoint. Cycle 6 is still gated.

## 4. Automated verification

Environment:

- Java: Homebrew OpenJDK 17 (`/opt/homebrew/opt/openjdk@17`).
- Android SDK / adb: `/opt/homebrew/share/android-commandlinetools`.
- Emulator: `emulator-5554`, `sdk_gphone64_arm64`.

Commands executed from the Cycle 5 worktree:

```text
./gradlew testDebugUnitTest \
  --tests dev.lui.huaweisync.ui.VisualTokensContractTest \
  --tests dev.lui.huaweisync.ui.AccessibilityResponsiveRegressionTest \
  --tests dev.lui.huaweisync.ui.VisualComponentsContractTest \
  --tests dev.lui.huaweisync.ui.theme.HuaweiSyncThemeTokensTest
# BUILD SUCCESSFUL

./gradlew clean test lint assembleDebug
# BUILD SUCCESSFUL

scripts/verify.sh
# BUILD SUCCESSFUL (clean test lint assembleDebug)

git diff --check
# exit 0

adb install -r app/build/outputs/apk/debug/app-debug.apk
# Success
```

APK:

```text
c00f04d4e8038d222964dbc9cd045b840927f3e81e7460928246efe46ef331fb  app-debug.apk
```

Regression coverage added or updated:

- `VisualTokensContractTest`: complete role scale, Material shape mapping, semantic/tonal role separation.
- `AccessibilityResponsiveRegressionTest`: WCAG AA checks for secondary text, accent foreground, primary action pairs, and semantic status colours in both themes.
- `VisualComponentsContractTest`: mixed-case labels, `48dp` targets, selected semantics, status rendering, and borderless `RaisedCard` construction.
- `HuaweiSyncThemeTokensTest`: Direction A palette and rounded geometry contract replace the retired red/square prototype assertions.

All existing runtime, honesty-copy, eyebrow-budget, accessibility, lint, and build checks remained green through the full verification commands.

## 5. Emulator evidence

Directory: `docs/milestones/M007/screenshots/cycle-05/`

| Capture | Surface checked |
|---|---|
| `home-emulator.png` | Existing Home structure with Direction A palette, rounded compatible cards, and unchanged navigation shell. |
| `pipeline-emulator.png` | Read-only pipeline and stage cards. |
| `history-emulator.png` | Ledger history and unchanged list-to-detail entry. |
| `detail-emulator.png` | Activity identity, sync path, and lifecycle surface. |
| `integrations-emulator.png` | Connection and evidence cards. |
| `diagnostics-emulator.png` | Diagnostics-only technical vocabulary and verified state. |
| `automation-emulator.png` | Preview-only controls and semantic attention treatment. |
| `assistant-emulator.png` | Local-only assistant disclosure and safe prompt actions. |
| `setup-emulator.png` | Setup actions using rounded primary/secondary compatibility wrappers. |
| `more-emulator.png` | Existing destination sheet; no new navigation introduced. |
| `sync-emulator.png` | Existing sync-status modal; no `ModalBottomSheet` introduced. |

The matrix was navigated through the installed APK using accessibility labels. A first coordinate-based attempt escaped to external emulator apps after a transition; those invalid captures were deleted and are not part of this evidence.

## 6. Human checkpoint

Cycle 5 stops here. Review the installed app and the screenshot matrix for:

1. the Calm Health Companion hue map in light and dark surfaces;
2. rounded tonal cards and mixed-case action labels where compatibility wrappers apply;
3. readable semantic status colours and hierarchy;
4. unchanged navigation flow and unchanged Home structure.

Do not start Cycle 6 or Cycle 7 until this checkpoint is explicitly accepted.
