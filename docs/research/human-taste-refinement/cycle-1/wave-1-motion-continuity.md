# Wave 1: mobile motion and spatial continuity

Research used live Firecrawl searches against official Android, Material, Apple, and W3C sources.
Raw results are under `.firecrawl/`. A final typography/clipping query failed with HTTP 402, so
direct platform guidance on partial-glyph clipping remains an explicit gap.

## 1. Motion should explain persistent identity

- **Source:** [The motion system — Material Design](https://m2.material.io/design/motion/the-motion-system.html)
- **Type:** Official design guidance; Material 2, now deprecated.
- **Sourced fact:** Material recommends container transform when a persistent container connects
  source and destination. Position, dimensions, and shape animate together, reinforcing that detail
  is an expansion of the same object.
- **Transfer inference:** The prayer instrument should remain recognizably the same instrument as
  it enters detail. Avoid an unrelated detail screen or cosmetic crossfade.
- **Testable heuristic:** At 25/50/75%, an observer can identify the same selected minute, pointer
  relationship, and circular geometry without endpoint context.
- **Misuse risk:** Treating every child—including clock glyphs—as flattened container pixels can
  produce scaling distortion and clipping. Persist conceptual identity, not one bitmap.

## 2. Sequence incompatible text; do not let it overlap

- **Source:** [The motion system — Material Design](https://m2.material.io/design/motion/the-motion-system.html)
- **Type:** Official design guidance; Material 2, now deprecated.
- **Sourced fact:** The fade-through variant fully removes outgoing content before incoming content
  appears, avoiding overlapping, partially transparent frames.
- **Transfer inference:** If the clock changes typographic role, independently laid-out clocks
  must never overlap or crossfade while misregistered. Prefer one overlay-rendered clock; if two
  presentations are unavoidable, sequence their handoff inside a safe region.
- **Testable heuristic:** Frame sampling finds zero doubled digits, mixed baselines, or partially
  clipped numerals.
- **Misuse risk:** A long fade-through gap can remove essential time information. Keep any handoff
  brief and preserve accessibility semantics continuously.

## 3. Transform the bounding instrument; manage children deliberately

- **Source:** [Motion.md — Material Components for Android](https://github.com/material-components/material-components-android/blob/master/docs/theming/Motion.md)
- **Type/date:** Maintained official implementation documentation; retrieved 2026-07-12.
- **Sourced fact:** `MaterialContainerTransform` treats start and end bounding containers as the
  shared element; their contents are swapped. Enter and return are supported, and companion
  transitions must use matched durations.
- **Transfer inference:** Animate instrument/aperture bounds from one progress model, but assign
  clock, labels, pointer, and semantic ownership explicitly. Container clipping does not guarantee
  acceptable child behavior.
- **Testable heuristic:** Every animated property derives from one normalized progress source;
  reverse at the same progress reproduces the same geometry and visibility.
- **Misuse risk:** Independently timed child animation drifts during Back or interruption.

## 4. Prefer interruption-safe, velocity-continuous motion

- **Source:** [Customize animations — Android Developers](https://developer.android.com/develop/ui/compose/animation/customize)
- **Type/date:** Official Compose documentation; updated 2026-06-18.
- **Sourced fact:** Compose documents that spring specs handle target changes smoothly because
  velocity remains continuous. It recommends springs for user-driven points and notes the default
  damping is non-bouncy.
- **Transfer inference:** Expansion/collapse and post-gesture settling should retarget from current
  position and velocity rather than restart from a synthetic endpoint.
- **Testable heuristic:** Trigger Back/opposite commands at 10%, 35%, 60%, and 90%; position remains
  continuous and first post-retarget velocity does not jump unnaturally.
- **Misuse risk:** Spring does not authorize bounce. Overshoot is inappropriate here and may drive
  text across clip boundaries.

## 5. A transition must tolerate a new transition at any time

- **Source:** [Enhance your UI animations and transitions — Apple, WWDC24](https://developer.apple.com/videos/play/wwdc2024/10145/)
- **Timestamps:** 1:35–2:03 continuity; 5:51–7:49 mid-transition reversal/state guidance;
  11:20–13:24 gesture retargeting.
- **Sourced fact:** Apple's zoom transition keeps corresponding UI visible and can be grabbed from
  the beginning or during transition. Apple advises readiness for a new transition at any time and
  minimizing temporary transition state.
- **Transfer inference:** Clock/header ownership, clipping, and semantics must be pure functions of
  progress and direction, not one-shot started/finished flags.
- **Testable heuristic:** Interrupt every sampled frame; the next frame has a valid clock/header
  without duplicate semantics, missing focus, or stale clip ownership.
- **Misuse risk:** Copying the large zoom effect literally would add spectacle. Adopt the state
  discipline, not its visual scale.

## 6. Calm springs can preserve velocity without visible bounce

- **Source:** [Animate with springs — Apple, WWDC23](https://developer.apple.com/videos/play/wwdc2023/10158/)
- **Timestamps:** 2:12–2:32 position/velocity continuity; 14:53–15:10 retargeting;
  20:50–22:13 bounce and interface character.
- **Sourced fact:** Apple describes smooth interruption as retaining current velocity. It states
  that zero bounce gives a smooth gradual change and recommends zero bounce when uncertain; spring
  character should match whether an interface is serious, relaxed, or playful.
- **Transfer inference:** bMuslim should use critically/strongly damped motion: continuous without
  recoil, elastic bezel motion, or celebratory settling.
- **Testable heuristic:** Instrument, aperture, and header never cross destination bounds during
  settle or reversal.
- **Misuse risk:** Natural physics can still become playful. Velocity continuity and bounce are
  separate decisions.

## 7. Reduced motion should preserve hierarchy comprehension

- **Source:** [Reduced Motion evaluation criteria — Apple](https://developer.apple.com/help/app-store-connect/manage-app-accessibility/reduced-motion-evaluation-criteria/)
- **Type/date:** Official accessibility criteria; retrieved 2026-07-12.
- **Sourced fact:** Scaling, spinning, depth simulation, multi-axis/multi-speed motion, and
  peripheral motion may be triggers. Removing meaningful motion can reduce understanding; suggested
  alternatives include dissolve, highlight fade, or color shift.
- **Transfer inference:** Reduced motion should replace wheel travel/scale with a short state change
  that retains selected minute, then reveal hierarchy through opacity, emphasis, and clip state.
- **Testable heuristic:** With motion removed, detail is understandable in at most two stable frames
  with no spinning, large translation, scale travel, or lost selected context.
- **Misuse risk:** A raw zero-duration snap may expose an invalid intermediate layout or erase the
  relationship between instrument and detail.

## 8. Interaction-triggered nonessential motion must be suppressible

- **Source:** [Understanding SC 2.3.3: Animation from Interactions — W3C](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html)
- **Type:** WCAG 2.2 understanding document.
- **Sourced fact:** Interaction-triggered motion animation should be disableable unless essential
  to functionality or information. W3C distinguishes essential movement from extra movement and
  documents vestibular risks.
- **Transfer inference:** Direct finger-bound scrubbing may remain; decorative orbit drift,
  parallax, overshoot, and ambient celestial movement should stop under reduced motion.
- **Testable heuristic:** Classify every moving property essential/nonessential. Under reduced
  motion, no nonessential property changes position continuously.
- **Misuse risk:** Calling ornamental motion informational defeats the requirement.

## Inferred clock/header and aperture state model

This is cross-source inference, not a platform-prescribed design:

- Render the clock as one persistent overlay element outside the aperture clipping layer.
- Clip only instrument graphics safe to crop: rings, rails, arcs, celestial marks, and background.
- The clock initially follows the instrument, then relocates to a reserved header anchor while
  staying above the clip. It never passes behind the aperture edge.
- Keep numeral glyph shape/aspect stable; animate position and only modest uniform size if needed.
- Accessibility exposes one stable time node even if the visual clock relocates.

| State | Clock/header behavior | Aperture/instrument behavior |
| --- | --- | --- |
| Early, 0–25% | Fully readable at source anchor above future clip; destination reserved but not separately semantic | Instrument begins moving; clip does not touch clock bounds |
| Middle, 25–60% | Same overlay clock moves to header with valid baseline and contrast | Crop becomes evident; decorative children may fade, selection remains coherent |
| Late, 60–100% | Reaches stable header before deep crop; no second clock | Aperture settles; only crop-safe graphics cross boundary |
| Interrupted | Current position/velocity become next initial conditions; no duplicate clock | Geometry and visibility recompute from current progress |
| Reverse | Same progress mapping backward; clock rejoins only after clearing boundary | Instrument expands continuously; selected minute/pointer stay fixed |
| Reduced motion | Immediate safe-anchor placement or restrained dissolve/emphasis; stable semantics | Two comprehensible layouts with minimal opacity/clip-state transition |

**Hypothesis to validate:** a single overlay clock will feel more coherent than fading between dial
and header clocks while eliminating half-glyph clipping. Test against a brief fade-through option.

## Contradictions and gaps

- Container transformation encourages content transformation/swapping, but literal typography
  transformation conflicts with stable, unwarped glyphs.
- Springs improve interruption continuity, yet visible spring character conflicts with the quiet
  devotional tone. Strong damping/zero bounce must be judged in capture.
- Meaningful hierarchy should survive reduced motion, while nonessential interaction motion must
  be disableable. Which circular relocation is essential remains unresolved.
- No direct authoritative source was recovered on partial glyph clipping during animated apertures;
  the overlay-layer rule is an inference requiring visual and implementation tests.
- Platform sources do not specify screen-reader focus migration when one semantic object changes
  visual region.
- 360×640 and 1.3× font scale may require a different path or earlier header handoff.

## Wave-2 recommendations

1. Find evidence on shared text layers, mask boundaries, baseline stability, and avoiding
   rasterized/scaled glyphs during shared-element transitions.
2. Research maintained Compose examples/tests for one semantic node moving between layers,
   including interruption, Back, and reduced motion.
3. Find native Android examples where a spatial transition becomes a two-state dissolve/highlight
   without losing hierarchy under reduced motion.
