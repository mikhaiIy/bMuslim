# Wave 2: rigorous taste evaluation and conditional Arabic typography

## Local scope fact

Arabic exists only for `zikir-pagi`, `zikir-petang`, and `al-mulk`. It renders inside expanded
`LivingLedger`, followed by Malay translation; citation and grade appear later in the scrollable
detail. The wheel contains Malay/Latin labels, no Arabic. Arabic tests therefore belong to ledger
reveal/detail/collapse, compact detail, and font-scale states—not wheel placement.

## Evidence-ranked evaluation protocol

### Rank A: deterministic product-state evidence

Android recommends Compose screenshot testing for visual attributes and identifies font/screen
sizes as useful dimensions. It warns about platform/font-rendering noise and recommends useful
combinations over full Cartesian explosion. Reference/actual/diff need human inspection; change is
not automatically defect: [Android screenshot testing](https://developer.android.com/training/testing/ui-tests/screenshot).

Protocol inference:

1. Freeze content, minute, dimensions, font scale, inset, direction, motion, and progress.
2. Capture identical candidate states: rest; finger-down/boundary; settle; expand 25/50/75%; detail;
   collapse 75/50/25%; reversal 10/35/60/90%; 360×640; font 1.3× and maximum; reduced motion; and
   Arabic ledger for all three Arabic deeds.
3. Store individual full-resolution receipts plus ordered strips/video.
4. Use visual diffs as change detectors; human classifies intended, defect, or renderer noise.
5. Fix emulator/API/font for regression, then run a small real-device/API shaping matrix.

[Roborazzi](https://github.com/takahirom/roborazzi) is an inspectable option supporting
record/compare/verify, Compose capture, qualifiers, and before/after interactions. Its experimental
AI assertion must not enter the evidence lock.

### Rank A: independent defect discovery before preference

Nielsen and Molich found heuristic evaluation depends on combining evaluators:
[CHI 1990](https://doi.org/10.1145/97243.97281). Later evaluator-effect research found people using
the same method detect different problem sets and disagree on severity across experience/method:
[Evaluator Effect](https://doi.org/10.1207/S15327590IJHC1304_05).

Protocol inference:

- Use at least three independent reviewers where feasible.
- Randomize candidate IDs/order and hide approach, author, AI prompt, and hypothesis.
- Reviewers independently inspect the full sequence and record atomic defects before preferences.
- Merge by same element + state + mechanism + consequence, not similar wording.
- Preserve unique and overlapping defects; low overlap is expected evidence, not automatic noise.

### Rank B: blinded paired comparison

Serrano, Roudaut, and Irani used blinded pairwise comparison and a Bradley–Terry–Luce model; some
results contradicted visual conventions:
[CHI 2017](https://hci.cs.umanitoba.ca/Publications/details/visual-composition-of-graphical-elements-on-non-rectangular-displays).

After P0/P1 gating, ask one forced comparison per axis: selected association; invalid intermediate
states; calm primary emphasis without prayer loss; Arabic → translation → source respect; compact/
enlarged-text legibility. Allow tie and require a state coordinate/reason. With small samples,
report wins/ties and reasons; do not imply population ranking.

### Rank B: agreement is calibration, not truth

Krippendorff's alpha supports multiple raters, missing data, and nominal/ordinal distances:
[Hughes 2021](https://arxiv.org/abs/2103.12170).

Report raw agreement and alpha separately for normalized defect presence, severity, and pairwise
preference. Include uncertainty when sample permits. Low agreement triggers rubric/receipt/expertise
inspection; agreement measures repeatability, not correctness or quality.

### Rank C: visual regression is necessary, insufficient

Pixel tests are sensitive to renderer variation; structural GUI relationships can reduce false
positives and improve diagnosis:
[Visual Testing of GUIs by Abstraction](https://arxiv.org/abs/2007.10419).

Pair screenshots with assertions: text bounds inside owner; no partial clock; one time semantic
identity; no hidden focusable duplicate; Arabic not truncated; detail scroll reachable; same
progress gives same ownership both ways.

## Compact scoring sheet

One row per reviewer × candidate × state:

| Field | Required value |
| --- | --- |
| Candidate/state | Blind ID; minute; progress; direction; size; font; motion |
| Gate | Pass / P0 / P1 / P2 |
| Element | Clock, deed label, Arabic, translation, source, pointer, aperture, semantics |
| Observable defect | One factual sentence |
| Evidence | Frame coordinate/timestamp |
| Consequence | Meaning, ambiguity, clipping, focus, attention, or cosmetic |
| Confidence | High / medium / low |
| Pairwise choice | A / B / tie, after defect discovery |
| Reason axis | Legibility, continuity, attention, hierarchy, accessibility |
| Actionability | Reproducible state and correction target: yes/no |

Severity:

- **P0:** wrong/missing devotional content; broken Arabic shaping/order; unreachable essential
  content/action; duplicated/missing canonical selected-time identity.
- **P1:** clipping/overlap/partial glyph; oscillating context; reversal-invalid frame; translation/
  source loss; enlarged-text failure; diacritic clipping; misleading association.
- **P2:** observable refinement without content/task/hierarchy/accessibility loss.

The hypothesis that this improves agreement needs calibration on real receipts.

## Arabic and dynamic-type acceptance

### Normative facts

- Unicode stores bidirectional text logically; visual order follows UBA. Arabic shaping depends on
  neighbors and width uses shaped glyphs: [UAX #9](https://www.unicode.org/reports/tr9/).
- Arabic mark sequences can render inconsistently; UAX #53 covers shadda/vowel combining behavior
  and warns normalization alone may not yield expected stacking:
  [UAX #53](https://www.unicode.org/standard/reports/tr53/).
- Compose `TextLayoutResult` exposes Bidi run direction, character boxes, line count, and size:
  [TextLayoutResult](https://developer.android.com/reference/kotlin/androidx/compose/ui/text/TextLayoutResult).
- Line-height distribution/trimming use font metrics; Android recommends `LineBreak.Paragraph` for
  paragraphs: [Paragraph styling](https://developer.android.com/develop/ui/compose/text/style-paragraph).

### Checks

1. Test exact three Arabic strings in settled detail, relevant reveal/collapse, 360×640, 1.0/1.3/
   maximum font. Translation/source checks apply to all deeds.
2. Preserve model code-point order; never reverse manually. Copy/accessibility equals source. Pure
   Arabic resolves RTL; Malay app/ledger does not globally flip.
3. Letters join; shadda/vowels/marks attach without collision, dotted circles, or disappearance.
   Use an Arabic-literate reviewer; pixel diff cannot establish linguistic correctness.
4. Generic `FontFamily.Serif` is not a stable Arabic typeface contract. Capture minimum/target APIs;
   if adopting a font, test explicit fallback:
   [Compose fonts](https://developer.android.com/develop/ui/compose/text/fonts).
5. No mark ink touches/crosses container; no first/last line clip with default font padding off.
   Inspect rendered pixels as well as layout metrics.
6. Arabic is complete, never ellipsized; wrapping reshapes correctly. Translation/source remain
   complete and scroll-reachable; citation digits/apostrophes stay LTR in separate field.
7. Use real sp/nonlinear scaling. No overlap, clip, horizontal scroll, or fixed-height loss; all
   actions/content remain reachable. XA expansion and AR-XB are stress tools, not real Arabic proof:
   [Android pseudolocales](https://developer.android.com/guide/topics/resources/pseudolocales).
8. TalkBack order is Arabic → Malay translation → source. Alignment does not replace direction or
   semantic order; no hidden duplicate is focusable during reveal/collapse.

## Rule for retaining AI critique prompts

Run baseline/candidate prompts as a blinded crossover on identical held-out receipts. Human
adjudicators create the accepted defect list without prompt identity.

Primary metric:

```text
accepted unique P0/P1 defects found ÷ evaluated artifact set
```

Secondary safeguards: recall against adjudicated list; actionable precision; baseline-only misses;
duplicate/vague count; reviewer time/output length.

Retain a prompt only if it increases accepted unique P0/P1 yield without losing a baseline-found
P0/P1 or materially reducing precision. Reject gains limited to prose, rationale length, aesthetic
vocabulary, or P2 observations. Split results require another held-out set; neither prompt becomes
authoritative.

## Unsupported claims

- Pairwise preference proves usability/accessibility/correctness.
- High agreement proves good design; low agreement proves bad design.
- Pixel equality proves Arabic shaping.
- `TextAlign.End` alone establishes RTL.
- Generic Serif is identical across Android versions.
- Stored devotional text should be destructively normalized.
- AR-XB substitutes for Arabic review.
- 1.3× is a complete accessibility pass.
- All Unicode behavior is identical on every supported API.
- Eloquent AI critique is more accurate, or more issues means a better prompt.
- Arabic belongs on the wheel despite no wheel Arabic.
- This protocol selects a final winner; it only disciplines evidence.
