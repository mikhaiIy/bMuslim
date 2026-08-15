# aMuslim starter data-design judge — round 2

## Verdict

**94/100 — PASS**

Remaining P0: none. Remaining P1: none.

The judge confirmed that real schema validation, typed temporal discriminators, source-faithful
prayer scope, independent localization publication claims, and a deed-specific neutral runtime
resolved all round-one P1 findings.

## Residual P2 response

- Accepted: recurrence/expression semantic pair checks were added.
- Accepted: duplicated weekday values are cross-checked by the validator.
- Accepted: primary and corroborative evidence overlap is rejected.
- Modified/deferred: Ajv is exactly pinned through `npx` when no local `ajv` executable exists.
  A locally installed CI dependency is deferred until the project adopts this dataset in a build;
  adding a Node package toolchain solely for a non-production research artefact is not justified.

The current catalogue passes the validator and deliberately remains outside Android assets.
