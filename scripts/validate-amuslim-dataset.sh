#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
catalog="${1:-$repo_root/datasets/amuslim/starter-v0.2/catalog.draft.json}"
schema="$(cd "$(dirname "$catalog")" && pwd)/catalog.schema.json"

if command -v ajv >/dev/null 2>&1; then
  if ! validation_output="$(
    ajv validate --spec=draft2020 --strict=false -s "$schema" -d "$catalog" 2>&1
  )"; then
    echo "$validation_output" >&2
    exit 1
  fi
elif command -v npx >/dev/null 2>&1; then
  if ! validation_output="$(
    npx --yes ajv-cli@5.0.0 validate \
      --spec=draft2020 \
      --strict=false \
      -s "$schema" \
      -d "$catalog" 2>&1
  )"; then
    echo "$validation_output" >&2
    exit 1
  fi
else
  echo "Ajv or npx is required for Draft 2020-12 schema validation" >&2
  exit 1
fi

jq -e '
  .schemaVersion == 2 and
  .catalogId == "amuslim.starter" and
  .contentVersion == "0.2.0-draft" and
  .purpose == "RESEARCH_CANDIDATE" and
  .lifecycle == "IN_REVIEW" and
  .approvals == [] and
  (.entries | length == 11) and
  (.evidence | length == 14) and
  ([.entries[].id] | length == (unique | length)) and
  ([.entries[].actionClaim.id, .entries[].timingClaim.id] | length == (unique | length)) and
  ([.evidence[].id] | length == (unique | length)) and
  (all(.entries[]; .lifecycle == "IN_REVIEW")) and
  (all(.entries[]; .actionClaim.reviewStatus == "UNREVIEWED")) and
  (all(.entries[]; .timingClaim.reviewStatus == "UNREVIEWED")) and
  (all(.entries[]; .actionClaim.claimType == "ACT")) and
  (all(.entries[]; .timingClaim.claimType == "TIMING")) and
  ([.entries[] | select(.id == "deed.daily.duha-two-rakah")] | length == 1) and
  ([.entries[] | .actionClaim.variationEvidenceIds[]?] | sort == [
    "evidence.muslim.719a"
  ]) and
  ([.entries[] | .timingClaim.variationEvidenceIds[]?] | sort == [
    "evidence.muslim.748b"
  ]) and
  ([.evidence[].id] | index("evidence.abudawud.1522") != null) and
  ([.evidence[].id] | index("evidence.abudawud.2449") != null) and
  ([.evidence[].id] | index("evidence.abudawud.2418") == null)
' "$catalog" >/dev/null

jq -e '
  all(
    .entries[];
    (.actionClaim.corroborativeEvidenceIds // []) as $action_corroborative |
    (.timingClaim.corroborativeEvidenceIds // []) as $timing_corroborative |
    ([.actionClaim.evidenceIds[] as $id | select(($action_corroborative | index($id)) != null)] |
      length == 0) and
    ([.timingClaim.evidenceIds[] as $id | select(($timing_corroborative | index($id)) != null)] |
      length == 0)
  )
' "$catalog" >/dev/null

jq -e '
  all(
    .entries[];
    if .timingClaim.payload.expression.parameters.weekdays? then
      ([.timingClaim.payload.recurrence.weekdays[]] | sort) ==
        ([.timingClaim.payload.expression.parameters.weekdays[]] | sort)
    elif .timingClaim.payload.expression.parameters.weekday? then
      .timingClaim.payload.expression.parameters.weekday as $weekday |
      .timingClaim.payload.recurrence.weekdays | index($weekday) != null
    else
      true
    end
  )
' "$catalog" >/dev/null

jq -e '
  [.evidence[].id] as $evidence_ids |
  all(
    .entries[];
    all(
      (
        .actionClaim.evidenceIds +
        (.actionClaim.corroborativeEvidenceIds // []) +
        (.actionClaim.variationEvidenceIds // []) +
        .timingClaim.evidenceIds +
        (.timingClaim.corroborativeEvidenceIds // []) +
        (.timingClaim.variationEvidenceIds // [])
      )[];
      . as $id | $evidence_ids | index($id) != null
    )
  )
' "$catalog" >/dev/null

jq -e '
  (.evidence | map({key: .id, value: .supportsClaimTypes}) | from_entries) as $supports |
  all(
    .entries[];
    all(
      (.actionClaim.evidenceIds + (.actionClaim.corroborativeEvidenceIds // []) + (.actionClaim.variationEvidenceIds // []))[];
      . as $id | $supports[$id] | index("ACT") != null
    ) and
    all(
      (.timingClaim.evidenceIds + (.timingClaim.corroborativeEvidenceIds // []) + (.timingClaim.variationEvidenceIds // []))[];
      . as $id | $supports[$id] | index("TIMING") != null
    )
  )
' "$catalog" >/dev/null

jq -e '
  [
    .entries[] |
    (
      .actionClaim.evidenceIds +
      (.actionClaim.corroborativeEvidenceIds // []) +
      (.actionClaim.variationEvidenceIds // []) +
      .timingClaim.evidenceIds +
      (.timingClaim.corroborativeEvidenceIds // []) +
      (.timingClaim.variationEvidenceIds // [])
    )[]
  ] as $referenced |
  all(.evidence[].id; . as $id | $referenced | index($id) != null)
' "$catalog" >/dev/null

if jq -r '.. | strings' "$catalog" | grep -Eq '^[0-2][0-9]:[0-5][0-9]$'; then
  echo "raw clock time found in religious catalogue" >&2
  exit 1
fi

if jq -r '.. | objects | keys[]' "$catalog" | grep -Eq '^(priority|ranking|recommendedScore|rawClockTime)$'; then
  echo "forbidden ranking or raw-time field found" >&2
  exit 1
fi

echo "aMuslim starter dataset structure is valid"
