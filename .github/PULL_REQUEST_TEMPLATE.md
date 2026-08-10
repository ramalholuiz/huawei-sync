## Scope

-

## Verification

- [ ] `./gradlew clean test lint assembleDebug`
- [ ] `scripts/verify.sh`
- [ ] Device/manual evidence, if needed:

## Sync safety

- [ ] Deterministic `clientRecordId` preserved
- [ ] No duplicate Health Connect write path introduced
- [ ] Health data and raw provider payloads stay out of diagnostics/logs
- [ ] Source-specific models stay out of the domain layer

## Notes

- Blockers:
- Follow-up issues:

