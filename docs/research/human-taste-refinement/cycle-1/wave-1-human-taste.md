# Wave 1: human taste, calm craft, and AI design-agent workflows

Three Firecrawl `search --scrape` passes produced 21 full-page results; raw evidence is under
`.firecrawl/`, and feedback was submitted. A narrower follow-up hit HTTP 402, so this report uses
only successfully scraped evidence.

## 1. Functional animation taxonomy

- **Source:** Raquel Ávila Muñoz, Jorge Clemente Mediavilla, María José Pérez-Luque Maricalva,
  [Validation Process of the Taxonomy of Functional Animation in User Interface Design](https://docta.ucm.es/bitstreams/33b1b0f7-e69a-4f7f-8699-ff1e23b940d7/download)
- **Type/date:** Peer-reviewed paper, 2024.
- **Fact:** After reviewing more than 2,400 references and surveying 36 experts, the authors
  classify touchscreen animation into identifying, structural, guidance, feedback, didactic,
  aesthetic, emotional, and security functions. Feedback scored highest for UX importance,
  4.83/5; aesthetics lowest, 3.81/5. The taxonomy scored 4.26/5 overall.
- **Transfer inference:** Every bMuslim motion should declare its functional job. Wheel response is
  feedback; clock/aperture movement preserves structure; label relocation provides guidance and
  feedback. “It feels serene” is insufficient.
- **Testable heuristic:** Annotate every animated property with one primary function. Reject motion
  with only aesthetic/emotional justification unless it measurably improves comprehension.
- **Misuse risk:** Categories are not permission to layer simultaneous animations; the taxonomy
  classifies purpose, not duration or flourish quality.

## 2. Replace “polish” with observable critique

- **Source:** Thomas DiNatale / Everyday Industries,
  [Why “Visual Polish” Is Not Design Feedback](https://everydayindustries.com/visual-polish-is-not-feedback/)
- **Type/date:** Practitioner process article, 2025-08-22.
- **Fact:** DiNatale replaces vague polish requests with named problem areas and effects: hierarchy,
  spacing, alignment, icon weight, type competition, and abrupt transitions. Exit criteria cover
  type scale, spacing grid, rhythm/alignment, hierarchy-supporting color, brand tone, and cohesion.
- **Transfer inference:** State defects precisely: “At 50% the clock glyph crosses the aperture
  clip,” or “At angle X the saffron label intersects a prayer node and loses a reading origin.”
- **Testable heuristic:** Every critique contains element + state/progress + observable defect +
  user impact. “Calmer,” “premium,” and “polished” fail unless decomposed.
- **Misuse risk:** A checklist can standardize a lifeless result. Relational rhythm still needs
  controlled visual comparison.

## 3. Structured critique as risk discovery

- **Source:** UX Tigers, [How to Run a UX Design Critique](https://www.uxtigers.com/post/design-crit)
- **Type/date:** Practitioner synthesis with 51 references and videos, 2026-03-12.
- **Fact:** The method frames user, goal, constraints, maturity, and requested feedback; separates
  critique from sign-off; uses silent feedback; asks reviewers for risks before fixes; records
  decisions; and recommends narrative journeys over isolated screens.
- **Transfer inference:** Review bMuslim as rest → touch → scrub → release → settle → expand at
  25/50/75% → detail → reverse/interruption rather than endpoint images.
- **Testable heuristic:** Give independent reviewers the same state matrix without proposed fixes.
  Cluster repeated observations; repeated state-specific issues become priority corrections.
- **Misuse risk:** The source is broad synthesis. Its procedure is more useful than implied
  authority; retain independent judgment and direct product evidence.

## 4. Calm Technology principles and exercises

- **Source:** Amber Case, [Principles of Calm Technology](https://calmtech.com/)
- **Type/date:** Primary practitioner reference; maintained, with May 2024 presentation.
- **Fact:** Calm technology asks for the smallest necessary attention, movement between periphery
  and center, minimal sufficient technology, communication without always speaking, and graceful
  failure. Concrete patterns include quiet status, reserved urgency, haptics, and delay/interrupt
  testing during state change.
- **Transfer inference:** During scrub, peripheral geometry updates while only selected context
  receives primary emphasis. Interruption must land in a usable, semantically coherent state.
- **Testable heuristic:** At each transition sample, one glance answers the selected time/context.
  No more than one element demands primary attention. Aborted motion resolves to a valid role
  within one frame.
- **Misuse risk:** Minimum attention is not hiding prayer information or flattening contrast.
  Calmness is low interruption plus clear hierarchy, not low information.

## 5. Periphery, pass-through, and micro-detail

- **Source:** Amber Case / AIGA Design,
  [Calm Technology: Embracing the Periphery in Design](https://www.youtube.com/watch?v=3uv6-QfTBp4)
- **Type/date:** Practitioner talk with transcript, uploaded 2025-02-25.
- **Fact:** Case distinguishes high-resolution primary attention from peripheral awareness and
  argues for pass-through interfaces where attention returns to the human task. Stable physical
  controls are contrasted with changing touchscreens that demand visual confirmation.
- **Transfer inference:** A calm scrub becomes learnable through stable spatial rules: fixed
  saffron pointer, predictable wheel response, and a selected-label home that does not jump.
  Active touch should not demand reading moving fine text; detail can increase on settle.
- **Testable heuristic:** During finger-down drag, preserve input ownership and selected-context
  location; avoid layout mode changes until release unless essential. Reversal must not require
  reacquiring the control visually.
- **Misuse risk:** Peripheral cues cannot carry sacred text, accessibility-critical state, or
  color-only meaning.

## 6. Calmness includes constrained conditions

- **Source:** Sheena Lyonnais interviewing Amber Case / Adobe,
  [Calm Technology in the Era of Experience Design](https://blog.adobe.com/en/publish/2016/10/12/calm-technology-in-the-era-of-experience-design)
- **Type/date:** Practitioner interview, 2016-10-12.
- **Fact:** Case connects calm design to bandwidth, accessibility, data limits, respect for time,
  alternate senses, and interfaces that do not force people to adapt to the machine.
- **Transfer inference:** Compact height, 1.3× text, reduced motion, and screen reader use are tests
  of respect, not cleanup variants. A relocating label needs stable semantic identity.
- **Testable heuristic:** For every hidden/clipped/relocated visual, verify semantics, focus order,
  description, and touch target separately.
- **Misuse risk:** Haptics or color can exclude users if mandatory. Essential state needs robust
  visual and semantic routes.

## 7. Thin interfaces are hierarchical, not merely sparse

- **Source:** Harri Heikkilä / LAB University of Applied Sciences,
  [Towards a Silent Revolution of Calm Technology](https://www.labopen.fi/en/lab-pro/towards-a-silent-revolution-of-calm-technology-thin-interfaces-pass-through-and-cognitive-sustainability/)
- **Type/date:** UX/visual communication practitioner article, 2025-11-10.
- **Fact:** Thin interfaces are not visual minimalism; they require information hierarchy, removal
  of unnecessary cognitive work, natural action, graceful degradation, predictable failure, and
  respect for human rhythms.
- **Transfer inference:** The wheel may stay visually rich if interaction is automatic and
  hierarchy stable. Restraint targets attention switching, label competition, and unpredictable
  geometry—not meaningful prayer/deed structure.
- **Testable heuristic:** Count forced attention switches. Scrubbing to a deed and opening detail
  should not require unnecessary gaze movement among pointer, label, clock, and CTA.
- **Misuse risk:** Automaticity can hide unfamiliar custom gestures; maintain discoverability,
  feedback, and accessible alternatives.

## 8. An inspectable critique-skill artifact

- **Source:** AI UX Playground / slb2248,
  [Design Critique skill](https://aiuxplayground.com/skills/design-critique)
- **Type/date:** Installable agent skill, undated.
- **Fact:** The artifact encodes context/scope preparation, stage-appropriate feedback,
  Observation → Impact → Suggestion, accessibility examples, documentation, prioritization, and
  assigned outcomes.
- **Transfer inference:** Its value is procedural consistency. Replace generic prompts with bMuslim
  identity and the exact state matrix; require evidence coordinates instead of free-form taste.
- **Testable heuristic:** Compare reviews with/without the protocol for vague duplicates, missed
  transition states, and actionable issue yield.
- **Misuse risk:** “I like / I wish / what if” remains preference without user-task evidence.
  Installing a skill is not demonstrated taste improvement.

## 9. Persisted artifacts and screenshot-gated review

- **Source:** Julian Oczkowski, [designer-skills](https://github.com/julianoczkowski/designer-skills)
- **Type/date:** Open-source agent workflow; current revision July 2026.
- **Fact:** The repository chains interrogation, design brief, IA, tokens, task slicing,
  implementation, and structured review; persists `DESIGN_BRIEF.md`, `TASKS.md`, and
  `DESIGN_REVIEW.md`; detects existing components/tokens; and supports mandatory screenshot review.
- **Transfer inference:** The valuable pattern is persistent constraints, inspection of the native
  system, controlled screenshots, and durable review—not named aesthetic menus.
- **Testable heuristic:** Every accepted change cites a product constraint and attaches evidence
  for affected matrix states. Reject done without visual receipts and code-level checks.
- **Misuse risk:** Philosophy presets are a cargo-cult vector disconnected from Malaysian
  devotional identity.

## 10. Taste as governed review infrastructure

- **Source:** Developers Digest,
  [Taste Skills Are Turning Agent Review Into Infrastructure](https://www.developersdigest.tech/blog/taste-skills-ai-agents-design-review)
- **Type/date:** Industry analysis, 2026-05-30.
- **Fact:** The proposed loop is local strategy/contract → focused skill → smallest change →
  deterministic checks → self-review → human evaluation → repeated feedback encoded locally. It
  recommends judging skills by fewer corrections, design-system adherence, cross-size quality,
  and reduced generic output.
- **Transfer inference:** Useful taste enforcement is approved/rejected examples, state receipts,
  accessibility gates, and repeated local lessons rather than prompt theater.
- **Testable heuristic:** Track first-pass P0/P1 issues and recurring critique categories. Retain a
  taste skill only if those measures improve.
- **Misuse risk:** This is analysis, not controlled evaluation; the improvement claim must be
  tested, not assumed.

## What genuinely resists generic output

Supported by artifacts/sources: product-specific constraints; critique framed around user tasks;
exact state coverage; local components/tokens; approved and rejected examples; independent visual
comparison; motion with declared function; deterministic accessibility/layout tests; persisted
review receipts; and learning from repeated corrections.

**Inference:** Human taste is relational judgment across type, space, motion, content, and touch
behavior over time—not a collection of individually premium ingredients.

**Cargo cult:** named aesthetics, muted palettes, whitespace, soft easing, texture, Arabic
ornament, or calm copy applied without comprehension, collision, interruption, compact, and
accessibility testing.

## Contradictions and gaps

- Calm Technology favors peripheral, low-attention cues; devotional and accessible content can
  require explicit central text. The selected-deed boundary remains unresolved.
- Stable label placement reduces effort, but a radial label may better preserve spatial causality.
- Motion explains structure, while reduced motion must communicate it with little interpolation.
- The animation taxonomy validates purposes, not durations, easing, collision, or interruption.
- AI workflow repositories demonstrate artifacts but no scraped source supplies controlled
  before/after proof of better visual taste.
- Arabic/translation/source typography metrics under compact height remain a gap.

## Wave-2 recommendations

1. Search horology, automotive/HUD, medical radial visualization, chart/map label algorithms, and
   edge avoidance for stable tangent/rail handoff and flip hysteresis.
2. Target primary Android/Compose, Apple, Material Motion, and experienced practitioners for
   gesture cancellation, reversal, reduced motion, and 25/50/75% evidence.
3. Find Arabic mobile typography, dynamic-type/reflow evidence, and repositories or papers that
   compare AI frontend outputs with blinded screenshots or visual-regression gates.
