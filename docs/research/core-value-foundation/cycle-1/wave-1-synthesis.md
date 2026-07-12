# Wave 1 synthesis

## Verdict

The three proposals are dependent parts of one truth system:

```text
Published Content Ledger ─┐
                          ├─ Resolver/ranking contract ─ Act-now journey
PrayerDaySnapshot state ──┘
```

The ledger determines what the app may claim and which evidence supports each atomic claim.
`PrayerDaySnapshot` determines whether temporal inputs are valid for the chosen zone and civil
date. The act-now journey is the user-facing integration test. Building the act-now UI before both
truth contracts would only make unsupported assertions more persuasive.

This synthesis does not select an implementation.

## Evidence classes

### Sourced facts

- JAKIM/KDN guidance requires authoritative, accurate, traceable Islamic content and distinct
  controls for hadith and Quran publication.
- Official e-Solat pages establish JAKIM as the authority for displayed Malaysian zone schedules,
  but wave one found no supported public API or redistribution contract.
- Android/Java documentation supports explicit `Clock`, date-bearing zoned instants, offline-first
  local reads, inexact WorkManager behavior, and exact-alarm limitations.
- Current source directly confirms a default `Sahih` grade; fused religious claims and sample
  times; a static JAKIM/WLY01 claim; fixed now/date; transient completion; unreviewed sorting;
  plain citation strings; and no provenance or publication schema.
- Current tests cover geometry and sample timeline behavior, not publication provenance, durable
  completion, date rollover, or end-to-end product truth.

### Cross-source inferences

- “Best deed right now” is compound:
  `approved act + approved timing + valid schedule + reviewed ranking + honest freshness wording`.
- The content ledger and snapshot selector are independent truth producers; neither alone creates a
  safe recommendation.
- The resolver is a composition seam, not the only domain-risk seam.
- Fail-closed behavior is appropriate: unpublished content or unavailable time truth must not
  produce a plausible fallback recommendation.
- Content version belongs in completion identity or migration policy.
- A human-readable official page does not imply an API, caching, or redistribution agreement.

### Hypotheses still needing evidence

- One primary recommendation plus visible alternatives improves comprehension without asserting
  unique religious superiority.
- Zone plus level is the minimum setup.
- A linear action path should lead while the wheel remains exploratory.
- Some bounded same-date stale schedule is acceptable.
- Users understand source, reviewer, and app-estimate distinctions.
- The proposed completion key is sufficient for corrected/versioned content.
- Existing BM instructions are actionable and doctrinally accurate.

## Ranked principles

1. No religious claim without atomic evidence, reviewer scope, licence status, and publication
   state.
2. No current-time claim without zone, civil date, date-bearing instants, provenance, completeness,
   and freshness.
3. The weakest input governs the recommendation; missing review or unavailable schedule means no
   “best now” assertion.
4. Separate act, text, translation, timing, ruling, virtue, and ranking evidence.
5. Prefer explicit uncertainty or unavailability over fabricated continuity.
6. Treat “primary now” as reviewed policy, not sort order.
7. Make the smallest journey executable and verifiable.
8. Provide direct accessible operation without a wheel gesture.
9. Scope completion to resolved day and versioned content identity.
10. Defer Hijri suggestions, Quran excerpts, and reminders until their distinct dependencies are
    settled.

## Highest product risks

| Risk | Status | Impact | Required control |
| --- | --- | --- | --- |
| Mismatched religious text/citation | Demonstrated | Critical | Atomic ledger, review, fingerprint, withdrawal |
| Related source used for unsupported timing/ruling | Demonstrated | Critical | Claim-specific evidence scope |
| Static/wrong-zone/date schedule presented as current | Demonstrated architecture | Critical | Snapshot selector, fail-closed state |
| Sort order presented as “best” | Demonstrated | Critical | Qualified ranking/wording contract |
| Umm al-Qura presented as Malaysian date truth | High if PRD followed | High | Malaysian calendar policy |
| Invalid payload overwrites valid cache | Unknown | High | Repository contract tests |
| Correct link mistaken for reuse permission | High | High | Licence/permission gate |
| Completion leaks across day/content correction | High | Medium | Scoped versioned identity |
| Wheel-only journey excludes assistive technology | Medium | High | Linear accessible route |
| Notification precision exceeds platform guarantee | High if built now | High | Defer and define fallback |

## Measurable audit checklist

### Content

- Zero published claims missing evidence, review, supported scope, jurisdiction, review date,
  licence status, or conflict disposition.
- Zero default grades, rulings, windows, authenticity states, or ranking weights.
- Every visible claim maps to explicit evidence IDs and versioned source content.
- Each release has an integrity hash, correction history, and tested single-entry withdrawal.
- Draft, in-review, and withdrawn content cannot enter the published runtime catalogue.

### Time

- Every snapshot contains JAKIM zone, civil date, explicit zone, provenance, fetch time,
  completeness, and date-bearing prayer instants.
- Tests cover fresh, stale, absent, incomplete, wrong-zone/date, midnight, next-day Fajr, clock
  jumps, and zone changes.
- Invalid success never replaces valid local data.
- Network failure returns matching stale state or unavailable, never another date's schedule.
- Last-third calculation spans Maghrib(date) to Fajr(date+1) and is labelled approximate.

### Act-now journey

- Unpublished content or unavailable schedule yields zero confident recommendations.
- Fresh/stale/estimated state is visible and semantic.
- Primary and all valid alternatives remain reachable.
- Instructions state only reviewed action, amount, and window.
- Completion survives force-stop and handles day/content-version changes explicitly.
- Touch, TalkBack, and Switch Access can complete the journey without dragging.
- Permission denial does not block the core journey.

## Unsupported or weak current assumptions

- “Widely agreed,” “human verified,” “from JAKIM,” and “cached about 24h” are not sufficient
  contracts.
- A bare `Sahih`/`Hasan` grade lacks grader and edition context.
- Related hadith do not necessarily prove timing or ruling claims.
- `HijrahChronology` does not provide authoritative Malaysian observation truth.
- “Everything works offline” cannot include external verification pages.
- Usability literature supports hypotheses, not religious ranking.
- The PRD's claim that the resolver contains all domain risk excludes upstream content, provider,
  calendar, and ranking risks.

## Safe foundations before external approvals

- Ledger schema, lifecycle, validators, hashes, history, withdrawal, and synthetic draft fixtures.
- Snapshot/result types, clock/zone/date selection, repository interfaces, and synthetic tests.
- Local-source-of-truth and invalid-payload rejection using fake providers.
- Resolver contracts that accept only published content and explicit snapshot state.
- Fresh/stale/unavailable/setup states.
- Accessible linear act-now shell using clearly non-production demo content.
- Date/content-version-scoped completion and rollover tests.

Not safe to ship yet: authoritative recommendations, “best” ranking, fiqh labels, Arabic/BM
translations, Quran excerpts, live schedules described as official API data, Malaysian Hijri
prompts, exact tahajjud boundaries, or precise notifications without their respective agreements
and review.

## Second-wave brief

### Lane 1: publication and ranking authority

Determine the exact review, regulatory, claim-scope, ranking, wording, and licence requirements for
a minimal catalogue. External organizations may not be contacted without user authorization, so
this wave must distinguish what can be proven online from what requires a qualified reviewer or
written KDN determination.

### Lane 2: Malaysian temporal-data contract

Determine supported transport, fields, zone/date semantics, terms, caching rights, corrections,
stale/absence/travel policies, and separate prayer-versus-Hijri conclusions. If no contract exists,
define the exact unavailable/manual/bundled boundary.

### Lane 3: truthful accessible journey

Evaluate neutral state models for linear act-now versus wheel-first entry, primary plus alternatives,
fresh/stale/unavailable disclosure, evidence access, persistence, and non-drag operation. With no
participants supplied, distinguish evidence-backed Android requirements from hypotheses that still
need BM-first usability sessions including TalkBack.

The eventual research lock is conditional: live recommendations require publishable inputs from
the first two lanes; otherwise implementation must remain non-claiming infrastructure and failure
states.
