# Wave 1: core value and non-visual UX

## Verdict

The highest-impact improvement before visual polish is a **truthful, actionable “right now”
vertical slice**. The minimum useful journey is:

> confirm zone → resolve the real current moment → show one clearly justified current/next deed →
> explain how to do it → verify its source → mark it done → restore that state after restart.

The wheel can remain, but should be an exploration instrument around this journey rather than the
only way to discover or act.

Firecrawl search returned HTTP 402, so the scout used live primary-source retrieval through the
workflow's documented fallback. No project files were edited by the scout.

## Current-prototype audit

Strong foundations include deterministic deed ordering, separate availability windows and act
duration, an adjustable accessibility range for the wheel, semantic handoff, large-font geometry
tests, a direct return-to-now action, and a detail treatment containing basic guidance and citation
labels.

Journey-breaking gaps:

1. `NowMinute` is fixed at 15:00 and “return to now” returns to that constant.
2. The app uses `sampleDeeds()` and `sampleDayStates()`; Thursday fasting, WLY01, and all times are
   hard-coded.
3. The displayed Gregorian and Hijri dates are fixed.
4. No zone, level, language, wake/sleep, or settings flow exists.
5. No ViewModel, durable data source, repository, network, or background-work layer exists.
6. Completion is transient `rememberSaveable` state rather than date-scoped product data.
7. Actual current time and scrubbed selected time are conflated.
8. Only the first active/next deed is fully actionable when valid deeds overlap.
9. Some guidance is not sufficient to perform the named practice.
10. Authenticity is asserted with strings rather than verifiable source links and review metadata.

## Evidence and heuristics

### 1. Offline core truth needs a durable local source of truth

- Source: Android Developers, “Build an offline-first app,” updated 2026.
- URL: https://developer.android.com/topic/architecture/data-layer/offline-first
- Transfer: prayer times, reviewed deed content, zone, and completion should be read locally;
  network refresh updates local data instead of blocking Home.
- Test: airplane-mode cold start still yields a valid current/next deed, zone, data date, and
  explicit freshness state.
- Risk: never present expired prayer times as confidently current; expose a bounded stale state.

### 2. `rememberSaveable` is not product persistence

- Source: Android Developers, “Save UI states,” updated 2026.
- URL: https://developer.android.com/topic/libraries/architecture/saving-states
- Transfer: store completion as at least `{localDate, zone, deedId, completedAt}` and settings in
  durable storage.
- Test: completion survives force-stop/relaunch and resets correctly when the resolved local day
  changes.
- Risk: a bare deed ID produces wrong-day completion around midnight and zone changes.

### 3. Custom gestures need an equivalent direct path

- Source: Android Developers, “Principles for improving app accessibility,” updated 2026-06-16.
- URL: https://developer.android.com/guide/topics/ui/accessibility/principles
- Transfer: keep the adjustable wheel, but expose a linear, focusable current/next action and
  alternatives for overlapping active deeds.
- Test: TalkBack and Switch Access users can learn what is best now, open instructions, mark done,
  and return without horizontal dragging.
- Risk: duplicated wheel and detail semantics can create repeated announcements; preserve one
  semantic owner.

### 4. Controls require at least a 48×48dp focusable/touch area

- Source: Android Developers, “Make apps more accessible,” updated 2026-04-21.
- URL: https://developer.android.com/guide/topics/ui/accessibility/views/apps-views
- Transfer: apply the existing completion target standard to source links, overlap choices, zone,
  retry, freshness, and other compact actions.
- Test: accessibility scanning reports no undersized target in the complete current-deed journey.
- Risk: overlapping enlarged hit regions create ambiguity.

### 5. “When, where, and how” turns intention into action

- Source: Gollwitzer & Sheeran, “Implementation Intentions and Goal Achievement: A Meta-Analysis
  of Effects and Processes,” *Advances in Experimental Social Psychology* 38, 2006.
- URL: https://www.researchgate.net/publication/37367696_Implementation_Intentions_and_Goal_Achievement_A_Meta-Analysis_of_Effects_and_Processes
- Transfer: each primary recommendation needs a reviewer-approved cue/window, action, amount, and
  duration.
- Test: after five seconds, a participant can answer what to do, how much, and by when.
- Risk: do not invent counts, sequences, or “best” windows merely to make copy actionable.

### 6. “Just in time” means useful and receptive, not merely clock-matched

- Source: Hardeman et al., “Just-in-Time Feedback in Diet and Physical Activity Interventions,”
  *JMIR*, 2018.
- URL: https://www.jmir.org/2018/3/e106/
- Transfer: distinguish active, next, done, ended, not-configured, stale, and unavailable states;
  suppress repeated pressure after completion.
- Test: wording and primary action change correctly at each time boundary and after completion.
- Risk: do not imply inferred spiritual receptivity; v1 should use explicit settings and
  observable date/time state only.

### 7. Choice-overload evidence does not support hiding all alternatives

- Source: Scheibehenne, Greifeneder & Todd, “Can There Ever Be Too Many Options? A Meta-Analytic
  Review of Choice Overload,” *Journal of Consumer Research* 37(3), 2010.
- URL: https://academic.oup.com/jcr/article-abstract/37/3/409/1827647
- Transfer: provide one primary recommendation while disclosing other simultaneously valid deeds.
- Test: if two deeds are valid, one is primary and the others remain plainly reachable.
- Risk: “fewer is always better” can erase important context or imply unique authority.

### 8. Plain language improves comprehension

- Source: Magwood et al., randomized plain-language recommendations trial, 2023.
- URL: https://pubmed.ncbi.nlm.nih.gov/38008266/
- Transfer: use common BM/English verbs, one instruction per sentence, explain fiqh labels, and
  separate “what to do” from “why/source.”
- Test: at least four of five target participants can paraphrase action, timing, and completion
  condition without opening the citation.
- Risk: comprehension evidence transfers; religious authority does not. Simplification must not
  alter a ruling or devotional meaning.

### 9. Permission requests belong in context and must degrade gracefully

- Sources: Android Developers, “Notification runtime permission” and “Schedule exact alarms.”
- URLs:
  - https://developer.android.com/develop/ui/compose/notifications/notification-permission
  - https://developer.android.com/develop/background-work/services/alarms
- Transfer: do not request notification or exact-alarm permission during minimum setup. Ask only
  when a user enables a specific reminder.
- Test: the whole core journey works after both permissions are denied.
- Risk: not every window-based deed needs an exact alarm.

## Ranked slices

### 1. First: truthful act-now loop

1. Ask only for JAKIM zone and level on first run; avoid permission dialogs.
2. Inject a real clock, local date, and zone-aware day boundary.
3. Read prayer times from a local cache, refresh from a provider, and expose fresh/stale/unavailable.
4. Resolve a small reviewer-approved catalogue.
5. State actual time, active/next status, reason, window, act duration, one primary action, and
   reachable valid alternatives.
6. Provide complete instructions, translation where relevant, grade, source link, and review
   metadata.
7. Persist completion on disk scoped to resolved local date.
8. Handle loading, empty, stale, offline, zone-change, and rollover states.
9. Keep the wheel as optional exploration; reopening Home returns to actual now.

Acceptance test: a Malaysian user can install with seeded content, choose a zone, discover a
truthful current/next deed, understand it, verify its source, mark it done, force-stop, reopen, and
see correct state using touch or TalkBack.

### 2. Next: overlap and day-rule integrity

- Multiple simultaneously valid deeds and reviewer-approved hierarchy.
- Monday/Thursday and Hijri 13–15 rules.
- Personal wake/sleep anchors and rollover.
- Explicit not-today, ended, and changed-date handling.

### 3. Later: re-engagement

- Contextual opt-in notifications and inexact fallback.
- Reminder deduplication after completion.
- Richer settings, planner, and progress.
- Visual refinement after usability and accessibility checks.

## Contradictions and open questions

- The PRD's original vertical Gantt conflicts with the accepted now-focused instrument; the newer
  structural decision should win.
- “Best deed” is currently only `ruling.priority → start → id`, not a reviewed definition.
- “Now” means both actual time and selected scrubbed time.
- The domain says every active block drives detail, but only the first is actionable.
- `TimelineDeed` lacks the PRD's level field.
- “Everything works offline” must exclude opening external source pages; bundled source metadata
  can remain offline.
- The atomic completion unit for a collection such as morning adhkar is unresolved.
- Authority for ranking obligation, primary sunnah, other valid deeds, and next deed is unresolved.
- The acceptable official prayer/Hijri source and safe staleness window remain unresolved.

## Next-wave questions

1. With a qualified Malaysian reviewer, what ranking and wording contract determines active
   obligation, primary sunnah now, other valid now, and next?
2. Against the chosen prayer/Hijri source, which freshness, stale fallback, zone-change, midnight,
   and offline rules prevent confidently wrong recommendations?
3. In BM-first usability sessions including a TalkBack user, can people identify, explain, start,
   verify, and complete a recommendation without being taught the wheel?
