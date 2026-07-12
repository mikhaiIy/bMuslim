# Cycle 1, wave 1 synthesis

## Epistemic baseline

Use three labels throughout later phases:

- **Sourced fact:** stated or demonstrated by a cited source.
- **Cross-source/product inference:** a reasoned transfer to bMuslim that needs verification.
- **Hypothesis:** a candidate explanation/direction that has not won a comparison.

The two user-reported failures are accepted product facts. Their mechanisms have strong code
support but no runtime capture yet.

For the deed label, code proves unmeasured centered text, no safe region/collision model/backing/
truncation/flip, immediate replacement, and dp-based Canvas sizing. It does **not** prove current
sample data reaches lane 2: current contextual deeds do not overlap and likely stay lane 0. The
lane-2 compact calculation is a valid future stress case, not the observed defect's established
cause.

For the clock, code gives a strong causal account: header moving height equals `wheelTop`; children
may overflow; the later-painted wheel begins at that boundary and can overpaint overflow. No
clock-safe bounds, ownership handoff, or typography transform exists. Runtime must locate the exact
failing frames and device/font conditions.

## Evidence-quality map

| Evidence | Authority | Defect specificity | Transfer risk | Proper use |
| --- | --- | ---: | ---: | --- |
| User reports + current Compose code | Primary product evidence | Very high | Low–medium without capture | Establish failure/test targets; capture still needed for visual pass |
| Android animation docs | Official maintained guidance | High for retargeting | Low–medium | Inform interruption experiments, not visual character |
| Android semantics docs | Official maintained guidance | High for Canvas semantics | Low | Define one adjustable control and semantics tests |
| WCAG 2.2 animation guidance | Normative accessibility | Medium–high | Low | Classify essential motion and require suppression |
| Apple reduced-motion/transition/spring sessions | Official platform guidance | High conceptually | Medium on Android | Use continuity/zero-bounce principles, verify in Compose |
| Material motion/container transform | Official but deprecated M2 | Medium–high | Medium | Persistent identity and text sequencing, not a component mandate |
| Vega label transform | Maintained production visualization | High for bounds/priority | Medium | Measured candidates, forbidden regions, deterministic priority |
| Stein & Décoret | Peer-reviewed | High for temporal coherence | Medium–high | Hysteresis/temporal penalty and hard constraints |
| Circular boundary labeling | Recent peer-reviewed geometry | Medium | High | Feasibility of reserved orbit, not endorsement of curved text |
| Mote candidate placement | Research paper | Medium–high | Medium | Finite candidates/priority, not exact algorithm |
| Ekman thesis | Academic thesis | Medium | Medium–high | Secondary persistence evidence, not physics mandate |
| Spectrum slider | Mature design system | Medium–high | Medium | Stable selected output and localization tests |
| WAI-ARIA slider | W3C but web-specific | Medium | Medium on Android | Stable semantic value identity, verify with TalkBack |
| amCharts tutorial | Product tutorial | Low–medium | Medium–high | Weak support for upright text |
| Functional-animation taxonomy | Peer-reviewed validation | Medium | Medium | Declare motion purpose; not easing/taste evidence |
| Calm Technology | Influential primary practice | Medium | Medium | Attention economy/interruption/periphery, not numeric rules |
| Critique-process articles | Practitioner evidence | Medium | Medium | Improve review language, not visual decisions |
| AI design skills/repos/analysis | Inspectable, no controlled outcomes | Low | High | Mine procedures only; installation is not proof of taste |

Direct product evidence, official behavior, and measurable tests outrank aesthetic consensus.

## Actionable principles

### 1. Measure text before choosing position

- **Acceptance:** For every active deed/boundary, 360/390dp, font 1.0/1.3/declared maximum, and
  progress 0/.25/.5/.75/1, measured label bounds remain inside a safe rectangle and outside P0
  forbidden regions.
- **Anti-pattern:** Guarding anchor points instead of glyph bounds; `sizeSp.dp`; assuming current
  Malay strings cover localization pressure.

### 2. Use deterministic finite candidates with hysteresis

- **Acceptance:** Identical input gives identical placement. A minute step does not switch candidate
  unless current placement breaks a hard constraint or loses by a documented margin. Boundary
  scrub logs contain no oscillation.
- **Anti-pattern:** Force layout, frame-optimal hopping, or raw lane-driven jumps.

### 3. Protect selected context before passive annotations

- **Acceptance:** Selected deed never loses to hour/prayer text, celestial decoration, pointer,
  bezel, or edges. When space is insufficient, passive text yields by documented order while
  essential prayer nodes/context remain.
- **Anti-pattern:** Maximizing label count, hiding all prayer context, or defaulting to a pill.

### 4. Preserve pointer causality without requiring adjacency

- **Acceptance:** Blinded reviewers associate selected deed with the fixed pointer without search;
  leaders do not cross; reading origin stays stable under finger-down manipulation.
- **Anti-pattern:** Minimizing leader length as sole objective or inheriting raw lane movement.

### 5. Give time text explicit visual and semantic ownership

- **Acceptance:** Every frame has zero partial glyph, doubled time, or mixed baseline and exactly
  one intended spoken selected-time identity. Handoff is bounds-safe and direction-independent.
- **Anti-pattern:** Z-order-only repair; misregistered clock crossfade; invisible focusable clock.

### 6. Derive transition state purely from normalized progress

- **Acceptance:** Given progress reproduces geometry, clip, label ownership, and visibility in both
  directions. Reverse at 10/35/60/90%; next frame is bounds-valid with no duplicate semantics.
- **Anti-pattern:** Start/end flags, independent child clocks, completion-only ownership.

### 7. Separate continuity from bounce

- **Acceptance:** Retargeting is position-continuous; instrument/header/label never overshoot.
  Compare current tween with a strongly damped zero-overshoot spring rather than choosing by label.
- **Anti-pattern:** “Spring equals natural,” visible recoil, or endpoint-only acceptance.

### 8. Reduced motion preserves comprehension and ownership

- **Acceptance:** Same selected minute/semantic identity reaches detail in at most two valid states;
  no large translation, scaling, spinning, or decorative orbit remains; focus matches destination.
- **Anti-pattern:** Zero-duration invalid flashes or removal of structural cues.

### 9. Calmness is an attention budget

- **Acceptance:** At most one primary emphasis per audited state; selected context is answerable at
  a glance; meaningful geometry remains; direct manipulation triggers no unrelated mode change.
- **Anti-pattern:** Empty minimalism, flattened contrast, ambient movement, or competing saffron.

### 10. Require observable critique and controlled receipts

- **Acceptance:** Every issue records element + exact state/progress/minute + observable defect +
  user effect + correction evidence. Every motion has a structural/guidance/feedback function.
- **Anti-pattern:** Premium/calm/polished claims without coordinates and acceptance evidence.

## Frame/state audit checklist

Inspect each contract at rest, finger-down scrub, deed-boundary crossing, release/snap, settle,
expand 25/50/75%, detail, collapse 75/50/25%, reversal 10/35/60/90%, 360×640, font 1.3× and maximum,
reduced motion, and inset variants.

| Element/contract | Checks |
| --- | --- |
| Selected minute | Same value drives angle, clock, summary, semantics, announcement; no stale retarget frame |
| Clock/header | Full glyph bounds; baseline; no duplicate/missing time; inset/font budget; reversible ownership |
| Deed label | Measured bounds; candidate; hysteresis; title handoff; leader; no clip/collision; upright text |
| Prayer/hour labels | Bounds, not anchors; selected priority; suppression retains essential meaning |
| Pointer/arc | Fixed pointer and selected arc agree; no ambiguous leader or obstruction |
| Celestial rail | Cannot obscure P0 text; reduced-motion subordinate behavior |
| Aperture | Only crop-safe graphics cross; never bisect text, focus indicator, or touch target |
| Detail summary | Below aperture; owns context before source relinquishes; no double prominence |
| CTA/navigation | Visibility, semantics, hit ownership agree; no alpha-hidden focus; Back valid |
| Semantics | One adjustable wheel; stable name/value; no live-region spam; hidden visuals absent |
| Typography | Real sp/font metrics; long Malay; Arabic/Bidi only where present; baseline clipping |
| Motion | Position continuity; bounded velocity; no overshoot; direction-independent progress |
| Attention | One primary emphasis; sacred hierarchy preserved; no decorative competition |
| Touch | Wheel ownership remains obvious; visual crossings do not change interception ambiguously |
| Evidence | Minute, size, font scale, progress, direction, motion setting, and inset recorded |

Existing tests cover monotonic geometry, radius, content below aperture, and reveal order. They do
not cover text bounds, clock safety, candidate stability, semantic uniqueness, font scaling, or
interruption quality.

## Bounded conflict experiments

### Fixed context vs pointer adjacency

Compare identical content/geometry under:

1. pointer-near finite candidates with hysteresis;
2. fixed upright safe rail with restrained attachment cue;
3. hybrid pointer-near while safe, one-way handoff with a larger return threshold.

All require zero P0 collision/clipping, no oscillation, deterministic output, and stable semantics.
Compare switches, travel, leader length/crossing, title stability, and blinded association. Reject
the hybrid if it creates more mode switching than it prevents.

### Tween vs zero-bounce spring

Trace current asymmetric 620/460ms tween and critically/strongly damped no-overshoot spring.
Retarget at 10/35/60/90% both ways. Measure position continuity, first-frame velocity, settle,
overshoot, and clip violations; judge unlabeled captures. Tween may remain if interruption passes.
Spring wins only with no recoil and predictable settling.

### Single overlay clock vs fade-through

Compare one clock outside aperture clip moving between reserved anchors against two strictly
sequenced roles where outgoing is completely gone before incoming begins and one semantic node
persists. Sample every 5%, reverse during handoff, and test compact/inset/font scale. Reject partial
glyphs, doubles, mixed baselines, semantic duplication, or interruption-invalid state. Penalize
fade gaps and distracting overlay travel.

### Rich vs calm

Compare full passive context, pruning only during scrub, and pruning plus settle restoration.
Selected time/deed stays dominant and prayer meaning remains. Compare comprehension, gaze shifts,
and selected-context errors. Calm cannot erase meaning; rich cannot create equal emphasis.

## Claims excluded from the research lock

- Single overlay clock is definitely correct.
- Current defect is caused by lane 2.
- Deed glyphs rotate.
- Fixed rail or pointer adjacency is inherently superior.
- Circular boundary research endorses curved devotional text.
- Springs are universally more natural/better; zero bounce alone guarantees calmness.
- Any exact duration, easing, hysteresis, opacity, or leader length comes from wave 1.
- Fade-through automatically preserves continuity.
- A recovered platform rule directly prohibits partial glyph clipping; the prohibition here is a
  product-quality contract grounded in the observed defect.
- 1.3× is a complete accessibility pass.
- Installing a taste skill proves improved quality.
- Muted palette, whitespace, texture, ornament, or named aesthetics create taste.
- More source volume increases confidence.
- Haptics, pills, abbreviations, or passive-label hiding are required.

## Exactly three wave-2 scout briefs

### Scout 1: deterministic selected-label geometry and hysteresis

Find the smallest placement contract that guarantees readability while preserving association and
temporal stability. Inspect local data/geometry; use built-in web only for primary Android
`TextMeasurer`/metrics and inspectable maintained code. Define bounds, safe rect, forbidden regions,
2–4 candidates, scoring, hysteresis, suppression priority, and fallback. Run a non-production
experiment across every minute and boundary ±10, progress 0/.25/.5/.75/1, widths 360/390, scales
1.0/1.3/maximum. Treat lane 2 as synthetic. Deliver contract, experiment table, gaps, tests.

### Scout 2: Compose semantics, reduced motion, and time ownership

Use official Android/Compose sources and local code. Determine behavior of Canvas `setProgress`,
alpha-hidden nodes, focusable summaries, semantic clearing/invisibility, announcements, and focus.
Compare overlay clock and strict fade-through as ownership machines. Specify animator-disabled and
reduced-motion alternatives. Propose unit/UI seams for unique time identity, steps, hidden nodes,
focus, and reversal. Deliver sourced contract, state diagram, tests, caveats, TalkBack checks.

### Scout 3: rigorous taste evaluation with conditional Arabic/dynamic typography

Find primary or inspectable methods for blinded pairwise UI comparison, visual regression,
sequence review, and agreement; exclude promotional AI claims. Identify where local Arabic,
translation, and source actually appear. Research Android/Unicode shaping, Bidi, metrics, fallback,
diacritics, truncation, and dynamic type only for those states. Deliver evidence-ranked protocol,
relevant typography checks, rejected claims, and a compact scoring sheet.
