package com.bmuslim.app

import com.bmuslim.app.truth.TruthBlocker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionEntryTest {
    @Test
    fun entryAlwaysResolvesToTheLockedNotReadyState() {
        val resolution = productionTruthResolution()

        assertFalse(resolution.isReady)
        assertNull(resolution.readyPresentation)
        assertEquals(
            listOf(
                TruthBlocker.ContentMissing,
                TruthBlocker.TemporalProviderUnconfigured,
                TruthBlocker.RankingMissing,
            ),
            resolution.blockers.toList(),
        )
    }

    @Test
    fun shellCopyIsBmFirstAndContainsNoSampleTruth() {
        val renderedCopy = "$NotReadyHeading $NotReadyExplanation"
        val prohibited = listOf(
            "WLY01",
            "Khamis, 2 Julai 2026",
            "17 Muharram",
            "AMALAN UTAMA SEKARANG",
            "Sahih",
            "Subuh",
            "Maghrib",
            "Demo",
        )

        assertEquals("Panduan belum tersedia", NotReadyHeading)
        assertTrue(NotReadyExplanation.startsWith("Panduan yang disahkan"))
        prohibited.forEach { assertFalse("prohibited shell copy: $it", renderedCopy.contains(it)) }
    }
}
