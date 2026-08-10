# Engineering standards

These standards are the daily rules for this repo. `AGENTS.md` remains the mission and hard-boundary document.

## Definition of done

A change is done only when:

- The scope is documented in the PR or progress note.
- Sync behavior has a regression test when behavior changed.
- `scripts/verify.sh` or the equivalent Gradle command passes in an environment with Java and Android SDK.
- Any unavailable verification is called out as `BLOCKED` with the exact missing prerequisite.
- Health data, provider payloads, raw identifiers, and exception text are not exported into diagnostics.

## Sync rules

- Route every source through the domain model before writing to Health Connect.
- Keep source-specific SDK, Bluetooth, or API models out of the domain layer.
- Every Health Connect write needs a deterministic `clientRecordId`.
- Deduplication must survive app reinstall.
- Do not add metrics such as heart rate, calories, distance, sleep, SpO2, or body composition until the workout vertical slice is stable.
- Prefer Health Connect for app-to-app sync. Direct app APIs require a documented reason.

## Bluetooth rules

- Bluetooth is a source-ingestion transport, not a product scope expansion.
- Scan/connect code must be behind a preflight that checks adapter, enabled state, permissions, and pairing approach.
- Controlled payloads come before real watch data.
- Real Huawei watch integration requires official/documented authorization or user-controlled watch-side software.
- No mesh chat, Nostr, WhatsApp/Telegram reply feature, or reverse-engineered Huawei protocol belongs in the MVP.

## Testing rules

- Parser, mapper, ledger, idempotency, permission manifest, and failure-state changes need unit tests.
- UI-only visual tweaks need contract or state tests only when behavior/copy/state changes.
- Add instrumented tests only when unit tests cannot cover the Android framework behavior.
- Keep tests focused on the risk introduced by the change.

## Release rules

- Keystores and signing secrets stay outside Git.
- Release builds need a recorded version, verification command, APK/AAB hash, and privacy review.
- Store rollout waits until Gate 4 proves one real workout end to end.

