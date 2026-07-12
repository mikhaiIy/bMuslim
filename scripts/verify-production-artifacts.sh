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
  while IFS= read -r needle; do
    checked=$((checked + 1))
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
    echo "Production artifact denylist passed all $checked fixture entries and $identity_checked identities: $apk"
  fi
done
