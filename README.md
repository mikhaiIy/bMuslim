# bMuslim

A Muslim daily-sunnah companion app. It answers one question well: **what is the best good deed to do right now?** — rendered as a Gantt-style daily timeline anchored to your real solat times, with every suggestion backed by an authentic, cited source.

> **v1 scope:** the **Daily Sunnah Timeline** only. The Hijri Planner and a full Verified-References engine are planned for later. Android-native first (Kotlin + Jetpack Compose); iOS later. Market focus: Malaysia (JAKIM prayer times, Bahasa Melayu + English).

## How the timeline works

Each **deed** (e.g. morning adhkar, Duha, Surah Al-Mulk before bed) is **anchored** to an event and given a best-**window**:

- `PRAYER(Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha)` — live from the JAKIM prayer-times API
- `PERSONAL(wake | sleep)` — from the user's settings
- `NIGHT(last_third)` — computed from Maghrib → Fajr

The app renders each window against today's real times, so the schedule bends to the seasons and your location automatically. **Date rules** surface extra deeds on qualifying days (Mon/Thu sunnah fasting; Ayyam al-Bidh on 13–15 Hijri).

## Principles

- **Authenticity first.** Content is a curated, human-verified, cited database — never LLM-generated rulings. Each deed stores Arabic + BM/EN text, a citation (collection + number + grade), and deep-links to [sunnah.com](https://sunnah.com) / [e-Fatwa](https://efatwa.muftiwp.gov.my/).
- **Offline-first.** Prayer times cached; deed content bundled in-app.
- **No accounts, no cloud** in v1. Free.

## Tech

Kotlin · Jetpack Compose · MVVM · Room · Retrofit · WorkManager/AlarmManager · `java.time` HijrahChronology.

Prayer times: [Waktu Solat API](https://api.waktusolat.app/) (JAKIM data).
