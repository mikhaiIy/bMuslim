# aMuslim temporal visualization design agreement

Date: 2026-07-15  
Status: design lock for the `productionDebug` structural prototype

## Agreement

The bMuslim wheel restores the demo's third semantic rail and overlap-aware stacked deed marks. It does not force every deed into a clock interval. The authoring structure keeps four orthogonal concepts separate:

1. `applicability`: which civil/religious date context applies;
2. `validityWindow`: a resolved or provisional clock range;
3. `trigger`: a user/schedule event and its `AT`, `AFTER`, or `BEFORE` relation;
4. `focusRegion`: a broad search envelope that is not uniformly available or precise.

Every prototype mapping carries its source expression separately from app-created geometry and identifies a truth class: `DIRECT`, `POLICY_MAPPED`, `PROVISIONAL_RESEARCH`, `SCHEDULE_CONTEXT_PROXY`, `EVENT_REQUIRED`, or `UNRESOLVED`. Unreviewed geometry is allowed in this debug design prototype, but it is never presented as published, active, due, ranked, or religiously approved. Ulama review remains a future publication gate.

## Visual grammar

| Meaning | Wheel/folio primitive | Required disclosure |
| --- | --- | --- |
| Reviewed clock window | Solid rounded arc and solid bead | mapped policy/source |
| Provisional named period | Continuous rounded arc and hollow bead | `ANGGARAN STRUKTUR · BELUM DISAHKAN` |
| Prayer-completion trigger | Hollow rectangular tab aligned outside each prayer node | schedule proxy; prayer entry does not prove completion |
| Personal event | Research-library entry with `APABILA`/`SEBELUM` relation | no clock position; do not fake a wheel coordinate |
| Applicable weekday | Third `HARI INI` summary cell | visible only on matching day |
| Computed night segment | Continuous striped cross-midnight arc | formula and provisional boundary policy |
| Unresolved search region | Continuous hatched envelope with terminal emphasis | envelope is not the whole response-time claim |

The clock rail supports at most three contextual lanes for this starter set. Repeated trigger tabs do not consume interval lanes. Personal events and day contexts never consume clock lanes. Shape and copy—not colour alone—communicate type.

## Starter mapping

- Three protective surahs: two provisional continuous occurrences with hollow beads, `Subuh→Syuruk` and `Asar→Maghrib`.
- Post-prayer tasbih: five independent hollow tabs beneath Subuh, Zohor, Asar, Maghrib, and Isyak. The source wording remains `selepas setiap solat`; these five are displayed JAKIM contexts, not a narrowing of the report or proof of completion.
- Dhuha two rakaat: provisional continuous `JAKIM Dhuha→before Zohor` prototype with a hollow bead.
- Bedtime protective surahs: unpositioned personal-event row, `AT INTEND_SLEEP_AT_NIGHT`.
- Last-third dua/istighfar: provisional cross-midnight last-third calculation based on Maghrib→next Subuh; if adjacent-day data is unavailable, the prototype must identify its proxy/fallback and may not call it approved.
- Witr before sleep: unpositioned personal-event row, `BEFORE INTEND_SLEEP`.
- Monday/Thursday fasting: weekday ribbon on Monday/Thursday only. A fasting clock span requires a separately sourced mapping and is not inferred merely from weekday evidence.
- Friday salawat: Friday ribbon only; no full-day clock arc.
- Friday final-period dua: Friday-only provisional Asar→Maghrib search envelope with terminal emphasis, not a claim that the whole interval is the response time.

## Interaction and accessibility

- The wheel readout is centered above the fixed pointer with a straight vertical leader. Its text and leader fade together during disclosure; it never cuts a rectangular hole from the wheel rails.
- The configured folio proceeds directly from `Tutup amalan` to the canonical bottom navigation. Source, zone-change, and research-library action buttons do not appear in this expanded Ready flow; loading/failure zone recovery remains available.
- The wheel pointer owns contextual selection. An intersecting occurrence is selected by stable lane and occurrence ID; otherwise the next occurrence around the clock is shown. This is display ordering, never religious ranking.
- The readout, open action, three-cell summary, and single folio update atomically from that pointer selection. Five post-prayer tabs share one deed ID but retain distinct occurrence IDs.
- The expanded view uses the demo composition: cropped wheel, `RINGKASAN SAAT INI` three-cell strip, and one warm-paper editorial folio. It has no separate legend, miniature timeline key, schedule table, personal-event shelf, day ribbon, or nested bar-card ledger.
- The folio announces title, source timeframe phrase, visual/mapping status, occurrence anchor, evidence reference, and recorded assessment. The non-colour wheel primitive plus accessibility description provides the temporal alternative.
- Personal-event candidates remain in the separate nine-candidate research library until an honest user-event interaction exists. Applicable weekday candidates appear in `HARI INI` only on their schedule-zone date.
- At maximum font scale, wheel geometry remains available without on-wheel labels and the shared outer-scroll disclosure remains reachable.

## Research basis

The research lane used the existing hadith catalogue plus Malaysian official/context sources: JAKIM e-Solat fields; Mufti Wilayah material on Dhuha, night division, Friday after-Asar discussion, and fasting boundaries; and JAKIM MyHadith Friday salawat material. Temporal uncertainty remains encoded without relying on colour through hollow beads, stripes, hatching, terminal markers, and explicit copy, while W3C complex-visual guidance requires a complete textual alternative.

## Publication holds

Qualified review is still required for hard morning/evening boundaries, adopted Dhuha integration, adjacent-day night calculation, Friday religious-day boundary, prayer-completion scope, fasting eligibility and boundaries, final-Friday narrowing, Malay copy, and source/content-use rights.
