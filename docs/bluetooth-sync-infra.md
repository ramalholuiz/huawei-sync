# Bluetooth sync infrastructure

Status: planned.
Implementation started: source seam, Android Bluetooth capability reader, Bluetooth preflight contract, workout summary payload parser, controlled payload source adapter, minimal Android BLE central reader, controlled loopback peripheral harness, and Android wiring for the Bluetooth coordinator.

## Purpose

Shift the next work back to infrastructure: prove a controlled Bluetooth path can move workout-shaped data from a companion device into the existing ledger and Health Connect sync pipeline.

This does not replace the official Huawei Health path unless the watch exposes an official, documented Bluetooth protocol or we control the software running on the watch side.

## Current baseline to preserve

- Gate 1 Health Connect synthetic sync is proven.
- The Room ledger is the durable sync boundary.
- Health Connect `clientRecordId` and `clientRecordVersion` remain the deduplication truth.
- UI, themes, assistant screens, automation screens, and chat-like features are frozen until source ingestion is proven.

## Reference project boundary

The BitChat Android project is useful as a Bluetooth architecture reference, not as product code to import. It proves a production BLE messaging app needs explicit transport ownership: foreground/background lifecycle, scanning/pairing, framing, retry/outbox, peer state, and diagnostics.

Reusable ideas:

- Bluetooth service lifecycle.
- Compact packet framing.
- ACK/retry discipline.
- Outbox/inbox queues.
- Duplicate suppression.
- Privacy-safe diagnostics.

Do not copy into this app:

- Mesh routing.
- Channels.
- Nostr.
- Chat identity.
- Social messaging UI.
- Message encryption machinery until a real workout transport needs it.

Sources:

- BitChat Android: https://github.com/permissionlesstech/bitchat-android
- Android Bluetooth permissions: https://developer.android.com/develop/connectivity/bluetooth/bt-permissions
- Android companion device pairing: https://developer.android.com/develop/connectivity/bluetooth/companion-device-pairing
- Android foreground service types: https://developer.android.com/develop/background-work/services/fgs/service-types

## Minimum next slice

### S01: Source seam

Extract the synthetic-only coordinator into a source-agnostic path:

```text
WorkoutSourceReader -> DomainWorkout -> SyncLedgerStore -> HealthConnectWorkoutWriter -> HealthConnectWorkoutInspector
```

Keep the existing synthetic source as the first implementation so Gate 1 remains reproducible.

Exit evidence:

- Existing Gate 1 tests still pass.
- `WorkoutSource.SYNTHETIC` still produces the same deterministic Health Connect identity.
- `WorkoutSource.BLUETOOTH` can feed the same ledger and Health Connect writer through `WorkoutSourceReader`.
- Controlled Bluetooth payload bytes can be adapted into `WorkoutSourceReader` without touching the ledger directly.
- `AndroidBluetoothSyncWiring` can assemble the Bluetooth source, preflight, Health Connect writer, inspector, and existing coordinator.

### S02: Bluetooth capability preflight

Add Android-native capability checks before any scan/connect attempt:

- Bluetooth adapter present.
- Bluetooth enabled.
- Android 12+ runtime permissions accounted for: `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.
- Older-device location requirement is checked before scan support is claimed.
- Companion Device Manager is evaluated for first pairing.

Exit evidence:

- A local unit test maps each missing capability to a closed error code.
- The manifest changes are limited to the minimum permissions required by the implemented behavior.
- No foreground service is claimed until a physical loopback proves the need.
- `BLUETOOTH_ADVERTISE` is declared only for the controlled loopback peripheral harness.

### S03: Controlled Bluetooth loopback

Before targeting Huawei Watch, prove a simple app-to-app or test-peripheral transfer:

```text
hello -> ack
workout-summary-v1 -> ack
duplicate workout-summary-v1 -> duplicate ack/no new ledger write
```

Exit evidence:

- One workout-shaped payload converts into `DomainWorkout`.
- Three transfers leave one ledger row and one Health Connect record.
- Reinstall behavior still relies on deterministic Health Connect identity, not Room.
- The current repo has the central-side scan/connect/read reader. A peripheral or second app still needs to advertise the service and characteristic.
- The current repo also has a single-read GATT peripheral harness for a second Android device to advertise one workout summary payload.

### S04: Real source decision

Choose one real ingestion path:

1. Official Huawei Health Service Kit/API.
2. Official/documented Huawei Bluetooth protocol.
3. Software we control on the watch side.

If none exists, Bluetooth direct-to-watch remains blocked. Do not reverse engineer Huawei private protocols or read private app storage.

### S05: Notifications and replies

WhatsApp and Telegram reply support is a separate track. It may use Android notification APIs, but it must not block workout sync.

Do not start this until workout ingestion has one real source.

## Blocking criteria

Stop the Bluetooth path if:

- The watch exposes no official or controlled data protocol.
- Required Android permissions cannot be explained clearly to the user.
- Sync requires background behavior that cannot meet Android foreground service rules.
- A duplicate transfer can create a second Health Connect workout.
- The source cannot provide a stable workout identity or enough fields for a deterministic hash.
