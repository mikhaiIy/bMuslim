# Final independent implementation judgment

## Verdict

**94/100 — IMPLEMENTATION PASS.** The compact 2× remediation removes the final P1 without
regressing the previously accepted label, clock, hierarchy, handoff, range, or semantic behavior.
Maximum detail now has one outer `LazyColumn` as its sole scroll owner. The ledger is measured at its
intrinsic height and has no nested scroll owner in that branch. The flow order is complete and
meaningful: fardhu/context/date summary, full deed ledger with completion action and source material,
close action, then navigation. Because the whole intrinsic sequence is one lazy flow, the ~250–260dp
compact paper viewport no longer needs to fit every section simultaneously; all sections remain
reachable by scrolling.

Scroll and semantics are endpoint-owned. During motion, the outer flow is non-scrollable and its
semantic subtree is cleared; close and navigation are not composed. At settled detail, the summary
receives focus first and the outer flow exposes content in visual order. The explicit opening
announcement remains time-free, so the summary owns the selected-time phrase once.

The source now clears every implementation hard gate from the research/design locks. Remaining
items are P2 polish or conditional device evidence, not reasons to withhold this pass. This judgment
is independent and did not use Fable.

## Hard-gate result

- **Pass — compact maximum-type reachability.** `Maximum` selects `SingleOuterScroll`
  (`HomeScene.kt:168-173`). Production uses one `LazyColumn` filling the paper viewport, with
  `userScrollEnabled = handoff.detailInteractive` (`TimelineScreen.kt:396-404`). Summary, ledger,
  close, and navigation are separate items in that order (`TimelineScreen.kt:405-463`).
- **Pass — intrinsic non-weighted ledger.** Maximum passes `internallyScrollable = false` and no
  weight to `LivingLedger` (`TimelineScreen.kt:428-447`). The ledger's outer box/column use
  intrinsic `fillMaxWidth`; the internal `verticalScroll` exists only when the regular branch passes
  `internallyScrollable = true` (`TimelineScreen.kt:1244-1291`). There is no nested maximum scroll
  owner.
- **Pass — endpoint-only semantics and scrolling.** `detailInteractive` exists only for settled
  `SemanticOwner.Detail`. Before settle, the whole maximum flow receives
  `clearAndSetSemantics`, user scrolling is disabled, and close/navigation are structurally absent
  (`TimelineScreen.kt:397-404`, `449-463`). The main summary/ledger nodes are also silent while
  transitioning.
- **Pass — compact 2× inset budget contract.** The test covers 360×640 maximum detail at 0/24/36dp
  insets, a complete four-section intrinsic flow, zero weighted children, positive outer viewport,
  required scrolling, and reachability (`HomeSceneTest.kt:250-285`).
- **Pass — prior visual ownership gates.** Real measured text still drives header/readout/key
  geometry; `ClipOp.Difference` still protects the readout from every non-owner layer; the maximum
  policy still retains all five prayer names; one measured clock remains above the aperture; and
  return-to-now remains detail-endpoint-only.
- **Pass — devotional and range truth.** Fardhu remains the one primary role at all overlap
  boundaries, contextual practice stays subordinate, direct manipulation prunes only secondary
  copy, and every selected/accessibility state remains a coherent five-minute value through 23:55
  and midnight.

## Score

| Criterion | Score | Judgment |
| --- | ---: | --- |
| Devotional truth and hierarchy | 20/20 | Exact fardhu-first ownership, subordinate context, all-five maximum prayer reference, and full reachable detail content survive every modeled boundary. |
| Readout geometry and touch craft | 19/20 | Measured stable readout, bounded touch feedback, and complete negative-space clipping resolve the original occlusion; RTL/device interruption remain P2 evidence. |
| Clock/header transition integrity | 14/15 | Measured inset-aware header corridor and one static numeral run remove bisecting/doubling; raster proof on real devices remains conditional. |
| Adaptive composition | 14/15 | Four type classes, external maximum readout, smaller wheel, five-name key, and one intrinsic scroll flow form a real compact recomposition. |
| Semantics, range, and reduced motion | 14/15 | Endpoint-only ownership/scroll/focus, one selected-time phrase, coherent quantization, and direct snap behavior pass; device speech/focus ordering remains conditional. |
| Verification quality | 8/10 | 29 forced JVM tests, compile, APK assembly, lint, and diff checks pass; no Compose instrumentation or screenshot/accessibility device run exists. |
| Calm visual language/build quality | 5/5 | Negative space, paper, restrained leader, and linear reading order remain calm without card/dashboard/icon regression. |
| **Total** | **94/100** | **Pass — no P0/P1/hard gate remains** |

## Ten-correction final audit

1. **Measure, do not imitate — pass.** Production `TextMeasurer`/`TextLayoutResult` supplies header,
   readout, prayer, hour, and five-name key dimensions. Real density/font scale and status inset feed
   the aperture solver. **Residual P2:** placement remains pointer-right and does not consume
   `LocalLayoutDirection`; RTL mirroring is unproven.

2. **Pure, immediate ownership — pass.** One selected minute immediately derives fardhu,
   contextual practice, displayed copy, palette, and spoken order. Tests cover every locked overlap
   boundary without hysteresis delaying devotional truth.

3. **Reserve the readout bay before drawing — pass.** All non-owner wheel content is rendered inside
   a measured rectangular difference clip; pointer, leader, and readout alone are drawn afterward.
   No opaque patch or silent truncation hides collisions.

4. **Clock corridor and early path — pass.** One measured visual clock stays above the aperture at
   all progress values. The exhaustive pure matrix includes four type scales, compact/regular, and
   0/24/36dp inset. No first-frame clock jump, duplicate source/destination run, or digit stretching
   exists.

5. **Deterministic disclosure handoff — pass.** One pure handoff controls wheel/detail alpha,
   structural visibility, semantic owner, and interactivity in both directions. Moving content does
   not enter focus traversal.

6. **Touch geometry invariant — pass at source level.** Direct scrub retains the measured origin,
   leader route, and wheel scale; only the secondary prefix and bounded node halo change. Drag
   output is quantized continuously. **Residual P2:** explicitly reconcile `rawDragMinute` on cancel
   and validate pointer/sheet arbitration during interruption on a device.

7. **Reduced motion structurally — pass.** Animator-disabled transitions snap to endpoint geometry;
   endpoint-derived handoff immediately yields the correct sole semantic/scroll owner. No
   intermediate duplicate disclosure is constructed by the state model.

8. **Adaptive composition as breakpoint — pass.** Regular/comfortable/large/maximum are distinct
   composition policies. Maximum allocates external disclosure, reduces/repositions the wheel,
   preserves all five prayer names, and uses a single outer intrinsic detail flow instead of
   shrinking, clipping, or collapsing the ledger. The 360×640 budget is now deliberately scrollable
   rather than falsely claiming simultaneous fit.

9. **Semantics contract — pass.** Settled glance exposes one adjustable wheel; settled detail
   exposes one time-bearing summary followed by deed action, close, and navigation. Visual
   clock/readout/key are silent. Return-to-now is endpoint-only. The opening announcement contains
   no selected time, and hidden/transition content is structurally or semantically removed.

10. **Verify rather than infer — pass for available environment.** Pure tests cover type/inset/full
    progress matrices, reverse handoff, exact devotional boundaries, all range minutes, layer
    exclusion policy, maximum prayer policy, return control, and compact maximum outer-scroll
    reachability. Build, test, lint, and diff checks pass. Device-only evidence is explicitly
    conditional below rather than fabricated.

## Verification performed

- `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest --rerun-tasks assembleDebug lintDebug`
  — **passed**, all 51 tasks executed.
- JUnit XML reports **29 tests, 0 failures/errors/skips**: 18 `HomeSceneTest` and 11
  `TimelineModelTest`.
- `git diff --check` — **passed**.
- Source inspection confirms the maximum branch contains one outer `LazyColumn`; its
  `LivingLedger` is intrinsic/non-weighted and receives `internallyScrollable = false`.

## Residual P2 items

- Feed `LocalLayoutDirection` into readout and five-name-key placement; test mirrored leader and
  safe bounds in RTL.
- Make the five-name key band's vertical allocation derive from its measured multiline height
  rather than a fixed 60dp, especially for future localization or font scales above the current
  target.
- Add pairwise collision and screen-bound tests for all-five radial labels before the key handoff.
- Reconcile drag-cancel state explicitly and gate pointer input during handoff if device testing
  reveals ambiguous wheel/sheet ownership.
- Replace the enum-policy clipping test with a Compose/pixel assertion of the actual difference
  clip.
- Consider removing the generic explicit open/close announcements if TalkBack testing shows they
  interrupt focus-driven speech.
- The budget test uses conservative representative intrinsic section heights rather than actual
  Compose measurements. A Compose layout test should assert maximum flow semantics/order and last
  item reachability at 360×640.

## Conditional device evidence

The following remain **unproven, not failed** because no configured device/emulator or Android
instrumented source set was available:

- screenshots at settled/touch/0/25/50/75/100%, interrupted reverse, compact maximum type, and
  0/24/36dp inset;
- physical scroll reachability and scroll-indicator behavior for the full maximum detail flow;
- TalkBack/Switch Access visual-order traversal, one-time speech, adjustable wheel behavior, and
  focus transfer;
- animator-scale-zero endpoint behavior and stale-focus cancellation;
- drag cancel, reverse interruption, and wheel/sheet gesture arbitration;
- OEM font fallback, actual contrast through animated palette states, and Arabic/Bidi shaping in
  ledger states containing Arabic.

## Final disposition

The implementation satisfies the research lock and selected design direction at the level the
available environment can prove. It is ready to commit as the implementation pass. The residual
items should remain documented as P2/device validation work and must not be relabeled as completed
device evidence.
