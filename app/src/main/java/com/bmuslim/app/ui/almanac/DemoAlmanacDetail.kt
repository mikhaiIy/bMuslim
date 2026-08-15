package com.bmuslim.app.ui.almanac

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DemoDetailPine = Color(0xFF0B4938)
private val DemoDetailPaper = Color(0xFFF4F2E9)
private val DemoDetailPaperBright = Color(0xFFFAF8F0)
private val DemoDetailInk = Color(0xFF102A21)
private val DemoDetailMuted = Color(0xFF5B7067)
private val DemoDetailSaffron = Color(0xFFE4A43A)

data class DemoSummaryCellContent(
    val eyebrow: String,
    val title: String,
    val value: String,
)

data class DemoMomentSummaryContent(
    val accessibilityText: String,
    val label: String,
    val first: DemoSummaryCellContent,
    val second: DemoSummaryCellContent,
    val third: DemoSummaryCellContent,
    val footerEyebrow: String,
    val footerText: String,
    val footerCount: Int,
    val maximumPrimaryTitle: String,
    val maximumPrimaryValue: String,
    val maximumSecondaryText: String,
    val maximumFooterText: String,
)

enum class DemoLedgerSectionStyle { Body, Muted, Arabic, Translation }

data class DemoLedgerSection(
    val label: String? = null,
    val text: String,
    val style: DemoLedgerSectionStyle = DemoLedgerSectionStyle.Body,
)

data class DemoLedgerContent(
    val accessibilityText: String,
    val eyebrow: String,
    val timing: String,
    val status: String,
    val title: String,
    val classification: String,
    val sections: List<DemoLedgerSection>,
    val footerLeftLabel: String,
    val footerLeftValue: String,
    val footerRightLabel: String,
    val footerRightValue: String,
)

@Composable
fun DemoAlmanacMomentSummary(
    snapshot: SharedAlmanacSnapshot,
    content: DemoMomentSummaryContent,
    modifier: Modifier = Modifier,
) {
    when {
        snapshot.fontComposition == AlmanacFontComposition.Maximum -> Surface(
            color = DemoDetailPaperBright,
            contentColor = DemoDetailInk,
        ) {
            Column(
                modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                    contentDescription = content.accessibilityText
                }.padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(
                    content.maximumPrimaryTitle,
                    color = DemoDetailPine,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                )
                Text(
                    content.maximumPrimaryValue,
                    color = DemoDetailInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
                Text(
                    content.maximumSecondaryText,
                    color = DemoDetailMuted,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    content.maximumFooterText,
                    color = DemoDetailMuted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        snapshot.compact -> Surface(color = DemoDetailPaperBright, contentColor = DemoDetailInk) {
            Row(
                modifier.fillMaxWidth().height(58.dp).semantics(mergeDescendants = true) {
                    contentDescription = content.accessibilityText
                }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DemoSummaryCell(content.first, Modifier.weight(.9f))
                DemoSummaryCell(content.second, Modifier.weight(1.2f))
                DemoSummaryCell(content.third, Modifier.weight(1f))
            }
        }

        else -> Surface(color = DemoDetailPaperBright, contentColor = DemoDetailInk) {
            Column(
                modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                    contentDescription = content.accessibilityText
                }.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    content.label,
                    color = DemoDetailMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = .6.sp,
                )
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.Top) {
                    DemoSummaryCell(content.first, Modifier.weight(1f))
                    DemoSummaryCell(content.second, Modifier.weight(1.2f))
                    DemoSummaryCell(content.third, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text(
                        content.footerEyebrow,
                        color = DemoDetailMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = .55.sp,
                    )
                    Text(content.footerCount.toString(), color = DemoDetailMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    content.footerText,
                    color = DemoDetailInk,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun DemoSummaryCell(content: DemoSummaryCellContent, modifier: Modifier) {
    Column(modifier) {
        Text(content.eyebrow, color = DemoDetailMuted, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            content.title,
            color = DemoDetailInk,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            lineHeight = 12.sp,
        )
        Text(content.value, color = DemoDetailMuted, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
}

@Composable
fun DemoAlmanacLedger(
    content: DemoLedgerContent,
    compact: Boolean,
    internallyScrollable: Boolean,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier.fillMaxWidth(),
        color = DemoDetailPaper,
        contentColor = DemoDetailInk,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    ) {
        Box(Modifier.fillMaxSize().semantics(mergeDescendants = false) {
            contentDescription = content.accessibilityText
        }) {
            Canvas(Modifier.fillMaxSize()) {
                val x = 14.dp.toPx()
                drawLine(DemoDetailSaffron, Offset(x, 15.dp.toPx()), Offset(x, size.height - 15.dp.toPx()), 1.5.dp.toPx())
                drawCircle(DemoDetailPaper, 5.dp.toPx(), Offset(x, 31.dp.toPx()))
                drawCircle(DemoDetailSaffron, 3.2.dp.toPx(), Offset(x, 31.dp.toPx()), style = Stroke(1.2.dp.toPx()))
                val marker = Path().apply {
                    moveTo(x, 23.dp.toPx())
                    lineTo(x + 3.dp.toPx(), 27.dp.toPx())
                    lineTo(x, 31.dp.toPx())
                    lineTo(x - 3.dp.toPx(), 27.dp.toPx())
                    close()
                }
                drawPath(marker, DemoDetailSaffron)
            }
            Column(
                Modifier.fillMaxSize().then(
                    if (internallyScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                ).padding(start = 29.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            content.eyebrow,
                            color = DemoDetailMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = .7.sp,
                        )
                        Text(content.timing, color = DemoDetailMuted, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                    }
                    Text(
                        content.status,
                        color = DemoDetailMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        textAlign = TextAlign.End,
                    )
                }
                Text(
                    content.title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp,
                )
                Text(content.classification, color = DemoDetailPine, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(DemoDetailPine.copy(alpha = .18f)))
                if (compact) action?.invoke()
                content.sections.forEach { section ->
                    section.label?.let {
                        Text(it, color = DemoDetailMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    when (section.style) {
                        DemoLedgerSectionStyle.Body -> Text(section.text, fontSize = 13.sp, lineHeight = 19.sp)
                        DemoLedgerSectionStyle.Muted -> Text(section.text, color = DemoDetailMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        DemoLedgerSectionStyle.Arabic -> Text(
                            section.text,
                            modifier = Modifier.fillMaxWidth(),
                            color = DemoDetailPine,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Serif,
                            fontSize = 25.sp,
                            lineHeight = 37.sp,
                        )
                        DemoLedgerSectionStyle.Translation -> Text(section.text, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
                if (!compact) action?.invoke()
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(DemoDetailPine.copy(alpha = .18f)))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1.35f)) {
                        Text(content.footerLeftLabel, color = DemoDetailMuted, fontSize = 8.sp)
                        Text(content.footerLeftValue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(content.footerRightLabel, color = DemoDetailMuted, fontSize = 8.sp)
                        Text(
                            content.footerRightValue,
                            color = DemoDetailPine,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}
