# JAKIM live almanac — implementation judgment round 2

## Decision

**93/100 — PASS.** No P0 or P1 remains.

The live Malaysia clock follows until scrub ownership begins; scrubbing freezes selection and
return-to-now resumes following. Header and wheel semantics use the same ownership state. Runtime
radial labels use measured bounds, clamp the selected label and suppress an intersecting next
label; deterministic dawn-cluster tests cover compact/default geometry through font scale 1.79.
The complete eight-row key remains canonical and maximum type suppresses radial text. A visible
filled/hollow category legend and category/name/time semantics are present.

Atomic refresh clears stale values and resets the replacement schedule to actual now. Artifact
exemptions are limited to unavoidable raw JAKIM fields `dhuha` and `maghrib`.

Non-blocking evidence gap: physical-device/Compose screenshot and TalkBack verification at compact
height and maximum font scale. Optional polish: make the legend's hollow glyph reproduce the
wheel marker's small visual notch.
