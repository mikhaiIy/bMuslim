# Design round 1 independent judgment

## Decision

**ITERATE.** Direction B is the strongest base, but needs Direction A's physical isolation and a
stricter variant matrix. Scores out of 90: A 66, B 73, C 48; corrected hybrid estimate 82.

## Core findings

- Debug/release is a tooling axis; demo/production is the product-truth axis.
- Runtime classification cannot prove fixture absence while fixture text/resources/classes remain
  in main. Direction C is rejected.
- `demoRelease` would still be a distributable sacred synthetic artifact and must be disabled.
- The safe matrix is `demoDebug`, `productionDebug`, and `productionRelease`.
- Demo needs an application ID suffix, distinct label, and visible/semantic prototype disclosure.

## P0 corrections

1. Move the whole current fixture-dependent timeline evidence instrument to demo ownership, not
   merely `sampleDeeds()`. Main currently embeds fixed date/time, ranking language, day phases,
   sample citations, and direct construction.
2. Disable `demoRelease` using the Android variant API.
3. Resolve a sealed entry state before any timeline function; the current renderer cannot safely
   accept empty input.
4. Bind every approval to the exact non-empty typed claim fingerprint; the release fingerprint
   covers ordered claim identity and payload.
5. Enforce physical source/resource/dependency exclusion; runtime flags do not count.

## P1 corrections

- Model permission as `Permitted`, `Denied`, or `Unknown`, separately for use and persistence.
- Return a deterministic blocker set or define an explicit failure precedence.
- Keep ruling priority and sort-derived primary selection demo-only.
- Unavailable production renders no clock/date/Hijri/prayer/current/next claim.
- Move fixture tests to demo; run truth-gate tests against production variants.
- Inspect assembled production artifacts against fixture IDs, fingerprints, classes, resources, and
  dependencies.
- Demo disclosure is visible and semantic; production shell is clear, focusable, BM-first, and has
  no disabled controls implying hidden guidance.

## Smallest corrected lock requested

1. Correct variant matrix and disabled demo release.
2. Demo owns current prototype; main owns contracts/validators/resolver; production owns shell.
3. Production injects empty/unconfigured/no-ranking inputs and renders not-ready.
4. Only resolver creates ready from all approved gates.
5. Reviews bind to claim fingerprints; no truth defaults.
6. Deterministic multi-blocker behavior.
7. Production artifact proof.
8. Existing demo geometry/interactions preserved.
