# Wave 2: truthful accessible journey

## Verdict

The safe UX work is a fail-closed state contract and accessible linear journey using synthetic
content. Live recommendation wording and usability claims remain blocked on content/provider
approval and BM-first device sessions.

Firecrawl returned HTTP 402; the scout used official Android/W3C sources and research literature.
No files were edited.

## Findings

1. Lead with a focusable act-now route and retain the wheel for exploration. Complex gesture
   semantics need direct actions; user preference still needs BM-first testing.
   https://developer.android.com/guide/topics/ui/accessibility/composables
2. A primary option must come from approved policy; all valid alternatives remain operable. Default
   effects support caution, not a particular devotional ranking.
3. Setup, unavailable, stale, and demo are mutually exclusive truth states. Stale recommends only
   under an approved provider policy; demo never uses current/best/official wording.
4. `actualMoment` and `exploredMoment` need separate identities. Scrubbing must not relabel explored
   past/future time as now.
5. Evidence must be claim-specific, offline-readable metadata with accessible supplementary links;
   placement and terminology still need user testing.
   https://developer.android.com/develop/ui/compose/accessibility/semantics
6. Completion is durable versioned application data, not `rememberSaveable`. Correction migration
   requires explicit equivalence policy.
   https://developer.android.com/topic/libraries/architecture/saving-states
7. Day/zone/schedule/content changes invalidate before replacement. Important non-focus changes may
   use concise polite status announcements; clocks must not become noisy live regions.
8. The entire setup → state → primary → alternatives → evidence → complete/undo → return journey
   must work with touch, TalkBack, keyboard/D-pad, and Switch Access without dragging.
   https://developer.android.com/guide/topics/ui/accessibility/testing

## State contract

```text
Boot
 ├─ SetupRequired: no recommendation; choose/change setup
 ├─ Resolving: no new claim; retain only still-valid matching local state
 ├─ ReadyFresh: actual-now plus approved primary and alternatives
 ├─ ReadyStale: only if approved; bounded disclosure and refresh
 ├─ Unavailable: no recommendation; retry/change/status
 └─ Demo: synthetic examples, separate persistence, no live claims
```

Within a ready state, actual-now leads to primary summary, instructions, evidence, and completion;
alternatives use the same detail contract. Wheel exploration owns `exploredMoment`, never mutates
`actualMoment`, and offers explicit return to now.

Recommended completion identity includes civil date, zone, deed ID, content version,
schedule/window version, occurrence ID, and completed instant. Corrections preserve audit history
but restore current completion only through explicit migration.

## Acceptance tests

- Exactly one actual-now semantic owner; no duplicate focusable handoff copies.
- TalkBack and Switch Access complete the full journey and reach alternatives without drag.
- The wheel remains adjustable through semantic actions.
- Setup/unavailable/stale/demo/loading/error/completed states are programmatically determinable and
  never color-only.
- Status changes announce once without unexpected focus movement.
- Focus lands logically after open, close, retry, rollover, and withdrawal.
- Every action has a non-overlapping target of at least 48×48dp.
- Completion restores only for matching date/zone/content/schedule occurrence.
- Airplane cold start yields exact valid local state, permitted stale, or unavailable—never another
  date/zone.
- Manual device gates cover scanner, TalkBack, Switch Access, keyboard/D-pad, large font, and BM
  pronunciation/comprehension.

Safe now: state types/reducer, actual versus explored moment, linear semantics, failure/demo states,
evidence interfaces, versioned completion, focus and traversal tests. Not safe: live primary
recommendations, production wording/rankings, provider freshness claims, automatic correction
migration, or declaring usability before BM-first assistive-tech sessions.
