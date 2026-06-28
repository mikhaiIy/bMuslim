# PRD: bMuslim v1 — Daily Sunnah Timeline

## Problem Statement

A practising Muslim wants to fill their day with the good deeds (sunnah) that are best for *this particular moment* — the dhikr after Fajr, Duha after sunrise, witr and Surah Al-Mulk before sleep, tahajjud in the last third of the night, fasting on the right days. But:

- Knowing *what* is recommended *when* requires scattered knowledge most people don't have at their fingertips.
- The "right time" for many deeds is tied to the sun and the prayer schedule, which shifts every day and differs by location — so a fixed clock-time list is wrong most of the year.
- Some deeds depend on the Islamic (Hijri) date or the weekday (Monday/Thursday fasting; Ayyam al-Bidh on the 13th–15th), which is easy to lose track of.
- Existing apps focus on prayer times, Quran, or generic reminders; none presents a single living "best deed for right now" timeline.
- Above all, the user does not want made-up or unsourced religious content — every suggestion must be traceable to an authentic source.

## Solution

A daily companion whose home screen is a **vertical, Gantt-style timeline of the user's whole day**. Each recommended deed is drawn as a "best window" bar positioned against *today's real prayer times* (from JAKIM) and the user's own sleep/wake settings, with a "now" marker. The user can tap any deed to read its text (Arabic + Bahasa Melayu/English) and its citation, check it off, and see daily progress. Deeds that depend on the date (Monday/Thursday fasting, Ayyam al-Bidh) appear automatically on qualifying days. Notifications nudge at prayer times and for a few key deeds. Everything works offline, and every suggestion carries a verified citation that deep-links to the source (sunnah.com) and relevant Malaysian fatwa.

v1 is **Android-native** (Kotlin + Jetpack Compose) and targets Malaysia. The Hijri Planner and a broader references engine are deliberately deferred.

## User Stories

1. As a new user, I want to choose my prayer-time zone during onboarding, so that the timeline matches the official times for my location in Malaysia.
2. As a new user, I want to set my usual sleep and wake times, so that deeds anchored to "before sleep" or "after waking" land at the right moment for me.
3. As a new user, I want to pick a level (beginner or advanced), so that I'm shown an amount of practice that fits where I am rather than being overwhelmed.
4. As a new user, I want to choose my language (Bahasa Melayu or English), so that I can read the app comfortably.
5. As a daily user, I want to open the app and immediately see what good deed is best to do right now, so that I can act without having to think or research.
6. As a daily user, I want to see my whole day as a top-to-bottom timeline with each deed's best window shown as a bar, so that I can understand the rhythm of my day at a glance.
7. As a daily user, I want a "now" indicator on the timeline, so that I can see what is current and what is coming up next.
8. As a daily user, I want each deed's window to be positioned against today's actual prayer times, so that the schedule is correct as the times shift through the year.
9. As a daily user, I want deeds that are anchored to the last third of the night (e.g. tahajjud) to be placed correctly, so that I know roughly when to wake for them.
10. As a daily user, I want to tap a deed to see what exactly to do, so that I can follow it correctly.
11. As a daily user, I want to read the Arabic text of a dhikr/dua along with its translation, so that I can recite it properly and understand it.
12. As a user who values authenticity, I want each deed to show its source citation (collection, number, and grade such as sahih), so that I can trust it is not made up.
13. As a user who values authenticity, I want to tap through to the original source on sunnah.com and to the relevant fatwa, so that I can verify the ruling myself.
14. As a daily user, I want to mark a deed as done, so that I can track that I completed it today.
15. As a daily user, I want to see my progress for the day (e.g. 4 of 7 done), so that I feel a sense of accomplishment and consistency.
16. As a daily user, I want my completion state to persist when I close and reopen the app, so that I don't lose my progress.
17. As a daily user, I want completions to reset for each new day, so that the timeline reflects today.
18. As a user observing the sunnah, I want a fasting suggestion to appear on Mondays and Thursdays, so that I'm reminded of these recommended fasts.
19. As a user observing the sunnah, I want a fasting suggestion to appear on the white days (13th–15th of the Hijri month), so that I don't miss Ayyam al-Bidh.
20. As a user, I want to know today's Hijri date, so that I understand which date-driven deeds apply.
21. As a busy user, I want notifications at prayer times, so that I'm reminded to pray even when not looking at the app.
22. As a busy user, I want a few key deed reminders (morning adhkar after Fajr, evening adhkar after Asr, Al-Mulk before sleep), so that I keep up the most important habits.
23. As a user, I want to toggle each notification on or off, so that I control how much the app nudges me.
24. As a user with poor connectivity, I want the timeline and deed content to work offline, so that I can use the app in a mosque or with no signal.
25. As a user, I want prayer times to be cached, so that the app still shows a correct-enough schedule when offline.
26. As a returning user, I want to change my zone, sleep/wake times, level, language, and notification settings later, so that the app keeps fitting my life.
27. As a beginner, I want the advanced-only deeds hidden, so that my timeline stays achievable.
28. As an advancing user, I want to switch to the advanced level, so that I see the fuller set of sunnah practices.
29. As a privacy-conscious user, I want the app to work without creating an account, so that I can use it without handing over personal data.

## Implementation Decisions

- **Single hero feature.** v1 builds only the Daily Sunnah Timeline. The Hijri Planner (multi-month preparation) and a standalone references engine are out of scope.
- **Platform & architecture.** Android-native, Kotlin + Jetpack Compose, MVVM with a repository layer. Local persistence via Room. Async via Coroutines + Flow; UI state via ViewModel + StateFlow. Optional Hilt for dependency injection. (iOS is a separate, later project.)
- **Domain model (ubiquitous language).**
  - **Deed** — the atomic recommended action. Fields: title, optional Arabic text, BM + EN translation, `level` (beginner/advanced), an **anchor + window**, and a **citation**.
  - **Anchor** — what a deed's timing hangs off: `PRAYER(Fajr|Sunrise|Dhuhr|Asr|Maghrib|Isha)`, `PERSONAL(wake|sleep)`, or `NIGHT(last_third)`.
  - **Window** — the best time range relative to the anchor (offset/duration), rendered to concrete start/end times against today's data.
  - **Date rule** — predicate that makes a deed appear only on qualifying days: `weekday ∈ {Mon, Thu}` and `hijriDay ∈ {13,14,15}`.
  - **Citation** — `{ hadithRef, grade, quranRef, sourceUrl, fatwaUrl }`.
- **The TimelineResolver (core seam).** A pure function `resolveTimeline(date, prayerTimes, userSettings, deedCatalog) -> List<ResolvedTimelineItem>` that resolves anchors+windows to concrete times, computes `NIGHT(last_third)` from the Maghrib→Fajr span, applies date rules, and filters by level. It has no Android/network/DB dependencies. All domain risk is concentrated here.
- **Content as data, not code, and not generated.** The deed catalogue is a curated, human-verified, versioned dataset (bundled JSON asset seeded into Room), ~30–50 entries covering one full day plus the date-driven deeds. No LLM produces rulings at runtime. v1 limits itself to widely-agreed, well-known sahih adhkar to keep ruling ambiguity low until a qualified reviewer is engaged.
- **Citation display.** Each deed stores its own verified Arabic + translation text plus a citation line and outbound deep-links (sunnah.com, e-Fatwa). The app does not bulk-import sunnah.com's database (respecting their redistribution terms). Quran text, where needed, comes from a permissively-licensed Quran API and is cached.
- **Prayer times.** Fetched from the Waktu Solat API (JAKIM data, zone-based), wrapped behind a `PrayerTimesRepository` interface so the source can be swapped if the community wrapper breaks. Cached ~24h for offline use.
- **Hijri dates.** Computed locally via `java.time` HijrahChronology (Umm al-Qura) — no network needed — to drive date rules and display today's Hijri date.
- **Notifications.** `AlarmManager` exact alarms for the 5 prayer times plus the key deed reminders, scheduled from cached times so they fire offline; per-notification toggles in settings. `WorkManager` refreshes the prayer cache daily. Standard notification channels.
- **No accounts / no cloud.** All state is local in v1. App is free.
- **Languages.** UI and translations in Bahasa Melayu and English (string resources); sacred text shown in Arabic.
- **UI / UX direction (to be refined during build, not finalised now).** Four screens: (1) Onboarding — zone, level, sleep/wake, language; (2) Timeline/Home — the Gantt day with now-marker, progress, and check-off; (3) Deed detail — Arabic text, translation, citation, source/fatwa links; (4) Settings. Indicative home layout:

  ```
   NOW ↓
  ┌─────────────────────────────────────────────┐
  │ 05:45  ☀ FAJR                                │
  │  ▓▓▓▓░░  Morning adhkar    (Fajr → sunrise)  │ [ref]
  │ 07:05  Sunrise                               │
  │  ░░▓▓    Duha             (~20m → Dhuhr)     │ [ref]
  │ 13:15  ☀ DHUHR                               │
  │ 16:30  ☀ ASR                                 │
  │  ▓▓▓▓░░  Evening adhkar   (Asr → Maghrib)    │ [ref]
  │ 19:25  ☀ MAGHRIB                             │
  │ 20:40  ☀ ISHA                                │
  │  ▓▓      Surah Al-Mulk    (before bed)       │ [ref]
  │ 23:00  🌙 Sleep (your setting)               │
  │  ░░░▓▓   Tahajjud         (last third)       │ [ref]
  └─────────────────────────────────────────────┘
  Today: Thursday · 12 Muharram — sunnah to fast
  ```

  Detailed visual design (colour, type, spacing, component polish) happens iteratively in Compose during the relevant build milestone.

## Testing Decisions

- **What makes a good test here:** assert *external behaviour* — given inputs, the right deeds appear at the right resolved times — not internal implementation details. Tests should not break when internals are refactored.
- **Primary coverage — the TimelineResolver.** Fast JVM unit tests (no emulator). Feed fixtures (sample prayer times + user settings + a fixed date) and assert: each deed resolves to the expected start/end; `NIGHT(last_third)` is computed correctly, including short and long nights; windows that cross midnight behave; the correct date-driven deeds appear on Mon/Thu and on Hijri 13–15 and are absent otherwise; level filtering includes/excludes the right deeds.
- **Date logic.** Unit-test the HijrahChronology conversion + weekday rules against known calendar dates.
- **Repositories behind interfaces.** `PrayerTimesRepository`, `DeedRepository`, and an injectable `Clock`/date provider are faked in resolver tests so the core logic is tested in isolation; the prayer cache/offline-fallback path gets its own focused test (network failure → cached times used).
- **Content audit (not a unit test but a release gate).** Every shipped deed must have a non-empty, manually re-verified citation.
- **UI & notifications.** Light manual/instrumented checks (timeline renders against a known zone's published times; a prayer notification fires at the right time; completion persists across restart) rather than heavy automated UI coverage in v1.
- **Prior art:** none yet — this is a greenfield repo, so these establish the testing patterns for the project.

## Out of Scope

- The Hijri Planner: multi-month preparation and user-planned calendar (e.g. ramping into Ramadan months ahead).
- Per-user adaptive/AI recommendations based on history.
- Full habit analytics: streaks, history charts, long-term stats.
- Per-deed custom reminders (v1 ships a fixed small set of toggleable reminders).
- Accounts, cloud sync, and multi-device.
- iOS app (a later, separate project).
- Qibla finder, full Quran reader, audio recitation.
- Runtime LLM generation of any religious content.

## Further Notes

- **Key risk — content authenticity governance.** The curated database is only as trustworthy as its reviewer. Before any public launch, either engage a qualified reviewer (e.g. a local ustaz) or keep v1 strictly to widely-agreed sahih adhkar. This is the single most important non-technical decision.
- **Key dependency risk — prayer times.** The Waktu Solat API wraps an undocumented JAKIM source and is community-maintained; isolating it behind a repository interface limits the blast radius if it changes.
- **Build order (learning-oriented):** static timeline UI → prayer API + cache → resolver engine → content DB + detail + check-off → date rules → notifications → onboarding/settings/polish/i18n. Each milestone teaches a distinct Android concept.
- This PRD was synthesised from a planning/grilling conversation; the approved plan lives at `~/.claude/plans/woolly-watching-pinwheel.md`.
