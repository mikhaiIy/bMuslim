# aMuslim research preview execution brief

Date: 2026-07-15  
Status: implemented for `productionDebug`; excluded from `productionRelease`

## Outcome

The canonical `amuslim.starter` `0.1.0-draft` catalogue is rendered inside the real JAKIM prayer-times app as a read-only research preview. The launcher entry is available before or after zone selection. Clock-compatible candidates use the demo wheel's stacked temporal rails and five repeated post-prayer tabs. The wheel pointer drives the readout, open action, demo three-cell summary, and one demo editorial folio as a single contextual selection. Personal-event candidates remain in the separate research library because they have no honest phone-clock coordinate. This debug-only visualization does not change truth resolution, notifications, completion state, ranking, or current-time behavior.

The installable preview is intentionally a separate application:

- application ID: `com.bmuslim.app.debug`
- launcher label: `bMuslim Research Preview`
- APK: `app/build/outputs/apk/production/debug/app-production-debug.apk`

The normal release remains `com.bmuslim.app` and contains no draft catalogue, preview implementation, deed IDs, unique Malay copy, evidence IDs, or evidence URLs.

## Orchestrated design decision

The architecture, UX, and religious-content safety reviews agreed on these boundaries:

1. Package the canonical JSON only in `productionDebug`.
2. Parse it with an exact, fail-closed debug-only parser.
3. Present all candidates with equal visual weight, grouped as six daily-context and three weekly-context candidates.
4. Keep the `IN_REVIEW`, `UNREVIEWED`, empty-approval state visible and enforce it in the parser.
5. Keep source timeframe phrases separate from provisional geometry. Continuous draft bars use hollow beads, stripes, hatching, terminal markers, and explicit structural-estimate copy; hollow prayer tabs are schedule proxies; personal events receive no clock coordinate.
6. Provide evidence collection/reference, recorded assessment, and canonical source links, but no full copied hadith text.
7. Leave the existing production truth interlock unchanged: official prayer timing data cannot unlock unreviewed guidance.
8. Make `productionRelease` use a no-op variant bridge and prove the boundary by inspecting both APKs.

## Runtime structure

- `src/production`: generic entry/destination hooks integrated with the existing JAKIM UI.
- `src/productionDebug`: real preview bridge, strict parser, repository, temporal plan, stacked timeline UI, label, and generated canonical asset.
- `src/productionRelease`: no-op preview bridge only.
- `src/testProductionDebug`: canonical and negative parser/repository tests.
- `scripts/verify-amuslim-preview-artifacts.sh`: debug positive-control and release negative-control APK audit.

Malformed, oversized, partial, unexpectedly expanded, or non-review catalogues fail as a whole and display a neutral unavailable screen. No subset is rendered.

## User experience

The entry is labelled `Pustaka penyelidikan · 9 calon`. The inline Today prototype follows the demo composition:

- full wheel in glance and the same cropped wheel in the expanded state;
- provisional stacked clock bands and exactly five post-prayer event tabs;
- a three-cell `RINGKASAN SAAT INI · DRAF` strip for next prayer, pointer-selected/next deed, and applicable day context;
- one warm-paper editorial folio with draft action, source timeframe phrase, mapping disclosure, reference, and recorded assessment;
- no prayer schedule table, separate legend, miniature linear key, personal-event shelf, weekday ribbon, nested timeline card, checkbox, streak, reminder, recommendation, or due state.
- no configured-folio source, zone-change, or research-library action stack; Close leads directly to the bottom navigation.

Scrubbing updates the contextual presentation atomically. Stable lane/occurrence ordering is an interface rule, not a recommendation or religious priority.
The glance readout is centered above the pointer. Broad wheel rails remain continuous beneath it, and the label reservation is released as soon as disclosure begins, so no empty rectangle survives the fading copy.

The separate full-screen library shows all nine candidates and is not labelled as today's list.

System Back and the visible back action restore the existing JAKIM screen state.

## Verification

Passed locally:

- dataset structural validation;
- productionDebug, productionRelease, and demo unit tests;
- productionDebug and productionRelease assembly;
- demoDebug, productionDebug, and productionRelease lint;
- existing production artifact leakage audit;
- 115 productionDebug, 81 productionRelease, and 95 demoDebug unit tests;
- preview artifact audit with the byte-identical debug asset and 132 draft-content absence checks against productionRelease;
- emulator installation and navigation at 1080 × 2400 at normal and 2× font scale, including demo/preview folio comparison and an atomic scrub from an intersecting evening range to the next post-prayer occurrence.

The debug and release APKs build side by side because their application IDs differ.
