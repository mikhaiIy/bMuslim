# Research, orchestration, design, execution, and judging workflow

This document describes the reusable multi-agent workflow used to design and implement the
bMuslim two-view home experience. It is intended to be sufficient context for starting a clean
session without relying on the original conversation.

The workflow deliberately separates **research**, **design discovery**, and **implementation**.
Each phase has its own feedback loop, artifacts, score, and stopping gate. Inspiration does not
become direction until it is synthesized into useful principles, a concept does not enter
production because it looks attractive, and working code does not pass merely because it compiles.

## Agent setup

| Role | Preferred model | Responsibility | May edit app code? |
| --- | --- | --- | --- |
| Creative orchestrator | GPT-5.6 SOL, xhigh/max | Owns user intent, product direction, constraints, priorities, and final decisions | No during design; only directs execution |
| Research scouts | GPT-5.6 Terra, medium (fresh agents each wave) | Search distinct evidence lanes and return cited, design-relevant findings | No |
| Research synthesizer | GPT-5.6 SOL, high (separate agent) | Compares scout findings, extracts principles, finds gaps, and directs the next wave | No |
| Research judge | GPT-5.6 SOL, high (fresh independent agent) | Evaluates research quality after every two waves and decides whether it can enter design | No |
| Design renderer | Built-in image-generation model | Produces separate, readable visual states from the orchestrator's concepts | No |
| Creative design judge | GPT-5.6 SOL, high (separate agent) | Critiques concepts, scores them, and supplies constructive alternatives | No |
| Native executor | GPT-5.6 Terra, medium | Implements the locked direction in Android/Compose and verifies it | Yes |
| Implementation judge | GPT-5.6 SOL, high (same independent judge) | Audits actual code and evidence; reports defects and bounded improvements | No |
| Meta-judge | The original SOL xhigh/max orchestrator | Rates the judge's work, resolves disagreements, and decides the next bounded iteration | No |

The SOL-high judge must be a different agent from the SOL-xhigh/max orchestrator. It should not be
asked to defend the orchestrator's concept or reward effort. The orchestrator remains accountable
for the final decision: the independent judge advises and challenges; the orchestrator accepts,
modifies, or rejects that advice with reasons.

Do not run multiple agents against the same writable implementation files. Design work and
judging can run independently, but only the executor should modify app code during an execution
round. With four total concurrency slots, run at most three scouts beside the root orchestrator;
finish the scout wave before starting the SOL-high synthesis or judgment turn.

## Workflow at a glance

```text
RESEARCH LOOP

User story + observed defects
   ↓
2–3 fresh Terra-medium research scouts on separate evidence lanes
   ↓
SOL-high synthesis: principles, contradictions, gaps, next-wave questions
   ↓
2–3 new clean-context scouts using the synthesis
   ↓
Independent SOL-high research judgment after two waves
   ↓
SOL-xhigh/max orchestrator accepts, narrows, or repeats
   └──────────── repeat in two-wave cycles until RESEARCH PASS


DESIGN LOOP

User story
   ↓
SOL creative direction
   ↓
Image-generation states
   ↓
Independent SOL-high creative judgment + directions
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
Independent SOL-high code/product judgment + directions
   ↓
SOL rates the judgment and issues bounded corrections
   └──────────── repeat until IMPLEMENTATION PASS
```

Never merge the loops into a vague cycle. Research-loop feedback changes what the team knows and
which principles are credible. Design-loop feedback changes the proposed experience.
Execution-loop feedback changes the implementation of the already locked experience. If design
reveals a missing knowledge question, reopen research explicitly. If implementation reveals a
fundamental concept failure, explicitly reopen the design loop.

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

## Phase R: two-wave human-taste research loop

The purpose of research is not to collect fashionable screenshots. It is to improve the agents'
ability to notice what a careful human designer notices: occlusion during motion, interrupted
reading, awkward clipping, ambiguous gesture ownership, poor rhythm, mechanical easing, weak
shape relationships, lost focus, and moments that stop feeling calm under interaction.

### R1. The orchestrator defines independent evidence lanes

Start each cycle with two or three bounded research lanes. For the bMuslim refinement, use:

1. **Motion and continuity:** high-quality mobile motion systems, spatial continuity, choreography,
   easing, interruption, clipping, progressive disclosure, and transition-state legibility.
2. **Radial information and occlusion:** clocks, dials, scrubbers, circular timelines, label
   collision/placement, edge clipping, adaptive annotations, and strategies for keeping selected
   information readable while geometry moves.
3. **Human taste and calm craft:** expert workflows for critique, animation polish, zen or calming
   interfaces, contemplative products, typography/rhythm, microinteraction restraint, and methods
   that help AI design agents produce less generic work.

Broaden beyond Android when a transferable principle is credible. Good sources may include native
platform guidance, conference talks, tutorial videos, case studies, practitioner essays, research
papers, respected open-source code, design-system motion specifications, and well-documented AI
design workflows. Do not limit discovery to “mobile app AI prompts.”

### R2. Fresh Terra-medium scouts search the web

Each scout owns one lane and starts with a clean context. Scouts must search rather than rely on
memory. They return compact evidence, not a link dump.

#### Research-scout command

```text
You are a focused design-research scout. Search the live web for the assigned evidence lane. Use
credible original sources where possible: official platform/design guidance, talks or tutorials
from experienced practitioners, case studies, papers, maintained repositories, and specific
workflows with visible outcomes. Community posts are useful only when the author demonstrates a
concrete method or artifact.

For every useful source, provide its title, author/organization, URL, source type, publication or
update date when available, the exact relevant idea, and why it transfers to this Android product.
For videos, summarize the relevant segment and provide timestamps when evidence allows. For code
or skills, identify the exact file/pattern instead of merely naming the repository.

Extract no more than ten strong findings. For each, translate the evidence into a testable design
heuristic and name its misuse risk. Explicitly cover transition states, not only static endpoints.
End with contradictions, unanswered questions, and three recommendations for the next research
wave. Do not edit project files and do not propose final UI screens.
```

The root agent saves each response under `docs/research/<feature>/cycle-N/wave-N/`. Every factual
or borrowed claim must retain a direct source URL so later agents can verify it.

### R3. SOL-high synthesizes wave one

After the first scout wave finishes, start a separate SOL-high synthesis agent. Give it all scout
reports, the current source code/visual evidence, and the actual user story.

#### Research-synthesizer command

```text
You are the senior design-research synthesizer. Read every scout report and inspect the current
product evidence. Do not reward source volume. Evaluate source authority, specificity, transfer
risk, contradictions, and relevance to the observed product defects.

Cluster the evidence into principles a designer or engineer can act on. Separate sourced facts,
cross-source inference, and your own hypothesis. Convert the strongest findings into an interaction
audit checklist and measurable design questions. Identify shallow claims, missing transition
states, and places where website techniques do not transfer safely to native mobile.

Produce a focused brief for a second wave of fresh scouts. The next wave should verify disputed
ideas, go deeper on the highest-value gaps, and look for implementation-level examples. Do not
select a final design and do not edit files.
```

### R4. Fresh scouts run a synthesis-informed second wave

Do not reuse the first-wave agent contexts. Start new Terra-medium agents and supply only the
user story, current evidence, first-wave synthesis, and their new narrow question. Suitable second
wave tasks include:

- verifying collision and occlusion strategies in production radial interfaces;
- finding frame-by-frame examples of text surviving translation, crop, and scale changes;
- studying calm motion that remains clear under interruption and reduced motion;
- locating code-level Compose or native examples for coordinate-aware label placement;
- testing whether AI “taste” workflows actually include critique rubrics, visual comparison,
  constraint checking, and iteration rather than prompt decoration.

The same citation and testable-heuristic requirements apply.

### R5. An independent SOL-high judge evaluates every two waves

After wave two, start a fresh SOL-high judge that was neither a scout nor the synthesizer. Give it
the latest reports, the synthesis, selected earlier evidence, the user story, and observed defects.

#### Research-judge command

```text
You are an independent research-quality judge and constructive design critic. Audit this two-wave
research cycle against the user's real product problem. You did not produce the research and must
not defend it.

Score source quality, evidence diversity, depth, contradiction handling, transfer to native mobile,
attention to in-between animation states, human-craft usefulness, calm-product relevance, and
conversion into testable design principles. Check important claims against their cited sources
when possible. Identify unsupported trend language, cargo-cult techniques, duplicated findings,
and advice that would make the interface busier rather than calmer.

For every weakness, give a precise direction: another source class, a narrower question, a missing
counterexample, or a stronger acceptance test. Return a score out of 100, P0/P1/P2 research gaps,
and RESEARCH PASS or ANOTHER TWO-WAVE CYCLE. Passing requires credible principles that can constrain
the image-led design phase, not merely an impressive bibliography. Do not edit files or design the
final screen.
```

The SOL-xhigh/max orchestrator meta-rates this judgment, marks major recommendations ACCEPT,
MODIFY, or REJECT, and either starts another two-wave research cycle or publishes a research lock.

### R6. Research stopping gate

Pass research into design only when:

- important claims have direct, inspectable sources;
- multiple evidence types are represented instead of one visual trend ecosystem;
- findings address static endpoints and in-between animation states;
- conflicts and platform-transfer risks are explicit;
- insights have become testable heuristics, audit questions, and anti-patterns;
- the active product defects have plausible, evidence-backed design approaches;
- the independent judge has no accepted P0/P1 research gap;
- the orchestrator publishes a bounded **research lock** for the design agents.

The research lock should say what is known, what remains a hypothesis, which patterns are banned,
and exactly which questions image generation must explore. Research does not authorize app edits.

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

For refinement work, each direction also needs an **interaction-state matrix**. Audit every
important element at rest, while scrubbing, at early/middle/late transition, after interruption,
at the destination, during reverse, on compact height, at increased font scale, and under reduced
motion. Record visibility, overlap, clip ownership, contrast, semantic state, and input ownership.
Human-feeling craft is often the quality of these in-between states rather than the endpoint image.

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

### A3. The independent SOL-high agent judges creatively

Create a fresh GPT-5.6 SOL high agent for the judge role. It must not be the orchestrator agent and
must remain read-only. Give it exact artifact paths and enough conversation context to understand
the user story, while preserving its independence from the orchestrator's preferences.

Suggested agent task name: `sol_high_creative_judge`.

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

Save the judge's complete response in `docs/`; do not rely on transient agent messages.

### A4. The SOL-xhigh/max orchestrator meta-judges the reviewer

The orchestrator now evaluates the quality of the independent review rather than blindly following
it. The orchestrator and judge must remain separate agent identities throughout the loop.

#### SOL meta-judge command

```text
Act as the accountable design orchestrator and meta-judge. Read the original user story, all
concept evidence, and the SOL-high agent's independent judgment. Rate the review out of 40 for evidence
use, user-intent fidelity, creative value, technical feasibility, and prioritization.

For every important judge recommendation, mark ACCEPT, MODIFY, or REJECT and explain why. Resolve
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
- the independent judge has no unresolved P0 issue;
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

It must include an element-by-state contract for any moving or clipped text. A label should have a
defined anchor, safe region, collision response, fade or relocation rule, semantic lifetime, and
compact fallback. No important text may become accidentally half-visible; intentional clipping
must read as a deliberate aperture treatment and never destroy the selected time or action context.

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

Only Terra writes implementation files during this phase. Both SOL agents remain read-only.

### B3. The independent SOL-high agent judges the actual implementation

Reuse the same independent SOL-high judge role, but start a fresh judging turn and point it at the
code, tests, build evidence, and available captures—not only the execution brief. It must not edit
the implementation.

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
diff, test/build evidence, and the SOL-high agent's implementation review. Rate the judgment out of 40 for
evidence, severity calibration, user-story fidelity, creative usefulness, and feasibility.

Mark each P0/P1 recommendation ACCEPT, MODIFY, or REJECT with concrete reasoning. Correct factual
errors by checking the implementation. Issue only bounded executor corrections that protect the
design lock. Optional P2 polish must not restart the loop unless it exposes a core user failure.
Declare IMPLEMENTATION PASS only when no accepted P0/P1 remains and verification is current.
```

Terra performs the bounded corrections, regenerates evidence, and returns the work to the judge. The
D → E → F → D loop repeats until it passes.

### B5. Implementation stopping gate

Pass the implementation when:

- no accepted P0 or P1 issue remains;
- the user story works in both directions;
- state and data survive the transition correctly;
- accessibility semantics, focus, reduced motion, and system Back are coherent;
- compact behavior has credible code/test evidence;
- tests, assembly, lint, and `git diff --check` pass;
- the orchestrator has rated the independent judge's final review and explicitly accepted the result.

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

- Independent design-judge score: use as comparative evidence, not an automatic winner.
- Independent implementation-judge pass: no unresolved P0/P1, normally at least 80/100.
- SOL meta-rating: evaluate the judge out of 40; 30+ indicates a useful, well-calibrated review.

Numbers support decisions; they do not replace explicit reasoning.

## Required artifacts

Keep these artifacts in the repository so the workflow survives a context reset:

```text
docs/
  session-handoff-<feature>.md
  research/<feature>/
    cycle-1-wave-1-<lane>.md
    cycle-1-wave-1-synthesis.md
    cycle-1-wave-2-<lane>.md
    cycle-1-judge.md
    research-lock.md
  <feature>-execution-brief.md
  <feature>-judge-design-round-1.md
  <feature>-judge-design-round-2.md
  <feature>-judge-implementation-round-1.md
  <feature>-judge-implementation-round-2.md

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
final report should give the branch, baseline and feature commit hashes, final judge and orchestrator
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

- independent design review: 83/100;
- independent implementation review: 85/100, no P0/P1;
- SOL final meta-rating: 35/40;
- 13 unit tests, APK assembly, lint, and `git diff --check` passed.

The feature-specific handoff and locked brief remain in:

- `docs/session-handoff-two-view-home.md`
- `docs/two-view-execution-brief.md`
