# Local development tooling

## Ponytail

Ponytail is already installed in Codex, and its hooks have been reviewed and approved. Use it to review changes for simplicity. It must not remove security requirements, Room persistence, idempotency, or tests.

## Graphify

- Version: 0.9.15
- Project skill: `.agents/skills/graphify`
- Purpose: codebase navigation and analysis
- Gate: run it only after Gate 1 passes
- Future command: `/graphify .`

The graph has not been generated yet.

## GSD Pi

- Version: 1.11.0
- Purpose: planning and local project memory

Do not run GSD auto while Codex has an active `/goal`. Do not allow GSD and Codex to edit the same branch or worktree at the same time. Authentication and onboarding are manual owner actions.

## Instruction hierarchy

1. Explicit owner instruction and active `/goal`
2. `AGENTS.md`
3. `docs/gates.md` and `docs/plan.md`
4. Tests and `scripts/verify.sh`
5. Ponytail
6. Graphify
7. GSD, until promoted to the primary orchestrator
