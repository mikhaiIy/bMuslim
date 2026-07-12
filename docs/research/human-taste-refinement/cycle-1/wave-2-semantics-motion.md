# Wave 2: Compose semantics, reduced motion, and time ownership

## Repository risks

1. `ValueAnimator.areAnimatorsEnabled()` is captured once with `remember`; a live setting change
   is not observed.
2. Summary becomes composed, visible, and `focusable()` around progress .357, far before settled
   detail at .999.
3. Header clock, wheel `stateDescription`, and summary repeat selected time without a declared
   canonical semantic owner.
4. Range semantics advertise continuous `0..1439`, while `setProgress` rounds to five minutes; a
   request at 1439 rounds to 1440 and wraps to midnight.
5. `FocusRequester.requestFocus()` does not prove TalkBack focus; `View.announceForAccessibility()`
   is deprecated at API 36 and has no semantic meaning.

## Sourced Android/Compose contract

- Compose semantics are separate from drawing and consumed by accessibility/tests; low-level
  Canvas controls need explicit semantics:
  [Semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics).
- `ProgressBarRangeInfo` represents value/range/steps. `steps = 0` means continuous; positive steps
  describe evenly distributed values:
  [ProgressBarRangeInfo](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/ProgressBarRangeInfo),
  [progressSemantics](https://developer.android.com/reference/kotlin/androidx/compose/foundation/progressSemantics.modifier).
- `setProgress` is the standard range-value semantic action:
  [SemanticsActions](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/SemanticsActions).
- `clearAndSetSemantics {}` replaces descendant semantics; newer `hideFromAccessibility()` hides
  redundant/decorative accessibility content:
  [Merging and clearing](https://developer.android.com/develop/ui/compose/accessibility/merging-clearing).
  This project uses Compose 1.7.6, where the compatible name is `invisibleToUser()`.
- `Modifier.alpha()` is drawing behavior. In 1.7.6, zero-alpha layers may be treated transparent by
  Android accessibility, but the Compose semantics node still exists and positive alpha remains
  accessible. Alpha is not an ownership policy:
  [alpha modifier](https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/alpha.modifier).
- `paneTitle` describes a distinct pane; live regions should not be used for frequently changing
  content:
  [Semantics package](https://developer.android.com/reference/kotlin/androidx/compose/ui/semantics/package-summary).
- `View.announceForAccessibility()` is deprecated at API 36; services may ignore its event:
  [Android View reference](https://developer.android.com/reference/android/view/View#announceForAccessibility(java.lang.CharSequence)).
- `ValueAnimator.areAnimatorsEnabled()` is system-wide and can be false from duration scale zero or
  Battery Saver; it is not a specific reduced-motion preference:
  [ValueAnimator](https://developer.android.com/reference/android/animation/ValueAnimator).
- Compose `MotionDurationScale` scales Compose motion; zero completes on the next frame and is an
  injectable test seam:
  [MotionDurationScale](https://developer.android.com/reference/kotlin/androidx/compose/ui/MotionDurationScale).
- Automated semantics checks complement but do not replace TalkBack/Switch Access. Compose's newer
  accessibility checks need 1.8+, while this project is 1.7.6:
  [Accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing).

## Inferred ownership contract

Separate:

- **Value owner:** singular `selectedMinute`.
- **Canonical semantic owner:** one stable adjustable selected-time node across every state.
- **Visual renderer:** overlay clock or two sequenced clock roles, neither independently semantic.

The Canvas adjustable node is the natural current canonical owner because it has range/actions.
Clock `Text` nodes should be redundant/decorative semantically. Summaries describe context but do
not become a second adjustable/canonical owner.

Wheel contract: stable 24-hour-selector label, `stateDescription = Masa dipilih HH:mm`, matching
range/step semantics, `setProgress`, and optionally a test-only semantic owner key.

The cyclic day conflicts with a linear range. Two hypotheses require device testing:

- H1: expose `0..1435`, values at five-minute intervals, 286 intermediate steps.
- H2: retain minute range but add explicit earlier/later-five-minute custom actions.

The current `0..1439` plus rounding is internally inconsistent.

## Progress/direction state diagram

Use `lastSettled` to prevent semantics oscillating on reversal. Visuals may use progress; semantic
focusability uses settlement.

```text
G_STABLE: p=0, target=Glance, lastSettled=Glance
  canonical time exposed; open action exposed; detail absent from accessibility

EXPANDING: 0<p<1, target=Detail, lastSettled=Glance
  same canonical time; open disabled/cleared; summary hidden and not focusable; no focus event

D_STABLE: p=1, target=Detail, lastSettled=Detail
  same canonical time; detail summary/actions exposed; one pane/state transition

COLLAPSING: 0<p<1, target=Glance, lastSettled=Detail
  same canonical time; outgoing summary may remain settled anchor; detail actions disabled;
  glance action hidden until stable; no focus event

INTERRUPT/REVERSE
  target changes, p remains current; canonical time never changes identity; visuals recompute
  from p; semantic pane remains lastSettled; canceled destination emits no delayed effect
```

Retaining last-settled pane during reversal is a hypothesis requiring TalkBack testing.

## Overlay clock versus strict fade-through

| Contract | One overlay | Strict fade-through |
| --- | --- | --- |
| Visual instances | One | Source and destination roles |
| Visual owner | Always overlay | Source before t1; neither in gap; destination after t2 |
| Semantic owner | Stable Canvas/time node | Same stable node |
| Clock semantics | Cleared/hidden | Cleared/hidden on both |
| Direction | Geometry pure from p | Alpha/role pure from p |
| Reverse | Moves from current geometry | Thresholds run backward |
| Main failure | Bounds/clip/path | Overlap, gap, or duplication |
| Invariant | One digit run | Never both roles active |

Strict function:

```text
p < t1: source only
t1 ≤ p ≤ t2: neither
p > t2: destination only
```

No thresholds are sourced. At ownership level, `t1 < t2` and both cannot be active together.

## Reduced-motion contract

Distinguish system animators, optional app reduced-motion preference, and injectable Compose
`MotionDurationScale`.

- Disabled animators reach complete destination no later than next frame.
- No intermediate state receives focus or destination semantics.
- Canonical time and selected minute remain unchanged.
- Settlement fires once after final valid composition.
- Do not rely only on `tween(0)`; transition policy selects a valid endpoint atomically.
- A later app preference becomes injected `MotionPolicy`, not alias for animator enabled state.
- Re-read system state on action/resume or inject an observable policy if live changes matter.
- Do not live-announce scrub changes; focused range state is the channel.

## Exact test seams

### Pure unit seams

1. `quantizeAccessibleMinute(requested)` tests -1, 0, 2, 3, 1435, 1439, 1440 and explicit midnight.
2. `timeVisualState(progress, strategy)` samples every 1%; overlay has one renderer; fade-through
   never exceeds one; same p is direction-independent.
3. `semanticTransitionState(progress, target, lastSettled)` guarantees one canonical time owner,
   settled-only summary focus, valid actions, and no premature destination subtree.
4. `settlementEffect(previous,current,pendingTarget)` emits once at completed destination and never
   after canceled destination.
5. `motionPlan(policy,from,target)` gives one valid endpoint when disabled and retains value/owner.

### Compose UI seams

Add Compose UI-test support. Inject progress/controller; disable auto-advance; sample reversal;
assert one custom `CanonicalTimeOwner`; invoke `SetProgress`; assert clock visual roles carry no
accessibility semantics; summary is hidden/not focusable until stable; collapse cancels effects;
inspect merged and unmerged trees; use `assertIsFocused()` only for input focus; test
`MotionDurationScale = 0f`. New accessibility checks require a Compose upgrade to 1.8+.

Official testing references:

- [Semantics testing](https://developer.android.com/develop/ui/compose/testing/semantics)
- [Testing APIs](https://developer.android.com/develop/ui/compose/testing/apis)
- [Common patterns](https://developer.android.com/develop/ui/compose/testing/common-patterns)

## API caveats

- Use project-compatible `invisibleToUser()`; newer docs say `hideFromAccessibility()`.
- Prefer `clearAndSetSemantics` for redundant clock meaning; invisibility is for occluded content.
- Alpha-positive transition frames remain semantically risky.
- FocusRequester does not certify TalkBack focus.
- `announceForAccessibility` remains callable at target 35 but is deprecated at API 36.
- `getDurationScale()` is API 33; app minimum is 26.
- Animator enabled may reflect Battery Saver.
- Range semantics are linear; cyclic edge behavior must be explicit.
- TalkBack may choose its own range increment; test on device.

## Device/TalkBack gaps

Without device or `androidTest`, these remain unproven: accessibility focus result of
`summaryFocus`, spoken output/increment for `setProgress`, focus when summary disappears on reverse,
`paneTitle` behavior across TalkBack/OEMs, API 26 vs 35/36, live animator-scale changes, Battery
Saver versus Remove Animations, Switch Access discoverability, and low-vision comprehension of a
fade-through no-owner visual gap. Manual TalkBack, Switch Access, animator-zero, Battery Saver, and
at least one OEM screen-reader pass remain required.
