# Wave 2: publication and ranking authority

## Verdict

bMuslim can implement a publication ledger, validators, release integrity, withdrawal/correction,
and a neutral resolver using synthetic/draft fixtures. It must not publish religious text, BM
translations, rulings, timing claims, or a primary/best-now ranking until each claim has qualified
review, usable source rights, and—where Quran content is involved—written KDN evidence applicable
to the exact app release.

Firecrawl returned HTTP 402; the scout used live official sources. No files were edited.

## Findings

1. JAKIM's media-baharu guidance directly includes app creators and requires authoritative sources,
   permission/credit, accurate Quran/hadith content, and recognised scholarship. It does not issue
   an approval certificate for each app entry or designate its reviewer.
   https://www.islam.gov.my/images/garis-panduan/Garis_Panduan_Kandungan_Berunsur_Islam_Dalam_Media_Baharu.pdf
2. KDN hadith guidance requires original matn, verified translation, collection/kitab/bab/number,
   edition/bibliography, status, and verification beyond an online result. `citation: String` and a
   default grade cannot represent this.
   https://www.moha.gov.my/utama/index.php/ms/component/content/article/321-garis-panduan-hadith?catid=112
3. Non-Bukhari/Muslim status must attribute the assessment authority and target. An unclassified
   report ultimately needs someone `berkeahlian`; no grade may be inferred or defaulted.
   https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/Panduan_Asas_Penyemakan_Sumber_dan_Status_Hadith.pdf
4. Online guidance requires hadith experts but does not publish objective general qualification
   thresholds. Quran consultant qualifications are explicit and distinct. An internal reviewer
   record is safe; claiming KDN-qualified hadith review is not.
   https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/GARIS_PANDUAN_PENULISAN_DAN_PENERBITAN_TEKS_HADITH.pdf
5. KDN definitions cover Quran portions below one juz and offline/online mobile apps, but the exact
   certificate path for short excerpts remains ambiguous. Do not infer an exemption.
   https://www.moha.gov.my/utama/images/Akta%20Perundangan/GARIS%20PANDUAN%20TEKS%20Al-QURAN/GARIS_PANDUAN_PERMOHONAN_PENERBITAN_TEKS_AL-QURAN_SECARA_DIGITAL_KDN_2024.pdf
6. Where the digital-Quran process applies, controls include prescribed source/rasm/riwayah, JAKIM
   translation approval, consultants, proof review, release proof, publisher identity, and a
   publication-specific certificate. Another app or release's approval does not transfer.
7. Official processes require correction and resubmission, but no public runtime withdrawal API or
   SLA exists. Versioned receipts and single-entry withdrawal are useful bMuslim safety mechanisms,
   not claims of an official KDN system.
8. No official generic best-deed-now algorithm was found. Case-specific comparisons show ranking is
   a substantive reviewed claim, not an enum sort.
   https://www.muftiwp.gov.my/ar/artikel/irsyad-al-hadith/4555-irsyad-al-hadith-siri-ke-506-kedudukan-selawat-berbanding-zikir

## Implementable contract

- `CatalogueRelease`: release ID, schema, lifecycle, fixture class, hash, entries, corrections.
- `Entry`: stable ID/version, lifecycle, atomic claims, ranking policy, rights, supersession and
  withdrawal.
- `Claim`: type, exact text/locale/fingerprint, evidence, review, and regulatory clearance IDs.
- `HadithEvidence`: collection, author, kitab, bab, number/scheme, narrator, edition, publisher,
  matn fingerprint, assessment target, status and authority, translation/sharh provenance, rights.
- `QuranEvidence`: surah/ayah, fingerprint, rasm/source/riwayah, translation approval, KDN scope and
  certificate, rights.
- `Review`: identity and credential evidence, expertise, jurisdiction/mazhab, reviewed claim
  fingerprints, decision/date/re-review triggers.
- `RankingPolicy`: exact candidates, circumstances, ordering/selection, wording, evidence, review.
- `Correction`: affected claims, old/new fingerprints, reason, decision, and withdrawal behavior.

Production accepts only published production content with a valid release hash. Synthetic/draft
fixtures are forbidden from release assets. Fingerprint mutations invalidate review. Quran and
ranking claims fail closed without applicable clearance/review. With no ranking policy, the
resolver returns a neutral set or unavailable—never a primary recommendation.

## Falsifiable tests

- Current `zikir-pagi` fails for mismatch, incomplete provenance, default grade, and no review.
- `al-mulk` fails independently for Quran clearance and unsupported timing.
- Non-Bukhari/Muslim `Sahih` without an authority fails.
- Quran substrings fail production without applicable clearance.
- With no approved ranking, no primary or “utama sekarang” language exists.
- One-character source mutation invalidates fingerprint, review, and release.
- Withdrawn entries disappear from fresh and cached resolution.
- Release packaging fails if synthetic/draft fixtures are included.

## Recommended first slice

Implement only the ledger types, validator, deterministic release hash, lifecycle/withdrawal, and
fail-closed resolver tests. Use clearly synthetic, non-sacred draft fixtures. Demonstrate that
draft/incomplete content cannot publish, neutral resolution emits no primary, mutation invalidates
review, withdrawal works, and release packaging contains no fixtures.

Online research cannot approve current wording/rulings/windows/rankings, establish rights, define a
hadith/fiqh reviewer conclusively, or classify short Quran excerpts for this app. Those remain
external blockers.
