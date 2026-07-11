# Locked two-view native execution brief

## Design lock

The selected direction is the **Meridian–Aperture hybrid**.

- Cold launch opens in a centered full-wheel glance state.
- A labeled action outside the scrub region reads `Amalan pada HH:mm`.
- The same wheel moves upward and is aperture-cropped into the existing detail crescent.
- Summary, ledger, close control, and navigation enter only after the wheel vacates space.
- Collapse and system back reverse the scene.
- Expansion contributes exactly **0°** of angular rotation.

Locked references:

- `design/two-view/round-2/r2-1-glance.png`: glance composition and trigger separation.
- `design/two-view/round-2/r2-3-detail.png`: region ordering only; native content wins.
- `design/two-view/round-2/r2-4-collapse.png`: reverse intent.
- `design/two-view/round-2/r2-5-compact.png`: region budget only; ignore form rows/four tabs.

Round-3 is generator-failure evidence and is not a locked visual reference.

## Protected invariants

- One `selectedMinute` source of truth survives expand/collapse.
- Horizontal drag only scrubs time and never expands.
- No tap-anywhere or prayer-node expansion.
- Current prayer/deed priority, completion state, palette, orbit, ledger data, and three-tab nav
  remain.
- No generated prayer times, rulings, Arabic, translation, citation, or completion state enters
  native code.
- One renderer and one detail tree; never crossfade duplicate wheels/screens.

## State and motion

Use a saveable `HomePresentation { Glance, Detail }` with cold-launch default `Glance` and one
normalized progress owner:

- `0f`: full centered glance
- `1f`: current detail state
- expand: 620ms
- collapse: 460ms
- geometry easing: `CubicBezierEasing(.2f, 0f, 0f, 1f)`
- no spring, bounce, overshoot, or angular interpolation
- interruption reverses from current progress
- disabled system animation snaps geometry and visibility to destination

Suggested reveal segments:

### Expand

- 0–400ms: wheel translation and aperture crop
- 40–280ms: header compaction/detail-only metadata
- 210–420ms: context surface enters only below wheel clip
- 280–460ms: summary text
- 330–560ms: ledger
- 390–540ms: close seam
- 470–620ms: navigation

### Collapse

- 0–100ms: navigation/deep ledger semantics deactivate
- 60–180ms: ledger and summary retreat
- 110–300ms: paper/content field clears downward
- 120–460ms: aperture opens and wheel center returns to glance
- 300–460ms: open action returns

## Geometry

Use one coordinate system and interpolate wheel center and aperture clip. Do not fake cropping by
drawing paper over the wheel.

Detail endpoints remain:

- regular viewport 220dp
- compact viewport 182dp
- local center Y = viewport height × .93
- existing hour/prayer/deed/celestial radius grammar

Glance endpoint:

- full outer wheel fits with at least 8dp clip clearance and 16dp horizontal screen margins
- optical center around 50–54% usable height on regular screens
- all five prayer names/times visible
- current active deed label, arcs, beads, actual-now mark, fixed pointer, and celestial body visible
- at least 16dp gap before the 48dp open action
- no summary, ledger, count, or navigation

At progress .6, tests must show center above glance center, meaningful lower crop (about 25–35%),
and detail content at or below visible clip bottom.

## Trigger and keystone

The six-o'clock keystone is code-native bezel architecture only: flush trapezoid/notch, optional
fine saffron meridian rule, no click semantics, no chain/pendant/string form.

One separate 48dp action outside the scrub canvas:

`Amalan pada HH:mm`

Semantics include selected time plus active/next deed context and `Role.Button`.

In detail, provide a 48dp `Tutup amalan` control with downward/neutral seam mark. System back
collapses before leaving.

## Accessibility

- Glance semantics: header, adjustable wheel, open action only.
- Detail semantics: header, wheel, summary, ledger, close, navigation.
- Hidden content must be absent/invisible to accessibility, not merely alpha zero.
- Open completion: focus/announce summary and `Paparan amalan dibuka untuk HH:mm`.
- Close completion: return focus to open action and announce `Paparan amalan ditutup`.
- Do not move focus on scrub.
- Reduced motion snaps while retaining focus/announcement sequence.

## Compact behavior

Reuse `maxHeight <= 700.dp`.

Glance 360×640:

- complete wheel, five prayer labels/times, 48dp open action, no bottom nav
- preserve readability at 1.3× font scale

Detail:

- 182dp instrument
- ≤58dp context strip
- current open prose ledger, not generated key:value form
- current three-tab nav, not generated four-tab nav
- visible 48dp close action above system inset
- ledger remains scrollable and completion reachable

## Tests

Add pure JVM coverage where possible:

1. progress 0 clip contains complete outer wheel;
2. progress 1 equals current 220/182dp detail geometry;
3. progress .6 center moved upward and lower portion is clipped;
4. every sampled progress keeps detail content below clip bottom;
5. expansion angular offset is always zero;
6. reveal order is wheel → summary → ledger → nav;
7. compact controls/navigation fit usable height;
8. selected minute is unchanged by presentation toggles;
9. all existing model tests pass.

Add stable tags for wheel, open action, close action, summary, ledger, and navigation.

## Required previews

- 390×844 glance at 15:00
- 390×844 detail at 19:45
- 360×800 glance
- 360×640 detail at 1.3× font scale
- before-dawn glance

## Verification

```bash
JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:testDebugUnitTest :app:assembleDebug --no-parallel

JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:lintDebug --no-parallel

git diff --check
```

Report missing emulator/device evidence honestly. Do not convert missing evidence into a visual
pass or a proven defect.
