# aMuslim starter data-design judge — round 1

## Verdict

**80/100 — REVISE**

No P0 defect. JSON-before-Room, draft isolation, explicit unsupported inferences, and the absence
of ruling/ranking defaults were judged sound.

## P1 findings

1. The validator ran selected `jq` checks but not the supplied Draft 2020-12 schema; the schema's
   temporal discriminators were also too permissive.
2. The post-prayer timing narrowed “every prayer” to obligatory prayer.
3. Embedded localized copy had no documented path to independently fingerprinted and approved
   `TEXT` / `TRANSLATION` claims.
4. The existing ranked `TruthResolution` cannot directly represent neutral deed availability,
   semantic periods, user events, or night segments.

## Orchestrator response

All four findings were accepted:

- the validator now invokes Ajv Draft 2020-12 validation and retains cross-record checks;
- recurrence and expression kinds have conditional schemas with closed parameter sets;
- prayer scope now matches the cited phrase;
- the publication transform and separate deed-specific neutral runtime are documented in
  `docs/amuslim-starter-runtime-design.md`.

Additional accepted hardening includes full weekday vocabulary, non-whitespace copy checks,
source-assessment/grader consistency, removal of the hard-coded nine-entry validator limit, and
orphan-evidence rejection.
