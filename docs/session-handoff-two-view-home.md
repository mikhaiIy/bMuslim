# Session handoff: two-view home transition

## Current preserved milestone

The native Android home is a Compose-based devotional time instrument. It currently has:

- a rotating half-crescent 24-hour wheel with fixed selected-time pointer;
- three semantic rails for hours, obligatory prayer anchors, and deed windows;
- an independent actual-now mark;
- sun and moon on one concentric celestial rail;
- continuous authored time-of-day atmosphere;
- explicit active, obligatory, next-deed, and day-state modeling;
- a compact moment-summary strip and an open ledger-style deed detail;
- adaptive short-screen behavior, adjustable semantics, saved completion state, and tests.

The previous design loop scored 57 → 72 → 81 with its independent judge. Build, lint,
five unit tests, contrast sampling, and `git diff --check` passed. A real 360×640 capture at
1.3× font scale remains useful release evidence because no emulator/AVD was configured.

## Protected visual identity

- Malaysian contemporary prayer almanac: pine, mineral atmosphere, paper, saffron accent.
- Fixed saffron pointer; prayer nodes; deed-window arcs and act beads.
- Time-proportional geometry and deterministic fardhu-over-sunnah priority.
- Sacred Arabic → translation → source hierarchy without decorative Islamic clichés.
- No mosque silhouettes, ornamental crescent wallpaper, glassmorphism, card stacks, or pills.

## New goal: two-view home

The app should open in a calm **glance state**:

- the full circular/near-circular time wheel is centered in the screen;
- it shows the existing wheel information and all five prayer times;
- no moment-summary strip, ledger, or bottom detail is visible;
- the wheel remains scrubbable and accessible.

An intentional user action changes it into the **detail state**:

- the same wheel—not a replacement—moves upward and resolves into the existing cropped
  detailed position;
- the moment summary, active-deed context, ledger, and bottom navigation reveal smoothly;
- selected time and semantic focus are preserved through the transition;
- reverse/collapse behavior must be designed, not assumed.

The trigger is deliberately undecided. Candidate triggers to test include:

1. tapping the fixed pointer/current selection;
2. tapping the active prayer or deed arc;
3. a small explicit “Lihat amalan” affordance tied to the selected time;
4. upward drag on the wheel/folio boundary.

The trigger must be discoverable, accessible, reversible, and must not conflict with horizontal
time scrubbing. Avoid automatic expansion on first touch because it would punish exploration.

## Required two-loop workflow

### Phase A: design loop

1. SOL 5.6 xhigh orchestrates interaction hypotheses and image/motion states.
2. Generate separate screen images for glance, transition beats, detail, and reverse state.
3. Claude Code CLI Fable-5 judges creativity, user intent, discoverability, motion continuity,
   accessibility, and fidelity to protected identity; every criticism must include direction.
4. SOL meta-evaluates Fable's judgment, rates it, resolves disagreements, and requests another
   design iteration until a direction passes.

### Phase B: execution loop

1. SOL converts the chosen design into a bounded native implementation brief.
2. Terra-medium implements in Compose with tests and verification.
3. Claude Code CLI Fable-5 judges the actual code/evidence and gives constructive direction.
4. SOL rates that judgment and issues bounded corrections until implementation passes.

## Likely implementation surface

Primary files:

- `app/src/main/java/com/bmuslim/app/ui/timeline/TimelineScreen.kt`
- `app/src/main/java/com/bmuslim/app/ui/timeline/TimelineModel.kt`
- `app/src/test/java/com/bmuslim/app/ui/timeline/TimelineModelTest.kt`

Prefer a single stateful scene with shared geometry and progress-driven layout rather than two
duplicated screens. A normalized expansion progress should drive wheel center/radius, header
opacity, summary/ledger reveal, navigation reveal, and semantics. Reduced-motion users receive
an immediate but coherent state change.

## Verification commands

```bash
JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:testDebugUnitTest :app:assembleDebug --no-parallel

JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:lintDebug --no-parallel

git diff --check
```

## Existing references

- `design/concepts/moment-summary-1500-v2.png`
- `design/concepts/maghrib-priority-1945-v2.png`
- `design/concepts/zikir-reading-0620-v2.png`
- `design/concepts/compact-home-360x800-v2.png`
- `docs/agent-commands-v2.md`

Generated images are composition references only. Runtime times, rulings, Arabic, translations,
and citations must come from reviewed native model data.
