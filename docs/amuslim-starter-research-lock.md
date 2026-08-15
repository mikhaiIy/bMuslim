# aMuslim starter deed research lock

## Status

- Research wave complete on 2026-07-15.
- Scope: a small pre-production dataset of deeds whose recurrence or trigger is explicit in the
  cited report.
- Dataset: `datasets/amuslim/starter-v0.1/catalog.draft.json`.
- Publication status: **IN REVIEW — NOT FOR RELEASE APP DISPLAY**.

The `productionDebug` application is a separate, clearly labelled structural prototype. It may render
unreviewed records and provisional geometry so the data/UI contract can be evaluated, provided that
the source phrase remains visible, estimates are visually and textually marked, and none of this
content enters `productionRelease`. This narrow prototype exception does not relax the publication
requirements below.

## Locked starter candidates

| Candidate | Explicit source timeframe | Evidence |
| --- | --- | --- |
| Three protective surahs | morning and evening | Sunan Abi Dawud 5082, Hasan (Al-Albani) |
| Post-prayer tasbih sequence | after every prayer | Sahih Muslim 597a |
| Two rak'ahs of Dhuha | in the forenoon | Sahih Muslim 720 |
| Three surahs with blowing and wiping | when going to bed each night | Sahih al-Bukhari 5017 |
| Dua, asking, and istighfar | when the last third of the night remains | Sahih al-Bukhari 1145 |
| Witr | before sleeping | Sahih al-Bukhari 1981 |
| Voluntary fasting | Monday and Thursday | Sunan Abi Dawud 2436, Sahih (Al-Albani) |
| Increased salawat | Friday | Sunan Abi Dawud 1047, Sahih (Al-Albani) |
| Dua in Friday's final period | final period after Asr | Sunan Abi Dawud 1048, Sahih (Al-Albani), with Sahih al-Bukhari 935 corroboration |

## Evidence principles

1. The source's temporal phrase is stored separately from the app's future computed window.
2. Event-relative deeds remain event-relative. A prayer start does not prove prayer completion;
   Isyak does not prove bedtime.
3. Qualitative periods such as morning, evening, forenoon, last third, and final Friday period are
   not silently converted into arbitrary minute offsets.
4. Reported grades retain the displayed grader. Sahih al-Bukhari and Sahih Muslim records are
   identified by collection rather than assigned a fabricated per-record grade.
5. The catalogue contains no ruling, virtue, or ranking defaults.
6. The research-candidate tier may include a report displayed as Sahih or as Hasan by a named
   grader. The exact assessment is preserved; Hasan is never normalised to Sahih, and neither label
   replaces qualified publication review.

## Explicit exclusions and holds

### Friday-specific charity

Excluded from the strict Prophetic-Sunnah starter. The commonly cited report that charity is
multiplied on Friday is attributed to Ka'b in Musannaf Ibn Abi Shaybah 5433, not directly to the
Prophet. General charity can later be offered as a user-created weekly habit, but the product must
not label Friday charity a Prophetic Sunnah or claim a special Friday multiplier without stronger
reviewed evidence. Audit locator:
<https://hadithportal.com/index.php?bab_id=688&book=62&chapter_id=4&e=703&f=618&show=bab&sub_idBab=0>.

### Surah al-Kahf on Friday

Held for a later research pack. A report is available through Mishkat 2175 / al-Bayhaqi and is
displayed with a favourable assessment, but transmission routes, grading presentation, the full
surah action, and Friday day-boundary interpretation deserve a dedicated review rather than being
mixed into this smallest starter set.

### Friday ghusl and early attendance

Held for an eligibility-aware Jumu'ah pack. Sahih al-Bukhari 881 explicitly supports the sequence,
but it needs local mosque event data, audience/eligibility rules, and a reviewed interpretation of
the report's five “hours”. JAKIM Zohor is not the exact start of a mosque's khutbah.

## Required design before production

- A reviewed Malaysian temporal-policy layer for named periods and night/day boundaries.
- A user-event model for prayer completion and intended sleep.
- Eligibility states for fasting and Jumu'ah-specific practices.
- Claim fingerprints and one qualified approval per published atomic claim.
- Translation/content-use approval for every visible Malay, English, and Arabic string.
- A neutral chronological timeline; any “best deed now” wording remains behind the existing
  separately approved ranking policy.

## Banned shortcuts

- Copying this draft JSON into release/main production assets; the isolated `productionDebug` generated asset is the documented prototype exception.
- Treating Sunnah.com as a redistribution licence.
- Mapping morning to Fajr-to-Syuruk or evening to Asr-to-Maghrib without review.
- Treating the JAKIM Dhuha field as proof of a religious window.
- Turning `sa'ah` into a literal sixty-minute interval automatically.
- Assigning fixed salawat counts not present in Sunan Abi Dawud 1047.
- Presenting Friday charity as a special Sunnah based on the excluded report.
