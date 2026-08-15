#!/usr/bin/env bash
set -euo pipefail

debug_apk="${1:-app/build/outputs/apk/production/debug/app-production-debug.apk}"
release_apk="${2:-app/build/outputs/apk/production/release/app-production-release-unsigned.apk}"
catalog="datasets/amuslim/starter-v0.2/catalog.draft.json"
asset_path="assets/amuslim/starter-v0.2/catalog.draft.json"

android_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
build_tools="$(find "$android_sdk/build-tools" -mindepth 1 -maxdepth 1 -type d -print | sort -V | tail -n 1)"
aapt="$build_tools/aapt"
dexdump="$build_tools/dexdump"

for required in "$debug_apk" "$release_apk" "$catalog" "$aapt" "$dexdump"; do
  if [[ ! -e "$required" ]]; then
    echo "Missing required preview-audit input: $required" >&2
    exit 1
  fi
done

work_dir="$(mktemp -d)"
trap 'rm -rf "$work_dir"' EXIT
debug_dir="$work_dir/debug"
release_dir="$work_dir/release"
mkdir -p "$debug_dir" "$release_dir"
unzip -qq "$debug_apk" -d "$debug_dir"
unzip -qq "$release_apk" -d "$release_dir"

if [[ ! -f "$debug_dir/$asset_path" ]]; then
  echo "Research catalog asset is missing from the productionDebug APK" >&2
  exit 1
fi
if ! cmp -s "$catalog" "$debug_dir/$asset_path"; then
  echo "Research catalog asset differs from the canonical draft" >&2
  exit 1
fi
if [[ -e "$release_dir/$asset_path" ]]; then
  echo "Research catalog asset leaked into the productionRelease APK" >&2
  exit 1
fi
while IFS= read -r release_file; do
  if cmp -s "$catalog" "$release_file"; then
    echo "Byte-identical research catalog leaked into productionRelease at ${release_file#"$release_dir/"}" >&2
    exit 1
  fi
done < <(find "$release_dir" -type f -print)

debug_badging="$($aapt dump badging "$debug_apk" 2>/dev/null || true)"
release_badging="$($aapt dump badging "$release_apk" 2>/dev/null || true)"
grep -F "package: name='com.bmuslim.app.debug'" <<<"$debug_badging" >/dev/null || {
  echo "productionDebug must use the distinct com.bmuslim.app.debug package" >&2
  exit 1
}
grep -F "application-label:'bMuslim Research Preview'" <<<"$debug_badging" >/dev/null || {
  echo "productionDebug must carry the Research Preview app label" >&2
  exit 1
}
grep -F "package: name='com.bmuslim.app'" <<<"$release_badging" >/dev/null || {
  echo "productionRelease package identity changed unexpectedly" >&2
  exit 1
}

debug_identities=(
  ResearchCatalogParser
  ResearchCatalogRepository
  ResearchPreviewScreen
  ResearchCatalogResult
  ResearchCatalog
  ResearchDeed
  ResearchEvidence
  ResearchAssessment
  ResearchCadence
  ResearchEvidenceRole
  ResearchTimelinePlan
  ResearchWheelIntervalsKt
  ResearchLunarDateContext
  ResearchLunarDateContextState
  ResearchEventGroup
  ResearchOverflowGroup
  ResearchSemanticLanePlan
  ResearchPointerConcordance
  ResearchConcordanceItem
  ResearchConcordanceSection
  ResearchPersonalEvent
  ResearchDayContext
  ResearchTemporalOccurrence
  ResearchValidityWindow
  ResearchTrigger
  ResearchFocusRegion
)
for identity in "${debug_identities[@]}"; do
  if ! grep -aFRl -- "$identity" "$debug_dir" >/dev/null; then
    echo "Debug preview identity is missing: $identity" >&2
    exit 1
  fi
  if grep -aFRl -- "$identity" "$release_dir" >/dev/null; then
    echo "Debug preview identity leaked into productionRelease: $identity" >&2
    exit 1
  fi
done

shared_detail_identity="DemoAlmanacDetailKt"
for artifact_dir in "$debug_dir" "$release_dir"; do
  if ! grep -aFRl -- "$shared_detail_identity" "$artifact_dir" >/dev/null; then
    echo "Shared demo-detail identity is missing: $shared_detail_identity" >&2
    exit 1
  fi
done

retired_preview_identities=(
  ResearchTemporalLegend
  ResearchTimelineKey
  TimelineOccurrenceRow
  MiniTimelineBar
  PersonalEventSection
  DayContextSection
  ResearchDemoLedger
)
for identity in "${retired_preview_identities[@]}"; do
  if grep -aFRl -- "$identity" "$debug_dir" "$release_dir" >/dev/null; then
    echo "Retired preview component returned: $identity" >&2
    exit 1
  fi
done

prototype_needles=(
  "KONKORDANS SAAT INI · DRAF"
  "KONKORDANS NEUTRAL"
  "BELUM DISAHKAN"
  "berkongsi proksi"
  "Hari putih · belum disahkan"
)
for needle in "${prototype_needles[@]}"; do
  if ! grep -aFRl -- "$needle" "$debug_dir" >/dev/null; then
    echo "Debug temporal-prototype content is missing: $needle" >&2
    exit 1
  fi
  if grep -aFRl -- "$needle" "$release_dir" >/dev/null; then
    echo "Debug temporal-prototype content leaked into productionRelease: $needle" >&2
    exit 1
  fi
done

release_corpus="$work_dir/release-corpus.txt"
while IFS= read -r dex; do
  "$dexdump" -d "$dex" >>"$release_corpus"
done < <(find "$release_dir" -type f -name '*.dex' -print | sort)
"$aapt" dump --values resources "$release_apk" >>"$release_corpus" 2>/dev/null
while IFS= read -r release_file; do
  cat "$release_file" >>"$release_corpus"
done < <(find "$release_dir" -type f -print | sort)

mapfile -t draft_needles < <(
  jq -r '
    .catalogId,
    .contentVersion,
    (.entries[] | .id, .actionClaim.id, .timingClaim.id,
      .actionClaim.payload.title["ms-MY"],
      .actionClaim.payload.title["en-GB"],
      .actionClaim.payload.instruction["ms-MY"],
      .actionClaim.payload.instruction["en-GB"],
      .timingClaim.payload.sourceTimePhrase["ms-MY"],
      .timingClaim.payload.sourceTimePhrase["en-GB"],
      .timingClaim.payload.unsupportedInferences[]),
    (.evidence[] | .id, .canonicalUrl, .sourceSummary)
  ' "$catalog" | LC_ALL=C sort -u
)

if ((${#draft_needles[@]} < 40)); then
  echo "Preview audit generated too few draft-content needles" >&2
  exit 1
fi
for needle in "${draft_needles[@]}"; do
  if LC_ALL=C grep -aF -- "$needle" "$release_corpus" >/dev/null; then
    echo "Draft research content leaked into productionRelease: $needle" >&2
    exit 1
  fi
done

echo "aMuslim preview artifact boundary passed: canonical asset and temporal prototype in productionDebug, ${#draft_needles[@]} draft-content checks absent from productionRelease"
