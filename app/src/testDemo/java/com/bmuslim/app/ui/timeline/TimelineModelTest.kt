package com.bmuslim.app.ui.timeline

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.bmuslim.app.ui.almanac.AlmanacVisualRole

class TimelineModelTest {
    private val deeds = sampleDeeds()

    @Test
    fun demoMapperTargetsSharedContractWithoutMovingFixtureOwnership() {
        val model = demoAlmanacModel(deeds, 15 * 60)
        assertEquals("demo-stable-fixture", model.modelKey)
        assertEquals(5, model.points.count { it.role == AlmanacVisualRole.Primary })
        assertEquals(deeds.count { it.ruling != Ruling.Fardh }, model.intervals.size)
        assertEquals(5, model.minuteStep)
        assertTrue(model.points.none { it.title.contains("Khamis") })
        assertEquals(6 * 60, model.sunriseMinute)
    }

    @Test
    fun demoIntervalLanesUseOriginalOverlapAwareAssignment() {
        val base = deeds.first { it.id == "zikir-pagi" }
        val overlapping = listOf(
            base.copy(id = "a", start = LocalTime.of(6, 0), end = LocalTime.of(8, 0)),
            base.copy(id = "b", start = LocalTime.of(7, 0), end = LocalTime.of(9, 0)),
            base.copy(id = "c", start = LocalTime.of(9, 0), end = LocalTime.of(10, 0)),
        )
        assertEquals(mapOf("a" to 0, "b" to 1, "c" to 0), assignDemoDeedLanes(overlapping))
    }

    @Test
    fun activeDeedsArePriorityOrderedWhenWindowsOverlap() {
        val active = activeDeedsAt(deeds, 5 * 60 + 59)

        assertEquals(listOf("subuh", "zikir-pagi"), active.map { it.id })
    }

    @Test
    fun inactiveMomentReturnsNoActiveDeedsAndTheRealNextDeed() {
        assertTrue(activeDeedsAt(deeds, 15 * 60).isEmpty())
        assertEquals("asar", nextDeedAt(deeds, 15 * 60).id)
    }

    @Test
    fun obligatoryContextPrefersActivePrayerOtherwiseNextPrayer() {
        assertEquals("maghrib", obligatoryPrayerContextAt(deeds, 19 * 60 + 45).id)
        assertEquals("isyak", obligatoryPrayerContextAt(deeds, 19 * 60 + 50).id)
    }

    @Test
    fun contextualDeedNeverDuplicatesObligatoryPrayer() {
        assertEquals("zikir-petang", contextualDeedAt(deeds, 15 * 60).id)
        assertEquals("zikir-pagi", contextualDeedAt(deeds, 6 * 60 + 20).id)
        assertTrue(contextualDeedAt(deeds, 15 * 60).ruling != Ruling.Fardh)
    }

    @Test
    fun deedWindowCanCrossMidnight() {
        val overnight = deeds.first().copy(
            id = "overnight",
            start = LocalTime.of(22, 0),
            end = LocalTime.of(2, 0),
        )

        assertTrue(overnight.isActiveAt(23 * 60))
        assertTrue(overnight.isActiveAt(60))
    }

    @Test
    fun devotionalReadoutChangesOwnershipAtExactOverlapBoundaries() {
        assertEquals(null, wheelReadoutAt(deeds, 5 * 60 + 58, false))

        val overlapStart = wheelReadoutAt(deeds, 5 * 60 + 59, false)!!
        assertEquals("subuh", overlapStart.prayerId)
        assertEquals("zikir-pagi", overlapStart.contextualId)
        assertEquals("05:59 dipilih · Waktu Subuh", overlapStart.primary)
        assertEquals("Amalan berkaitan · Zikir pagi", overlapStart.secondary)

        assertEquals("subuh", wheelReadoutAt(deeds, 6 * 60 + 19, false)!!.prayerId)
        val afterPrayer = wheelReadoutAt(deeds, 6 * 60 + 20, false)!!
        assertEquals(null, afterPrayer.prayerId)
        assertEquals("06:20 dipilih", afterPrayer.primary)
        assertEquals("Amalan berkaitan · Zikir pagi", afterPrayer.secondary)
    }

    @Test
    fun directManipulationPrunesOnlySecondaryCopy() {
        val settled = wheelReadoutAt(deeds, 16 * 60 + 45, false)!!
        val touching = wheelReadoutAt(deeds, 16 * 60 + 45, true)!!
        assertEquals(settled.primary, touching.primary)
        assertEquals("Amalan berkaitan · Zikir petang", settled.secondary)
        assertEquals("Zikir petang", touching.secondary)
    }

    @Test
    fun sharedDemoReadoutVisiblyOwnsEveryExactSelectedMinute() {
        val model = demoAlmanacModel(deeds, 15 * 60)
        (0 until 24 * 60).forEach { minute ->
            val selection = model.selectionPolicy.select(model.points, minute)
            val readout = model.wheelReadout(selection, minute, false)
            assertTrue(
                "selected minute $minute",
                listOfNotNull(readout.first, readout.second).joinToString(" ").contains(formatTimelineMinute(minute)),
            )
        }
        assertEquals("Leret roda kiri atau kanan untuk pilih waktu", model.copy.wheelHint)
    }

    @Test
    fun everyLockedOverlapKeepsFardhuPrimaryAtItsBoundaries() {
        val overlaps = listOf(
            Triple(13 * 60 + 21, "zohor", "dhuha"),
            Triple(16 * 60 + 45, "asar", "zikir-petang"),
            Triple(19 * 60 + 29, "maghrib", "zikir-petang"),
            Triple(20 * 60 + 42, "isyak", "al-mulk"),
        )
        overlaps.forEach { (minute, prayerId, contextualId) ->
            val readout = wheelReadoutAt(deeds, minute, false)!!
            assertEquals(prayerId, readout.prayerId)
            assertEquals(contextualId, readout.contextualId)
            assertTrue(readout.primary.contains(" · Waktu "))
            assertTrue(readout.secondary!!.startsWith("Amalan berkaitan "))
        }

        assertEquals(null, wheelReadoutAt(deeds, 17 * 60 + 6, false)!!.prayerId)
        assertEquals(null, wheelReadoutAt(deeds, 21 * 60 + 3, false)!!.prayerId)
    }

    @Test
    fun spokenWheelStateNamesActiveFardhuOnlyOnce() {
        val description = wheelStateDescription(deeds, 5 * 60 + 59)
        assertTrue(description.startsWith("Masa dipilih 05:59."))
        assertEquals(1, Regex("Subuh").findAll(description).count())
        assertTrue(description.contains("Amalan berkaitan Zikir pagi"))
    }

    @Test
    fun openActionDoesNotRepeatTheSelectedTimeOwner() {
        val active = activeDeedsAt(deeds, 5 * 60 + 59)
        val description = openActionDescription(active, nextDeedAt(deeds, 5 * 60 + 59))
        assertTrue(description.startsWith("Buka butiran amalan."))
        assertTrue(!description.contains("05:59"))
    }

    @Test
    fun maximumTypePrayerPolicyRetainsAllFiveNames() {
        val prayers = deeds.filter { it.ruling == Ruling.Fardh }
        val glancePolicy = prayerDisplayPolicy(FontComposition.Maximum, 0f)
        assertEquals(PrayerDisplayPolicy.AllRadial, glancePolicy)
        assertEquals(5, prayers.count { prayerLabelVisible(glancePolicy, false, false, false) })

        val detailPolicy = prayerDisplayPolicy(FontComposition.Maximum, 1f)
        assertEquals(PrayerDisplayPolicy.FiveNameKey, detailPolicy)
        assertEquals(listOf("Subuh", "Zohor", "Asar", "Maghrib", "Isyak"), prayerKeyTitles(prayers))
    }
}
