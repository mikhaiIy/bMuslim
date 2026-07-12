# Session handoff: core-value foundation

## Goal

Use the repository's gated research, design, execution, and independent-judgment workflow to
identify and implement the highest-impact improvement to bMuslim before further visual polish.
Prioritize product truth, religious-content trust, correctness, reliability, accessibility, and
the smallest useful end-to-end journey. UX is in scope when it protects or unlocks core value;
beautification is deferred.

## Starting point

- Baseline commit: `df386c2` (`Refine timeline interaction geometry`).
- Working branch: `feature/core-value-foundation`.
- The existing human-taste refinement is complete and must not be reopened.
- Existing unrelated local files and changes listed by `git status` must remain untouched.

## Product identity to protect

- A contemporary Malaysian prayer almanac: quiet, precise, warm, and devotional.
- The core promise is a trustworthy answer to “what good deed is best for right now?”
- Religious suggestions must remain traceable to reviewed sources and must never be generated at
  runtime.
- The continuous prayer-time instrument and its existing accessibility geometry are protected
  unless the new product slice requires a narrowly justified UX change.

## Observed implementation state

The current repository is a polished native Compose prototype, not yet an end-to-end v1:

- `TimelineScreen` owns the experience directly; no ViewModel or repository layer exists.
- `NowMinute`, the displayed Gregorian/Hijri date, prayer times, deeds, citations, and day state are
  hard-coded sample data.
- No zone onboarding, live prayer-time source, cache/offline fallback, completion persistence,
  daily reset, settings, notifications, bundled reviewed catalogue, or BM/English resource system
  exists yet.
- Current deterministic geometry and timeline-model tests pass.

These are observations, not the priority decision. Live research and independent judgment must
rank the first vertical slice.

## Baseline verification

On 2026-07-12:

```text
./gradlew :app:testDebugUnitTest :app:assembleDebug --no-parallel
BUILD SUCCESSFUL
```

## Active workflow

Research wave one has three independent lanes:

1. religious-content trust and authenticity governance;
2. Malaysian prayer-time/time-zone/offline scheduling reliability;
3. core-value and non-visual UX, including accessibility and cognitive load.

Research must produce cited, testable principles. It does not authorize app-code edits. A separate
synthesis, second wave, independent research judgment, and orchestrator research lock are required
before design. Only a dedicated executor may edit app implementation after design is locked.

## Completed state (2026-07-12)

- Research passed at 90/100; orchestrator meta-rating 38/40.
- Contract-led design passed at 100/100; orchestrator meta-rating 40/40.
- Native implementation passed final independent review at 100/100 with no P0/P1/P2; final
  orchestrator meta-rating 40/40.
- The app now has physically isolated demo and production flavors. `demoDebug` preserves the
  existing timeline with visible/semantic sample disclosure; `demoRelease` is disabled;
  production debug/release contain no demo fixture content and resolve to a production-safe
  NotReady shell.
- Shared truth contracts enforce content publication, exact temporal key/permission, and reviewed
  ranking before any ready recommendation can exist.
- Final independent verification: 72 tests, all requested assemblies and lints, exhaustive demo
  positive-control scan, both production negative scans, and `git diff --check` pass.
- APKs:
  - `app/build/outputs/apk/demo/debug/app-demo-debug.apk`
  - `app/build/outputs/apk/production/debug/app-production-debug.apk`
  - `app/build/outputs/apk/production/release/app-production-release-unsigned.apk`

External release gates remain: qualified Malaysian content/ranking review, content rights, a
supported prayer-data contract with caching/reuse terms, Malaysian Hijri authority, KDN Quran
scope, and BM-first assistive-technology device validation.
