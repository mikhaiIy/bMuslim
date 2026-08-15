# Stacked deeds independent design judgment

Date: 2026-07-16  
Scope: `productionDebug` research preview  
Judge: Sol/xhigh, read-only

## Decision

Select **Direction 1 — Concentric concordance**, with two corrections:

1. The counted event tab adapts with type scale instead of remaining 14×9dp.
2. Overflow depicts only the union of real member coverage; it never invents a
   generic or full-day interval.

The direction scored 89/100, versus 64/100 for Horizon register and 56/100 for
Meridian manuscript. It is the only direction that preserves temporal truth,
neutral focus, the established wheel-to-folio motion, and the observed 360×640
maximum-font geometry.

## Dataset decision

Include schema/catalog v2 in this wave. Create `starter-v0.2` rather than
mutating v0.1. The implementation must contain exactly 11 entries and 14 catalog
evidence records, including the two admitted deeds and Dhuha variation evidence.
Abi Dawud 2418 stays external eligibility-policy evidence rather than being
misclassified as ACT/TIMING catalog evidence.

## Execution contracts

- Group prescribed-prayer proxies by date, prayer slot, and marker minute, but
  keep independent deed, occurrence, and completion identities.
- Replace first-item selection with a neutral concordance containing exact event
  groups, exact semantic windows, all applicable day contexts, and separate
  untimed personal events.
- Event ±10-minute tolerance is visual proximity only. Semantic membership and
  contact emphasis require exact marker equality.
- Stable folio order is `PERISTIWA`, `TEMPOH`, `HARI INI`, then
  `PERISTIWA PERIBADI`; stable occurrence ID is the only within-section order.
- No deed title owns the summary before explicit row selection.
- Add an injected lunar context that fails closed when missing, mismatched, or
  Dhu al-Hijjah 13. Never infer Hijri dates or fasting clock geometry.
- Add one flavor-neutral counted event glyph at `0.86R`; use 14×9dp regular,
  16×12dp comfortable/large, and 18×14dp maximum.
- Plan semantic lanes deterministically between `0.90R` and `R−3dp`, with at
  least 6dp center separation and budgets 3 regular, 2 comfortable/large, and 1
  maximum. Return overflow instead of throwing.
- The outer overflow binding draws only actual coverage union. The folio and
  accessibility output retain every original member and truth class.
- Keep the paper/pine/saffron editorial system, official JAKIM point geometry,
  quiet-grip/return controls, motion timing, and release folio unchanged.
- Keep research content, types, fixtures, and assets absent from
  `productionRelease`.

## Required validation

- Strict v2 catalog validation: 11 entries, 14 evidence records, four new
  evidence IDs, variation links, one Dhuha deed.
- One two-member glyph after every prescribed-prayer proxy, with distinct
  occurrence/completion keys.
- Exact-versus-proximity behavior at marker ±1 minute.
- Fajr multi-dimension collision and Friday-after-Asr neutral exposure.
- Missing lunar context, Monday/13, Friday/14, and blocked Dhu al-Hijjah 13.
- Overflow at regular, large, and maximum type without throwing or narrowing the
  folio/accessibility result.
- 360×640 captures at 1×, 1.5×, and 2× for collision, proximity, overflow,
  concordance, ledger, and focus return.
- One wheel accessibility node, deterministic row focus, existing semantic
  handoff/reduced-motion endpoints, and production release isolation.

## Explicit exclusions

No publication, generic Hijri converter, notifications, prayer-completion
detection, completion persistence, bedtime candidates, extra post-prayer deeds,
religious ranking, palette redesign, navigation redesign, or motion retiming.
