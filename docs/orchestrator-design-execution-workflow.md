# Orchestrator, design, execution, and judging workflow

This document describes the reusable multi-agent workflow used to design and implement the
bMuslim two-view home experience. It is intended to be sufficient context for starting a clean
session without relying on the original conversation.

The workflow deliberately separates **design discovery** from **implementation**. Each phase has
its own feedback loop, artifacts, score, and stopping gate. A concept does not enter production
because it looks attractive, and working code does not pass merely because it compiles.

## Agent setup

| Role | Preferred model | Responsibility | May edit app code? |
| --- | --- | --- | --- |
| Creative orchestrator | GPT-5.6 SOL, xhigh/max | Owns user intent, product direction, constraints, priorities, and final decisions | No during design; only directs execution |
| Design renderer | Built-in image-generation model | Produces separate, readable visual states from the orchestrator's concepts | No |
| Creative design judge | Claude Code CLI, Fable-5 high | Critiques concepts, scores them, and supplies constructive alternatives | No |
| Native executor | GPT-5.6 Terra, medium | Implements the locked direction in Android/Compose and verifies it | Yes |
| Implementation judge | Claude Code CLI, Fable-5 high | Audits actual code and evidence; reports defects and bounded improvements | No |
| Meta-judge | The same SOL orchestrator | Rates Fable's judgment, resolves disagreements, and decides the next bounded iteration | No |

The Fable judge is independent. It should not be asked to defend the orchestrator's concept or
reward effort. SOL remains accountable for the final decision: Fable advises and challenges;
SOL accepts, modifies, or rejects that advice with reasons.

Do not run multiple agents against the same writable implementation files. Design work and
judging can run independently, but only the executor should modify app code during an execution
round.

## Workflow at a glance

```text
DESIGN LOOP

User story
   ↓
SOL creative direction
   ↓
Image-generation states
   ↓
Fable creative judgment + directions
   ↓
SOL rates the judgment and chooses corrections
   └──────────── repeat until DESIGN LOCK


EXECUTION LOOP

Locked design + acceptance criteria
   ↓
SOL implementation brief
   ↓
Terra implementation + tests + evidence
   ↓
Fable code/product judgment + directions
   ↓
SOL rates the judgment and issues bounded corrections
   └──────────── repeat until IMPLEMENTATION PASS
```

Never merge the loops into a vague cycle. Design-loop feedback changes the proposed experience.
Execution-loop feedback changes the implementation of the already locked experience. If
implementation reveals a fundamental concept failure, explicitly reopen the design loop.

## Phase 0: preserve the starting point

Before exploration:

1. Inspect `git status`, the current branch, recent commits, and available project docs.
2. Keep unrelated user changes out of the milestone commit.
3. Verify the existing build where practical.
4. Commit the known-good starting point.
5. Create a feature branch for the new design.
6. Write or update a durable handoff containing the current product identity, user story,
   constraints, relevant paths, verification commands, and remaining unknowns.

Example:

```bash
git status --short
git log -5 --oneline --decorate
git switch -c feature/<feature-name>
```

Do not use destructive cleanup commands on a dirty worktree. A clean feature commit means the
feature's patch is isolated; it does not mean deleting unrelated local work.

## Phase A: design loop

### A1. SOL creates the design round

SOL converts the user's story into a small number of meaningfully different interaction
directions. Each direction should define:

- the product idea and emotional intent;
- the initial, transition, resolved, and reverse states;
- the trigger and how it avoids gesture conflicts;
- what remains invariant across the transition;
- accessibility and reduced-motion behavior;
- compact-screen behavior;
- risks that the judge should challenge.

Three strong alternatives are usually enough. Variants that only change color or decoration do
not count as different directions.

#### SOL design-orchestrator command

```text
You are the creative director, interaction architect, and project manager for a native Android
design exploration. Read the user story, current implementation, product handoff, prior evidence,
and protected visual identity. Do not edit implementation files.

Create three meaningfully different but implementable interaction directions. For each, specify
the entry state, trigger, transition beats, resolved state, reverse action, accessibility,
reduced-motion behavior, compact-screen behavior, signature visual idea, and main risk. Protect
the product's data truth and cultural character. Avoid generic Material layouts, decorative
Islamic clichés, glassmorphism, arbitrary card stacks, and concepts that confuse scrubbing with
navigation.

Select the exact images needed to judge each direction. Finish with precise prompts for separate,
readable mobile-screen images rather than a compressed mood board. Do not choose a winner yet.
```

### A2. Generate visual and motion evidence

Use image generation for design questions that genuinely benefit from visual evidence. Generate
separate images for states such as:

- glance/entry;
- early or mid-transition;
- resolved detail;
- reverse/collapse;
- compact-screen adaptation.

Keep a consistent device size, content, palette, typography hierarchy, selected time, and wheel
orientation across sequential states. Change only what the proposed transition requires. Save
each direction in its own clearly named directory.

Generated images are composition references, not sources of truth. They must not invent prayer
times, rulings, Arabic, translations, citations, or completion state for production.

If the generator repeatedly fails to demonstrate a precise geometric transition, record that as
an evidence limitation. Do not keep generating cosmetically different images forever; move the
question into deterministic native geometry and tests.

### A3. Fable judges creatively

Run Claude Code from the repository root and give it exact artifact paths. Read-only tools keep
the judge independent:

```bash
claude -p \
  --model fable \
  --effort high \
  --tools Read \
  "$(cat /path/to/design-judge-prompt.txt)"
```

The judge prompt must ask for both criticism and direction:

```text
You are an independent, adversarial product-design judge and constructive creative director.
You did not create these concepts and must not protect them. Read the user story, protected
identity, current product evidence, and every named concept image.

Score each direction for user-intent fidelity, immediate comprehension, trigger discoverability,
gesture safety, continuity of the same instrument, transition quality, reverse behavior,
accessibility, compact-screen viability, cultural/product character, originality, and native
implementability. Separate evidence-backed defects from taste judgments.

For every criticism, provide a concrete directional improvement. Identify the strongest concept,
useful elements worth borrowing from others, P0/P1 risks, missing evidence, and the smallest next
design round needed. Give numeric scores and an explicit PASS, ITERATE, or REJECT decision. Do not
edit files and do not judge only the written brief when visual evidence exists.
```

Save Fable's complete response in `docs/`; do not rely on terminal history.

### A4. SOL meta-judges Fable

SOL now evaluates the quality of Fable's review rather than blindly following it.

#### SOL meta-judge command

```text
Act as the accountable design orchestrator and meta-judge. Read the original user story, all
concept evidence, and Fable's independent judgment. Rate Fable's review out of 40 for evidence
use, user-intent fidelity, creative value, technical feasibility, and prioritization.

For every important Fable recommendation, mark ACCEPT, MODIFY, or REJECT and explain why. Resolve
contradictions, distinguish generator failure from concept failure, and prevent optional polish
from displacing the core interaction. Then issue one bounded next-round brief or declare DESIGN
LOCKED. A lock must name the winning direction, borrowed elements, rejected ideas, protected
invariants, exact reference images, interaction behavior, and measurable acceptance criteria.
Do not edit implementation files.
```

### A5. Design stopping gate

Continue the A → B → C → A loop only while another round can answer a material question. Lock the
design when:

- the user story is represented end to end;
- the trigger is discoverable and does not conflict with the primary gesture;
- entry, transition, destination, and reverse are defined;
- the same core instrument is visibly and technically continuous;
- accessibility, reduced motion, and compact layouts have explicit behavior;
- Fable has no unresolved P0 issue;
- SOL has rated the judgment and written a decisive design lock.

The result of Phase A is a **locked execution brief**, not production code.

## Phase B: execution loop

### B1. SOL writes the native execution brief

The brief should translate the design lock into:

- protected invariants and forbidden shortcuts;
- state ownership and interruption behavior;
- exact motion timing and easing where important;
- geometry endpoints and reveal ordering;
- semantics, focus, announcements, and Android Back behavior;
- compact and font-scale budgets;
- stable test tags and pure geometry functions;
- previews or device states;
- build, test, lint, and evidence requirements.

The brief should be bounded enough that Terra can execute without redesigning the feature.

### B2. Terra implements

#### Terra executor command

```text
You are the pragmatic native Android design engineer. Read the locked execution brief, current
Compose implementation, model tests, and exact reference images before editing.

Implement the locked direction faithfully with the smallest coherent architecture. Preserve one
source of truth for user data and interaction state. Prefer one shared renderer and progress-driven
geometry over duplicated screens or crossfaded facsimiles. Generated images are composition
references only; production content must come from reviewed native data.

Compile early. Add pure JVM tests for deterministic state, geometry, ordering, and compact-layout
budgets where possible. Add stable semantics/test tags. Verify unit tests, APK assembly, lint, and
git diff --check. Report exactly what changed, evidence produced, and any missing emulator/device
proof. Do not silently broaden the feature or modify unrelated files.
```

Only Terra writes implementation files during this phase. SOL and Fable remain read-only.

### B3. Fable judges the actual implementation

Use Fable high again, but point it at the code, tests, build evidence, and available captures—not
only the execution brief.

```text
You are an independent implementation judge, adversarial product reviewer, and constructive
creative critic. Read the original user story, design lock, implementation diff, relevant source
files, tests, build/lint evidence, and any device or preview captures. Do not edit files.

Judge whether the implemented experience—not merely the plan—preserves the user story and locked
interaction. Score story fidelity, trigger safety, continuity of one instrument, geometry and
motion, reveal order, data truth, visual identity, accessibility/focus, compact behavior, and
code/test quality. Identify P0, P1, and P2 findings separately.

Every finding must include evidence, user impact, and a bounded correction with an acceptance
test. Do not promote optional taste preferences to blockers. Return a score out of 100 and an
explicit PASS or ITERATE decision. Missing device evidence should be labeled missing evidence,
not automatically treated as either a defect or a pass.
```

Save every implementation judgment as a new round document.

### B4. SOL rates the implementation judgment

```text
Act as the accountable orchestrator and implementation meta-judge. Read the locked design, actual
diff, test/build evidence, and Fable's implementation review. Rate Fable's judgment out of 40 for
evidence, severity calibration, user-story fidelity, creative usefulness, and feasibility.

Mark each P0/P1 recommendation ACCEPT, MODIFY, or REJECT with concrete reasoning. Correct factual
errors by checking the implementation. Issue only bounded executor corrections that protect the
design lock. Optional P2 polish must not restart the loop unless it exposes a core user failure.
Declare IMPLEMENTATION PASS only when no accepted P0/P1 remains and verification is current.
```

Terra performs the bounded corrections, regenerates evidence, and returns the work to Fable. The
D → E → F → D loop repeats until it passes.

### B5. Implementation stopping gate

Pass the implementation when:

- no accepted P0 or P1 issue remains;
- the user story works in both directions;
- state and data survive the transition correctly;
- accessibility semantics, focus, reduced motion, and system Back are coherent;
- compact behavior has credible code/test evidence;
- tests, assembly, lint, and `git diff --check` pass;
- SOL has rated Fable's final review and explicitly accepted the result.

Device captures may remain optional evidence when no emulator/device exists. Record that honestly.

## Scoring and severity conventions

Use consistent labels across both loops:

- **P0:** prevents the primary story, corrupts data, creates a serious accessibility failure, or
  makes the feature unsafe/unusable. Must be fixed.
- **P1:** substantial comprehension, interaction, layout, reliability, or accessibility defect.
  Must be fixed before pass.
- **P2:** valuable polish, additional evidence, or resilience improvement. Track, but do not keep
  the loop open automatically.

Recommended gates:

- Fable design score: use as comparative evidence, not an automatic winner.
- Fable implementation pass: no unresolved P0/P1, normally at least 80/100.
- SOL meta-rating: evaluate the judge out of 40; 30+ indicates a useful, well-calibrated review.

Numbers support decisions; they do not replace explicit reasoning.

## Required artifacts

Keep these artifacts in the repository so the workflow survives a context reset:

```text
docs/
  session-handoff-<feature>.md
  <feature>-execution-brief.md
  <feature>-fable-design-round-1.md
  <feature>-fable-design-round-2.md
  <feature>-fable-implementation-round-1.md
  <feature>-fable-implementation-round-2.md

design/<feature>/
  direction-a/
  direction-b/
  direction-c/
  round-2/
```

Each round document should include inputs, score, P0/P1/P2 findings, directions, decision, and
the next bounded action. The handoff should name the current branch and commits, protected product
identity, completed verification, uncommitted unrelated files, and exact next step.

## Final verification and commit

For this Android project:

```bash
JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:testDebugUnitTest :app:assembleDebug --no-parallel

JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:lintDebug --no-parallel

git diff --check
git status --short
git diff --cached --check
```

Stage only the feature's files, inspect the staged diff, and create a focused feature commit. The
final report should give the branch, baseline and feature commit hashes, final Fable and SOL
scores, verification results, APK path, important artifacts, and any evidence that remains
unavailable.

## Example: two-view home result

The workflow produced the **Meridian–Aperture hybrid**:

- centered, complete prayer wheel on cold launch;
- a separate `Amalan pada HH:mm` action outside the scrub surface;
- the same wheel translating upward and aperture-cropping with zero angular rotation;
- detail content entering only after space is vacated;
- explicit close action and Android Back support;
- compact, reduced-motion, focus, announcement, and state-preservation behavior.

Recorded outcomes:

- Fable design review: 83/100;
- Fable implementation review: 85/100, no P0/P1;
- SOL final meta-rating: 35/40;
- 13 unit tests, APK assembly, lint, and `git diff --check` passed.

The feature-specific handoff and locked brief remain in:

- `docs/session-handoff-two-view-home.md`
- `docs/two-view-execution-brief.md`
