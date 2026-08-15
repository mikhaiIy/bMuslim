package com.bmuslim.app

import com.bmuslim.app.prayer.JakimAdaptResult
import com.bmuslim.app.prayer.JakimPrayerAdapter
import com.bmuslim.app.prayer.JakimPrayerRecord
import com.bmuslim.app.prayer.JakimPrayerResponse
import com.bmuslim.app.prayer.JakimPrayerTimesRepository
import com.bmuslim.app.prayer.JakimZones
import com.bmuslim.app.prayer.PrayerTimesRepository
import com.bmuslim.app.prayer.PrayerTimesResult
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.TruthBlocker
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerTimesStateHolderTest {
    private val clock = MutableClock(Instant.parse("2026-07-13T02:00:00Z"))
    private val zone = requireNotNull(JakimZones.find("WLY01"))

    @Test
    fun initiallyUnconfiguredWithoutSavedZone() = runTest {
        val holder = holder(FakeRepository(mutableListOf()), MemoryZonePreference(), this)

        assertEquals(PrayerScreenState.Unconfigured, holder.state)
    }

    @Test
    fun zoneSelectionPersistsLoadsAndBecomesReady() = runTest {
        val preferences = MemoryZonePreference()
        val holder = holder(FakeRepository(mutableListOf(success())), preferences, this)

        holder.selectZone(zone)
        assertEquals(PrayerScreenState.Loading(zone), holder.state)
        runCurrent()

        assertTrue(holder.state is PrayerScreenState.Ready)
        assertEquals(8, (holder.state as PrayerScreenState.Ready).almanacSchedule.values.size)
        assertEquals("WLY01", preferences.value)
    }

    @Test
    fun savedZoneRestoresAndFetchesWithoutDefaultingFreshUsers() = runTest {
        val holder = holder(FakeRepository(mutableListOf(success())), MemoryZonePreference("WLY01"), this)
        assertEquals(PrayerScreenState.Loading(zone), holder.state)

        runCurrent()

        assertTrue(holder.state is PrayerScreenState.Ready)
    }

    @Test
    fun staleDataMapsToInvalidFailureAndNeverShowsOldTimes() = runTest {
        val repository = FakeRepository(mutableListOf(PrayerTimesResult.InvalidData("wrong-date")))
        val holder = holder(repository, MemoryZonePreference(), this)

        holder.selectZone(zone)
        runCurrent()

        assertEquals(PrayerScreenState.Failure(zone, PrayerFailureKind.INVALID_DATA), holder.state)
    }

    @Test
    fun retryRunsTheSameZoneAndCanRecover() = runTest {
        val repository = FakeRepository(mutableListOf(PrayerTimesResult.NoConnection, success()))
        val holder = holder(repository, MemoryZonePreference(), this)
        holder.selectZone(zone)
        runCurrent()
        assertEquals(PrayerScreenState.Failure(zone, PrayerFailureKind.NO_CONNECTION), holder.state)

        holder.retry()
        assertEquals(PrayerScreenState.Loading(zone), holder.state)
        runCurrent()

        assertTrue(holder.state is PrayerScreenState.Ready)
        assertEquals(2, repository.keys.size)
    }

    @Test
    fun zoneReplacementClearsOldScheduleAndPublishesNewScheduleAtomically() = runTest {
        val first = CompletableDeferred<PrayerTimesResult>().apply { complete(success()) }
        val second = CompletableDeferred<PrayerTimesResult>()
        val repository = DeferredRepository(mutableListOf(first, second))
        val holder = holder(repository, MemoryZonePreference(), this)
        holder.selectZone(zone)
        runCurrent()
        assertTrue(holder.state is PrayerScreenState.Ready)

        val labuan = requireNotNull(JakimZones.find("WLY02"))
        holder.selectZone(labuan)
        assertEquals(PrayerScreenState.Loading(labuan), holder.state)

        second.complete(success(zoneCode = "WLY02"))
        runCurrent()
        val ready = holder.state as PrayerScreenState.Ready
        assertEquals("WLY02", ready.zone.code)
        assertEquals("WLY02", ready.almanacSchedule.exactKey.zoneCode)
        assertEquals(8, ready.almanacSchedule.values.size)
    }

    @Test
    fun foregroundRefreshClearsReadyValuesAndRequestsNewMalaysiaDay() = runTest {
        val repository = FakeRepository(
            mutableListOf(success(LocalDate.of(2026, 7, 13)), success(LocalDate.of(2026, 7, 14))),
        )
        val holder = holder(repository, MemoryZonePreference(), this)
        holder.selectZone(zone)
        runCurrent()
        assertTrue(holder.state is PrayerScreenState.Ready)

        clock.currentInstant = Instant.parse("2026-07-13T16:01:00Z")
        holder.refreshIfDayChanged()

        assertEquals(PrayerScreenState.Loading(zone), holder.state)
        runCurrent()
        assertEquals(LocalDate.of(2026, 7, 14), repository.keys.last().civilDate)
        assertEquals(
            LocalDate.of(2026, 7, 14),
            (holder.state as PrayerScreenState.Ready).temporalState.requestedKey.civilDate,
        )
    }

    @Test
    fun foregroundSchedulerRefreshesAtMidnightWithoutAnotherResumeAndReschedules() = runTest {
        val first = CompletableDeferred<PrayerTimesResult>().apply {
            complete(success(LocalDate.of(2026, 7, 13)))
        }
        val second = CompletableDeferred<PrayerTimesResult>()
        val third = CompletableDeferred<PrayerTimesResult>()
        val repository = DeferredRepository(mutableListOf(first, second, third))
        val holder = holder(repository, MemoryZonePreference(), this)
        holder.onForeground()
        holder.selectZone(zone)
        runCurrent()
        assertTrue(holder.state is PrayerScreenState.Ready)

        clock.currentInstant = Instant.parse("2026-07-13T16:00:00Z")
        advanceTimeBy(14 * 60 * 60 * 1000L)
        runCurrent()

        assertEquals(PrayerScreenState.Loading(zone), holder.state)
        assertEquals(LocalDate.of(2026, 7, 14), repository.keys.last().civilDate)
        second.complete(success(LocalDate.of(2026, 7, 14)))
        runCurrent()
        assertEquals(
            LocalDate.of(2026, 7, 14),
            (holder.state as PrayerScreenState.Ready).temporalState.requestedKey.civilDate,
        )

        clock.currentInstant = Instant.parse("2026-07-14T16:00:00Z")
        advanceTimeBy(24 * 60 * 60 * 1000L)
        runCurrent()

        assertEquals(PrayerScreenState.Loading(zone), holder.state)
        assertEquals(LocalDate.of(2026, 7, 15), repository.keys.last().civilDate)
        third.complete(success(LocalDate.of(2026, 7, 15)))
        runCurrent()
        assertTrue(holder.state is PrayerScreenState.Ready)
        holder.onBackground()
    }

    @Test
    fun readyPrayerScheduleDoesNotUnlockRecommendations() = runTest {
        val temporal = (success() as PrayerTimesResult.Success).temporalState

        val resolution = productionTruthResolution(temporal)

        assertFalse(resolution.isReady)
        assertEquals(
            listOf(TruthBlocker.ContentMissing, TruthBlocker.RankingMissing),
            resolution.blockers.toList(),
        )
    }

    private fun holder(
        repository: PrayerTimesRepository,
        preference: ZonePreference,
        scope: CoroutineScope,
    ) = PrayerTimesStateHolder(repository, preference, scope, clock)

    private fun success(
        date: LocalDate = LocalDate.of(2026, 7, 13),
        zoneCode: String = "WLY01",
    ): PrayerTimesResult {
        val key = PrayerDayKey(zoneCode, date)
        val providerDate = date.format(DateTimeFormatter.ofPattern("dd-MMM-uuuu", Locale.ENGLISH))
        val response = JakimPrayerResponse(
            status = "OK!",
            zone = zoneCode,
            prayerTimes = listOf(
                JakimPrayerRecord(
                    providerDate,
                    "05:45:00",
                    "05:55:00",
                    "07:15:00",
                    "07:40:00",
                    "13:20:00",
                    "16:45:00",
                    "19:28:00",
                    "20:42:00",
                ),
            ),
        )
        val adapted = JakimPrayerAdapter().adapt(key, response)
        adapted as JakimAdaptResult.Success
        return PrayerTimesResult.Success(adapted.almanacSchedule, adapted.temporalState)
    }

    private class FakeRepository(val results: MutableList<PrayerTimesResult>) : PrayerTimesRepository {
        val keys = mutableListOf<PrayerDayKey>()
        override suspend fun fetch(key: PrayerDayKey): PrayerTimesResult {
            keys += key
            return results.removeFirst()
        }
    }

    private class DeferredRepository(
        private val results: MutableList<CompletableDeferred<PrayerTimesResult>>,
    ) : PrayerTimesRepository {
        val keys = mutableListOf<PrayerDayKey>()
        override suspend fun fetch(key: PrayerDayKey): PrayerTimesResult {
            keys += key
            return results.removeFirst().await()
        }
    }

    private class MemoryZonePreference(var value: String? = null) : ZonePreference {
        override fun read(): String? = value
        override fun write(zoneCode: String) {
            value = zoneCode
        }
    }

    private class MutableClock(var currentInstant: Instant) : Clock() {
        private val malaysia = ZoneId.of("Asia/Kuala_Lumpur")
        override fun getZone(): ZoneId = malaysia
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = currentInstant
    }
}
