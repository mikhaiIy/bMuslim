# Round 1 independent implementation judgment

## Verdict

**61/100 — IMPLEMENTATION REJECT.** The patch compiles and it materially improves devotional copy,
exact-minute ownership, touch-state density, and accessible range quantization. It does not clear
the implementation hard gates. The central problem is that it replaces the old unmeasured Canvas
label with fixed-dp proxy rectangles and an opaque Compose text patch, while the accepted design
explicitly required production text measurement and a wheel exclusion region. At maximum type the
readout bay spans almost the entire wheel, so the selected prayer node and halo can sit inside the
bay; the bay is then painted over the already-rendered wheel. That is a likely ownership/occlusion
failure, not merely missing evidence.

The clock is visually moved into a more generous header, but its safety test measures a hard-coded
rectangle unrelated to the actual `AlmanacHeader` layout. The patch therefore has no production
proof for status-bar insets, nonlinear font scaling, early transition progress, or alternate text
metrics. Accessibility ownership also changes while the transition is still moving, and several
alpha-revealed detail descendants remain semantic/focusable before they are settled.

This judgment is independent and did not use Fable.

## Hard-gate result

- **Reject — measured rectangle integrity (P1).** There is no `TextMeasurer`, `TextLayoutResult`,
  `onTextLayout`, or measured-bounds data path. `headerSafeGeometry` invents clock bounds at
  `HomeScene.kt:100-112`, and `readoutBayGeometry` invents a 48/78dp bay at
  `HomeScene.kt:115-131`. Those helpers do not consume the actual header/readout layouts.
- **Reject — protected readout ownership (P1).** The full wheel is drawn first at
  `TimelineScreen.kt:452-499`; the readout then paints `palette.middle` over it at
  `TimelineScreen.kt:501-526`. No rotating arc, prayer label, celestial mark, or touch halo is
  excluded from the bay in `drawTimeInstrument` (`TimelineScreen.kt:558-654`). At maximum type the
  bay is `x=16..344` on a 360dp screen (`HomeScene.kt:121-126`) while the selected top prayer node is
  near the horizontal center, so the node/halo can enter the bay and be covered. This violates the
  selected direction's one clear leader/owner and is a likely visible regression.
- **Reject — genuine maximum-type adaptation (P1).** The breakpoint changes some scene constants,
  but Canvas prayer/hour labels remain fixed Android `Paint` sizes through `drawLabel`
  (`TimelineScreen.kt:578-604`, `TimelineScreen.kt:725-742`) and are not driven by nonlinear font
  scaling. The readout uses larger `sp` inside a fixed, unmeasured bay. This is partial reflow, not
  the required measured 1.3x/1.6x/2.0x composition.
- **Reject — semantic/focus ownership (P1).** The summary becomes focusable at progress 0.50
  (`TimelineScreen.kt:297-307`), not only when settled. Ledger, close, and navigation content are
  revealed with alpha without structural semantic removal (`TimelineScreen.kt:339-366`). In glance,
  both the adjustable wheel and the open action speak the selected time
  (`TimelineScreen.kt:460-467`, `TimelineScreen.kt:1137-1154`), so there is not one selected-time
  phrase across the focusable surface.
- **Conditional reject — clock safety.** Normal layouts are plausibly safer because `detailTop` was
  increased (`HomeScene.kt:67-78`) and the header is clipped to `scene.wheelTop`
  (`TimelineScreen.kt:237-245`). However, the asserted clock rectangle is not measured from
  `AlmanacHeader` (`TimelineScreen.kt:375-427`), and no inset/type/device capture establishes that
  the complete numeral ink stays above the aperture. I do not claim an observed device clipping
  failure; I reject the claimed proof.

## Score

| Criterion | Score | Judgment |
| --- | ---: | --- |
| Devotional truth and hierarchy | 17/20 | Exact fardhu/context ownership and safer `Waktu ...` copy are good; visual ownership fails when the bay covers wheel evidence. |
| Readout geometry and touch craft | 8/20 | Origin and touch-pruned copy are deterministic, but no production measurement/exclusion exists and maximum type creates a likely overlap. |
| Clock/header transition integrity | 10/15 | Header allocation is improved and the clock is single, but the safety rectangle is a disconnected proxy and no early/reverse/device proof exists. |
| Adaptive composition | 7/15 | There is a real breakpoint and reduced wheel, but fixed Paint labels and fixed proxy bays make the maximum-type claim incomplete. |
| Semantics, range, and reduced motion | 8/15 | Range quantization and silent visual header improve; mid-transition focus, alpha-only semantics, and duplicate selected-time phrases remain. |
| Verification quality | 7/10 | Unit tests, compile, lint, and assembly pass; tests mostly assert helper constants rather than rendered UI behavior. |
| Calm visual language/build quality | 4/5 | No icon/dashboard regression was added, but the opaque rectangular label patch risks reading as a pasted card. |
| **Total** | **61/100** | **Reject** |

## Ten-correction audit

1. **Measure, do not imitate — fail (P1).** The only production measurement is normal Compose
   layout after placement. Geometry decisions are made beforehand from fixed values. The test at
   `HomeSceneTest.kt:99-113` validates the same invented rectangle, so it is circular evidence.
   **Smallest fix:** measure header clock and both readout lines with `TextMeasurer`/actual
   `TextLayoutResult`, include layout direction and safe inset, then feed those measured ink rects
   into a pure placement/exclusion function used by both layout and drawing.

2. **Pure, immediate ownership — pass with a small caveat.** `wheelReadoutAt` derives both roles
   directly from the selected minute and changes at exact boundaries (`HomeScene.kt:134-161`); the
   boundary tests at `TimelineModelTest.kt:49-92` are useful. `TimelineScreen` independently derives
   `active`, `prayer`, and `contextual` from the same minute (`TimelineScreen.kt:148-152`). The caveat
   is duplicated derivation rather than one immutable presentation state, which raises drift risk
   but does not currently delay truth.

3. **Reserve readout bay before drawing — fail (P1/hard gate).** Passing `readoutBay` to the Canvas
   only draws a leader (`TimelineScreen.kt:635-645`); it does not suppress or reroute any rail,
   label, body, or halo. The opaque background hides collisions after the fact. **Smallest fix:**
   calculate measured ink plus padding, reject/suppress every wheel primitive whose bounds intersect
   that exclusion rect, and choose an alternate disclosure composition when the measured primary
   and secondary cannot fit. Do not solve this with a background-colored rectangle.

4. **Clock corridor and early path — partial, insufficient proof.** There is one static header clock,
   avoiding doubled clocks and stretched interpolation. Moving only the aperture can be a valid
   overlay strategy. But `headerSafeGeometry` is not connected to actual font/inset bounds and tests
   only 0/25/50/75/100, not the early continuum or interruption. **Smallest fix:** derive the header
   minimum height from the measured clock/date/brand stack plus safe inset and assert the actual ink
   rect against every interpolated aperture state (including 0.01 and interrupted/reversed states).

5. **Deterministic disclosure handoff — partial fail.** The thresholds are pure
   (`HomeScene.kt:163-165`) and visual wheel copy is absent by 50%. Yet it disappears abruptly at
   0.42 while summary content begins revealing around 0.36, and semantic ownership waits until 0.50.
   The test at `HomeSceneTest.kt:131-139` compares functions to themselves and does not prove reverse
   behavior. **Smallest fix:** express visual and semantic ownership as one pure handoff state with
   explicit incoming/outgoing alpha and structural visibility; test forward and interrupted reverse
   sequences, not repeated function calls.

6. **Touch geometry invariant — partial fail.** Readout origin and copy remain stable, cancel clears
   drag state, and only the secondary prefix is pruned (`TimelineScreen.kt:442-487`). The bounded
   halo is a good restraint. But the halo is never collision-tested against measured ink, especially
   in the full-width maximum-type bay. **Smallest fix:** include halo bounds in the same measured
   exclusion solver and add cancel/interruption state tests that assert ownership and geometry.

7. **Reduced motion structurally — partial pass, unverified.** Animator-disabled transitions snap
   directly to the endpoint (`TimelineScreen.kt:154-160`, `TimelineScreen.kt:198-227`), which is the
   right basic behavior. There is no test for animator scale zero, stale pending focus, or semantic
   owner at the snapped endpoint. **Smallest fix:** extract destination transition state and test
   both snap directions, including pending-focus cancellation/replacement.

8. **Adaptive composition as breakpoint — fail (P1/hard gate).** `maximumType` does alter header,
   wheel, and detail disclosure (`TimelineScreen.kt:170-175`, `TimelineScreen.kt:309-316`;
   `HomeScene.kt:67-96`). That is better than uniform scale. But there are only two classes
   (`<1.8`, `>=1.8`), no 1.3/1.6 behavior, no measured reflow, and wheel names stay fixed-size Canvas
   paint. The full-width readout bay crosses the selected node. **Smallest fix:** use actual measured
   line results and at least the locked 1.3/1.6/2.0 layout cases; reallocate a disclosure region
   outside the wheel at 2.0 rather than laying an opaque text strip across it.

9. **Semantics contract — fail (P1/hard gate).** The visual header and readout are correctly silent,
   and `wheelStateDescription` speaks time -> fardhu -> context once
   (`TimelineScreen.kt:1287-1302`). The accessible range no longer wraps 1439 to midnight
   (`HomeScene.kt:167-170`). However, the open action repeats the selected time, the summary becomes
   focusable during motion, and later layers rely on alpha while retaining semantics. Also, during
   direct drag at 23:56-23:59 the progress value is clamped to 23:55 while the spoken/visual minute
   may still be later (`TimelineScreen.kt:462-465`), creating inconsistent state. **Smallest fix:**
   define one canonical selected-time semantic owner per presentation, structurally remove hidden
   descendants from traversal until settled, and make drag/range representation agree at the top
   boundary.

10. **Verify rather than infer — fail.** The newly added model boundary tests are valuable. The
    geometry tests validate constants rather than rendered text, use no 1.3x/1.6x cases, no 0/24/36
    inset inputs, no real interrupted animation, and no Compose semantics tree. There is no
    `androidTest` source set. **Smallest fix:** add Compose UI/instrumented coverage for measured
    bounds and semantics, parameterize pure tests across all required progress/type/inset states,
    and capture device evidence where an emulator/device becomes available.

## Verification performed

- `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest --rerun-tasks` — **passed**, 22
  tasks executed; this proves the JVM unit suite ran against the current sources.
- `JAVA_HOME=/opt/android-studio/jbr ./gradlew testDebugUnitTest assembleDebug lintDebug` —
  **passed**; compile, debug APK assembly, and lint are green.
- No Android device/emulator evidence was available in this review. There is therefore **no claim**
  of screenshot rectangle safety, TalkBack/Switch Access order, touch interruption behavior,
  animator-zero focus behavior, OEM font fallback, or Arabic/Bidi shaping. The current ledger data
  inspected here does not require an Arabic shaping claim.

## Creative correction direction

Keep the successful conceptual move: one left-anchored devotional disclosure, one short leader, one
static header clock, and exact fardhu-first language. Replace the pasted color patch with a genuinely
quiet **negative-space bay**: before drawing, omit only the rail ticks/labels that enter the measured
ink envelope while letting distant arc continuity remain visible. At maximum type, stop trying to
place that bay inside the instrument. Give it a shallow header-owned disclosure band above the
smaller wheel, aligned to the same pointer axis; preserve the relationship through a short leader
that terminates before the text field. This retains the crafted almanac feeling without introducing
a card, and it solves measurement, hierarchy, and node ownership together.

## Pass conditions for round 2

Round 2 can pass at **>=90** only after all four P1 groups are removed:

1. real measured ink/inset rectangles drive clock and readout placement;
2. wheel primitives and halo respect an actual exclusion bay, with an external maximum-type
   composition;
3. hidden/moving content is structurally absent from semantics and one selected-time phrase owns
   each presentation;
4. rendered/pure tests cover required type, inset, transition, reverse/interruption, range, and
   reduced-motion states, with device-only proof explicitly conditional if still unavailable.
