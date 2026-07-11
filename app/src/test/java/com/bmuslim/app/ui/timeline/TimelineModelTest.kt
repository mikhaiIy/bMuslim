package com.bmuslim.app.ui.timeline

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineModelTest {
    private val deeds = sampleDeeds()

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
}
