# Cycle-2 geometry validation harness

This is an isolated, production-read-only feasibility harness. It does not select a visual design.

## Exact-path attempt and blocker

The project resolves `androidx.compose.ui:ui-text-android:1.7.6` from Compose BOM `2024.12.01`.
That artifact is an Android AAR whose paragraph and font resolver require an Android runtime. At
validation time the host had no `adb`, emulator executable/configured AVD, or cached Robolectric
runtime. Running Android `TextMeasurer` against host-JVM Android stubs would not produce valid text
metrics, so the harness does not pretend to do so.

The fallback invokes ImageMagick through `fontconfig` for deterministic host measurements. Every
result is labeled `HOST_PROXY_NOT_ANDROID_TEXTMEASURER_PROOF`. In particular, the 2.0 scale is a
linear stress proxy and does not reproduce Android 14 nonlinear font scaling.

## Run

```bash
tools/research/human-taste-refinement/geometry/run.sh
```

Outputs:

- `output/dual-matrix.csv.gz`: exhaustive dual A/B/C placement and owner-first gate results.
- `output/clock-matrix.csv.gz`: overlay/fade clock rectangles, counts, gaps, and safety.
- `output/summary.json`: versions, exact matrix, aggregates, limits, and first counterexamples.
- `output/debug/*.svg`: rectangle diagnostics for the first failures.

The harness covers the five synthesized overlap ranges plus/minus ten minutes; widths 360/390;
insets 0/24/36; scales 1.0/1.3/1.6/2.0; progress every .05; forward/reverse; touch/settled;
dual A/B/C; clock strings 00:00, 11:11, 19:45, 23:59; and overlay/fade strategies.

## Interpretation limits

Passing means only that this declared proxy rectangle/state contract is feasible. Production proof
still requires Android `TextMeasurer`/`TextLayoutResult`, actual insets and font fallback, runtime
frames, animation interruption, contrast, touch occlusion, and TalkBack/manual evaluation.

The full-width fallback is modeled as an explicitly reserved region below the clipped wheel.
Existing glance CTA or detail content would have to reflow around it; the harness does not claim
that the current production layout already owns that space.
