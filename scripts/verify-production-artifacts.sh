#!/usr/bin/env bash
set -euo pipefail

mode="absent"
if [[ "${1:-}" == "--expect-present" ]]; then
  mode="present"
  shift
fi

if (($# == 0)); then
  set -- \
    app/build/outputs/apk/production/debug/app-production-debug.apk \
    app/build/outputs/apk/production/release/app-production-release-unsigned.apk
fi

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
manifest="$script_dir/demo-fixture-denylist.txt"
mapfile -t denylist < <(sed -e '/^[[:space:]]*#/d' -e '/^[[:space:]]*$/d' "$manifest")
if ((${#denylist[@]} == 0)); then
  echo "Empty fixture leakage manifest: $manifest" >&2
  exit 1
fi
identity_denylist=(TimelineDeed TimelineScreen DemoDisclosureText)
shared_identity="com/bmuslim/app/ui/almanac/SharedAlmanacShell"
obsolete_identities=(LiveAlmanacScreen AlmanacInstrumentModel)
# Required by the production JAKIM schema/UI and also used by demo fixtures.
# Keep them in the demo positive control, but do not treat reviewed official-timing terms as leakage.
production_schema_allowlist=(subuh dhuha zohor asar maghrib isyak)
if ((${#production_schema_allowlist[@]} != 6)) ||
  [[ "${production_schema_allowlist[*]}" != "subuh dhuha zohor asar maghrib isyak" ]]; then
  echo "Production schema allowlist must contain exactly the reviewed official-timing terms" >&2
  exit 1
fi
# The research preview is deliberately confined to productionDebug. These two
# assessment labels overlap old demo fixture text; productionRelease gets no exemption.
research_preview_debug_allowlist=(Sahih Hasan)
if ((${#research_preview_debug_allowlist[@]} != 2)) ||
  [[ "${research_preview_debug_allowlist[*]}" != "Sahih Hasan" ]]; then
  echo "Research preview debug allowlist must contain exactly its two assessment labels" >&2
  exit 1
fi

android_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
build_tools="$(find "$android_sdk/build-tools" -mindepth 1 -maxdepth 1 -type d -print | sort -V | tail -n 1)"
dexdump="$build_tools/dexdump"
aapt="$build_tools/aapt"
if [[ ! -x "$dexdump" || ! -x "$aapt" ]]; then
  echo "Android build-tools dexdump/aapt are required for exact artifact string matching" >&2
  exit 1
fi

for apk in "$@"; do
  if [[ ! -f "$apk" ]]; then
    echo "Missing production APK: $apk" >&2
    exit 1
  fi

  extraction_dir="$(mktemp -d)"
  trap 'rm -rf "$extraction_dir"' EXIT
  unzip -qq "$apk" -d "$extraction_dir"
  string_corpus="$extraction_dir/artifact-string-corpus.txt"
  while IFS= read -r dex; do
    "$dexdump" -d "$dex" >> "$string_corpus"
  done < <(find "$extraction_dir" -type f -name '*.dex' -print | sort)
  "$aapt" dump --values resources "$apk" >> "$string_corpus" 2>/dev/null

  failed=0
  checked=0
  exempted=0
  debug_preview_exempted=0
  if ! grep -aFRl -- "$shared_identity" "$extraction_dir" >/dev/null; then
    echo "SHARED IDENTITY MISSING from $apk: $shared_identity" >&2
    failed=1
  fi
  for obsolete in "${obsolete_identities[@]}"; do
    if grep -aFRl -- "$obsolete" "$extraction_dir" >/dev/null; then
      echo "OBSOLETE ALMANAC IDENTITY in $apk: $obsolete" >&2
      failed=1
    fi
  done
  while IFS= read -r needle; do
    checked=$((checked + 1))
    if [[ "$mode" == "absent" ]] &&
      printf '%s\n' "${production_schema_allowlist[@]}" | LC_ALL=C grep -Fx -- "$needle" >/dev/null; then
      exempted=$((exempted + 1))
      continue
    fi
    if [[ "$mode" == "absent" && "$apk" == *"/production/debug/"* ]] &&
      printf '%s\n' "${research_preview_debug_allowlist[@]}" | LC_ALL=C grep -Fx -- "$needle" >/dev/null; then
      debug_preview_exempted=$((debug_preview_exempted + 1))
      continue
    fi
    if LC_ALL=C grep -F -- "\"$needle\"" "$string_corpus" >/dev/null; then
      if [[ "$mode" == "absent" ]]; then
        echo "DENYLIST MATCH in $apk: $needle" >&2
        failed=1
      fi
    elif [[ "$mode" == "present" ]]; then
      echo "POSITIVE CONTROL MISSING from $apk: $needle" >&2
      failed=1
    fi
  done < <(printf '%s\n' "${denylist[@]}")

  identity_checked=0
  while IFS= read -r identity; do
    identity_checked=$((identity_checked + 1))
    if LC_ALL=C grep -aFRl -- "$identity" "$extraction_dir" >/dev/null; then
      if [[ "$mode" == "absent" ]]; then
        echo "DEMO IDENTITY MATCH in $apk: $identity" >&2
        failed=1
      fi
    elif [[ "$mode" == "present" ]]; then
      echo "POSITIVE CONTROL MISSING demo identity from $apk: $identity" >&2
      failed=1
    fi
  done < <(printf '%s\n' "${identity_denylist[@]}")

  if [[ "$mode" == "absent" ]] &&
    find "$extraction_dir" -type f -print | LC_ALL=C grep -E '/(demo|timeline)(/|[^/]*$)' >/dev/null; then
    echo "DENYLIST MATCH in $apk: demo/timeline artifact path" >&2
    failed=1
  fi

  rm -rf "$extraction_dir"
  trap - EXIT
  if ((failed)); then
    exit 1
  fi
  if [[ "$mode" == "present" ]]; then
    echo "Demo positive control found all $checked fixture entries and $identity_checked identities: $apk"
  else
    actual_checked=$((checked - exempted - debug_preview_exempted))
    echo "Production artifact denylist passed $actual_checked checked fixture entries and $identity_checked identities ($exempted shared-schema exemptions; $debug_preview_exempted debug research-preview exemptions): $apk"
  fi
done
