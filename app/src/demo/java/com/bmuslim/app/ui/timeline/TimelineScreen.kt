package com.bmuslim.app.ui.timeline

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmuslim.app.ui.almanac.AlmanacCopy
import com.bmuslim.app.ui.almanac.AlmanacHeader
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacPoint
import com.bmuslim.app.ui.almanac.AlmanacPresentation
import com.bmuslim.app.ui.almanac.AlmanacSelection
import com.bmuslim.app.ui.almanac.AlmanacSelectionPolicy
import com.bmuslim.app.ui.almanac.AlmanacVisualRole
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.DemoAlmanacLedger
import com.bmuslim.app.ui.almanac.DemoAlmanacMomentSummary
import com.bmuslim.app.ui.almanac.DemoLedgerContent
import com.bmuslim.app.ui.almanac.DemoLedgerSection
import com.bmuslim.app.ui.almanac.DemoLedgerSectionStyle
import com.bmuslim.app.ui.almanac.DemoMomentSummaryContent
import com.bmuslim.app.ui.almanac.DemoSummaryCellContent
import com.bmuslim.app.ui.almanac.SharedAlmanacModel
import com.bmuslim.app.ui.almanac.SharedAlmanacShell
import com.bmuslim.app.ui.almanac.SharedAlmanacSnapshot
import com.bmuslim.app.ui.almanac.formatMinute
import com.bmuslim.app.ui.theme.BMuslimTheme

private const val DemoNowMinute = 15 * 60
private val Pine = Color(0xFF0B4938)
private val Paper = Color(0xFFF4F2E9)
private val PaperBright = Color(0xFFFAF8F0)
private val PaperInk = Color(0xFF102A21)
private val PaperMuted = Color(0xFF5B7067)

@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    initialMinute: Int = DemoNowMinute,
    initialPresentation: HomePresentation = HomePresentation.Glance,
) {
    val deeds = remember { sampleDeeds() }
    val dayState = remember { sampleDayStates().first() }
    var completedIds by rememberSaveable(stateSaver = listSaver(save = { it }, restore = { it })) {
        mutableStateOf(emptyList<String>())
    }
    val model = remember(deeds, initialMinute) { demoAlmanacModel(deeds, initialMinute) }
    SharedAlmanacShell(
        model = model,
        modifier = modifier,
        initialPresentation = if (initialPresentation == HomePresentation.Detail) AlmanacPresentation.Folio else AlmanacPresentation.Glance,
        summary = { snapshot, summaryModifier ->
            DemoMomentSummary(snapshot, deeds, dayState, summaryModifier)
        },
        body = { snapshot, internallyScrollable, bodyModifier ->
            val active = activeDeedsAt(deeds, snapshot.selectedMinute)
            val primary = active.firstOrNull() ?: nextDeedAt(deeds, snapshot.selectedMinute)
            DemoLedger(
                deed = primary,
                active = primary in active,
                done = primary.id in completedIds,
                compact = snapshot.compact,
                internallyScrollable = internallyScrollable,
                onToggle = {
                    completedIds = if (primary.id in completedIds) completedIds - primary.id else completedIds + primary.id
                },
                modifier = bodyModifier,
            )
        },
    )
}

internal fun demoAlmanacModel(deeds: List<TimelineDeed>, initialMinute: Int): SharedAlmanacModel {
    val points = deeds.filter { it.ruling == Ruling.Fardh }.map {
        AlmanacPoint(
            id = it.id,
            title = it.title.removePrefix("Solat "),
            minute = it.startMinute,
            role = AlmanacVisualRole.Primary,
            accessibilityText = "Waktu ${it.title.removePrefix("Solat ")} pada ${formatMinute(it.startMinute)}",
        )
    }
    val contextual = deeds.filter { it.ruling != Ruling.Fardh }
    val lanes = assignDemoDeedLanes(contextual)
    val intervals = contextual.map {
        AlmanacInterval(it.id, it.startMinute, it.endMinute, it.actMinute, it.ruling == Ruling.Muakkadah, lanes.getValue(it.id))
    }
    val policy = AlmanacSelectionPolicy { _, minute ->
        val prayer = obligatoryPrayerContextAt(deeds, minute)
        val previous = points.lastOrNull { it.minute <= minute }
        AlmanacSelection(previous, points.firstOrNull { it.id == prayer.id }, points.firstOrNull { it.minute == minute }, false)
    }
    return SharedAlmanacModel(
        modelKey = "demo-stable-fixture",
        header = AlmanacHeader("bMuslim", "Khamis, 2 Julai 2026", "17 Muharram 1448 H") { minute, _ -> dayPart(minute) },
        points = points,
        intervals = intervals,
        initialMinute = quantizeCircularMinute(initialMinute),
        actualNowMinute = DemoNowMinute,
        followsNow = false,
        minuteStep = 5,
        sunriseMinute = 6 * 60,
        sunsetMinute = 19 * 60 + 29,
        copy = AlmanacCopy(
            wheelDescription = "Roda waktu 24 jam. Leret mendatar atau laras untuk memilih masa.",
            wheelHint = "Leret roda kiri atau kanan untuk pilih waktu",
            openLabel = { "Amalan pada ${formatMinute(it)}" },
            openDescription = { minute -> openActionDescription(activeDeedsAt(deeds, minute), nextDeedAt(deeds, minute)) },
            closeLabel = "Tutup amalan",
            closeDescription = "Tutup amalan dan kembali ke roda penuh",
            closedAnnouncement = "Paparan amalan ditutup",
            returnToNow = "Kembali ke sekarang",
            todayLabel = "Hari ini",
            plannerLabel = "Perancang",
            settingsLabel = "Tetapan",
            unavailableLabel = "belum tersedia",
        ),
        selectionPolicy = policy,
        wheelReadout = { _, minute, direct ->
            wheelReadoutAt(deeds, minute, direct)?.let { it.primary to it.secondary }
                ?: ("${formatMinute(minute)} dipilih" to null)
        },
        wheelStateDescription = { _, minute, _ -> wheelStateDescription(deeds, minute) },
    )
}

internal fun assignDemoDeedLanes(deeds: List<TimelineDeed>): Map<String, Int> {
    val laneEnds = mutableListOf<Int>()
    val result = mutableMapOf<String, Int>()
    deeds.sortedWith(compareBy<TimelineDeed> { it.startMinute }.thenBy { it.ruling.priority }.thenBy { it.id }).forEach { deed ->
        val reusable = laneEnds.indexOfFirst { deed.startMinute >= it }
        val lane = if (reusable >= 0) reusable else laneEnds.size
        if (reusable >= 0) laneEnds[lane] = deed.endMinute else laneEnds += deed.endMinute
        result[deed.id] = lane
    }
    return result
}

@Composable
private fun DemoMomentSummary(
    snapshot: SharedAlmanacSnapshot,
    deeds: List<TimelineDeed>,
    dayState: DayState,
    modifier: Modifier,
) {
    val active = activeDeedsAt(deeds, snapshot.selectedMinute)
    val prayer = obligatoryPrayerContextAt(deeds, snapshot.selectedMinute)
    val contextual = contextualDeedAt(deeds, snapshot.selectedMinute)
    val prayerActive = prayer.isActiveAt(snapshot.selectedMinute)
    val contextualActive = contextual.isActiveAt(snapshot.selectedMinute)
    val contextualActiveList = active.filter { it.ruling != Ruling.Fardh }
    DemoAlmanacMomentSummary(
        snapshot = snapshot,
        content = DemoMomentSummaryContent(
            accessibilityText = momentSummaryDescription(snapshot.selectedMinute, active, prayer, contextual, dayState),
            label = "RINGKASAN SAAT INI",
            first = DemoSummaryCellContent(
                if (prayerActive) "SOLAT AKTIF" else "SOLAT BERIKUT",
                prayer.title.removePrefix("Solat "),
                formatMinute(prayer.startMinute),
            ),
            second = DemoSummaryCellContent(
                if (contextualActive) "AMALAN AKTIF" else "SETERUSNYA",
                contextual.title,
                if (contextualActive) "hingga ${formatMinute(contextual.endMinute)}" else formatMinute(contextual.startMinute),
            ),
            third = DemoSummaryCellContent("HARI INI", dayState.title, dayState.ruling.label),
            footerEyebrow = "AKTIF PADA PENUNJUK",
            footerText = if (contextualActiveList.isEmpty()) {
                "Tiada amalan berwaktu pada ${formatMinute(snapshot.selectedMinute)}"
            } else {
                contextualActiveList.joinToString("  /  ") { "${it.title} hingga ${formatMinute(it.endMinute)}" }
            },
            footerCount = contextualActiveList.size,
            maximumPrimaryTitle = if (prayerActive) prayer.title.removePrefix("Solat ") else "Solat berikut · ${prayer.title.removePrefix("Solat ")}",
            maximumPrimaryValue = if (prayerActive) "Waktu ${prayer.title.removePrefix("Solat ")} · ${formatMinute(snapshot.selectedMinute)}" else formatMinute(prayer.startMinute),
            maximumSecondaryText = if (contextualActive) "Amalan berkaitan · ${contextual.title}" else "Amalan berikut · ${contextual.title}",
            maximumFooterText = "Khamis, 2 Julai 2026 · 17 Muharram 1448 H · ${dayState.title}",
        ),
        modifier = modifier,
    )
}

@Composable
private fun DemoLedger(
    deed: TimelineDeed,
    active: Boolean,
    done: Boolean,
    compact: Boolean,
    internallyScrollable: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier,
) {
    val completion by animateFloatAsState(if (done) 1f else 0f, tween(220), label = "demo completion")
    val sections = buildList {
        add(DemoLedgerSection("PANDUAN RINGKAS", deed.description))
        add(DemoLedgerSection(text = archetypeLabel(deed), style = DemoLedgerSectionStyle.Muted))
        deed.arabic?.let { add(DemoLedgerSection(text = it, style = DemoLedgerSectionStyle.Arabic)) }
        deed.translation?.let { add(DemoLedgerSection(text = it, style = DemoLedgerSectionStyle.Translation)) }
    }
    DemoAlmanacLedger(
        content = DemoLedgerContent(
            accessibilityText = "${deed.title}. ${deed.description}. Sumber ${deed.citation}. Gred ${deed.grade}.",
            eyebrow = if (active) "AMALAN UTAMA SEKARANG" else "AMALAN SETERUSNYA",
            timing = "${formatMinute(deed.startMinute)} hingga ${formatMinute(deed.endMinute)}",
            status = if (done) "SELESAI" else deed.windowLabel.uppercase(),
            title = deed.title,
            classification = "${deed.type.label}  /  ${deed.ruling.label}",
            sections = sections,
            footerLeftLabel = "SUMBER",
            footerLeftValue = deed.citation,
            footerRightLabel = "GRED HADIS",
            footerRightValue = deed.grade,
        ),
        compact = compact,
        internallyScrollable = internallyScrollable,
        modifier = modifier,
        action = { DemoCompletionAction(deed, done, completion, onToggle) },
    )
}

@Composable
private fun DemoCompletionAction(deed: TimelineDeed, done: Boolean, completion: Float, onToggle: () -> Unit) {
    Button(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().height(52.dp).semantics { role = Role.Checkbox; contentDescription = "Tandai ${deed.title} selesai"; stateDescription = if (done) "Selesai" else "Belum selesai" },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Pine),
                border = BorderStroke(1.dp, Pine.copy(alpha = .38f + completion * .42f)),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
    ) { Text(if (done) "Ditandai selesai" else "Tandai selesai", fontWeight = FontWeight.ExtraBold) }
}

private fun archetypeLabel(deed: TimelineDeed): String = when (deed.archetype) {
    RenderArchetype.Point -> "Tindakan sekitar ${deed.actMinutes} minit dalam ruang masa ini"
    RenderArchetype.Window -> "Boleh diamalkan sepanjang ruang masa ini"
    RenderArchetype.DaySpan -> "Amalan sepanjang hari"
    RenderArchetype.Anytime -> "Boleh dilakukan bila-bila masa"
}

internal fun wheelStateDescription(deeds: List<TimelineDeed>, minute: Int): String {
    val active = activeDeedsAt(deeds, minute)
    val prayer = obligatoryPrayerContextAt(deeds, minute)
    val contextual = active.filter { it.ruling != Ruling.Fardh }
    val prayerText = if (prayer.isActiveAt(minute)) "Waktu ${prayer.title.removePrefix("Solat ")}" else "Solat berikut ${prayer.title.removePrefix("Solat ")} pada ${formatMinute(prayer.startMinute)}"
    val contextualText = if (contextual.isEmpty()) "Tiada amalan berkaitan yang aktif" else "Amalan berkaitan ${contextual.joinToString { it.title }}"
    return "Masa dipilih ${formatMinute(minute)}. $prayerText. $contextualText."
}

internal fun openActionDescription(active: List<TimelineDeed>, next: TimelineDeed): String =
    "Buka butiran amalan. ${active.firstOrNull()?.title ?: "amalan berikut ${next.title}"}."

private fun momentSummaryDescription(minute: Int, active: List<TimelineDeed>, prayer: TimelineDeed, contextual: TimelineDeed, dayState: DayState): String {
    val contextualActive = active.filter { it.ruling != Ruling.Fardh }
    val activeText = if (contextualActive.isEmpty()) "Amalan berikut ${contextual.title}" else "Amalan berkaitan ${contextualActive.joinToString { it.title }}"
    val prayerText = if (prayer.isActiveAt(minute)) "Waktu ${prayer.title.removePrefix("Solat ")}" else "Solat berikut ${prayer.title.removePrefix("Solat ")} pada ${formatMinute(prayer.startMinute)}"
    return "Masa dipilih ${formatMinute(minute)}. $prayerText. $activeText. Keadaan hari: ${dayState.title}."
}

private fun dayPart(minute: Int): String = when (com.bmuslim.app.ui.almanac.normalizeMinute(minute)) {
    in 0 until 5 * 60 + 59 -> "Malam, sebelum Subuh"
    in 5 * 60 + 59 until 7 * 60 + 11 -> "Subuh, menuju Syuruk"
    in 7 * 60 + 11 until 13 * 60 + 21 -> "Pagi, selepas Syuruk"
    in 13 * 60 + 21 until 16 * 60 + 45 -> "Petang, sebelum Asar"
    in 16 * 60 + 45 until 19 * 60 + 29 -> "Petang, menuju Maghrib"
    in 19 * 60 + 29 until 20 * 60 + 42 -> "Senja, menuju Isyak"
    else -> "Malam, selepas Isyak"
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun TimelinePreview() { BMuslimTheme { TimelineScreen() } }

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 640, fontScale = 2f)
@Composable
private fun TimelineMaximumPreview() { BMuslimTheme { TimelineScreen(initialPresentation = HomePresentation.Detail) } }
