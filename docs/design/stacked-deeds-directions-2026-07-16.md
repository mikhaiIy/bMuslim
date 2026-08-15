# Stacked deeds design directions

Date: 2026-07-16  
Scope: `productionDebug` research preview  
Status: directions awaiting independent judgment

## Audit

- Coincident event intervals currently overpaint one another at one radius and
  angle; they do not form one grouped glyph.
- Wheel emphasis uses the first visual intersection, while folio options use
  exact semantic intersection. Event tolerance can therefore look selected
  before it is semantically selected.
- Stable lane/ID order is deterministic but presently behaves like a preferred
  deed in the summary.
- At 360×640, semantic lane centers are about 4.8dp apart at regular type,
  3.7dp at large type, and 1.6dp at maximum type while selected strokes can be
  7dp wide.
- The research lane allocator throws when all three semantic lanes are occupied.
- The summary exposes only the first day context; the folio concatenates all day
  contexts rather than giving each a distinct row.
- The existing wheel, pine/paper/saffron palette, serif/monospace hierarchy, and
  glance-to-folio motion are strong and should remain the design system.

Every direction keeps events, semantic windows, personal events, and day
contexts orthogonal.

## Direction 1 — Concentric concordance

The wheel remains a quiet overview; the folio is the complete neutral index.

### Wheel

- Put shared triggers on one event rail near `0.86R`.
- Render one 14×9dp hollow tab containing an upright monospace count.
- Plan semantic lanes between about `0.90R` and `R−3dp` with a 6dp minimum
  center separation: up to three at regular compact size, two at large type,
  and one at maximum type.
- Preserve the existing solid, hollow-bead, striped, and hatched non-color
  grammar.
- Give every exact intersection the same small saffron contact tick; proximity
  alone remains muted.

### Readout and folio

- Prayer/time stays primary.
- Collision copy names counts and temporal dimensions, never the first deed:
  `4 amalan bertemu di sini` or `2 selepas solat · 2 dalam tempoh`.
- CTA: `Buka 4 amalan · 05:59`.
- Open to a ruled concordance ordered by `PERISTIWA`, `TEMPOH`, then `HARI INI`.
- Each deed is a full-width editorial row separated by pine hairlines; every day
  context receives its own narrow ribbon.
- Initial focus stays on the neutral group heading. A deed ledger appears only
  after explicit selection.

### Motion, access, and overflow

- Keep existing open/close durations, reveal thresholds, reduced-motion
  endpoints, and semantic handoff.
- The wheel remains one adjustable node whose state announces time, prayer,
  group count, and every intersecting title.
- Back from a deed returns to its previous row.
- A pure lane planner returns visible assignments plus an overflow group instead
  of throwing. The outermost lane may become one finely hatched aggregate, while
  the folio lists every member.
- Complexity: medium; it changes grouping, lane planning, Canvas primitives,
  research selection, and tests without replacing the shell.

## Direction 2 — Meridian manuscript

Intersecting deeds occupy the wheel's quiet center and the pointer becomes a
manuscript spine.

### Wheel and folio

- Extend the fixed pointer inward as a fine saffron meridian.
- During drag, show only a count. After settlement, show up to three titles
  aligned to the meridian with primitive marks and `dan n lagi` overflow.
- Crossfade the center index into the existing saffron folio spine; the same
  ruled list then becomes navigable.
- Large and maximum type suppress center titles and fall back to the external
  neutral index.

### Tradeoffs

- Strong settled discoverability and narrative continuity.
- High risk from measured center-label collisions, duplicate semantic ownership
  during transition, and additional screenshot/golden burden.
- Complexity: high.

## Direction 3 — Horizon register

The wheel stays almost purely geometric; stacked meaning lives in a restrained
Compose register immediately below it.

### Wheel and register

- Draw one counted event tab, at most two separated windows, and a bundled outer
  binding rule for the rest.
- Readout gives time, prayer, and neutral count.
- A flat hairline-separated register shows one `PADA PENUNJUK` row plus one row
  per day context; rows are not cards, chips, or pills.
- On open, the register becomes the folio header and the complete index continues
  below it.

### Tradeoffs

- Strong Switch Access and large-type behavior because all rows are Compose
  nodes.
- On 360×640 it competes with the learned quiet-grip band and can push the CTA
  below the fold.
- It requires new shell height allocation and shared-surface handoff.
- Complexity: medium-high.

## Orchestrator comparison

| Criterion | Concentric | Meridian | Horizon |
| --- | --- | --- | --- |
| Quiet glance | Strong | Moderate | Strong |
| Group discoverability | Moderate | Strong | Strong |
| 360×640 | Strong | Moderate | Moderate |
| Maximum type | Strong | Moderate | Strong |
| Neutral focus | Strong | Strong after settle | Strong |
| Motion risk | Low | High | Medium |
| Architecture fit | Strongest | Weakest | Moderate |
| Cost | Medium | High | Medium-high |

The orchestrator recommends **Concentric concordance** because it adds honest
grouped geometry without introducing a second glance layout or changing the
proven motion system. This recommendation is not the final design judgment.
