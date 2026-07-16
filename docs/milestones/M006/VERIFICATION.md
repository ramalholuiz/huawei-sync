# M006 · Verification evidence

## Runtime verified

- `./gradlew :app:testDebugUnitTest` — **passed** (199 tests).
- `./gradlew :app:testReleaseUnitTest` — **passed**.
- `./gradlew clean test lint assembleDebug` — **passed** (92 tasks, no lint errors).
- `scripts/verify.sh` — **passed**.
- `git diff --check` — clean.
- Worktree clean before commit.

## APK

- Path: `app/build/outputs/apk/debug/app-debug.apk`
- Size: 18 466 784 bytes (18.5 MB) — no font bundling, matches expectations for S10 fallback path.
- SHA-256: `4a4732d5dbf84e7583ab8145f9ca09ba73cb3251b81739f32bc8e7d1bc752514`

## Emulator smoke test

Installed on `HuaweiSync_API_35` (Android 15 / API 35, 1080×2400).

Screenshots under `docs/milestones/M006/screenshots/`:

1. `01-dashboard-dark-compact.png` — Dashboard in light theme, compact viewport. Verifies S02 (FAB above Verification), S03 (Health Connect + GymRats copy), S04 (MORE not SETTINGS), S05 (no unbacked counts), S08 (no failure card when null), S09 (no section eyebrows), S10a (28sp page title).
2. `02-dashboard-dark-toggled.png` — Same in dark theme. Verifies S06 (bodySmall readable on dark).
3. `03-history-dark.png` — History with 1 record, dark theme. Content branch layout preserved.
4. `04-sync-modal-dark.png` — S01: tapping the Dashboard FAB opens the SyncNowModal with the honest S03 copy "The workout is available for GymRats to import; delivery has not been claimed."
5. `05-dashboard-light-final.png` — Dashboard light theme mid-sync (80% verification phase) with hero body copy "Confirmed in Health Connect. GymRats can import it on next open."
6. `06-dashboard-wide-landscape.png` — Wide primary navigation rail on the landscape viewport. Same hero copy across compact and wide.

## Slice coverage

| # | Slice | Status | Commit |
|---|---|---|---|
| S01 | Sync-now visual feedback + FAB a11y | Done | `8a39747` |
| S02 | FAB clipping, padding, spacing | Done | `fd916fe` |
| S03 | Sync-truth copy for HC & GymRats | Done | `c420482` |
| S04 | Settings → Integrations label | Done | `b62f378` |
| S05 | Remove unbacked step counter | Done | `67e7fae` |
| S06 | Dark ink3 AA contrast | Done | `fe29922`, snapshot in `36dd759` |
| S07 | History empty-state centering | Done | `0df083d` |
| S08 | Failure summary at top of Dashboard | Done | `c98e664` |
| S09 | One eyebrow per screen | Done | `b40a388` |
| S10a | Dashboard page title 28sp | Done | `6312a99` |
| S10b | Bundle Archivo + JetBrains Mono | **Blocked** | — |

## Human decision required

- **Visual approval** on light/dark, compact/wide screenshots above.
- **S10 font-bundling (blocked).** Etapa 4 required verifying SIL OFL 1.1 provenance before shipping the font binaries. I have not verified the provenance from an offline OFL-bundled source, so per Etapa 4's explicit instruction I kept the `FontFamily.SansSerif` / `FontFamily.Monospace` fallback and skipped the bundling. To unblock: hand me licensed offline archives of Archivo and JetBrains Mono (from Google Fonts, JetBrains, or a mirror with an accompanying OFL file), and I will land the bundling + `Type.kt` swap as a follow-up S10b slice.

## Preserved invariants

Diff against the base commit `1bd7126` for the runtime packages listed in the M006 plan §5 acceptance criteria:

- `dev.lui.huaweisync.data.*` (Room ledger + coordinator wiring): untouched.
- `dev.lui.huaweisync.domain.*`: untouched.
- `dev.lui.huaweisync.sync.*` (coordinator, deduplication, reconciliation, claims): untouched.
- `dev.lui.huaweisync.healthconnect.*`: untouched (only labels changed; enums and types identical).
- `ExerciseSessionRecord` shape: untouched; still the only P0 record type.
- Gradle, manifest, and font resources: untouched.

All changes are in `dev.lui.huaweisync.ui.*` presentation code and tests.
