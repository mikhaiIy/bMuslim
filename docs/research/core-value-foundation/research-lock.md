# Research lock: Production Truth Interlock

## Status

- Two-wave research cycle complete.
- Independent research judgment: **90/100 — PASS**.
- Orchestrator meta-rating: **38/40**.
- Locked target: **Production Truth Interlock**.

## User problem being solved first

The current application is a polished prototype that presents fixed sample time, date, prayer
schedule, religious content, authenticity grades, and ranking language with production confidence.
The first improvement is to prevent unsupported demo inputs from ever masquerading as current,
official, reviewed product truth.

## Known constraints

1. A confident current recommendation is valid only when all exist:
   - published content whose atomic visible claims have required evidence and review;
   - exact zone/date temporal data permitted by a documented source contract;
   - a reviewed ranking policy for the candidate set and exact wording.
2. Missing any gate yields explicit setup-required or unavailable, never sample fallback.
3. Synthetic fixtures may be used in unit tests and developer/demo previews only.
4. The observed e-Solat JSON surface is not an authorized production integration: no supported API
   contract/reuse grant was found, and current responses include `no-store`.
5. Quran/Hijri/fiqh/ranking production claims remain blocked on their own approvals and review.
6. A content fingerprint proves identity only; it does not prove truth or approval.

## Locked first slice

### Content gate

- Explicit fixture class and content lifecycle.
- Atomic claim types sufficient to distinguish act, text/translation, timing, ruling, virtue, and
  ranking.
- No default grade, ruling, timing, authenticity, or ranking in the truth path.
- Production validation rejects incomplete, mutated-after-review, withdrawn, draft, in-review, and
  synthetic entries.
- Production release assembly contains no synthetic/demo catalogue.

### Temporal gate

- Exact `PrayerDayKey(zone, civilDate)` and date-bearing schedule values.
- Explicit provider/source contract and provenance fields.
- Pure validation and selection with unconfigured/setup/unavailable states.
- No production network provider, persistent e-Solat cache, stale policy, Hijri fallback, or
  JAKIM-official wording in this slice.

### Composition gate

- A resolver may produce a confident primary recommendation only from published content, ready
  permitted temporal truth, and a reviewed ranking policy.
- Otherwise it returns an explicit non-recommendation reason.
- Existing sample UI is identified as demo/developer evidence; it cannot be the production default.

## Required design questions

The design phase is contract-led, not visual-image-led. It must compare a small number of domain and
entry-state architectures and lock:

- where the three gates compose;
- how build variants/source sets segregate demo fixtures;
- the minimum claim/evidence/review schema without overbuilding a CMS;
- exact fail-closed reasons and whether production launches to unavailable or an explanatory shell;
- how existing previews/tests retain sample data without leaking into the release artifact;
- migration path from current `TimelineDeed` without editing the visual instrument unnecessarily.

Visual image generation is out of scope because it cannot answer these contract and packaging
questions. UI beautification remains explicitly deferred.

## Acceptance criteria

- Current citation mismatch and default-grade sample cannot pass production validation.
- No synthetic/draft fixture is packaged in the production runtime path.
- With an unconfigured temporal provider, production resolves no current/next religious claim.
- With no approved ranking policy, no primary/best/utama recommendation is produced.
- Exact zone/date mismatch, incomplete schedule, unknown rights, and invalid state all fail closed.
- Unit tests cover every gate and non-recommendation reason.
- Existing geometry/model tests remain green.
- Debug/demo evidence remains available without being confused with production behavior.
- APK assembly, unit tests, lint, and `git diff --check` pass.

## Banned shortcuts

- Renaming sample data as seeded production data.
- Treating a public URL, CORS, or HTTP success as permission or authority.
- Encoding `Sahih`, ruling, timing, or priority defaults.
- Using a sort order as religious ranking.
- Falling back to WLY01, the device date, Umm al-Qura, yesterday's schedule, or another zone without
  explicit approved policy.
- Shipping sacred synthetic fixtures merely because the screen says demo.
- Building Room/network/notifications before the interlock is proven.

## External release gates retained

- Qualified Malaysian review and ranking/wording policy.
- Source and translation rights.
- Written prayer-provider integration/cache/redistribution/correction terms.
- Malaysian Hijri contract and correction process.
- KDN applicability for Quran excerpts.
- BM-first usability and assistive-technology device validation.
