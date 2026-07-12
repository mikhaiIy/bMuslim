#!/usr/bin/env node

/**
 * Cycle-2 geometry feasibility harness.
 *
 * IMPORTANT: Text metrics are a host ImageMagick/fontconfig proxy. They are not
 * production Android TextMeasurer proof. The geometry and ownership rules are
 * deterministic and intentionally isolated from application production code.
 */

import { execFileSync } from "node:child_process";
import { mkdirSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const outputDir = resolve(here, "output");
const debugDir = resolve(outputDir, "debug");
mkdirSync(debugDir, { recursive: true });

const WIDTHS = [
  { width: 360, height: 640, compact: true },
  { width: 390, height: 844, compact: false },
];
const INSETS = [0, 24, 36];
const SCALES = [1.0, 1.3, 1.6, 2.0];
const PROGRESS = Array.from({ length: 21 }, (_, i) => i / 20);
const DIRECTIONS = ["forward", "reverse"];
const INTERACTIONS = ["touch", "settled"];
const DUALS = ["A", "B", "C"];
const CLOCKS = ["00:00", "11:11", "19:45", "23:59"];
const CLOCK_STRATEGIES = ["overlay", "fade"];

const contextual = [
  { id: "zikir-pagi", title: "Zikir pagi", start: 359, end: 431 },
  { id: "dhuha", title: "Dhuha", start: 450, end: 801 },
  { id: "zikir-petang", title: "Zikir petang", start: 1005, end: 1169 },
  { id: "al-mulk", title: "Surah Al-Mulk", start: 1242, end: 1380 },
  { id: "tahajud", title: "Tahajud", start: 210, end: 330 },
];

const prayers = [
  { id: "subuh", title: "Subuh", start: 359, end: 379 },
  { id: "zohor", title: "Zohor", start: 801, end: 821 },
  { id: "asar", title: "Asar", start: 1005, end: 1025 },
  { id: "maghrib", title: "Maghrib", start: 1169, end: 1189 },
  { id: "isyak", title: "Isyak", start: 1242, end: 1262 },
];

// Exact five overlap ranges plus ten minutes on either side, per synthesis.
const minuteRanges = [
  { id: "subuh-zikir", start: 349, end: 389 },
  { id: "zohor-dhuha", start: 791, end: 811 },
  { id: "asar-zikir", start: 995, end: 1035 },
  { id: "maghrib-zikir", start: 1159, end: 1179 },
  { id: "isyak-almulk", start: 1232, end: 1272 },
];

const minutes = minuteRanges.flatMap((range) =>
  Array.from({ length: range.end - range.start + 1 }, (_, i) => ({
    range: range.id,
    minute: range.start + i,
  })),
);

const wrapMinute = (value) => ((value % 1440) + 1440) % 1440;
const activeAt = (item, minute) => {
  const value = wrapMinute(minute);
  return item.start <= item.end
    ? value >= item.start && value <= item.end
    : value >= item.start || value <= item.end;
};
const activePrayerAt = (minute) => prayers.find((item) => activeAt(item, minute)) ?? null;
const activeContextAt = (minute) => contextual.find((item) => activeAt(item, minute)) ?? null;
const formatMinute = (minute) => `${String(Math.floor(wrapMinute(minute) / 60)).padStart(2, "0")}:${String(wrapMinute(minute) % 60).padStart(2, "0")}`;

const fontFiles = {
  sans: execFileSync("fc-match", ["-f", "%{file}", "sans-serif"], { encoding: "utf8" }),
  sansBold: execFileSync("fc-match", ["-f", "%{file}", "sans-serif:style=Bold"], { encoding: "utf8" }),
  mono: execFileSync("fc-match", ["-f", "%{file}", "monospace"], { encoding: "utf8" }),
};
const metricCache = new Map();

function measure(text, sp, scale, family = "sans", weight = "normal") {
  const pointSize = sp * scale;
  const key = [text, pointSize, family, weight].join("|");
  if (metricCache.has(key)) return metricCache.get(key);
  const requested = family === "sans" && weight === "bold" ? fontFiles.sansBold : fontFiles[family];
  const out = execFileSync("magick", [
    "-background", "none",
    "-font", requested,
    "-pointsize", String(pointSize),
    `label:${text}`,
    "-format", "%w,%h",
    "info:",
  ], { encoding: "utf8" }).trim();
  const [width, height] = out.split(",").map(Number);
  const result = { width, height, baselineProxy: height * 0.78, pointSize };
  metricCache.set(key, result);
  return result;
}

function measureLines(lines, sp, scale, family = "sans", weight = "normal", lineGap = 2) {
  const metrics = lines.map((line) => measure(line, sp, scale, family, weight));
  return {
    width: Math.max(...metrics.map((item) => item.width)),
    height: metrics.reduce((sum, item) => sum + item.height, 0) + lineGap * Math.max(0, metrics.length - 1),
    baselineProxy: metrics[0].baselineProxy,
    lines: metrics.length,
  };
}

const rect = (left, top, right, bottom) => ({ left, top, right, bottom });
const rectWidth = (value) => value.right - value.left;
const rectHeight = (value) => value.bottom - value.top;
const inside = (inner, outer) => inner.left >= outer.left && inner.top >= outer.top && inner.right <= outer.right && inner.bottom <= outer.bottom;
const intersects = (a, b) => a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top;
const centerY = (value) => (value.top + value.bottom) / 2;
const boxAt = (x, y, metric, paddingX = 4, paddingY = 3) => rect(x, y, x + metric.width + paddingX * 2, y + metric.height + paddingY * 2);

function sceneGeometry(config, progress) {
  const detailViewport = config.compact ? 182 : 220;
  const detailTop = config.compact ? 112 : 120;
  const detailCenter = detailViewport * 0.93;
  const outerRadius = Math.min(config.width * 0.445, detailCenter - 15);
  const glanceViewport = outerRadius * 2 + 28;
  const glanceGlobalCenter = config.height * (config.compact ? 0.47 : 0.48);
  const glanceCenter = outerRadius + 14;
  const glanceTop = glanceGlobalCenter - glanceCenter;
  return {
    wheelTop: glanceTop + (detailTop - glanceTop) * progress,
    viewportHeight: glanceViewport + (detailViewport - glanceViewport) * progress,
    centerY: glanceCenter + (detailCenter - glanceCenter) * progress,
    outerRadius,
  };
}

function chooseFirstValid(candidates, safeRect, hardRects) {
  for (const candidate of candidates) {
    if (inside(candidate.bounds, safeRect) && !hardRects.some((hard) => intersects(candidate.bounds, hard))) {
      return candidate;
    }
  }
  return null;
}

function dualLayout({ config, progress, scale, minute, dual, interaction }) {
  const geometry = sceneGeometry(config, progress);
  const safe = rect(12, 8, config.width - 12, geometry.viewportHeight - 8);
  const externalSafe = rect(12, geometry.wheelTop + geometry.viewportHeight + 4, config.width - 12, config.height - 12);
  const cx = config.width / 2;
  const prayerRadius = geometry.outerRadius * 0.775;
  const pointerY = geometry.centerY - prayerRadius;
  const pointerHard = rect(cx - 7, geometry.centerY - geometry.outerRadius * 0.88 - 16, cx + 7, pointerY + 9);
  const prayerNodeHard = rect(cx - 12, pointerY - 12, cx + 12, pointerY + 12);
  const hard = [pointerHard, prayerNodeHard];
  const prayer = activePrayerAt(minute);
  const context = activeContextAt(minute);
  const ownerType = prayer ? "fardhu" : context ? "context" : "none";
  // Cycle-2 gate: active fardhu retains full kind/name, active state, and start time.
  const ownerLines = prayer
    ? [`Solat ${prayer.title} aktif`, formatMinute(prayer.start)]
    : context ? [context.title] : [];
  const ownerText = ownerLines.join(" / ");
  const ownerMetric = ownerText
    ? measureLines(ownerLines, prayer ? 11 : 10, scale, "sans", "bold")
    : { width: 0, height: 0, baselineProxy: 0 };
  const ownerWidth = ownerMetric.width + 8;
  const ownerHeight = ownerMetric.height + 6;

  const ownerCandidates = [
    { id: "right", bounds: boxAt(cx + 18, pointerY - ownerHeight / 2, ownerMetric) },
    { id: "left", bounds: boxAt(cx - 18 - ownerWidth, pointerY - ownerHeight / 2, ownerMetric) },
    { id: "rail", bounds: boxAt(cx + 18, safe.top, ownerMetric) },
  ];
  const owner = ownerText ? chooseFirstValid(ownerCandidates, safe, hard) : null;
  const ownerFallbackMetric = ownerMetric;
  const ownerFallbackBounds = ownerText
    ? boxAt(externalSafe.left, externalSafe.top, ownerFallbackMetric)
    : null;
  const ownerFallbackFits = Boolean(ownerFallbackBounds) && inside(ownerFallbackBounds, externalSafe);

  let secondaryText = prayer && context ? `Amalan tambahan · ${context.title}` : "";
  if (dual === "C" && interaction === "touch" && secondaryText) secondaryText = "Amalan tambahan";
  const secondaryMetric = secondaryText ? measure(secondaryText, 9, scale, "sans", "normal") : { width: 0, height: 0, baselineProxy: 0 };
  const secondaryWidth = secondaryMetric.width + 8;
  const secondaryHeight = secondaryMetric.height + 6;
  const primaryBounds = owner?.bounds ?? null;
  const secondaryHard = primaryBounds ? [...hard, primaryBounds] : hard;
  let secondaryCandidates = [];
  if (secondaryText && owner) {
    if (dual === "A" || (dual === "C" && interaction === "settled")) {
      const rightX = owner.id === "left" ? cx - 18 - secondaryWidth : cx + 18;
      secondaryCandidates = [
        { id: "stack-below", bounds: boxAt(rightX, owner.bounds.bottom + 4, secondaryMetric) },
        { id: "stable-rail", bounds: boxAt(safe.left, safe.top, secondaryMetric) },
      ];
    } else if (dual === "B") {
      secondaryCandidates = [
        { id: "stable-rail", bounds: boxAt(safe.left, safe.top, secondaryMetric) },
        { id: "stack-below", bounds: boxAt(cx + 18, owner.bounds.bottom + 4, secondaryMetric) },
      ];
    } else {
      secondaryCandidates = [
        { id: "compact-cue", bounds: boxAt(cx + 18, owner.bounds.bottom + 4, secondaryMetric) },
        { id: "stable-rail", bounds: boxAt(safe.left, safe.top, secondaryMetric) },
      ];
    }
  }
  const secondary = secondaryText ? chooseFirstValid(secondaryCandidates, safe, secondaryHard) : null;
  // Fallback is an explicitly reserved external selected-context region below
  // the clipped wheel. Existing CTA/detail content must reflow around it.
  const secondaryFallbackTop = ownerFallbackFits
    ? ownerFallbackBounds.bottom + 4
    : externalSafe.top;
  const secondaryFallbackBounds = secondaryText
    ? boxAt(externalSafe.left, secondaryFallbackTop, secondaryMetric)
    : null;
  const secondaryFallback = Boolean(
    secondaryText && (owner || ownerFallbackFits) && !secondary &&
    secondaryFallbackBounds && inside(secondaryFallbackBounds, externalSafe),
  );

  const primaryReadable = ownerType !== "fardhu" || Boolean(owner || ownerFallbackFits);
  const secondarySafeOrFallback = !secondaryText || Boolean(secondary || secondaryFallback);
  const onePrimary = ownerType === "none" || Boolean(owner || ownerFallbackFits);
  const primaryNotSuppressed = ownerType !== "fardhu" || Boolean(owner || ownerFallbackFits);
  const baselineSafe = !owner || ownerMetric.baselineProxy > 0 && ownerMetric.baselineProxy < rectHeight(owner.bounds);

  return {
    ownerType,
    ownerText,
    contextText: context?.title ?? "",
    ownerCandidate: owner?.id ?? (ownerFallbackFits ? "full-width-fallback" : "none"),
    secondaryCandidate: secondary?.id ?? (secondaryFallback ? "detail-disclosure" : "none"),
    primaryReadable,
    secondarySafeOrFallback,
    onePrimary,
    primaryNotSuppressed,
    baselineSafe,
    ownerBounds: owner?.bounds ?? (ownerFallbackFits ? ownerFallbackBounds : null),
    secondaryBounds: secondary?.bounds ?? (secondaryFallback ? secondaryFallbackBounds : null),
    safe,
    externalSafe,
  };
}

function preferredClockTop(config, inset, scale) {
  return config.compact ? inset + 5 : inset + 5 + (25 + 12) * scale;
}

function clockLayout({ config, inset, scale, progress, text, strategy }) {
  const geometry = sceneGeometry(config, progress);
  const safe = rect(16, inset + 5, config.width - 16, geometry.wheelTop - 4);
  const sourceSp = config.compact ? 28 : 38;
  const destinationSp = 28;
  const sourceMetric = measure(text, sourceSp, scale, "mono", "normal");
  const destinationMetric = measure(text, destinationSp, scale, "mono", "normal");
  const sourceRect = rect(
    (config.width - sourceMetric.width) / 2,
    preferredClockTop(config, inset, scale),
    (config.width + sourceMetric.width) / 2,
    preferredClockTop(config, inset, scale) + sourceMetric.height,
  );
  const destinationRect = rect(
    config.width - 16 - destinationMetric.width,
    inset + 5,
    config.width - 16,
    inset + 5 + destinationMetric.height,
  );

  if (strategy === "fade") {
    const sourceVisible = progress < 0.45;
    const destinationVisible = progress > 0.55;
    const visible = sourceVisible ? [sourceRect] : destinationVisible ? [destinationRect] : [];
    const safeVisible = visible.every((bounds) => inside(bounds, safe));
    return {
      visibleCount: visible.length,
      safeVisible,
      gap: visible.length === 0,
      gapWidth: 0.10,
      role: sourceVisible ? "source" : destinationVisible ? "destination" : "gap",
      bounds: visible[0] ?? null,
      safe,
      baselineSafe: visible.length === 0 || (sourceVisible ? sourceMetric : destinationMetric).baselineProxy < rectHeight(visible[0]),
    };
  }

  const t = Math.max(0, Math.min(1, (progress - 0.10) / 0.40));
  const sp = sourceSp + (destinationSp - sourceSp) * t;
  const metric = measure(text, sp, scale, "mono", "normal");
  const preferredX = (config.width - sourceMetric.width) / 2 + (config.width - 16 - destinationMetric.width - (config.width - sourceMetric.width) / 2) * t;
  const preferredY = preferredClockTop(config, inset, scale) + (inset + 5 - preferredClockTop(config, inset, scale)) * t;
  // Constraint path: reserve metadata first, then keep the single overlay inside
  // the current header-safe rectangle. A negative available height still fails.
  const x = Math.max(safe.left, Math.min(preferredX, safe.right - metric.width));
  const y = Math.max(safe.top, Math.min(preferredY, safe.bottom - metric.height));
  const bounds = rect(x, y, x + metric.width, y + metric.height);
  return {
    visibleCount: 1,
    safeVisible: inside(bounds, safe),
    gap: false,
    gapWidth: 0,
    role: "overlay",
    bounds,
    safe,
    baselineSafe: metric.baselineProxy > 0 && metric.baselineProxy < rectHeight(bounds),
  };
}

const dualRows = [];
const clockRows = [];
const directionFingerprints = new Map();
const counterexamples = [];

for (const config of WIDTHS) {
  for (const inset of INSETS) {
    for (const scale of SCALES) {
      for (const progress of PROGRESS) {
        for (const direction of DIRECTIONS) {
          for (const { range, minute } of minutes) {
            for (const dual of DUALS) {
              for (const interaction of INTERACTIONS) {
                const result = dualLayout({ config, progress, scale, minute, dual, interaction });
                const row = {
                  width: config.width, height: config.height, inset, scale, progress,
                  direction, range, minute, minuteText: formatMinute(minute), dual, interaction,
                  ...result,
                };
                dualRows.push(row);
                const key = ["dual", config.width, inset, scale, progress, range, minute, dual, interaction].join("|");
                const fingerprint = JSON.stringify([result.ownerType, result.ownerCandidate, result.secondaryCandidate, result.primaryReadable, result.secondarySafeOrFallback]);
                if (directionFingerprints.has(key) && directionFingerprints.get(key) !== fingerprint) {
                  counterexamples.push({ type: "direction-dual", row });
                } else directionFingerprints.set(key, fingerprint);
                if ((!result.primaryReadable || !result.secondarySafeOrFallback || !result.baselineSafe) && counterexamples.length < 100) {
                  counterexamples.push({ type: "dual-gate", row });
                }
              }
            }
          }

          for (const direction of DIRECTIONS) {
            for (const text of CLOCKS) {
              for (const strategy of CLOCK_STRATEGIES) {
                const result = clockLayout({ config, inset, scale, progress, text, strategy });
                const row = {
                  width: config.width, height: config.height, inset, scale, progress,
                  direction, text, strategy, ...result,
                };
                clockRows.push(row);
                const key = ["clock", config.width, inset, scale, progress, text, strategy].join("|");
                const fingerprint = JSON.stringify([result.visibleCount, result.safeVisible, result.gap, result.role, result.bounds]);
                if (directionFingerprints.has(key) && directionFingerprints.get(key) !== fingerprint) {
                  counterexamples.push({ type: "direction-clock", row });
                } else directionFingerprints.set(key, fingerprint);
                if ((!result.safeVisible || result.visibleCount > 1 || !result.baselineSafe) && counterexamples.length < 100) {
                  counterexamples.push({ type: "clock-gate", row });
                }
              }
            }
          }
        }
      }
    }
  }
}

function csvValue(value) {
  if (value === null || value === undefined) return "";
  const text = typeof value === "object" ? JSON.stringify(value) : String(value);
  return /[",\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text;
}

function writeCsv(path, rows, columns) {
  const lines = [columns.join(",")];
  for (const row of rows) lines.push(columns.map((column) => csvValue(row[column])).join(","));
  writeFileSync(path, `${lines.join("\n")}\n`);
}

const dualColumns = [
  "width", "height", "inset", "scale", "progress", "direction", "range", "minute", "minuteText", "dual", "interaction",
  "ownerType", "ownerText", "contextText", "ownerCandidate", "secondaryCandidate", "primaryReadable", "secondarySafeOrFallback",
  "onePrimary", "primaryNotSuppressed", "baselineSafe", "ownerBounds", "secondaryBounds", "safe", "externalSafe",
];
const clockColumns = [
  "width", "height", "inset", "scale", "progress", "direction", "text", "strategy", "visibleCount", "safeVisible", "gap", "gapWidth",
  "role", "bounds", "safe", "baselineSafe",
];
writeCsv(resolve(outputDir, "dual-matrix.csv"), dualRows, dualColumns);
writeCsv(resolve(outputDir, "clock-matrix.csv"), clockRows, clockColumns);

const summarize = (rows, predicate) => rows.reduce((sum, row) => sum + (predicate(row) ? 1 : 0), 0);
const byDual = Object.fromEntries(DUALS.map((dual) => {
  const rows = dualRows.filter((row) => row.dual === dual);
  const overlapRows = rows.filter((row) => row.ownerType === "fardhu" && row.contextText);
  return [dual, {
    rows: rows.length,
    overlapRows: overlapRows.length,
    primaryReadableFailures: summarize(rows, (row) => !row.primaryReadable),
    primaryDirect: summarize(rows, (row) => row.ownerType !== "none" && !["none", "full-width-fallback"].includes(row.ownerCandidate)),
    primaryFallback: summarize(rows, (row) => row.ownerCandidate === "full-width-fallback"),
    secondarySafeOrFallbackFailures: summarize(rows, (row) => !row.secondarySafeOrFallback),
    baselineFailures: summarize(rows, (row) => !row.baselineSafe),
    secondaryDirect: summarize(rows, (row) => !["none", "detail-disclosure"].includes(row.secondaryCandidate)),
    secondaryFallback: summarize(rows, (row) => row.secondaryCandidate === "detail-disclosure"),
    ownerCandidateCounts: Object.fromEntries([...new Set(rows.map((row) => row.ownerCandidate))].sort().map((candidate) => [candidate, summarize(rows, (row) => row.ownerCandidate === candidate)])),
    secondaryCandidateCounts: Object.fromEntries([...new Set(rows.map((row) => row.secondaryCandidate))].sort().map((candidate) => [candidate, summarize(rows, (row) => row.secondaryCandidate === candidate)])),
    overlapByScale: Object.fromEntries(SCALES.map((scale) => {
      const subset = overlapRows.filter((row) => row.scale === scale);
      return [scale, {
        rows: subset.length,
        primaryFallback: summarize(subset, (row) => row.ownerCandidate === "full-width-fallback"),
        secondaryDirect: summarize(subset, (row) => !["none", "detail-disclosure"].includes(row.secondaryCandidate)),
        secondaryFallback: summarize(subset, (row) => row.secondaryCandidate === "detail-disclosure"),
      }];
    })),
  }];
}));
const byClock = Object.fromEntries(CLOCK_STRATEGIES.map((strategy) => {
  const rows = clockRows.filter((row) => row.strategy === strategy);
  const unsafeRows = rows.filter((row) => !row.safeVisible);
  const unsafeByConfiguration = {};
  for (const row of unsafeRows) {
    const key = `${row.width}x${row.height}/inset${row.inset}/scale${row.scale}`;
    const entry = unsafeByConfiguration[key] ?? { rows: 0, firstProgress: row.progress, lastProgress: row.progress };
    entry.rows += 1;
    entry.firstProgress = Math.min(entry.firstProgress, row.progress);
    entry.lastProgress = Math.max(entry.lastProgress, row.progress);
    unsafeByConfiguration[key] = entry;
  }
  return [strategy, {
    rows: rows.length,
    unsafe: unsafeRows.length,
    unsafeByConfiguration,
    visibleCountFailures: summarize(rows, (row) => row.visibleCount > 1),
    baselineFailures: summarize(rows, (row) => !row.baselineSafe),
    gapRows: summarize(rows, (row) => row.gap),
    declaredGapProgress: strategy === "fade" ? [0.45, 0.55] : null,
  }];
}));

const boundaryChecks = [
  [358, "none"], [359, "fardhu"], [379, "fardhu"], [380, "context"],
  [800, "context"], [801, "fardhu"], [802, "fardhu"],
  [1004, "none"], [1005, "fardhu"], [1025, "fardhu"], [1026, "context"],
  [1168, "context"], [1169, "fardhu"], [1170, "fardhu"],
  [1241, "none"], [1242, "fardhu"], [1262, "fardhu"], [1263, "context"],
].map(([minute, expected]) => ({ minute, expected, actual: activePrayerAt(minute) ? "fardhu" : activeContextAt(minute) ? "context" : "none" }));

const summary = {
  generatedAt: new Date().toISOString(),
  evidenceClass: "HOST_PROXY_NOT_ANDROID_TEXTMEASURER_PROOF",
  blocker: {
    resolvedDependency: "androidx.compose.ui:ui-text-android:1.7.6",
    reason: "Android AAR paragraph/font resolver requires Android runtime; no adb, emulator, configured AVD, or cached Robolectric runtime was available.",
  },
  versions: {
    node: process.version,
    imageMagick: execFileSync("magick", ["-version"], { encoding: "utf8" }).split("\n")[0],
    fontconfig: execFileSync("sh", ["-c", "fc-match --version 2>&1"], { encoding: "utf8" }).trim(),
    sansFont: fontFiles.sans,
    sansBoldFont: fontFiles.sansBold,
    monoFont: fontFiles.mono,
  },
  matrix: {
    widths: WIDTHS, insets: INSETS, scales: SCALES, progress: PROGRESS,
    directions: DIRECTIONS, interactions: INTERACTIONS, duals: DUALS,
    clocks: CLOCKS, clockStrategies: CLOCK_STRATEGIES, minuteRanges,
    minuteSamples: minutes.length,
  },
  rows: { dual: dualRows.length, clock: clockRows.length },
  ownership: {
    contract: "active fardhu first, independent of progress/direction/dual hypothesis",
    boundaryChecks,
    boundaryFailures: boundaryChecks.filter((item) => item.expected !== item.actual).length,
  },
  dual: byDual,
  clock: byClock,
  directionFailures: counterexamples.filter((item) => item.type.startsWith("direction-")).length,
  firstCounterexamples: counterexamples.slice(0, 30),
  limitations: [
    "ImageMagick/fontconfig metrics are not Compose TextLayoutResult metrics.",
    "Scale 2.0 is linear host scaling and does not reproduce Android 14 nonlinear font scaling.",
    "Header metadata is assumed reflowed/reserved before clock strategy; this harness proves only the allocated clock rectangle.",
    "Secondary detail-disclosure fallback proves recoverability in the state model, not visual discoverability.",
    "Leader routing, contrast, touch occlusion, TalkBack, and perceived calmness require later runtime/manual evidence.",
  ],
};
writeFileSync(resolve(outputDir, "summary.json"), `${JSON.stringify(summary, null, 2)}\n`);

function debugSvg(counterexample, index) {
  const row = counterexample.row;
  const width = row.width;
  const height = counterexample.type.includes("clock") ? Math.max(160, row.safe?.bottom + 20) : Math.max(220, row.safe?.bottom + 20);
  const shapes = [];
  if (row.safe) shapes.push(`<rect x="${row.safe.left}" y="${row.safe.top}" width="${rectWidth(row.safe)}" height="${rectHeight(row.safe)}" fill="none" stroke="#1f8a70" stroke-width="2"/>`);
  if (row.bounds) shapes.push(`<rect x="${row.bounds.left}" y="${row.bounds.top}" width="${rectWidth(row.bounds)}" height="${rectHeight(row.bounds)}" fill="#e4a43a55" stroke="#e4a43a" stroke-width="2"/>`);
  if (row.ownerBounds) shapes.push(`<rect x="${row.ownerBounds.left}" y="${row.ownerBounds.top}" width="${rectWidth(row.ownerBounds)}" height="${rectHeight(row.ownerBounds)}" fill="#e4a43a55" stroke="#e4a43a" stroke-width="2"/>`);
  if (row.secondaryBounds) shapes.push(`<rect x="${row.secondaryBounds.left}" y="${row.secondaryBounds.top}" width="${rectWidth(row.secondaryBounds)}" height="${rectHeight(row.secondaryBounds)}" fill="#8fb9a855" stroke="#8fb9a8" stroke-width="2"/>`);
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}"><rect width="100%" height="100%" fill="#052b23"/>${shapes.join("")}<text x="8" y="${height - 8}" fill="#fff" font-size="10">${counterexample.type} ${row.strategy ?? row.dual ?? ""} p=${row.progress} scale=${row.scale}</text></svg>\n`;
}
counterexamples.slice(0, 12).forEach((item, index) => writeFileSync(resolve(debugDir, `${String(index + 1).padStart(2, "0")}-${item.type}.svg`), debugSvg(item, index)));

execFileSync("gzip", ["-f", "-9", resolve(outputDir, "dual-matrix.csv")]);
execFileSync("gzip", ["-f", "-9", resolve(outputDir, "clock-matrix.csv")]);

console.log(JSON.stringify({ outputDir, rows: summary.rows, dual: summary.dual, clock: summary.clock, directionFailures: summary.directionFailures, boundaryFailures: summary.ownership.boundaryFailures }, null, 2));
