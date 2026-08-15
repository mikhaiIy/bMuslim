# Scrollwheel discoverability directions

Date: 2026-07-16  
Status: Quiet Grip selected for bounded implementation by independent design judgment

## Design read

Preservation redesign of a devotional instrument for repeated daily use. The visual language stays
quiet, precise, and product-specific. Working dials: variance 5, motion 3, density 5.

## Shared contract

- The centered readout exposes the exact selected `HH:mm`; a next-event time is explicitly labeled
  as next and never masquerades as the pointer minute.
- The fixed pointer, straight leader, continuous broad rails, devotional hierarchy, pine/saffron/
  paper identity, and complete eight-event maximum-type key remain unchanged.
- Wheel angle, selected minute, readout, emphasis, CTA, and semantics derive from one canonical
  minute in every drag frame.
- No inertia, bounce, per-drag announcement, glass, pill, card, ornamental religious scenery, or
  decorative motion enters the shared shell.

## Direction 1: Quiet Grip

### Model

Drag the wheel field directly. A first-use sentence, `Leret roda kiri atau kanan untuk pilih
waktu`, sits at the lower crop with two restrained saffron grip notches on the outer rail. It
retires only after a real horizontal scrub succeeds.

The Malay teaching sentence is provisional pending language review; that review does not reopen
the selected geometry or interaction ownership.

### State behavior

- Rest: exact selected time and instruction are visible.
- Touch: instruction softens; the fixed pointer/leader gains restrained weight only after contact.
- Drag: the wheel rotates under the stationary pointer; there is no inertial continuation.
- Settle/reverse: commit the represented minute and reverse immediately from current geometry.
- Transition: the cue leaves with wheel ownership and returns only if still unlearned at settled
  glance.
- Reduced motion: static instruction and stroke feedback remain; no teaching animation.
- Compact/maximum: give the instruction its own measured line; never shrink it over the key.

### Human-craft reason and risk

The cue teaches the actual object at the actual grip point, then gets out of the way. A persistent
instruction would feel patronizing, so retirement must follow demonstrated success rather than a
tap or elapsed time.

### Acceptance

- At least 8/10 uncoached first-time participants scrub within 10 seconds.
- At least 9/10 report that the wheel moves and the pointer stays fixed.
- Touch without horizontal slop neither changes the minute nor retires the cue.
- Same-frame tests keep minute, angle, readout, emphasis, CTA, and semantics atomic.

## Direction 2: Bring to the Meridian

### Model

Tap a visible wheel position to bring that moment to the fixed pointer, then keep contact to drag
for fine adjustment. A small target notch marks the pointer; first-use copy says `Sentuh waktu pada
roda untuk bawa ke penunjuk`.

### State behavior

- Accept taps only inside a measured annulus; a temporary halo marks the touched moment.
- Recenter through the shortest declared angular path; the readout shows interpolated minutes,
  never the destination before geometry reaches it.
- Retarget from current geometry. Reduced motion snaps to the identical endpoint.
- At maximum type, labels may yield under existing policy while ticks, shapes, readout, and full
  external key remain.

### Human-craft reason and risk

The user points to a moment and the instrument brings it to the reading meridian. Accidental large
time jumps and midnight ambiguity are the risks; annulus ownership and a deterministic direction
policy are mandatory.

### Acceptance

- At least 8/10 participants select two requested times without coaching.
- Pure tests cover every annulus angle, midnight, equal-distance policy, retarget, and interruption.
- Contacts outside the annulus never change time.

## Direction 3: Readout Crown

### Model

The fixed readout is the scrub handle. Two hairline grips flank the unboxed readout inside an
invisible 48dp target; first-use copy says `Leret masa dipilih`.

### State behavior

- Touch compresses the grips and weights the straight leader.
- Drag keeps readout and pointer fixed while every rail, tick, marker, and interval rotates below.
- Touch outside the crown does not scrub. The crown disables immediately when settled wheel
  ownership ends.
- Maximum type expands the handle with the external readout bay without consuming the key band.

### Human-craft reason and risk

This resembles setting an instrument through its crown and makes result and control colocated.
Users may expect the text to follow the finger, so the first-use cue and fixed-object response must
carry the concept.

### Acceptance

- At least 8/10 discover the handle within 10 seconds and 9/10 identify the displayed time as the
  pointer minute.
- The full target remains in bounds at 360x640 and 2x type.
- Touch outside the handle never scrubs; reversal has no stale title or dead zone.

## Adjunct challenger: Precision Focus

### Model

Press and hold inside the wheel to reveal a local fine-scale fan at the pointer, then drag to adjust
by the flavor's minute step. First-use copy says `Tekan dan leret untuk laras tepat`.
This is an optional adjunct challenger to a primary scrub model, not an equal fourth primary
direction and never the only accessible adjustment path.

### State behavior

- Rest shows only three local precision ticks.
- Hold reveals the local fan without changing time; early movement cancels the hold mode.
- Precision drag updates scale and readout atomically; settle clears the fan.
- Reduced motion shows/hides instantly. The fan must stay inside measured wheel bounds at compact
  and maximum type.

### Human-craft reason and risk

Detail appears only at the point of attention, like a loupe on a measuring instrument. Long press
is less discoverable and less motor-accessible, so it remains a challenger and never the only
accessible path.

### Acceptance

- At least 7/10 discover precision mode from the instruction and 9/10 reach an arbitrary requested
  production minute within two attempts.
- Early movement cannot trigger a delayed mode switch.
- TalkBack keeps ordinary adjustable progress and previous/next-event actions.

## Provisional recommendation

Implement the shared exact-selected-minute readout contract plus **Quiet Grip** as one bounded
experiment. It directly targets discoverability while preserving the current geometry and input
model. Do not combine the tap, crown, or long-press models into the same experiment. Compare those
only if Quiet Grip misses the comprehension thresholds.
