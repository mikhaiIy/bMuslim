# Quiet Orbit UI execution brief

## Goal

Refine the bMuslim home instrument so dense JAKIM timings—especially Imsak, Subuh,
Syuruk, and Dhuha—remain exact and discoverable without competing for the same few
degrees of radial label space. Preserve the 24-hour orbit and stacked deed windows.

## Evidence lock

- The current wheel maps four minutes to one degree. A ten-minute Imsak–Subuh gap is
  therefore only 2.5 degrees, so direct `name + HH:mm` labels cannot fit truthfully at
  phone scale.
- Daily cyclic data benefits from a radial overview, but controlled studies found a
  linear 24-hour display faster and more accurate for exact reading. The selected
  direction therefore keeps the radial overview and adds a small linear precision
  surface only for the focused neighborhood:
  [Waldner et al.](https://arxiv.org/abs/1907.13534).
- Focus-plus-context can improve complex small-screen calendar tasks without losing
  the surrounding day:
  [Microsoft Research DateLens](https://www.microsoft.com/en-us/research/publication/datelens-a-fisheye-calendar-interface-for-pdas/).
- Empirical visualization work supports decluttering the overview and explicitly
  focusing a smaller annotated subset:
  [Declutter and Focus](https://pubmed.ncbi.nlm.nih.gov/33760737/).
- Calm interfaces should move information between the periphery and the center of
  attention while demanding the smallest practical amount of attention:
  [Calm Tech Institute principles](https://www.calmtech.institute/calm-tech-principles).
- Android requires readable contrast, scalable content, meaningful semantics, and at
  least 48dp touch targets for interactive controls:
  [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps).

## Selected direction: Quiet Orbit + Horizon Rail

The orbit shows the whole day; the meridian speaks one moment.

1. Keep every official JAKIM point at its exact angle.
2. Keep the five obligatory prayers as the strongest stable anchors.
3. Render Imsak, Syuruk, and Dhuha as distinct secondary shapes rather than forcing
   persistent time strings into the dawn arc.
4. Keep deed intervals on the undistorted outer lanes.
5. Use a screen-fixed horizon rail for nearby exact timings. Around dawn it presents
   Imsak, Subuh, Syuruk, and Dhuha in chronological order without moving the source
   points.
6. Let the fixed readout own the selected time and its exact meaning. The CTA must
   name the same selected or active occurrence; future occurrences must be described
   as future, never as active at the pointer.
7. At large text scales, reflow exact timings into the focus rail/chronological
   alternative instead of shrinking or dropping all core information.

## Visual system

- Deep mineral pine sky, warm paper, ivory type, and one restrained saffron focus
  accent.
- Name-only radial prayer labels; exact times use tabular figures in the fixed
  readout or focus rail.
- Hairline orbit geometry, distinct filled/outlined marker shapes, no glass cards,
  decorative mosque silhouettes, generic dashboard widgets, or purple-blue
  gradients.
- Calm comes from progressive disclosure and stable hierarchy, not from empty space
  around a noisy dial.

## Generated concepts

- `design/review/quiet-orbit-concept-v1.png`: boundary-label / dawn-constellation
  exploration. Useful for material and palette direction; rejected as the primary
  information architecture because the fan is diagrammatic and the generated
  occurrence arithmetic is not authoritative.
- `design/review/quiet-orbit-horizon-rail-v2.png`: selected direction. The static
  text is visual reference only; implementation must use live model data.

Both concepts were generated with the built-in image-generation tool in `ui-mockup`
mode. The first prompt requested a premium Android “Quiet Orbit” with an expanded
dawn constellation. The second prompt retained its mineral-pine editorial language,
set the focused example to 06:20, and requested an undistorted wheel plus a linear
05:30–08:00 horizon rail containing Imsak, Subuh, Syuruk, and Dhuha.

## Hard acceptance conditions

- No morning label collisions at compact, regular, or 200% text.
- Exact point angles and interval duration geometry remain unchanged.
- Current, selected, active, and next meanings do not contradict one another.
- Official timings and provisional deed windows remain distinguishable without
  color alone.
- Every official timing remains immediately discoverable with TalkBack and through
  a visible chronological alternative.
- No release build exposes debug-only research content.

