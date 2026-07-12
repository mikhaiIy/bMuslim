# Cycle 2 independent research judgment

## Score: 89/100

| Axis | Score | Judgment |
| --- | ---: | --- |
| Source quality | 9/10 | Strong primary religious and Android/platform sources; indirect design-system sources are appropriately bounded |
| Evidence diversity | 9/10 | Religious authority, platform guidance, WCAG, HCI, repository inspection, executable models, and proxy geometry |
| Depth | 11/12 | Thorough state and boundary treatment; geometry explores substantial pressure cases |
| Contradiction handling | 10/10 | Separates fardhu priority from iqamah, calmness from erasure, and proxy feasibility from production proof |
| Native-mobile transfer | 11/12 | Compose-specific ownership, inset, font-scale, semantics, and interruption constraints; runtime proof remains later |
| In-between animation attention | 11/12 | Forward, reverse, retarget, reduced-motion, and sampled progress are explicitly constrained |
| Human-craft usefulness | 7/10 | Good comparison criteria, but less strong at perceptual craft than at systems analysis |
| Calm/devotional relevance | 9/10 | Protects singular devotional emphasis and avoids equal-weight dashboard treatment |
| Conversion to testable constraints | 12/14 | Excellent contract matrix, weakened by proxy assumptions and a largely self-asserting semantics checker |
| **Total** | **89/100** | **Pass, with design-phase hard gates** |

## Direct answers

### 1. Cycle 1 P1 is closed at the research level

Yes. Cycle 2 establishes a culturally and religiously bounded product contract:

- Fardhu owns the primary visual and spoken position during a model-reported overlap.
- Contextual Sunnah remains visible as related, secondary practice.
- The hierarchy is explicitly identified as a **product inference from existing model priority**, not a new ruling.
- The work explicitly says bMuslim knows neither iqamah nor canonical prayer/action windows and must not command abandonment of another valid act or present its 20-minute intervals as fiqh. See [wave-1-devotional-ownership.md](</home/mikhaily/dev/bi/bMuslim/docs/research/human-taste-refinement/cycle-2/wave-1-devotional-ownership.md:47>).

That closes cycle 1’s actual P1: the UI may no longer allow contextual Sunnah to visually or semantically displace an active fardhu identity.

One copy-level caution remains. “Solat Subuh aktif” is ambiguous because the model’s artificial 20-minute interval is not the prayer’s legal time span. Image exploration should prefer language such as “Waktu Subuh · 05:59” or otherwise make the product interval unmistakable. It must never imply iqamah, an instruction to stop zikir, or that Subuh ceases to be valid at 06:20. This is a bounded copy/design test, not a need for another general research cycle.

### 2. Both observed defects are understood well enough

Yes.

For the saffron label, research now establishes:

- fardhu-first ownership before placement;
- one primary high-emphasis text role;
- measured safe placement or an external fallback;
- secondary reflow/disclosure before primary truncation;
- exact boundary handoff;
- touch versus settled behavior;
- reverse/reduced-motion invariance;
- nonduplicative spoken order.

This directly addresses the current unmeasured fixed-offset label at [TimelineScreen.kt](</home/mikhaily/dev/bi/bMuslim/app/src/main/java/com/bmuslim/app/ui/timeline/TimelineScreen.kt:548>). The harness does not prove a final placement, but research has established how to reject bad ones.

For the clock, the cause is also understood: the header height is literally `scene.wheelTop`, while the aperture begins at the same coordinate, so shrinking the header can bisect content regardless of z-order. See [TimelineScreen.kt](</home/mikhaily/dev/bi/bMuslim/app/src/main/java/com/bmuslim/app/ui/timeline/TimelineScreen.kt:234>) and [HomeScene.kt](</home/mikhaily/dev/bi/bMuslim/app/src/main/java/com/bmuslim/app/ui/timeline/HomeScene.kt:29>). Overlay and exclusive fade-through have distinct, testable burdens; neither is prematurely declared the winner.

### 3. The 2.0×/36dp failure is now a design question

It is not a remaining research P1.

The research phase needed to determine:

- why the current clock fails;
- what must remain invariant;
- which strategy families are plausible;
- what each strategy must prove;
- what adaptive fallback is required when the header budget is insufficient.

Those questions are answered. The proxy shows that strategy selection alone cannot fit the existing compact/header allocation at 2.0× with a 36dp inset. That is a valuable negative result: the design must change header height, metadata arrangement, typography role, wheel/aperture allocation, or disclosure behavior.

The image-led phase exists precisely to compare those adaptive compositions. Requiring a successful fixed geometry before entering that phase would collapse research and design into one gate and wrongly treat “the current allocation cannot fit” as an unknown principle.

The design phase must not merely render the two already-failing strategies inside unchanged geometry. It must compare genuinely adaptive layouts, with rectangle failures treated as automatic rejection.

### 4. The semantics checker is meaningful, but its pass count is weak evidence

It proves:

- the proposed abstract contract is internally enumerable;
- its minute-boundary logic produces fardhu-first token order;
- the proposed normal versus zero/reduced subtree rule is deterministic;
- absolute five-minute adjustment and cyclic earlier/later behavior can be specified without midnight ambiguity;
- expected results can be persisted and inspected.

It does **not** prove:

- the current Compose tree satisfies the contract;
- one actual semantics owner exists;
- actual clock nodes are cleared;
- compact/high-type controls are reachable;
- structural hiding is implemented;
- TalkBack speech, focus retention, Switch Access, OEM behavior, or runtime interruption works.

Several “assertions” are assigned as constants:

- `value_owner_count = 1`;
- `semantic_clock_count = 0`;
- `no_alpha_hiding = True`;
- compact/high-type reachability is merely `bool(order)`.

See [check_semantics_state.py](</home/mikhaily/dev/bi/bMuslim/tools/research/human-taste-refinement/semantics/check_semantics_state.py:185>). Therefore, “23,400/23,400 passed” mainly means the model reproduces its own stipulated contract. It must not be cited as application accessibility validation.

The persisted counterexamples do add real value. The range wrap, premature focusability, duplicated fardhu speech, and semantic header clock correspond to inspectable current code. For example, the current range advertises `0..1439` and rounds to five minutes at [TimelineScreen.kt](</home/mikhaily/dev/bi/bMuslim/app/src/main/java/com/bmuslim/app/ui/timeline/TimelineScreen.kt:427>). These receipts are more useful than the checker’s aggregate pass count.

### 5. Evidence labeling is mostly honest, with identifiable overreach

Correctly labeled:

- host text metrics are explicitly not Android `TextMeasurer` proof;
- 2.0× is explicitly linear and not Android nonlinear scaling;
- the 4dp gap, thresholds, offsets, and other constants are provisional;
- Carbon, Spectrum, Apple, WCAG, attention research, and UEyes are treated as transfers rather than direct prescriptions;
- external disclosure is described as requiring content reflow, not existing free space;
- runtime, Arabic shaping, TalkBack, touch occlusion, contrast, and device capture remain unproven.

Remaining overreach or cargo cult:

1. The geometry report claims “205 minute samples,” while the generator and persisted summary contain **165**. See [wave-2-geometry-validation.md](</home/mikhaily/dev/bi/bMuslim/docs/research/human-taste-refinement/cycle-2/wave-2-geometry-validation.md:30>) versus [summary.json](</home/mikhaily/dev/bi/bMuslim/tools/research/human-taste-refinement/geometry/output/summary.json:113>).

2. Direction equality is largely constructional because direction is not used by the layout functions. It proves purity of this model, not real interrupted animation equivalence.

3. The full-width fallback is assumed as a reserved rectangle. Its fit rate does not prove that the CTA, summary, ledger, or navigation can coexist calmly.

4. Dual A and B produce identical aggregate direct/fallback rates because their candidate geometry is barely differentiated. Their percentages should not be treated as comparative design evidence.

5. The overlay is clamped into its safe rectangle. Its results test whether sufficient height exists, not whether the resulting movement is visually natural or avoids all metadata.

6. Baseline and reachability “passes” are too synthetic to carry strong evidentiary weight.

These are P2 evidence-quality issues, not unresolved research principles.

## Research gaps and next direction

### P0 research gaps

None.

### P1 research gaps

None accepted.

The following are **P1 design-validation obligations**, not research gaps:

- solve the clock/header allocation at 360×640 and 390×844, including 36dp inset and 2.0× type;
- allocate real space for primary and secondary external fallback without hiding actions or producing a permanent dashboard;
- ensure copy does not turn the prototype 20-minute focus interval into a claimed prayer-validity interval;
- demonstrate the saffron-label hierarchy and clock ownership at intermediate and reverse frames.

### P2 research/evidence gaps

- Correct the 205-versus-165 sample-count statement.
- Describe the semantics checker as a contract enumerator, not a semantic implementation validator.
- Treat direction, baseline, reachability, and fallback percentages according to what the code actually measures.
- Preserve production `TextMeasurer`, nonlinear scaling, device frames, Arabic shaping, TalkBack, and OEM behavior as later validation gates.
- Current unit tests establish geometry monotonicity and one fardhu-first model example, but do not yet test the cycle-2 ownership, compact, transition, or semantic contracts.

## Mandatory image-led comparison conditions

The next phase must test:

- overlap boundaries and interiors, especially 05:58/05:59/06:19/06:20 and 16:44/16:45/17:05/17:06;
- dual-context hypotheses A/B/C, with only one primary textual emphasis;
- contextual retention without equal saffron headlines, twin leaders, or permanent two-card structure;
- problematic scrub angles in touch and settled states;
- 360×640 and 390×844; 0/24/36dp inset; 1.0/1.3/1.6/2.0 type;
- expansion and collapse at 25/50/75%, reverse at the same progress, and interrupted ownership;
- overlay and exclusive fade-through plus adaptive header/metadata arrangements;
- no partial, doubled, stretched, or mixed clock digits;
- no header or clock ink crossing the aperture;
- compact layouts that reflow/disclose secondary information before degrading fardhu identity;
- reduced-motion states that are complete valid endpoints rather than frozen intermediate frames;
- wording that distinguishes prayer-time context from iqamah and from the prototype focus-window end.

Accessibility semantics, runtime focus, and motion cannot pass from images; those remain explicit implementation-phase checks.

## Provider-quota impact

Provider quota did not materially weaken cycle 2. The corpus records the earlier Firecrawl limitation, but cycle 2 obtained sufficient religious, platform, accessibility, design-system, and HCI authority through the alternate provider. The remaining weaknesses are artifact/model quality and missing runtime evidence, not missing web coverage. No web access was needed for this judgment.

# **RESEARCH PASS**
