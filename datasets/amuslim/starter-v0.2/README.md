# aMuslim starter deed dataset v0.2

This directory is a research artefact for the `productionDebug` Concentric Concordance preview.
It is not published religious content and is never packaged into `productionRelease`.

`catalog.draft.json` contains exactly eleven in-review candidates and fourteen evidence records:
the original starter candidates, the post-prescribed-prayer supplication collision, the white-day
lunar-date candidate, and Dhuha variation evidence linked to the existing single Dhuha deed.

The v0.2 timing vocabulary keeps event, semantic-period, weekday, lunar-date, and personal-event
dimensions separate. A JAKIM prayer point is only a schedule proxy; it is not proof that a user
completed prayer. White days require an injected, reviewed Malaysian lunar-date and eligibility
policy. Missing, mismatched, or blocked lunar context remains unresolved, and Dhu al-Hijjah 13 is
blocked by the independently reviewed Tashriq restriction evidence rather than being added as
catalog evidence.

The two Dhuha variation records do not create another deed, clock interval, minimum, maximum, or
extra reward claim. They remain variation evidence for `deed.daily.duha-two-rakah`.

Run `scripts/validate-amuslim-dataset.sh` to validate the Draft 2020-12 schema and cross-record
invariants.
