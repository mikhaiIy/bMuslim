package com.bmuslim.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimAlmanacSlot
import com.bmuslim.app.prayer.JakimAlmanacValue
import com.bmuslim.app.truth.PrayerDayKey
import com.bmuslim.app.truth.TemporalProvenance
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.AlmanacSelection
import com.bmuslim.app.ui.almanac.SharedAlmanacSnapshot
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ResearchConcordanceBackFocusTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun systemBackReturnsToExactConcordanceRowWithoutClosingTheFolio() {
        val selectedTitle = "Baca tiga surah perlindungan"

        compose.setContent {
            MaterialTheme {
                ResearchPreviewInlineFolio(
                    schedule = schedule(),
                    snapshot = SharedAlmanacSnapshot(
                        selectedMinute = 400,
                        actualNowMinute = 400,
                        hasScrubbed = true,
                        selection = AlmanacSelection(null, null, null, false),
                        points = emptyList(),
                        fontComposition = AlmanacFontComposition.Maximum,
                    ),
                    internallyScrollable = true,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        compose.onNodeWithText(selectedTitle).assertExists().performClick()
        compose.onNodeWithTag("research-demo-ledger").assertExists()

        compose.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        compose.waitForIdle()

        compose.onNodeWithTag("research-demo-ledger").assertDoesNotExist()
        compose.onNodeWithText("KONKORDANS NEUTRAL").assertExists()
        compose.onNodeWithText(selectedTitle).assertExists().assertIsFocused()
    }

    private fun schedule(): JakimAlmanacSchedule {
        val date = LocalDate.of(2026, 7, 15)
        val times = listOf(
            JakimAlmanacSlot.IMSAK to LocalTime.of(5, 45),
            JakimAlmanacSlot.FAJR to LocalTime.of(5, 55),
            JakimAlmanacSlot.SYURUK to LocalTime.of(7, 15),
            JakimAlmanacSlot.DHUHA to LocalTime.of(7, 40),
            JakimAlmanacSlot.DHUHR to LocalTime.of(13, 20),
            JakimAlmanacSlot.ASR to LocalTime.of(16, 45),
            JakimAlmanacSlot.MAGHRIB to LocalTime.of(19, 30),
            JakimAlmanacSlot.ISHA to LocalTime.of(20, 45),
        )
        return JakimAlmanacSchedule(
            exactKey = PrayerDayKey("WLY01", date),
            scheduleZoneId = "Asia/Kuala_Lumpur",
            values = times.map { (slot, time) -> JakimAlmanacValue(slot, date, time) },
            provenance = TemporalProvenance("JAKIM", "test", "instrumentation-fixture"),
        )
    }
}
