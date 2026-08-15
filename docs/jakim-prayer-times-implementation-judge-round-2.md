# JAKIM prayer-times implementation judgment — round 2

## Decision

**84/100 — ITERATE**

## Resolved

The compile P0, strict duplicate/missing/wrong-type parsing, redirect/provenance safety, transport
tests, compact stacked actions, current Gradle matrix, artifact scan, and diff checks now pass.

## Remaining P1

**Continuously resumed midnight rollover remains incomplete.** `refreshIfDayChanged()` only runs on
`ON_RESUME`. If the app remains foregrounded across Kuala Lumpur midnight, yesterday's Ready
schedule remains under “Waktu solat hari ini.” Schedule a refresh for the next
`Asia/Kuala_Lumpur` midnight (or an equivalent lifecycle-aware date ticker), immediately clear the
old Ready values, and fetch the new key. A deterministic clock/scheduler test must prove rollover
without an `ON_RESUME` event.

## P2

The production artifact verifier exempts the legitimate shared lowercase `maghrib` JSON field but
still reports all 66 entries as checked. Report the actual checked count plus the explicit shared
schema exemption and guard that exemption from casual expansion.

## Missing non-blocking evidence

- Compact maximum-font-scale device capture.
- Integration proof of the Compose lifecycle-observer wiring.
- Machine-checkable comparison of all 60 zones with a dated official registry snapshot.
