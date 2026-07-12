# Cycle 2, wave 2: semantic contract enumeration

## Result

The isolated model reproduced **23,400/23,400** stipulated contract combinations with zero internal
contradictions. Several owner/reachability values are assigned by the proposed model, so this is a
contract enumerator—not validation that the current Compose tree implements those values. It does
not modify/import app or Gradle production code and does not claim accessibility/runtime proof.

Artifacts:

- `tools/research/human-taste-refinement/semantics/check_semantics_state.py`
- `tools/research/human-taste-refinement/semantics/README.md`
- `tools/research/human-taste-refinement/semantics/exhaustive-results.csv.gz`
- `tools/research/human-taste-refinement/semantics/summary.json`
- `tools/research/human-taste-refinement/semantics/current-code-counterexamples.csv`
- `tools/research/human-taste-refinement/semantics/range-results.csv`
- `tools/research/human-taste-refinement/semantics/cyclic-adjust-results.csv`

Reproduce:

```bash
python3 tools/research/human-taste-refinement/semantics/check_semantics_state.py
```

## Coverage

- 75 minutes covering every overlap interior and first−1/first/last/last+1.
- Glance, scrub, settle, expand/collapse .25/.5/.75, detail.
- Reversal .1/.35/.6/.9 with both targets and last-settled states.
- Normal, animator-zero, and app-reduced policies.
- Regular/compact × normal/high-type semantic reachability.
- Absolute requests -1, 0, 2, 3, 1435, 1439, 1440.
- Explicit cyclic midnight and interior adjustment.

## Validated model contract

```text
value owner = 1
canonical adjustable owner = 1
semantic clock nodes = 0
spoken order = time → active fardhu once → context → day
ownership = minute-driven, never progress-driven
normal transition subtree = lastSettled
zero/app-reduced subtree = target endpoint atomically
hiding = structural, never inferred from alpha
detail action = reachable only in valid detail
collapse = outgoing may anchor, actions disabled
absolute SetProgress = clamp/quantize within 00:00..23:55
explicit earlier/later = cyclic 23:55 ↔ 00:00
```

Boundary receipts include no fardhu at 05:58; Subuh first/once 05:59–06:19 and zikir promotion at
06:20; Zohor/Dhuha order at 13:21/13:22; Maghrib/zikir at 19:29/19:30.

## Thirteen current-code counterexamples

- 1439/1440 quantize to 1440 and wrap 00:00 instead of safe 23:55 absolute value.
- Summary becomes composed/focusable just above .357; sampled failures .36/.5/.75/.9.
- At each active-fardhu boundary, `wheelDescription` speaks fardhu in active and prayer text.
- Visible header clock emits normal `Text` semantics, violating zero semantic clocks.
- Detail subtree relies partly on alpha; alpha is not semantic ownership.

## Version caveats

Project resolves Compose UI 1.7.6/BOM 2024.12.01. Compatible API is `invisibleToUser()`, while
newer docs name `hideFromAccessibility()`. Redundant clock meaning should be cleared/consolidated.
Alpha-zero may be filtered by Android but nodes remain in Compose tree; positive alpha is exposed.
FocusRequester is input focus, not TalkBack proof. `announceForAccessibility` is deprecated API 36.
Animator enabled may be false from Battery Saver. Automated accessibility checks require Compose
1.8+.

## Manual validation remaining

Google and OEM TalkBack; API 26/33/35/36; normal/animator-zero/Battery Saver/app-reduced; TalkBack
range actions; Switch Access; keyboard/D-pad; compact/high type; reversals at four points. The
checker proves internal contract consistency only. The persisted current-code counterexamples are
the stronger direct evidence; neither the pass count nor constant owner fields prove service
speech/focus, actual semantics-tree ownership, high-type reachability, or visual geometry.
