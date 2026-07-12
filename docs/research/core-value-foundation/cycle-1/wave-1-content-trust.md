# Wave 1: religious-content trust and authenticity

## Verdict

Implement a **Religious Content Ledger with an enforced publication gate** before beautification,
timeline expansion, notifications, or a large catalogue.

The current prototype demonstrates silent content corruption:

- The Arabic `اللَّهُمَّ بِكَ أَصْبَحْنَا...` belongs to Sunan Abi Dawud 5068, while the app cites
  5074, which is a different supplication.
- `grade` defaults every deed to `Sahih` unless overridden.
- A bare grade omits the grader and review provenance.
- Tirmidhi 2891 establishes a virtue of Surah al-Mulk but not the separate “before sleep” timing
  claim.
- Timing, ruling, virtue, translation, and act text are fused into one unchecked record even though
  each is a separate religious claim.

## Ranked evidence and heuristics

### 1. JAKIM requires authoritative, accurate Islamic content in new media

- Source: JAKIM, *Garis Panduan Kandungan Berunsur Islam Dalam Media Baharu*, 2024.
- URL: https://www.islam.gov.my/images/garis-panduan/Garis_Panduan_Kandungan_Berunsur_Islam_Dalam_Media_Baharu.pdf
- Evidence: the guidance is written for app creators and requires reliable sources, permission or
  credit, accurate Quran citation/recitation, accurate hadith matn, translation and explanation,
  and avoidance of fabricated hadith or conflict with authoritative fatwa.
- Heuristic: no deed reaches `published` unless every atomic claim—act, Arabic, translation,
  timing, ruling, and virtue—has evidence and reviewer attestation. CI rejects incomplete entries.
- Qualification: JAKIM states the content duties; CI enforcement is the scout's engineering
  inference. A link alone does not prove the app's wording matches the source.

### 2. Malaysian hadith-publication guidance requires traceable verification

- Source: KDN Lajnah Tahqiq Hadith, *Garis Panduan Penulisan dan Penerbitan Teks Hadith*, 2018;
  portal updated 2025-07-21.
- URLs:
  - https://www.moha.gov.my/utama/index.php/ms/component/content/article/321-garis-panduan-hadith?catid=112
  - https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/GARIS_PANDUAN_PENULISAN_DAN_PENERBITAN_TEKS_HADITH.pdf
  - https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/Panduan_Asas_Penyemakan_Sumber_dan_Status_Hadith.pdf
- Evidence: source checking, accurate quotation, translation from the original using authoritative
  commentary, status declaration, and complete references are prescribed.
- Heuristic: require collection, canonical and in-book identifiers, numbering scheme, exact matn
  hash, translation provenance, status, grader, reviewer, review date, and conflict note. Remove
  grade defaults.
- Risk: scholarly grading can differ; unattributed `Sahih` overstates certainty.

### 3. The prototype has a direct primary-record mismatch

- Sources:
  - https://sunnah.com/abudawud:5068
  - https://sunnah.com/abudawud:5074
- Evidence: 5068 contains the prototype's morning Arabic and reports Sahih (Al-Albani); 5074 is a
  different, longer supplication.
- Heuristic: catalogue-conformance tests compare normalized text fingerprints and reviewed semantic
  identifiers. The current `zikir-pagi` record must fail.
- Risk: Sunnah.com is useful for verification and deep links but does not establish redistribution
  permission.

### 4. Related evidence must not masquerade as direct evidence

- Sources:
  - https://sunnah.com/tirmidhi:2891
  - https://sunnah.com/bukhari:1145
- Evidence: Tirmidhi 2891 describes al-Mulk's intercession but does not state “before sleep.”
  Bukhari 1145 concerns the last third's virtue but does not by itself establish every scheduling
  and ruling detail shown by the app.
- Heuristic: model `actEvidence`, `timingEvidence`, `rulingEvidence`, and `virtueEvidence`
  separately; every user-visible claim must be covered.
- Risk: a related hadith is not necessarily direct proof. Timing and classification still require
  qualified review.

### 5. Quran content has a distinct Malaysian regulatory path

- Source: KDN, *Garis Panduan Permohonan Penerbitan Teks Al-Qur’an Secara Digital*, under the 2023
  amendment.
- URL: https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/GARIS_PANDUAN_PERMOHONAN_PENERBITAN_TEKS_AL-QURAN_SECARA_DIGITAL_KDN_2024.pdf
- Evidence: mobile apps are digital publication; Quran text, translation, review, consultants,
  proof, and certification have prescribed requirements.
- Heuristic: block Quran snippets/translations from release until exact source, approval
  applicability, and licence are documented. Seek a written KDN scope determination.
- Open issue: the document distinguishes Quran material, including portions, from a Quran text of
  one juz or more; do not assume short excerpts are exempt or fully covered.

### 6. Approval of another Quran app does not transfer

- Source: JAKIM, “Smart Quran,” published 2020-12-04.
- URL: https://www.islam.gov.my/ms/quran-hadith/smart-quran
- Heuristic: a safer early option is to deep-link to an approved Quran product for complete reading
  while bMuslim's own regulatory route is clarified.
- Risk: Smart Quran approval does not license or approve reuse in bMuslim.

### 7. Open licensing and Malaysian approval are separate concerns

- Sources: Tanzil Quran Text License and update log.
- URLs:
  - https://tanzil.net/docs/text_license
  - https://tanzil.net/updates/
- Evidence: CC BY 3.0 permits verbatim copying/distribution, forbids alteration, and requires
  attribution, linking, notice, and update tracking.
- Heuristic: if later approved for use, store upstream version and hash, prohibit manual edits,
  retain attribution, and monitor changes.
- Risk: an open licence is not Malaysian regulatory approval; Arabic and translations may have
  different terms.

### 8. Malaysia's Hijri date is not simply Umm al-Qura calculation

- Sources: JAKIM material on Malaysian rukyah, hisab, and imkanur-rukyah.
- URLs:
  - https://www.islam.gov.my/en/media-statement/4724-kenyataan-media-ketua-pengarah-jabatan-kemajuan-islam-malaysia-berkenaan-kaedah-penentuan-awal-zulhijah-dan-aidiladha-di-malaysia-1446h/2025m
  - https://www.islam.gov.my/ms/kenyataan-media/346-kenyataan-media-kp-jakim-berkenaan-isu-penentuan-awal-bulan-syawal-1432h
- Heuristic: do not treat Java `HijrahChronology`/Umm al-Qura as authoritative for Malaysian
  date-driven deeds. Use a versioned Malaysian calendar with reviewed offline fallback and an
  explicit estimated state; test known month-boundary divergence.
- Risk: wrong Hijri dates can trigger wrong Ayyam al-Bidh or fasting prompts while appearing exact.

### 9. Last-third calculations are scheduling assistance, not exact religious boundaries

- Source: Mufti Wilayah, *Pembahagian Malam dan Qiamullail*.
- URL: https://www.muftiwp.gov.my/en/artikel/al-kafi-li-al-fatawi/1376-al-kafi-251-pembahagian-malam-dan-qiamullail
- Evidence: Tahajjud follows waking from sleep and precedes Fajr; division of the night is described
  as approximate (`taqribi`), not exact (`tahdidi`).
- Heuristic: model `afterSleep`, permissible window, preferred period, and approximate calculation
  separately. Never word a computed minute as a definitive religious boundary.
- Risk: “best deed exactly now” can make an approximation look like a ruling.

### 10. The catalogue needs release integrity and correction history

- Source: NIST SP 800-218 v1.1, February 2022.
- URL: https://csrc.nist.gov/pubs/sp/800/218/final
- Evidence: preserve releases and supporting provenance, verify integrity, and update provenance
  as components change.
- Heuristic: version and hash the approved catalogue; preserve reviewer attestations and prior
  versions; support correction history and emergency withdrawal.
- Qualification: applying software supply-chain integrity to religious content is an engineering
  inference, not a jurisprudential claim.

## First-capability acceptance criteria

- `ContentClaim`: claim type, exact content, locale, and evidence IDs.
- `Evidence`: authority, title, canonical reference/URL, edition/numbering, supported scope, grade
  plus grader, and licence status.
- `Review`: named qualified reviewer, scope, jurisdiction/mazhab basis, decision, date, and
  re-review trigger.
- `ContentStatus`: `draft → in_review → approved → published → withdrawn`.
- No default grade, ruling, time window, or authenticity.
- A build fails when published claims lack evidence, review, licence status, or conflict
  disposition.
- Runtime reads only a versioned, integrity-checked approved catalogue.
- One deed can be withdrawn without waiting for a full application release.
- User-facing language distinguishes source text, reviewer classification, and app-calculated
  estimates.

## Contradictions and open questions

- The PRD says human-verified but defines no reviewer qualification, approval record, conflict
  resolution, re-review, or withdrawal.
- Sunnah.com is an index/translation site, not the original collection; no bulk-reuse licence was
  established.
- A relevant fatwa requires a specific ruling URL, issuing authority, jurisdiction, date, and
  applicability note, not an e-Fatwa homepage.
- JAKIM's guideline requires authority but does not define who approves each app entry.
- KDN must clarify whether bMuslim's short Quran excerpts trigger the full digital-publication
  process.
- A qualified Malaysian reviewer for rulings, timing, BM translation, and conflicts is not yet
  named.
- An official machine-readable Malaysian Hijri calendar suitable for caching/redistribution is not
  yet identified.

## Next-wave questions

1. Can KDN's Quran/Hadith unit give written classification of short Arabic excerpts, BM
   translations, offline bundling, and approval duties?
2. Can a Malaysian hadith/fiqh reviewer audit the initial 8–10 deeds claim-by-claim, including
   competing grades, windows, and `Sunnah Muakkadah` classifications?
3. Which official JAKIM/e-Solat/MyRHK source supplies redistributable Malaysian Hijri dates and
   correction notices for offline fasting rules?
