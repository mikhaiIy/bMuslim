# Agent-led frontend design research

## What practitioners are doing

1. Give the agent semantic design intent, not only screenshots or color values. Google's DESIGN.md work describes tokens by purpose so an agent knows what a color is for and can validate choices against accessibility rules. Source: https://blog.google/innovation-and-ai/models-and-research/google-labs/stitch-design-md/
2. Go wide before going deep. Figma recommends generating distinct stylistic and structural approaches, comparing them, choosing one, and then iterating deeply. It also notes that direct hands-on editing is often faster and more precise than prompting all the way to a final result. Source: https://www.figma.com/blog/the-figma-agent-is-here/
3. Supply real context and reusable tools so output resembles the product team rather than the model's defaults. Figma frames components, tokens, standards, examples, custom tools, and skills as the difference between generic production and genuine collaboration. Source: https://www.figma.com/blog/agent-custom-tools-context-skills/
4. Keep curated examples close to the agent. Vercel describes injecting current, task-specific guidance and maintaining hand-curated example directories rather than trusting a chain of search summaries. Deterministic checks and post-generation fixers then handle repeatable failures. Source: https://vercel.com/blog/how-we-made-v0-an-effective-coding-agent
5. Judge the rendered interface across states. Chromatic's agent workflow treats visual, interaction, and accessibility results as separate review surfaces with approved baselines. Source: https://www.chromatic.com/frontend-workflow-for-ai

## Working method for bMuslim

- Preserve the product's signature mechanic and religious-content contract.
- Explore directions that differ in composition and material, not only palette.
- Select one narrative concept that can explain every visual choice.
- Render at real phone sizes and inspect important time states.
- Use an independent model as a critic with a fixed rubric.
- Convert criticism into specific edits, then render and judge again.
- Keep deterministic checks for copy, accessibility hooks, reduced motion, and timeline accuracy.

## Selected direction: Prayer Almanac

A contemporary astronomical field instrument influenced by printed almanacs, calibrated clocks, and Malaysian modernist editorial design. The interface should feel measured, quiet, and culturally grounded without mosque silhouettes, ornamental crescents, faux calligraphy, gold luxury styling, or wellness-app cards.

### Preserve

- The rotatable half-crescent day wheel and fixed pointer.
- Exact prayer anchors and deed windows.
- Time-driven atmosphere and semantic sun/moon state.
- Horizontal drag, wheel, keyboard, back-to-now, and completion persistence.
- Bahasa Melayu UI and verified sacred text from the existing project.

### Transform

- Replace the thick translucent lifestyle ring with a calibrated, layered instrument face.
- Replace floating pills and nested cards with ruled bands, open whitespace, and one solid control shape.
- Make the selected time and next prayer a single editorial composition.
- Use a restrained mono register for measurements and Plus Jakarta Sans for actions and reading.
- Let the time palette affect paper, ink, and celestial signal without changing the stable action green.
- Make the bottom navigation a quiet edge rail rather than a floating white capsule.

### Reject

- Generic cream-and-green wellness styling.
- Glassmorphism, neon, purple, glow-heavy atmosphere, sparkles, and decorative stars.
- Equal cards, pill overload, fake dashboard chrome, or icons used as filler.
- Curved labels that become unreadable at phone size.
- Motion without feedback, hierarchy, or state meaning.

## Judge rubric

1. The crescent reads as a usable instrument within one second.
2. The screen has a recognizable design idea beyond colors and rounded corners.
3. Selected time, current prayer state, and next action are legible without interpreting the wheel.
4. Visual hierarchy remains strong at 360x800 and 390x844.
5. Prayer anchors and deed windows remain accurate.
6. Every animation communicates scrubbing, selection, completion, or time transition.
7. No generic agent-design tells remain.
8. Touch, keyboard, reduced-motion, contrast, and copy checks pass.
