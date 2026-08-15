package com.bmuslim.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import com.bmuslim.app.prayer.JakimAlmanacSchedule
import com.bmuslim.app.research.ResearchCadence
import com.bmuslim.app.research.ResearchCatalogRepository
import com.bmuslim.app.research.ResearchCatalogResult
import com.bmuslim.app.research.ResearchConcordanceItem
import com.bmuslim.app.research.ResearchDeed
import com.bmuslim.app.research.ResearchEvidence
import com.bmuslim.app.research.ResearchPointerSelection
import com.bmuslim.app.research.ResearchPointerConcordance
import com.bmuslim.app.research.ResearchPreviewDialog
import com.bmuslim.app.research.ResearchTimelinePlan
import com.bmuslim.app.research.researchConcordanceCollisionCopy
import com.bmuslim.app.research.researchPointerConcordance
import com.bmuslim.app.research.researchTimelinePlan
import com.bmuslim.app.ui.almanac.AlmanacInterval
import com.bmuslim.app.ui.almanac.AlmanacFontComposition
import com.bmuslim.app.ui.almanac.AlmanacIntervalTruthClass
import com.bmuslim.app.ui.almanac.AlmanacIntervalVisual
import com.bmuslim.app.ui.almanac.DemoAlmanacLedger
import com.bmuslim.app.ui.almanac.DemoAlmanacMomentSummary
import com.bmuslim.app.ui.almanac.DemoLedgerContent
import com.bmuslim.app.ui.almanac.DemoLedgerSection
import com.bmuslim.app.ui.almanac.DemoLedgerSectionStyle
import com.bmuslim.app.ui.almanac.DemoMomentSummaryContent
import com.bmuslim.app.ui.almanac.DemoSummaryCellContent
import com.bmuslim.app.ui.almanac.SharedAlmanacSnapshot
import com.bmuslim.app.ui.almanac.formatMinute
import com.bmuslim.app.ui.almanac.intervalAnchorMinute
import com.bmuslim.app.ui.almanac.intervalOccurrence
import java.time.DayOfWeek

private val ResearchPreviewPine = Color(0xFF0B4938)

internal fun showScheduleRowsInFolio(): Boolean = false
internal fun showResearchPreviewDemoLayout(): Boolean = true

@Composable
internal fun ResearchPreviewAction(
    onOpen: () -> Unit,
    modifier: Modifier,
) {
    TextButton(
        onClick = onOpen,
        modifier = modifier.heightIn(min = 48.dp).testTag("deeds-preview-open"),
        border = BorderStroke(1.dp, ResearchPreviewPine.copy(alpha = .36f)),
    ) {
        Text("Pustaka penyelidikan · 11 calon", color = ResearchPreviewPine, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun ResearchPreviewDestination(onBack: () -> Unit) {
    val context = LocalContext.current
    val result = remember(context) { ResearchCatalogRepository().load(context.assets) }
    ResearchPreviewDialog(
        result = result,
        onBack = onBack,
        onOpenSource = { url -> openResearchSource(context, url) },
    )
}

@Composable
internal fun rememberResearchPreviewIntervals(
    schedule: JakimAlmanacSchedule,
    composition: AlmanacFontComposition,
): List<AlmanacInterval> {
    val context = LocalContext.current
    val result = remember(context) { ResearchCatalogRepository().load(context.assets) }
    return remember(result, schedule, composition) {
        val catalog = (result as? ResearchCatalogResult.Ready)?.catalog
        if (catalog == null) emptyList() else researchTimelinePlan(catalog, schedule, composition = composition).intervals
    }
}

@Composable
internal fun ResearchPreviewMomentSummary(
    schedule: JakimAlmanacSchedule,
    snapshot: SharedAlmanacSnapshot,
    modifier: Modifier,
) {
    val point = productionPointSummary(snapshot.selection)
    val loaded = rememberResearchUi(schedule, snapshot.selectedMinute, snapshot.fontComposition)
    val concordance = loaded?.concordance
    val exactCount = concordance?.exactItemCount ?: 0
    val collisionCopy = concordance?.let(::researchConcordanceCollisionCopy)
        ?: "Pratonton penyelidikan tidak tersedia"
    val dayLabels = concordance?.dayItems?.map { it.timingLabel }.orEmpty()
    val dayTitle = when {
        dayLabels.isEmpty() -> "Tiada konteks hari khusus"
        dayLabels.size == 1 -> "1 konteks hari ini"
        else -> "${dayLabels.size} konteks hari ini"
    }
    val dayValue = dayLabels.joinToString(" · ").ifBlank { malayDayName(schedule.exactKey.civilDate.dayOfWeek) }
    val relatedTitles = concordance?.exactItems?.map { it.deed.title }?.distinct().orEmpty()
    val untimedStatus = loaded?.plan?.let(::researchUntimedContextSummary)
        ?: "Pratonton penyelidikan tidak tersedia"
    val accessibility = buildString {
        append("${point.eyebrow.lowercase()} ${point.title}")
        if (!point.complete) append(" pada ${point.value}")
        append(". $collisionCopy.")
        if (relatedTitles.isNotEmpty()) append(" Ahli: ${relatedTitles.joinToString("; ")}.")
        if (loaded != null) append(" ${researchUntimedContextSummary(loaded.plan)}.")
        else append(" Pratonton penyelidikan tidak tersedia.")
    }
    DemoAlmanacMomentSummary(
        snapshot = snapshot,
        content = DemoMomentSummaryContent(
            accessibilityText = accessibility,
            label = "KONKORDANS SAAT INI · DRAF",
            first = DemoSummaryCellContent(point.eyebrow, point.title, point.value),
            second = DemoSummaryCellContent(
                if (exactCount > 0) "$exactCount AMALAN" else "AMALAN TEPAT",
                if (exactCount > 0) "$exactCount amalan bertemu di sini" else "Tiada amalan tepat",
                collisionCopy,
            ),
            third = DemoSummaryCellContent("HARI INI", dayTitle, dayValue),
            footerEyebrow = "AHLI TEPAT PADA PENUNJUK",
            footerText = relatedTitles.joinToString("  /  ").ifBlank { "Tiada jalur draf tepat pada ${formatMinute(snapshot.selectedMinute)}" },
            footerCount = exactCount,
            maximumPrimaryTitle = "${point.eyebrow.lowercase().replaceFirstChar { it.uppercaseChar() }} · ${point.title}",
            maximumPrimaryValue = point.value,
            maximumSecondaryText = collisionCopy,
            maximumFooterText = "${schedule.exactKey.civilDate} · ${schedule.exactKey.zoneCode} · $dayTitle · $untimedStatus",
        ),
        modifier = modifier.semantics { heading() },
    )
}

@Composable
internal fun ResearchPreviewInlineFolio(
    schedule: JakimAlmanacSchedule,
    snapshot: SharedAlmanacSnapshot,
    internallyScrollable: Boolean,
    modifier: Modifier = Modifier,
) {
    val loaded = rememberResearchUi(schedule, snapshot.selectedMinute, snapshot.fontComposition)
    if (loaded == null) {
        DemoAlmanacLedger(
            content = DemoLedgerContent(
                accessibilityText = "Pratonton draf tidak tersedia kerana fail penyelidikan gagal disahkan.",
                eyebrow = "DRAF PENYELIDIKAN",
                timing = "Tidak tersedia",
                status = "GAGAL DISAHKAN",
                title = "Pratonton tidak tersedia",
                classification = "aMuslim  /  Dalam semakan",
                sections = listOf(DemoLedgerSection("STATUS", "Fail penyelidikan tidak dapat disahkan.")),
                footerLeftLabel = "SUMBER",
                footerLeftValue = "Tiada",
                footerRightLabel = "STATUS",
                footerRightValue = "Ditolak",
            ),
            compact = snapshot.compact,
            internallyScrollable = internallyScrollable,
            modifier = modifier,
        )
        return
    }
    var selectedIntervalId by remember(schedule.exactKey) { mutableStateOf<String?>(null) }
    var returnFocusId by remember(schedule.exactKey) { mutableStateOf<String?>(null) }
    val rowFocusRequesters = remember(schedule.exactKey) { mutableMapOf<String, FocusRequester>() }
    fun closeLedger() {
        selectedIntervalId?.let { selectedId ->
            returnFocusId = selectedId
            // A requester detached with the ledger swap can no longer own the newly composed row.
            rowFocusRequesters.remove(selectedId)
        }
        selectedIntervalId = null
    }
    BackHandler(enabled = selectedIntervalId != null, onBack = ::closeLedger)
    if (selectedIntervalId == null) {
        ResearchConcordanceFolio(
            concordance = loaded.concordance,
            internallyScrollable = internallyScrollable,
            focusRequesters = rowFocusRequesters,
            focusReturnId = returnFocusId,
            onFocusReturned = { returnFocusId = null },
            onSelect = { id -> selectedIntervalId = id },
            modifier = modifier,
        )
        return
    }
    val item = loaded.concordance.sections.flatMap { it.items }.firstOrNull { it.id == selectedIntervalId }
    if (item == null) {
        selectedIntervalId = null
        return
    }
    val evidence = item.deed.evidence
    DemoAlmanacLedger(
        content = DemoLedgerContent(
            accessibilityText = researchLedgerAccessibility(item),
            eyebrow = researchItemOccurrenceLabel(item, snapshot.selectedMinute).uppercase(),
            timing = item.timingLabel,
            status = mappingBadge(item.truthClass),
            title = item.deed.title,
            classification = "${cadenceLabel(item.deed)}  /  DALAM SEMAKAN",
            sections = listOf(
                DemoLedgerSection("TINDAKAN DALAM DRAF", item.deed.instruction),
                DemoLedgerSection("FRASA WAKTU SUMBER", item.deed.sourceTimePhrase),
                DemoLedgerSection(text = mappingSentence(item), style = DemoLedgerSectionStyle.Muted),
            ),
            footerLeftLabel = "SUMBER",
            footerLeftValue = evidence.joinToString(" · ") { "${it.collection} ${it.reference}" },
            footerRightLabel = "PENILAIAN DIREKODKAN",
            footerRightValue = evidence.joinToString(" · ", transform = ::assessmentText),
        ),
        compact = snapshot.compact,
        internallyScrollable = internallyScrollable,
        modifier = modifier.fillMaxWidth().testTag("research-demo-ledger"),
        action = {
            TextButton(
                onClick = ::closeLedger,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("research-concordance-back"),
            ) { Text("Kembali ke konkordans") }
        },
    )
}

@Composable
private fun ResearchConcordanceFolio(
    concordance: ResearchPointerConcordance,
    internallyScrollable: Boolean,
    focusRequesters: MutableMap<String, FocusRequester>,
    focusReturnId: String?,
    onFocusReturned: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier,
) {
    LaunchedEffect(focusReturnId, concordance) {
        val focusId = focusReturnId ?: return@LaunchedEffect
        if (concordance.sections.none { section -> section.items.any { it.id == focusId } }) {
            onFocusReturned()
            return@LaunchedEffect
        }
        repeat(3) {
            withFrameNanos { }
            focusRequesters[focusId]?.requestFocus()
        }
        onFocusReturned()
    }
    Surface(modifier.fillMaxWidth(), color = Color(0xFFF4F2E9), contentColor = Color(0xFF102A21)) {
        Column(
            Modifier.fillMaxWidth()
                .then(if (internallyScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                "KONKORDANS NEUTRAL",
                color = Color(0xFF5B7067),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = .7.sp,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                "${concordance.exactItemCount} amalan tepat pada ${formatMinute(concordance.minute)}",
                color = ResearchPreviewPine,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 5.dp, bottom = 12.dp),
            )
            concordance.sections.forEach { section ->
                Text(
                    section.label,
                    color = Color(0xFF5B7067),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .6.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                )
                if (section.items.isEmpty()) {
                    Text(
                        "Tiada item dalam bahagian ini.",
                        color = Color(0xFF5B7067),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else section.items.forEach { item ->
                    val rowFocusRequester = focusRequesters.getOrPut(item.id) { FocusRequester() }
                    val isDayRibbon = section.section == com.bmuslim.app.research.ResearchConcordanceSection.HARI_INI
                    TextButton(
                        onClick = { onSelect(item.id) },
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = if (isDayRibbon) 48.dp else 56.dp)
                            .focusRequester(rowFocusRequester)
                            .focusable()
                            .semantics { contentDescription = researchConcordanceAccessibility(item) }
                            .testTag(if (isDayRibbon) "research-day-ribbon-${item.id}" else "research-concordance-${item.id}"),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 0.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.deed.title, color = ResearchPreviewPine, fontWeight = FontWeight.Bold)
                                Text(
                                    listOfNotNull(
                                        mappingBadge(item.truthClass),
                                        item.visual?.let(::researchVisualLabel),
                                        item.occurrence.applicability.basis,
                                    ).joinToString(" · "),
                                    color = Color(0xFF5B7067),
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                )
                            }
                            Text(item.timingLabel, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 9.sp)
                        }
                    }
                    Spacer(Modifier.fillMaxWidth().height(1.dp).background(ResearchPreviewPine.copy(alpha = .18f)))
                }
            }
        }
    }
}

internal fun researchOptionAccessibility(pointer: ResearchPointerSelection, minute: Int): String = buildString {
    val presentation = pointer.presentation
    val memberInterval = pointer.interval.copy(
        visual = presentation?.visual ?: pointer.interval.visual,
        truthClass = presentation?.truthClass ?: pointer.occurrence.truthClass,
        timingLabel = presentation?.timingLabel ?: pointer.interval.timingLabel,
    )
    append(pointer.deed.title).append(". ")
    append(productionOccurrenceLabel(intervalOccurrence(memberInterval, minute))).append(". ")
    append(presentation?.timingLabel ?: formatMinute(intervalAnchorMinute(pointer.interval)))
    append(". Applicability ${presentation?.applicability?.basis ?: pointer.occurrence.applicability.basis}: ")
    append(presentation?.applicability?.value ?: pointer.occurrence.applicability.value)
    append(". Occurrence ${pointer.occurrence.id}; completion ${pointer.occurrence.completionId}. Buka butiran draf.")
}

internal fun researchConcordanceAccessibility(item: ResearchConcordanceItem): String = buildString {
    append(item.deed.title).append(". ")
    append(item.section.name.replace('_', ' ')).append(". ")
    append(item.timingLabel).append(". ")
    append(mappingBadge(item.truthClass)).append(". ")
    item.visual?.let { append("${researchVisualLabel(it)}. ") }
    append("Applicability ${item.applicability.basis}: ${item.applicability.value}. ")
    append("Occurrence ${item.occurrence.id}; completion ${item.occurrence.completionId}.")
    item.visual?.let { append(" Visual ${it.name}.") }
    append(" Buka butiran draf.")
}

internal fun researchUntimedContextSummary(plan: ResearchTimelinePlan): String = buildList {
    if (plan.personalEvents.isNotEmpty()) add("${plan.personalEvents.size} peristiwa peribadi kekal tersedia")
    if (plan.dayContexts.isNotEmpty()) add("${plan.dayContexts.size} konteks hari kekal tersedia")
}.joinToString(". ").ifBlank { "Tiada konteks tak berjam direkodkan dalam draf ini" }

private data class LoadedResearchUi(
    val catalog: com.bmuslim.app.research.ResearchCatalog,
    val plan: ResearchTimelinePlan,
    val concordance: ResearchPointerConcordance,
)

@Composable
private fun rememberResearchUi(
    schedule: JakimAlmanacSchedule,
    minute: Int,
    composition: AlmanacFontComposition,
): LoadedResearchUi? {
    val context = LocalContext.current
    val result = remember(context) { ResearchCatalogRepository().load(context.assets) }
    return remember(result, schedule, minute, composition) {
        val catalog = (result as? ResearchCatalogResult.Ready)?.catalog ?: return@remember null
        val plan = researchTimelinePlan(catalog, schedule, composition = composition)
        LoadedResearchUi(catalog, plan, researchPointerConcordance(catalog, plan, minute))
    }
}

private fun cadenceLabel(deed: ResearchDeed): String =
    when (deed.cadence) {
        ResearchCadence.DAILY_CONTEXT -> "Harian"
        ResearchCadence.WEEKLY_CONTEXT -> "Khusus hari"
        ResearchCadence.MONTHLY_CONTEXT -> "Konteks bulanan"
    }

private fun mappingBadge(truthClass: AlmanacIntervalTruthClass): String = when (truthClass) {
    AlmanacIntervalTruthClass.ScheduleContextProxy -> "PROKSI JADUAL"
    AlmanacIntervalTruthClass.EventRequired -> "PERISTIWA PENGGUNA"
    AlmanacIntervalTruthClass.Unresolved -> "BELUM DIPUTUSKAN"
    AlmanacIntervalTruthClass.ProvisionalResearch -> "BELUM DISAHKAN"
    AlmanacIntervalTruthClass.Direct -> "FRASA LANGSUNG"
    AlmanacIntervalTruthClass.PolicyMapped -> "DASAR PEMETAAN"
}

private fun researchVisualLabel(visual: AlmanacIntervalVisual): String = when (visual) {
    AlmanacIntervalVisual.PublishedWindow -> "JALUR DITERBITKAN"
    AlmanacIntervalVisual.NamedPeriodEstimate -> "ANGGARAN TEMPOH"
    AlmanacIntervalVisual.NightSegmentEstimate -> "SEGMEN MALAM"
    AlmanacIntervalVisual.SearchEnvelope -> "KAWASAN CARIAN"
    AlmanacIntervalVisual.EventTrigger -> "PERISTIWA SELEPAS SOLAT"
}

private fun researchItemOccurrenceLabel(item: ResearchConcordanceItem, minute: Int): String =
    item.interval?.let { interval ->
        productionOccurrenceLabel(
            intervalOccurrence(
                interval.copy(
                    visual = item.visual ?: interval.visual,
                    truthClass = item.truthClass,
                ),
                minute,
            ),
        )
    } ?: item.section.name

private fun mappingSentence(item: ResearchConcordanceItem): String = when (item.visual) {
    AlmanacIntervalVisual.NamedPeriodEstimate -> "Anggaran struktur pada roda; sempadan tempoh belum diluluskan."
    AlmanacIntervalVisual.NightSegmentEstimate -> "Kiraan malam sementara; dasar sempadan malam belum diluluskan."
    AlmanacIntervalVisual.SearchEnvelope -> "Kawasan carian visual; bukan dakwaan bahawa seluruh tempoh mempunyai ketepatan yang sama."
    AlmanacIntervalVisual.EventTrigger -> "Peristiwa selepas solat; waktu masuk JAKIM hanya proksi jadual, bukan bukti solat selesai."
    AlmanacIntervalVisual.PublishedWindow -> "Sempadan masa yang dipetakan."
    null -> "Konteks tanpa geometri jam; tarikh atau peristiwa mesti disahkan berasingan."
}

private fun researchLedgerAccessibility(item: ResearchConcordanceItem): String = buildString {
    append("${item.deed.title}. Draf penyelidikan dalam semakan. ")
    append("${item.timingLabel}. ")
    append("Tindakan: ${item.deed.instruction}. ")
    append("Frasa waktu sumber: ${item.deed.sourceTimePhrase}. ")
    item.visual?.let { append("Visual: ${researchVisualLabel(it)}. ") }
    append("Applicability ${item.applicability.basis}: ${item.applicability.value}. ")
    append(mappingSentence(item)).append(" ")
    append("Occurrence ${item.occurrence.id}; completion ${item.occurrence.completionId}.")
}

private fun assessmentText(evidence: ResearchEvidence): String =
    if (evidence.assessment.kind == "DISPLAYED_GRADE") {
        "${evidence.assessment.label} — ${evidence.assessment.grader}"
    } else {
        "Koleksi ${evidence.collection}"
    }

private fun malayDayName(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Isnin"
    DayOfWeek.TUESDAY -> "Selasa"
    DayOfWeek.WEDNESDAY -> "Rabu"
    DayOfWeek.THURSDAY -> "Khamis"
    DayOfWeek.FRIDAY -> "Jumaat"
    DayOfWeek.SATURDAY -> "Sabtu"
    DayOfWeek.SUNDAY -> "Ahad"
}

private fun openResearchSource(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: ActivityNotFoundException) {
        Unit
    }
}
