# JAKIM live almanac — implementation judgment round 1

## Decision

**82/100 — ITERATE.** No P0.

## P1

1. `actualNowMinute` advances but `selectedMinute` remains frozen while the header still says
   `Sekarang`; wheel accessibility always says `Masa dipilih`. Synchronize selected time on every
   tick only while not scrubbed/dragging, and prove follow → scrub freeze → return-to-now behavior.
2. Selected and next radial labels can collide in the dawn cluster. Syuruk and Dhuha are only
   6.25 degrees apart. Add deterministic bounds-aware placement that keeps the selected label and
   suppresses/relocates the next when bounds intersect or clip; keep the full key canonical.

## P2 accepted for correction

- Add a visible filled/hollow two-item legend for obligatory entry times versus takwim markers.
- Remove the ineffective claim that a local scrub survives an atomic Ready→Loading refresh; truth
  replacement resets to now.
- Keep artifact exemptions limited to unavoidable raw-schema collisions (`dhuha`, `maghrib`).

## Verified strengths

Strict eight-value parsing/chronology, five-prayer derivation, category separation, no invented
windows, no post-Isyak wrap, atomic refresh, continuous-midnight behavior, truth gating, demo
isolation, tests, APKs, lints and artifact controls otherwise pass.
