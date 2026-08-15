# JAKIM live almanac — locked execution brief

## Locked direction

Implement the existing bMuslim almanac visual language as a production-safe **dual-rail official
timing instrument**. Five obligatory entry times use the dominant filled prayer rail. Imsak,
Syuruk and Dhuha use a quieter adjacent takwim rail with hollow/notched nodes. Shape and weight,
not color alone, distinguish categories.

## Data contract

- Extend the strict JAKIM parser to require exactly one record containing `date`, `imsak`, `fajr`,
  `syuruk`, `dhuha`, `dhuhr`, `asr`, `maghrib`, and `isha` as strings.
- Reject duplicates, missing/wrong types, wrong zone/date, malformed values, non-OK status,
  non-single record, or non-chronological canonical order.
- Add a provider-owned eight-slot almanac model. Do not misclassify auxiliary markers as
  `PrayerSlot`. Derive the existing five-value `TemporalState.Ready` from the same validated set so
  existing truth gates stay intact.
- Carry the full validated schedule through repository success and production Ready state.
- Keep timetable responses memory-only and persist only the user's selected zone.

Canonical order: Imsak, Subuh, Syuruk, Dhuha, Zohor, Asar, Maghrib, Isyak.

## Screen contract

- A saved zone resolves directly to the live instrument; first run retains explicit zone choice.
- Header uses the validated civil date, selected HH:mm, `Sekarang` or `Masa dipilih`, and a visible
  zone/area action. Do not show an unvalidated Hijri date.
- All eight nodes are always present in Ready. Only the selected and next marker may have radial
  labels, preventing dawn-cluster collisions. A persistent, reachable eight-entry almanac key shows
  every name and HH:mm.
- Filled larger nodes represent Subuh/Zohor/Asar/Maghrib/Isyak. Hollow/notched smaller nodes
  represent Imsak/Syuruk/Dhuha. Semantics announce category, name and time.
- The fixed pointer owns selected time. Horizontal scrubbing switches the header to
  `Masa dipilih`; a separate actual-now mark remains. `Kembali ke sekarang` resolves from the live
  `Asia/Kuala_Lumpur` clock.
- Readouts use point language: `Masuk waktu`, `Solat berikut`, `Penanda takwim`. Never use `aktif`,
  invent a duration/end time, or add rakaat/recommendation/completion copy.
- Do not circularly wrap next-event language. After Isyak, show `Jadual hari ini selesai`; tomorrow
  may appear only from a separately validated snapshot.
- Loading/error may retain the atmospheric scaffold but shows no timing nodes/values. Zone/date
  replacement is atomic. Midnight/foreground refresh clears old data, resets the replacement to
  actual now, and never labels stale data as today.
- At maximum font scale, suppress radial text and use one vertically scrollable eight-row key.
  All actions remain at least 48dp and scroll-reachable on compact height.
- Keep a restrained `Panduan amalan belum tersedia` disclosure. Prayer readiness must not unlock
  content/ranking guidance.

## Architecture boundary

- Create a production-safe official almanac model and renderer; do not import or adapt
  `TimelineDeed`, `sampleDeeds`, fixed demo dates, deed arcs, rulings, citations, day states,
  completion controls or demo disclosure.
- Reuse the existing palette, concentric geometry, fixed pointer and scrub interaction as visual
  language, either through truth-neutral primitives or a production-only renderer.
- Keep demo behavior and tests unchanged.

## Acceptance evidence

- Parser/adapter tests cover all eight fields, every duplicate/missing/wrong-type case, date/zone,
  chronology and five-prayer derivation.
- Instrument pure tests cover geometry, category distinction, selected/next without day wrap, and
  actual-now reset behavior. Add state tests for full schedule propagation and atomic refresh.
- Preserve lifecycle/midnight, transport, truth and demo tests.
- Run demo/production unit tests; demo, production debug and production release assemblies; all
  three lints; demo positive-control and production negative artifact scans; `git diff --check`.
