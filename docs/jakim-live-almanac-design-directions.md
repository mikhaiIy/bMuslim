# JAKIM live almanac — design directions

## Audit

The polished wheel is demo-owned and instantiates synthetic deeds, fixed dates and day states.
Production has the correct live JAKIM state and truth gates but only a basic list. JAKIM supplies
timing entry points, not fabricated prayer-duration windows.

## Direction A — dual-rail official almanac (recommended)

Preserve the existing 24-hour wheel language. Five obligatory entry times use the privileged
filled prayer rail. Imsak, Syuruk and Dhuha use a quieter adjacent takwim rail with hollow nodes and
hairline ticks. The distinction must remain legible without color.

- Glance: validated civil date, actual/selected Malaysia time, zone control, all eight markers and
  an honest next-event readout.
- Scrub: fixed pointer remains the owner. Use point language such as `Masuk waktu Asar` and
  `Penanda Syuruk`; never `aktif` or an invented end time.
- Loading/error: preserve the atmospheric scaffold but suppress all nodes until the complete day
  validates. Never show yesterday under today's label.
- Maximum font: remove radial text and provide one scrollable eight-row almanac key.

## Direction B — single rail

Place all eight events on one rail using filled/large versus hollow/small nodes. This is simpler but
over-equalizes event categories and makes the clustered dawn timings harder to parse.

## Direction C — prayer wheel plus ribbon

Keep five prayer points radial and move Imsak/Syuruk/Dhuha into a synchronized ribbon. This reduces
collisions but makes the explicitly requested auxiliary timing set feel appended.

## Recommended acceptance contract

1. Ready requires exactly Imsak, Subuh, Syuruk, Dhuha, Zohor, Asar, Maghrib and Isyak in canonical
   chronological order. Invalid input renders no timing nodes.
2. A saved zone resolves directly to the polished live wheel; first run retains zone selection.
3. Obligatory and auxiliary markers differ by shape/weight and category semantics, not color alone.
4. No production copy invents prayer activity windows. Use entry/next-event language.
5. All eight values remain reachable at every font scale; maximum type uses an external key.
6. Header date comes from the validated response. No fixed demo/Hijri/day-state copy enters.
7. Zone/day changes clear old data and swap the validated wheel atomically.
8. Midnight/foreground refresh returns to real now; failure never masquerades stale data as today.
9. No demo deeds, citations, rulings, completion UI or synthetic guidance enter production.
10. The guidance truth resolution remains blocked on content/ranking while timings are visible.

Existing visual evidence is sufficient; the open question is truthful data/category integration,
not a new visual concept, so new generated images are not required.
