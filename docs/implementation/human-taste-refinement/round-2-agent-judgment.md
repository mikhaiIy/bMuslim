# Round 2 independent implementation judgment

## Verdict

**78/100 — IMPLEMENTATION REJECT.** Round two removes most of the structural defects from the first
implementation. Production `TextMeasurer`/`TextLayoutResult` now drives the clock corridor, readout
dimensions, and Canvas text; the status-bar inset is included; four font-composition classes exist;
maximum type moves the disclosure outside the wheel; drag values remain quantized; visual
disclosure crossfades; and the main wheel/detail semantics are structurally absent until an endpoint
settles. Those are substantive corrections, not test-only camouflage.

Two visual hard gates remain. First, the readout exclusion is incomplete: labels, nodes, beads, and
the celestial body consult the measured bay, but the full deed arcs and concentric rails are still
drawn through it. At the regular pointer-side bay, the upper-right portions of the outer rails/arcs
geometrically pass through the text region; an active contextual arc can therefore cross readable
devotional copy. Second, the maximum-type composition explicitly shows only the active or next
prayer label, although the locked adaptive direction requires the smaller wheel to retain all five
prayer names. This is not a preference difference; it is a direct loss of required devotional
information.

One semantics hard gate also remains: the transition suppresses the main wheel and detail trees,
but `Kembali ke sekarang` is created from progress 0.95 before the detail endpoint settles, and the
explicit “opened for HH:MM” announcement is immediately followed by focus on a summary that speaks
the same selected time. Thus the implementation still permits transition focus and duplicate spoken
selected-time ownership.

This judgment is independent and did not use Fable.

## Hard-gate result

- **Pass — production text measurement and clock corridor.** Header styles are measured with the
  same strings/type roles used by `AlmanacHeader`; status-bar inset and measured content height feed
  `homeSceneGeometry` (`TimelineScreen.kt:179-260`). The measured clock rect is asserted above the
  aperture for every composition (`TimelineScreen.kt:272-274`). There is one static header clock,
  so there are no doubled/stretched/mixed-baseline digits.
- **Reject — incomplete readout exclusion (P1).** The measured bay is used by `allowed`, and nodes,
  labels, beads, and celestial body consult it (`TimelineScreen.kt:709-716`, `732-750`, `766-783`,
  `803-809`). However, the broad deed rail, hour rail, prayer rail, contextual `drawClockArc`,
  celestial rail, and actual-now mark are unconditional (`TimelineScreen.kt:718-726`, `755-756`,
  `788-808`, `840-845`). The locked correction explicitly includes rotating arcs. An active 7dp
  contextual arc can cross the measured primary/secondary ink envelope.
- **Reject — maximum-type content loss (P1).** Maximum type gets a genuine external band and smaller
  wheel (`HomeScene.kt:82-113`, `135-154`), but `showPrayer` for `Large` and `Maximum` is
  `active || next` (`TimelineScreen.kt:773-777`). At an active prayer, that normally leaves one
  prayer name, not the required five. The endpoint therefore no longer matches the accepted
  adaptive composition.
- **Reject — settled-only semantics/focus (P1).** Main handoff ownership is correctly `None` while
  moving (`HomeScene.kt:186-205`) and the summary/ledger/navigation are gated. But the return-to-now
  `Button` appears whenever progress exceeds 0.95, independently of `detailInteractive`
  (`TimelineScreen.kt:675-684`). It is focusable during the last opening frames and first closing
  frames. At settle, `announceForAccessibility("... ${formatMinute(selectedMinute)}")` is followed
  by focus on the summary, whose content begins with the selected time
  (`TimelineScreen.kt:275-293`, `930-946`, `1497-1521`), producing two spoken selected-time phrases.
- **Pass — range consistency.** Initial values, direct drag, callback values, and accessibility
  adjustment all quantize to five-minute states (`TimelineScreen.kt:154`, `360`, `627-653`;
  `HomeScene.kt:208-211`). The previous 23:56-23:59 visual/range mismatch is removed.

## Score

| Criterion | Score | Judgment |
| --- | ---: | --- |
| Devotional truth and hierarchy | 18/20 | Fardhu/context copy and exact boundary ownership remain strong, but maximum type drops required prayer-name context. |
| Readout geometry and touch craft | 13/20 | Real measurement, stable origin, touch pruning, and node/label exclusion are good; full arcs/rails can still cross the ink. |
| Clock/header transition integrity | 14/15 | Measured header/inset plumbing and one static clock resolve the original bisected-clock mechanism; device raster proof remains conditional. |
| Adaptive composition | 10/15 | Four classes and an external maximum band are real adaptations, but the maximum wheel violates the five-prayer-name lock. |
| Semantics, range, and reduced motion | 10/15 | Endpoint handoff and quantization are much better; return-to-now leaks focus during motion and selected time is spoken twice at detail entry. |
| Verification quality | 8/10 | All 25 unit tests rerun and pass, with exhaustive progress/type/inset loops; no Compose/device accessibility proof and arc exclusion is not tested. |
| Calm visual language/build quality | 5/5 | The opaque patch is gone; measured negative space and external disclosure preserve the quiet almanac language without cards/icons. |
| **Total** | **78/100** | **Reject** |

## Ten-correction audit

1. **Measure, do not imitate — pass, with P2 layout-direction debt.** `rememberTextMeasurer` measures
   brand/date/clock/day and readout/hour/prayer text using production density/font scale
   (`TimelineScreen.kt:179-251`, `547-614`). `TextLayoutResult` is passed into the renderer and drawn
   directly (`TimelineScreen.kt:689-700`, `746-750`, `779-783`, `827-836`). Header status inset is
   real, not a fixture. Remaining **P2:** readout placement is always pointer-right and no
   `LocalLayoutDirection` enters `readoutBayGeometry`; RTL behavior is therefore unproven.

2. **Pure, immediate ownership — pass.** `wheelReadoutAt` remains an exact-minute pure function
   (`HomeScene.kt:157-184`) and tests cover all locked overlap boundaries
   (`TimelineModelTest.kt:49-92`). Presentation density changes with `isDragging`, but devotional
   truth does not wait for the gesture to settle.

3. **Reserve the readout bay before drawing — fail (P1/hard gate).** The executor correctly removed
   the opaque background and uses measured ink to omit intersecting tick segments, text, nodes,
   halos, beads, and the celestial body. But whole circular rails and deed arcs still ignore the
   exclusion. For a regular wheel, the bay begins right of center and just below the pointer top;
   the upper-right outer circle necessarily enters that rectangle. **Smallest fix:** apply a
   difference clip/path or split arc intervals so every rail, contextual arc, and actual-now mark
   omits the padded measured exclusion. Keep pointer/leader as the intentional foreground
   relationship and add a test using actual circle/arc segment bounds, not only rectangles passed to
   `primitiveIsOutsideExclusion`.

4. **Clock corridor and early path — pass at source/unit level.** The strategy is one stable overlay
   clock while the aperture moves. Measured header height sets both glance and detail minimum tops,
   and the assertion runs on every recomposition/progress. The unit matrix checks 101 progress
   values across 1.0/1.3/1.6/2.0, 0/24/36dp inset, compact/regular
   (`HomeSceneTest.kt:99-126`). Remaining device raster evidence is conditional, not a code P1.

5. **Deterministic disclosure handoff — pass.** `presentationHandoff` defines monotonic wheel/detail
   alpha, structural visibility, endpoint-only semantics, and endpoint-only detail interaction
   (`HomeScene.kt:186-205`). Forward and reverse sequences are now genuinely iterated in
   `HomeSceneTest.kt:146-179`. The 0.24-0.50 crossfade has one pure source of truth.

6. **Touch geometry invariant — partial pass; P2.** Touch keeps the same measured origin and leader,
   prunes only the secondary prefix, adds a bounded halo, and quantizes during drag. Node/halo
   bounds now consult the exclusion (`TimelineScreen.kt:766-771`). Cancel clears presentation
   density. Remaining **P2:** `onDragCancel` does not explicitly re-emit/reconcile `rawDragMinute`,
   and the Canvas remains pointer-active while `SemanticOwner.None`; interruption/device gesture
   arbitration is not proven.

7. **Reduced motion structurally — pass in implementation, conditional in evidence.** Animator-off
   uses `snapTo` in both directions (`TimelineScreen.kt:295-324`), and endpoint-derived handoff means
   no intermediate semantics or duplicate visual disclosure at the snapped state. Remaining
   **conditional evidence:** no animator-scale-zero UI test or focus capture proves pending focus on
   a device.

8. **Adaptive composition as breakpoint — fail (P1/hard gate).** `fontComposition` defines four
   classes (`HomeScene.kt:128-133`); `sp`-based `TextMeasurer` supplies nonlinear Android scaling;
   maximum type allocates an 84dp external disclosure band and reduces the wheel. That is genuine
   composition. But hiding all non-active/non-next prayer labels at Large/Maximum directly violates
   “retain five prayer names.” **Smallest fix:** preserve all five names at the maximum endpoint,
   using measured collision-aware radial placement or an upright five-name key within the external
   composition; do not simply shrink or truncate them. Large may prune density only if all required
   prayer context remains recoverable.

9. **Semantics contract — fail (P1/hard gate).** The wheel visual clock/readout are silent, the
   wheel owns one adjustable node only at settled glance, moving detail descendants are cleared, and
   the open action no longer repeats HH:MM (`TimelineScreen.kt:364-378`, `620-633`, `1479-1495`).
   The range has a coherent 0..1435/5-minute contract. Two leaks remain: return-to-now is structurally
   present during transition, and detail entry explicitly announces HH:MM before moving focus to a
   summary that speaks HH:MM again. **Smallest fix:** render return-to-now only for an endpoint owner
   (or clear/disable it while owner is `None`) and remove the time-bearing explicit announcement;
   let the focused summary deliver the locked time -> fardhu -> context -> day phrase once.

10. **Verify rather than infer — partial pass; P2/conditional.** The unit matrix now covers the four
    type classes, three inset values, every percent of progress, reverse handoff, full minute range,
    exact overlap boundaries, and primitive rectangle exclusion. Compile, lint, and assembly pass.
    Remaining **P2:** no test proves complete circle/arc exclusion, measured multiline bay fit, or
    cancel/reduced-motion focus state. Remaining **conditional device evidence:** there is still no
    Android screenshot series, TalkBack/Switch Access traversal, animator-zero capture, OEM font
    fallback, or interruption/reverse gesture capture.

## Verification performed

- `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest --rerun-tasks assembleDebug lintDebug`
  — **passed**, 51 tasks executed.
- JUnit XML reports **25 tests, 0 failures/errors/skips**: 15 `HomeSceneTest` and 10
  `TimelineModelTest`.
- `git diff --check` — **passed**.
- There is still no `app/src/androidTest` suite or configured device/emulator evidence in this
  review. I therefore make no claim of raster-perfect clock/readout bounds, actual TalkBack or
  Switch Access order, gesture interruption behavior, animator-zero focus transfer, OEM font
  fallback, or Arabic/Bidi shaping. The inspected sample ledger does not require an Arabic shaping
  claim.

## Remaining P2 items after the hard gates

- Feed `LocalLayoutDirection` into the readout placement solver and test RTL/mirrored leader
  behavior.
- Reconcile/codify drag-cancel state and disable pointer ownership during the semantic handoff if
  touch testing shows ambiguous sheet/wheel arbitration.
- Add measured multiline bay tests for the longest localized primary/secondary strings.
- Replace the synthetic `measuredContent` values in the pure clock matrix with a small exported
  measurement snapshot contract or Compose layout test, while keeping device proof conditional.
- Add animator-zero and interrupted reverse focus tests when a Compose/device harness is available.

## Pass conditions for round 3

Round three can pass at **>=90** only when all remaining hard gates are removed:

1. every rail/deed arc/now mark respects the padded measured readout exclusion;
2. maximum type retains all five prayer names in a measured, unclipped composition;
3. no focusable return-to-now control exists during transition and selected time is spoken only
   once at the detail endpoint;
4. targeted tests prove the corrected arc exclusion and semantic endpoint gates, with device-only
   evidence still reported honestly if unavailable.
