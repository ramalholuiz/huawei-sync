# Cycle 1 — Visual research

**Cycle scope:** Dashboard, Sync-now modal, Pipeline.
**Author role:** Visual Researcher.
**Purpose:** collect current references, then feed them into the Product Art Director step. All references below are **visual references only** unless the classification column says otherwise. Nothing here is auto-approved for the APK — see `cycle-01-asset-manifest.md` for anything considered for shipping.

## 1. What Cycle 1 needs

Cycle 1 has to make three surfaces feel less static, more legible, and honest about what the coordinator actually knows. The reference hunt therefore focuses on:

- **Sync / transfer flows** where waiting, writing, verifying, confirmed, error must be visually distinguishable without lying about progress.
- **Dashboards** that stay calm but not dead — motion that reports state, not decoration.
- **Wearable/companion apps** that describe a pipeline between the watch and a downstream service (this is the closest analogue to Huawei → Huawei Sync → Health Connect → GymRats).
- **Observability / pipeline UIs** for the endpoint-strip metaphor.
- **Micro-interaction and motion craft** to inform the motion spec.

Explicit exclusions: marketing landing pages, casino/game UI, LLM chat UI, generic Material 3 tutorial pages (already the baseline the codebase uses).

## 2. Reference table

Classifications:

- **REF** — visual reference only, does not enter the APK.
- **ASSET** — candidate for the APK, requires a row in `cycle-01-asset-manifest.md` before shipping.
- **BLOCKED** — must not be copied, imitated 1:1, or shipped.

Access date for every row: 2026-07-16 (session date).

### 2.1 Sync / transfer flows

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| S1 | Google / Android | https://developer.android.com/design/ui/mobile/guides/patterns/settings | Reference for calm settings-style hierarchy where each row reveals underlying state. | REF |
| S2 | Google / Android Health | https://developer.android.com/health-and-fitness/guides/health-connect | Canonical language and iconography for Health Connect endpoints. Read to keep our terminology aligned. | REF |
| S3 | Dropbox | https://help.dropbox.com/installs/desktop-app-sync-status | Long-standing example of clear "queued / uploading / synced / error" state distinction on a busy client. | REF |
| S4 | GitHub Desktop | https://docs.github.com/en/desktop/managing-commits/pushing-changes-to-github-from-github-desktop | Two-endpoint pipeline (local ↔ remote) with a strip that never fakes progress. | REF |
| S5 | Rclone | https://rclone.org/commands/rclone_sync/ | CLI-style state vocabulary (checked, transferred, deleted, errors) — reminder that state text can be more honest than a bar. | REF |

### 2.2 Dashboards / calm status surfaces

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| D1 | Linear | https://linear.app/ | Neutral typographic dashboard, restrained motion, single accent — a peer for Direction A/B. | REF |
| D2 | Vercel | https://vercel.com/docs/observability | Observability header pattern (status pill + one primary CTA). | REF |
| D3 | Grafana | https://grafana.com/docs/grafana/latest/dashboards/ | Reference for row-based status density we deliberately reject (too heavy for our audience). | REF |
| D4 | Fitbit (Google) | https://www.google.com/fitbit/app | Consumer dashboard that reduces to a single "today" hero — argues for a stronger Dashboard hero. | REF |
| D5 | Whoop | https://www.whoop.com/us/en/the-locker/whoop-4-0-launch/ | Wearable brand energy achieved through typography + one accent — inspirational for Direction C, not for copy. | REF |

### 2.3 Pipeline / topology UIs

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| P1 | GitHub Actions | https://docs.github.com/en/actions/using-workflows/about-workflows | Node → node pipeline rendered with per-node state color and inline status text. | REF |
| P2 | Temporal | https://docs.temporal.io/develop/ui/ | State-machine UI where each step carries both a shape and a status label — argues against relying on color alone. | REF |
| P3 | Datadog | https://docs.datadoghq.com/service_catalog/ | Endpoint-strip pattern with monogram + label + inline status. | REF |
| P4 | Airbyte | https://docs.airbyte.com/using-airbyte/getting-started/ | Explicit source → destination framing that maps 1:1 onto Huawei → Huawei Sync → Health Connect → GymRats. | REF |

### 2.4 Motion & micro-interaction craft

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| M1 | Material 3 | https://m3.material.io/styles/motion/overview | Duration/easing tokens, in-and-out curves, guidance on "arrivals". | REF |
| M2 | Android Compose docs | https://developer.android.com/develop/ui/compose/animation/introduction | Native API inventory (`AnimatedVisibility`, `AnimatedContent`, `animateContentSize`, `updateTransition`, `Animatable`, `Canvas`, shared-element). Confirms Cycle 1 does not need Lottie/Rive. | REF |
| M3 | Apple Human Interface Guidelines | https://developer.apple.com/design/human-interface-guidelines/motion | Cross-platform sanity check on "motion should communicate state or causality". | REF |
| M4 | Nielsen Norman Group | https://www.nngroup.com/articles/animation-purpose-ux/ | Framework for evaluating whether a motion earns its place. | REF |
| M5 | WCAG 2.2 | https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html | Reduced-motion accessibility requirements — reinforces the `HuaweiSyncMotionPolicy.Reduced` branch. | REF |

### 2.5 Iconography (potential ASSET rows, still REF for Cycle 1)

| # | Owner | URL | Purpose | Class |
|---|---|---|---|---|
| I1 | Material Symbols | https://fonts.google.com/icons | Apache-2.0 icon set already implicitly used through `material-icons-extended`. Reference to justify keeping only what we need. | REF |
| I2 | Feather Icons | https://feathericons.com/ | MIT-licensed line iconography — considered as ASSET, deferred until we have a concrete gap. | REF |

**No Huawei brand asset, no Health Connect logo, no GymRats logo, no third-party wearable brand imagery is referenced for inclusion.** These would move to BLOCKED if proposed.

## 3. What the research settles

- **Distinct visual states.** Every peer we studied (Dropbox, GitHub Desktop, Temporal, Airbyte) distinguishes states with a combination of color + shape + label, not color alone. Cycle 1 must do the same, and must keep the reduced-motion path intact.
- **No fake percent.** Rclone, GitHub Actions, and Temporal all report state, not synthesized percentages. This matches the existing `PipelinePresentation.numericProgress == null` invariant — no reason to change it.
- **Pipeline framing.** Airbyte's source → destination framing is the strongest analogue for Huawei → Huawei Sync → Health Connect → GymRats. Cycle 1 will lean into a four-endpoint model, with GymRats explicitly labelled "available to import" until Gate 2.
- **Motion earns its place.** M1/M3/M4 all agree: motion is justified when it communicates state or causality. That eliminates infinite ambient loops on the Dashboard and eliminates any completion animation before evidence.
- **Native Compose is enough.** M2 confirms every motion we need (state transition, endpoint pulse, confirmed acknowledgement) is available in native Compose APIs. No new dependencies for Cycle 1.

## 4. Brainstorm inputs handed to the Art Director

- Dashboard needs a stronger single "today" hero (D4 argues for hero, D1 for restraint — resolve in Directions).
- Sync-now needs a state-labelled endpoint strip (P4, S3) plus a state-distinct central signal (S3 vs S4).
- Pipeline needs per-node dual-encoding (shape + color + label; P2).
- Confirmed is a one-shot acknowledgement (M3 arrivals), not a persistent animation.
- Error is static; the ambient accent border already used in `SyncHero` is fine — refine the treatment, do not animate it.
- Motion tokens must survive the reduced-motion policy — verify each new motion has a `motion.reducedMotion` branch.
