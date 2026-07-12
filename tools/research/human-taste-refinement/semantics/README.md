# Semantic contract enumerator

This directory is an isolated research artifact. It enumerates a proposed state contract and
persists current-code counterexamples. Some owner/reachability values are model inputs, so a clean
run does not validate the current Compose semantics tree. It does not import, generate, or modify
app or Gradle production sources.

## Reproduce

```bash
python3 tools/research/human-taste-refinement/semantics/check_semantics_state.py
```

The command writes:

- `exhaustive-results.csv.gz`: every overlap interior and boundary × interaction × target/last-settled
  reversal × motion policy × compact/high-type mode.
- `range-results.csv`: required absolute `SetProgress` edge results.
- `cyclic-adjust-results.csv`: explicit earlier/later five-minute wrapping.
- `current-code-counterexamples.csv`: traces derived from current `TimelineScreen.kt` behavior.
- `summary.json`: deterministic totals and status.

## Authority and version boundary

| Item | Project/source contract | Consequence |
| --- | --- | --- |
| Compose BOM | `2024.12.01`, resolved Compose UI `1.7.6` | Use 1.7.6 behavior when judging current code. |
| Semantic hiding | 1.7.6 exposes `invisibleToUser()`; current docs call its successor `hideFromAccessibility()` | Do not paste the newer name without a Compose upgrade. Prefer `clearAndSetSemantics {}` for redundant clock meaning. |
| Alpha | 1.7.6 Android delegate can filter exactly transparent layers, but nodes remain in the Compose semantics tree and positive alpha is not hidden | Ownership must be structural, never alpha-only. |
| Range | `ProgressBarRangeInfo.steps=0` means continuous; current code quantizes to five minutes | Advertised range/steps and action output must agree. |
| Motion | `ValueAnimator.areAnimatorsEnabled()` may be false from duration-scale zero or Battery Saver | It is not a specific reduced-motion preference. App-reduced is a separate modeled input. |
| Focus/announcement | `FocusRequester` proves input focus only; `announceForAccessibility` is deprecated at API 36 and may be ignored | Automated model/UI checks cannot certify TalkBack focus or speech. |
| Accessibility checks | Compose accessibility-check integration starts at 1.8 | Current 1.7.6 needs semantics tests plus manual services; upgrade is a separate decision. |

Official references:

- https://developer.android.com/develop/ui/compose/accessibility/semantics
- https://developer.android.com/develop/ui/compose/accessibility/merging-clearing
- https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/ProgressBarRangeInfo
- https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsActions
- https://developer.android.com/reference/kotlin/androidx/compose/ui/MotionDurationScale
- https://developer.android.com/reference/android/animation/ValueAnimator
- https://developer.android.com/develop/ui/compose/accessibility/testing

## Manual TalkBack/OEM matrix still required

| Axis | Cases | Observe |
| --- | --- | --- |
| Screen reader | Google TalkBack; Samsung TalkBack/Voice Assistant where available | Spoken order, one fardhu occurrence, range increment, focus retention on reversal |
| API | 26, 33, 35; API 36 when available | pane/announcement differences, duration-scale API boundary |
| Motion | normal; animator scale 0; Battery Saver; app-reduced hypothesis | atomic endpoint, no stale focus/effect, no intermediate subtree |
| Input | swipe exploration; range adjust; Switch Access; keyboard/D-pad | canonical owner discoverability and reachable order |
| Layout | 360 compact; 390 regular; font 1.0; 1.3; Android 14 200% | semantics remain reachable; visual bounds require the separate geometry validation |
| Transition | expand/collapse; reverse at .1/.35/.6/.9 | no destination announcement after cancellation; accessibility focus does not vanish |

The executable model proves internal contract consistency only. It does not prove TalkBack/OEM
speech, accessibility focus, visual clock safety, or high-type geometry.
