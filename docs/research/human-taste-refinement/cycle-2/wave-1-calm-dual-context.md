# Cycle 2, wave 1: calm dual-context presentation

## Local product facts

- Fardh priority is 0 and active list returns it before Sunnah.
- Overlaps: Subuh/Zikir 05:59–06:19; Zohor/Dhuha at 13:21; Asar/Zikir 16:45–17:05;
  Maghrib/Zikir at 19:29; Isyak/Al-Mulk 20:42–21:02.
- Wheel draws active prayer and contextual labels independently; both may be saffron near pointer.
- `MomentSummary`/`CompactContextStrip` use parallel cells. Both can be accented while contextual
  receives greater weight, contradicting model precedence even without collision.
- Ledger follows model priority. Wheel/summary semantics are prayer-first but duplicative and do
  not explicitly distinguish required from recommended.

## Evidence

### One high-emphasis role, subordinate support

[IBM Carbon button usage](https://carbondesignsystem.com/components/button/usage/) permits one
primary action and lowers emphasis for related support. Transfer is about emphasis hierarchy, not
copying enterprise buttons. During overlap, prayer owns the only high-emphasis textual role;
context remains visible but tertiary/supporting. Risk: turning status into loud CTA chrome.

### Compact composition changes before hierarchy flattens

[Adobe Spectrum button groups](https://spectrum.adobe.com/page/button-group/) stack critical and
secondary roles by importance when horizontal space fails. Transfer: parallel columns are not
sacred; compact/high type should stack/disclose secondary rather than shrink both. Risk: copying
button location conventions instead of the adaptive importance principle.

### Use placement/order/grouping, not color alone

[Apple HIG layout](https://developer.apple.com/design/human-interface-guidelines/layout) uses
placement, reading order, alignment, grouping, and progressive disclosure to express importance.
Transfer: one stable pointer-side reading origin; secondary becomes related child, and compact
uses logical single-column order. Removing color should still reveal hierarchy.

### Physical salience competes with behavioral relevance

Luck et al., [Attentional Capture Debate](https://pmc.ncbi.nlm.nih.gov/articles/PMC7872136/),
distinguishes physical salience from behavioral relevance. Equal saffron/bold/pointer adjacency
can compete despite model/doctrinal priority. Secondary onset must not pulse/glow/move more strongly
than prayer. Exact color/size is not prescribed.

### Mobile first-read hierarchy matters

Jiang et al., [UEyes](https://arxiv.org/abs/2402.05202), found mobile viewing concentrated on fewer
elements and had lower revisit ratios in a large static eye-tracking dataset. Transfer risk is high
for interaction/LTR, but it motivates short-exposure first-read tests: prayer should be named
before context without relying on rescanning equal columns.

### Font scaling breaks fixed relationships

[Android scalable content](https://developer.android.com/develop/ui/compose/accessibility/scalable-content)
distinguishes density scaling from nonlinear font scaling. Fixed cells and dp-sized Canvas labels
cannot preserve hierarchy automatically. Primary prayer remains full; secondary reflows/relocates.

### Circular layouts need explicit semantic order

[Compose traversal guidance](https://developer.android.com/develop/ui/compose/accessibility/traversal)
uses a clock face as an example requiring traversal groups/indexes. During overlap, semantics should
say active fardhu once, then additional recommendation; visual relocation cannot duplicate/reorder.

### Hierarchy cannot depend on motion

[WCAG interaction animation](https://www.w3.org/WAI/WCAG22/Understanding/animation-from-interactions.html)
requires suppressible nonessential interaction motion. Stable layouts must express prayer/context
priority; reduced motion uses immediate role update or restrained nonspatial dissolve.

## Cross-source hierarchy contract

1. Active fardhu owns primary status and readable text in glance/detail.
2. Contextual deed remains as secondary recommendation, never equal high-emphasis peer.
3. Only one high-emphasis text role exists per selected moment.
4. Use at least two non-color channels: order/placement plus size, weight, spacing, enclosure, or
   leader treatment.
5. Pointer identifies selected minute, not two equal obligations. Shared attachment forms a parent/
   child group rather than twin leaders.
6. Semantics/status change exactly at boundary; visual hysteresis cannot delay fardhu truth.
7. Spoken phrasing is nonduplicative: selected time → active fardhu → additional practice.
8. Compact/high type protects prayer first through stack/disclosure.
9. Reduced motion keeps same role order without spatial dependence.
10. Outside overlaps, avoid empty permanent primary/secondary dashboard structures.

## State sequence

| State | Required hierarchy |
| --- | --- |
| Before boundary | Context may lead if no active fardhu; next prayer remains recoverable |
| Approaching under touch | Stable origin, no anticipatory flicker |
| First fardhu minute | Exact textual/semantic handoff; contextual reframed |
| Overlap scrub | Fardhu primary every minute; secondary stable/subordinate |
| Settle | Secondary may become clearer without equal weight |
| Expand | One owner each frame; summary receives roles before source relinquishes |
| Detail | Fardhu principal ledger/action; context supporting |
| Collapse/reverse | Same progress gives same ownership; no double primary |
| Last fardhu minute | Prayer remains primary through inclusive boundary |
| First minute after | Exact semantic change; bounded contextual promotion |
| Compact/high type | Ordered stack/disclosure, full prayer first |
| Reduced motion | In-place role change/nonspatial dissolve |

## Reject in image generation

Two equal saffron headlines/pills/columns/leaders; node-only fardhu with readable Sunnah text;
permanent two-card dashboard; enterprise CTA/gamification; color-only hierarchy; entirely hidden
secondary; both contexts shrunk in compact; delayed fardhu handoff; more energetic recommendation;
duplicated/secondary-first speech; reduced-motion double-primary flash.

## Controlled hypotheses

### A: pointer-side hierarchical stack

One group: larger active fardhu first line; smaller/quiet “amalan tambahan” second line. Outside
overlap, collapses to one line. Compare association, dashboard feel, vertical fit, boundary swap,
and high-type competition.

### B: pointer-primary plus stable secondary rail

Fardhu alone at pointer; contextual deed on upright external rail/strip with restrained relation and
no saffron headline. Compare immediacy, discoverability, gaze travel, clipping, reversal.

### C: fardhu at pointer, secondary disclosed by interaction state

During touch, full fardhu plus compact secondary cue; on settle/detail, cue expands into complete
subordinate recommendation. Compare calmness, retention, boundary stability, reduced motion, and
risk of over-hiding.

Use identical states at 05:58/05:59/06:19/06:20 and 16:44/16:45/17:05/17:06, glance/detail,
360/390dp, increased type, standard/reduced motion, and reverse.
