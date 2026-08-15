package com.bmuslim.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmuslim.app.prayer.JakimAlmanacCategory
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.prayer.JakimAlmanacSlot
import com.bmuslim.app.prayer.JakimZone
import com.bmuslim.app.ui.almanac.AlmanacCopy
import com.bmuslim.app.ui.almanac.AlmanacHeader
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacPoint
import com.bmuslim.app.ui.almanac.AlmanacSelection
import com.bmuslim.app.ui.almanac.AlmanacVisualRole
import com.bmuslim.app.ui.almanac.NonCircularPointSelection
import com.bmuslim.app.ui.almanac.SharedAlmanacModel
import com.bmuslim.app.ui.almanac.SharedAlmanacShell
import com.bmuslim.app.ui.almanac.SharedAlmanacSnapshot
import com.bmuslim.app.ui.almanac.formatMinute
import com.bmuslim.app.ui.almanac.intervalAnchorMinute
import com.bmuslim.app.ui.almanac.AlmanacIntervalOccurrence
import com.bmuslim.app.ui.almanac.almanacFontComposition
import com.bmuslim.app.ui.almanac.intervalOccurrence
import com.bmuslim.app.ui.almanac.memberCountAt
import com.bmuslim.app.ui.almanac.memberTitlesAt
import com.bmuslim.app.ui.almanac.semanticallyIntersectingIntervalsAt
import com.bmuslim.app.ui.almanac.selectIntervalForMinute
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

private val Pine = Color(0xFF0B4938)
private val Paper = Color(0xFFF4F2E9)
private val PaperBright = Color(0xFFFAF8F0)
private val PaperInk = Color(0xFF102A21)
private val PaperMuted = Color(0xFF5B7067)
private val Saffron = Color(0xFFE4A43A)
@Composable
internal fun ProductionAlmanacScreen(
    state: PrayerScreenState.Ready,
    clock: Clock? = null,
) {
    val composition = almanacFontComposition(LocalDensity.current.fontScale)
    val researchIntervals = rememberResearchPreviewIntervals(state.almanacSchedule, composition)
    var actualNow by remember(state.almanacSchedule.exactKey) { mutableIntStateOf(currentPhoneMinute(clock)) }
    var foregroundGeneration by remember(state.almanacSchedule.exactKey) { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, clock, state.almanacSchedule.exactKey) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                actualNow = currentPhoneMinute(clock)
                foregroundGeneration += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(clock, state.almanacSchedule.exactKey) {
        while (true) {
            actualNow = currentPhoneMinute(clock)
            delay(millisUntilNextPhoneMinute(clock))
        }
    }
    val model = remember(state.almanacSchedule, state.zone, actualNow, foregroundGeneration, researchIntervals) {
        productionAlmanacModel(
            schedule = state.almanacSchedule,
            zone = state.zone,
            actualNowMinute = actualNow,
            foregroundGeneration = foregroundGeneration,
            researchIntervals = researchIntervals,
        )
    }
    SharedAlmanacShell(
        model = model,
        modifier = Modifier.fillMaxSize(),
        summary = { snapshot, modifier ->
            if (showResearchPreviewDemoLayout()) {
                ResearchPreviewMomentSummary(state.almanacSchedule, snapshot, modifier)
            } else {
                ProductionScheduleSummary(snapshot, modifier)
            }
        },
        body = { snapshot, internallyScrollable, modifier ->
            ProductionScheduleFolio(
                schedule = state.almanacSchedule,
                snapshot = snapshot,
                internallyScrollable = internallyScrollable,
                modifier = modifier,
            )
        },
    )
}

internal fun productionAlmanacModel(
    schedule: JakimAlmanacSchedule,
    zone: JakimZone,
    actualNowMinute: Int,
    foregroundGeneration: Int = 0,
    researchIntervals: List<AlmanacInterval> = emptyList(),
): SharedAlmanacModel {
    val points = schedule.values.map { value ->
        AlmanacPoint(
            id = value.slot.name.lowercase(Locale.ROOT),
            title = value.slot.displayName,
            minute = value.time.hour * 60 + value.time.minute,
            role = if (value.slot.category == JakimAlmanacCategory.OBLIGATORY_PRAYER) AlmanacVisualRole.Primary else AlmanacVisualRole.Secondary,
            accessibilityText = when {
                value.slot.category == JakimAlmanacCategory.OBLIGATORY_PRAYER ->
                    "Masuk waktu ${value.slot.displayName} pada ${value.time.toHm()}"
                value.slot == JakimAlmanacSlot.DHUHA -> "Waktu Dhuha pada ${value.time.toHm()}"
                else -> "Penanda ${value.slot.displayName} pada ${value.time.toHm()}"
            },
        )
    }
    val syuruk = points.first { it.id == JakimAlmanacSlot.SYURUK.name.lowercase(Locale.ROOT) }.minute
    val maghrib = points.first { it.id == JakimAlmanacSlot.MAGHRIB.name.lowercase(Locale.ROOT) }.minute
    fun contextualInterval(minute: Int) = selectIntervalForMinute(researchIntervals, minute)
    fun semanticIntervals(minute: Int) = semanticallyIntersectingIntervalsAt(researchIntervals, minute)
    return SharedAlmanacModel(
        modelKey = "${schedule.exactKey.zoneCode}:${schedule.exactKey.civilDate}:foreground=$foregroundGeneration",
        header = AlmanacHeader(
            brand = "bMuslim",
            dateLine = schedule.exactKey.civilDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM uuuu", Locale.forLanguageTag("ms-MY"))),
            secondaryDateLine = "${zone.code} · ${zone.area}",
            timeContext = { _, selected -> if (selected) "Masa dipilih" else "Sekarang" },
        ),
        points = points,
        intervals = researchIntervals,
        initialMinute = actualNowMinute,
        actualNowMinute = actualNowMinute,
        followsNow = true,
        minuteStep = 1,
        sunriseMinute = syuruk,
        sunsetMinute = maghrib,
        copy = AlmanacCopy(
            wheelDescription = "Roda takwim JAKIM 24 jam. Leret mendatar atau laras untuk memilih masa.",
            wheelHint = "Leret roda kiri atau kanan untuk pilih waktu",
            openLabel = { minute ->
                val intersecting = semanticIntervals(minute)
                val interval = intersecting.firstOrNull() ?: contextualInterval(minute)
                val itemCount = intervalItemCount(intersecting, minute)
                when {
                    itemCount > 1 -> "Buka $itemCount amalan · ${formatMinute(minute)}"
                    itemCount == 1 -> "Buka concordance · ${formatMinute(minute)}"
                    interval != null && interval in intersecting -> "${productionOpenKind(interval)} · ${formatMinute(minute)}"
                    interval != null -> "${productionOpenKind(interval).removePrefix("Buka ").replaceFirstChar { it.uppercase() }} seterusnya · ${formatMinute(intervalAnchorMinute(interval))}"
                    else -> "Buka takwim · ${formatMinute(minute)}"
                }
            },
            openDescription = { minute ->
                val intersecting = semanticIntervals(minute)
                val interval = intersecting.firstOrNull() ?: contextualInterval(minute)
                val itemCount = intervalItemCount(intersecting, minute)
                when {
                    itemCount > 0 -> "Buka concordance dengan $itemCount amalan pada masa dipilih ${formatMinute(minute)}. " +
                        intervalTitles(intersecting, minute).joinToString("; ")
                    interval != null && interval in intersecting ->
                        "Buka concordance untuk satu amalan. ${productionOccurrenceLabel(intervalOccurrence(interval, minute))} ${formatMinute(minute)}"
                    interval != null ->
                        "Buka concordance seterusnya, ${productionOccurrenceLabel(intervalOccurrence(interval, minute)).lowercase()} pada ${formatMinute(intervalAnchorMinute(interval))} hari ini"
                    else -> "Buka jadual takwim rasmi pada ${formatMinute(minute)}"
                }
            },
            closeLabel = if (researchIntervals.isEmpty()) "Tutup takwim" else "Tutup amalan",
            closeDescription = if (researchIntervals.isEmpty()) "Tutup jadual takwim dan kembali ke roda penuh" else "Tutup butiran amalan dan kembali ke roda penuh",
            closedAnnouncement = if (researchIntervals.isEmpty()) "Paparan takwim ditutup" else "Paparan amalan ditutup",
            returnToNow = "Kembali ke sekarang",
            todayLabel = "Hari ini",
            plannerLabel = "Perancang",
            settingsLabel = "Tetapan",
            unavailableLabel = "belum tersedia",
        ),
        selectionPolicy = NonCircularPointSelection,
        wheelReadout = { selection, minute, _ ->
            productionPointReadout(selection, minute)
        },
        wheelStateDescription = { selection, minute, selected ->
            buildString {
                append(productionWheelStateDescription(selection, minute, selected))
                val intersecting = semanticIntervals(minute)
                val itemCount = intervalItemCount(intersecting, minute)
                if (itemCount > 1) {
                    append(" $itemCount amalan bertemu di sini. ")
                    append(intervalTitles(intersecting, minute).joinToString("; "))
                    append(".")
                } else if (itemCount == 1) {
                    append(" Satu amalan bertemu di sini. ")
                    append(intervalTitles(intersecting, minute).joinToString("; "))
                    append(".")
                } else contextualInterval(minute)?.let { interval ->
                    append(" ")
                    append(productionOccurrenceLabel(intervalOccurrence(interval, minute)))
                    intervalTitles(listOf(interval), intervalAnchorMinute(interval))
                        .joinToString("; ")
                        .takeIf(String::isNotBlank)
                        ?.let { append(" ").append(it) }
                    append(" pada ").append(formatMinute(intervalAnchorMinute(interval))).append(".")
                }
            }
        },
    )
}

internal fun productionOccurrenceLabel(occurrence: AlmanacIntervalOccurrence): String = when (occurrence) {
    AlmanacIntervalOccurrence.Active -> "Masa sumber merangkumi masa dipilih"
    AlmanacIntervalOccurrence.PolicyWindow -> "Ruang amalan merangkumi masa dipilih"
    AlmanacIntervalOccurrence.DraftWindow -> "Jalur draf merangkumi masa dipilih"
    AlmanacIntervalOccurrence.ScheduleContext -> "Penanda proksi pada masa dipilih"
    AlmanacIntervalOccurrence.SearchRegion -> "Kawasan carian merangkumi masa dipilih"
    AlmanacIntervalOccurrence.Trigger -> "Pencetus pada masa ini"
    AlmanacIntervalOccurrence.Upcoming -> "Amalan berikut"
    AlmanacIntervalOccurrence.UpcomingPolicyWindow -> "Ruang amalan berikut hari ini"
    AlmanacIntervalOccurrence.UpcomingDraftWindow -> "Jalur draf berikut hari ini"
    AlmanacIntervalOccurrence.UpcomingScheduleContext -> "Penanda proksi berikut hari ini"
    AlmanacIntervalOccurrence.UpcomingSearchRegion -> "Kawasan carian berikut hari ini"
    AlmanacIntervalOccurrence.UpcomingTrigger -> "Pencetus berikut hari ini"
    AlmanacIntervalOccurrence.Unresolved -> "Masa belum ditentukan"
}

internal fun productionOpenKind(interval: AlmanacInterval): String = when {
    interval.visual == com.bmuslim.app.ui.almanac.AlmanacIntervalVisual.SearchEnvelope -> "Buka kawasan carian"
    interval.truthClass == com.bmuslim.app.ui.almanac.AlmanacIntervalTruthClass.ScheduleContextProxy -> "Buka penanda"
    else -> "Buka amalan"
}

internal fun productionIntervalAnnotation(interval: AlmanacInterval, minute: Int, overlapCount: Int): String {
    if (overlapCount > 1) return "$overlapCount amalan bertemu di sini"
    val anchor = formatMinute(intervalAnchorMinute(interval))
    val timing = interval.timingLabel?.takeIf(String::isNotBlank)
    val compactTiming = when {
        timing == null -> anchor
        timing.contains(anchor) -> timing
        else -> "$timing · $anchor"
    }
    return listOfNotNull(
        productionOccurrenceLabel(intervalOccurrence(interval, minute)),
        intervalTitles(listOf(interval), minute).joinToString("; ").takeIf(String::isNotBlank),
        compactTiming,
    ).joinToString(" · ")
}

internal fun productionRelatedCount(count: Int): String = "$count amalan bertemu di sini"

internal fun intervalItemCount(intervals: List<AlmanacInterval>, minute: Int? = null): Int = intervals.sumOf { interval ->
    minute?.let(interval::memberCountAt) ?: interval.memberCount
}

internal fun intervalTitles(intervals: List<AlmanacInterval>, minute: Int? = null): List<String> = intervals
    .flatMap { interval -> minute?.let(interval::memberTitlesAt) ?: interval.memberTitles.ifEmpty { listOfNotNull(interval.title) } }
    .distinct()

internal fun productionWheelStateDescription(
    selection: AlmanacSelection,
    minute: Int,
    selectedTimeOwned: Boolean = true,
): String = buildString {
    append(if (selectedTimeOwned) "Masa dipilih " else "Sekarang ")
    append("${formatMinute(minute)}. ")
    selection.exact?.let { append(it.accessibilityText).append(".") } ?: run {
        if (selection.dayComplete) append("Jadual hari ini selesai.")
        else selection.next?.let { append("${productionPointCategory(it, upcoming = true)} pada ${formatMinute(it.minute)}.") }
    }
}

internal fun productionPointReadout(selection: AlmanacSelection, minute: Int): Pair<String, String?> = when {
    selection.exact != null -> {
        "${formatMinute(minute)} dipilih · ${productionPointCategory(selection.exact, upcoming = false)}" to null
    }
    selection.dayComplete -> "${formatMinute(minute)} dipilih" to "Jadual hari ini selesai"
    selection.next != null -> "${formatMinute(minute)} dipilih" to
        "Seterusnya · ${selection.next.title} · ${formatMinute(selection.next.minute)}"
    else -> "${formatMinute(minute)} dipilih" to "Takwim hari ini"
}

internal fun productionPointCategory(point: AlmanacPoint, upcoming: Boolean): String {
    val id = point.id.lowercase(Locale.ROOT)
    val prefix = when {
        point.role == AlmanacVisualRole.Primary -> if (upcoming) "Waktu berikutnya" else "Masuk waktu"
        id == "dhuha" -> if (upcoming) "Waktu Dhuha berikutnya" else "Waktu Dhuha"
        else -> if (upcoming) "Penanda berikutnya" else "Penanda"
    }
    return if (id == "dhuha") prefix else "$prefix ${point.title}"
}

internal data class ProductionPointSummary(
    val eyebrow: String,
    val title: String,
    val value: String,
    val complete: Boolean = false,
)

internal fun productionPointSummary(selection: AlmanacSelection): ProductionPointSummary {
    val point = selection.exact ?: selection.next
    if (point == null) return ProductionPointSummary("TAKWIM RASMI", "Jadual hari ini selesai", "—", complete = true)
    val id = point.id.lowercase(Locale.ROOT)
    val eyebrow = when {
        point.role == AlmanacVisualRole.Primary -> if (selection.exact != null) "MASUK WAKTU" else "SOLAT BERIKUT"
        id == "dhuha" -> "WAKTU DHUHA"
        else -> "PENANDA TAKWIM"
    }
    return ProductionPointSummary(eyebrow, point.title, formatMinute(point.minute))
}

@Composable
private fun ProductionScheduleSummary(snapshot: SharedAlmanacSnapshot, modifier: Modifier) {
    val focus = productionPointSummary(snapshot.selection)
    Surface(color = PaperBright, contentColor = PaperInk) {
        Column(
            modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                heading()
                contentDescription = productionSelectionDescription(snapshot.selection)
            }.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(focus.eyebrow, color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
            Text(focus.title, color = Pine, fontFamily = FontFamily.Serif, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            if (!focus.complete) Text(focus.value, color = PaperInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            if (snapshot.selection.exact != null) snapshot.selection.next?.let {
                Text("Seterusnya · ${productionPointCategory(it, upcoming = true)} · ${formatMinute(it.minute)}", color = PaperMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProductionScheduleFolio(
    schedule: JakimAlmanacSchedule,
    snapshot: SharedAlmanacSnapshot,
    internallyScrollable: Boolean,
    modifier: Modifier,
) {
    if (showResearchPreviewDemoLayout()) {
        ResearchPreviewInlineFolio(
            schedule,
            snapshot,
            internallyScrollable,
            modifier.fillMaxWidth(),
        )
        return
    }
    Surface(modifier.fillMaxWidth(), color = Paper, contentColor = PaperInk, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)) {
        Column(
            Modifier.fillMaxSize().then(if (internallyScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (showScheduleRowsInFolio()) {
                Text("Jadual hari ini", color = Pine, fontFamily = FontFamily.Serif, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Bulatan berisi · solat fardu    Bulatan kosong · penanda takwim", color = PaperMuted, fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .18f)))
                schedule.values.forEach { value ->
                    val primary = value.slot.category == JakimAlmanacCategory.OBLIGATORY_PRAYER
                    val classification = productionScheduleClassification(value.slot)
                    Row(
                        Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                            contentDescription = "${value.slot.displayName}, ${value.time.toHm()}, ${classification.lowercase()}"
                        }.padding(vertical = 3.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(value.slot.displayName, fontFamily = FontFamily.Serif, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(classification, color = if (primary) Pine else PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Text(value.time.toHm(), color = if (snapshot.selection.exact?.id == value.slot.name.lowercase(Locale.ROOT)) Saffron else PaperInk, fontFamily = FontFamily.Monospace, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .18f)))
                Text("PENGESAHAN JADUAL", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                Text("${schedule.exactKey.zoneCode} · ${schedule.exactKey.civilDate} · JAKIM e-Solat", color = PaperInk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            ResearchPreviewInlineFolio(
                schedule,
                snapshot,
                internallyScrollable,
                Modifier.fillMaxWidth(),
            )
        }
    }
}

internal fun productionScheduleClassification(slot: JakimAlmanacSlot): String = when {
    slot.category == JakimAlmanacCategory.OBLIGATORY_PRAYER -> "SOLAT FARDU"
    slot == JakimAlmanacSlot.DHUHA -> "WAKTU DHUHA"
    else -> "PENANDA TAKWIM"
}

internal fun productionSelectionDescription(selection: AlmanacSelection): String = buildString {
    val focus = productionPointSummary(selection)
    append(focus.eyebrow.lowercase().replaceFirstChar(Char::uppercase))
    append(" ").append(focus.title)
    if (!focus.complete) append(" pada ").append(focus.value)
    append(".")
    if (selection.exact != null) selection.next?.let {
        append(" Seterusnya ").append(productionPointCategory(it, upcoming = true)).append(" pada ").append(formatMinute(it.minute)).append(".")
    }
}

internal fun currentPhoneMinute(clock: Clock? = null): Int =
    java.time.LocalTime.now(clock ?: Clock.systemDefaultZone()).let { it.hour * 60 + it.minute }

internal fun millisUntilNextPhoneMinute(clock: Clock? = null): Long {
    val epochMillis = (clock ?: Clock.systemDefaultZone()).instant().toEpochMilli()
    val elapsedInMinute = Math.floorMod(epochMillis, 60_000L)
    return (60_000L - elapsedInMinute).coerceIn(1L, 60_000L)
}

internal fun java.time.LocalTime.toHm(): String = "%02d:%02d".format(hour, minute)
