# Bluetooth desktop validation

Use this on the Windows desktop with Android Studio. The Mac worktree is for code only.

## Preconditions

- Java and Android SDK available on the desktop.
- Two Android devices or emulators with Bluetooth support.
- Health Connect installed and available on the receiver device.
- Nearby devices permissions granted for this app.
- Health Connect exercise read/write permissions granted for this app.

## Build checks

Run from the repo root on the desktop:

```bash
./gradlew clean test lint assembleDebug
scripts/verify.sh
```

Expected:

- Unit tests pass.
- Lint has no errors.
- Debug APK is produced.

## Controlled loopback shape

Peripheral device:

- Starts `AndroidBleWorkoutLoopbackPeripheral`.
- Advertises service `c4a6f6f0-6c2f-4f65-9df1-3698b9db2c01`.
- Serves characteristic `c4a6f6f1-6c2f-4f65-9df1-3698b9db2c01`.
- Payload must be `huawei-sync-workout-v1` and at most 512 bytes for this harness.

Receiver device:

- Uses `AndroidBluetoothSyncWiring.coordinator(context, ledgerStore)`.
- Runs the existing coordinator action.
- The coordinator reads BLE bytes, decodes `DomainWorkout`, writes Health Connect, and records the Room ledger.

## Evidence to capture

- Desktop command output for Gradle verification.
- Android versions and app version.
- Bluetooth permission state on both devices.
- Health Connect permission state on receiver.
- Sanitized result code from first transfer.
- Sanitized result code from two duplicate transfers.
- Room row count for the Bluetooth source identity.
- Health Connect match count for the deterministic client record.

## Pass condition

- First transfer writes one Health Connect workout.
- Second and third transfers do not create duplicates.
- Room has one row for the Bluetooth source identity.
- No raw health payload or raw provider identifier is exported.

## Current blocker

This repo now has central and peripheral loopback code, but the physical loopback is not marked `PASS` until the desktop/device evidence above is recorded.

