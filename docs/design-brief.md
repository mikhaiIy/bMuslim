# bMuslim — Home Screen Design Brief

A copy-paste brief for generating a visual mockup of the home screen with an AI design tool
(e.g. Claude, asking for an HTML/CSS artifact). See [CONTEXT.md](../CONTEXT.md) for the
vocabulary and [docs/adr/0001-home-screen-timeline-model.md](./adr/0001-home-screen-timeline-model.md)
for why the screen is structured this way.

## How to use it
- Ask for a **single self-contained HTML+CSS file** of the **home screen only** — one screen
  at a time.
- **Attach `CONTEXT.md`** so it uses our terms.
- Ask for **light + dark**, and the **bottom sheet in both peek and expanded states**.
- Iterate: get v1, then refine ("make the timeline the dominant element", "calmer", etc.).
- Optionally attach 1–3 screenshots of apps whose *feel* you like.

## The brief

```
Design the HOME SCREEN of "bMuslim", an Android (Material 3) daily-sunnah companion
app for Muslims in Malaysia. Output a single self-contained HTML+CSS file I can preview,
showing the screen in BOTH light and dark mode, portrait phone size. Label everything in
Bahasa Melayu (this is for a Malaysian user).

CONCEPT
The home screen is a horizontal, real-time-scaled TIMELINE of today's recommended good
deeds, anchored to real prayer times. It opens centred on the current moment ("now").
A fixed dashed vertical NEEDLE sits at the horizontal centre. The user scrubs the timeline
left/right; a bottom sheet shows the detail of whatever the needle is over.

LAYOUT (top to bottom)
1. Date bar: Gregorian + Hijri date (e.g. "Khamis, 28 Jun 2026 · 12 Muharram 1448").
2. Day banner (only when relevant): an all-day state, e.g. fasting today ("Puasa sunat").
3. Timeline (the hero): drawn to real time scale (gaps between events are preserved).
   - Each deed is a BLOCK positioned by its time window.
   - Overlapping blocks STACK into lanes.
   - The 5 daily prayers are pinned to a base lane and always shown in their time range.
   - A fixed dashed NEEDLE at centre; a small "Kembali ke sekarang" (back-to-now) button.
4. Bottom sheet (peeking, expandable): shows the deed(s) under the needle.
   - Peek: title + time + checkboxes.
   - Expanded: Arabic text + Malay translation + citation (source + authenticity grade)
     with a link icon.

HOW TO SHOW EACH DEED (3 attributes)
- TYPE (use a small icon): Salah (prayer), Sawm (fasting), Dhikr, Du'a, Quran, Sadaqah, Adab.
- RULING (use colour/weight, strongest = most prominent): Fardh > Sunnah Muakkadah > Sunnah > Mandub.
- A short deed is a small solid marker sitting inside a faint "window" track (the window =
  the period you may do it; the marker = the act).

SAMPLE DATA (Kuala Lumpur, a Thursday — use these exactly)
Prayer times: Subuh 05:59, Syuruk 07:11, Zohor 13:21, Asar 16:45, Maghrib 19:29, Isyak 20:42.
Assume "now" = 15:00. Deeds:
- Zikir pagi — Dhikr, Sunnah Muakkadah — window Subuh→Syuruk.
- Solat Dhuha — Salah, Sunnah — window ~07:30→Zohor.
- 5x Solat fardhu (Subuh/Zohor/Asar/Maghrib/Isyak) — Salah, Fardh — short blocks at each time.
- Zikir petang — Dhikr, Sunnah Muakkadah — window Asar→Maghrib.
- Baca Surah Al-Mulk — Quran, Sunnah — window Isyak→tidur (23:00).
- Solat Tahajud — Salah, Sunnah Muakkadah — last third of the night (~03:30–05:30).
- Puasa sunat Khamis — Sawm, Sunnah — show as the Day banner (item 2), not a timeline block.

VISUAL DIRECTION
Calm, modern, spiritual — NOT cliché (avoid mosque silhouettes, heavy gold filigree, crescent
clichés). Green + subtle gold accent palette. Beautiful, legible Arabic typography. Generous
spacing, soft rounded cards, gentle shadows. Must feel serene, not busy. Follow Material 3.

DELIVERABLE
One HTML file. Show: the full home screen with the sample data, the stacked timeline with the
needle at 15:00 (between Zohor and Asar), the fasting day banner, and the bottom sheet in both
peek and expanded states. Provide light and dark side by side (or a toggle).
```
