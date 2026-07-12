# Cycle 2, wave 1: clock-safe geometry

## Bottom line

Current 390×844 regular header is not clock-safe across the matrix. With 24dp status inset:

- font 1.0: full header unsafe at p=.95;
- font 1.3: header unsafe at p=.70, clock numeral at p=.90;
- font 1.6: header unsafe at p=.40, numeral at p=.65.

At 360×640 the two-column header fails only at tested worst case—36dp inset and 1.6 scale—from
p=.75. Reverse traverses the same invalid rectangles. Animator-zero avoids intermediate failures
but lands on an invalid endpoint where applicable. This is geometry/layout, not z-order.

## Evidence classification

### Repository formulas

- Regular `wheelTop`: 217.57dp glance → 120dp detail.
- Compact `wheelTop`: 132.54dp glance → 112dp detail.
- Header height and wheel start both equal `wheelTop`.
- Regular line-height budget: 25 + 12 + 40 + 12 = 89sp.
- Compact right group: 31 + 11 = 42sp.
- Geometry is direction-independent; lower crop at p=.60 is about .257.

### Reproducible proxy

Sampled sizes 360×640/390×844; insets 0/24/36dp; scales 1.0/1.3/1.6; progress 0,.05…1;
both directions; normal/zero endpoints. Assumptions: 4dp aperture gap, production 5dp top/bottom
padding, linear line-height proxy, inset inside fixed height. Required regular bottom:
`inset + 10 + 89 × scale`; compact: `inset + 10 + 42 × scale`.

Android uses nonlinear scaling; production needs `Density`/`TextMeasurer`:
[Scalable Compose content](https://developer.android.com/develop/ui/compose/accessibility/scalable-content).

Noto Sans Mono proxy estimates are not proof of Android generic monospace. Production uses
[TextMeasurer](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/TextMeasurer)
and [TextLayoutResult](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/TextLayoutResult).

| Clock role | Scale | Width proxy | Ink-height proxy |
| --- | ---: | ---: | ---: |
| 38sp source | 1.0 | 108dp | 27dp |
| 38sp source | 1.3 | 141dp | 36dp |
| 38sp source | 1.6 | 173dp | 45dp |
| 28sp destination | 1.0 | 79dp | 20dp |
| 28sp destination | 1.3 | 103dp | 26dp |
| 28sp destination | 1.6 | 127dp | 32dp |

## Quantified matrix

First unsafe progress / unsafe samples of 21:

### 360×640

| Inset | Scale | Header | Numeral |
| ---: | ---: | ---: | ---: |
| 0 | 1.0/1.3/1.6 | safe | safe |
| 24 | 1.0/1.3/1.6 | safe | safe |
| 36 | 1.0/1.3 | safe | safe |
| 36 | 1.6 | .75 / 6 | safe |

Worst compact group needs about 117.2dp plus gap; detail offers 112dp.

### 390×844

| Inset | Scale | Header | Numeral |
| ---: | ---: | ---: | ---: |
| 0 | 1.0 | safe | safe |
| 0 | 1.3 | .95 / 2 | safe |
| 0 | 1.6 | .65 / 8 | .90 / 3 |
| 24 | 1.0 | .95 / 2 | safe |
| 24 | 1.3 | .70 / 7 | .90 / 3 |
| 24 | 1.6 | .40 / 13 | .65 / 8 |
| 36 | 1.0 | .85 / 4 | 1.00 / 1 |
| 36 | 1.3 | .55 / 10 | .75 / 6 |
| 36 | 1.6 | .30 / 15 | .55 / 10 |

Representative 390/inset24/scale1.3:

| p | Aperture top | Header bottom needed | Clock bottom needed | Result |
| ---: | ---: | ---: | ---: | --- |
| 0 | 217.57 | 153.7 | 133.1 | safe |
| .50 | 168.78 | 153.7 | 133.1 | safe |
| .65 | 154.15 | 153.7 | 133.1 | marginal |
| .70 | 149.27 | 153.7 | 133.1 | header fails |
| .85 | 134.64 | 153.7 | 133.1 | numeral marginal |
| .90 | 129.76 | 153.7 | 133.1 | numeral fails |
| 1 | 120 | 153.7 | 133.1 | both fail |

Worst inset36/scale1.6: header fails p=.30; source numeral p=.55, so source-clock handoff must
complete before .55 if this geometry remains.

## Pure safe-bound contract

Inputs: size, actual status inset, density/font scale, progress, target/last-settled, motion policy,
clock strategy, and measured layouts for every visual role.

```text
apertureRect = Rect(0, wheelTop(p), width, clipBottom(p))
headerSafeRect = Rect(16dp, statusInset + 5dp, width - 16dp, wheelTop(p) - 4dp)
```

The 4dp gap is provisional. Apply insets in layout, not fixed nominal values:
[Inset setup](https://developer.android.com/develop/ui/compose/system/insets-ui),
[Inset fundamentals](https://developer.android.com/develop/ui/compose/system/insets).

Measure with production font/direction/scale/constraints. Invariants at every state:

- clock paint rect wholly inside header safe rect and disjoint from aperture;
- all header groups disjoint from aperture;
- no visual overflow; baselines inside assigned line boxes;
- visible clock count 0 or 1; canonical semantic owner exactly 1;
- selected minute identical across roles.

Visual ownership is pure from progress; forward/reverse at p have identical owner/bounds. Retarget
from current geometry; no canceled destination focus/announcement; no start/end booleans.

Canonical semantics stays on adjustable wheel. All visual clock roles are decorative/cleared;
summary may describe but not control selected time.

## Overlay versus strict fade-through

| Criterion | One overlay | Strict fade-through |
| --- | --- | --- |
| Instances | One | Source/destination, max one visible |
| Strength | No missing-time gap, continuous identity | Stable endpoint typography, no traveling numeral |
| Risk | Path crosses brand/date/aperture | Disappearance gap or reversal wrong-role |
| Proof | Every interpolated paint rect | Exclusive thresholds plus endpoint bounds |
| Reverse | Same path back | Same thresholds back |
| Zero motion | Destination endpoint | Destination only, never gap |

### Overlay hypothesis

Worst regular source becomes unsafe at .55, so relocation into a compact destination must finish by
.50. Hypothesis: source anchor through .10; transition .10–.50; destination from .50. Every 1%
paint rect must fit; brand/date must reserve destination space first.

### Fade-through hypothesis

Conservative regular hypothesis: source only below .45; neither .45–.55; destination only above
.55. Thresholds are experimental. Never both; source disappears before unsafe; destination is
bounds-safe before appearing. Animator-zero goes directly source endpoint → destination endpoint.

### Shared destination issue

28sp/31sp two-column destination fits tested regular cases, with only ~2.8dp in worst endpoint. At
compact inset36/scale1.6, full row exceeds endpoint by ~5.2dp although numeral fits. Both strategies
must reserve ~118dp, reflow non-clock content, move date/daypart, or explicitly justify a lower
supported scale. Strategy switch alone does not solve group budget.

## Required image hypotheses

For overlay and fade-through, identical data/background at:

1. 390×844 inset36/scale1.6 p=.30;
2. same p=.50;
3. same p=.55;
4. reverse p=.50;
5. 360×640 inset36/scale1.6 p=.75;
6. animator-zero detail endpoint.

Judge recoverability, doubled/partial/stretched/mixed digits, competition with wheel travel, fade
gap intention, reverse coherence, compact date/daypart retention, and singular saffron emphasis.
Hard rectangle/ownership failure rejects before taste.

## Acceptance matrix

Widths 360/390; heights 640/844; inset 0/24/36; scales 1.0/1.3/1.6; p every .05; both directions;
normal/zero; overlay/fade; times 00:00, 11:11, 19:45, 23:59.

Assert visible clock/header wholly safe, no overflow, valid baselines, overlay count one,
fade count ≤1 and bounded gap, direction invariance, reversal 10/35/60/90 valid, zero motion valid
endpoint, one canonical semantics owner, cleared visual semantics, and compact worst endpoint solved.

Compose animation tests should control clock and sample forward/reverse/retarget:
[Animation testing](https://developer.android.com/develop/ui/compose/animation/testing),
[MotionDurationScale](https://developer.android.com/reference/kotlin/androidx/compose/ui/MotionDurationScale).

Runtime proof still needs production font/TextLayoutResult, frames for 390/360 with insets/scales,
forward/reverse/interruption, animator-zero, and TalkBack single-time identity.

## Conclusion

Both remain viable. Overlay has stronger continuity with larger path-proof burden. Fade-through has
stable roles with an intentional no-clock gap. First obligation is a reserved destination header
rectangle and solved compact inset36/scale1.6 budget; strategy follows hard constraints.
