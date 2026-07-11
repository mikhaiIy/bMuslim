# Session handoff: human-taste refinement

## Goal

Research, design, implement, and judge a fine-detail refinement of the bMuslim native Android
two-view home. The outcome should feel calm, deliberate, and unusually well considered throughout
interaction—not only in static screenshots.

The work has three separate gated phases:

1. two-wave web research cycles about human design taste and interaction craft;
2. an image-led design loop using the resulting research lock;
3. a native Compose execution loop with tests and independent review.

Do not use Fable in this phase.

## Git state at handoff

- `master` has been fast-forwarded locally to the complete `v1` commit, `1d4cb5d`.
- Annotated tag `v1` still points to `1d4cb5d`.
- Active branch: `feature/human-taste-refinement`, created from that exact commit.
- The SOL-high judge conversion and new research workflow belong to this branch, not the frozen
  `v1` tag.
- Do not stage or remove unrelated existing `.gitignore`, `skills-lock.json`, `.agents/`, IDE,
  Gradle cache, old HTML review, learning, or local configuration changes.
- Remote `origin/master` has not been pushed; it may still point to `14aae9c`.

## Required agent topology

| Work | Agent/model | Identity constraint |
| --- | --- | --- |
| Overall orchestration and meta-judgment | GPT-5.6 SOL xhigh/max | One persistent accountable orchestrator |
| Web research scouts | GPT-5.6 Terra medium | Fresh clean-context agents for each wave |
| Research synthesis | GPT-5.6 SOL high | Separate from scouts and final judge |
| Research judgment | GPT-5.6 SOL high | Fresh independent agent after every two waves |
| Image-led creative judgment | GPT-5.6 SOL high | Separate from orchestrator |
| Native implementation | GPT-5.6 Terra medium | Only agent allowed to edit app implementation |
| Implementation judgment | GPT-5.6 SOL high | Independent, read-only |
| Visual references | Built-in image-generation model | Images only; never runtime devotional data |

There are four concurrency slots including the root agent. Run at most three scouts in parallel,
then end that wave before starting synthesis or judgment. The root is the SOL-xhigh/max-equivalent
orchestrator. Exact internal model routing may not be controllable; preserve the named cognitive
roles and independence even when the runtime chooses the concrete model.

## Product identity to protect

- Contemporary Malaysian prayer almanac: pine, mineral atmosphere, paper, and restrained saffron.
- Devotional, quiet, precise, and warm—not gamified, ornamental, or generic wellness UI.
- One continuous prayer-time instrument with time-proportional geometry.
- Fixed saffron pointer, prayer nodes, deed-window arcs, act beads, actual-now mark, and celestial
  rail remain meaningful.
- Sacred Arabic → translation → source hierarchy remains respectful and readable.
- Avoid decorative mosque/crescent wallpaper, glassmorphism, stacked generic cards, gratuitous
  pills, springy spectacle, excessive ambient motion, or novelty that competes with worship.

“Zen” means clear hierarchy, breathing room, unhurried but responsive motion, stable focus,
legible transitions, and restraint. It does not mean removing useful information or flattening all
contrast.

## Primary observed defects

### 1. Active-deed saffron text can become occluded

The yellow/saffron text associated with the selected deed bar can be blocked, clipped, or leave a
poor reading position while the user scrubs the wheel. Audit it at every wheel angle, not only at
the preview minute.

Questions to resolve through research and design:

- Should the label remain radial, move to a stable tangent/safe rail, or hand off to a fixed
  selected-context region?
- When does it flip alignment, shorten, fade, relocate, or gain a quiet backing surface?
- How does it avoid prayer labels, the pointer, orbit bodies, bezel, and viewport edges?
- What remains visible during active drag, settle, expand, collapse, and interrupted motion?
- What is announced to accessibility while the visual label relocates?

### 2. Top clock is cut in half in detail mode/transition

When the wheel moves upward into the detailed UI, the clock/time treatment at the top can appear
accidentally bisected. Determine whether it belongs inside the aperture, outside it, or should
transform into another stable header role. An accidental half-glyph is not acceptable.

Audit early, middle, and late expansion; destination; reverse; short height; 1.3× font scale;
reduced motion; animation interruption; and system inset behavior.

## Broader micro-detail audit

Do not stop after fixing the two reported defects. For every visible element, ask:

- What happens while the user touches, drags, releases, interrupts, reverses, or presses Back?
- Can text overlap, clip, jitter, cross an edge, or temporarily lose contrast?
- Does shape interpolation look intentional at every frame?
- Does input ownership remain obvious when visual elements cross regions?
- Does the layout still breathe at compact height and increased font scale?
- Are hidden elements also absent semantically, and does focus land somewhere meaningful?
- Does reduced motion preserve comprehension rather than merely remove duration?
- Is the detail delightful because it clarifies meaning, or only because it moves?

Create an element-by-state matrix covering rest, scrub, settle, expand at roughly 25/50/75%,
detail, collapse, interrupted/reversed transition, compact height, font scale, and reduced motion.

## Phase R: research plan

The canonical commands and gates are in:

- `docs/orchestrator-design-execution-workflow.md`

On the first clean-context turn:

1. Read the entire workflow document and this handoff.
2. Read the applicable `firecrawl-search` skill before any web research; the user explicitly
   requested live online research.
3. Inspect `TimelineScreen.kt`, `HomeScene.kt`, tests, and existing two-view images.
4. If possible, render or capture the current states to ground research in evidence. If no device
   exists, inspect Compose geometry and state transitions and record the capture limitation.
5. Spawn three fresh Terra-medium scouts in parallel:
   - mobile motion, spatial continuity, and interruption;
   - radial UI label collision, clocks/dials, and clipping-safe annotations;
   - human design taste, calm interface craft, and proven AI-agent design workflows.
6. Save cited reports under `docs/research/human-taste-refinement/cycle-1/`.
7. Start a separate SOL-high synthesis turn after all scouts finish.
8. Use the synthesis to launch a second wave of fresh scouts on narrower gaps.
9. After wave two, start a fresh independent SOL-high research judge.
10. Have the root/SOL-xhigh orchestrator meta-rate the judgment and either lock research or begin
    another complete two-wave cycle.

Source results may include videos, tutorials, practitioner posts, blogs, research, official design
guidance, GitHub repositories, and agent skills. Preserve direct URLs, authorship, dates, video
timestamps where possible, exact relevant ideas, transfer risks, and testable heuristics. Avoid
SEO inspiration lists and uncited aesthetic opinions.

## Phase A: image-led design plan

Begin only after a research lock passes. Read the complete `imagegen-frontend-mobile` and base
`imagegen` skill instructions before generating.

Use the research heuristics to generate separate, large mobile images for:

- problematic scrub angles for the selected-deed label;
- early/middle/late expansion showing the clock and aperture relationship;
- settled glance and detail endpoints;
- reverse/interrupted states where a still image can communicate them;
- 360×640 compact and increased-font-scale states.

Do not use a single compressed board as evidence. Keep content/time/geometry controlled between
sequential frames. Ask an independent SOL-high creative judge to score the designs and give
constructive direction; the root orchestrator meta-rates it. Repeat until the interaction-state
matrix and design lock have no accepted P0/P1 issue.

## Phase B: native execution plan

SOL writes a bounded execution brief. Terra medium implements. The same conceptual instrument and
selected minute must survive every state; avoid duplicate screens and cosmetic crossfades.

Likely implementation paths:

- `app/src/main/java/com/bmuslim/app/ui/timeline/TimelineScreen.kt`
- `app/src/main/java/com/bmuslim/app/ui/timeline/HomeScene.kt`
- `app/src/test/java/com/bmuslim/app/ui/timeline/HomeSceneTest.kt`
- `app/src/main/java/com/bmuslim/app/ui/timeline/TimelineModel.kt`
- `app/src/test/java/com/bmuslim/app/ui/timeline/TimelineModelTest.kt`

Prefer deterministic, testable placement functions for labels, clips, centers, radii, reveal
progress, and compact budgets. Add tests for multiple selected minutes/angles and sampled
transition progress, not only two endpoint screenshots.

An independent SOL-high agent judges actual code, tests, diffs, and captures. The root orchestrator
rates the judgment and sends only bounded corrections back to Terra. No Fable.

## Verification baseline

```bash
JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:testDebugUnitTest :app:assembleDebug --no-parallel

JAVA_HOME=/opt/android-studio/jbr PATH=/opt/android-studio/jbr/bin:$PATH \
  ./gradlew :app:lintDebug --no-parallel

git diff --check
```

The v1 baseline had 13 passing unit tests, successful APK assembly and lint, and no known build
failure. A real device/emulator capture was unavailable in the previous phase, so motion and
360×640 at 1.3× font scale remain valuable evidence rather than assumed passes.

## Immediate next action after context reset

Do not redesign or edit Compose immediately. Start Phase R, wave one: inspect current native
evidence, read the web-search skill, launch the three independent research lanes, save their cited
reports, and synthesize only after all three return.
