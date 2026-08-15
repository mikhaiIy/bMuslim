# Concentric concordance implementation notes

Date: 2026-07-16  
Scope: `productionDebug` research preview

The accepted wave stays inside the existing almanac shell. The shared renderer
still receives `List<AlmanacInterval>` so the paper/pine/saffron geometry,
JAKIM point geometry, quiet grip, return controls, motion timing, and release
folio do not need a second implementation. Research-only metadata is carried
by interval member IDs, member titles, coverage segments, and group keys; the
full neutral concordance remains in the debug research plan.

Three boundaries are deliberate:

- Lunar dates are never derived by the wheel. `ResearchLunarDateContext` is
  injected with a policy ID and fingerprint, and the default is `Missing`.
  Missing, mismatched, unresolved, and blocked contexts produce a day ribbon,
  never fasting clock geometry. Dhu al-Hijjah 13 is blocked by the preview
  policy even when a context is supplied.
- When semantic lanes overflow, the outer aggregate is the union of the real
  member coverage segments. `occurrenceCoverage` keeps each member's exact
  coverage available to pointer concordance, so the aggregate cannot turn a
  gap or another member's segment into an exact claim.
- A grouped prescribed-prayer glyph shares only the visual proxy. Its deed,
  occurrence, completion, evidence, and ledger identity remain independent.
  A completion key records the identity contract; this wave does not infer
  prayer completion, persist completions, or add notifications.

The research catalog and all preview UI remain in the `productionDebug`
source set. `productionRelease` keeps the unchanged release folio and receives
neither the v0.2 draft asset nor the research types/content.

## Final runtime proof

- The production-debug Compose device regression opens a semantic deed row,
  sends system Back from its ledger, asserts that the ledger is gone while the
  neutral concordance remains, and verifies focus on the exact originating
  occurrence row. The row owns an explicit focus target; a detached requester
  is discarded before the folio is recomposed.
- A 360×640, 1× runtime capture at the live JHR02 Fajr marker shows one counted
  shared-event glyph and the neutral `4 amalan` collision copy: two post-prayer
  event members plus two semantic periods. Its opened folio repeats the same
  four-member count without assigning ownership to the first deed.
- Final automated evidence: 149 production-debug unit tests, 102 production-
  release unit tests, 110 demo unit tests, and one production-debug emulator
  test pass. Dataset validation and both APK isolation audits also pass.
