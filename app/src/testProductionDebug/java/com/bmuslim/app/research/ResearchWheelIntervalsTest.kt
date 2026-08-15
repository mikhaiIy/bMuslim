package com.bmuslim.app.research

import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimAlmanacSlot
import com.bmuslim.app.prayer.JakimAlmanacValue
import com.bmuslim.app.prayer.JakimZone
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.TemporalProvenance
import com.bmuslim.app.ui.almanac.AlmanacIntervalVisual
import com.bmuslim.app.ui.almanac.AlmanacIntervalTruthClass
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.memberTitlesAt
import com.bmuslim.app.productionAlmanacModel
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.bmuslim.app.researchOptionAccessibility
import com.bmuslim.app.researchUntimedContextSummary

class ResearchWheelIntervalsTest {
    private val catalog = (ResearchCatalogParser().parse(readCanonicalCatalog()) as ResearchCatalogResult.Ready).catalog

    @Test
    fun ordinaryDayUsesOnlyClockCompatibleBarsAndKeepsThreeLaneBudget() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))

        assertEquals(9, plan.intervals.size)
        assertTrue(plan.intervals.all { it.visual != AlmanacIntervalVisual.PublishedWindow })
        assertEquals(plan.intervals.size, plan.intervals.map { it.id }.distinct().size)
        assertTrue(plan.intervals.all { !it.accessibilityText.isNullOrBlank() })
        assertTrue(plan.intervals.all { it.lane in 0..3 })
        assertTrue(plan.intervals.filter { it.visual != AlmanacIntervalVisual.EventTrigger }.all { it.lane in 1..3 })
        assertTrue(plan.intervals.filter { it.visual == AlmanacIntervalVisual.EventTrigger }.all { it.lane == 0 })
    }

    @Test
    fun postPrayerDeedsShareFiveCountedProxyGlyphsWithIndependentIdentities() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val tabs = plan.intervals.filter { it.visual == AlmanacIntervalVisual.EventTrigger }

        assertEquals(5, tabs.size)
        assertEquals(
            setOf(5 * 60 + 55, 13 * 60 + 20, 16 * 60 + 45, 19 * 60 + 28, 20 * 60 + 42),
            tabs.map { it.markerMinute }.toSet(),
        )
        assertTrue(tabs.all { it.visual == AlmanacIntervalVisual.EventTrigger })
        assertTrue(tabs.all { it.startMinute == it.endMinute })
        assertTrue(tabs.all { it.memberCount == 2 })
        assertTrue(tabs.all { it.memberTitles.size == 2 })
        assertTrue(tabs.all { it.accessibilityText!!.lowercase().contains("berkongsi proksi") })
        assertEquals(10, plan.occurrences.count { it.proxyGroupKey != null })
        assertEquals(10, plan.occurrences.filter { it.proxyGroupKey != null }.map { it.completionId }.distinct().size)
    }

    @Test
    fun personalSleepEventsNeverReceiveClockCoordinates() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val personalIds = plan.personalEvents.map { it.deed.id }.toSet()

        assertEquals(
            setOf("deed.daily.bedtime-three-quls", "deed.daily.witr-before-sleep"),
            personalIds,
        )
        assertFalse(plan.intervals.any { it.deedId in personalIds })
        assertTrue(plan.personalEvents.all { it.accessibilityText.lowercase().contains("tidak diletakkan") })
    }

    @Test
    fun provisionalNamedPeriodsUseTheirOwnVisualAndTruthClass() {
        val estimates = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15))).intervals
            .filter { it.visual == AlmanacIntervalVisual.NamedPeriodEstimate }

        assertEquals(3, estimates.size)
        assertTrue(estimates.all { it.accessibilityText!!.contains("Jalur penuh") })
        assertTrue(estimates.all { it.truthClass == AlmanacIntervalTruthClass.ProvisionalResearch })
        assertTrue(estimates.none { it.deedId == "deed.daily.post-prayer-tasbih" })
    }

    @Test
    fun lastThirdEstimateUsesNightLengthAndStaysProvisional() {
        val interval = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15))).intervals
            .single { it.deedId == "deed.daily.last-third-dua-istighfar" }

        assertEquals(2 * 60 + 26, interval.startMinute)
        assertEquals(5 * 60 + 55, interval.endMinute)
        assertEquals(AlmanacIntervalVisual.NightSegmentEstimate, interval.visual)
        assertEquals(AlmanacIntervalTruthClass.ProvisionalResearch, interval.truthClass)
        assertTrue(interval.accessibilityText!!.contains("prototaip"))
        assertTrue(interval.accessibilityText!!.contains("belum diluluskan"))
    }

    @Test
    fun weekdayDeedsExistOnlyInApplicableDayPlan() {
        val wednesday = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val thursday = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 16)))
        val friday = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 17)))

        assertEquals(setOf("deed.monthly.fast-white-days"), wednesday.dayContexts.map { it.deed.id }.toSet())
        assertFalse(wednesday.visibleTodayDeeds.any { it.id.startsWith("deed.weekly.") })
        assertEquals(setOf("deed.weekly.fast-monday-thursday", "deed.monthly.fast-white-days"), thursday.dayContexts.map { it.deed.id }.toSet())
        assertEquals(setOf("deed.weekly.friday-salawat", "deed.monthly.fast-white-days"), friday.dayContexts.map { it.deed.id }.toSet())
        assertEquals(
            setOf("deed.weekly.friday-salawat", "deed.weekly.friday-final-period-dua"),
            friday.visibleTodayDeeds.filter { it.id.startsWith("deed.weekly.") }.map { it.id }.toSet(),
        )
        assertTrue(friday.intervals.any { it.deedId == "deed.weekly.friday-final-period-dua" })
        assertFalse(friday.intervals.any { it.deedId == "deed.weekly.friday-salawat" })
    }

    @Test
    fun fridaySearchEnvelopeHasASeparateFocusRegionAndTerminalMarker() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 17)))
        val interval = plan.intervals.single { it.deedId == "deed.weekly.friday-final-period-dua" }
        val occurrence = plan.occurrences.single { it.id == interval.id }

        assertEquals(AlmanacIntervalVisual.SearchEnvelope, interval.visual)
        assertEquals(AlmanacIntervalTruthClass.Unresolved, interval.truthClass)
        assertEquals(19 * 60 + 28, interval.markerMinute)
        assertEquals(interval.markerMinute, occurrence.focusRegion?.terminalMinute)
        assertEquals(null, occurrence.validityWindow)
    }

    @Test
    fun runtimeOccurrenceContractKeepsTemporalDimensionsOrthogonal() {
        val thursday = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 16)))
        val prayer = thursday.occurrences.first { it.deedId == "deed.daily.post-prayer-tasbih" }
        val bedtime = thursday.occurrences.single { it.deedId == "deed.daily.bedtime-three-quls" }
        val fasting = thursday.occurrences.single { it.deedId == "deed.weekly.fast-monday-thursday" }

        assertEquals("SCHEDULE_ZONE_LOCAL_DATE", prayer.applicability.basis)
        assertEquals("AFTER", prayer.trigger?.relation)
        assertEquals(null, prayer.validityWindow)
        assertEquals(AlmanacIntervalTruthClass.ScheduleContextProxy, prayer.truthClass)
        assertEquals("AT", bedtime.trigger?.relation)
        assertEquals(null, bedtime.trigger?.scheduleAnchorMinute)
        assertEquals(AlmanacIntervalTruthClass.EventRequired, bedtime.truthClass)
        assertEquals("SCHEDULE_ZONE_LOCAL_WEEKDAY", fasting.applicability.basis)
        assertEquals(null, fasting.validityWindow)
        assertEquals(null, fasting.trigger)
        assertTrue(thursday.occurrences.all { it.sourceExpression.isNotBlank() })
    }

    @Test
    fun dayContextsAreRibbonsNotFakeFullDayIntervals() {
        val dates = listOf(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 16), LocalDate.of(2026, 7, 17))
        val plans = dates.map { researchTimelinePlan(catalog, schedule(it)) }
        val dayContextIds = plans.flatMap { it.dayContexts }.map { it.deed.id }.toSet()

        assertEquals(setOf("deed.weekly.fast-monday-thursday", "deed.weekly.friday-salawat", "deed.monthly.fast-white-days"), dayContextIds)
        assertTrue(plans.flatMap { it.intervals }.none { it.startMinute == 0 && it.endMinute == 1439 })
    }

    @Test
    fun allElevenCandidatesHaveOneOfTheAgreedTemporalRepresentations() {
        val plans = listOf(
            LocalDate.of(2026, 7, 13),
            LocalDate.of(2026, 7, 16),
            LocalDate.of(2026, 7, 17),
        ).map { researchTimelinePlan(catalog, schedule(it)) }
        val represented = buildSet {
            addAll(plans.flatMap { it.intervals }.mapNotNull { it.deedId })
            addAll(plans.flatMap { it.eventGroups }.flatMap { it.deedIds })
            addAll(plans.flatMap { it.personalEvents }.map { it.deed.id })
            addAll(plans.flatMap { it.dayContexts }.map { it.deed.id })
        }

        assertEquals(catalog.deeds.map(ResearchDeed::id).toSet(), represented)
    }

    @Test
    fun selectionEmphasizesEveryOccurrenceOfSharedDeedIdentity() {
        val selectedId = "deed.daily.post-prayer-tasbih"
        val intervals = researchWheelIntervals(catalog, schedule(LocalDate.of(2026, 7, 15)), selectedId)

        assertEquals(5, intervals.count { it.visual == AlmanacIntervalVisual.EventTrigger && it.emphasized })
        assertTrue(intervals.filter { it.visual != AlmanacIntervalVisual.EventTrigger }.none { it.emphasized })
    }

    @Test
    fun wheelPointerOwnsTheContextualDeedAndAllVisibleCopy() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val exactPrayer = researchPointerSelection(catalog, plan, 13 * 60 + 20)!!
        val afterAllWindows = researchPointerSelection(catalog, plan, 22 * 60)

        assertEquals("deed.daily.post-prayer-tasbih", exactPrayer.deed.id)
        assertTrue(exactPrayer.intersectsPointer)
        assertEquals(AlmanacIntervalVisual.EventTrigger, exactPrayer.interval.visual)
        assertTrue(exactPrayer.interval.timingLabel!!.contains("Zohor"))
        assertTrue(exactPrayer.interval.memberIds.contains(exactPrayer.occurrence.id))

        assertEquals(null, afterAllWindows)
    }

    @Test
    fun overlappingResearchBarsExposeStableRelatedOrderWithoutRankingClaim() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val pointer = researchPointerSelection(catalog, plan, 5 * 60 + 55)!!

        assertEquals(3, pointer.intersectingIntervals.size)
        assertEquals(pointer.intersectingIntervals.map { it.lane }.sorted(), pointer.intersectingIntervals.map { it.lane })
        assertTrue(pointer.intersectingIntervals.any { it.visual == AlmanacIntervalVisual.EventTrigger })
        assertEquals(pointer.intersectingIntervals.first().id, pointer.interval.id)
    }

    @Test
    fun overlapChooserRepresentsEverySemanticIntervalAndVisualToleranceDoesNotLeak() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val options = researchPointerOptions(catalog, plan, 5 * 60 + 55)
        assertEquals(4, options.size)
        assertEquals(options.map { it.occurrence.id }.distinct().size, options.size)
        assertTrue(options.all { it.deed.title.isNotBlank() && !it.interval.timingLabel.isNullOrBlank() })
        assertTrue(options.all { researchOptionAccessibility(it, 5 * 60 + 55).contains("Buka butiran draf") })

        val insideVisualToleranceOnly = researchPointerOptions(catalog, plan, 5 * 60 + 50)
        assertFalse(insideVisualToleranceOnly.any { it.deed.id == "deed.daily.post-prayer-tasbih" })
    }

    @Test
    fun afterTimedScheduleRetainsPersonalAndDayContextWithoutCallingThemAbsent() {
        val thursday = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 16)))
        val summary = researchUntimedContextSummary(thursday)
        assertTrue(summary.contains("2 peristiwa peribadi kekal tersedia"))
        assertTrue(summary.contains("2 konteks hari kekal tersedia"))
        assertFalse(summary.contains("Tiada amalan"))
    }

    @Test
    fun missingLunarContextLeavesWhiteDaysUnresolvedWithoutClockGeometry() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val white = plan.dayContexts.single { it.deed.id == "deed.monthly.fast-white-days" }

        assertFalse(white.resolved)
        assertEquals(AlmanacIntervalTruthClass.Unresolved, white.occurrence.truthClass)
        assertTrue(plan.intervals.none { it.deedId == "deed.monthly.fast-white-days" })
        assertEquals(ResearchLunarDateContextState.Missing, plan.lunarContext.state)
    }

    @Test
    fun mismatchedInjectedLunarContextFailsClosedWithoutAClockInterval() {
        val date = LocalDate.of(2026, 7, 15)
        val plan = researchTimelinePlan(
            catalog,
            schedule(date),
            ResearchLunarDateContext.ready(date.minusDays(1), "Shawwal", 13, "malaysia-policy", "wrong-date"),
        )
        val white = plan.dayContexts.single { it.deed.id == "deed.monthly.fast-white-days" }

        assertEquals(ResearchLunarDateContextState.Mismatched, plan.lunarContext.resolutionFor(date))
        assertFalse(white.resolved)
        assertTrue(plan.intervals.none { it.deedId == "deed.monthly.fast-white-days" })
    }

    @Test
    fun injectedMondayThirteenAndFridayFourteenExposeEveryApplicableDayRibbon() {
        val monday = LocalDate.of(2026, 7, 13)
        val friday = LocalDate.of(2026, 7, 17)
        val mondayPlan = researchTimelinePlan(
            catalog,
            schedule(monday),
            ResearchLunarDateContext.ready(monday, "Shawwal", 13, "malaysia-policy", "fp-monday"),
        )
        val fridayPlan = researchTimelinePlan(
            catalog,
            schedule(friday),
            ResearchLunarDateContext.ready(friday, "Shawwal", 14, "malaysia-policy", "fp-friday"),
        )

        assertEquals(
            setOf("deed.weekly.fast-monday-thursday", "deed.monthly.fast-white-days"),
            mondayPlan.dayContexts.map { it.deed.id }.toSet(),
        )
        assertTrue(mondayPlan.dayContexts.all(ResearchDayContext::resolved))
        assertEquals(
            setOf("deed.weekly.friday-salawat", "deed.monthly.fast-white-days"),
            fridayPlan.dayContexts.map { it.deed.id }.toSet(),
        )
        assertTrue(fridayPlan.dayContexts.all(ResearchDayContext::resolved))
    }

    @Test
    fun dhuAlHijjahThirteenFailsClosedEvenWithInjectedCalendarContext() {
        val date = LocalDate.of(2026, 7, 13)
        val plan = researchTimelinePlan(
            catalog,
            schedule(date),
            ResearchLunarDateContext.ready(date, "Dhu al-Hijjah", 13, "malaysia-policy", "fp-blocked"),
        )
        val white = plan.dayContexts.single { it.deed.id == "deed.monthly.fast-white-days" }

        assertEquals(ResearchLunarDateContextState.Blocked, plan.lunarContext.resolutionFor(date))
        assertFalse(white.resolved)
        assertTrue(white.dayLabel.contains("disekat"))
        assertTrue(plan.intervals.none { it.deedId == "deed.monthly.fast-white-days" })
    }

    @Test
    fun zulhijjahAndCommonDhuAlHijjahSpellingsAlwaysBlockLunarDayThirteen() {
        val date = LocalDate.of(2026, 7, 13)
        listOf(
            "Zulhijjah",
            "Zul Hijjah",
            "Zul-Hijjah",
            "Dhu al-Hijjah",
            "Dhul Hijjah",
            "Dhu'l-Hijjah",
            "Dzul Hijjah",
        ).forEach { month ->
            val context = ResearchLunarDateContext.ready(date, month, 13, "malaysia-policy", "fp-$month")
            assertEquals(
                "day 13 must block for $month",
                ResearchLunarDateContextState.Blocked,
                context.resolutionFor(date),
            )
        }
    }

    @Test
    fun neutralConcordanceKeepsEventMembersAndSemanticWindowsIndependent() {
        val plan = researchTimelinePlan(catalog, schedule(LocalDate.of(2026, 7, 15)))
        val concordance = researchPointerConcordance(catalog, plan, 5 * 60 + 55)

        assertEquals(listOf("PERISTIWA", "TEMPOH", "HARI_INI", "PERISTIWA_PERIBADI"), concordance.sections.map { it.section.name })
        assertEquals(2, concordance.eventItems.size)
        assertEquals(2, concordance.semanticItems.size)
        assertEquals(4, concordance.exactItemCount)
        assertEquals(2, concordance.eventItems.map { it.occurrence.completionId }.distinct().size)
        assertTrue(concordance.dayItems.isNotEmpty())
        assertEquals(0, researchPointerOptions(catalog, plan, 5 * 60 + 54).count { it.interval.visual == AlmanacIntervalVisual.EventTrigger })
    }

    @Test
    fun semanticLanePlannerReturnsTruthfulOverflowAtEveryTypeScale() {
        val candidates = listOf(
            ResearchLaneCandidate("a", 100, 300),
            ResearchLaneCandidate("b", 150, 350),
            ResearchLaneCandidate("c", 200, 400),
            ResearchLaneCandidate("d", 250, 450),
        )
        listOf(
            AlmanacFontComposition.Regular to 3,
            AlmanacFontComposition.Large to 2,
            AlmanacFontComposition.Maximum to 1,
        ).forEach { (composition, budget) ->
            val plan = planResearchSemanticLanes(candidates, composition)
            assertEquals(budget, plan.laneBudget)
            assertTrue(plan.overflow != null)
            assertTrue(plan.overflow!!.coverageSegments.all { it.startMinute >= 0 && it.endMinute < 1440 })
            assertTrue(plan.assignments.values.all { it in 1..budget })
        }
    }

    @Test
    fun maximumOverflowResolvesDisjointMembersForCtaWheelAndConcordance() {
        val date = LocalDate.of(2026, 7, 15)
        val schedule = schedule(date)
        val plan = researchTimelinePlan(catalog, schedule, composition = AlmanacFontComposition.Maximum)
        val aggregate = plan.intervals.single { it.isOverflowAggregate }

        assertTrue(aggregate.memberCoverage.size > 1)
        assertEquals(1, aggregate.memberTitlesAt(400).size)
        assertEquals(1, aggregate.memberTitlesAt(500).size)
        assertTrue(aggregate.memberTitlesAt(400) != aggregate.memberTitlesAt(500))

        val model = productionAlmanacModel(
            schedule = schedule,
            zone = JakimZone("Wilayah Persekutuan", "WLY01", "Kuala Lumpur"),
            actualNowMinute = 400,
            researchIntervals = plan.intervals,
        )
        assertEquals("Buka concordance · 06:40", model.copy.openLabel(400))
        assertTrue(model.copy.openDescription(400).contains("Baca tiga surah perlindungan"))
        assertFalse(model.copy.openDescription(400).contains("Solat Dhuha"))

        val morning = researchPointerConcordance(catalog, plan, 400).semanticItems.single()
        assertEquals("deed.daily.three-quls-morning-evening", morning.deed.id)
        assertEquals("05:55 hingga 07:15 · anggaran", morning.timingLabel)
        assertEquals(AlmanacIntervalVisual.NamedPeriodEstimate, morning.visual)
        assertEquals(AlmanacIntervalTruthClass.ProvisionalResearch, morning.truthClass)
        assertEquals("SCHEDULE_ZONE_LOCAL_DATE", morning.applicability.basis)

        val duha = researchPointerConcordance(catalog, plan, 500).semanticItems.single()
        assertEquals("deed.daily.duha-two-rakah", duha.deed.id)
        assertEquals("07:40 hingga 13:20 · anggaran", duha.timingLabel)
        assertEquals(AlmanacIntervalVisual.NamedPeriodEstimate, duha.visual)
        assertEquals(AlmanacIntervalTruthClass.ProvisionalResearch, duha.truthClass)
        assertEquals("SCHEDULE_ZONE_LOCAL_DATE", duha.applicability.basis)
    }

    private fun schedule(date: LocalDate) = JakimAlmanacSchedule(
        exactKey = PrayerDayKey("WLY01", date),
        scheduleZoneId = "Asia/Kuala_Lumpur",
        values = listOf(
            value(date, JakimAlmanacSlot.IMSAK, 5, 45),
            value(date, JakimAlmanacSlot.FAJR, 5, 55),
            value(date, JakimAlmanacSlot.SYURUK, 7, 15),
            value(date, JakimAlmanacSlot.DHUHA, 7, 40),
            value(date, JakimAlmanacSlot.DHUHR, 13, 20),
            value(date, JakimAlmanacSlot.ASR, 16, 45),
            value(date, JakimAlmanacSlot.MAGHRIB, 19, 28),
            value(date, JakimAlmanacSlot.ISHA, 20, 42),
        ),
        provenance = TemporalProvenance("jakim-e-solat", "v1", "https://example.test"),
    )

    private fun value(date: LocalDate, slot: JakimAlmanacSlot, hour: Int, minute: Int) =
        JakimAlmanacValue(slot, date, LocalTime.of(hour, minute))

    private fun readCanonicalCatalog(): String {
        val candidates = listOf(
            File("datasets/amuslim/starter-v0.2/catalog.draft.json"),
            File("../datasets/amuslim/starter-v0.2/catalog.draft.json"),
        )
        return candidates.firstOrNull(File::isFile)?.readText()
            ?: error("Canonical aMuslim starter catalog was not found")
    }
}
