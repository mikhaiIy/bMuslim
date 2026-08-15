# JAKIM prayer-times execution brief

## Goal

Replace the production temporal-provider placeholder with a small, truthful vertical slice that
lets a user choose a JAKIM prayer zone, fetches that zone's current schedule directly from JAKIM,
strictly validates it, and displays the five obligatory prayer times. Keep recommendations blocked
until separate reviewed content and ranking inputs exist.

## Locked source contract

- Request `GET https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone={ZONE}`.
- Provider ID: `jakim-e-solat`; contract version: a repository-owned adapter version such as
  `takwimsolat-today-v1`; provenance source reference must be the exact request URL.
- Treat the public endpoint as permitted for live use for this user-directed integration and link
  attribution/terms to the official e-Solat portal. Formal persistence permission has not been
  established: prayer responses remain memory-only. Persisting a user's selected zone is allowed
  because it is user preference, not copied timetable data.
- Never fall back to the third-party `api.waktusolat.app`, calculated times, or demo fixtures.

## Architecture

Keep new provider code in `src/main` so it is testable without religious fixture content, with
Android orchestration/UI in `src/production`.

1. A pure JAKIM response model/parser accepts only the documented response fields needed by the
   app. Use a maintained JSON parser rather than regex parsing.
2. A pure adapter takes requested `PrayerDayKey` plus the response and returns a validated
   `PrayerDaySnapshot` or a typed failure.
3. A small HTTP data source owns URL construction, timeouts, HTTP status handling, and body size.
4. A repository fetches one requested zone/day, maps it, and returns a typed result. No timetable
   disk cache in this slice.
5. A production ViewModel/state holder owns selected zone, loading, ready, and error/retry state.
6. The Compose production screen provides zone selection, source attribution, retry, and the five
   prayer times. It may display prayer times independently, while the existing `TruthResolution`
   continues to report content/ranking blockers and must not produce a recommendation.

## Validation contract

Reject the response unless all conditions hold:

- requested zone matches `^[A-Z]{3}[0-9]{2}$` and is in the bundled current JAKIM zone registry;
- HTTP status is successful and response body is bounded;
- top-level `status` is exactly `OK!`, top-level `zone` equals the requested zone, and exactly one
  record represents the requested civil date in `Asia/Kuala_Lumpur`;
- `date` parses strictly as case-insensitive `dd-MMM-uuuu` with English month names;
- `fajr`, `dhuhr`, `asr`, `maghrib`, and `isha` parse strictly as local times;
- all five canonical slots are present once, date-bearing, and strictly chronological;
- provenance and source contract identifiers agree.

Do not use the endpoint's server date as the app's requested date and do not show a stale response.
Ignore Imsak, Syuruk, Dhuha, Hijri date, bearing, and server time in this slice.

## UI and state

- Do not silently assume WLY01. Start unconfigured until the user chooses a zone.
- Use a compact native selector with the complete current official zone code/name registry.
- On selection, fetch today's schedule using `Asia/Kuala_Lumpur`; show progress without old values.
- Ready state shows BM-first date/zone/source context and Subuh, Zohor, Asar, Maghrib, Isyak.
- Failure state distinguishes no connection, provider rejection, and invalid/stale data in concise
  BM-first copy and offers retry/change-zone actions.
- Preserve maximum-font-scale scrolling, semantic heading, content descriptions, and minimum touch
  targets. Do not introduce demo content into production.
- Keep the existing recommendation shell honest: schedule availability must not imply verified
  deed guidance is ready.

## Tests and evidence

- Pure adapter/parser tests: happy path, malformed JSON, non-OK status, wrong zone, stale/wrong date,
  missing/duplicate schedule, malformed time, and reversed chronology.
- Repository/data-source tests should cover URL encoding and representative HTTP/network failures
  without calling the live endpoint during unit tests.
- Production state tests: initially unconfigured, zone selection loads, success renders ready data,
  stale data fails, retry works, and recommendations remain blocked by missing content/ranking.
- Preserve all existing demo and truth-domain tests.
- Run demo/production unit tests, production debug/release assembly, all three variant lints,
  `scripts/verify-production-artifacts.sh`, and `git diff --check`.

## Forbidden shortcuts

- No default/sample prayer schedule, regex JSON parsing, silent stale cache, device-local prayer
  calculation, demo import, recommendation unlock, broad timeline redesign, notification work, or
  Hijri/content expansion.
