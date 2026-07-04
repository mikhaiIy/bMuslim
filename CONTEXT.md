# CONTEXT — bMuslim domain glossary

The shared vocabulary for talking about bMuslim. Definitions only — no implementation
detail. When we use these words, we mean exactly this. Update as terms sharpen.

## The day

- **Timeline** — the home screen's core: a horizontal, real-time-scaled view of today.
  Opens centred on the current moment; the user scrubs left/right through time.
- **Needle** — the fixed vertical marker at the centre of the Timeline. It represents a
  single moment in time. Whatever crosses the Needle drives the Detail.
- **Detail** — the area showing every Block the Needle currently crosses: what the user
  could be doing at that moment. Scrolls horizontally when there are several.
- **Planner** — a separate page giving the whole-day / week overview at a glance. The
  Timeline is deliberately "now-focused", so the Planner is where you see everything.

## The things on the timeline

- **Deed** (amal) — a single recommended action (e.g. morning adhkar, Duha, Al-Mulk before
  sleep). The atomic unit of content.
- **Block** — a Deed as drawn on the Timeline: positioned and sized from its Window.
- **Lane** — a stacking row. When Blocks overlap in time they stack into separate Lanes.
  Mandatory Blocks (the five daily prayers) sit in a privileged base Lane.
- **Anytime tray** — a shelf, separate from the Timeline, holding Deeds that have no fixed
  time and can be done whenever during the day.
- **Day banner** — a full-day indicator near the date bar for a state the user is "in" all
  day (e.g. fasting), rather than a point on the Timeline.

## How a deed is timed

- **Anchor** — the event a Deed's timing hangs off: a prayer (Fajr…Isha), a personal event
  (wake, sleep), or a night calculation (last third of the night).
- **Window** — the range of time during which a Deed *may* be done (relative to its Anchor).
  Determines where its Block sits and how wide it is.
- **Act-duration** — roughly how long actually *doing* the Deed takes. Distinct from Window:
  Window = when you *may*; Act-duration = how long it *takes*.

## How a deed is classified (three independent axes)

- **Type** — what kind of act it is: Salah, Sawm, Dhikr, Du'a, Quran, Sadaqah, Adab.
  Drives icon/colour and filtering. Capped at ~7; the long tail folds into Adab.
- **Ruling** — its religious weight: Fardh, Sunnah Muakkadah, Sunnah, Mandub. A fiqh
  classification, so it must be verified like all religious content.
- **Level** — how much we surface to this user: Beginner or Advanced.
- **Render archetype** — how a Deed is drawn, usually implied by its Type:
  - *Point* — a short act done once inside a Window (Salah, Du'a, Zikir). Drawn as a solid
    act-marker inside a faint Window track.
  - *Day-span* — a state held all day; shown as a Day banner, not a Block (Sawm).
  - *Anytime* — no fixed Window; lives in the Anytime tray (general dhikr, Adab).

## Sourcing

- **Citation** — the authenticity proof attached to a Deed: hadith reference + grade,
  Quran reference, and links to the source (sunnah.com) and the relevant fatwa (e-Fatwa).
