# Cycle 2, wave 1: devotional ownership

## Current overlap inventory

`isActiveAt` endpoints are inclusive. `activeDeedsAt` orders Fardh before Muakkadah/Sunnah and the
main ledger selects `active.firstOrNull()`.

| Fardhu | Contextual | Simultaneous interval | Boundary |
| --- | --- | ---: | --- |
| Subuh | Zikir pagi | 05:59–06:19 | Both begin 05:59; only zikir at 06:20 |
| Zohor | Dhuha | 13:21 only | Dhuha through 13:21; only Zohor at 13:22 |
| Asar | Zikir petang | 16:45–17:05 | Both begin 16:45; only zikir at 17:06 |
| Maghrib | Zikir petang | 19:29 only | Zikir ends as Maghrib begins; only Maghrib at 19:30 |
| Isyak | Al-Mulk | 20:42–21:02 | Both begin 20:42; only Al-Mulk at 21:03 |

All-day Thursday fasting coexists but is modeled separately. “Selepas Subuh/Asar/Isyak” windows
currently begin at the prayer minute; “sebelum Zohor/hingga Maghrib” include the prayer-start
minute. These are product-model boundary effects, not sourced religious determinations.

Current UI: ledger/open context choose fardhu; wheel prayer/context labels can both be saffron;
summary gives similar accent; wheel speech duplicates prayer in active list and prayer status.

## Evidence and limits

- JAKIM distinguishes five obligatory prayers from Sunnah and describes rawatib around fardhu:
  [e-Muallaf prayer guidance](https://emuallaf.islam.gov.my/papar_maklumat.php?ID=23).
- JAKIM describes fardhu as obligation and Quran/zikir/doa as additional worship:
  [Panduan Qiamullail](https://www.islam.gov.my/images/ePenerbitan/Panduan_Qiamullail_JAKIM.pdf).
- A JAKIM publication describes Sunnah worship as complementing shortcomings in fardhu:
  [Hadith Sahih Series](https://www.islam.gov.my/images/ePenerbitan/buku/Himpunan_Hadith-Hadith_Sahih_Siri_1_Keutamaan_Puasa__Ramadan-web.pdf).
- Mufti Wilayah says once iqamah is called, attention turns to fardhu, with nuance for a Sunnah
  already underway: [Irsyad 173](https://muftiwp.gov.my/ar/artikel/irsyad-hukum/umum/1027-irsyad-al-fatwa-ke-173-hukum-membatalkan-solat-sunat-apabila-sudah-iqamah),
  [Al-Kafi 1645](https://muftiwp.gov.my/ms/artikel/al-kafi-li-al-fatawi/4285-al-kafi-1645-beri-minyak-wangi-kepada-jemaah-selepas-iqamah-dilaungkan).
- Entering prayer time is not iqamah; preparation/Sunnah may occur between azan and iqamah:
  [Al-Kafi 1755](https://muftiwp.gov.my/ms/artikel/al-kafi-li-al-fatawi/4640-al-kafi-1755-adakah-tempoh-waktu-yang-khusus-di-antara-azan-dan-iqamah).
- Compose semantics/traversal need explicit grouping/order:
  [Accessibility](https://developer.android.com/develop/ui/compose/accessibility),
  [Traversal](https://developer.android.com/develop/ui/compose/accessibility/traversal).
- Frequent live-region announcements can overwhelm:
  [Semantics guidance](https://developer.android.com/develop/ui/compose/accessibility/semantics).
- Android 14 supports nonlinear font scale to 200%:
  [Android 14 scaling](https://developer.android.com/about/versions/14/features#non-linear-font-scaling).
- Meaningful/focus order should preserve meaning:
  [WCAG sequence](https://www.w3.org/WAI/WCAG21/Understanding/meaningful-sequence.html),
  [focus order](https://www.w3.org/WAI/WCAG22/Understanding/focus-order.html).

**Product inference:** when the model reports simultaneous fardhu and contextual Sunnah, fardhu
owns primary visual/spoken position. Sunnah remains useful secondary context. This applies existing
model priority; it is not a new ruling.

**Important limit:** bMuslim knows prototype prayer/action windows, not iqamah. It must not tell
users to stop zikir, declare contextual acts invalid across the window, or present its 20-minute
interval as fiqh.

## Ownership contract

### Primary

When any fardhu is active:

1. Its name/time are the single primary identity.
2. It alone gets strongest contrast/weight and primary saffron text emphasis.
3. Textual identity remains recoverable; node alone is insufficient.
4. Completion does not transfer ownership before the interval ends.

Without active fardhu, highest-priority contextual deed may become primary.

### Secondary

During overlap, contextual geometry remains when space permits, but its title moves to stable
secondary context or detail rather than competing beside fardhu. Its wheel title may yield under
pressure, while meaning remains in detail/speech. All-day state is tertiary.

### Suppression

Yield first: celestial decoration, passive hours, upcoming-prayer explanation (retain nodes),
contextual wheel title, then contextual arc only as last visual fallback with context elsewhere.

Never suppress for secondary content: active fardhu name/time/node/state, fardhu-first speech, or
the distinction between fardhu and Sunnah.

### Spoken owner

One nonduplicative announcement:

> Masa 05:59. Solat Subuh aktif. Amalan berkaitan: Zikir pagi, selepas Subuh hingga Syuruk.

Order: selected time; fardhu once; contextual second; day state last/separate. No live announcement
per drag. Detail traversal: fardhu → contextual → day state → ledger action.

## State contract

| State | Ownership |
| --- | --- |
| Glance | Fardhu text/node primary; contextual secondary outside primary collision rail |
| Finger-down | Recompute immediately by minute; secondary may prune; no live-region spam |
| Enter fardhu | Promote at exact inclusive boundary; hysteresis never delays priority |
| Settle | Restore secondary only after fardhu identity stabilizes |
| Expand | Ownership from minute, never progress; contextual hands to summary |
| Detail | Ledger fardhu; contextual follows in summary/index |
| Collapse | Fardhu wheel identity present before detail retreats |
| Reverse | No prior-state latch can restore contextual primacy |
| Reduced motion | Snap between valid ownership states; no equal-weight transient |
| Fardhu end | Promote contextual only on first inactive minute |

Boundary-only assertions: 13:20 Dhuha primary, 13:21 Zohor primary/Dhuha secondary, 13:22 Zohor;
19:28 zikir primary, 19:29 Maghrib primary/zikir secondary, 19:30 Maghrib.

## Test matrix

Every minute across overlap ±10: 05:49–06:29, 13:11–13:31, 16:35–17:15, 19:19–19:39,
20:32–21:12. Cross width 360/390; font 1.0/1.3/200%; glance/detail; rest/scrub/settle;
expand/collapse/reverse 25/50/75%; reduced motion.

Assertions: at most one primary; active fardhu always primary; full readable name/time; contextual
never suppresses it; spoken exactly once and first; open/detail order fardhu first; no live spam;
ownership independent of presentation/direction; secondary reflows before primary truncates;
boundary minutes choose fardhu; completion cannot promote contextual early.

## Misuse risks

Do not equate prayer time with iqamah, command abandonment of another valid act, call prototype
windows canonical, describe Sunnah as unimportant, erase secondary context, let collision/hysteresis
override ruling priority, treat web WCAG technique as Compose API, or call 1.3× maximum.
