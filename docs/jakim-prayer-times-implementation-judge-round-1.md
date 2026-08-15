# JAKIM prayer-times implementation judgment — round 1

## Decision

**52/100 — ITERATE**

## P0

1. **Current source does not compile.** Fresh demo/production unit-test compilation fails at
   `JakimPrayerTimes.kt:59` because `parse` uses a non-local `return` from an expression body.
   Existing APK and lint reports predate the source. Convert the parser to a block body and rerun
   the complete verification matrix against current sources.

## P1

1. **Ready data can cross midnight while still labeled as today's schedule.** Add a lifecycle-aware
   refresh-if-day-changed path that clears old Ready values before fetching the new Malaysia date;
   prove it with a mutable-clock rollover test.
2. **Duplicate required JSON members are not rejected.** Gson tree parsing overwrites duplicate
   object members. Use strict maintained parsing with duplicate-member tracking and test duplicates
   of every required canonical time field.
3. **Redirect provenance is unsafe.** `HttpURLConnection` follows redirects by default while the
   snapshot attributes the original JAKIM URL. Disable redirects and reject 3xx, with a transport
   acceptance test.
4. **Maximum-font-scale actions have no safe layout.** Fixed action rows may overflow on compact
   screens. Stack/adapt actions so every control remains visible, scroll-reachable, and at least
   48dp high.

## P2

1. Exercise the real HTTP data-source behavior deterministically: redirects/status, declared and
   streamed body limits, disconnect, timeout, and IO failure.
2. Derive or validate the adapter provenance URL rather than accepting an arbitrary reference.

## Missing evidence

- Current green test/build/lint/APK/artifact evidence.
- Compact maximum-font-scale device or Compose evidence.
- Machine-checkable comparison of the 60 bundled zones against a dated official registry snapshot.

## Confirmed strengths

The implementation uses the locked direct HTTPS endpoint, validates exact date/zone/status/time
chronology, has no default zone, persists only the user's zone, keeps timetable data memory-only,
provides BM-first attribution/errors, retains content/ranking blockers, and contains no calculation
or third-party fallback.
