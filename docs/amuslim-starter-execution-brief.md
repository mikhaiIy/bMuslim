# aMuslim starter deed dataset execution brief

## Objective

Create a small, reviewable seed dataset of recurring good deeds whose temporal trigger or
recurrence is explicit in a cited Qur'anic verse or Prophetic report. This phase establishes the
research and data contract only; it does not authorize religious recommendations in the app.

## Orchestration

| Role | Responsibility | Writes product files? |
| --- | --- | --- |
| Root orchestrator | Scope, synthesis, exclusions, final bounded decisions | Yes, dataset/docs only |
| Daily evidence scout | Find explicit daily triggers and reject expanded windows | No |
| Weekly evidence scout | Find weekly recurrences and audit Friday charity | No |
| Dataset architect | Propose schema, temporal vocabulary, governance, JSON-vs-Room decision | No |
| Evidence judge | Independently audit action/timing/source fidelity | No |
| Data-design judge | Independently audit schema, validator, and production boundary | No |
| Root meta-judge | Accept, modify, or reject judge findings and close the research gate | Docs only |

## Locked scope

- A few daily and weekly candidates, not a complete worship catalogue.
- Hadith/Qur'an timing must be explicit; jurisprudential computation is a separate reviewed policy.
- Malay-first candidate copy with English companion text, both unreviewed.
- Versioned JSON outside Android assets.
- No Room database, UI integration, notifications, rankings, or production publication.

## Research lanes

### Daily

- prayer-relative triggers;
- named day periods;
- bedtime/waking events;
- night segments;
- exact action sequence and repetition count.

### Weekly

- Monday/Thursday fasting;
- Friday salawat, dua, Quran recitation, preparation, and attendance;
- special audit of Friday charity claims;
- distinction between Prophetic reports and companion/successor statements.

### Data contract

- atomic action and timing claims;
- source-specific grade/grader metadata;
- recurrence and event-relative expressions;
- unresolved temporal-policy state;
- content lifecycle, approval, and fingerprint path;
- strict separation of religious content from user completion state.

## Acceptance gate

The phase passes only when:

1. Every candidate has evidence supporting both its action and its temporal claim.
2. The data preserves the source timeframe without invented clock offsets.
3. All grades are reported with their source/grader rather than normalised into a bare truth flag.
4. Friday charity is excluded unless a direct reviewed Prophetic basis is found.
5. Draft data is structurally valid and remains absent from production assets.
6. No ruling, virtue, or ranking default is encoded.
7. Independent judges report no accepted P0 or P1 defect after revision.

## Deliverables

- `datasets/amuslim/starter-v0.1/catalog.schema.json`
- `datasets/amuslim/starter-v0.1/catalog.draft.json`
- `datasets/amuslim/starter-v0.1/README.md`
- `scripts/validate-amuslim-dataset.sh`
- `docs/amuslim-starter-research-lock.md`
- independent judge reports and final meta-judgment

## Next phase, deliberately deferred

After qualified Malaysian religious review, build the temporal-policy resolver that maps approved
semantic expressions onto JAKIM prayer/solar data and user events. Production content packaging
remains blocked until claim fingerprints, approvals, translations, and source-use terms are ready.
