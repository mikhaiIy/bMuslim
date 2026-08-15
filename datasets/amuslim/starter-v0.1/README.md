# aMuslim starter deed dataset v0.1

This directory is a research artefact, not production religious content.

`catalog.draft.json` contains nine conservative candidate deeds with explicit temporal evidence:

- six daily candidates;
- three weekly candidates;
- atomic action and timing claims;
- structured event, named-period, night-segment, and weekday expressions;
- source-specific grading metadata;
- explicit statements of what must not be inferred.

The catalogue is intentionally `IN_REVIEW`, contains no approvals or fingerprints, and lives outside
Android assets. The production app must not parse or package it yet.

## Why JSON before Room

The catalogue is small, immutable, and review-heavy. Versioned JSON is easier to inspect, diff,
fingerprint, and review than a Room database. Room should be introduced later for user completion
state, searchable content packs, or signed catalogue updates. User state and reviewed religious
content must remain separate.

## Publication path

1. Verify every source locator and source-to-claim match.
2. Obtain Malaysian fiqh review for every temporal resolution policy and visible Malay phrase.
3. Obtain translation and content-use approval.
4. Split or revise any disputed composite claim.
5. Canonicalise and fingerprint every atomic claim and the complete release.
6. Add exactly one matching approval per published claim.
7. Transform it into the deed-specific release/runtime domain described in
   `docs/amuslim-starter-runtime-design.md`, reusing the existing fingerprint and approval rules.
8. Only then copy a generated immutable release into the production source set.

## Fail-closed rules

- No raw clock time is stored in the religious catalogue.
- JAKIM prayer points do not prove when a user completed prayer.
- JAKIM's Dhuha marker needs a separately approved religious mapping policy.
- Missing sleep, mosque-event, calendar, or night-boundary inputs remain unresolved.
- Chronological ordering is not religious ranking.
- No entry may be called “best”, “utama”, `Sunnah Muakkadah`, or similar without an approved claim.
- Friday charity is not included as a Friday-specific Prophetic Sunnah in this starter set.
- This research tier admits Sahih reports and named-grader Hasan reports as candidates. Hasan is
  never relabelled Sahih, and every candidate still requires qualified publication review.

Run `scripts/validate-amuslim-dataset.sh` for Draft 2020-12 schema and cross-record checks.
