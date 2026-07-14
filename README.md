# huawei-sync

Personal sideloaded Android app to sync workouts from Huawei Health into Android Health Connect so apps such as GymRats can consume them.

Primary flow:

```text
Huawei Watch Fit 5 Pro → Huawei Health → huawei-sync → Health Connect → GymRats
```

Optional later fallback flow, not before Gate 1 and Gate 2 pass:

```text
Huawei Health → Strava → huawei-sync → Health Connect
```

The project is gate-driven. Do not build the full product until Gate 1 Health Connect synthetic write, Gate 2 manual GymRats import validation, Gate 3 Huawei real workout read, and Gate 4 Huawei → Health Connect → GymRats vertical slice are proven with reproducible evidence. GymRats is validated manually through Health Connect only; this app must not implement any direct GymRats integration.

## Documentation

- `AGENTS.md` — permanent project rules.
- `docs/research.md` — official API references, limitations, and open questions.
- `docs/plan.md` — implementation phases, risks, and decisions.
- `docs/gates.md` — gate checklists and PASS/BLOCKED evidence.
- `docs/progress.md` — progress log and next steps.
- `docs/data-mapping.md` — Huawei/Strava → domain → Health Connect mapping.
- `scripts/verify.sh` — verification entry point.

## Verification

After the Android project exists, run:

```bash
scripts/verify.sh
```

The final definition of done requires both commands to pass:

```bash
./gradlew clean test lint assembleDebug
scripts/verify.sh
```
