# Cycle 2, wave 2: overlap and clock geometry validation

## Result

The full synthesis gate **does not pass**. Dual-context geometry is recoverable only through
substantial fallback, and neither tested clock strategy survives the entire 2.0×/36dp-inset matrix.

Artifacts:

- `tools/research/human-taste-refinement/geometry/geometry-matrix.mjs`
- `tools/research/human-taste-refinement/geometry/run.sh`
- `tools/research/human-taste-refinement/geometry/README.md`
- compressed raw dual/clock matrices, summary, debug SVGs, and invocation output under `output/`.

Reproduce:

```bash
tools/research/human-taste-refinement/geometry/run.sh
```

## Measurement authority

Exact Compose attempt was blocked: `ui-text-android:1.7.6` requires Android paragraph/font runtime,
but no adb/emulator/AVD/cached Robolectric runtime exists. Host stubs would be invalid. Results are
therefore labeled `HOST_PROXY_NOT_ANDROID_TEXTMEASURER_PROOF` and use ImageMagick/fontconfig with
Liberation Sans/JetBrains Mono and linear scales. 2.0 does not reproduce Android nonlinear scaling.

## Matrix

Five overlap ranges ±10 (**165** sampled rows); 360×640/390×844; insets 0/24/36; scales
1.0/1.3/1.6/2.0; p every .05; both directions; touch/settled; dual A/B/C; clock strings
00:00/11:11/19:45/23:59; overlay/fade. Fardhu owner is resolved before placement.

Final run: **997,920 dual rows + 16,128 clock rows**. Direction equality is constructional because
direction is deliberately not an input to the placement functions; it proves model purity, not
runtime interruption quality.

| Gate | Result |
| --- | --- |
| Fardhu primary and inclusive boundary truth | Pass, 0 ownership failures |
| Readable fardhu primary | Pass only with external fallback |
| Secondary never suppresses fardhu | Pass owner-first |
| Secondary safe/recoverable | Pass with external disclosure |
| Proxy baseline and direction | Pass, 0 failures |
| Overlay visible count 1 | Pass |
| Fade count ≤1 and bounded .45–.55 gap | Pass |
| Clock/header safe across matrix | **Fail both** |
| Worst compact adaptive state | **Fail 2.0×/36dp** |
| Production font proof | Not established |

Overall: fail/incomplete, not research lock by itself.

## Dual-context pressure

131,040 overlap rows per hypothesis:

| Hypothesis | Direct secondary | External fallback |
| --- | ---: | ---: |
| A hierarchical stack | 49,440 (37.7%) | 81,600 (62.3%) |
| B stable rail | 49,440 (37.7%) | 81,600 (62.3%) |
| C compact cue | 70,962 (54.2%) | 60,078 (45.8%) |

At 1.0 no secondary fallback. At 1.3: A/B 58.5%, C 29.2%. At 1.6: A/B 90.6%, C 54.2%,
and primary fallback 17.7%. At 2.0 every overlap uses full-width external primary and secondary
disclosure. C's feasibility is not proof it feels calmer.

## Clock results

| Strategy | Unsafe | Count failures | Gap rows |
| --- | ---: | ---: | ---: |
| Constrained overlay | 160/8,064 (1.98%) | 0 | 0 |
| Fixed fade | 480/8,064 (5.95%) | 0 | 1,152 |

Overlay fails compact inset36/2.0 from p=.60–1 and regular inset36/2.0 detail endpoint. It passes
proxy rows through 1.6. Fade also fails regular inset36/1.6 p=.35–.40 and multiple 2.0 states;
fixed thresholds do not remove unsafe source/destination roles.

At 2.0, destination header needs more height or different metadata/type arrangement. Strategy
choice cannot solve insufficient height.

## Evidence still required

Repeat with Android TextMeasurer/device; actual nonlinear 2.0/fallback fonts; whole header group;
external fallback plus CTA/detail reflow; image comparison at 1.3/1.6; clock unsafe frames/reverse;
runtime leader/contrast/touch/interruption/TalkBack checks.

No final UI choice is justified by this feasibility harness.
