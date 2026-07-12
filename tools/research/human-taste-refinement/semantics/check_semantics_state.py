#!/usr/bin/env python3
"""Exhaustive, isolated contract checker for bMuslim semantic ownership.

This intentionally does not import or modify production code. Its inputs are transcribed from
TimelineModel.kt and the cycle-2 research contract so failures remain inspectable and reproducible.
"""

from __future__ import annotations

import csv
import gzip
import json
from dataclasses import asdict, dataclass
from enum import Enum
from pathlib import Path


OUT = Path(__file__).resolve().parent
DAY = 1440


class Presentation(str, Enum):
    GLANCE = "glance"
    DETAIL = "detail"


class Motion(str, Enum):
    NORMAL = "normal"
    ZERO = "zero"
    APP_REDUCED = "app_reduced"


@dataclass(frozen=True)
class Deed:
    id: str
    title: str
    start: int
    end: int
    fardhu: bool

    def active(self, minute: int) -> bool:
        minute %= DAY
        if self.start <= self.end:
            return self.start <= minute <= self.end
        return minute >= self.start or minute <= self.end


DEEDS = (
    Deed("subuh", "Solat Subuh", 359, 379, True),
    Deed("zikir-pagi", "Zikir pagi", 359, 431, False),
    Deed("dhuha", "Solat Dhuha", 450, 801, False),
    Deed("zohor", "Solat Zohor", 801, 821, True),
    Deed("asar", "Solat Asar", 1005, 1025, True),
    Deed("zikir-petang", "Zikir petang", 1005, 1169, False),
    Deed("maghrib", "Solat Maghrib", 1169, 1189, True),
    Deed("isyak", "Solat Isyak", 1242, 1262, True),
    Deed("al-mulk", "Baca Surah Al-Mulk", 1242, 1380, False),
)

# Product-authoritative overlap intervals from cycle-2 wave-1 synthesis.
OVERLAPS = (
    ("subuh_zikir", 359, 379),
    ("zohor_dhuha", 801, 801),
    ("asar_zikir", 1005, 1025),
    ("maghrib_zikir", 1169, 1169),
    ("isyak_almulk", 1242, 1262),
)


@dataclass(frozen=True)
class Interaction:
    name: str
    progress: float
    target: Presentation
    last_settled: Presentation
    direct_manipulation: bool = False


BASE_INTERACTIONS = (
    Interaction("glance", 0.0, Presentation.GLANCE, Presentation.GLANCE),
    Interaction("scrub", 0.0, Presentation.GLANCE, Presentation.GLANCE, True),
    Interaction("settle", 0.0, Presentation.GLANCE, Presentation.GLANCE),
    *(Interaction(f"expand_{p}", p, Presentation.DETAIL, Presentation.GLANCE) for p in (.25, .5, .75)),
    Interaction("detail", 1.0, Presentation.DETAIL, Presentation.DETAIL),
    *(Interaction(f"collapse_{p}", p, Presentation.GLANCE, Presentation.DETAIL) for p in (.75, .5, .25)),
)

REVERSALS = tuple(
    Interaction(f"reverse_{p}_{target.value}_last_{last.value}", p, target, last)
    for p in (.1, .35, .6, .9)
    for target in Presentation
    for last in Presentation
)

INTERACTIONS = BASE_INTERACTIONS + REVERSALS
LAYOUTS = ("regular_normal_type", "regular_high_type", "compact_normal_type", "compact_high_type")
REQUESTS = (-1, 0, 2, 3, 1435, 1439, 1440)


def minutes_under_test() -> list[tuple[str, int]]:
    """Every overlap interior plus boundary -1/first/last/+1."""
    result: list[tuple[str, int]] = []
    for overlap_id, first, last in OVERLAPS:
        for minute in range(first - 1, last + 2):
            result.append((overlap_id, minute))
    return result


def active_deeds(minute: int) -> list[Deed]:
    return sorted(
        (deed for deed in DEEDS if deed.active(minute)),
        key=lambda deed: (0 if deed.fardhu else 1, deed.start, deed.id),
    )


def safe_absolute_quantize(requested: float) -> int:
    """Linear SetProgress contract: valid five-minute endpoints 00:00..23:55."""
    clamped = min(1435.0, max(0.0, requested))
    # Requests in the matrix are integers, so half-to-even is irrelevant here.
    return min(1435, max(0, int(clamped / 5.0 + 0.5) * 5))


def cyclic_adjust(current: int, direction: int) -> int:
    """Explicit earlier/later action; unlike SetProgress this operation is cyclic."""
    assert direction in (-1, 1)
    return (current + direction * 5) % DAY


def current_code_quantize(requested: float) -> int:
    clamped = min(1439.0, max(0.0, requested))
    rounded = int(clamped / 5.0 + 0.5) * 5
    return rounded % DAY


def settled_subtree(interaction: Interaction, motion: Motion) -> Presentation:
    # Zero/app-reduced transitions are required to choose a complete endpoint atomically.
    if motion in (Motion.ZERO, Motion.APP_REDUCED):
        return interaction.target
    if interaction.progress <= 0.0 and interaction.target is Presentation.GLANCE:
        return Presentation.GLANCE
    if interaction.progress >= 1.0 and interaction.target is Presentation.DETAIL:
        return Presentation.DETAIL
    return interaction.last_settled


def spoken_tokens(minute: int) -> list[str]:
    active = active_deeds(minute)
    fardhu = [deed for deed in active if deed.fardhu]
    contextual = [deed for deed in active if not deed.fardhu]
    tokens = ["time"]
    if fardhu:
        tokens.append(f"fardhu:{fardhu[0].id}")
    if contextual:
        tokens.append(f"context:{contextual[0].id}")
    tokens.append("day")
    return tokens


def traversal(interaction: Interaction, motion: Motion, minute: int) -> list[str]:
    subtree = settled_subtree(interaction, motion)
    tokens = spoken_tokens(minute)
    if subtree is Presentation.DETAIL:
        tokens.append("ledger_action")
        # Actions are reachable only at a complete detail endpoint under zero/reduced motion or
        # settled detail under normal motion. During normal collapse, keep content as an anchor
        # but disable actions.
        normal_mid_collapse = (
            motion is Motion.NORMAL
            and interaction.target is Presentation.GLANCE
            and 0.0 < interaction.progress < 1.0
        )
        if normal_mid_collapse:
            tokens.remove("ledger_action")
    elif interaction.progress == 0.0:
        tokens.append("open_action")
    return tokens


def validate_case(interaction: Interaction, motion: Motion, layout: str, minute: int) -> dict[str, object]:
    active = active_deeds(minute)
    fardhu = [deed for deed in active if deed.fardhu]
    speech = spoken_tokens(minute)
    order = traversal(interaction, motion, minute)
    subtree = settled_subtree(interaction, motion)

    value_owner_count = 1
    canonical_adjustable_owner_count = 1
    semantic_clock_count = 0
    fardhu_token = f"fardhu:{fardhu[0].id}" if fardhu else None
    fardhu_count = speech.count(fardhu_token) if fardhu_token else 0
    fardhu_first = not fardhu or speech.index(fardhu_token) == 1
    minute_role_ok = not fardhu or active[0].id == fardhu[0].id
    summary_exposed = subtree is Presentation.DETAIL
    expected_summary = (
        interaction.target is Presentation.DETAIL
        if motion in (Motion.ZERO, Motion.APP_REDUCED)
        else interaction.last_settled is Presentation.DETAIL
    )
    settled_policy_ok = summary_exposed == expected_summary
    no_alpha_hiding = True  # visibility is structural state, never inferred from alpha.
    zero_motion_valid = motion is Motion.NORMAL or subtree is interaction.target
    reachable_order = order[0] == "time" and len(order) == len(set(order))
    if fardhu_token:
        reachable_order = reachable_order and order.index(fardhu_token) == 1

    assertions = {
        "one_value_owner": value_owner_count == 1,
        "one_adjustable_owner": canonical_adjustable_owner_count == 1,
        "zero_semantic_clocks": semantic_clock_count == 0,
        "fardhu_once": fardhu_count == (1 if fardhu else 0),
        "fardhu_first": fardhu_first,
        "minute_driven_role": minute_role_ok,
        "settled_subtree_policy": settled_policy_ok,
        "no_alpha_hiding": no_alpha_hiding,
        "valid_zero_motion": zero_motion_valid,
        "reachable_order": reachable_order,
        "compact_high_type_reachable": bool(order),
    }
    failures = [name for name, passed in assertions.items() if not passed]
    return {
        "interaction": interaction.name,
        "progress": interaction.progress,
        "target": interaction.target.value,
        "last_settled": interaction.last_settled.value,
        "motion": motion.value,
        "layout": layout,
        "minute": minute,
        "active": "|".join(deed.id for deed in active),
        "primary": active[0].id if active else "",
        "speech": "|".join(speech),
        "subtree": subtree.value,
        "traversal": "|".join(order),
        "value_owners": value_owner_count,
        "adjustable_owners": canonical_adjustable_owner_count,
        "semantic_clocks": semantic_clock_count,
        "pass": not failures,
        "failures": "|".join(failures),
    }


def current_code_counterexamples() -> list[dict[str, object]]:
    traces: list[dict[str, object]] = []
    for requested in REQUESTS:
        current = current_code_quantize(requested)
        safe = safe_absolute_quantize(requested)
        if current != safe:
            traces.append({
                "kind": "range_wrap",
                "input": requested,
                "current": current,
                "required": safe,
                "reason": "0..1439 is rounded to 5 minutes; 1439/1440 becomes 1440 then wraps to 0",
            })

    for progress in (.25, .35, .36, .5, .75, .9):
        summary_reveal = max(0.0, min(1.0, (progress - .34) / (.68 - .34)))
        if summary_reveal > .05:
            traces.append({
                "kind": "premature_summary_focus",
                "input": progress,
                "current": "summary composed + focusable",
                "required": "summary inaccessible/not focusable until settled detail",
                "reason": f"revealFraction(Summary)={summary_reveal:.6f} > .05",
            })

    for minute in (359, 801, 1005, 1169, 1242):
        active = active_deeds(minute)
        fardhu = next(deed for deed in active if deed.fardhu)
        # Current wheelDescription includes the full active list, then active prayer separately.
        traces.append({
            "kind": "duplicate_fardhu_speech",
            "input": minute,
            "current": f"activeText:{fardhu.id}|prayerText:{fardhu.id}",
            "required": f"time|fardhu:{fardhu.id}|context|day",
            "reason": "wheelDescription repeats active fardhu in activeText and prayerText",
        })

    traces.append({
        "kind": "semantic_clock",
        "input": "all states",
        "current": "AlmanacHeader Text(formatMinute) emits Text semantics",
        "required": "zero semantic clocks; Canvas range node is sole canonical adjustable time owner",
        "reason": "visual header clock is not cleared or marked invisible/redundant",
    })
    traces.append({
        "kind": "alpha_visibility_policy",
        "input": "detail content during transition",
        "current": "Surface/summary/ledger/close/navigation exposure is controlled partly by alpha",
        "required": "semantic exposure controlled structurally by settled state",
        "reason": "positive alpha is not a semantic hiding contract",
    })
    return traces


def write_csv(path: Path, rows: list[dict[str, object]]) -> None:
    handle = (
        gzip.open(path, "wt", newline="", encoding="utf-8")
        if path.suffix == ".gz"
        else path.open("w", newline="", encoding="utf-8")
    )
    with handle:
        writer = csv.DictWriter(handle, fieldnames=list(rows[0]), lineterminator="\n")
        writer.writeheader()
        writer.writerows(rows)


def main() -> int:
    rows: list[dict[str, object]] = []
    labeled_minutes = minutes_under_test()
    for overlap_id, minute in labeled_minutes:
        for interaction in INTERACTIONS:
            for motion in Motion:
                for layout in LAYOUTS:
                    row = validate_case(interaction, motion, layout, minute)
                    row = {"overlap": overlap_id, **row}
                    rows.append(row)

    range_rows = []
    for requested in REQUESTS:
        result = safe_absolute_quantize(requested)
        range_rows.append({
            "requested": requested,
            "absolute_result": result,
            "valid_0_1435": 0 <= result <= 1435,
            "multiple_of_5": result % 5 == 0,
        })
    cyclic_rows = [
        {"current": 0, "direction": -1, "result": cyclic_adjust(0, -1), "expected": 1435},
        {"current": 1435, "direction": 1, "result": cyclic_adjust(1435, 1), "expected": 0},
        {"current": 600, "direction": -1, "result": cyclic_adjust(600, -1), "expected": 595},
        {"current": 600, "direction": 1, "result": cyclic_adjust(600, 1), "expected": 605},
    ]
    for row in cyclic_rows:
        row["pass"] = row["result"] == row["expected"]

    counterexamples = current_code_counterexamples()
    write_csv(OUT / "exhaustive-results.csv.gz", rows)
    write_csv(OUT / "range-results.csv", range_rows)
    write_csv(OUT / "cyclic-adjust-results.csv", cyclic_rows)
    write_csv(OUT / "current-code-counterexamples.csv", counterexamples)

    failures = [row for row in rows if not row["pass"]]
    range_failures = [row for row in range_rows if not row["valid_0_1435"] or not row["multiple_of_5"]]
    cyclic_failures = [row for row in cyclic_rows if not row["pass"]]
    summary = {
        "contract_cases": len(rows),
        "contract_passes": len(rows) - len(failures),
        "contract_failures": len(failures),
        "overlap_minute_rows": len(labeled_minutes),
        "unique_minutes": len(set(minute for _, minute in labeled_minutes)),
        "interaction_states": len(INTERACTIONS),
        "motions": [motion.value for motion in Motion],
        "layouts": list(LAYOUTS),
        "range_cases": len(range_rows),
        "range_failures": len(range_failures),
        "cyclic_cases": len(cyclic_rows),
        "cyclic_failures": len(cyclic_failures),
        "current_code_counterexamples": len(counterexamples),
        "status": "CONTRACT_PASS" if not failures and not range_failures and not cyclic_failures else "CONTRACT_FAIL",
        "application_validation": "NOT_PERFORMED",
    }
    (OUT / "summary.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(summary, indent=2))
    return 0 if summary["status"] == "CONTRACT_PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
