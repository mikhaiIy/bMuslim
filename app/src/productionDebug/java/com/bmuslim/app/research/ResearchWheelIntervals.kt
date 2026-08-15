package com.bmuslim.app.research

import com.bmuslim.app.prayer.JakimAlmanacCategory
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimAlmanacSlot
import com.bmuslim.app.ui.almanac.AlmanacCoverageSegment
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacIntervalTruthClass
import com.bmuslim.app.ui.almanac.AlmanacIntervalVisual
import com.bmuslim.app.ui.almanac.formatMinute
import com.bmuslim.app.ui.almanac.intervalSemanticallyContains
import com.bmuslim.app.ui.almanac.memberCoversAt
import com.bmuslim.app.ui.almanac.normalizeMinute
import com.bmuslim.app.ui.almanac.selectIntervalForMinute
import com.bmuslim.app.ui.almanac.semanticallyIntersectingIntervalsAt
import java.time.DayOfWeek
import java.time.LocalDate
import java.text.Normalizer
import java.util.Locale

internal enum class ResearchLunarDateContextState {
    Missing,
    Ready,
    Mismatched,
    Blocked,
    Unresolved,
    ;

    companion object {
        // Upper-case aliases keep the state readable in data fixtures and tests.
        val MISSING = Missing
        val READY = Ready
        val MISMATCHED = Mismatched
        val BLOCKED = Blocked
        val UNRESOLVED = Unresolved
        val Available = Ready
        val AVAILABLE = Ready
    }
}

/**
 * The preview never derives a Hijri date. A caller must inject an explicit, reviewed context.
 */
internal data class ResearchLunarDateContext(
    val policyId: String? = null,
    val fingerprint: String? = null,
    val state: ResearchLunarDateContextState = ResearchLunarDateContextState.Missing,
    val civilDate: LocalDate? = null,
    val lunarMonth: String? = null,
    val lunarDay: Int? = null,
) {
    companion object {
        fun missing(): ResearchLunarDateContext = ResearchLunarDateContext()

        fun ready(
            civilDate: LocalDate,
            lunarMonth: String,
            lunarDay: Int,
            policyId: String,
            fingerprint: String,
        ): ResearchLunarDateContext = ResearchLunarDateContext(
            policyId = policyId,
            fingerprint = fingerprint,
            state = ResearchLunarDateContextState.Ready,
            civilDate = civilDate,
            lunarMonth = lunarMonth,
            lunarDay = lunarDay,
        )
    }
}

internal data class ResearchPersonalEvent(
    val deed: ResearchDeed,
    val relationLabel: String,
    val accessibilityText: String,
    val occurrence: ResearchTemporalOccurrence,
)

internal data class ResearchDayContext(
    val deed: ResearchDeed,
    val dayLabel: String,
    val accessibilityText: String,
    val occurrence: ResearchTemporalOccurrence,
    val resolved: Boolean = true,
)

internal data class ResearchApplicability(
    val basis: String,
    val value: String,
)

internal data class ResearchValidityWindow(
    val startMinute: Int,
    val endMinute: Int,
    val boundaryPolicy: String,
)

internal data class ResearchTrigger(
    val relation: String,
    val event: String,
    val scheduleAnchorMinute: Int? = null,
)

internal data class ResearchFocusRegion(
    val startMinute: Int,
    val endMinute: Int,
    val terminalMinute: Int,
)

internal data class ResearchTemporalOccurrence(
    val id: String,
    val deedId: String,
    val applicability: ResearchApplicability,
    val validityWindow: ResearchValidityWindow? = null,
    val trigger: ResearchTrigger? = null,
    val focusRegion: ResearchFocusRegion? = null,
    val sourceExpression: String,
    val truthClass: AlmanacIntervalTruthClass,
    val completionId: String = "completion.$id",
    val proxyGroupKey: String? = null,
) {
    val occurrenceId: String get() = id
    val completionKey: String get() = completionId
}

/** Member-local presentation survives an aggregate Canvas union. */
internal data class ResearchOccurrencePresentation(
    val timingLabel: String,
    val visual: AlmanacIntervalVisual,
    val truthClass: AlmanacIntervalTruthClass,
    val applicability: ResearchApplicability,
)

internal data class ResearchEventGroup(
    val key: String,
    val prayerLabel: String,
    val markerMinute: Int,
    val occurrenceIds: List<String>,
    val deedIds: List<String>,
    val completionIds: List<String>,
) {
    val memberCount: Int get() = occurrenceIds.size
}

internal data class ResearchTimelinePlan(
    val intervals: List<AlmanacInterval>,
    val personalEvents: List<ResearchPersonalEvent>,
    val dayContexts: List<ResearchDayContext>,
    val visibleTodayDeeds: List<ResearchDeed>,
    val occurrences: List<ResearchTemporalOccurrence>,
    val eventGroups: List<ResearchEventGroup> = emptyList(),
    val overflowGroups: List<ResearchOverflowGroup> = emptyList(),
    val lunarContext: ResearchLunarDateContext = ResearchLunarDateContext.missing(),
    val occurrenceCoverage: Map<String, List<AlmanacCoverageSegment>> = emptyMap(),
    val occurrencePresentations: Map<String, ResearchOccurrencePresentation> = emptyMap(),
)

internal data class ResearchPointerSelection(
    val deed: ResearchDeed,
    val occurrence: ResearchTemporalOccurrence,
    val interval: AlmanacInterval,
    val intersectsPointer: Boolean,
    val intersectingIntervals: List<AlmanacInterval>,
    val eventGroup: ResearchEventGroup? = null,
    val presentation: ResearchOccurrencePresentation? = null,
)

internal enum class ResearchConcordanceSection {
    PERISTIWA,
    TEMPOH,
    HARI_INI,
    PERISTIWA_PERIBADI,
}

internal data class ResearchConcordanceItem(
    val id: String,
    val section: ResearchConcordanceSection,
    val deed: ResearchDeed,
    val occurrence: ResearchTemporalOccurrence,
    val interval: AlmanacInterval?,
    val timingLabel: String,
    val truthClass: AlmanacIntervalTruthClass,
    val visual: AlmanacIntervalVisual?,
) {
    val applicability: ResearchApplicability get() = occurrence.applicability
}

internal data class ResearchConcordanceSectionModel(
    val section: ResearchConcordanceSection,
    val label: String,
    val items: List<ResearchConcordanceItem>,
)

internal data class ResearchPointerConcordance(
    val minute: Int,
    val sections: List<ResearchConcordanceSectionModel>,
) {
    val exactItems: List<ResearchConcordanceItem> get() = sections.take(2).flatMap { it.items }
    val eventItems: List<ResearchConcordanceItem> get() = sections.first { it.section == ResearchConcordanceSection.PERISTIWA }.items
    val semanticItems: List<ResearchConcordanceItem> get() = sections.first { it.section == ResearchConcordanceSection.TEMPOH }.items
    val dayItems: List<ResearchConcordanceItem> get() = sections.first { it.section == ResearchConcordanceSection.HARI_INI }.items
    val personalItems: List<ResearchConcordanceItem> get() = sections.first { it.section == ResearchConcordanceSection.PERISTIWA_PERIBADI }.items
    val exactItemCount: Int get() = exactItems.size
}

internal fun researchPointerSelection(
    catalog: ResearchCatalog,
    plan: ResearchTimelinePlan,
    minute: Int,
): ResearchPointerSelection? {
    val interval = selectIntervalForMinute(plan.intervals, minute) ?: return null
    val occurrenceId = interval.memberIds.firstOrNull { plan.occurrenceCoversAt(interval, it, minute) } ?: interval.id
    return pointerSelectionFor(
        catalog = catalog,
        plan = plan,
        interval = interval,
        occurrenceId = occurrenceId,
        minute = minute,
        intersecting = semanticallyIntersectingIntervalsAt(plan.intervals, minute),
    )
}

internal fun researchPointerOptions(
    catalog: ResearchCatalog,
    plan: ResearchTimelinePlan,
    minute: Int,
): List<ResearchPointerSelection> {
    val intersecting = semanticallyIntersectingIntervalsAt(plan.intervals, minute)
    return intersecting.flatMap { interval ->
        val memberIds = interval.memberIds.ifEmpty { listOf(interval.id) }
        memberIds.filter { occurrenceId -> plan.occurrenceCoversAt(interval, occurrenceId, minute) }.mapNotNull { occurrenceId ->
            pointerSelectionFor(catalog, plan, interval, occurrenceId, minute, intersecting)
        }
    }
}

private fun ResearchTimelinePlan.occurrenceCoversAt(
    interval: AlmanacInterval,
    occurrenceId: String,
    minute: Int,
): Boolean {
    if (interval.memberCoverage.isNotEmpty()) return interval.memberCoversAt(occurrenceId, minute)
    if (!interval.isOverflowAggregate) return intervalSemanticallyContains(interval, minute)
    val selected = normalizeMinute(minute)
    return occurrenceCoverage[occurrenceId].orEmpty().any { segment ->
        if (segment.startMinute <= segment.endMinute) selected in segment.startMinute..segment.endMinute
        else selected >= segment.startMinute || selected <= segment.endMinute
    }
}

internal fun researchPointerConcordance(
    catalog: ResearchCatalog,
    plan: ResearchTimelinePlan,
    minute: Int,
): ResearchPointerConcordance {
    val exact = researchPointerOptions(catalog, plan, minute)
    val eventItems = exact
        .filter { it.interval.visual == AlmanacIntervalVisual.EventTrigger }
        .map { selection -> selection.toConcordanceItem(ResearchConcordanceSection.PERISTIWA) }
        .sortedBy { it.occurrence.id }
    val semanticItems = exact
        .filter { it.interval.visual != AlmanacIntervalVisual.EventTrigger }
        .map { selection -> selection.toConcordanceItem(ResearchConcordanceSection.TEMPOH) }
        .sortedBy { it.occurrence.id }
    val dayItems = plan.dayContexts
        .map { context ->
            ResearchConcordanceItem(
                id = context.occurrence.id,
                section = ResearchConcordanceSection.HARI_INI,
                deed = context.deed,
                occurrence = context.occurrence,
                interval = null,
                timingLabel = context.dayLabel,
                truthClass = context.occurrence.truthClass,
                visual = null,
            )
        }
        .sortedBy { it.occurrence.id }
    val personalItems = plan.personalEvents
        .map { event ->
            ResearchConcordanceItem(
                id = event.occurrence.id,
                section = ResearchConcordanceSection.PERISTIWA_PERIBADI,
                deed = event.deed,
                occurrence = event.occurrence,
                interval = null,
                timingLabel = event.relationLabel,
                truthClass = event.occurrence.truthClass,
                visual = null,
            )
        }
        .sortedBy { it.occurrence.id }
    return ResearchPointerConcordance(
        minute = normalizeMinute(minute),
        sections = listOf(
            ResearchConcordanceSectionModel(ResearchConcordanceSection.PERISTIWA, "PERISTIWA", eventItems),
            ResearchConcordanceSectionModel(ResearchConcordanceSection.TEMPOH, "TEMPOH", semanticItems),
            ResearchConcordanceSectionModel(ResearchConcordanceSection.HARI_INI, "HARI INI", dayItems),
            ResearchConcordanceSectionModel(ResearchConcordanceSection.PERISTIWA_PERIBADI, "PERISTIWA PERIBADI", personalItems),
        ),
    )
}

internal fun researchConcordanceCollisionCopy(concordance: ResearchPointerConcordance): String {
    val eventCount = concordance.eventItems.size
    val semanticCount = concordance.semanticItems.size
    return buildList {
        if (eventCount > 0) add("$eventCount selepas solat")
        if (semanticCount > 0) add("$semanticCount dalam tempoh")
    }.joinToString(" · ").ifBlank { "Tiada amalan tepat pada masa dipilih" }
}

private fun ResearchPointerSelection.toConcordanceItem(section: ResearchConcordanceSection): ResearchConcordanceItem {
    val memberPresentation = presentation ?: ResearchOccurrencePresentation(
        timingLabel = interval.timingLabel ?: formatMinute(interval.markerMinute),
        visual = interval.visual,
        truthClass = occurrence.truthClass,
        applicability = occurrence.applicability,
    )
    return ResearchConcordanceItem(
        id = occurrence.id,
        section = section,
        deed = deed,
        occurrence = occurrence,
        interval = interval,
        timingLabel = memberPresentation.timingLabel,
        truthClass = memberPresentation.truthClass,
        visual = memberPresentation.visual,
    )
}

private fun pointerSelectionFor(
    catalog: ResearchCatalog,
    plan: ResearchTimelinePlan,
    interval: AlmanacInterval,
    occurrenceId: String,
    minute: Int,
    intersecting: List<AlmanacInterval>,
): ResearchPointerSelection? {
    val occurrence = plan.occurrences.firstOrNull { it.id == occurrenceId } ?: return null
    val deed = catalog.deeds.firstOrNull { it.id == occurrence.deedId } ?: return null
    val group = interval.groupKey?.let { key -> plan.eventGroups.firstOrNull { it.key == key } }
    return ResearchPointerSelection(
        deed = deed,
        occurrence = occurrence,
        interval = interval,
        intersectsPointer = intervalSemanticallyContains(interval, minute),
        intersectingIntervals = intersecting,
        eventGroup = group,
        presentation = plan.occurrencePresentations[occurrenceId],
    )
}

internal data class ResearchLaneCandidate(
    val id: String,
    val startMinute: Int,
    val endMinute: Int,
)

internal data class ResearchCoverageSegment(
    val startMinute: Int,
    val endMinute: Int,
)

internal data class ResearchOverflowGroup(
    val id: String,
    val memberIds: List<String>,
    val coverageSegments: List<ResearchCoverageSegment>,
)

internal data class ResearchSemanticLanePlan(
    val laneBudget: Int,
    val assignments: Map<String, Int>,
    val overflow: ResearchOverflowGroup?,
)

/** Pure, stable interval packing. It returns an honest union for overflow instead of throwing. */
internal fun planResearchSemanticLanes(
    candidates: List<ResearchLaneCandidate>,
    composition: AlmanacFontComposition,
): ResearchSemanticLanePlan {
    require(candidates.map(ResearchLaneCandidate::id).distinct().size == candidates.size)
    val budget = when (composition) {
        AlmanacFontComposition.Regular -> 3
        AlmanacFontComposition.Comfortable,
        AlmanacFontComposition.Large,
        -> 2
        AlmanacFontComposition.Maximum -> 1
    }
    fun assign(capacity: Int): Pair<Map<String, Int>, List<ResearchLaneCandidate>> {
        val occupied = List(capacity) { mutableSetOf<Int>() }
        val assignments = linkedMapOf<String, Int>()
        val overflow = mutableListOf<ResearchLaneCandidate>()
        candidates.sortedWith(compareBy<ResearchLaneCandidate> { normalizeMinute(it.startMinute) }.thenBy { normalizeMinute(it.endMinute) }.thenBy { it.id })
            .forEach { candidate ->
                val minutes = candidate.visualMinutes()
                val lane = occupied.indexOfFirst { it.intersect(minutes).isEmpty() }
                if (lane < 0) overflow += candidate
                else {
                    occupied[lane].addAll(minutes)
                    assignments[candidate.id] = lane + 1
                }
            }
        return assignments to overflow
    }

    val first = assign(budget)
    if (first.second.isEmpty()) return ResearchSemanticLanePlan(budget, first.first, null)
    val reservedOuterLane = assign((budget - 1).coerceAtLeast(0))
    val overflow = reservedOuterLane.second
    return ResearchSemanticLanePlan(
        laneBudget = budget,
        assignments = reservedOuterLane.first,
        overflow = ResearchOverflowGroup(
            id = "overflow.semantic",
            memberIds = overflow.map(ResearchLaneCandidate::id),
            coverageSegments = unionCoverage(overflow),
        ),
    )
}

private fun ResearchLaneCandidate.visualMinutes(): Set<Int> {
    val result = linkedSetOf<Int>()
    var minute = normalizeMinute(startMinute)
    val final = normalizeMinute(endMinute)
    while (true) {
        result += minute
        if (minute == final || result.size == 1440) break
        minute = normalizeMinute(minute + 1)
    }
    return result
}

private fun unionCoverage(candidates: List<ResearchLaneCandidate>): List<ResearchCoverageSegment> {
    if (candidates.isEmpty()) return emptyList()
    val occupied = BooleanArray(1440)
    candidates.forEach { candidate -> candidate.visualMinutes().forEach { occupied[it] = true } }
    val segments = mutableListOf<ResearchCoverageSegment>()
    var start: Int? = null
    occupied.forEachIndexed { minute, present ->
        when {
            present && start == null -> start = minute
            !present && start != null -> {
                segments += ResearchCoverageSegment(start!!, minute - 1)
                start = null
            }
        }
    }
    if (start != null) segments += ResearchCoverageSegment(start!!, 1439)
    return segments
}

internal fun ResearchLunarDateContext.resolutionFor(scheduleDate: LocalDate): ResearchLunarDateContextState {
    if (state != ResearchLunarDateContextState.Ready) return state
    if (civilDate != scheduleDate || policyId.isNullOrBlank() || fingerprint.isNullOrBlank()) {
        return ResearchLunarDateContextState.Mismatched
    }
    val day = lunarDay ?: return ResearchLunarDateContextState.Mismatched
    val month = lunarMonth?.let(::normalizeLunarMonth)
        ?: return ResearchLunarDateContextState.Mismatched
    if (day !in 1..30) return ResearchLunarDateContextState.Mismatched
    if (day == 13 && month in DHU_AL_HIJJAH_NAMES) {
        return ResearchLunarDateContextState.Blocked
    }
    return if (day in 13..15) ResearchLunarDateContextState.Ready
    else ResearchLunarDateContextState.Unresolved
}

private fun normalizeLunarMonth(value: String): String =
    Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace("\u2019", "'")
        .replace(Regex("\\p{M}+"), "")
        .filter(Char::isLetter)

private val DHU_AL_HIJJAH_NAMES = setOf(
    "dhuulhijjah",
    "dhuulhijja",
    "dhuulhijah",
    "dhualhijjah",
    "dhualhijja",
    "dhualhijah",
    "dhulhijjah",
    "dhulhijja",
    "dhulhijah",
    "dulhijjah",
    "dulhijja",
    "dulhijah",
    "zulhijjah",
    "zulhijja",
    "zulhijah",
    "dzulhijjah",
    "dzulhijja",
    "dzulhijah",
)

internal fun researchTimelinePlan(
    catalog: ResearchCatalog,
    schedule: JakimAlmanacSchedule,
    selectedDeedId: String? = null,
    lunarContext: ResearchLunarDateContext = ResearchLunarDateContext.missing(),
    composition: AlmanacFontComposition = AlmanacFontComposition.Regular,
): ResearchTimelinePlan {
    val minutes = schedule.values.associate { value ->
        value.slot to value.time.hour * 60 + value.time.minute
    }
    fun minute(slot: JakimAlmanacSlot) = requireNotNull(minutes[slot])
    val date = schedule.exactKey.civilDate
    val weekday = date.dayOfWeek
    val raw = mutableListOf<ResearchIntervalDraft>()
    val eventMembers = mutableListOf<ResearchEventMemberDraft>()
    val personalEvents = mutableListOf<ResearchPersonalEvent>()
    val dayContexts = mutableListOf<ResearchDayContext>()

    fun occurrenceId(deed: ResearchDeed, suffix: String) = "$date.${deed.id}.$suffix"
    fun completionId(deed: ResearchDeed, suffix: String) = "completion.$date.${deed.id}.$suffix"
    fun addDayContext(
        deed: ResearchDeed,
        label: String,
        accessibility: String,
        basis: String,
        value: String,
        truthClass: AlmanacIntervalTruthClass,
        resolved: Boolean = true,
    ) {
        val suffix = value.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        val id = occurrenceId(deed, suffix.ifBlank { "day" })
        dayContexts += ResearchDayContext(
            deed = deed,
            dayLabel = label,
            accessibilityText = accessibility,
            resolved = resolved,
            occurrence = ResearchTemporalOccurrence(
                id = id,
                deedId = deed.id,
                applicability = ResearchApplicability(basis, value),
                sourceExpression = deed.sourceTimePhrase,
                truthClass = truthClass,
                completionId = completionId(deed, suffix.ifBlank { "day" }),
            ),
        )
    }

    catalog.deeds.forEach { deed ->
        when (deed.id) {
            "deed.daily.three-quls-morning-evening" -> {
                raw += estimate(
                    deed,
                    occurrence = "morning",
                    start = minute(JakimAlmanacSlot.FAJR),
                    end = minute(JakimAlmanacSlot.SYURUK),
                    marker = minute(JakimAlmanacSlot.FAJR),
                    note = "Jalur penuh pagi ialah anggaran struktur Subuh hingga Syuruk; sempadan pagi belum diluluskan.",
                )
                raw += estimate(
                    deed,
                    occurrence = "evening",
                    start = minute(JakimAlmanacSlot.ASR),
                    end = minute(JakimAlmanacSlot.MAGHRIB),
                    marker = minute(JakimAlmanacSlot.ASR),
                    note = "Jalur penuh petang ialah anggaran struktur Asar hingga Maghrib; sempadan petang belum diluluskan.",
                )
            }

            "deed.daily.post-prayer-tasbih",
            "deed.daily.post-prayer-help-in-worship",
            -> {
                schedule.values
                    .filter { it.slot.category == JakimAlmanacCategory.OBLIGATORY_PRAYER }
                    .forEach { value ->
                        val prayerMinute = value.time.hour * 60 + value.time.minute
                        val slotId = value.slot.name.lowercase()
                        eventMembers += ResearchEventMemberDraft(
                            deed = deed,
                            prayerSlot = value.slot,
                            markerMinute = prayerMinute,
                            occurrenceId = occurrenceId(deed, slotId),
                            completionId = completionId(deed, slotId),
                        )
                    }
            }

            "deed.daily.duha-two-rakah" -> raw += estimate(
                deed,
                occurrence = "forenoon",
                start = minute(JakimAlmanacSlot.DHUHA),
                end = minute(JakimAlmanacSlot.DHUHR),
                marker = minute(JakimAlmanacSlot.DHUHA),
                note = "Jalur penuh Dhuha hingga Zohor ialah anggaran struktur; penanda Dhuha JAKIM bukan dasar hukum yang diluluskan.",
            )

            "deed.daily.bedtime-three-quls" -> personalEvents += ResearchPersonalEvent(
                deed = deed,
                relationLabel = "Ketika hendak tidur",
                accessibilityText = "${deed.title}. Frasa waktu sumber: ${deed.sourceTimePhrase} Jenis visual: peristiwa peribadi tanpa jam. Kelas pemetaan: EVENT_REQUIRED. Tidak diletakkan pada waktu Isyak atau jam tetap.",
                occurrence = ResearchTemporalOccurrence(
                    id = occurrenceId(deed, "intend-sleep-at-night"),
                    deedId = deed.id,
                    applicability = ResearchApplicability("PERSONAL_EVENT_OCCURRENCE", "INTEND_SLEEP_AT_NIGHT"),
                    trigger = ResearchTrigger("AT", "INTEND_SLEEP_AT_NIGHT"),
                    sourceExpression = deed.sourceTimePhrase,
                    truthClass = AlmanacIntervalTruthClass.EventRequired,
                    completionId = completionId(deed, "intend-sleep-at-night"),
                ),
            )

            "deed.daily.last-third-dua-istighfar" -> {
                val fajr = minute(JakimAlmanacSlot.FAJR)
                val maghrib = minute(JakimAlmanacSlot.MAGHRIB)
                val nightLength = 1440 - maghrib + fajr
                val estimatedStart = normalizeMinute(fajr - nightLength / 3)
                raw += estimate(
                    deed,
                    occurrence = "last-third",
                    start = estimatedStart,
                    end = fajr,
                    marker = estimatedStart,
                    visual = AlmanacIntervalVisual.NightSegmentEstimate,
                    boundaryPolicy = "MAGHRIB_TO_SAME_SCHEDULE_FAJR_PROTOTYPE",
                    note = "Jalur penuh berjalur dikira untuk prototaip daripada Maghrib hingga Subuh pada jadual ini; dasar sempadan malam belum diluluskan.",
                )
            }

            "deed.daily.witr-before-sleep" -> personalEvents += ResearchPersonalEvent(
                deed = deed,
                relationLabel = "Sebelum tidur",
                accessibilityText = "${deed.title}. Frasa waktu sumber: ${deed.sourceTimePhrase} Jenis visual: peristiwa peribadi tanpa jam. Kelas pemetaan: EVENT_REQUIRED. Tidak diletakkan selepas Isyak atau pada jam tetap.",
                occurrence = ResearchTemporalOccurrence(
                    id = occurrenceId(deed, "before-sleep"),
                    deedId = deed.id,
                    applicability = ResearchApplicability("PERSONAL_EVENT_OCCURRENCE", "INTEND_SLEEP"),
                    trigger = ResearchTrigger("BEFORE", "INTEND_SLEEP"),
                    sourceExpression = deed.sourceTimePhrase,
                    truthClass = AlmanacIntervalTruthClass.EventRequired,
                    completionId = completionId(deed, "before-sleep"),
                ),
            )

            "deed.weekly.fast-monday-thursday" -> {
                if (weekday == DayOfWeek.MONDAY || weekday == DayOfWeek.THURSDAY) {
                    val label = if (weekday == DayOfWeek.MONDAY) "Isnin" else "Khamis"
                    addDayContext(
                        deed = deed,
                        label = label,
                        accessibility = "${deed.title}. Frasa waktu sumber: ${deed.sourceTimePhrase} Jenis visual: reben konteks hari. Kelas pemetaan: DIRECT untuk hari, UNRESOLVED untuk sempadan puasa. Bukan jalur waktu puasa.",
                        basis = "SCHEDULE_ZONE_LOCAL_WEEKDAY",
                        value = weekday.name,
                        truthClass = AlmanacIntervalTruthClass.Direct,
                    )
                }
            }

            "deed.weekly.friday-salawat" -> {
                if (weekday == DayOfWeek.FRIDAY) {
                    addDayContext(
                        deed = deed,
                        label = "Jumaat",
                        accessibility = "${deed.title}. Frasa waktu sumber: ${deed.sourceTimePhrase} Jenis visual: reben konteks hari. Kelas pemetaan: UNRESOLVED. Konteks Jumaat sahaja; sempadan hari agama masih memerlukan dasar yang disemak.",
                        basis = "RELIGIOUS_DAY_POLICY_REQUIRED",
                        value = "FRIDAY",
                        truthClass = AlmanacIntervalTruthClass.Unresolved,
                    )
                }
            }

            "deed.weekly.friday-final-period-dua" -> {
                if (weekday == DayOfWeek.FRIDAY) {
                    raw += estimate(
                        deed,
                        occurrence = "friday-after-asr-search-region",
                        start = minute(JakimAlmanacSlot.ASR),
                        end = minute(JakimAlmanacSlot.MAGHRIB),
                        marker = minute(JakimAlmanacSlot.MAGHRIB),
                        visual = AlmanacIntervalVisual.SearchEnvelope,
                        truthClass = AlmanacIntervalTruthClass.Unresolved,
                        focusRegion = ResearchFocusRegion(
                            startMinute = minute(JakimAlmanacSlot.ASR),
                            endMinute = minute(JakimAlmanacSlot.MAGHRIB),
                            terminalMinute = minute(JakimAlmanacSlot.MAGHRIB),
                        ),
                        note = "Jalur penuh berpalang Asar hingga Maghrib ialah kawasan carian visual, bukan dakwaan bahawa seluruh tempoh ialah waktu tersebut.",
                    )
                }
            }

            "deed.monthly.fast-white-days" -> Unit
        }
    }

    catalog.deeds.firstOrNull { it.id == "deed.monthly.fast-white-days" }?.let { deed ->
        val lunarResolution = lunarContext.resolutionFor(date)
        when (lunarResolution) {
            ResearchLunarDateContextState.Ready -> {
                val day = requireNotNull(lunarContext.lunarDay)
                if (day in deed.lunarDateSet) {
                    addDayContext(
                        deed = deed,
                        label = "Hari lunar $day",
                        accessibility = "${deed.title}. Konteks lunar yang disuntikkan: ${lunarContext.lunarMonth} $day. Jenis visual: reben konteks hari. Dasar kelayakan masih berasingan daripada geometri jam.",
                        basis = "RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED",
                        value = "${lunarContext.lunarMonth}:$day",
                        truthClass = AlmanacIntervalTruthClass.PolicyMapped,
                    )
                }
            }
            ResearchLunarDateContextState.Blocked -> addDayContext(
                deed = deed,
                label = "Hari lunar 13 · disekat",
                accessibility = "${deed.title}. Konteks Dhu al-Hijjah 13 disekat oleh polisi kelayakan Tashriq. Tiada jalur puasa atau dakwaan kebolehlaksanaan dipaparkan.",
                basis = "RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED",
                value = "BLOCKED_DHU_AL_HIJJAH_13",
                truthClass = AlmanacIntervalTruthClass.Unresolved,
                resolved = false,
            )
            ResearchLunarDateContextState.Missing,
            ResearchLunarDateContextState.Mismatched,
            ResearchLunarDateContextState.Unresolved,
            -> addDayContext(
                deed = deed,
                label = "Hari putih · belum disahkan",
                accessibility = "${deed.title}. Tarikh lunar atau polisi kelayakan belum disuntik dengan lengkap. Reben ini tidak membuktikan tarikh atau kebolehlaksanaan puasa.",
                basis = "RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED",
                value = lunarResolution.name.uppercase(),
                truthClass = AlmanacIntervalTruthClass.Unresolved,
                resolved = false,
            )
        }
    }

    val eventGroups = eventMembers
        .groupBy { member ->
            val slot = member.prayerSlot.name.lowercase()
            "$date:$slot:${member.markerMinute}"
        }
        .toSortedMap()
        .map { (key, members) ->
            ResearchEventGroup(
                key = key,
                prayerLabel = members.first().prayerSlot.displayName,
                markerMinute = members.first().markerMinute,
                occurrenceIds = members.map { it.occurrenceId },
                deedIds = members.map { it.deed.id },
                completionIds = members.map { it.completionId },
            )
        }
    val eventGroupByKey = eventGroups.associateBy(ResearchEventGroup::key)
    val eventRaw = eventGroups.map { group ->
        val members = eventMembers.filter { member ->
            val key = "$date:${member.prayerSlot.name.lowercase()}:${member.markerMinute}"
            key == group.key
        }
        ResearchIntervalDraft(
            id = "event-proxy.${group.key.replace(':', '.')}",
            deedId = null,
            start = group.markerMinute,
            end = group.markerMinute,
            marker = group.markerMinute,
            visual = AlmanacIntervalVisual.EventTrigger,
            truthClass = AlmanacIntervalTruthClass.ScheduleContextProxy,
            accessibility = buildString {
                append("${group.memberCount} amalan berkongsi proksi selepas solat pada ")
                append(formatMinute(group.markerMinute)).append(". ")
                append(members.joinToString(" dan ") { it.deed.title }).append(". ")
                append("Satu glyph peristiwa neutral; waktu masuk JAKIM bukan bukti solat selesai.")
            },
            timingLabel = "${group.memberCount} item selepas ${group.prayerLabel} · ${formatMinute(group.markerMinute)} proksi",
            groupKey = group.key,
            memberIds = group.occurrenceIds,
            memberTitles = members.map { it.deed.title },
            fixedLane = EVENT_TAB_LANE,
        )
    }
    val semanticDrafts = raw.filter { it.visual != AlmanacIntervalVisual.EventTrigger }
    val lanePlan = planResearchSemanticLanes(
        semanticDrafts.map { ResearchLaneCandidate(it.id, it.start, it.end) },
        composition,
    )
    val datePrefix = "$date."
    fun occurrenceIdFor(draft: ResearchIntervalDraft) = datePrefix + draft.id
    val semanticOccurrences = semanticDrafts.map { draft ->
        ResearchTemporalOccurrence(
            id = occurrenceIdFor(draft),
            deedId = requireNotNull(draft.deedId),
            applicability = ResearchApplicability("SCHEDULE_ZONE_LOCAL_DATE", date.toString()),
            validityWindow = if (draft.focusRegion == null && draft.trigger == null) {
                ResearchValidityWindow(
                    startMinute = normalizeMinute(draft.start),
                    endMinute = normalizeMinute(draft.end),
                    boundaryPolicy = draft.boundaryPolicy,
                )
            } else null,
            trigger = draft.trigger,
            focusRegion = draft.focusRegion,
            sourceExpression = catalog.deeds.first { it.id == draft.deedId }.sourceTimePhrase,
            truthClass = draft.truthClass,
            completionId = "completion.$date.${draft.deedId}.${draft.id.substringAfterLast('.')}",
        )
    }
    val eventOccurrences = eventMembers.map { member ->
        ResearchTemporalOccurrence(
            id = member.occurrenceId,
            deedId = member.deed.id,
            applicability = ResearchApplicability("SCHEDULE_ZONE_LOCAL_DATE", date.toString()),
            trigger = ResearchTrigger("AFTER", "PRAYER_COMPLETED", member.markerMinute),
            sourceExpression = member.deed.sourceTimePhrase,
            truthClass = AlmanacIntervalTruthClass.ScheduleContextProxy,
            completionId = member.completionId,
            proxyGroupKey = "$date:${member.prayerSlot.name.lowercase()}:${member.markerMinute}",
        )
    }
    val occurrenceByRawId = semanticOccurrences.associateBy { it.id.removePrefix(datePrefix) }
    val occurrenceCoverage = semanticDrafts.associate { draft ->
        occurrenceIdFor(draft) to listOf(
            AlmanacCoverageSegment(normalizeMinute(draft.start), normalizeMinute(draft.end)),
        )
    }
    val semanticPresentations = semanticDrafts.associate { draft ->
        val occurrence = requireNotNull(semanticOccurrences.firstOrNull { it.id == occurrenceIdFor(draft) })
        occurrence.id to ResearchOccurrencePresentation(
            timingLabel = draft.timingLabel,
            visual = draft.visual,
            truthClass = draft.truthClass,
            applicability = occurrence.applicability,
        )
    }
    val eventPresentations = eventOccurrences.associate { occurrence ->
        val member = requireNotNull(eventMembers.firstOrNull { it.occurrenceId == occurrence.id })
        occurrence.id to ResearchOccurrencePresentation(
            timingLabel = "${member.deed.sourceTimePhrase} · ${formatMinute(member.markerMinute)} proksi",
            visual = AlmanacIntervalVisual.EventTrigger,
            truthClass = occurrence.truthClass,
            applicability = occurrence.applicability,
        )
    }
    val occurrencePresentations = semanticPresentations + eventPresentations
    val intervals = mutableListOf<AlmanacInterval>()
    eventRaw.forEach { draft ->
        val group = requireNotNull(eventGroupByKey[draft.groupKey])
        intervals += draft.toInterval(
            id = draft.id,
            lane = EVENT_TAB_LANE,
            emphasized = draft.memberIds.any { memberId ->
                occurrenceByRawId[memberId]?.deedId == selectedDeedId || eventOccurrences.firstOrNull { it.id == memberId }?.deedId == selectedDeedId
            },
            memberCount = group.memberCount,
            memberCoverage = group.occurrenceIds.associateWith { occurrenceId ->
                listOf(AlmanacCoverageSegment(group.markerMinute, group.markerMinute))
            },
        )
    }
    semanticDrafts.forEach { draft ->
        val lane = lanePlan.assignments[draft.id] ?: return@forEach
        val occurrence = requireNotNull(occurrenceByRawId[occurrenceIdFor(draft).removePrefix(datePrefix)])
        intervals += draft.toInterval(
            id = occurrence.id,
            lane = lane,
            emphasized = draft.deedId == selectedDeedId,
            memberCount = 1,
            memberIds = listOf(occurrence.id),
            memberTitles = listOf(catalog.deeds.first { it.id == draft.deedId }.title),
        )
    }
    val overflowGroup = lanePlan.overflow
    val overflowIntervals = if (overflowGroup == null) emptyList() else {
        val overflowOccurrences = overflowGroup.memberIds.mapNotNull { occurrenceByRawId[it] }
        val coverage = overflowGroup.coverageSegments.map { AlmanacCoverageSegment(it.startMinute, it.endMinute) }
        val first = coverage.firstOrNull()
        if (first == null) emptyList() else listOf(
            AlmanacInterval(
                id = "$date.overflow.semantic",
                startMinute = first.startMinute,
                endMinute = first.endMinute,
                markerMinute = first.startMinute,
                emphasized = overflowOccurrences.any { it.deedId == selectedDeedId },
                lane = lanePlan.laneBudget,
                visual = AlmanacIntervalVisual.SearchEnvelope,
                truthClass = AlmanacIntervalTruthClass.ProvisionalResearch,
                deedId = null,
                accessibilityText = "${overflowOccurrences.size} jalur semantic dikumpulkan pada union liputan sebenar tanpa mencipta tempoh umum. Ahli penuh kekal dalam concordance.",
                title = null,
                timingLabel = overflowGroup.coverageSegments.joinToString(" · ") { "${formatMinute(it.startMinute)}–${formatMinute(it.endMinute)}" },
                groupKey = overflowGroup.id,
                memberCount = overflowOccurrences.size,
                memberIds = overflowOccurrences.map(ResearchTemporalOccurrence::id),
                memberTitles = overflowOccurrences.map { occurrence -> catalog.deeds.first { it.id == occurrence.deedId }.title },
                coverageSegments = coverage,
                isOverflowAggregate = true,
                memberCoverage = overflowOccurrences.associate { occurrence ->
                    occurrence.id to occurrenceCoverage[occurrence.id].orEmpty()
                },
            ),
        )
    }
    intervals += overflowIntervals
    val intervalOccurrences = semanticOccurrences + eventOccurrences
    val applicableIds = buildSet {
        addAll(catalog.dailyDeeds.map(ResearchDeed::id))
        addAll(dayContexts.map { it.deed.id })
        addAll(intervalOccurrences.map { it.deedId })
    }
    val visibleDeeds = catalog.deeds.filter { it.id in applicableIds }
    return ResearchTimelinePlan(
        intervals = intervals,
        personalEvents = personalEvents,
        dayContexts = dayContexts,
        visibleTodayDeeds = visibleDeeds,
        occurrences = intervalOccurrences + personalEvents.map(ResearchPersonalEvent::occurrence) +
            dayContexts.map(ResearchDayContext::occurrence),
        eventGroups = eventGroups,
        overflowGroups = listOfNotNull(overflowGroup),
        lunarContext = lunarContext,
        occurrenceCoverage = occurrenceCoverage,
        occurrencePresentations = occurrencePresentations,
    )
}

internal fun researchTimelinePlan(
    catalog: ResearchCatalog,
    schedule: JakimAlmanacSchedule,
    lunarContext: ResearchLunarDateContext,
    composition: AlmanacFontComposition = AlmanacFontComposition.Regular,
): ResearchTimelinePlan = researchTimelinePlan(catalog, schedule, null, lunarContext, composition)

internal fun researchWheelIntervals(
    catalog: ResearchCatalog,
    schedule: JakimAlmanacSchedule,
    selectedDeedId: String? = null,
    lunarContext: ResearchLunarDateContext = ResearchLunarDateContext.missing(),
    composition: AlmanacFontComposition = AlmanacFontComposition.Regular,
): List<AlmanacInterval> = researchTimelinePlan(catalog, schedule, selectedDeedId, lunarContext, composition).intervals

private fun estimate(
    deed: ResearchDeed,
    occurrence: String,
    start: Int,
    end: Int,
    marker: Int,
    note: String,
    visual: AlmanacIntervalVisual = AlmanacIntervalVisual.NamedPeriodEstimate,
    truthClass: AlmanacIntervalTruthClass = AlmanacIntervalTruthClass.ProvisionalResearch,
    boundaryPolicy: String = "UNREVIEWED_NAMED_PERIOD_PROTOTYPE",
    focusRegion: ResearchFocusRegion? = null,
) = ResearchIntervalDraft(
    id = "${deed.id}.$occurrence",
    deedId = deed.id,
    start = start,
    end = end,
    marker = marker,
    visual = visual,
    truthClass = truthClass,
    boundaryPolicy = boundaryPolicy,
    focusRegion = focusRegion,
    timingLabel = when (visual) {
        AlmanacIntervalVisual.SearchEnvelope -> "${formatMinute(start)} hingga ${formatMinute(end)} · kawasan carian"
        AlmanacIntervalVisual.NightSegmentEstimate -> "${formatMinute(start)} hingga ${formatMinute(end)} · kiraan malam"
        else -> "${formatMinute(start)} hingga ${formatMinute(end)} · anggaran"
    },
    accessibility = buildString {
        append("${deed.title}. Frasa waktu sumber: ${deed.sourceTimePhrase} ")
        append(
            when (visual) {
                AlmanacIntervalVisual.NamedPeriodEstimate -> "Jenis visual: jalur tempoh bernama penuh dengan penanda kosong. "
                AlmanacIntervalVisual.NightSegmentEstimate -> "Jenis visual: jalur segmen malam berjalur. "
                AlmanacIntervalVisual.SearchEnvelope -> "Jenis visual: sampul carian bertanda hujung. "
                AlmanacIntervalVisual.PublishedWindow -> "Jenis visual: jalur diterbitkan. "
                AlmanacIntervalVisual.EventTrigger -> "Jenis visual: tab peristiwa kosong. "
            },
        )
        append("Kelas pemetaan: ${truthClass.name.uppercase()}. ")
        append(note)
    },
)

private data class ResearchIntervalDraft(
    val id: String,
    val deedId: String?,
    val start: Int,
    val end: Int,
    val marker: Int,
    val visual: AlmanacIntervalVisual,
    val truthClass: AlmanacIntervalTruthClass,
    val accessibility: String,
    val timingLabel: String,
    val boundaryPolicy: String = "NOT_APPLICABLE",
    val trigger: ResearchTrigger? = null,
    val focusRegion: ResearchFocusRegion? = null,
    val fixedLane: Int? = null,
    val groupKey: String? = null,
    val memberIds: List<String> = emptyList(),
    val memberTitles: List<String> = emptyList(),
)

private data class ResearchEventMemberDraft(
    val deed: ResearchDeed,
    val prayerSlot: JakimAlmanacSlot,
    val markerMinute: Int,
    val occurrenceId: String,
    val completionId: String,
)

private fun ResearchIntervalDraft.toInterval(
    id: String,
    lane: Int,
    emphasized: Boolean,
    memberCount: Int,
    memberIds: List<String> = this.memberIds,
    memberTitles: List<String> = this.memberTitles,
    memberCoverage: Map<String, List<AlmanacCoverageSegment>> = emptyMap(),
) = AlmanacInterval(
    id = id,
    startMinute = normalizeMinute(start),
    endMinute = normalizeMinute(end),
    markerMinute = normalizeMinute(marker),
    emphasized = emphasized,
    lane = fixedLane ?: lane,
    visual = visual,
    truthClass = truthClass,
    deedId = deedId,
    accessibilityText = accessibility,
    title = null,
    timingLabel = timingLabel,
    groupKey = groupKey,
    memberCount = memberCount,
    memberIds = memberIds,
    memberTitles = memberTitles,
    memberCoverage = memberCoverage,
)

private const val EVENT_TAB_LANE = 0
