# Round 3 independent implementation judgment

## Verdict

**88/100 — IMPLEMENTATION REJECT.** All three hard gates named in the round-two pass conditions are
now corrected in production code:

- one difference clip wraps the complete non-owner wheel draw pass, so broad/hour/prayer/celestial
  rails, contextual arcs, labels, nodes, celestial body, actual-now mark, and keystone cannot cross
  the measured internal readout bay;
- maximum type retains all five prayer names, using all measured radial labels before the midpoint
  and a measured upright five-name key afterward;
- return-to-now is endpoint-only, and the detail-open announcement no longer speaks the selected
  time before focus moves to the time-bearing summary.

The implementation does not yet earn a final pass because the integrated compact 2× detail layout
has a new hard rectangle/space failure. `homeSceneGeometry` adds a 60dp prayer-key band to the
maximum-type detail viewport, moving the paper surface down: at 360×640, detail top is at least
144dp and the wheel/key viewport is 236dp, leaving about 260dp for all paper content. The adaptive
summary alone contains four scaled text lines, 24dp vertical padding, and spacing; at 1.8–2.0 font
scale it consumes roughly 150–175dp. The fixed 48dp close action plus 62dp navigation already bring
the minimum to roughly 260–285dp before navigation-bar inset and before the weighted ledger receives
any usable height. The ledger therefore collapses toward zero and the bottom region is likely
clipped/overflowing. This violates the locked maximum-type requirement to reallocate space before
hierarchy or action content degrades, and it is not covered by the current non-maximum compact
budget test.

This is a narrow final rejection, not a rollback of the round-three fixes. The regular composition,
measured label ownership, clock corridor, transition handoff, range behavior, and core semantics are
source-level sound. This judgment is independent and did not use Fable.

## Hard-gate result

- **Pass — complete negative-space ownership.** `drawNonOwnerLayers` contains every non-owner wheel
  primitive (`TimelineScreen.kt:734-832`) and is rendered inside a `ClipOp.Difference` cutout when
  the measured internal bay exists (`TimelineScreen.kt:834-848`). Pointer, leader, and readout are
  intentionally drawn afterward (`TimelineScreen.kt:860-887`). No background-colored card is used.
- **Pass — all-five prayer retention.** `Maximum` uses `AllRadial` before progress 0.5 and
  `FiveNameKey` afterward (`HomeScene.kt:153-172`). The key is generated from all sorted fardhu
  prayers (`HomeScene.kt:175-178`), measured to available width
  (`TimelineScreen.kt:618-627`), and drawn in its reserved upper band
  (`TimelineScreen.kt:850-857`).
- **Pass — endpoint-only return control.** `returnToNowVisible` requires
  `handoff.detailInteractive`, which exists only for settled detail (`HomeScene.kt:231-250`,
  `269-270`), and production uses that predicate (`TimelineScreen.kt:689-699`).
- **Pass — single time-bearing endpoint speech.** The explicit announcement is now only “Paparan
  amalan dibuka”; the focused summary owns the selected-time phrase
  (`TimelineScreen.kt:277-295`, `971-987`, `1539-1562`). The visual header and key remain silent.
- **Reject — maximum-type compact rectangle integrity (P1).** Maximum detail adds 60dp to the 176dp
  wheel viewport and therefore makes `clipBottom` at least 380dp on 640dp height
  (`HomeScene.kt:97-131`). Paper content starts at that boundary. `AdaptiveContextDisclosure`
  contains scaled line heights of 25sp, 17sp, 16sp, and 14sp plus 31dp fixed padding/spacing
  (`TimelineScreen.kt:1082-1125`); close and navigation add fixed 48dp and 62dp regions
  (`TimelineScreen.kt:1396-1422`, `1340-1351`). At the very font scale that selects this layout,
  these non-weight children consume all or more than the available ~260dp, leaving the weighted
  ledger no credible viewport and risking bottom clipping. No measured budget prevents this.

## Score

| Criterion | Score | Judgment |
| --- | ---: | --- |
| Devotional truth and hierarchy | 19/20 | Exact fardhu/context ownership and all five prayer names survive; the collapsed detail ledger can still make the selected action hard to recover at 2× compact. |
| Readout geometry and touch craft | 19/20 | Measured layout and complete difference clipping resolve the original label occlusion; RTL and real gesture interruption remain P2/device work. |
| Clock/header transition integrity | 14/15 | One measured, inset-aware clock stays above the aperture throughout; actual device raster evidence remains conditional. |
| Adaptive composition | 9/15 | External readout and five-name key are real reflows, but the added key band starves the compact detail paper and violates end-to-end fit. |
| Semantics, range, and reduced motion | 14/15 | Endpoint ownership, single time phrase, quantization, and snap behavior are correct; device focus/animator-zero proof remains conditional. |
| Verification quality | 8/10 | 28 unit tests, full build, lint, and diff checks pass; the critical maximum-type paper budget and real rendered clipping are not tested. |
| Calm visual language/build quality | 5/5 | No card/dashboard/icon regression; negative space, leader, key, and paper remain restrained and coherent. |
| **Total** | **88/100** | **Reject — one P1 adaptive rectangle gate remains** |

## Ten-correction audit

1. **Measure, do not imitate — pass; residual P2.** Production `TextMeasurer`/`TextLayoutResult`
   drives header, readout, hour, prayer, and five-name key dimensions. Safe status inset and measured
   header height drive the aperture. **P2:** `LocalLayoutDirection` still does not enter the readout
   placement solver, so pointer-right geometry is not mirrored/proven for RTL.

2. **Pure, immediate ownership — pass.** Exact-minute fardhu/context truth remains pure and covered
   at every locked boundary. Prayer display policy changes representation, not devotional truth.

3. **Reserve readout bay before drawing — pass.** The whole non-owner pass is clipped by the
   measured bay. The additional per-primitive checks are redundant but safe. Pointer, leader, and
   readout alone may occupy the ownership path. **P2:** the unit test asserts a layer-policy enum
   rather than exercising the actual draw lambda/clip output; screenshot or pixel evidence remains
   useful but is no longer a source-level hard gate.

4. **Clock corridor and early path — pass at source/unit level.** The measured clock rect plus inset
   sets the minimum aperture top, and the pure matrix covers every percentage across all locked type
   and inset classes. The single static clock avoids path interpolation defects.

5. **Deterministic disclosure handoff — pass.** Wheel/detail alpha and endpoint semantic ownership
   have one monotonic pure state for forward and reverse. Transition descendants are structurally
   non-interactive.

6. **Touch geometry invariant — pass at source level; residual P2.** The measured bay and leader do
   not move when touch begins; only secondary prefix and bounded node halo change. Drag output stays
   quantized. **P2:** `onDragCancel` only clears `isDragging`, and the Canvas remains pointer-active
   while semantic owner is `None`; reconcile/capture interruption and sheet/wheel gesture ownership
   on a device.

7. **Reduced motion structurally — pass; conditional evidence.** Animator-disabled opening and
   closing snap directly to endpoint geometry, and endpoint-derived semantics prevent intermediate
   focus owners. No animator-zero device/Compose focus capture exists.

8. **Adaptive composition as breakpoint — fail (P1/hard gate).** The four composition classes,
   nonlinear measured text, external maximum readout, reduced wheel, and five-name key are all
   genuine. The failure is the combined vertical budget: reserving the key by increasing detail
   viewport height reduces paper height below the minimum required by scaled disclosure plus fixed
   controls. **Smallest fix:** keep the maximum detail `clipBottom` within a measured paper budget.
   Place the key inside the existing aperture/header allocation and reduce/reposition the wheel, or
   make the entire paper region one structurally scrollable composition with endpoint controls that
   remain reachable. Derive this from measured disclosure height plus navigation inset, and require
   a nonzero usable ledger/action viewport; do not shrink the fardhu/context copy or remove the five
   names.

9. **Semantics contract — pass; residual P2.** One adjustable wheel node exists only at settled
   glance, one summary owns selected time at settled detail, return-to-now is endpoint-only, visual
   clocks/readouts/key are silent, range states are coherent 5-minute values, and fardhu is spoken
   once. **P2:** requesting summary focus and then issuing a generic explicit announcement may have
   device-dependent speech ordering; a TalkBack capture should confirm it does not interrupt the
   summary.

10. **Verify rather than infer — partial pass; P2/conditional.** Tests now cover negative-space
    layer policy, maximum prayer policy/order, endpoint return control, type/inset/progress matrices,
    reverse handoff, all minutes, and overlap boundaries. The missing decisive test is a maximum
    360×640 detail budget using measured/scaled summary and navigation inset. Add that pure/Compose
    assertion before claiming rectangle integrity. Device-only evidence remains conditional.

## Verification performed

- `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest --rerun-tasks assembleDebug lintDebug`
  — **passed**, all 51 tasks executed.
- JUnit XML reports **28 tests, 0 failures/errors/skips**: 17 `HomeSceneTest` and 11
  `TimelineModelTest`.
- `git diff --check` — **passed**.
- No Android instrumented suite or configured device/emulator evidence was present during this
  review.

## Residual P2 items after the remaining hard gate

- Feed layout direction into the readout/key placement solver and verify mirrored leader geometry.
- Make the five-name key band height measured rather than a fixed 60dp; test multiline key height at
  the longest supported font scale/localization.
- Add pairwise collision/screen-bound tests for the five `AllRadial` labels before the key handoff.
- Reconcile drag cancel and confirm pointer ownership while the sheet is transitioning.
- Replace the policy-enum clipping test with Compose/pixel evidence that the difference clip is
  applied to every rendered non-owner layer.
- Verify generic announcement/focus ordering with TalkBack; remove the explicit announcement if it
  interrupts the summary.

## Conditional device evidence

The following remain **unproven, not failed** because no device/emulator harness was available:

- screenshots at settled/touch/0/25/50/75/100%, interrupted reverse, 360×640 maximum type, and
  0/24/36dp status inset;
- TalkBack and Switch Access traversal, one-time speech, and focus transfer;
- animator-scale-zero endpoint and stale-focus behavior;
- drag cancel, reverse interruption, and sheet/wheel gesture arbitration;
- OEM font fallback, actual contrast across animated palettes, and Arabic/Bidi shaping for ledger
  states that contain Arabic.

## Final pass condition

This phase can pass after one focused remediation: prove and implement a 360×640 maximum-type detail
budget that preserves the complete fardhu/context disclosure, all-five prayer key, reachable detail
action/ledger, close control, and navigation without clipping. Once that P1 is removed, the remaining
items above are P2 or honestly conditional device evidence rather than blockers.
