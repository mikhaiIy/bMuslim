# Shared almanac UI — implementation judgment round 3

## Decision

**95/100 — PASS.** No P0/P1.

Every visible measured label now uses one deterministic priority/bounds/readout-exclusion pipeline
with no fallback painting. Maximum font has the complete external key. Demo overlap-aware interval
lanes, canonical 06:00/19:29 celestial inputs, and regular/compact/maximum summary branches are
restored. Wheel speech remains flavor-owned.

Both flavors use `com.bmuslim.app.ui.almanac.SharedAlmanacShell`. Production maps exactly eight
point-only JAKIM values with five primary and three secondary roles, empty intervals, non-circular
selection and no demo guidance leakage.

Fresh evidence: 73 production and 89 demo tests, all enabled APKs and lints, both artifact controls,
obsolete-identity checks and `git diff --check` pass.

Non-blocking gap: no physical-device/Compose instrumentation evidence for TalkBack focus, reverse
during motion, 360×640, maximum-font reachability or zone replacement during motion.
