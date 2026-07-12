# Execution brief: Production Truth Interlock

## Objective

Make it impossible for bMuslim's existing synthetic religious content and fixed prayer/date/time
data to masquerade as production truth, while preserving the complete current instrument as a
developer-only demo.

## Locked variant matrix

- `demoDebug`: existing timeline prototype and fixtures; application ID suffix; distinct app label;
  visible and semantic demo disclosure.
- `demoRelease`: disabled using Android Components variant configuration.
- `productionDebug`: truth interlock and production NotReady shell; no demo sources/resources.
- `productionRelease`: same truth interlock and shell; no demo sources/resources.

Use one flavor dimension such as `truthMode`. Do not use runtime flags as artifact isolation.

## Source ownership

### Move to `src/demo`

- The entire current fixture-dependent timeline instrument, model, geometry helpers, and sample
  tests unless a file is first made fully truth-neutral.
- Fixed date/time/day-state/sample religious content, citations, Arabic/BM translations, ranking,
  and demo-only resources.
- Preserve current behavior and the 29 existing tests under the demo variant.

### Keep/add in `src/main`

- Theme code only if it contains no fixed claims.
- Truth-domain types, canonical hashing, validators, blocker contract, and `TruthResolution`.
- No sacred/demo fixture strings, WLY01 sample data, fixed Gregorian/Hijri dates, default grade,
  ruling/timing/ranking, or demo-only presentation DTOs.

### Add to `src/production`

- Production app content/entry point and accessible NotReady shell.
- Inputs are deliberately: no content release, temporal provider unconfigured, no ranking.
- No current time/date/Hijri/prayer/deed/citation/grade/progress display.

Variant-specific code may supply the app-root composable called by shared `MainActivity`, or
`MainActivity` itself may be flavor-owned. Choose the smallest compile-safe arrangement.

## Minimal truth domain

Implement only what the design lock requires:

- fixture class and lifecycle enums;
- typed atomic claim with non-empty payload and evidence IDs;
- evidence record;
- claim approval with decision and reviewed fingerprint;
- content release containing ordered claims/evidence/approvals and stored release fingerprint;
- explicit permission states and source contract with contract/terms reference;
- exact prayer-day key, date-bearing snapshot/provenance, and unconfigured/ready temporal state;
- ranking policy and approval bound to canonical ordered candidates, wording, and scope;
- stable ordered blocker enum/set;
- `TruthResolution` with private constructor and the sole public `resolve(inputs)` factory.

Use Java/Kotlin standard-library hashing; do not add crypto or serialization libraries solely for
this slice. Canonicalization must be deterministic and tested. Hashes are integrity checks, not
signatures.

## Validation and blocker behavior

Follow the exact applicability/cascade contract in the design lock. In particular:

- missing/synthetic/non-published content suppresses inapplicable child checks;
- withdrawn maps specifically to withdrawn;
- published content validates non-empty payload, evidence resolution, canonical claim/release
  hashes, and matching approved claim fingerprints;
- unconfigured temporal state suppresses temporal child checks;
- ready temporal state validates terms reference, use permission, exact requested key, and complete
  date-bearing prayer chronology;
- persistence permission is checked only when persistence is requested;
- missing ranking suppresses policy child checks; existing policy validates canonical fingerprint,
  matching approval, candidate membership, exact wording, and scope;
- every ready gate must pass before ready presentation exists.

The blocker result is deterministic and returns all applicable blockers in stable order.

## Production shell

- Semantic heading and initial reading owner: `Panduan belum tersedia`.
- Concise BM-first explanation that verified guidance and schedule data have not been configured.
- No false actions, disabled setup controls, sample values, or motion.
- Readable at maximum font scale and compact height; use simple scroll if necessary.
- The shell is functional state communication, not a visual redesign.

## Demo disclosure

- Distinct launcher/app label such as `bMuslim Demo`.
- Persistent visible text stating that times and content are sample design data, not live guidance.
- Equivalent accessibility semantics; do not rely on color.
- Do not change the wheel/detail interaction beyond the minimum disclosure.

## Tests

### Main/production truth tests

- Every blocker and cascade/applicability rule.
- Canonical claim, release, and ranking fingerprints.
- Payload mutation invalidates approval/release.
- Unknown and denied permissions remain distinct and fail closed.
- Exact temporal requested-key mismatch and incomplete chronology fail.
- No ranking yields no ready/primary result.
- Private resolution construction is enforced by API shape.
- Production app entry resolves NotReady from empty/unconfigured/no-ranking inputs.

### Demo tests

- Preserve the existing geometry, transition, semantics, compact, font-scale, and model tests.
- Add a demo-disclosure state/semantic check where practical.

### Artifact proof

- `demoRelease` task/variant is absent.
- Build `productionDebug` and `productionRelease` without demo imports.
- Inspect production APK contents and extracted DEX/resource strings for a denylist covering known
  fixture IDs, literal Arabic/BM fixture text, `Khamis, 2 Julai 2026`, WLY01 sample citation,
  `AMALAN UTAMA SEKARANG`, demo class/resource names, and prototype app label.
- Prefer a deterministic repository script/test for this inspection if small and maintainable.

## Verification

Run:

```text
./gradlew :app:testDemoDebugUnitTest :app:testProductionDebugUnitTest \
  :app:assembleDemoDebug :app:assembleProductionDebug :app:assembleProductionRelease --no-parallel
./gradlew :app:lintDemoDebug :app:lintProductionDebug :app:lintProductionRelease --no-parallel
git diff --check
```

Adapt task names only as required by the chosen flavor naming.

## Forbidden expansion

No networking, direct e-Solat adapter, Room/DataStore, completion persistence, onboarding, zone
registry, notifications, Hijri logic, Quran content, new religious recommendations, broad UI/UX
redesign, font/palette beautification, or new third-party library unless compilation genuinely
requires it.

## Required report

Return exact files changed, variant/task names, test counts, build/lint/artifact evidence, APK paths,
and any missing device proof. Do not commit or modify unrelated dirty files.
