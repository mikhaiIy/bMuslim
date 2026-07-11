# Fable-5 high: two-view design judgment, round 1

Model invocation: Claude Code CLI `--model fable --effort high`, with read-only access to
`design/two-view/` and the session handoff.

## Verdict

Fable selected **Meridian Key** to advance despite **Aperture** having the slightly higher raw
score. Raw totals:

- Aperture: 753/1000
- Meridian Key: 745/1000
- Almanac Leaf: 523/1000

The reason for overriding the raw total was structural: Aperture's generated transition lets a
paper sheet cover the still-centered dial before the dial moves. That fails the central promise
that one instrument physically resolves into the detail state. Meridian's problems were judged
bounded and correctable without discarding its continuity idea.

Fable requires another design-generation round before native implementation.

## Aperture judgment

Strengths:

- Best trigger: explicit, minute-stamped `Lihat amalan pada 15:00` outside the scrub surface.
- Best TalkBack and motor-accessibility prospects.
- Cleanest 24-hour mapping and glance comprehension.
- Only family with a designed reverse state and explicit `Tutup amalan` action.
- Strong protected identity and sacred-content hierarchy.

Defects:

- In the mid-frame, paper occludes the wheel before the wheel translates/crops.
- Detail keeps too much of a smaller full circle instead of committing to the current crescent.
- The visual result is competent but not an ownable "aperture" yet.
- Compact 360x640 detail was not proven.

Bounded correction: wheel moves and crops first; paper enters only space already vacated; summary
chips fade before the wheel crosses their band; detail uses the existing crescent position.

## Meridian Key judgment

Strengths:

- Strongest proof of one continuous instrument: the saffron meridian links selection, trigger,
  wheel movement, and folio margin.
- Most ownable signature idea.
- Glance teaches the gesture split with `Leret masa · tekan Amalan`.
- Detail geometry is closest to the current native crescent destination.

Defects:

- Generated key reads as a chain/pendant, which is explicitly rejected.
- Mid-transition frame is nearly static and does not prove the movement.
- No reverse/collapse frame.
- Deed-window arcs disappear and the destination drifts into a generic category menu.
- Compact state was not generated.

Bounded correction:

- Dock the key into the 6-o'clock bezel as a keystone tab; no chain or dangling form.
- Label it `Amalan pada 19:45` so its relationship to the selected minute is explicit.
- Reinstate the native deed-window arcs and neutral prayer-node grammar.
- Show a real wheel-first transition, explicit reverse state, and compact stress state.
- Borrow Aperture's summary strip and `Tutup amalan` behavior.

## Almanac Leaf judgment

Rejected. Its wheel barely moves while a leaf covers the bottom of the screen, so it reads as a
generic bottom sheet rather than one resolving instrument. The thin drag seam is an ambiguous
small target, conflicts with scrolling once open, and provides weak accessibility. Its only
useful contribution is compact-screen discipline and proof that a labeled folio boundary can
coexist with the wheel.

## Image-generation artifacts separated from concept defects

- Aperture: inaccurate Hijri date, garbled micro-labels and navigation copy.
- Meridian: contradictory numeral track; time-inconsistent Maghrib prose; garbled nav labels.
- Leaf: duplicate/contradictory Asar state, inconsistent prayer times, stray date and garbage
  header microcopy.

None of these artifacts may enter native model data.

## Fable's synthesis

Advance Meridian's meridian-continuity idea, but use:

- Aperture's minute-stamped explicit trigger;
- Aperture's `Tutup amalan` reverse affordance;
- Aperture's compact three-part summary;
- Leaf's 360x640 stress-test discipline.

Fable additionally suggested testing whether the wheel should rotate as it rises so the selected
minute arrives exactly at the fixed detail pointer. This would make continuity kinematic rather
than merely stylistic, but must not change the selected minute.

## Required round-2 images

1. 390x844 glance with a de-pendanted bezel key and restored deed arcs.
2. True mid-flight frame: wheel translating (and optionally rotating), thread/meridian extending,
   folio entering only vacated space, zero dial/folio overlap.
3. Collapse beat with explicit close affordance and preserved 19:45.
4. 360x640 compact detail with summary, ledger, and navigation.

If the mid-flight frame cannot make wheel-leads/folio-follows obvious, Fable considers that a
concept failure rather than an image-generation failure.
