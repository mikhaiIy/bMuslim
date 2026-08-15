# aMuslim demo-parity research preview — final judgment

Date: 2026-07-15  
Verdict: **PASS — 98/100**  
Scope: separate `productionDebug` research application only

No P0 or P1 findings remain.

## Orchestration record

Research first audited the canonical demo and locked the intended hierarchy. Design required one
wheel, one three-cell summary, and one editorial folio, while retaining the earlier separation of
applicability, validity window, trigger, and focus region. The executor ported those exact shared
presentation primitives into the real JAKIM almanac. The independent judge returned
`REVISE — 46/100` before the port and `PASS — 97/100` after every P0 and P1 finding was closed.
The later centered-readout/action-cleanup refinement received `REVISE — 84/100` for an orphan
transition cutout, then `PASS — 98/100` after the broad-wheel clip was removed and the shared
transition contract was verified.

## Accepted structure

- `ResearchTemporalOccurrence` separately carries applicability, validity window, trigger, focus
  region, stored source expression, and truth class.
- Named periods use continuous demo-style bands with hollow beads and explicit provisional copy.
- The last-third prototype uses a separately typed striped night segment and discloses its
  Maghrib-to-same-schedule-Subuh fallback.
- Friday's final-period prototype uses a hatched search envelope and a terminal diamond at Maghrib;
  it is not represented as a uniform validity window.
- Post-prayer tasbih has exactly five distinct hollow rectangular wheel tabs. They share one deed ID
  while retaining five occurrence IDs, and JAKIM entry times remain schedule proxies rather than
  proof of prayer completion.
- Bedtime deeds remain personal events without a clock coordinate or fake Isyak anchor.
- Monday/Thursday fasting and Friday salawat appear in the third `HARI INI` summary cell only on
  applicable dates; no full-day clock arc is created.
- Personal-event candidates remain in the separate all-nine research library until an honest
  user-event interaction exists.

## Interaction and accessibility

- The contextual wheel readout is horizontally centered above the pointer, including maximum type. Its straight leader and copy share one fade, broad rails are never clipped, and no orphan rectangle remains during open/close.
- The configured expanded action cluster is removed. `Tutup amalan` now leads directly to the retained `Hari ini` / `Perancang` / `Tetapan` navigation; loading/failure zone recovery is unchanged.
- The wheel pointer deterministically selects an intersecting occurrence or the next clock occurrence.
  Stable lane/ID ordering is display ordering, never religious priority.
- The same pointer selection atomically controls wheel emphasis, readout, open action, three-cell
  summary, and the single editorial folio.
- Demo and JAKIM preview call the same shared summary and folio components. The primary Today flow
  has no schedule table, separate legend, miniature linear timeline, personal-event shelf,
  pagination, or nested ledger.
- Text alternatives announce the stored source phrase, visual type, mapping/truth class, and anchor.
- Three contextual lanes remain constrained inside the outer wheel. Maximum font scale removes
  radial labels and retains the shared single-outer-scroll disclosure.

## Final evidence

- productionDebug unit tests: **115 PASS**;
- productionRelease unit tests: **81 PASS**;
- demoDebug unit tests: **95 PASS**;
- demoDebug, productionDebug, and productionRelease lint: **PASS**;
- productionDebug and productionRelease assembly: **PASS**;
- canonical aMuslim dataset validator: **PASS**;
- preview artifact boundary: **PASS**, with the canonical asset and typed temporal prototype present
  in debug and 132 draft-content sentinels absent from release;
- existing production artifact denylist: **PASS** for both APKs;
- emulator at 1080 × 2400: **PASS** for the canonical demo/preview folio hierarchy, the real
  eight-point JAKIM wheel, five hollow tabs, stacked typed bands, atomic scrub update, and 2× reflow.

## Residual P2 hardening

A Compose-level same-frame scrub assertion, pixel-golden Canvas tests, and a physical Samsung S24
Ultra TalkBack/Switch Access session remain optional hardening. They do not block this provisional,
separately packaged debug prototype and do not relax the qualified-review gate for publication.
