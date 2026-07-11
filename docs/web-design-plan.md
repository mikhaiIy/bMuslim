# bMuslim Web — Design Direction & Site Plan

Authoritative brief for building the bMuslim **web site + web app** under `web/`.
Executors: read this file fully, plus [CONTEXT.md](../CONTEXT.md) (vocabulary),
[design/home-mockup.html](../design/home-mockup.html) (the established brand language),
and [docs/design-brief.md](./design-brief.md) (sample data).

## Design read

Reading this as: **product web app + quiet marketing landing for a Muslim daily-worship
companion (Malaysia market)**, for a design-conscious consumer audience, with a
**calm / serene / premium** language, extending an existing brand (green + gold dawn
palette, Plus Jakarta Sans, Noto Naskh Arabic). Dials: `DESIGN_VARIANCE: 5`,
`MOTION_INTENSITY: 3`, `VISUAL_DENSITY: 3`.

Method (chosen after judging 5 researched options): taste-skill discipline
(anti-default, pre-flight rules below) + minimalist/soft-variant restraint for app
screens + Anthropic frontend-design principles (distinctive type, one cohesive palette,
purposeful motion only, atmospheric backgrounds) + a shared token layer so every page
sits on one system.

**Not cliché:** no mosque silhouettes, no crescent-and-star decoration, no heavy gold
filigree, no Arabic-calligraphy-as-wallpaper. Reverence comes from space, light
(dawn-gradient atmosphere), and typographic care.

## Brand tokens (extend the mobile mockup exactly)

Light: bg `#F4F5F1`, surface `#FFFFFF`, text `#16211B`, muted `#5B6960`, faint `#909A93`,
line `rgba(21,33,27,.09)`, accent green `#1E6F4D` (on-accent `#FFF`), gold `#B8862F`,
track `rgba(21,33,27,.055)`.
Dark: bg `#0D1411`, surface `#161F1A`, text `#E7EDE9`, muted `#95A89D`, faint `#6F7F76`,
line `rgba(255,255,255,.10)`, accent `#5BBB8B` (on-accent `#06110C`), gold `#E1C574`,
track `rgba(255,255,255,.06)`.

- **One accent = green.** Gold is *functional only*: the now-needle, fasting/day-banner,
  authenticity grades. Never decorative gold.
- Dawn atmosphere: the two soft radial gradients from the mockup (`--dawn1`/`--dawn2`)
  behind page/app headers. Subtle SVG film grain overlay (opacity ≤ .04) on app surfaces.
- Type: **Plus Jakarta Sans** (400–800) for UI/display; **Noto Naskh Arabic** (500/700)
  for sacred text, `direction: rtl`, generous `line-height: 2`. Google Fonts CDN.
- Display style: 700–800 weight, `letter-spacing: -0.02em`+. Body ≤ 65ch, line-height 1.6+.
- **Radius lock:** cards/panels 16px, inputs 10px, chips/buttons full-pill. Nothing else.
- Shadows tinted to bg hue (`rgba(15,28,20,…)`), soft and low. No pure-black shadows.
- Theme: light + dark, `data-theme` attribute on `<html>`, respects
  `prefers-color-scheme`, toggle persists via `localStorage`. Whole-page theme lock.

## Hard rules (pre-flight — failing any = not done)

1. Language: **Bahasa Melayu** for all UI copy (matches product + market). Correct,
   natural BM — no machine-translation feel.
2. No em-dashes anywhere in visible copy. No "AI purple", no neon gradients, no
   glassmorphism-everywhere, no infinite loop animations.
3. Max 1 eyebrow label per 3 sections on the landing page. No split-header default.
4. All buttons/inputs pass WCAG AA contrast in BOTH themes. CTA labels ≤ 3 words,
   one label per intent across the whole page.
5. `prefers-reduced-motion` honored: all non-essential motion off.
6. No div-based fake screenshots. The landing page previews the product with the
   **real timeline component** (same tokens.css + data.js), embedded live.
7. Sacred content accuracy: use ONLY the Arabic texts, translations, and citations
   already present in `design/home-mockup.html` / `docs/design-brief.md`. Never invent
   hadith text, grades, or references. Sample data may be labeled "contoh".
8. Every page works 360px → 1440px+. Multi-column layouts declare explicit collapse.
9. No build step. Plain HTML/CSS/JS, self-contained, opens from a static server.
10. Fonts/icons: Google Fonts CDN + inline SVG icons (1.8px stroke, reuse mockup set).

## Files

```
web/
  index.html          landing page (marketing)
  app.html            the web app (Hari ini / Perancang / Tetapan views)
  onboarding.html     4-step first-run flow → app.html
  assets/tokens.css   design tokens, base elements, shared components
  assets/data.js      prayer times, deed catalog, strings (from design-brief sample day)
  assets/shared.js    theme manager, icon library, time helpers
  assets/app.js       timeline engine + app views
```

Sample day (use EXACTLY, from design-brief.md): Kuala Lumpur (zon WLY01), Khamis.
Subuh 05:59, Syuruk 07:11, Zohor 13:21, Asar 16:45, Maghrib 19:29, Isyak 20:42.
"Now" = 15:00. Hijri: 12 Muharram 1448. Sleep 23:00, wake 05:30. Deeds per
design-brief.md (zikir pagi/petang, Dhuha, 5 fardhu, Al-Mulk, Tahajud ~03:30–05:30,
puasa Khamis as Day banner) + Anytime tray items (istighfar, selawat, sedekah).

## Page specs

### app.html — the product (this is the hero deliverable)

Desktop app-shell, calm and spacious:
- **Top bar** (≤72px): wordmark "bMuslim", center nav (Hari ini / Perancang / Tetapan)
  as pill-tabs, right: progress ring (n/jumlah), theme toggle.
- **Header row:** salam + big Gregorian date, Hijri date; Day banner when fasting
  ("Puasa sunat Khamis", gold-tinted, moon icon).
- **Timeline (the hero, full width):** horizontal, drawn to real time scale
  03:00 → 23:30, following CONTEXT.md vocabulary exactly:
  - Hour ticks + labels; prayers pinned in a privileged base Lane (green dot chips
    with time + name).
  - Deed Blocks: faint dashed Window track + solid act marker (icon + title);
    ruling = visual weight (Muakkadah stronger fill than Sunnah). Overlaps stack
    into Lanes.
  - Fixed dashed **gold Needle** at horizontal center with a time chip; the timeline
    scrolls under it (drag / wheel / trackpad). Opens centered on 15:00.
  - "Kembali ke sekarang" pill button appears when scrubbed away.
  - Scrubbing updates the Detail panel live (nearest Blocks under the Needle).
- **Below the timeline, a two-column band** (collapses to stacked on mobile):
  - **Detail panel** (the web version of the bottom sheet): "Sekarang · 15:00" label,
    deed title + checkbox, chips (Type · Ruling · window), Arabic text, BM translation,
    citation card (source + grade pill + sunnah.com / e-Fatwa link rows), "Tandai
    selesai" primary button. When several Blocks under the Needle: small selector row.
  - **Anytime tray** ("Bila-bila masa"): 3 quiet cards with checkbox, no times.
- **Perancang view:** week strip (7 day cells, today active, gold fasting dots) +
  "Cadangan mengikut sunnah" cards (Isnin fast, Ayyam al-Bidh 13–15, Al-Kahf Jumaat,
  Dhuha) as in the mobile mockup, adapted to a wider 2-col grid on desktop.
- **Tetapan view:** clean settings groups: Zon waktu solat (select, WLY01), Waktu tidur
  & bangun (time inputs), Tahap (Pemula/Lanjutan segmented), Bahasa (BM/EN segmented),
  Notifikasi (5 labeled toggles: waktu solat, zikir pagi, zikir petang, Al-Mulk,
  ringkasan harian). Label above input, helper text where useful.
- Check-off state + theme persist in `localStorage`; progress ring updates live.
- Views switch client-side (hash routing `#hari-ini`, `#perancang`, `#tetapan`),
  no page reload.

### index.html — landing page

Quiet, confident, product-led. 6 sections, ≥4 layout families, real component previews:
1. **Hero (split):** left: headline (≤7 words, e.g. "Amalan terbaik untuk saat ini"),
   ≤20-word subtext, primary CTA "Buka aplikasi" → app.html (+ secondary "Lihat cara
   ia berfungsi" anchor). Right: a LIVE mini timeline component (real tokens + data,
   needle animating one gentle scrub on load, then still). Dawn atmosphere background.
2. **How it works (full-width):** the Anchor → Window → Block idea shown as a wide
   diagram strip built from the real timeline pieces, 3 short captions (anchored to
   prayer times, windows not alarms, needle = now).
3. **Authenticity (asymmetric):** citation card blown up as the visual (real component:
   grade pill, sunnah.com row), copy about verified sources, no invented content.
4. **Day-aware deeds:** compact band: Hijri date chip + fasting banner component,
   copy about Isnin/Khamis + Ayyam al-Bidh appearing automatically.
5. **Offline & privacy (quiet stack, centered allowed):** short manifesto-style copy:
   tiada akaun, tiada awan, berfungsi luar talian. No cards, typography only.
6. **Final CTA + footer:** one CTA (same label "Buka aplikasi"), small footer:
   wordmark, "Dibina untuk Malaysia · Data JAKIM", theme toggle, link to onboarding.
Motion: one entrance fade/rise on hero, scroll-reveal (CSS/IO, staggered, once) on
sections 2–5. Nothing loops.

### onboarding.html — 4 steps

One centered card flow (max-width ~520px), dawn background, step dots:
1. Bahasa (BM/EN cards), 2. Zon waktu solat (select of real JAKIM zone codes, WLY01
default), 3. Waktu tidur & bangun (two time inputs, helper: menentukan Tahajud &
Al-Mulk), 4. Tahap (Pemula vs Lanjutan cards with one-line explanation).
Progress persists to `localStorage`; final button "Mula" → app.html. "Langkau" quiet
link → app.html with defaults.

## Verification

Serve `python3 -m http.server` from `web/` and check: both themes on all 3 pages,
needle scrub updates Detail, check-off persists on reload, hash nav works, 360px and
1280px layouts, reduced-motion, no console errors.
