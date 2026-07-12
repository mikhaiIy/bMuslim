# Wave 2: deterministic selected-label geometry and hysteresis

## Finding

The smallest contract that can guarantee selected-deed readability is:

- three normal candidates: right pointer shoulder, mirrored left pointer shoulder, and fixed right
  safe rail;
- hard validity before scoring;
- deterministic costs plus prior-candidate hysteresis;
- selected label priority over passive annotations;
- reserved full-width two-line overlay fallback;
- a content precondition that approved display labels fit that fallback at maximum font scale.

Pointer adjacency passed all tested current lane-0 states but only 46.67% of synthetic lane-2
states. Fixed rail and hybrid covered 100%. Geometry cannot decide which looks calmer; image
comparison must judge rail versus hybrid.

## Evidence

### Sourced facts

- Compose `TextMeasurer` measures laid-out text with font, size, ligatures, spacing, density,
  constraints, and direction. `TextLayoutResult` exposes size, overflow, and baselines. Android
  recommends caching measurement work:
  [Graphics in Compose](https://developer.android.com/develop/ui/compose/graphics/draw/overview#draw-text),
  [TextMeasurer](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/TextMeasurer),
  [TextLayoutResult](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/TextLayoutResult).
- Finite candidates, collision detection, priority expense, and deterministic selection are
  established dynamic-label techniques: Kevin Mote,
  [Fast Point-Feature Label Placement](https://arxiv.org/pdf/1209.5765).
- Frame-by-frame relocation harms temporal coherence; stable methods minimize movement and improve
  tracking/preference: Bobák et al.,
  [Temporally Stable Boundary Labeling](https://cphoto.fit.vutbr.cz/interactive-labeling/data/paper/bobak20_paper.pdf).
- Vega's maintained transform tests ordered anchors, bounds, base marks, and avoided marks:
  [Vega Label Transform](https://vega.github.io/vega/docs/transforms/label/).

### Product facts

Current code draws the selected deed at `center.x + 57dp`, inherits lane radius for Y, uses
unmeasured centered `Paint` and `sizeSp.dp`, and lacks safe rectangle, candidates, text collision,
hysteresis, or fallback. Current contextual deeds occupy lane 0; lane 2 is synthetic only.

All numeric insets/costs below are proposed starting constants, not sourced prescriptions.

## Pure-function contract

```kotlin
data class LabelPlacementInput(
    val textLayout: TextLayoutResult,
    val canvasSize: Size,
    val scene: HomeSceneGeometry,
    val pointerCrossing: Offset,
    val hardRegions: List<Rect>,
    val passiveRegions: List<PassiveRegion>,
    val priorCandidate: CandidateId?,
)

enum class CandidateId { RightShoulder, LeftShoulder, RightRail }

data class Placement(
    val candidate: CandidateId?,
    val textTopLeft: Offset,
    val paddedBounds: Rect,
    val leader: List<Offset>,
    val suppressedPassiveIds: Set<String>,
    val useFallbackOverlay: Boolean,
)
```

### Measured bounds

Measure approved display label per text/style/density, real sp, `softWrap = false`, `maxLines = 1`.
Pad measured width by 4dp each side and height by 3dp above/below. Reject any normal candidate when
`hasVisualOverflow` is true.

### Safe rectangle

Canvas-local:

```text
left = 12dp
top = 8dp
right = width − 12dp
bottom = min(viewportHeight − 8dp, first overlay/control top − 4dp)
```

Entire padded bounds and leader, except intentional anchor contact, remain inside.

### Hard regions

- outside safe rectangle;
- pointer shaft/cap inflated 6dp;
- prayer node/active halo at least 12dp radius;
- pointer crossing except leader start;
- interactive overlay/control inflated 4dp;
- future meaning-bearing marks declared hard.

Suppressible regions are measured hour text, celestial treatment, passive prayer text, next-prayer
text, then active-prayer text. Nodes, selected pointer, and selected deed never disappear.

### Exactly three candidates

Let `B` be padded bounds and `A` pointer crossing:

1. **RightShoulder:** `B.left = centerX + 18dp`, `B.centerY = A.y + 3dp`; short leader.
2. **LeftShoulder:** `B.right = centerX − 18dp`, `B.centerY = A.y + 3dp`; mirrored leader.
3. **RightRail:** `B.left = centerX + 18dp`, `B.top = safeRect.top`; elbow leader.

Text remains upright. Do not clamp an invalid candidate because clamping silently changes its role.

### Deterministic scoring

Discard hard-invalid candidates, then score:

```text
base: RightShoulder 0, LeftShoulder 2, RightRail 12
+ 0.10 × leader length dp
+ 10 per hour-label intersection
+ 16 per celestial intersection
+ 18 per other prayer label
+ 30 per next-prayer label
+ 50 per active-prayer label
```

Choose minimum score with fixed candidate order as tie-break. Boolean intersection is the smallest
initial contract; overlap area is optional later refinement.

### Prior-candidate hysteresis

```kotlin
if (prior != null && prior.isHardValid && prior.score <= best.score + 18f) prior else best
```

Hard invalidity switches immediately. Otherwise a candidate must improve by more than 18. Prior
persists across title changes and reversal. Coordinates derive directly from current geometry; no
force simulation or separate animation state.

### Passive suppression

Suppress only intersecting content in order: hours, celestial treatment, passive prayer text,
next-prayer text, active-prayer text. Never suppress nodes, fixed pointer, or selected deed.
Opacity treatment is a later design decision.

### Fallback

If all normal candidates fail, hand off to a reserved overlay outside clipped Canvas but within
`TimeInstrument`: full safe width, up to two measured lines, restrained leader, and passive labels
suppressed beneath it. Never draw a clipped selected label.

Unbounded arbitrary text cannot have a geometric guarantee. Content invariant:

```text
Every localized selectedDeedDisplayLabel fits the two-line fallback
at 360dp and the supported maximum font scale.
```

Full title remains in wheel semantics even when visual short form is approved.

## Read-only proxy experiment

Matrix: five current contextual labels; start/end boundary ±10 minutes; active states; progress
0/.25/.5/.75/1; 360×640 and 390×844; scales 1.0/1.3/1.6; current lane 0 plus synthetic lane 2.
There were 3,300 states per lane, 6,600 total. Liberation Sans Bold host metrics were a conservative
proxy, not proof of Android font resolution; production must rerun with `TextMeasurer`.

| Data | Strategy | Hard-feasible | Clean before suppression | Suppression |
| --- | --- | ---: | ---: | --- |
| Current lane 0 | Pointer shoulders | 3,300/3,300 (100%) | 99.24% | 25 celestial intersections |
| Current lane 0 | Fixed rail | 3,300/3,300 (100%) | 90.00% | 330 celestial intersections |
| Current lane 0 | Hybrid | 3,300/3,300 (100%) | 99.24% | 25 celestial intersections |
| Synthetic lane 2 | Pointer shoulders | 1,540/3,300 (46.67%) | 46.67% | Cannot guarantee |
| Synthetic lane 2 | Fixed rail | 3,300/3,300 (100%) | 90.00% | 330 celestial intersections |
| Synthetic lane 2 | Hybrid | 3,300/3,300 (100%) | 94.67% | 176 celestial intersections |

Combined: pointer 73.33% hard-feasible; rail/hybrid 100%. Hybrid mean leader 11.6dp, max 13.2dp.
Ten candidate switches occurred across consecutive boundary sequences with proposed hysteresis.

Lane-2 pointer failures cluster in compact states and some regular glance/high-scale states. This
validates a stress test, not a cause claim for current production.

## Uncovered states

- Real Android font/fallback metrics and device insets.
- Leader intersections against all runtime-measured text/segments.
- Future overlap/cross-midnight lanes and longer localization.
- Saffron-on-saffron contrast and effect of celestial suppression.
- High-velocity drag/title handoff captures and TalkBack behavior.

## Acceptance tests

1. Every label/matrix state produces valid normal placement or fallback.
2. Padded bounds lie entirely inside safe rectangle and avoid all hard regions.
3. `TextLayoutResult.hasVisualOverflow == false`.
4. Identical complete input gives bitwise-equivalent placement.
5. Valid prior persists unless another candidate wins by more than 18.
6. Hard-invalid prior switches on the same frame.
7. Forward/reverse at identical geometry/prior gives identical placement.
8. Boundary sweep contains no A → B → A oscillation without hard invalidity.
9. Selected text never suppresses; passive order is exact; prayer nodes remain.
10. Current lane 0 passes matrix; synthetic lanes exercise fallback without product misreporting.
11. Every localized display label fits two-line fallback at 360dp/maximum scale.
12. Old `sizeSp.dp` selected-label measurement is removed.

## Required image comparison

Generate separate controlled images for pointer shoulder, fixed right rail, hybrid before/after
handoff, and each with necessary passive suppression. Priority states:

- 360×640, scale 1.6, progress 1, longest current title;
- 360×640, scale 1.6, progress .25/.5/.75 at deed boundary;
- current lane-0 celestial competition;
- explicitly synthetic lane-2 stress;
- forward/reverse frames around hybrid switch.

Judge association, stable reading origin, hybrid jump, whether rail looks like an almanac annotation
or generic form label, loss from celestial suppression, leader ambiguity, and pill-like backing.
Geometry does not select the visual winner.
