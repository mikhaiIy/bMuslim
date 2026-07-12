# Design round 2: lock candidate

## Selected direction

Use **product-flavor semantics with build-variant physical isolation**.

```text
demoDebug        → existing design prototype + demo fixtures
demoRelease      → disabled at variant configuration
productionDebug  → truth interlock + production NotReady shell
productionRelease→ truth interlock + production NotReady shell
```

Demo has an application-ID suffix and distinct application label. Both disclosure and app identity
state that it is a design prototype through visible text and accessibility semantics.

## Ownership boundary

### Shared main

- Truth-domain types only: fixture/lifecycle, typed claims, evidence/review binding, exact temporal
  key and snapshot, permission states, ranking policy, blocker set, resolver.
- Pure validators and deterministic resolution.
- Shared visual theme only where it contains no fixed religious/time claims.
- No sample catalogue, Arabic/BM sacred fixture, fixed prayer/date/time, default grade/ruling/timing,
  or sort-derived religious priority.

### Demo

- Existing timeline instrument, `TimelineDeed`, sample data, fixed preview date/time, sample ranking,
  current geometry and interaction behavior, and fixture-dependent tests.
- Visible and semantic disclosure remains present from app launch.
- Demo fixtures are explicitly synthetic design evidence, not reviewed product content.

### Production

- Entry graph supplies no content release, an unconfigured temporal source, and no ranking.
- Accessible `NotReady` shell only. It renders no current time/date, Hijri, prayer name/time,
  religious deed, citation, grade, progress, or hidden/disabled guidance action.

## Minimal domain contract

```text
FixtureClass = SYNTHETIC | PRODUCTION
ContentLifecycle = DRAFT | IN_REVIEW | PUBLISHED | WITHDRAWN
ClaimType = ACT | TEXT | TRANSLATION | TIMING | RULING | VIRTUE | RANKING

AtomicClaim
  id, type, nonEmptyPayload, storedFingerprint, evidenceIds

EvidenceRecord
  id, sourceReference, supportedClaimTypes

ClaimApproval
  claimId, reviewedFingerprint, decision = APPROVED | REJECTED, reviewerId

ContentRelease
  id, fixtureClass, lifecycle, orderedClaims, evidenceRecords,
  claimApprovals, storedReleaseFingerprint

PermissionState = PERMITTED | DENIED | UNKNOWN
SourceContract
  providerId, contractVersion, termsReference,
  usePermission, persistencePermission

PrayerDayKey
  zoneCode, civilDate

PrayerDaySnapshot
  exactKey, scheduleZoneId, dateBearingValues, provenance

RankingPolicy
  orderedCandidateIds, exactWording, scope,
  storedPolicyFingerprint, policyApproval

PolicyApproval
  reviewedPolicyFingerprint, decision = APPROVED | REJECTED, reviewerId

TemporalState
  Unconfigured
  Ready(requestedKey, snapshot, sourceContract)

TruthInputs
  contentRelease?, temporalState, rankingPolicy?

TruthResolution private-constructor
  readyPresentation? | blockers
  companion resolve(inputs): TruthResolution
```

Fingerprints are deterministic integrity values, not signatures or approval proof. Each approval
binds to one exact claim fingerprint. Any payload mutation invalidates that approval and the release
fingerprint. Validators canonically recompute claim, ordered release, and ranking-policy fingerprints
from payloads and compare them to stored/reviewed values; stored hashes are never trusted. Every
claim's non-empty evidence IDs must resolve within the release and its matching approval decision
must be `APPROVED`. A ranking-policy fingerprint covers the ordered candidate IDs, exact wording,
and scope; all candidates must exist in the same approved release. Production content must be
`PRODUCTION + PUBLISHED`; synthetic, draft, in-review, withdrawn, incomplete, or mutated claims are
blockers.

## Deterministic blocker contract

The resolver returns the complete non-empty blocker set sorted by stable enum order:

1. `ContentMissing`
2. `SyntheticContentForbidden`
3. `ContentNotPublished`
4. `ContentIncomplete`
5. `ContentApprovalMismatch`
6. `ContentWithdrawn`
7. `TemporalProviderUnconfigured`
8. `TemporalRightsUnknown`
9. `TemporalUseDenied`
10. `TemporalPersistenceRightsUnknown` only when persistence is requested
11. `TemporalPersistenceDenied` only when persistence is requested
12. `TemporalContractReferenceMissing`
13. `TemporalKeyMismatch`
14. `TemporalIncomplete`
15. `RankingMissing`
16. `RankingUnreviewed`

Production-safe messaging maps the blocker set to one calm top-level status plus technical details
available to tests/logs, not to a stack of alarming messages. No blocker triggers fallback data.

Blocker applicability is deterministic:

- missing content suppresses all content-child blockers;
- synthetic content is evaluated before lifecycle and suppresses claim-level approval checks;
- `WITHDRAWN` yields `ContentWithdrawn` only, not the generic not-published blocker;
- draft/in-review yields `ContentNotPublished` and suppresses claim approval/mutation checks;
- published production content evaluates completeness, canonical fingerprints, evidence resolution,
  and approvals together and returns every applicable blocker;
- unconfigured temporal state suppresses all temporal-child blockers;
- ready temporal state independently evaluates terms reference, use permission, requested-key match,
  and completeness; persistence blockers apply only to an explicitly requested persistence path;
- missing ranking suppresses ranking-child blockers; an existing policy evaluates canonical
  fingerprint, approval, candidate membership, wording, and scope together.

## Entry behavior

```text
Variant entry
  → construct TruthInputs
  → resolve once
  → Ready: reserved for future approved inputs
  → NotReady: accessible production shell
```

`TruthResolution` has a private constructor. Its companion's public `resolve(inputs)` function owns
validation and is the only construction path; successful construction carries a non-null ready
presentation and an empty blocker set, while failure carries no ready presentation and a non-empty
ordered blocker set. UI code can inspect the result but cannot instantiate or subclass it. No
screen derives readiness from a non-empty list, HTTP success, build type, or fixture label.

## Production shell contract

- Initial focus/semantic heading: “Panduan belum tersedia”.
- Concise BM-first explanation that verified guidance and schedule data have not been configured.
- No synthetic religious/time information.
- No disabled buttons or false setup path in this slice.
- Optional build/version diagnostics remain non-religious and non-interactive.
- Static state: no new motion, interruption, or compact-layout risk beyond text wrapping; it must
  remain readable at maximum font scale.

## Test and artifact contract

- Shared truth tests cover every blocker and complete-set ordering.
- Mutation invalidates approval and release.
- No ranking yields no primary recommendation.
- Exact temporal key/permission failure yields no current/next claim.
- Production tests assert the shell contains no prohibited strings/semantics.
- Demo tests preserve existing geometry, motion, focus, compact, and sample-model behavior.
- `productionDebug` and `productionRelease` compile without demo imports/dependencies.
- `demoRelease` does not exist.
- Assembled production APK inspection checks absence of known fixture IDs, Arabic fixture
  and BM normalized literal byte strings, sample dates, WLY01/sample citations, demo
  classes/resources, and prototype label. Fingerprint checks are supplemental because a fingerprint
  need not appear in the artifact.

## Deferred explicitly

Networking, Room, completion, onboarding, live zone registry, provider adapter, rights acquisition,
qualified content review, Hijri, Quran excerpts, notifications, broader UX, and all beautification.
