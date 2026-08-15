# Shared almanac UI — implementation judgment round 1

## Decision

**72/100 — ITERATE.** No P0.

## P1

1. The shared shell is a materially simplified rewrite, not a faithful extraction of the original
   demo UI. It drops the visible readout/leader, hour numerals, keystone, measured negative-space
   clipping, adaptive label/five-name policy, sun craft, multi-stop palette, adaptive summaries,
   active-deed index and full LivingLedger composition. Port the original primitives and adaptive
   folio frame from the pre-refactor source into parameterized shared code; preserve flavor-owned
   claims. Prove parity at the established fixture moments and compact/maximum states where tooling
   permits.
2. Label collision helpers are disconnected from drawing. The actual renderer directly draws
   previous/next labels, and maximum type has no replacement key. Use measured candidates in the
   draw path, selected-over-next priority and an external maximum-font key with all events.

## P2

- Inject flavor-owned wheel state descriptions instead of hardcoding `Masa dipilih` and duplicating
  exact/readout speech.
- Add Compose interaction evidence if the current stack permits; otherwise retain reducer tests,
  rerun current lints and explicitly record missing device focus/max-font/TalkBack evidence.

## Confirmed strengths

Both flavors use the canonical class; production maps eight point-only values with a 5/3 split and
no intervals/guidance; truth boundaries, live/key state, motion timing, Back/focus intent, no-wrap,
loading clear, source/zone actions, tests and artifact identity controls are structurally sound.
