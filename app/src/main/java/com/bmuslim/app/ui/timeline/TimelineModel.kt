package com.bmuslim.app.ui.timeline

import java.time.LocalTime

/**
 * A recommended deed shown on the timeline with its best window.
 *
 * Milestone 1 keeps this deliberately simple. Later milestones add the real domain model
 * (anchors, windows, citations with links, levels) described in docs/PRD.md.
 */
data class TimelineDeed(
    val title: String,
    val windowLabel: String,
    val start: LocalTime,
    val end: LocalTime,
    val citation: String,
    val done: Boolean = false,
)

/**
 * Hardcoded sample data with fake prayer times, just to build and learn the UI.
 * Later this is replaced by real JAKIM times + the curated, cited deed catalogue.
 */
fun sampleDeeds(): List<TimelineDeed> = listOf(
    TimelineDeed(
        title = "Zikir pagi (Morning adhkar)",
        windowLabel = "Selepas Subuh → Syuruk",
        start = LocalTime.of(5, 50),
        end = LocalTime.of(7, 5),
        citation = "Abu Dawud 5074 · sahih",
    ),
    TimelineDeed(
        title = "Solat Dhuha",
        windowLabel = "Selepas syuruk → sebelum Zohor",
        start = LocalTime.of(7, 25),
        end = LocalTime.of(13, 0),
        citation = "Muslim 720",
    ),
    TimelineDeed(
        title = "Zikir petang (Evening adhkar)",
        windowLabel = "Selepas Asar → Maghrib",
        start = LocalTime.of(16, 30),
        end = LocalTime.of(19, 25),
        citation = "Abu Dawud 5074 · sahih",
    ),
    TimelineDeed(
        title = "Baca Surah Al-Mulk",
        windowLabel = "Sebelum tidur",
        start = LocalTime.of(21, 30),
        end = LocalTime.of(23, 0),
        citation = "Tirmidhi 2891 · hasan",
    ),
    TimelineDeed(
        title = "Solat Tahajud",
        windowLabel = "1/3 malam terakhir",
        start = LocalTime.of(3, 30),
        end = LocalTime.of(5, 30),
        citation = "Bukhari 1145",
    ),
).sortedBy { it.start }
