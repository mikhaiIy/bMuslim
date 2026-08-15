# aMuslim starter deed dataset — final meta-judgment

## Decision

**PASS — research and design goal achieved; production publication remains blocked.**

| Gate | Result |
| --- | --- |
| Daily/weekly research | PASS |
| Evidence judge round 1 | 82/100 — REVISE |
| Data-design judge round 1 | 80/100 — REVISE |
| Evidence judge round 2 | 96/100 — PASS |
| Data-design judge round 2 | 94/100 — PASS |
| Structural/schema validation | PASS |
| Production app integration | DELIBERATELY NOT STARTED |

## Locked outcome

- Nine starter candidates: six daily-context candidates and three weekly candidates.
- Ten inspectable evidence records.
- Versioned JSON and Draft 2020-12 schema, not Room.
- Atomic action/timing claims with unsupported-inference boundaries.
- Machine-readable Sahih/Hasan candidate policy without relabelling grades.
- Semantic periods, user events, night segments, weekday rules, and unresolved policy states.
- No ruling, virtue, priority, or religious ranking defaults.
- Friday charity excluded from the Prophetic-Sunnah tier.
- Dedicated publication transform and neutral deed runtime design.

## Meta-judge resolution

All round-one P1 findings were accepted. Round-two judges reported no P0/P1. Their remaining P2
hardening was accepted except a local Node/Ajv project dependency, which is deferred until the
dataset enters a build. The validator nevertheless runs exact `ajv-cli@5.0.0` Draft 2020-12
validation through a local `ajv` command when present or a pinned `npx` fallback, followed by
cross-record checks.

## What this pass authorizes

- Continued research.
- Qualified review of the candidate claims and temporal policies.
- Design of a parser/resolver against the draft in a separate implementation phase.

## What this pass does not authorize

- Copying the draft into production assets.
- Showing any candidate as an approved Sunnah recommendation.
- Generating notifications or fixed windows from unresolved expressions.
- Using JAKIM time points as proof of user events or jurisprudential boundaries.
- Calling any deed “best”, “utama”, or higher-ranked without a separate approved ranking policy.

## Next bounded phase

Obtain qualified Malaysian source/fiqh and translation review. Then implement a strict draft parser
and a neutral temporal resolver behind a non-production feature flag, using JAKIM only for the
approved schedule inputs and keeping personal events explicit.
