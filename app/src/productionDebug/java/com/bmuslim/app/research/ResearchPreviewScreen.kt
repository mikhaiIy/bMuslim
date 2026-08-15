package com.bmuslim.app.research

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val PreviewPine = Color(0xFF0B4938)
private val PreviewPaper = Color(0xFFF4F2E9)
private val PreviewPaperBright = Color(0xFFFAF8F0)
private val PreviewInk = Color(0xFF102A21)
private val PreviewMuted = Color(0xFF5B7067)
private val PreviewSaffron = Color(0xFFE4A43A)

@Composable
internal fun ResearchPreviewDialog(
    result: ResearchCatalogResult,
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit,
) {
    BackHandler(onBack = onBack)
    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("deeds-preview-screen"),
            color = PreviewPaper,
            contentColor = PreviewInk,
        ) {
            when (result) {
                is ResearchCatalogResult.Ready -> ResearchPreviewContent(
                    catalog = result.catalog,
                    onBack = onBack,
                    onOpenSource = onOpenSource,
                )
                ResearchCatalogResult.Rejected -> ResearchPreviewUnavailable(onBack)
            }
        }
    }
}

@Composable
private fun ResearchPreviewContent(
    catalog: ResearchCatalog,
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("back") {
            TextButton(
                onClick = onBack,
                modifier = Modifier.heightIn(min = 48.dp).testTag("deeds-preview-back"),
            ) { Text("← Kembali ke waktu solat", color = PreviewPine, fontWeight = FontWeight.Bold) }
        }
        item("heading") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "PRATONTON PENYELIDIKAN",
                    color = PreviewMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .8.sp,
                )
                Text(
                    "Draf amalan aMuslim",
                    color = PreviewPine,
                    fontFamily = FontFamily.Serif,
                    fontSize = 31.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }
        item("warning") {
            ResearchWarning(catalog)
        }
        item("daily-heading") {
            SectionHeading(
                text = "Konteks harian · ${catalog.dailyDeeds.size} calon",
                tag = "deeds-preview-daily",
            )
        }
        items(catalog.dailyDeeds, key = ResearchDeed::id) { deed ->
            ResearchDeedCard(deed, onOpenSource)
        }
        item("weekly-heading") {
            SectionHeading(
                text = "Konteks mingguan · ${catalog.weeklyDeeds.size} calon",
                tag = "deeds-preview-weekly",
            )
        }
        items(catalog.weeklyDeeds, key = ResearchDeed::id) { deed ->
            ResearchDeedCard(deed, onOpenSource)
        }
        if (catalog.monthlyDeeds.isNotEmpty()) {
            item("monthly-heading") {
                SectionHeading(
                    text = "Konteks bulanan · ${catalog.monthlyDeeds.size} calon",
                    tag = "deeds-preview-monthly",
                )
            }
            items(catalog.monthlyDeeds, key = ResearchDeed::id) { deed ->
                ResearchDeedCard(deed, onOpenSource)
            }
        }
        item("footer") {
            Text(
                "Draf ini untuk pratonton penyelidikan sahaja. Ia bukan senarai amalan terbaik, ranking agama atau kandungan yang telah diterbitkan.",
                color = PreviewMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 10.dp).testTag("deeds-preview-footer"),
            )
        }
    }
}

@Composable
private fun ResearchWarning(catalog: ResearchCatalog) {
    Surface(
        color = PreviewSaffron.copy(alpha = .12f),
        contentColor = PreviewInk,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, PreviewSaffron.copy(alpha = .55f)),
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription =
                    "Dalam semakan. ${catalog.deeds.size} calon amalan ini ialah draf penyelidikan. " +
                    "Kandungan belum diterbitkan atau diluluskan sebagai panduan. " +
                    "Frasa waktu dipaparkan seperti yang dicatat daripada sumber; aplikasi belum " +
                    "menentukan julat masa atau menghantar peringatan. Susunan ini bukan ranking atau keutamaan."
        },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(9.dp).background(PreviewSaffron, CircleShape))
                Text(
                    "DALAM SEMAKAN",
                    color = PreviewPine,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .7.sp,
                    modifier = Modifier.testTag("deeds-preview-status"),
                )
            }
            Text(
                "${catalog.deeds.size} calon amalan ini ialah draf penyelidikan. Kandungan belum diterbitkan atau diluluskan sebagai panduan. Frasa waktu dipaparkan seperti yang dicatat daripada sumber; aplikasi belum menentukan julat masa atau menghantar peringatan.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
            )
            Text(
                "${catalog.catalogId} · versi ${catalog.contentVersion}",
                color = PreviewMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier.testTag("deeds-preview-version"),
            )
            Text("Susunan ini bukan ranking atau keutamaan.", color = PreviewMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SectionHeading(text: String, tag: String) {
    Text(
        text,
        color = PreviewPine,
        fontFamily = FontFamily.Serif,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() }.testTag(tag),
    )
}

@Composable
private fun ResearchDeedCard(deed: ResearchDeed, onOpenSource: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("deed-card-${deed.id}"),
        color = PreviewPaperBright,
        contentColor = PreviewInk,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, PreviewPine.copy(alpha = .16f)),
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Text(
                "CALON DRAF · BELUM DISEMAK",
                color = PreviewMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = .65.sp,
            )
            Text(
                deed.title,
                color = PreviewPine,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }.testTag("deed-title-${deed.id}"),
            )
            LabeledText("TINDAKAN DALAM DRAF", deed.instruction, "deed-action-${deed.id}")
            LabeledText("FRASA WAKTU SUMBER", deed.sourceTimePhrase, "deed-timeframe-${deed.id}")
            LabeledText(
                "STATUS PEMETAAN WAKTU",
                timingStatusCopy(deed.resolutionStatus),
                "deed-timing-status-${deed.id}",
            )
            deed.evidence.forEach { evidence ->
                ResearchEvidenceBlock(
                    deedId = deed.id,
                    evidence = evidence,
                    variationLabel = variationLabel(deed, evidence),
                    onOpenSource = onOpenSource,
                )
            }
        }
    }
}

@Composable
private fun ResearchEvidenceBlock(
    deedId: String,
    evidence: ResearchEvidence,
    variationLabel: String?,
    onOpenSource: (String) -> Unit,
) {
    val role = variationLabel ?: when (evidence.role) {
        ResearchEvidenceRole.PRIMARY -> "SUMBER UTAMA"
        ResearchEvidenceRole.CORROBORATIVE -> "SUMBER SOKONGAN"
        ResearchEvidenceRole.VARIATION -> "BUKTI VARIASI"
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(role, color = PreviewMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .55.sp)
        Text(
            "${evidence.collection} ${evidence.reference}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("deed-source-$deedId-${evidence.id}"),
        )
        Text(
            "PENILAIAN DIREKODKAN",
            color = PreviewMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = .55.sp,
        )
        Text(
            assessmentCopy(evidence),
            color = PreviewMuted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag("deed-assessment-$deedId-${evidence.id}"),
        )
        TextButton(
            onClick = { onOpenSource(evidence.canonicalUrl) },
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics {
                    contentDescription = "Buka rujukan ${evidence.collection} ${evidence.reference}"
                }
                .testTag("deed-source-link-$deedId-${evidence.id}"),
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
        ) {
            Text("Buka rujukan", color = PreviewPine, fontWeight = FontWeight.Bold)
        }
    }
}

private fun variationLabel(deed: ResearchDeed, evidence: ResearchEvidence): String? = when {
    deed.actionVariationEvidence.any { it.id == evidence.id } -> "VARIASI TINDAKAN"
    deed.timingVariationEvidence.any { it.id == evidence.id } -> "VARIASI WAKTU"
    else -> null
}

@Composable
private fun LabeledText(label: String, value: String, tag: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, color = PreviewMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .55.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, lineHeight = 21.sp, modifier = Modifier.testTag(tag))
    }
}

private fun assessmentCopy(evidence: ResearchEvidence): String = when (evidence.assessment.kind) {
    "DISPLAYED_GRADE" -> "${evidence.assessment.label} — ${evidence.assessment.grader}"
    else -> "Koleksi ${evidence.collection}; tiada gred berasingan dipaparkan"
}

private fun timingStatusCopy(status: String): String = when (status) {
    "DIRECT_EVENT_ONLY" -> "Mengikut peristiwa pengguna — bukan waktu mula JAKIM."
    else -> "Belum dipetakan — dasar waktu perlu disemak."
}

@Composable
private fun ResearchPreviewUnavailable(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp).testTag("deeds-preview-back")) {
            Text("← Kembali ke waktu solat", color = PreviewPine, fontWeight = FontWeight.Bold)
        }
        Text(
            "Pratonton draf tidak tersedia",
            color = PreviewPine,
            fontFamily = FontFamily.Serif,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Fail penyelidikan gagal disahkan, jadi tiada kandungan separa dipaparkan.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
