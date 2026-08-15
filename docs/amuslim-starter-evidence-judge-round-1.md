# aMuslim starter evidence judge — round 1

## Verdict

**82/100 — REVISE**

No P0 defect. The draft was correctly isolated as `IN_REVIEW`, unapproved, and not packaged for
the app.

## P1 findings

1. Sahih Muslim 597a says “after every prayer”; the first draft narrowed this to obligatory prayer
   without separate evidence.
2. Sahih al-Bukhari 1981 explicitly supports Witr before sleep, but the first draft encoded an
   unsupported every-day recurrence.

## P2 findings accepted for correction

- Personal sleep and prayer-completion triggers should use event occurrence rather than schedule
  date ownership.
- Dhuha recurrence should remain unspecified from the selected evidence.
- The bedtime three-surah action should preserve head/face/front order.
- Post-prayer dhikr needs an approved detail text before it is executable.
- The Friday dua end boundary must remain unresolved.
- Sahih al-Bukhari 935 is corroborative, not primary evidence for the after-Asr placement.
- The candidate tier must define how named-grader Hasan reports are handled.
- The Friday-charity exclusion needs an inspectable locator.

## Orchestrator response

All P1 findings were accepted. The listed P2 findings were also accepted and incorporated in round
2. The draft remains unavailable to production regardless of the next judge score.
