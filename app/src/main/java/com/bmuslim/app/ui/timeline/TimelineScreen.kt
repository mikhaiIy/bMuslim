package com.bmuslim.app.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bmuslim.app.ui.theme.BMuslimTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(modifier: Modifier = Modifier) {
    // Milestone 1: state lives right here in the screen. Later we hoist it to a ViewModel.
    val deeds = remember { sampleDeeds().toMutableStateList() }
    val doneCount = deeds.count { it.done }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("bMuslim", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Khamis · 12 Muharram 1448",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = {},
                        label = { Text("$doneCount / ${deeds.size}") },
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
            )
        },
    ) { padding ->
        // The visible "day" spans from the earliest deed start to the latest deed end.
        val dayStart = deeds.minOf { it.start }
        val dayEnd = deeds.maxOf { it.end }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(deeds) { deed ->
                DeedCard(
                    deed = deed,
                    dayStart = dayStart,
                    dayEnd = dayEnd,
                    onToggle = {
                        val i = deeds.indexOf(deed)
                        if (i >= 0) deeds[i] = deed.copy(done = !deed.done)
                    },
                )
            }
        }
    }
}

@Composable
private fun DeedCard(
    deed: TimelineDeed,
    dayStart: LocalTime,
    dayEnd: LocalTime,
    onToggle: () -> Unit,
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = deed.done, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(deed.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${deed.windowLabel} · " +
                            "${deed.start.format(timeFmt)}–${deed.end.format(timeFmt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            GanttBar(start = deed.start, end = deed.end, dayStart = dayStart, dayEnd = dayEnd)
            Spacer(Modifier.height(8.dp))
            Text(
                text = deed.citation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * A simple Gantt-style bar: a full-width track with a filled segment positioned to show
 * where this deed's window falls within the visible day. Uses Row weights for proportion,
 * which is a nice way to learn how Compose lays out flexible space.
 */
@Composable
private fun GanttBar(
    start: LocalTime,
    end: LocalTime,
    dayStart: LocalTime,
    dayEnd: LocalTime,
) {
    val total = minutesBetween(dayStart, dayEnd).toFloat().coerceAtLeast(1f)
    val before = minutesBetween(dayStart, start).toFloat().coerceIn(0f, total)
    val span = minutesBetween(start, end).toFloat().coerceIn(1f, total - before)
    val after = (total - before - span).coerceAtLeast(0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (before > 0f) Spacer(Modifier.weight(before))
        Box(
            Modifier
                .weight(span)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.primary),
        )
        if (after > 0f) Spacer(Modifier.weight(after))
    }
}

private fun minutesBetween(a: LocalTime, b: LocalTime): Int =
    (b.toSecondOfDay() - a.toSecondOfDay()) / 60

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun TimelineScreenPreview() {
    BMuslimTheme {
        TimelineScreen()
    }
}
