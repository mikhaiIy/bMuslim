package com.bmuslim.app.prayer

import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.PrayerSlot
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JakimPrayerAdapterTest {
    private val parser = JakimResponseParser()
    private val adapter = JakimPrayerAdapter()
    private val key = PrayerDayKey("WLY01", LocalDate.of(2026, 7, 13))
    private val source = JakimPrayerTimesRepository.requestUrl(key.zoneCode)

    @Test
    fun validResponseMapsEightCanonicalAlmanacSlotsAndDerivesFivePrayerSlots() {
        val result = adapt(validJson()) as JakimAdaptResult.Success

        assertEquals(JakimAlmanacSlot.entries, result.almanacSchedule.values.map { it.slot })
        assertEquals(key, result.temporalState.snapshot.exactKey)
        assertEquals(PrayerSlot.entries, result.temporalState.snapshot.dateBearingValues.map { it.slot })
        assertEquals(source, result.temporalState.snapshot.provenance.sourceReference)
        assertEquals(JAKIM_PROVIDER_ID, result.temporalState.sourceContract.providerId)
        assertEquals(JAKIM_CONTRACT_VERSION, result.temporalState.sourceContract.contractVersion)
    }

    @Test
    fun malformedJsonIsRejectedByParser() {
        assertEquals(JakimParseResult.MalformedJson, parser.parse("{not-json"))
    }

    @Test
    fun duplicateRequiredRecordMembersAreRejectedWithoutLastValueWinning() {
        val values = mapOf(
            "date" to "13-Jul-2026",
            "imsak" to "05:45:00",
            "fajr" to "05:55:00",
            "syuruk" to "07:15:00",
            "dhuha" to "07:40:00",
            "dhuhr" to "13:20:00",
            "asr" to "16:45:00",
            "maghrib" to "19:28:00",
            "isha" to "20:42:00",
        )
        values.forEach { (field, value) ->
            val duplicated = validJson().replace(
                "\"$field\":\"$value\"",
                "\"$field\":\"duplicate\",\"$field\":\"$value\"",
            )
            assertEquals("duplicate $field", JakimParseResult.MalformedJson, parser.parse(duplicated))
        }
    }

    @Test
    fun missingAndWrongTypedRequiredMembersAreRejected() {
        val values = mapOf(
            "date" to "13-Jul-2026",
            "imsak" to "05:45:00",
            "fajr" to "05:55:00",
            "syuruk" to "07:15:00",
            "dhuha" to "07:40:00",
            "dhuhr" to "13:20:00",
            "asr" to "16:45:00",
            "maghrib" to "19:28:00",
            "isha" to "20:42:00",
        )
        values.forEach { (field, value) ->
            val missing = validJson().replace("\"$field\":\"$value\",", "")
                .replace(",\"$field\":\"$value\"", "")
            val wrongType = validJson().replace("\"$field\":\"$value\"", "\"$field\":123")
            assertEquals("missing $field", JakimParseResult.MalformedJson, parser.parse(missing))
            assertEquals("wrong type $field", JakimParseResult.MalformedJson, parser.parse(wrongType))
        }
        assertEquals(
            JakimParseResult.MalformedJson,
            parser.parse(validJson().replace("\"status\":\"OK!\",", "")),
        )
        assertEquals(
            JakimParseResult.MalformedJson,
            parser.parse(validJson().replace("\"zone\":\"WLY01\"", "\"zone\":1")),
        )
    }

    @Test
    fun duplicateRequiredEnvelopeMembersAreRejected() {
        assertEquals(
            JakimParseResult.MalformedJson,
            parser.parse(validJson().replace("\"status\":", "\"status\":\"old\",\"status\":")),
        )
        assertEquals(
            JakimParseResult.MalformedJson,
            parser.parse(validJson().replace("\"zone\":", "\"zone\":\"WLY02\",\"zone\":")),
        )
        assertEquals(
            JakimParseResult.MalformedJson,
            parser.parse(validJson().replace("\"prayerTime\":", "\"prayerTime\":[],\"prayerTime\":")),
        )
    }

    @Test
    fun missingAndWrongTypedRequiredEnvelopeMembersAreRejected() {
        val requiredValues = mapOf(
            "status" to "\"OK!\"",
            "zone" to "\"WLY01\"",
            "prayerTime" to "[${validRecord()}]",
        )
        requiredValues.forEach { (field, value) ->
            val missing = validJson().replace("\"$field\":$value,", "")
                .replace(",\"$field\":$value", "")
            val wrongType = validJson().replace("\"$field\":$value", "\"$field\":123")
            assertEquals("missing envelope $field", JakimParseResult.MalformedJson, parser.parse(missing))
            assertEquals("wrong envelope type $field", JakimParseResult.MalformedJson, parser.parse(wrongType))
        }
    }

    @Test
    fun nonOkStatusIsProviderRejection() {
        assertFailure(validJson().replace("OK!", "NO_RECORD"), JakimValidationFailure.ProviderStatusRejected)
    }

    @Test
    fun wrongZoneIsRejected() {
        assertFailure(validJson().replace("WLY01", "WLY02"), JakimValidationFailure.ZoneMismatch)
    }

    @Test
    fun staleDateIsRejected() {
        assertFailure(validJson().replace("13-Jul-2026", "12-Jul-2026"), JakimValidationFailure.WrongDate)
    }

    @Test
    fun missingOrDuplicateScheduleIsRejected() {
        assertFailure(validJson().replace(Regex("\\[\\{.*}]"), "[]"), JakimValidationFailure.ScheduleCardinality)
        val record = validRecord()
        assertFailure(
            """{"status":"OK!","zone":"WLY01","prayerTime":[$record,$record]}""",
            JakimValidationFailure.ScheduleCardinality,
        )
    }

    @Test
    fun malformedRequiredTimeIsRejected() {
        assertFailure(validJson().replace("13:20:00", "1:20 pm"), JakimValidationFailure.MalformedTime)
    }

    @Test
    fun reversedChronologyIsRejected() {
        assertFailure(validJson().replace("16:45:00", "12:00:00"), JakimValidationFailure.NonChronological)
    }

    @Test
    fun auxiliaryChronologyIsAlsoStrictlyValidated() {
        assertFailure(validJson().replace("07:40:00", "07:00:00"), JakimValidationFailure.NonChronological)
        assertFailure(validJson().replace("05:45:00", "06:00:00"), JakimValidationFailure.NonChronological)
    }

    @Test
    fun zoneRegistryUsesCurrentOfficialCorrections() {
        assertTrue(JakimZones.contains("KTN02"))
        assertTrue(JakimZones.contains("PHG07"))
        assertTrue(!JakimZones.contains("KTN03"))
        assertEquals(60, JakimZones.all.size)
    }

    private fun adapt(json: String): JakimAdaptResult {
        val parsed = parser.parse(json) as JakimParseResult.Success
        return adapter.adapt(key, parsed.response)
    }

    private fun assertFailure(json: String, expected: JakimValidationFailure) {
        val result = adapt(json)
        assertTrue(result is JakimAdaptResult.Failure)
        assertEquals(expected, (result as JakimAdaptResult.Failure).reason)
    }

    private fun validJson(): String =
        """{"status":"OK!","zone":"WLY01","prayerTime":[${validRecord()}]}"""

    private fun validRecord(): String =
        """{"date":"13-Jul-2026","imsak":"05:45:00","fajr":"05:55:00","syuruk":"07:15:00","dhuha":"07:40:00","dhuhr":"13:20:00","asr":"16:45:00","maghrib":"19:28:00","isha":"20:42:00"}"""
}
