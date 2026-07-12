# Cycle 1 baseline product evidence

This is the local product evidence given to the research synthesis and judging stages. It records
what the code proves, what the user's report establishes, and what still needs runtime capture.

## Environment

- Branch: `feature/human-taste-refinement`
- Baseline tag: `v1` at `1d4cb5d`
- Planning commit: `31929b7`
- No `adb` executable, connected device, or configured AVD was found at the start of research.
- Existing Compose previews and generated design references are available, but they are not proof
  of the running animation.

## Reported defects

1. The saffron current-deed label can become blocked or unreadable while scrubbing the wheel.
2. The selected-time clock at the top appears cut in half as the wheel moves into detail.

Both reports are treated as real user-observed failures. Code inspection below explains plausible
mechanisms; it does not replace a later device capture.

## Deed-label evidence

Relevant production code: `TimelineScreen.kt:465–577`.

- The renderer calculates a new active contextual deed for every selected minute.
- The active label is always centered at a fixed point `center.x + 57dp`, roughly level with the
  selected-pointer crossing (`TimelineScreen.kt:545–556`).
- The connector and label do not use measured text bounds, a safe rectangle, collision candidates,
  prayer-label bounds, celestial-body bounds, or viewport-edge avoidance.
- `drawLabel` centers raw native Canvas text at the supplied point and has no max width, backing
  surface, ellipsis, alignment flip, or clipping response (`TimelineScreen.kt:647–664`).
- Prayer labels use only the prayer node point—not the resulting text bounds—as their visibility
  guard (`TimelineScreen.kt:519–524`). Hour labels use the same coarse point-guard pattern.
- All labels share one Canvas and can therefore overlap even while their anchor points pass the
  simple guards.
- The current implementation uses `sizeSp.dp.toPx()` for Canvas text, so it does not demonstrate
  correct user font scaling.

### Baseline inference

The defect is not caused by the deed label orbiting freely: the selected label is intentionally
tethered near the fixed pointer. The weak point is that the tethered position is not a guaranteed
text-safe zone. Long deed titles, prayer labels, the celestial marker, the aperture edge, and
compact width can compete with the unmeasured centered string. Scrubbing also swaps active strings
without a layout transition or stable width, so the label can pop between very different extents.

### Missing evidence

- Measured bounds for every active deed title at 360dp and 390dp widths.
- Frame capture while scrubbing across deed boundaries.
- Behavior at 1.0×, 1.3×, and maximum supported font scale.
- Exact collision cases against prayer labels and celestial markers.

## Bisected-clock evidence

Relevant production code: `TimelineScreen.kt:164–348`, `TimelineScreen.kt:352–405`, and
`HomeScene.kt:20–45`.

- The header is assigned exactly `scene.wheelTop` as its height.
- The wheel is composed later in the same parent and begins at `scene.wheelTop`, so it paints over
  header content that extends below this boundary.
- At settled regular detail, `wheelTop` is only 120dp. At compact detail it is 112dp.
- The regular header includes status-bar padding, 10dp vertical padding, and nominal line heights
  of 25 + 12 + 40 + 12dp. Even before accounting for the status bar, that is 99dp; with a typical
  status inset it exceeds the 120dp detail boundary.
- The compact header is shorter because it uses two columns, but it still shares the same moving
  boundary and has no explicit safe-bottom contract.
- Header typography does not interpolate, relocate, crossfade between defined roles, or reserve a
  clock-safe area during progress. Only the wheel geometry changes.

### Baseline inference

The top clock is not intentionally aperture-clipped. The wheel simply overtakes a header whose
content budget can exceed the final `wheelTop`. The likely fix needs explicit ownership: either a
header layout guaranteed to finish above the aperture at every progress value, or a designed
time-label transformation with mutually exclusive, bounds-safe roles. Merely changing z-order
could hide the symptom while creating wheel occlusion or ambiguous hierarchy.

### Missing evidence

- Frame captures around 25%, 50%, and 75% expansion and reverse.
- Actual status-bar inset and font metrics on target devices.
- Interrupted animation and animator-scale-zero behavior.
- Regular and compact results at increased font scale.

## Existing strengths to preserve

- One `selectedMinute` source of truth and one progress-driven scene.
- The wheel does not gain angular rotation during expand/collapse.
- Geometry center and crop are tested as monotonic.
- Detail content is tested to remain below the aperture.
- Scrubbing does not itself expand the interface.
- Hidden/destination focus is delayed until the presentation settles.
- Reduced motion snaps to the same destination and then follows the focus/announcement path.

## Research questions grounded in the code

1. What collision policy best fits a selected label tethered to a fixed radial pointer: safe-zone
   clamping, candidate placement, an external selected-context rail, or a hybrid handoff?
2. How should the policy behave at string changes, active-deed boundaries, and high drag velocity
   without adding restless motion?
3. What header/time transformation preserves continuity while guaranteeing no partial glyph at
   all progress values and font scales?
4. Which elements should remain geometrically continuous, which may fade, and which need stable
   semantic identity despite visual relocation?
5. Which micro-motion rules communicate care and calm rather than ornamental animation?

## Evidence standard for later phases

Research must produce testable bounds, state contracts, or critique heuristics. Image design must
show difficult minutes and transition states, not only flattering endpoints. Implementation must
sample multiple minutes, widths, font scales, and progress values in pure tests where possible,
then seek real-device evidence without misreporting its absence as a pass.
