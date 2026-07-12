# Human-taste refinement research lock

## Status

- Independent research judge: **89/100 — RESEARCH PASS**.
- Orchestrator meta-rating: **38/40**.
- Two research cycles completed.
- No accepted P0/P1 research gap remains.
- This locks constraints and comparison questions, not a final visual design.

## Known product truths

### Deed/prayer ownership

- Current label is unmeasured centered Canvas text at a fixed pointer-side offset, with no safe
  bounds, collision model, font scaling, hysteresis, or fallback.
- The selected label does not literally rotate; arcs rotate while the title and lane-derived anchor
  update near the pointer.
- Active fardhu and contextual Sunnah overlap in current data. Fardhu already sorts first in model
  and owns the ledger, but wheel/summary visuals can give contextual content equal or greater weight.
- During modeled overlap, fardhu owns the only primary textual role. Related Sunnah remains visible
  and secondary. Day state is tertiary.
- Ownership truth changes at the exact selected minute and cannot be delayed by animation or
  placement hysteresis.
- Do not equate the prototype prayer/action interval with iqamah or a canonical legal prayer span.
  Copy must not imply that a prayer becomes invalid at the model interval end or command the user
  to abandon another valid practice.

### Clock/header ownership

- Header height and aperture top both equal `wheelTop`; later-painted wheel can overtake header
  content. This is geometry, not just z-order.
- All clock ink and metadata require explicit measured header-safe bounds outside the aperture.
- One adjustable wheel node is the intended canonical selected-time semantic owner; visual clocks
  should be semantically redundant/decorative.
- Overlay and strict fade-through are both viable strategy families, but each has a different proof
  burden. Strategy choice alone cannot solve an under-height destination header.

### Calm/human craft

- Calm means one clear primary emphasis, predictable touch ownership, exact state truth, stable
  reading origins, low interruption, and graceful constrained behavior—not empty minimalism.
- Every animation needs a structural, guidance, or feedback function. Aesthetic-only movement does
  not enter by default.
- State quality at touch, boundary, 25/50/75%, interruption, reverse, compact, maximum type, and
  reduced motion matters as much as endpoints.
- Observable critique format: element + minute/state/progress + defect + consequence + evidence.

## Devotional hierarchy contract

At every overlap:

1. Full active fardhu name/time/state and prayer node remain immediately recoverable.
2. Contextual practice remains related secondary information, never an equal saffron headline.
3. Hierarchy survives grayscale using at least two non-color channels: order/placement plus size,
   weight, spacing, enclosure, or leader treatment.
4. Compact/high type reflows/discloses secondary before shrinking or truncating fardhu.
5. Spoken order is selected time → fardhu once → contextual practice → optional day state.
6. No per-drag live announcement; no duplicate prayer phrasing.
7. Outside overlap, any dual scaffold collapses naturally; no permanent dashboard grid.

Priority boundaries include:

- 05:58 no active fardhu; 05:59–06:19 Subuh primary + zikir secondary; 06:20 zikir primary.
- 13:20 Dhuha; 13:21 Zohor primary + Dhuha secondary; 13:22 Zohor.
- 16:44 no fardhu; 16:45–17:05 Asar primary + zikir secondary; 17:06 zikir primary.
- 19:28 zikir; 19:29 Maghrib primary + zikir secondary; 19:30 Maghrib.
- 20:41 no fardhu; 20:42–21:02 Isyak primary + Al-Mulk secondary; 21:03 Al-Mulk primary.

## Design hypotheses to compare

### Dual-context A: hierarchical pointer stack

One pointer-side group: primary fardhu first line, quieter related practice second line; collapses
to one role outside overlap.

### Dual-context B: pointer primary + stable secondary rail

Fardhu alone owns pointer-adjacent text; contextual practice remains on an upright stable external
rail/strip with restrained relationship.

### Dual-context C: touch-pruned context

During direct scrub, fardhu stays full and contextual becomes compact cue; on settle/detail it
expands into complete subordinate context.

### Clock O: one bounds-safe overlay

One visual numeral run relocates between reserved anchors. It must not distort, cross metadata or
aperture, duplicate, overshoot, or become semantically duplicated.

### Clock F: exclusive role handoff

Stable source/destination clocks use strict fade-through: never both visible, bounded gap, same
semantic owner, direction-independent thresholds, direct destination under reduced motion.

### Mandatory adaptive maximum-type variant

At 2.0×/36dp inset, unchanged header allocation fails both tested clock strategies and all overlap
states require external primary/secondary disclosure in the host proxy. Design must change actual
space allocation: header/aperture height, metadata grouping, type role, or disclosure. It may not
silently exclude, shrink, or clip maximum type.

## Mandatory image-led evidence

Generate separate full-resolution mobile screens, not one board. Keep data, minute, device,
background, and wheel geometry controlled between comparisons.

### Label/hierarchy set

- 05:58, 05:59, 06:19, 06:20;
- 16:44, 16:45, 17:05, 17:06;
- touch and settled states;
- A, B, and C hypotheses;
- difficult celestial/prayer-label competition;
- 360×640 and 390×844;
- at least 1.3×, 1.6×, and adaptive 2.0× composition.

### Clock/transition set

- expansion 25%, 50%, 75%;
- reverse at the same progress;
- detail endpoint;
- overlay and strict handoff;
- adaptive metadata/header arrangements;
- 0/24/36dp inset scenarios where useful;
- normal and reduced-motion endpoint story.

### Judge questions

- Is fardhu the single immediate primary without becoming enterprise CTA/gamification?
- Is related practice retained without equal saffron emphasis or permanent dashboard chrome?
- Does the label have a stable reading origin and unambiguous pointer relation during scrub?
- Are any digits/labels partial, doubled, stretched, baseline-mixed, or aperture-crossing?
- Does overlay travel compete with wheel motion? Does handoff gap read as disappearance?
- Does compact/high type adapt by composition before hierarchy degrades?
- Does every frame feel quiet because meaning is stable, not because information vanished?
- Does wording express time context without implying iqamah or legal prayer-window expiration?

Hard rectangle, hierarchy, content-truth, or ownership failures reject before preference scoring.

## Banned shortcuts

- Equal saffron prayer/context headlines, twin leaders, or permanent two-card dashboard.
- Fardhu represented only by a node while Sunnah gets readable primary text.
- Generic Material cards/pills, glass, decorative Islamic wallpaper, mosque silhouettes.
- Color-only priority; tiny text to force fit; silent truncation; clipped fallback.
- Spring/bounce justified as natural; motion that exists only to feel premium.
- Clamping a clock into a safe rect and treating that as proof the path looks intentional.
- Treating host-font geometry as Android proof or image generation as measurement proof.
- Treating the semantic contract enumerator as application accessibility validation.

## Implementation-phase obligations retained

- Production `TextMeasurer`/`TextLayoutResult` bounds and Android nonlinear scaling.
- Pure ownership, label-placement, visual-clock, quantization, and semantic-state functions.
- Compose UI semantics/instrumented tests where feasible.
- Actual range behavior at 23:55/midnight.
- Settled-only semantics/focus, canceled-effect handling, structural hiding.
- Device frames, interruption/reverse, animator-zero, TalkBack/Switch Access/OEM checks where the
  environment permits; missing device evidence remains conditional, never a fabricated pass.
- Arabic shaping/Bidi/font fallback checks only in ledger states that contain Arabic.
