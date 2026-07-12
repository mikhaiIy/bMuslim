# Wave 1: radial label collision and occlusion

Read-only live research. Raw Firecrawl results are under `.firecrawl/`.

## Current-code diagnosis

### Facts

- `drawLabel()` uses centered Android `Paint` text with no measured bounds, wrapping, truncation,
  backing, or collision testing.
- The active contextual-deed label does not literally rotate. It is repeatedly placed at a fixed
  top-pointer region: X is `center.x + 57dp`; Y is pointer crossing plus 3dp.
- Scrubbing rotates arcs under the fixed pointer; label text and lane-derived Y change when the
  first active deed changes.
- Lane changes move the anchor by 10dp immediately. Drag end snaps to five minutes without a
  label-settle treatment.
- The wheel is clipped to `scene.viewportHeight`; text bounds are not checked against the aperture.
- `sizeSp.dp.toPx()` ignores system font scaling.
- The whole Canvas is one adjustable semantic node; drawn labels are not separate focus nodes.

### Inference

The reported “text rotating into occlusion” is perceptually accurate but geometrically imprecise:
users see the deed orbit rotate while the selected annotation is replaced/repositioned at the
pointer. Occlusion comes from the fixed annotation region, aperture, lane jumps, competing marks,
and changing content—not rotating glyphs.

At 360dp compact detail, `outerRadius ≈ 160.2dp`, `centerY ≈ 169.3dp`, and deed base radius is
about 141dp before lane increments. A lane-2 deed anchors around `169.3 - 161 + 3 ≈ 11dp`; glyph
ascent can cross the clip top. This is a deterministic compact-state failure.

### Hypothesis

The selected-deed annotation needs a stable reserved reading role during manipulation, with a
modest pointer attachment cue. Inheriting every deed lane is likely the wrong invariant.

## 1. Candidate placement and explicit forbidden regions

- **Source:** [Label Transform — Vega](https://vega.github.io/vega/docs/transforms/label/)
- **Type/date:** Official production visualization documentation; available since Vega 5.16.
- **Fact:** Vega tests ordered anchor candidates, avoids base/other marks, respects chart size and
  padding, supports offsets and priority, and emits opacity/alignment/baseline.
- **Transfer:** Model explicit rectangles/forbidden regions: pointer, prayer labels/nodes,
  celestial body, bezel/aperture, and viewport safe inset.
- **Testable heuristic:** For every selected minute/font scale, chosen bounds remain inside the
  safe rect and avoid all P0 regions; selected deed is placed before passive labels.
- **Misuse risk:** Frame-by-frame unconstrained solving can jitter among equivalent anchors.

## 2. Temporal coherence matters as much as collision freedom

- **Source:** Thierry Stein and Xavier Décoret,
  [Dynamic Label Placement for Improved Interactive Exploration](https://inria.hal.science/inria-00269370/file/dl-npar08.pdf)
- **Type/date:** Peer-reviewed NPAR paper, 2008.
- **Fact:** Labels/leaders use hard nonintersection constraints; short/direct association, low
  obstruction, and coherent movement are optimization goals. The method adds temporal penalties,
  bounded velocity, interpolation, and gradual blending near visibility loss.
- **Transfer:** Placement must preserve continuity through scrub and interrupted transitions.
- **Testable heuristic:** A one-step minute change cannot switch candidate regions unless the
  current candidate exceeds a hard constraint plus hysteresis. Unavoidable relocation uses a
  bounded handoff that preserves attachment.
- **Misuse risk:** Generic optimization movement may feel floaty and distracting.

## 3. Prioritize persistence over frame-optimal placement

- **Source:** Sebastian Ekman,
  [Force-Based Label Placement for Dynamic Data](https://publications.lib.chalmers.se/records/fulltext/256043/256043.pdf)
- **Type/date:** Computer science master's thesis, 2018.
- **Fact:** The method combines label/feature collisions, pulling force, friction, prediction,
  hiding, and priority. Minor overlap may be tolerated during motion, prolonged overlap should not.
- **Transfer:** The active deed outranks passive hour/prayer annotations during manipulation.
- **Testable heuristic:** The selected label stays visible across scrub frames; any passive overlap
  has a small time budget; displacement/velocity are bounded.
- **Misuse risk:** Physics can lag or overshoot; deterministic candidates/hysteresis are safer.

## 4. A finite deterministic candidate set is enough

- **Source:** Kevin Mote,
  [Fast Point-Feature Label Placement for Dynamic Visualizations](https://arxiv.org/pdf/1209.5765)
- **Type/date:** Visualization research paper, arXiv 2012.
- **Fact:** The method evaluates finite candidate positions and costs them by priority; it supports
  fixed, sliding, and leader-line candidates. Lower-priority labels may be obscured first.
- **Transfer:** Use a small set: stable rail, mirrored side, abbreviated fallback, and passive-label
  suppression rather than arbitrary free placement.
- **Testable heuristic:** Identical geometry always gives identical output; selected deed never
  loses to an hour label.
- **Misuse risk:** Maximizing visible count can make the instrument busy; calm hierarchy prunes.

## 5. A reserved circular boundary rail is plausible but unproven here

- **Source:** Bonerath et al.,
  [Boundary Labeling in a Circular Orbit](https://drops.dagstuhl.de/storage/00lipics/lipics-vol320-gd2024/LIPIcs.GD.2024.22/LIPIcs.GD.2024.22.pdf)
- **Type/date:** Peer-reviewed GD 2024 paper.
- **Fact:** Circular boundary labeling places non-overlapping labels in a reserved annular orbit,
  connects features with straight/orbital-radial leaders, and minimizes leader length. Rectangular
  display techniques do not automatically generalize to round displays.
- **Transfer:** A boundary rail is credible but must be tested in this clipped phone aperture.
- **Testable heuristic:** Reserve arc length from measured text width, prohibit overlap, and keep
  leader association unambiguous.
- **Misuse risk:** Curved/tangent devotional text can become less readable and ornamental.

## 6. Selected context behaves like stable slider output

- **Source:** [Slider — Adobe Spectrum](https://spectrum.adobe.com/page/slider/)
- **Type/date:** Production design-system guidance; accessed 2026-07-12.
- **Fact:** Sliders should have a stable label/value; top placement suits long copy, localization,
  and responsive layout. Values are formatted for comprehension and overflowing labels wrap.
- **Transfer:** Selected time/deed is control output, not decoration attached to a moving rail.
- **Testable heuristic:** During drag/settle, one stable region exposes selected time/deed without
  crossing the wheel edge; test longest Malay title and localization expansion.
- **Misuse risk:** A conventional form label could weaken the almanac character or duplicate detail.

## 7. Preserve one stable accessible slider identity

- **Source:** [Slider Pattern — W3C WAI-ARIA APG](https://www.w3.org/WAI/ARIA/apg/patterns/slider/)
- **Type/date:** W3C living accessibility guidance; accessed 2026-07-12.
- **Fact:** A slider exposes current/min/max, human-readable value text when needed, stepwise
  adjustment, and a stable name. Touch assistive technology must be tested on target devices.
- **Transfer:** Visual relocation must not create/destroy semantic nodes or move focus. Spoken
  value conveys time plus relevant deed context.
- **Testable heuristic:** One adjustable node survives scrub/expand/collapse; accessibility
  adjustment uses predictable five-minute steps and concise settled phrasing.
- **Misuse risk:** Announcing every raw drag update can become unusably verbose.

## 8. Custom Canvas semantics must be explicit and quiet

- **Source:** [Semantics in Jetpack Compose — Android Developers](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- **Type/date:** Official Android living documentation; accessed 2026-07-12.
- **Fact:** Custom composables need explicit semantics; descriptions/actions determine service
  behavior. Android advises against live regions for frequently changing content.
- **Transfer:** Keep one Canvas control while annotation changes. Alternate visual candidates are
  not separately focusable and continuous scrub is not a live region.
- **Testable heuristic:** Semantics snapshots show exactly one wheel control in every state; focus
  remains during visual handoff; destination focus moves only after settle.
- **Misuse risk:** A full deed/prayer paragraph on every change creates noisy speech.

## 9. Offset or suppress passive labels before rotating selected text

- **Source:** [Selectively Offsetting Axis Labels — amCharts](https://www.amcharts.com/docs/v4/tutorials/selectively-offsetting-axis-labels/)
- **Type/date:** Official production-chart tutorial; amCharts 4 era.
- **Fact:** The example prefers selective staggered offsets to rotation because rotated text is
  harder to read.
- **Transfer:** Reposition/suppress passive prayer/hour labels before rotating the selected deed.
- **Testable heuristic:** Selected deed stays upright; passive labels move/disappear by documented
  priority.
- **Misuse risk:** Mechanical alternating offsets can look arbitrary and still fail at large type.

## State-specific behavior to investigate

- **Active scrub:** selected output remains readable and semantically stable. It does not inherit
  raw lane Y changes. Lower-priority labels yield first; no repeated live announcements.
- **Settle:** geometry and accessible value snap to the same five-minute result. A deed-boundary
  title change uses a bounded content handoff rather than moving the entire annotation region.
- **Expand:** annotation survives until an explicit handoff point; destination summary must be
  legible before source disappears.
- **Collapse:** reverse the same ownership path; wheel annotation is ready before summary retreats.
- **Interrupted:** candidate and semantic ownership depend on current progress; never show two
  selected-deed labels at full prominence.
- **Compact:** use measured bounds and a top safe inset; test every assigned lane.
- **Font scale:** solving true sp and collision together is mandatory. At 1.3×, the selected label
  cannot clip, overlap header/summary, or be smaller than equivalent surrounding information.
- **Reduced motion:** preserve ownership/handoff via immediate relocation or restrained crossfade.

## Contradictions

- Short leaders improve association; a fixed context region improves stability/localization.
- Frame-optimal clearance conflicts with retaining a slightly less optimal prior candidate.
- Circular/tangent labels use radial space; upright stable text is calmer and easier to read.
- A quiet backing guarantees contrast but can become an ornamental pill/card.
- Hiding passive labels protects selection but can erase prayer context.

## Unanswered questions

- Which lane indices occur for every active deed, including future overlaps and cross-midnight data?
- At which minutes/progress/densities is current text clipped or masked?
- Should detail mode retain direct scrub, and is its summary already the destination context?
- How does TalkBack announce the recomposing Canvas in touch and `setProgress` adjustment?
- What are the longest localized titles and minimum canvas-label size?
- Is abbreviation permitted for all contextual wording?

## Wave-2 recommendations

1. Find maintained native code that measures text, scores a small pointer/edge-safe candidate set,
   and preserves the prior candidate during continuous manipulation.
2. Verify a custom Compose adjustable Canvas with TalkBack: drag versus accessible adjustment,
   cadence, focus persistence, and settled value/deed phrasing.
3. Render all active-window boundaries at progress 0/.25/.5/.75/1, widths 360/390, and multiple
   font scales; compare pointer-safe candidate, stable tangent/rail, and fixed-context handoff.
