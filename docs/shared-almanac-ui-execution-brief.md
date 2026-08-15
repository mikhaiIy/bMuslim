# Shared almanac UI — execution brief

## Outcome

Production and demo use one shared visual/interaction shell extracted from the current demo:
atmospheric header, concentric 24-hour wheel, scrub, fixed pointer, glance-to-folio transition,
reverse/Back behavior, compact/maximum-font composition, close action and navigation.

Flavor code owns all claims. Demo injects synthetic deeds and disclosure. Production injects the
validated eight-point JAKIM schedule and an official schedule folio. No parallel production wheel
remains.

## Production folio

Because production has no approved deeds/ranking/citations, the same paper folio and reveal
choreography contains only selected/next official timing, all eight JAKIM entries, their 5/3
category distinction, validated civil date/zone/source, and `Panduan amalan belum tersedia`.
The glance CTA is `Lihat takwim pada HH:mm`. Never show `aktif`, invented windows/end times,
rakaat, rulings, citations, grades, completion, recommendations or tomorrow wrap.

## Shared boundary

Create truth-neutral models and UI under `src/main/.../ui/almanac/`:

- neutral header/event/action models and selection helpers;
- truth-neutral scene geometry/handoff/font/scroll/collision policies;
- the actual parameterized demo timeline shell;
- generic primary/secondary/context-window wheel renderer;
- shared paper folio frame with a flavor-owned detail content slot.

The shared shell must not import `TimelineDeed`, `Ruling`, `DeedType`, `DayState` or JAKIM provider
classes. Flavor mappers translate source data into neutral visual events.

The neutral contract is explicit:

- a point event has `id`, `title`, `minute`, visual role and flavor-owned accessibility text;
- an optional interval is a separate demo-only visual input and is never inferred from a point;
- readout/summary/CTA/close strings and next-selection policy are injected, not derived from a
  religious type in shared code;
- actual-now minute, quantizer and celestial bounds are injected. Demo retains five-minute fixture
  precision; production uses one-minute precision and Syuruk/Maghrib celestial bounds;
- production emits exactly eight points, no end/act/active property, and non-circular previous/next
  selection. After Isyak there is no tomorrow wrap.

## Demo ownership

Retain `TimelineModel.kt`, fixtures, deed ranking, religious copy and completed IDs in `src/demo`.
Add a demo mapper and demo folio body. Reduce the old `TimelineScreen` to a demo composition wrapper
around the shared shell. Preserve disclosure and existing demo behavior/tests.

## Production ownership

Add a JAKIM-to-neutral mapper and official schedule folio. Ready invokes the shared shell. Delete
the parallel `LiveAlmanacScreen`/`AlmanacInstrumentModel` only after their live-clock, no-wrap,
category, collision and source behavior has migrated.

## Protected invariants

- Same pine/saffron/paper identity, serif/monospace hierarchy, sky, concentric wheel, fixed pointer,
  actual-now mark and scrub.
- Same constant-radius aperture motion; 620ms open, 460ms close; reduced-motion snap equivalent.
- Reveal order wheel → summary → ledger/body → navigation.
- Only settled destination owns semantics; Back closes and restores focus.
- Minimum 48dp actions; one outer scroll at maximum font; compact 360×640 remains usable.
- Production now follows Malaysia time until scrubbed; return-to-now resumes. Zone/date model-key
  replacement resets to live now and glance. Demo remains deterministic.
- Loading/failure never renders old values. First run retains explicit zone selection.

The shared shell owns a reducer/state keyed by the flavor-provided `modelKey`. On a production
zone/date key change it cancels motion and drag, clears scrub ownership, samples Malaysia now,
snaps to Glance, clears pending focus and never announces/focuses a disposed action. Back during
opening/closing targets Glance and restores CTA focus only if the same key still owns the shell.
Return-to-now clears scrub and immediately resumes ticks. Demo uses one stable deterministic key.

Production folio wording is point-based: `Entri terdahulu` and `Entri berikutnya`; before Imsak has
no previous entry, after Isyak has no next entry and shows `Jadual hari ini selesai`. Never call an
entry active/current. The shared frame owns focus, Back, close/reveal choreography and scroll
composition; the first flavor folio heading is the focus target. Normal/compact uses one weighted
internal schedule scroller. Maximum font uses one outer `LazyColumn` with no nested scroll. CTA,
close semantics/copy, source and zone actions are injected; production uses neutral takwim copy.

## Forbidden in shared/production

Demo fixtures/types, fixed civil/Hijri date, synthetic windows, deed ranking, “amalan utama”,
religious detail summaries, citations, Arabic/translations, completion controls and demo semantics.

## Acceptance

- Shared geometry/interaction tests preserve existing demo invariants.
- Generic wheel tests cover roles, eight points, collision, accessibility ordering and no wrap.
- Production mapper proves canonical eight events, 5/3 split and no windows/guidance fields.
- Production folio-copy deny tests cover religious/demo claims.
- Existing demo behavior/model/disclosure tests stay green.
- Live-follow/scrub/reset/model-key state tests pass.
- Artifact rules prove shared shell exists in both APKs and demo types/fixtures remain absent from
  production; only unavoidable raw-schema exemptions remain.
- The canonical shared identity is `com.bmuslim.app.ui.almanac.SharedAlmanacShell`. Move pure
  geometry/handoff tests to `src/test`; add Compose interaction coverage where the current test
  stack permits for open/reverse/Back/settled semantics/focus, compact 360×640 and maximum-font
  reachability. Structural tests prove both flavor mappers target the same contract.
- Artifact verification must positively require `SharedAlmanacShell` in demo and production,
  require obsolete `LiveAlmanacScreen`/`AlmanacInstrumentModel` absent, and continue denying
  `TimelineDeed`, fixed demo date/Hijri copy, fixtures and demo folio bodies in production.
- Run both unit suites, all enabled assemblies/lints, both artifact controls and `git diff --check`.
