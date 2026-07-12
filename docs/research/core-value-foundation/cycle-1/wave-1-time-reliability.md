# Wave 1: prayer-time and Android reliability

## Verdict

Implement a pure **`PrayerDaySnapshot` + `PrayerDaySelector` truth seam** before networking,
notifications, or UI polish.

Model `{jakimZone, civilDate, source, fetchedAt, date-bearing prayer instants}` and select against
an injected `Clock` and explicit zone. Output `Fresh`, `Stale(snapshot, age)`, or `Unavailable`.
This replaces fabricated sample truth, prevents downstream ambiguity, and is small enough for
exhaustive JVM tests.

Firecrawl returned HTTP 402; the scout used current official primary-source pages through the
workflow's fallback. No project files were edited by the scout.

## Evidence and testable heuristics

### 1. Use date-bearing time and an injectable clock

- Sources: Java `Clock` and `ZonedDateTime` documentation.
- URLs:
  - https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/time/Clock.html
  - https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/ZonedDateTime.html
- Heuristic: inject `Clock`; bind prayer instants to date and zone. Do not persist a prayer as bare
  `LocalTime` or infer the device default later.
- Tests: fixed clocks, midnight rollover, next-day Fajr, wall-clock jumps, and explicit zone
  changes select the intended snapshot.

### 2. Treat official e-Solat/JAKIM output as authority, not a community wrapper

- Source: official e-Solat WLY01 rendering.
- URL: https://www.e-solat.gov.my/portalassets/www2/solatB.php?kod=WLY01&lang=BM&url=https%3A%2F%2Fwww.islam.gov.my
- Heuristic: persist the requested JAKIM zone, civil date, raw-source provenance, and completeness.
  A wrapper may transport/cache data but must not be described as an official JAKIM API.
- Risk: a successful HTTP response does not prove the zone, date, fields, or source are correct.
  No supported official public API contract was found in this wave.

### 3. Local data is the only read source

- Source: Android Developers, “Build an offline-first app.”
- URL: https://developer.android.com/topic/architecture/data-layer/offline-first
- Heuristic: network refresh validates and persists snapshots; the application reads local state.
  For requested `{zone,date}`:
  - `Fresh`: complete matching snapshot within the accepted provenance age;
  - `Stale`: complete matching snapshot outside it;
  - `Unavailable`: absent, wrong-date, wrong-zone, or incomplete.
- Risk: the PRD's “cached about 24h” is insufficient. Yesterday's schedule must never become today's
  simply because it was fetched less than 24 hours ago.

### 4. Chosen schedule zone and device zone are different concepts

- Source: Java `ZoneId` documentation.
- URL: https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/time/ZoneId.html
- Heuristic: derive the schedule date in an explicit chosen Malaysian zone; changing that selected
  zone invalidates/reselects cache atomically.
- Open product decision: a chosen JAKIM zone should likely remain authoritative during travel until
  the user explicitly changes it.

### 5. Recompute and reschedule after temporal discontinuities

- Sources: Android `SystemClock` and broadcast exceptions guidance.
- URLs:
  - https://developer.android.com/reference/android/os/SystemClock
  - https://developer.android.com/develop/background-work/background-tasks/broadcasts/broadcast-exceptions
- Heuristic: compute night across `Maghrib(date) → Fajr(date+1)` using date-bearing values. Refresh
  UI state on resume and handle wall-clock/date/zone changes and reboot for future scheduling.
- Tests: manual clock ±2h, zone change, reboot, and midnight in foreground/background produce no
  duplicate or yesterday-selected recommendation.

### 6. WorkManager is refresh plumbing, not temporal truth

- Source: Android `PeriodicWorkRequest` documentation.
- URL: https://developer.android.com/reference/androidx/work/PeriodicWorkRequest
- Evidence: periodic work is inexact, may be delayed by Doze, and has a 15-minute minimum.
- Heuristic: use unique, network-constrained work to refresh/prefetch; Home still reads local
  Fresh/Stale/Unavailable immediately.
- Risk: never assume a daily worker ran at midnight.

### 7. Defer exact alarms until truth and rollover rules are stable

- Sources: Android exact-alarm changes and alarm guidance.
- URLs:
  - https://developer.android.com/about/versions/14/changes/schedule-exact-alarms
  - https://developer.android.com/develop/background-work/services/alarms
- Evidence: Android 14+ denies `SCHEDULE_EXACT_ALARM` by default for many fresh installs; access must
  be checked, requested in context, and handled after grant/revocation.
- Heuristic: permission denial produces an explicit inexact fallback or notifications-off state,
  never a false precision claim.
- Risk: exact alarms have policy and battery costs; WorkManager is not a prayer-time firing tool.

### 8. Acceptance gate for the first slice

Table-driven JVM tests cover `(Clock, chosen zone, requested date, rows, fetchedAt)` for fresh,
stale, absent, incomplete, wrong-zone, wrong-date, midnight, next-day Fajr, clock jump, and zone
change. Repository-contract tests prove network failure emits cached stale state and an invalid
successful payload never replaces the last valid snapshot.

## Next-wave questions

1. Does JAKIM/e-Solat offer a supported machine-readable or date-range endpoint and redistribution
   permission, or must bMuslim use a licensed transport plus official spot checks?
2. On travel/device-zone change, should the selected Malaysian JAKIM zone remain authoritative until
   the user changes it?
3. What stale horizon is acceptable: same-date provenance indefinitely, hide after a limit, or
   bundle prevalidated future schedules?
