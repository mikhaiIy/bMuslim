package com.bmuslim.app

import com.bmuslim.app.prayer.JakimAlmanacCategory
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimAlmanacSlot
import com.bmuslim.app.prayer.JakimAlmanacValue
import com.bmuslim.app.prayer.JakimZone
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.TemporalProvenance
import com.bmuslim.app.ui.almanac.AlmanacVisualRole
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacIntervalTruthClass
import com.bmuslim.app.ui.almanac.AlmanacIntervalVisual
import java.time.LocalDate
import java.time.LocalTime
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductionAlmanacModelTest {
    private val date = LocalDate.of(2026, 7, 13)
    private val schedule = JakimAlmanacSchedule(
        PrayerDayKey("WLY01", date), "Asia/Kuala_Lumpur",
        listOf(
            value(JakimAlmanacSlot.IMSAK, 5, 45), value(JakimAlmanacSlot.FAJR, 5, 55),
            value(JakimAlmanacSlot.SYURUK, 7, 15), value(JakimAlmanacSlot.DHUHA, 7, 40),
            value(JakimAlmanacSlot.DHUHR, 13, 20), value(JakimAlmanacSlot.ASR, 16, 45),
            value(JakimAlmanacSlot.MAGHRIB, 19, 28), value(JakimAlmanacSlot.ISHA, 20, 42),
        ), TemporalProvenance("jakim-e-solat", "v1", "https://example.test"),
    )
    private val zone = JakimZone("Wilayah Persekutuan", "WLY01", "Kuala Lumpur")

    @Test fun mapperEmitsCanonicalEightPointsWithFiveThreeSplitAndNoIntervals() {
        val model = productionAlmanacModel(schedule, zone, 600)
        assertEquals(8, model.points.size)
        assertEquals(5, model.points.count { it.role == AlmanacVisualRole.Primary })
        assertEquals(3, model.points.count { it.role == AlmanacVisualRole.Secondary })
        assertTrue(model.intervals.isEmpty())
        assertEquals("WLY01:2026-07-13:foreground=0", model.modelKey)
        assertEquals(1, model.minuteStep)
        assertEquals(7 * 60 + 15, model.sunriseMinute)
        assertEquals(19 * 60 + 28, model.sunsetMinute)
    }

    @Test fun productionUsesPhoneClockZoneAndAlignsUpdatesToMinuteBoundary() {
        val phoneClock = Clock.fixed(
            Instant.parse("2026-07-13T02:34:45Z"),
            ZoneId.of("Asia/Tokyo"),
        )

        assertEquals(11 * 60 + 34, currentPhoneMinute(phoneClock))
        assertEquals(15_000L, millisUntilNextPhoneMinute(phoneClock))
    }

    @Test fun foregroundGenerationForcesSharedShellBackToLiveTime() {
        val first = productionAlmanacModel(schedule, zone, 600, foregroundGeneration = 1)
        val resumed = productionAlmanacModel(schedule, zone, 615, foregroundGeneration = 2)

        assertTrue(first.modelKey != resumed.modelKey)
        assertEquals(615, resumed.initialMinute)
        assertEquals(615, resumed.actualNowMinute)
    }

    @Test fun productionSelectionIsPointBasedAndNeverWrapsAfterIsyak() {
        val model = productionAlmanacModel(schedule, zone, 600)
        val dawn = model.selectionPolicy.select(model.points, 360)
        assertEquals("fajr", dawn.previous?.id)
        assertEquals("syuruk", dawn.next?.id)
        val after = model.selectionPolicy.select(model.points, 1380)
        assertEquals("isha", after.previous?.id)
        assertNull(after.next)
        assertTrue(after.dayComplete)
        assertEquals("23:00 dipilih" to "Jadual hari ini selesai", productionPointReadout(after, 23 * 60))
    }

    @Test fun productionDawnPointsUseLiveEightPointScheduleAndCategoryCorrectCopy() {
        val model = productionAlmanacModel(schedule, zone, 360)
        assertEquals(listOf("imsak", "fajr", "syuruk", "dhuha"), model.points.take(4).map { it.id })
        assertEquals("Waktu Dhuha pada 07:40", model.points.first { it.id == "dhuha" }.accessibilityText)
        assertEquals(
            "07:40 dipilih · Waktu Dhuha" to null,
            productionPointReadout(model.selectionPolicy.select(model.points, 7 * 60 + 40), 7 * 60 + 40),
        )
    }

    @Test fun exactIsyakWinsBeforeDayCompleteCopy() {
        val model = productionAlmanacModel(schedule, zone, 20 * 60 + 42)
        val exact = model.selectionPolicy.select(model.points, 20 * 60 + 42)
        assertTrue(exact.dayComplete)
        assertEquals("20:42 dipilih · Masuk waktu Isyak" to null, productionPointReadout(exact, 20 * 60 + 42))
        assertEquals("Masa dipilih 20:42. Masuk waktu Isyak pada 20:42.", model.wheelStateDescription(exact, 20 * 60 + 42, true))
    }

    @Test fun productionReadoutOwnsEverySelectedMinuteAndLabelsFutureTime() {
        val model = productionAlmanacModel(schedule, zone, 5 * 60)
        (0 until 24 * 60).forEach { minute ->
            val selection = model.selectionPolicy.select(model.points, minute)
            val readout = model.wheelReadout(selection, minute, false)
            val selectedTime = "%02d:%02d".format(minute / 60, minute % 60)
            assertTrue(
                "selected minute $selectedTime",
                listOfNotNull(readout.first, readout.second).joinToString(" ").contains(selectedTime),
            )
        }

        val beforeImsak = productionPointReadout(model.selectionPolicy.select(model.points, 5 * 60), 5 * 60)
        assertEquals("05:00 dipilih", beforeImsak.first)
        assertTrue(beforeImsak.second!!.startsWith("Seterusnya · "))
        assertTrue(beforeImsak.second!!.endsWith(" · 05:45"))
        assertEquals("Leret roda kiri atau kanan untuk pilih waktu", model.copy.wheelHint)
    }

    @Test fun pointSummaryIsCategoryAwareBeforeExactBetweenAndAfterSchedule() {
        val model = productionAlmanacModel(schedule, zone, 300)
        assertEquals(ProductionPointSummary("PENANDA TAKWIM", "Imsak", "05:45"), productionPointSummary(model.selectionPolicy.select(model.points, 300)))
        assertEquals(ProductionPointSummary("PENANDA TAKWIM", "Imsak", "05:45"), productionPointSummary(model.selectionPolicy.select(model.points, 345)))
        assertEquals(ProductionPointSummary("SOLAT BERIKUT", "Subuh", "05:55"), productionPointSummary(model.selectionPolicy.select(model.points, 350)))
        assertEquals(ProductionPointSummary("MASUK WAKTU", "Subuh", "05:55"), productionPointSummary(model.selectionPolicy.select(model.points, 355)))
        assertEquals(ProductionPointSummary("WAKTU DHUHA", "Dhuha", "07:40"), productionPointSummary(model.selectionPolicy.select(model.points, 460)))
        assertTrue(productionPointSummary(model.selectionPolicy.select(model.points, 1380)).complete)
    }

    @Test fun conciseCtaCountsSemanticOverlapAndUsesAnchorWithoutTitle() {
        val intervals = listOf(
            AlmanacInterval("one", 500, 700, 500, truthClass = AlmanacIntervalTruthClass.PolicyMapped, title = "Long first title"),
            AlmanacInterval("two", 550, 650, 550, lane = 1, truthClass = AlmanacIntervalTruthClass.ProvisionalResearch, title = "Long second title"),
        )
        val model = productionAlmanacModel(schedule, zone, 600, researchIntervals = intervals)
        assertEquals("Buka 2 amalan · 10:00", model.copy.openLabel(600))
        assertTrue(model.copy.openDescription(600).contains("concordance dengan 2 amalan"))
        assertFalse(model.copy.openLabel(600).contains("Long"))

        val withAnchor = intervals.first().copy(timingLabel = "Mula 08:20")
        val annotation = productionIntervalAnnotation(withAnchor, 600, overlapCount = 1)
        assertEquals(1, Regex("08:20").findAll(annotation).count())
    }

    @Test fun proxyAndSearchFutureCopyIsTypeAwareWithoutVisualToleranceLeak() {
        val proxy = AlmanacInterval(
            "proxy", 800, 800, 800,
            visual = AlmanacIntervalVisual.EventTrigger,
            truthClass = AlmanacIntervalTruthClass.ScheduleContextProxy,
            title = "Tasbih selepas solat",
            timingLabel = "Selepas selesai solat",
        )
        val model = productionAlmanacModel(schedule, zone, 795, researchIntervals = listOf(proxy))
        assertEquals("Penanda seterusnya · 13:20", model.copy.openLabel(795))
        assertTrue(model.copy.openDescription(795).contains("penanda proksi berikut hari ini"))
        assertFalse(model.copy.openDescription(795).contains("merangkumi masa dipilih"))
        val state = model.wheelStateDescription(model.selectionPolicy.select(model.points, 795), 795, true)
        assertTrue(state.contains("Penanda proksi berikut hari ini"))
        assertFalse(state.contains("Pencetus pada masa ini"))
        assertEquals("Buka concordance · 13:20", model.copy.openLabel(800))
        assertTrue(model.copy.openDescription(800).contains("concordance dengan 1 amalan"))

        val search = proxy.copy(
            id = "search",
            startMinute = 900,
            endMinute = 1000,
            markerMinute = 1000,
            visual = AlmanacIntervalVisual.SearchEnvelope,
            truthClass = AlmanacIntervalTruthClass.Unresolved,
        )
        assertEquals("Kawasan carian berikut hari ini", productionOccurrenceLabel(com.bmuslim.app.ui.almanac.intervalOccurrence(search, 850)))
    }

    @Test fun fullScheduleClassificationTreatsDhuhaAsTimeNotGenericMarker() {
        assertEquals("SOLAT FARDU", productionScheduleClassification(JakimAlmanacSlot.FAJR))
        assertEquals("PENANDA TAKWIM", productionScheduleClassification(JakimAlmanacSlot.IMSAK))
        assertEquals("PENANDA TAKWIM", productionScheduleClassification(JakimAlmanacSlot.SYURUK))
        assertEquals("WAKTU DHUHA", productionScheduleClassification(JakimAlmanacSlot.DHUHA))
    }

    @Test fun productionContractContainsNoSyntheticWindowsOrGuidanceFields() {
        val fieldNames = com.bmuslim.app.ui.almanac.AlmanacPoint::class.java.declaredFields.map { it.name }
        assertFalse(fieldNames.any { it in setOf("end", "endMinute", "act", "actMinutes", "citation", "ruling", "active") })
    }

    @Test fun productionCopyDenylistHasNoDemoClaims() {
        val model = productionAlmanacModel(schedule, zone, 600)
        val corpus = buildString {
            append(model.copy.openLabel(600)); append(model.copy.closeLabel)
            append(productionSelectionDescription(model.selectionPolicy.select(model.points, 600)))
        }.lowercase()
        listOf("amalan utama", "rakaat", "hadis", "sahih", "aktif", "hingga", "tomorrow").forEach {
            assertFalse("forbidden production claim: $it", corpus.contains(it))
        }
    }

    @Test fun wheelSpeechIsFlavorOwnedAndExactWithoutDuplicateEventSpeech() {
        val model = productionAlmanacModel(schedule, zone, 600)
        val exact = model.selectionPolicy.select(model.points, 5 * 60 + 55)
        val exactSpeech = model.wheelStateDescription(exact, 5 * 60 + 55, true)
        assertEquals("Masa dipilih 05:55. Masuk waktu Subuh pada 05:55.", exactSpeech)
        assertEquals(1, Regex("Masuk waktu Subuh").findAll(exactSpeech).count())

        val before = model.selectionPolicy.select(model.points, 5 * 60)
        assertEquals("Masa dipilih 05:00. Penanda berikutnya Imsak pada 05:45.", model.wheelStateDescription(before, 5 * 60, true))
        val complete = model.selectionPolicy.select(model.points, 23 * 60)
        assertEquals("Masa dipilih 23:00. Jadual hari ini selesai.", model.wheelStateDescription(complete, 23 * 60, true))
        assertEquals(
            "Sekarang 23:00. Jadual hari ini selesai.",
            model.wheelStateDescription(complete, 23 * 60, false),
        )
    }

    private fun value(slot: JakimAlmanacSlot, hour: Int, minute: Int) =
        JakimAlmanacValue(slot, date, LocalTime.of(hour, minute))
}
