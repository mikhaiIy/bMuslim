# Stacked deeds research lock

## Status

- Research completed on 2026-07-16 with two independent Sol/xhigh scouts and a
  separate Sol/xhigh synthesis pass.
- Scope: research-only `productionDebug` prototype data and interaction design.
- Publication status: **IN REVIEW — NOT FOR RELEASE APP DISPLAY**.
- Temporal dimensions stay orthogonal: a shared event, overlapping semantic
  periods, and coincident day contexts are not interchangeable.

## Admit as new candidates

### Supplication after prescribed prayer

- ID: `deed.daily.post-prayer-help-in-worship`.
- Evidence: [Sunan Abi Dawud 1522](https://sunnah.com/abudawud:1522), displayed
  `Sahih (Al-Albani)`.
- Source timing: after every prescribed prayer.
- Runtime expression:
  `AFTER_USER_EVENT(PRAYER_COMPLETED, prayerScope=PRESCRIBED_ONLY)`.
- Collision: shares a prayer-completion event with the existing post-prayer
  tasbih, but keeps a separate occurrence and completion identity.
- No clock geometry: a JAKIM prayer time remains a labelled schedule proxy, not
  proof that the user completed prayer.
- Do not infer voluntary-prayer scope, an expiry minute, ordering between deeds,
  or unapproved formula text.

### Fast the white days

- ID: `deed.monthly.fast-white-days`.
- Evidence: [Sunan Abi Dawud 2449](https://sunnah.com/abudawud:2449), displayed
  `Sahih (Al-Albani)`.
- Source timing: lunar dates 13, 14, and 15.
- Runtime expression: `FASTING_LUNAR_DATES([13,14,15])` with
  `RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED`.
- Collision: may share a day with the existing Monday/Thursday fast or Friday
  context, but must remain a separate deed and completion identity.
- Fail closed without an approved, injected Malaysian lunar-date context.
- Day 13 of Dhu al-Hijjah must remain blocked or unresolved because the Tashriq
  restriction is independently evidenced by
  [Sunan Abi Dawud 2418](https://sunnah.com/abudawud:2418).
- Do not create a full-day clock interval, derive Hijri dates from a generic
  converter, infer fasting boundaries, merge intentions, or claim extra reward.

## Merge as variation evidence

Keep one Dhuha deed, one interval, one occurrence, and one completion identity.

- [Sahih Muslim 719a](https://sunnah.com/muslim:719a): action variation for four
  rak'ahs and sometimes more; it does not replace the locked two-rak'ah action,
  establish a required minimum, or define a maximum.
- [Sahih Muslim 748b](https://sunnah.com/muslim:748b): qualitative timing
  variation involving sun-heated ground; it must not become minutes after
  sunrise, weather data, temperature data, or a JAKIM Dhuha mapping.

## Hold or reject

- Hold otherwise valid bedtime and waking candidates until the event model can
  represent clusters without assigning clock bars: Bukhari 2311, 5362, 6312,
  5009, and 1154; Abi Dawud 5055.
- Hold additional post-prayer deeds after the first collision is proven: Abi
  Dawud 1523 and Muslim 591.
- Hold Muslim 588b until an in-prayer-phase model exists; never map it to prayer
  completion.
- Continue the existing Friday holds and Friday-charity exclusion.
- Reject the weak Tirmidhi 586 candidate, the nightly al-Waqi'ah claim under the
  admission policy, the source-spliced “al-Mulk alone before bed” claim, an
  every-night claim derived from Bukhari 183, and a “last-third prayer” label
  derived from Bukhari 1146.

## Stacking contract

1. A shared event renders as one event glyph with a neutral item count, not
   multiple indistinguishable tabs on the same lane.
2. Coincident deeds retain independent IDs, evidence, detail, and completion.
3. Semantic intervals may occupy separate concentric lanes; their order is a
   stable display order, never a religious ranking.
4. Day contexts render as multiple ribbons or rows, never fake `00:00–23:59`
   intervals.
5. Pointer selection exposes every semantically intersecting item and preserves
   visual-type and truth-class labels.
6. Overflow is grouped, not rejected, when more semantic items exist than the
   wheel's visual lane budget.
7. Accessibility names every item in the group and provides deterministic
   navigation without relying on color or radius alone.

## Permitted prototype changes

- Draft schema/content bump to v2 / `0.2.0-draft` with 11 entries and 14 evidence
  records.
- Add `variationEvidenceIds`, `prayerScope`, `LUNAR_DATE_SET`,
  `RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED`, `FASTING_LUNAR_DATES`,
  `REQUIRES_REVIEWED_CALENDAR_AND_ELIGIBILITY_POLICY`, and `MONTHLY_CONTEXT`.
- Add an injected `ResearchLunarDateContext` with policy ID, fingerprint, and
  explicit state.
- Group event proxies by a shared trigger/proxy key while preserving deed
  occurrence IDs.
- Render all day contexts; `firstOrNull()` must not be the sole visible or
  accessible representation.
- Keep all changes and UI-only fixtures outside `productionRelease`.

## Required scenarios

- One prescribed-prayer completion shows two neutral deed options through one
  event visual, with independent completion.
- Friday after Asr exposes both existing semantic items without ranking either.
- Fajr remains stable when a shared prayer-event group intersects the morning
  estimate and last-third boundary.
- Dhuha stays one card after evidence variation is added.
- Missing lunar context leaves white days unresolved.
- Injected Monday/13 and Friday/14 contexts expose every applicable day ribbon.
- Dhu al-Hijjah 13 fails closed.
- Release artifact verification proves the draft and fixtures are absent.
