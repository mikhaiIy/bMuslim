# Wave 2: Malaysian temporal-data contract

## Verdict

Official e-Solat exposes observable machine-readable routes, but no supported API contract or reuse
grant was found. Current JSON responses say `no-store`; the only truthful first implementation is
provider-neutral infrastructure with no production provider enabled.

Firecrawl returned HTTP 402; the scout used live official endpoints and primary transport
documentation. No files were edited.

## Findings

1. The live e-Solat zone registry is mutable and the repository's web list is already stale: it has
   `KTN03` where live data publishes `KTN02` and omits `PHG07`. A versioned registry needs
   retired/unsupported states.
   https://www.e-solat.gov.my/index.php?pageId=43&siteId=24
2. An observed JSON route returns schedule fields for today/week/month/year/date, but no linked
   OpenAPI/schema/version/SLA documentation was found. Treat it as implementation detail.
   https://www.e-solat.gov.my/index.php?r=esolatApi/takwimsolat&period=week&zone=WLY01
3. HTTP 200 is not semantic success: invalid requests can yield `NO_RECORD`, HTML, or server errors.
   Validate envelope, exact zone/date, row cardinality, every field, and chronology before use.
4. Prayer and Hijri JSON returned `Cache-Control: no-store, no-cache, must-revalidate`, no ETag, and
   no Last-Modified. RFC 9111 says `no-store` responses must not be stored/reused by caches. Site
   copyright, CORS, export buttons, and iframe support are not a native-app reuse licence.
   https://www.rfc-editor.org/rfc/rfc9111.html#section-5.2.2.5
5. JAKIM data can be updated, but there is no public correction feed, revision ID, or schedule-level
   publication timestamp. Store observation time and payload hash once a licensed source exists;
   atomically supersede changed valid schedules.
6. The community Waktu Solat API explicitly disclaims JAKIM affiliation and is a transport candidate,
   not authority or upstream rights proof.
   https://api.waktusolat.app/docs
7. Prayer schedules and Malaysian Hijri truth are separate. A prayer row's Hijri string or observed
   converter route does not create a correction-aware licensed calendar contract; Hijri-driven
   prompts must fail closed independently.
8. Do not ship the observed direct adapter, durable JAKIM cache, static “Data JAKIM” wording, or a
   live recommendation until supported transport and reuse terms exist.

## Provider-neutral contract

- `PrayerDayKey`: JAKIM zone and civil date.
- `PrayerDaySnapshot`: exact key, explicit schedule zone, zone-registry version, date-bearing prayer
  values, and provenance.
- `TemporalProvenance`: provider ID, source kind, source/rights references, contract version,
  observation/fetch time, and payload hash.
- `SourceContract`: documented transport, persistence/redistribution rights, attribution, fresh and
  stale limits, and correction policy.
- `PrayerDayState`: ready with explicit freshness, setup required, or unavailable.
- `PrayerScheduleSource`, `PrayerTimesRepository`, and pure `PrayerDaySelector` interfaces.

Rules: fully validate before transaction; invalid success never overwrites valid state; persist only
when permitted; exact-key selection only; changed valid hashes supersede atomically; night
calculation requires today's Maghrib and tomorrow's Fajr; Hijri uses a separate repository and
contract.

For the observed direct e-Solat transport, caching and stale fallback are disabled. Generic stale
is allowed only when a licensed contract expressly permits persistence and a same-date stale
horizon, the key matches exactly, the snapshot is complete, and disclosure is explicit. Unknown
policy means stale forbidden.

## Required tests

Cover setup/rights, transport failures, semantic envelope errors, row ambiguity, every field,
chronology, repository concurrency/supersession, fresh/stale selection, midnight and clock/zone
changes, next-day Fajr, independent Hijri failure, corrections, and exhaustive UI mapping of every
sealed-state reason. No failure state may leak sample times, “JAKIM API,” or “best now.”

## Recommended first implementation

Implement contracts with an `UnconfiguredPrayerScheduleSource`, synthetic valid/invalid fixtures,
pure validator/repository/selector/correction tests, explicit setup/unavailable state, an external
link to the official schedule, and a disabled separate Hijri repository. A clearly labelled local
manual import may be explored separately; it must not imply provider approval.

Unresolved dependencies include written endpoint/schema/rate/correction/cache/reuse terms, a
licensed fallback transport, a Malaysian Hijri publication contract, an authoritative zone-change
feed, and a product decision about travel/device-zone behavior.
