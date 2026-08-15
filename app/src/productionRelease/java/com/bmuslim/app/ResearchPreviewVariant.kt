package com.bmuslim.app

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.SharedAlmanacSnapshot

internal fun showScheduleRowsInFolio(): Boolean = true
internal fun showResearchPreviewDemoLayout(): Boolean = false

@Composable
internal fun ResearchPreviewAction(
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit

@Composable
internal fun ResearchPreviewDestination(onBack: () -> Unit) = Unit

@Composable
internal fun rememberResearchPreviewIntervals(
    schedule: JakimAlmanacSchedule,
    composition: AlmanacFontComposition,
): List<AlmanacInterval> = emptyList()

@Composable
internal fun ResearchPreviewMomentSummary(
    schedule: JakimAlmanacSchedule,
    snapshot: SharedAlmanacSnapshot,
    modifier: Modifier,
) = Unit

@Composable
internal fun ResearchPreviewInlineFolio(
    schedule: JakimAlmanacSchedule,
    snapshot: SharedAlmanacSnapshot,
    internallyScrollable: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        "Panduan amalan belum tersedia",
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
    )
}
