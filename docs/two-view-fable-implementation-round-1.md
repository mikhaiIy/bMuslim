# Fable-5 high: two-view implementation judgment, round 1

## Verdict

**68/100 — fail, with one P0 and three bounded P1 corrections.**

Fable accepts the core architecture:

- cold-launch glance and explicit open action;
- one `Animatable`, one Canvas, one detail tree;
- real aperture clipping instead of paper overdraw;
- no expansion rotation;
- conditional composition removes hidden semantic trees;
- close/back, model preservation, protected identity, and three-tab navigation.

## Scores

- User story: 8/10
- Trigger/scrub separation: 9/10
- One instrument: 8/10
- Geometry/zero rotation: 7/10
- Reveal/no overlap: 8/10
- Prayer/deed comprehension: 7/10
- Identity/craft: 7/10
- Accessibility/back/reduced motion: 3/10
- Compact 360x640: 5/10
- Code/tests/verification: 6/10

## P0: reduced-motion focus timing

Fable reports that the animator-disabled path snaps expansion and immediately calls
`FocusRequester.requestFocus()` before the destination target can compose/attach. It flags the
same risk on collapse for the reappearing open action. Suggested correction: destination-driven
focus effect after composition/frame attachment, or globally-positioned gating. Acceptance:
animator-scale-zero open/back causes no exception and focus lands on summary/open action.

## P1: final source verification

The final `TimelineScreen.kt` timestamp was newer than the APK/test/lint artifacts inspected by
Fable. Re-run tests, assemble, lint, and diff-check after all corrections.

## P1: compact header budget

Fable calculates that the fixed header height derived from wheel top cannot fit status inset plus
brand/date/time/day-part at 360x640 and 1.3x font scale. It recommends naturally measuring the
header and computing glance geometry from remaining space, or a compact/font-scale-aware header
layout. Add a conservative pure budget test and a compact preview/capture if available.

## P1: weak/disconnected tests

- `expansionAngularOffset` is tested but not used by renderer.
- `HomeSceneState` preserves minute in a test double rather than the UI path.
- no-overlap assertion is close to a definitional identity.
- crop test uses `.25f..35f`, apparently intending `.25f..35f` only if 35f was deliberate; Fable
  interpreted it as a likely `.35f` typo and requests a meaningful bounded ratio assertion.

Correction: connect angular helper to renderer or test the actual angle function; make crop tests
fail under mutation; test monotonic crop/content-space properties; remove unused doubles/dead code.

## P2

- Regular detail currently uses compact strip and leaves richer summary/index code dead.
- Open action hard-pops instead of returning over the collapse segment.
- Raw pixel guards and density-dependent scrub sensitivity remain.
- Celestial glow clearance may be tighter than ring-only tests claim.
- Header font-size interpolation may shimmer; scale transform would be calmer.
- Glance lacks a quiet return-to-now affordance after scrubbing.

## Conditional evidence

No emulator/AVD capture exists for glance, mid-transition, collapse, or 360x640 at 1.3x. Fable
does not count missing screenshots as defects, but notes that compact evidence would resolve the
header-budget risk.
