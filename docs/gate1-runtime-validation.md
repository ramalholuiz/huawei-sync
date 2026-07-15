# Gate 1 runtime validation

This is the authoritative device procedure for Gate 1. It validates permission, one synthetic `ExerciseSessionRecord`, bounded readback, three actions without duplication, Room versus Health Connect counts, and uninstall/reinstall behavior.

Gate 1 remains **BLOCKED** until every expected result below has been observed on a real Health Connect runtime and the evidence packet has been recorded in `docs/progress.md` and linked from `docs/gates.md`. Passing Gradle or this document's static audit is not device evidence.

## Safety and evidence boundary

- Use a dedicated test device, emulator snapshot, or Android test profile on which this app has never written the Gate 1 record. The initial authoritative count must be zero; do not continue from an unknown baseline.
- Do not collect a Health Connect database, raw health payload, stack trace, exception text, full deterministic client record ID, or another user's health data.
- The app's **Share sanitized diagnostics** output is the canonical text evidence. It contains only stable codes, bounded counts, boolean version facts, and a closed recovery action.
- Screenshots may show the permission grant, the single synthetic exercise in Health Connect, and the app diagnostics, but must be cropped/redacted if unrelated health information or identifiers are visible.
- If any expected value differs, stop. Save the sanitized report for that step, leave Gate 1 `BLOCKED`, and follow only the report's `next_action`; never blind-retry an inconclusive write.

## Preconditions

1. Use repository HEAD with Android SDK platform 36 installed, JDK 17 selected, and `adb` available.
2. Connect exactly one supported Android device with Health Connect available. Accept the device authorization prompt.
3. Start from a clean test runtime where this app's authoritative Health Connect match count is zero.
4. Record the date/time zone, device model, Android version, Health Connect version or system-module build, source revision, and APK SHA-256. Do not record device serial numbers.

Use one platform procedure below, then continue at **Shared device procedure**.

## Windows PowerShell procedure

Run from the repository root in Windows PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify.ps1
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell am force-stop dev.lui.huaweisync
adb shell am start -W -n dev.lui.huaweisync/.MainActivity
Get-FileHash .\app\build\outputs\apk\debug\app-debug.apk -Algorithm SHA256
```

Expected: verification prints `PASS: clean test lint assembleDebug`; exactly one authorized device is listed; install and activity start succeed; the app reports Health Connect as available.

## POSIX shell procedure

Run from the repository root in macOS or Linux:

```bash
scripts/verify.sh
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop dev.lui.huaweisync
adb shell am start -W -n dev.lui.huaweisync/.MainActivity
shasum -a 256 app/build/outputs/apk/debug/app-debug.apk
```

On Linux, replace the final command with `sha256sum app/build/outputs/apk/debug/app-debug.apk` when `shasum` is unavailable.

Expected: Gradle exits zero; exactly one authorized device is listed; install and activity start succeed; the app reports Health Connect as available.

## Shared device procedure

For each checkpoint, tap **Share sanitized diagnostics**, save the text as the named evidence item, and compare every listed invariant. Exporting a report does not change sync state.

### 1. Grant permission and prove a clean baseline

1. In the app, tap **Request ExerciseSessionRecord permissions**.
2. In the system Health Connect permission UI, allow the exercise read and write permissions requested by Huawei Sync, then return to the app.
3. Tap **Refresh Room and Health Connect counts**.
4. Save the report as `01-clean-baseline`.

Expected:

- Health Connect is available and both exercise permissions are granted.
- `room_row_count=0`.
- `health_connect_match_count=0` and `version_match=false`.
- `next_action=RUN_SYNC`.

If the Health Connect count is not zero, restore a known-clean dedicated test runtime and restart this procedure. Do not delete unrelated records.

### 2. Action 1: write once

1. Tap **Run synthetic strength sync** once and wait for the busy indicator to clear.
2. Save the report as `02-after-write`.

Expected:

- `durable_status=VERIFICATION_PENDING` and `next_action=CONFIRM`.
- `write_attempt_count=1` and `room_row_count=1`.
- `health_connect_match_count=1` and `version_match=true`.

A timeout, provider error, process loss, duplicate count, version mismatch, or inconclusive scan is a failed checkpoint. Follow the displayed recovery action and keep Gate 1 blocked.

### 3. Action 2: authoritative confirmation

1. Tap **Confirm Health Connect record** once and wait for the busy indicator to clear.
2. Save the report as `03-after-confirm`.
3. Open the system Health Connect UI and verify exactly one exercise named **Gate 1 synthetic strength training** exists from `2026-07-14T11:15:00Z` through `2026-07-14T12:00:00Z`. Capture a privacy-safe screenshot.

Expected:

- `durable_status=VERIFIED` and `next_action=NONE`.
- `write_attempt_count=1` and `room_row_count=1`.
- `health_connect_match_count=1` and `version_match=true`.

### 4. Action 3: idempotent no-op

1. Tap **Run synthetic strength sync** once more and wait for the busy indicator to clear.
2. Tap **Refresh Room and Health Connect counts**.
3. Save the report as `04-after-third-action`.

Expected: state remains verified and all counters remain unchanged: `write_attempt_count=1`, `room_row_count=1`, `health_connect_match_count=1`, and `version_match=true`.

### 5. Uninstall/reinstall persistence

Keep the device connected. Uninstall removes the app's Room database but must not remove the Health Connect record.

Windows PowerShell:

```powershell
adb uninstall dev.lui.huaweisync
adb install .\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -W -n dev.lui.huaweisync/.MainActivity
```

POSIX shell:

```bash
adb uninstall dev.lui.huaweisync
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n dev.lui.huaweisync/.MainActivity
```

Then:

1. Grant exercise read/write permissions again if the system requests them.
2. Tap **Refresh Room and Health Connect counts** and save `05-after-reinstall-before-sync`.
3. Verify `room_row_count=0`, `health_connect_match_count=1`, and `version_match=true`. This proves the ledger was reset while the deterministic Health Connect record survived.
4. Tap **Run synthetic strength sync**, then perform the displayed **Confirm Health Connect record** action if requested.
5. Tap **Refresh Room and Health Connect counts** and save `06-after-reinstall-resync`.
6. Verify the final report has `room_row_count=1`, `health_connect_match_count=1`, `version_match=true`, and `write_attempt_count=1`. Verify Health Connect still shows exactly one synthetic exercise.

Any second Health Connect match fails reinstall deduplication. Stop and keep Gate 1 blocked.

## Evidence packet and Gate 1 transition

Record one row per checkpoint in `docs/progress.md`:

| Checkpoint | Status code | Durable state | Next action | Write attempts | Room rows | HC matches | Version match | Evidence path |
| --- | --- | --- | --- | ---: | ---: | ---: | --- | --- |
| clean baseline |  |  |  |  |  |  |  |  |
| after write |  |  |  |  |  |  |  |  |
| after confirm |  |  |  |  |  |  |  |  |
| third action |  |  |  |  |  |  |  |  |
| reinstall before sync |  |  |  |  |  |  |  |  |
| reinstall resync |  |  |  |  |  |  |  |  |

Also record the environment metadata, APK SHA-256, sanitized exports, permission screenshot, and Health Connect single-record screenshots. Only after every row matches this procedure may `docs/gates.md` change Gate 1 from `BLOCKED` to `PASS`.
