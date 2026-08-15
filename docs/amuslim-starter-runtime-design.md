# aMuslim starter runtime and publication design

## Purpose

The existing `TruthDomain` proves useful publication invariants, but its current resolver is built
around one ranked recommendation and five prayer points. It cannot directly represent semantic
periods, personal events, night segments, unranked deed availability, or per-occurrence completion.

The aMuslim implementation should reuse its fail-closed conventions, not force deed behaviour into
the current `TruthResolution` output.

## Publication transform

The draft's localized objects are an authoring convenience only. Before publication, a deterministic
transform must produce separately fingerprinted claims:

1. A language-neutral `ACT` claim containing the canonical action structure.
2. A `TIMING` claim containing recurrence, expression, resolution state, and policy references.
3. One `TEXT` claim for every approved source-language text the app is allowed to reproduce.
4. One `TRANSLATION` claim per visible locale and field, including title, instruction, and timing
   label. Malay and English changes invalidate only their own fingerprints and approvals.
5. Optional `RULING` and `VIRTUE` claims only when separately evidenced and approved.

Every generated claim must have exactly one matching approval before the release can become
`PUBLISHED`. The transform must reject a localized draft field that has no translation approval;
there is no runtime locale fallback for religious wording.

## Minimal deed-specific domain

```text
DeedContentRelease
  ├── DeedDefinition (stable identity and practice category)
  ├── AtomicClaim[] (act, timing, text, translation, optional ruling/virtue)
  ├── EvidenceRecord[]
  ├── ClaimApproval[]
  └── release fingerprint

DeedTemporalRule
  ├── recurrence
  ├── semantic/event expression
  ├── resolution status
  └── reviewed policy reference, when required

DeedAvailabilityResolver
  ├── approved content release
  ├── exact JAKIM day data when relevant
  ├── user-confirmed events such as prayer completion or intended sleep
  ├── reviewed temporal policies
  └── neutral Available / Upcoming / Unresolved results
```

The neutral resolver must not require `RankingPolicy`. It may sort resolved items chronologically
with stable ID as a technical tie-breaker, but must not call that order better, preferred, or
religiously superior.

Only an optional composition step that emits “best”, “primary”, or “utama” may require and consume
the existing separately approved `RankingPolicy`.

## Temporal ownership

- JAKIM owns prayer/solar schedule points, not the user's completion event.
- Personal-event occurrences use the device event instant and zone captured with that event, not a
  schedule-zone date silently.
- Named periods, last-third night, religious-day boundaries, and fasting boundaries remain
  unresolved until a versioned Malaysian temporal policy is approved.
- A policy identifier and fingerprint form part of the timing claim's reviewed payload.
- Cross-midnight instances carry explicit start/end dates; no modulo-time inference is allowed.

## User state

Completion state is not religious content and should live separately, eventually in Room:

```text
DeedCompletion(entryId, occurrenceId, completedAt, deviceZoneId)
```

`occurrenceId` must be deterministic for schedule-based occurrences and event-generated for
personal triggers. Updating the content catalogue must not rewrite or reinterpret past completion
records silently.

## Production gates

- A distinct published-release schema; never weaken the draft schema to permit publication.
- Strict parser rejects unknown fields/enums, duplicate IDs, dangling evidence, unapproved locale
  copy, and post-review mutation.
- Resolver unit tests cover named periods, personal events, prayer completion, last-third night,
  weekdays, cross-midnight rules, and missing inputs.
- APK audit proves draft and in-review files are absent.
- Qualified source, fiqh/timing, Malaysian-context, translation, and usage-rights approvals.
