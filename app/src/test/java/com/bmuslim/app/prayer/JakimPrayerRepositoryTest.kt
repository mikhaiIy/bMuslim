package com.bmuslim.app.prayer

import com.bmuslim.app.truth.PrayerDayKey
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JakimPrayerRepositoryTest {
    private val key = PrayerDayKey("WLY01", LocalDate.of(2026, 7, 13))
    private val validBody =
        """{"status":"OK!","zone":"WLY01","prayerTime":[{"date":"13-Jul-2026","imsak":"05:45:00","fajr":"05:55:00","syuruk":"07:15:00","dhuha":"07:40:00","dhuhr":"13:20:00","asr":"16:45:00","maghrib":"19:28:00","isha":"20:42:00"}]}"""

    @Test
    fun requestUrlUsesTheLockedEndpointAndEncodedZone() {
        assertEquals(
            "https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone=WLY01",
            JakimPrayerTimesRepository.requestUrl("WLY01"),
        )
        assertTrue(JakimPrayerTimesRepository.requestUrl("BAD ZONE").endsWith("zone=BAD+ZONE"))
    }

    @Test
    fun successfulHttpBodyReturnsValidatedTemporalState() = runTest {
        val repository = JakimPrayerTimesRepository(httpClient = JakimHttpClient { JakimHttpResult.Success(validBody) })

        val result = repository.fetch(key)

        assertTrue(result is PrayerTimesResult.Success)
        assertEquals(8, (result as PrayerTimesResult.Success).almanacSchedule.values.size)
    }

    @Test
    fun networkAndHttpFailuresStayDistinct() = runTest {
        val offline = JakimPrayerTimesRepository(httpClient = JakimHttpClient { JakimHttpResult.NoConnection })
        val rejected = JakimPrayerTimesRepository(httpClient = JakimHttpClient { JakimHttpResult.HttpFailure(503) })

        assertEquals(PrayerTimesResult.NoConnection, offline.fetch(key))
        assertEquals(PrayerTimesResult.ProviderRejected, rejected.fetch(key))
    }

    @Test
    fun malformedAndOversizedBodiesAreInvalidData() = runTest {
        val malformed = JakimPrayerTimesRepository(httpClient = JakimHttpClient { JakimHttpResult.Success("not-json") })
        val oversized = JakimPrayerTimesRepository(httpClient = JakimHttpClient { JakimHttpResult.ResponseTooLarge })

        assertTrue(malformed.fetch(key) is PrayerTimesResult.InvalidData)
        assertTrue(oversized.fetch(key) is PrayerTimesResult.InvalidData)
    }

    @Test
    fun unsupportedZoneDoesNotCallTransport() = runTest {
        var calls = 0
        val repository = JakimPrayerTimesRepository(
            httpClient = JakimHttpClient {
                calls += 1
                JakimHttpResult.Success(validBody)
            },
        )

        val result = repository.fetch(key.copy(zoneCode = "BAD01"))

        assertTrue(result is PrayerTimesResult.InvalidData)
        assertEquals(0, calls)
    }
}
