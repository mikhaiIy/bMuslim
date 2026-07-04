# ADR 0001 — Home screen: a time-scaled horizontal "playhead" timeline

- **Status:** Accepted (structure firm; visual/interaction details still evolving)
- **Date:** 2026-06-28

## Context

bMuslim's core job is to answer, at a glance: *what is the best good deed to do right now?*

The original PRD (Issue #1) assumed the home screen was a **vertical Gantt list** of deeds
with a flat resolver output (`List<ResolvedTimelineItem>`). A brainstorming session revealed
a stronger concept and surfaced requirements the flat list can't serve:

- Deeds are timed in three different ways (prayer-anchored, personal-event-anchored,
  night-calculation) and shift daily — already handled by anchors/windows.
- Deeds overlap in time (a long adhkar window can span several prayers), so they must be
  able to **stack**.
- Deeds are not all the same shape: some are short acts done inside a window, some are an
  all-day state (fasting), some have no fixed time at all.
- Deeds vary along more than one dimension — *what kind* of act, its *religious weight*, and
  *how much* to show a given user.
- Every deed must carry an authentic citation (unchanged).

## Decision

**The home screen is a horizontal, real-time-scaled Timeline with a fixed centre Needle
driving a Detail area.**

1. **Timeline** — horizontal, drawn to real time-scale (gaps preserved). Opens centred on
   the current moment; the user scrubs left/right through the day.
2. **Needle** — a fixed marker at the centre representing a single instant. The Detail shows
   every Block the Needle crosses (see [CONTEXT.md](../../CONTEXT.md)).
3. **Three independent classification axes** replace the single "level":
   - **Type** (Salah, Sawm, Dhikr, Du'a, Quran, Sadaqah, Adab) — drives icon/colour/filter.
   - **Ruling** (Fardh, Sunnah Muakkadah, Sunnah, Mandub) — religious weight; verified content.
   - **Level** (Beginner, Advanced) — how much we surface.
4. **Render archetype** governs how a deed is drawn, usually implied by Type:
   - *Point* → solid act-marker inside a faint Window track.
   - *Day-span* → a Day banner near the date bar, not a Block (e.g. fasting).
   - *Anytime* → the Anytime tray, not positioned on the Timeline.
5. **Stacking** — overlapping Blocks stack into Lanes; the five daily prayers are pinned to a
   privileged base Lane so they are always present within their time range.
6. **Resolver contract changes** — it produces a laid-out day (positioned + laned Blocks),
   not a flat list. Lane assignment is the standard overlapping-interval layout problem.
7. **Whole-day overview is deferred** to a separate **Planner** page; the Timeline stays
   deliberately "now-focused".

## Consequences

**Positive**
- Matches the user's mental model of a day that flows ("now → next → after").
- A genuine differentiator: no competitor centres a now-focused deed scrubber.
- The engine stays clean — the UI is a *presentation* of the anchor/window model, so the
  home screen can change later without touching the resolver.
- Opening the app immediately answers "what now?" (the Needle starts on the current moment).

**Costs / risks**
- The home screen is a harder first build than a vertical list (horizontal scroll +
  snap-to-block + needle-synced detail + lane layout).
- **Open question:** should a Block's width equal its *Window* or its *Act-duration*?
  Unresolved; the visual hinges on it.
- Stacking many Lanes on a short strip risks clutter — likely needs a Lane cap + overflow.
- `Ruling` is a fiqh classification, so it inherits the "who verifies content" risk.

**Reversibility** — moderate. The interaction model is hard to change once built, but because
it is decoupled from the engine, we can swap the home screen without redoing the domain logic.

## Alternatives considered

- **Vertical Gantt list (original PRD).** Simpler to build and a better whole-day overview,
  but weaker "immerse me in now" feel and doesn't express stacking/time-scale as naturally.
  Retained in spirit as the separate Planner page.
- **"Now" hero card + up-next list.** Minimal and glanceable, but hides the day and loses the
  sense of time and duration.
- **Agenda grouped by prayer.** Familiar checklist, but loses the visual sense of time.
