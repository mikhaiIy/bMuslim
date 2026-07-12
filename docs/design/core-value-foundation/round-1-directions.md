# Design round 1: Production Truth Interlock

## Purpose

This design round is architectural and state-contract-led. Visual generation cannot answer the
material questions: truth-gate composition, source-set segregation, release packaging, and
fail-closed behavior. The existing visual instrument is protected and beautification is deferred.

## Protected invariants

- Current visual geometry and interaction tests remain intact.
- Sample religious content is preserved only as developer evidence, never production truth.
- Main production code has no implicit grade/ruling/timing/ranking defaults.
- A confident recommendation requires content, temporal, and ranking gates.
- Production without approved inputs returns an explicit non-recommendation state.
- No network, database, notifications, Hijri rules, or completion system enters this slice.

## Direction A: build-type hard wall

### Idea

Treat debug as the existing interactive design laboratory and release as the production truth path.
Move every sample catalogue and sample schedule behind `src/debug`. Define shared gate contracts in
`src/main`; implement a release entry point in `src/release` with an unconfigured provider and no
published catalogue.

### Flow

```text
Debug APK → debug fixture factory → existing TimelineScreen
Release APK → release truth factory → Unavailable(no approved content/provider/ranking)
```

### Contract

- `src/main`: claim, lifecycle, temporal, ranking, validation, resolution, and non-recommendation
  types; existing visual component accepts explicit data.
- `src/debug`: synthetic fixture release and the current sample timeline.
- `src/release`: empty published catalogue, unconfigured temporal source, no ranking.
- Release packaging test/inspection proves sample identifiers and Arabic fixture text are absent.

### Strengths

- Strongest simple physical segregation.
- Preserves current design lab with little UI churn.
- Makes release behavior impossible to confuse with debug through runtime flags.

### Risk

Using debug as “demo” can become limiting when production-debug testing is later needed. A future
product flavor may be required.

## Direction B: explicit demo/production product flavors

### Idea

Create `demo` and `production` flavors, each supporting debug/release build types. Demo packages the
existing sample experience with permanent demo disclosure; production packages only approved
inputs or unavailable states.

### Flow

```text
demoDebug/demoRelease → demo fixtures → existing instrument
productionDebug/productionRelease → truth interlock → ready or unavailable
```

### Strengths

- Demo versus production is a product dimension, not a compiler/debug dimension.
- Allows production-debug instrumentation later.
- Clear artifact naming and distribution.

### Risk

- Adds variant and CI complexity early.
- Every dependency/resource/test task multiplies.
- More Gradle surface than the first interlock needs.

## Direction C: one runtime graph with fixture classification

### Idea

Keep all data in main code. A validator marks releases `SYNTHETIC`, `DRAFT`, or `PRODUCTION`; the
runtime resolver refuses non-production truth while a hidden developer toggle enables demo.

### Flow

```text
One APK → runtime mode → validator → demo renderer or production resolver
```

### Strengths

- Small Gradle change.
- Exercises the same graph in all modes.
- Simple preview/test injection.

### Risk

- Synthetic sacred content is still packaged in production.
- A flag, deep link, reflection, or configuration defect can expose demo as product truth.
- Fails the research lock's strongest release-segregation criterion.

## Shared minimal domain design

```text
ContentRelease
  id, fixtureClass, lifecycle, fingerprint, claims

ContentClaim
  id, type, exactText?, evidenceIds, reviewIds, fingerprint

PrayerDayKey
  zoneCode, civilDate

PrayerDaySnapshot
  key, scheduleZoneId, date-bearing prayer values, provenance

SourceContract
  providerId, documented, persistencePermitted, rightsKnown

RankingPolicy
  id, candidateIds, exactWording, reviewIds

TruthInputs
  contentRelease?, prayerDayState, rankingPolicy?

TruthResolution
  Ready(primary, alternatives, provenance)
  NotReady(reason)
```

The first implementation may use a deliberately narrow schema, but must distinguish lifecycle,
fixture class, atomic claim type, content fingerprint, exact temporal key, source contract, and
ranking presence. It must not pretend to implement a complete editorial CMS.

## Shared fail-closed reasons

- `NoPublishedContent`
- `SyntheticContentForbidden`
- `ContentIncomplete`
- `ContentMutatedAfterReview`
- `ContentWithdrawn`
- `TemporalProviderUnconfigured`
- `TemporalRightsUnknown`
- `TemporalKeyMismatch`
- `TemporalIncomplete`
- `RankingUnreviewed`

Every reason has a deterministic test and production-safe message. No reason falls back to sample
data.

## State behavior

- Production launch: resolve once from the three truth inputs.
- Not ready: show a calm non-recommendation shell describing that verified guidance is not yet
  available; no prayer/deed time or religious content is rendered.
- Debug/demo: retain the current wheel and detail as a labelled design prototype. Demo status is
  semantically owned and visible without relying on color.
- Back/reverse/accessibility behavior of the existing instrument is unchanged.
- Reduced motion and compact behavior are unchanged because no new transition is introduced.

## Evaluation request

Judge which direction best satisfies the research lock with the least irreversible complexity.
Specifically challenge whether build-type segregation is sufficient, whether flavors are justified
now, and whether any main-code fixture can meet the release no-leak requirement.
