package com.bmuslim.app.ui.timeline

import android.animation.ValueAnimator
import android.graphics.Paint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bmuslim.app.ui.theme.BMuslimTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val DayMinutes = 24 * 60
private const val NowMinute = 15 * 60

private val Pine = Color(0xFF0B4938)
private val DeepPine = Color(0xFF052B23)
private val Saffron = Color(0xFFE4A43A)
private val Paper = Color(0xFFF4F2E9)
private val PaperBright = Color(0xFFFAF8F0)
private val PaperInk = Color(0xFF102A21)
private val PaperMuted = Color(0xFF5B7067)

private data class SkyPalette(
    val top: Color,
    val middle: Color,
    val horizon: Color,
    val ink: Color = Color(0xFFF7F5EC),
    val muted: Color = Color(0xFFDCE2DE),
    val rail: Color = Color(0xFFB8C9C0),
)

private data class PaletteStop(val minute: Int, val palette: SkyPalette)

private val Midnight = SkyPalette(Color(0xFF031D18), Color(0xFF062A24), Color(0xFF123A30))
private val BeforeDawn = SkyPalette(Color(0xFF102B2B), Color(0xFF1E4540), Color(0xFF365D55))
private val Sunrise = SkyPalette(Color(0xFF173D38), Color(0xFF356158), Color(0xFF4E6A5B))
private val Daylight = SkyPalette(Color(0xFF173C32), Color(0xFF315B4E), Color(0xFF426A59))
private val LateDay = SkyPalette(Color(0xFF15382F), Color(0xFF365D4C), Color(0xFF626246))
private val Twilight = SkyPalette(Color(0xFF061F1D), Color(0xFF123832), Color(0xFF514D39))

private val paletteStops = listOf(
    PaletteStop(0, Midnight),
    PaletteStop(4 * 60 + 45, BeforeDawn),
    PaletteStop(6 * 60 + 20, Sunrise),
    PaletteStop(9 * 60, Daylight),
    PaletteStop(16 * 60 + 45, LateDay),
    PaletteStop(19 * 60 + 35, Twilight),
    PaletteStop(21 * 60 + 30, Midnight),
    PaletteStop(DayMinutes, Midnight),
)

@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
    initialMinute: Int = NowMinute,
) {
    val deeds = remember { sampleDeeds() }
    val dayStates = remember { sampleDayStates() }
    var completedIds by rememberSaveable(
        stateSaver = listSaver(save = { it }, restore = { it }),
    ) { mutableStateOf(emptyList<String>()) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(initialMinute) }
    val active = remember(deeds, selectedMinute) { activeDeedsAt(deeds, selectedMinute) }
    val prayer = remember(deeds, selectedMinute) { obligatoryPrayerContextAt(deeds, selectedMinute) }
    val next = remember(deeds, selectedMinute) { nextDeedAt(deeds, selectedMinute) }
    val contextual = remember(deeds, selectedMinute) { contextualDeedAt(deeds, selectedMinute) }
    val primary = active.firstOrNull() ?: next
    val palette = remember(selectedMinute) { paletteAt(selectedMinute) }
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    val duration = if (animationsEnabled) 180 else 0
    val top by animateColorAsState(palette.top, tween(duration), label = "sky top")
    val middle by animateColorAsState(palette.middle, tween(duration), label = "sky middle")
    val horizon by animateColorAsState(palette.horizon, tween(duration), label = "sky horizon")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, middle, horizon))),
    ) {
        val compact = maxHeight <= 700.dp
        Column(Modifier.fillMaxSize()) {
            AlmanacHeader(
                selectedMinute = selectedMinute,
                ink = palette.ink,
                muted = palette.muted,
                doneCount = completedIds.size,
                total = deeds.size,
                compact = compact,
            )
            TimeInstrument(
                deeds = deeds,
                selectedMinute = selectedMinute,
                palette = palette,
                modifier = Modifier.fillMaxWidth().height(if (compact) 182.dp else 220.dp),
                onSelectedMinute = { selectedMinute = wrapMinute(it) },
                onReturnToNow = { selectedMinute = NowMinute },
            )
            if (compact) {
                CompactContextStrip(
                    selectedMinute = selectedMinute,
                    active = active,
                    prayer = prayer,
                    contextual = contextual,
                    dayState = dayStates.first(),
                )
            } else {
                MomentSummary(
                    selectedMinute = selectedMinute,
                    active = active,
                    prayer = prayer,
                    contextual = contextual,
                    dayState = dayStates.first(),
                )
                ActiveDeedIndex(active = active, selectedMinute = selectedMinute)
            }
            Crossfade(
                targetState = primary.id,
                modifier = Modifier.weight(1f),
                animationSpec = tween(if (animationsEnabled) 180 else 0),
                label = "folio deed",
            ) { deedId ->
                val deed = deeds.first { it.id == deedId }
                LivingLedger(
                    deed = deed,
                    isActive = deed in active,
                    isDone = deed.id in completedIds,
                    animationsEnabled = animationsEnabled,
                    compact = compact,
                    onToggle = {
                        completedIds = if (deed.id in completedIds) completedIds - deed.id else completedIds + deed.id
                    },
                )
            }
            FolioNavigation()
        }
    }
}

@Composable
private fun AlmanacHeader(
    selectedMinute: Int,
    ink: Color,
    muted: Color,
    doneCount: Int,
    total: Int,
    compact: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = if (compact) 4.dp else 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text("bMuslim", color = ink, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(
                "Khamis, 2 Julai 2026",
                color = muted,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            )
            if (!compact) Text("17 Muharram 1448 H", color = muted, fontSize = 10.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatMinute(selectedMinute),
                    color = ink,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 31.sp,
                    letterSpacing = (-1.4).sp,
                )
                Text(dayPart(selectedMinute), color = muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(11.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(32.dp).border(1.dp, ink.copy(alpha = .45f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(doneCount.toString(), color = ink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("$doneCount/$total", color = muted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun TimeInstrument(
    deeds: List<TimelineDeed>,
    selectedMinute: Int,
    palette: SkyPalette,
    modifier: Modifier,
    onSelectedMinute: (Int) -> Unit,
    onReturnToNow: () -> Unit,
) {
    var dragMinute by remember { mutableIntStateOf(selectedMinute) }
    val currentSelectedMinute by rememberUpdatedState(selectedMinute)
    val currentOnSelectedMinute by rememberUpdatedState(onSelectedMinute)
    Box(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = wheelDescription(deeds, selectedMinute)
                    stateDescription = "Masa dipilih ${formatMinute(selectedMinute)}"
                    progressBarRangeInfo = ProgressBarRangeInfo(selectedMinute.toFloat(), 0f..1439f)
                    setProgress { requested ->
                        onSelectedMinute(((requested.coerceIn(0f, 1439f) / 5f).roundToInt() * 5))
                        true
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragMinute = currentSelectedMinute },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragMinute = wrapMinute(dragMinute - (dragAmount * 3.0f).roundToInt())
                            currentOnSelectedMinute(dragMinute)
                        },
                        onDragEnd = {
                            dragMinute = wrapMinute(((dragMinute + 2) / 5) * 5)
                            currentOnSelectedMinute(dragMinute)
                        },
                    )
                },
        ) {
            drawTimeInstrument(deeds, selectedMinute, palette)
        }

        if (circularMinuteDistance(selectedMinute, NowMinute) > 4) {
            Button(
                onClick = onReturnToNow,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 5.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = PaperInk),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Text("Kembali ke sekarang", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

private fun DrawScope.drawTimeInstrument(
    deeds: List<TimelineDeed>,
    selectedMinute: Int,
    palette: SkyPalette,
) {
    val center = Offset(size.width / 2f, size.height * .93f)
    val hourRadius = min(size.width * .285f, size.height * .62f)
    val prayerRadius = min(size.width * .345f, size.height * .72f)
    val deedBaseRadius = min(size.width * .39f, size.height * .80f)
    val laneStep = 10.dp.toPx()
    val celestialRadius = min(size.width * .445f, center.y - 15.dp.toPx())

    drawCircle(
        color = palette.rail.copy(alpha = .06f),
        radius = deedBaseRadius + 10.dp.toPx(),
        center = center,
        style = Stroke(18.dp.toPx()),
    )

    // Layer 1: the neutral 24-hour rail.
    drawCircle(palette.rail.copy(alpha = .42f), hourRadius, center, style = Stroke(1.dp.toPx()))
    for (hour in 0 until 24) {
        val angle = minuteAngle(hour * 60, selectedMinute)
        val major = hour % 3 == 0
        val outer = polar(center, hourRadius, angle)
        val inner = polar(center, hourRadius - if (major) 10.dp.toPx() else 5.dp.toPx(), angle)
        drawLine(
            palette.rail.copy(alpha = if (major) .86f else .38f),
            inner,
            outer,
            if (major) 1.3.dp.toPx() else .8.dp.toPx(),
        )
        if (major && outer.x in 18f..(size.width - 18f) && outer.y in 5f..size.height) {
            drawLabel(hour.toString().padStart(2, '0'), polar(center, hourRadius - 19.dp.toPx(), angle), palette.muted, 8.5f)
        }
    }

    // Layer 2: obligatory prayer context.
    drawCircle(palette.rail.copy(alpha = .55f), prayerRadius, center, style = Stroke(1.2.dp.toPx()))
    val prayers = deeds.filter { it.ruling == Ruling.Fardh }
    val activePrayer = activeDeedsAt(prayers, selectedMinute).firstOrNull()
    val nextPrayer = obligatoryPrayerContextAt(deeds, selectedMinute)
    prayers.forEach { prayer ->
        val angle = minuteAngle(prayer.startMinute, selectedMinute)
        val point = polar(center, prayerRadius, angle)
        val active = prayer.id == activePrayer?.id
        val next = !active && prayer.id == nextPrayer.id
        if (active) drawCircle(Saffron.copy(alpha = .22f), 11.dp.toPx(), point)
        if (next) drawCircle(palette.ink.copy(alpha = .20f), 9.dp.toPx(), point, style = Stroke(1.dp.toPx()))
        drawCircle(if (active) Saffron else Paper, if (active) 6.dp.toPx() else 4.8.dp.toPx(), point)
        drawCircle(if (active) DeepPine else Pine, if (active) 4.dp.toPx() else 3.1.dp.toPx(), point)
        if ((active || next) && point.x in 48f..(size.width - 48f) && point.y in 12f..size.height) {
            val title = prayer.title.removePrefix("Solat ")
            val label = if (active) title else "$title ${formatMinute(prayer.startMinute)}"
            drawLabel(label, polar(center, prayerRadius - 19.dp.toPx(), angle), if (active) Saffron else palette.ink, 9f, true)
        }
    }

    // Layer 3: outer deed-window lanes.
    val contextualDeeds = deeds.filter { it.ruling != Ruling.Fardh }
    val lanes = assignDeedLanes(contextualDeeds)
    contextualDeeds.forEach { deed ->
        val radius = deedBaseRadius + lanes.getValue(deed.id) * laneStep
        val active = deed.isActiveAt(selectedMinute)
        drawClockArc(
            center = center,
            radius = radius,
            startMinute = deed.startMinute,
            endMinute = deed.endMinute,
            selectedMinute = selectedMinute,
            color = if (active) Saffron else palette.rail.copy(alpha = if (deed.ruling == Ruling.Muakkadah) .52f else .28f),
            width = if (active) 7.dp.toPx() else 4.dp.toPx(),
        )
        val bead = polar(center, radius, minuteAngle(deed.actMinute, selectedMinute))
        drawCircle(if (active) Saffron else palette.rail.copy(alpha = .7f), if (active) 4.dp.toPx() else 2.3.dp.toPx(), bead)
    }
    activeDeedsAt(contextualDeeds, selectedMinute).firstOrNull()?.let { deed ->
        val radius = deedBaseRadius + lanes.getValue(deed.id) * laneStep
        val pointerCrossing = Offset(center.x, center.y - radius)
        val labelPoint = Offset(center.x + 57.dp.toPx(), pointerCrossing.y + 3.dp.toPx())
        drawLine(
            Saffron.copy(alpha = .68f),
            pointerCrossing + Offset(7.dp.toPx(), 0f),
            labelPoint - Offset(28.dp.toPx(), 0f),
            .8.dp.toPx(),
            StrokeCap.Round,
        )
        drawLabel(shortDeedTitle(deed), labelPoint, Saffron, 8.5f, true)
    }

    // The celestial body shares one concentric rail and never changes orbit radius.
    drawCircle(palette.rail.copy(alpha = .22f), celestialRadius, center, style = Stroke(.8.dp.toPx()))
    drawCelestial(center, celestialRadius, selectedMinute, palette)

    // Fixed selected-time pointer terminates precisely on the prayer rail.
    val pointerTop = Offset(center.x, center.y - deedBaseRadius - 13.dp.toPx())
    val pointerBottom = Offset(center.x, center.y - prayerRadius)
    drawLine(Saffron, pointerTop, pointerBottom, 2.dp.toPx(), StrokeCap.Round)
    drawCircle(Saffron, 4.dp.toPx(), pointerBottom)

    // Actual-now is independent of the selected-time pointer.
    val nowAngle = minuteAngle(NowMinute, selectedMinute)
    val nowOuter = polar(center, hourRadius, nowAngle)
    val nowInner = polar(center, hourRadius - 9.dp.toPx(), nowAngle)
    drawLine(Paper, nowInner, nowOuter, 3.dp.toPx(), StrokeCap.Round)
    drawCircle(DeepPine, 2.2.dp.toPx(), nowOuter)
}

private fun DrawScope.drawClockArc(
    center: Offset,
    radius: Float,
    startMinute: Int,
    endMinute: Int,
    selectedMinute: Int,
    color: Color,
    width: Float,
) {
    val duration = forwardMinuteDistance(startMinute, endMinute).coerceAtLeast(1)
    drawArc(
        color = color,
        startAngle = minuteAngle(startMinute, selectedMinute),
        sweepAngle = duration / 4f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawCelestial(
    center: Offset,
    radius: Float,
    minute: Int,
    palette: SkyPalette,
) {
    val sunrise = 6 * 60
    val sunset = 19 * 60 + 29
    val isSun = minute in sunrise until sunset
    val progress = if (isSun) {
        (minute - sunrise).toFloat() / (sunset - sunrise)
    } else {
        val nightMinute = if (minute >= sunset) minute else minute + DayMinutes
        (nightMinute - sunset).toFloat() / (DayMinutes - sunset + sunrise)
    }.coerceIn(0f, 1f)
    val angle = 180f + 180f * progress
    val point = polar(center, radius, angle)
    val glow = if (isSun) Saffron else Color(0xFFE2EAE5)
    drawCircle(glow.copy(alpha = .13f), 13.dp.toPx(), point)
    if (isSun) {
        drawCircle(glow, 4.5.dp.toPx(), point)
        repeat(8) { ray ->
            val rayAngle = ray * 45f
            drawLine(glow, polar(point, 7.dp.toPx(), rayAngle), polar(point, 10.dp.toPx(), rayAngle), 1.dp.toPx(), StrokeCap.Round)
        }
    } else {
        drawCircle(glow, 6.5.dp.toPx(), point)
        drawCircle(palette.middle, 6.dp.toPx(), point + Offset(3.dp.toPx(), -2.dp.toPx()))
    }
}

private fun DrawScope.drawLabel(
    label: String,
    point: Offset,
    color: Color,
    sizeSp: Float,
    bold: Boolean = false,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgbCompat()
        textSize = sizeSp.dp.toPx()
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT,
            if (bold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL,
        )
        textAlign = Paint.Align.CENTER
    }
    drawIntoCanvas { it.nativeCanvas.drawText(label, point.x, point.y + 3.dp.toPx(), paint) }
}

@Composable
private fun MomentSummary(
    selectedMinute: Int,
    active: List<TimelineDeed>,
    prayer: TimelineDeed,
    contextual: TimelineDeed,
    dayState: DayState,
) {
    val contextualIsActive = contextual.isActiveAt(selectedMinute)
    Surface(color = PaperBright, contentColor = PaperInk) {
        Column(
            Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = momentSummaryDescription(selectedMinute, active, prayer, contextual, dayState)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            RuledCaption("RINGKASAN SAAT INI")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                SummaryCell(
                    eyebrow = if (prayer.isActiveAt(selectedMinute)) "SOLAT AKTIF" else "SOLAT BERIKUT",
                    title = prayer.title.removePrefix("Solat "),
                    value = formatMinute(prayer.startMinute),
                    accent = prayer.isActiveAt(selectedMinute),
                    modifier = Modifier.weight(1f),
                )
                VerticalRule()
                SummaryCell(
                    eyebrow = if (contextualIsActive) "AMALAN AKTIF" else "SETERUSNYA",
                    title = contextual.title,
                    value = if (contextualIsActive) "hingga ${formatMinute(contextual.endMinute)}" else formatMinute(contextual.startMinute),
                    accent = contextualIsActive,
                    modifier = Modifier.weight(1.25f),
                )
                VerticalRule()
                SummaryCell(
                    eyebrow = "HARI INI",
                    title = dayState.title,
                    value = dayState.ruling.label,
                    accent = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CompactContextStrip(
    selectedMinute: Int,
    active: List<TimelineDeed>,
    prayer: TimelineDeed,
    contextual: TimelineDeed,
    dayState: DayState,
) {
    val contextualIsActive = contextual.isActiveAt(selectedMinute)
    Surface(color = PaperBright, contentColor = PaperInk) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .semantics(mergeDescendants = true) {
                    contentDescription = momentSummaryDescription(selectedMinute, active, prayer, contextual, dayState)
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryCell(
                eyebrow = if (prayer.isActiveAt(selectedMinute)) "SOLAT AKTIF" else "SOLAT BERIKUT",
                title = prayer.title.removePrefix("Solat "),
                value = formatMinute(prayer.startMinute),
                accent = prayer.isActiveAt(selectedMinute),
                modifier = Modifier.weight(.9f),
            )
            VerticalRule()
            SummaryCell(
                eyebrow = if (contextualIsActive) "AMALAN AKTIF" else "AMALAN BERIKUT",
                title = contextual.title,
                value = if (contextualIsActive) "hingga ${formatMinute(contextual.endMinute)}" else formatMinute(contextual.startMinute),
                accent = contextualIsActive,
                modifier = Modifier.weight(1.2f),
            )
            VerticalRule()
            SummaryCell(
                eyebrow = "HARI INI",
                title = dayState.title,
                value = dayState.ruling.label,
                accent = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryCell(
    eyebrow: String,
    title: String,
    value: String,
    accent: Boolean,
    modifier: Modifier,
) {
    Column(modifier.padding(horizontal = 7.dp)) {
        Text(eyebrow, color = if (accent) Color(0xFF9A6207) else PaperMuted, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
        Text(title, color = PaperInk, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, lineHeight = 12.sp)
        Text(value, color = PaperMuted, fontFamily = FontFamily.Monospace, fontSize = 8.sp)
    }
}

@Composable
private fun VerticalRule() {
    Spacer(Modifier.width(1.dp).height(36.dp).background(Pine.copy(alpha = .16f)))
}

@Composable
private fun RuledCaption(label: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f).height(1.dp).background(Pine.copy(alpha = .16f)))
        Text(label, modifier = Modifier.padding(horizontal = 8.dp), color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
        Spacer(Modifier.weight(1f).height(1.dp).background(Pine.copy(alpha = .16f)))
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ActiveDeedIndex(active: List<TimelineDeed>, selectedMinute: Int) {
    val label = if (active.isEmpty()) {
        "Tiada amalan berwaktu pada ${formatMinute(selectedMinute)}"
    } else {
        active.joinToString("  /  ") { "${it.title} hingga ${formatMinute(it.endMinute)}" }
    }
    Surface(color = PaperBright, contentColor = PaperInk) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("AKTIF PADA PENUNJUK", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .55.sp)
                Text(active.size.toString(), color = if (active.isNotEmpty()) Color(0xFF9A6207) else PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(5.dp))
            Text(label, color = if (active.isEmpty()) PaperMuted else PaperInk, fontSize = 10.sp, fontWeight = if (active.isEmpty()) FontWeight.Normal else FontWeight.Bold, maxLines = 2)
            Spacer(Modifier.height(7.dp))
            Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .17f)))
        }
    }
}

@Composable
private fun LivingLedger(
    deed: TimelineDeed,
    isActive: Boolean,
    isDone: Boolean,
    animationsEnabled: Boolean,
    compact: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completion by animateFloatAsState(
        targetValue = if (isDone) 1f else 0f,
        animationSpec = tween(if (animationsEnabled) 220 else 0),
        label = "completion",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Paper,
        contentColor = PaperInk,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val x = 14.dp.toPx()
                drawLine(Saffron.copy(alpha = .9f), Offset(x, 15.dp.toPx()), Offset(x, size.height - 15.dp.toPx()), 1.5.dp.toPx())
                drawCircle(Paper, 5.dp.toPx(), Offset(x, 31.dp.toPx()))
                drawCircle(Saffron, 3.2.dp.toPx(), Offset(x, 31.dp.toPx()), style = Stroke(1.2.dp.toPx()))
                val marker = Path().apply {
                    moveTo(x, 23.dp.toPx())
                    lineTo(x + 3.dp.toPx(), 27.dp.toPx())
                    lineTo(x, 31.dp.toPx())
                    lineTo(x - 3.dp.toPx(), 27.dp.toPx())
                    close()
                }
                drawPath(marker, Saffron)
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 29.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp),
            ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (isActive) "AMALAN UTAMA SEKARANG" else "AMALAN SETERUSNYA",
                            color = if (isActive) Color(0xFF9A6207) else PaperMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = .7.sp,
                        )
                        Text(
                            "${formatMinute(deed.startMinute)} hingga ${formatMinute(deed.endMinute)}",
                            color = PaperMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                        )
                    }
                    Text(if (isDone) "SELESAI" else deed.windowLabel.uppercase(), color = if (isDone) Pine else PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 2, textAlign = TextAlign.End)
                }
            }
            item {
                Text(
                    deed.title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-.6).sp,
                    lineHeight = 32.sp,
                )
                Text("${deed.type.label}  /  ${deed.ruling.label}", color = Pine, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .18f)))
            }
            if (compact) {
                item { CompletionAction(deed, isDone, completion, onToggle) }
            }
            item {
                Text("PANDUAN RINGKAS", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
                Text(deed.description, color = PaperInk, fontSize = 13.sp, lineHeight = 19.sp)
                Text(archetypeLabel(deed), color = PaperMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
            if (deed.arabic != null) {
                item {
                    Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .18f)))
                    Text(
                        deed.arabic.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        color = Pine,
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Serif,
                        fontSize = 25.sp,
                        lineHeight = 37.sp,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(deed.translation.orEmpty(), color = PaperInk, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            if (!compact) {
                item { CompletionAction(deed, isDone, completion, onToggle) }
            }
            item {
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Pine.copy(alpha = .18f)))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text("SUMBER", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
                        Text(deed.citation, color = PaperInk, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("GRED HADIS", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .5.sp)
                        Text(deed.grade, color = Pine, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}
}

@Composable
private fun CompletionAction(
    deed: TimelineDeed,
    isDone: Boolean,
    completion: Float,
    onToggle: () -> Unit,
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .semantics {
                role = Role.Checkbox
                contentDescription = "Tandai ${deed.title} selesai"
                stateDescription = if (isDone) "Selesai" else "Belum selesai"
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (isDone) Paper else Pine,
        ),
        border = BorderStroke(1.dp, Pine.copy(alpha = .38f + completion * .42f)),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(Pine.copy(alpha = completion), CircleShape)
                .border(1.dp, Pine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            CheckMark(color = if (isDone) Paper else Pine, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(if (isDone) "Ditandai selesai" else "Tandai selesai", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}

private enum class NavGlyph { Today, Planner, Settings }

@Composable
private fun FolioNavigation() {
    Surface(color = PaperBright, contentColor = PaperInk) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp).padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(NavGlyph.Today, "Hari ini", active = true, enabled = true)
            NavItem(NavGlyph.Planner, "Perancang", active = false, enabled = false)
            NavItem(NavGlyph.Settings, "Tetapan", active = false, enabled = false)
        }
    }
}

@Composable
private fun NavItem(glyph: NavGlyph, label: String, active: Boolean, enabled: Boolean) {
    val color = if (active) Pine else PaperMuted.copy(alpha = if (enabled) 1f else .55f)
    Column(
        modifier = Modifier.size(width = 92.dp, height = 56.dp).semantics {
            contentDescription = if (enabled) label else "$label, belum tersedia"
            stateDescription = if (active) "Halaman semasa" else "Tidak aktif"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NavigationIcon(glyph, color, Modifier.size(22.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold)
        Spacer(Modifier.height(3.dp))
        Spacer(Modifier.width(18.dp).height(2.dp).background(if (active) Saffron else Color.Transparent))
    }
}

@Composable
private fun NavigationIcon(glyph: NavGlyph, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        when (glyph) {
            NavGlyph.Today -> {
                drawRoundRect(color, Offset(size.width * .16f, size.height * .20f), Size(size.width * .68f, size.height * .66f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = stroke)
                drawLine(color, Offset(size.width * .16f, size.height * .38f), Offset(size.width * .84f, size.height * .38f), stroke.width)
                drawLine(color, Offset(size.width * .34f, size.height * .12f), Offset(size.width * .34f, size.height * .28f), stroke.width, StrokeCap.Round)
                drawLine(color, Offset(size.width * .66f, size.height * .12f), Offset(size.width * .66f, size.height * .28f), stroke.width, StrokeCap.Round)
            }
            NavGlyph.Planner -> {
                repeat(3) { index ->
                    val y = size.height * (.28f + index * .23f)
                    drawCircle(color, 1.4.dp.toPx(), Offset(size.width * .22f, y))
                    drawLine(color, Offset(size.width * .36f, y), Offset(size.width * .80f, y), stroke.width, StrokeCap.Round)
                }
            }
            NavGlyph.Settings -> {
                drawCircle(color, size.minDimension * .19f, center, style = stroke)
                drawCircle(color, size.minDimension * .37f, center, style = stroke)
                repeat(8) { index ->
                    val angle = index * 45f
                    drawLine(color, polar(center, size.minDimension * .37f, angle), polar(center, size.minDimension * .47f, angle), stroke.width, StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
private fun CheckMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * .16f, size.height * .53f)
            lineTo(size.width * .42f, size.height * .78f)
            lineTo(size.width * .86f, size.height * .24f)
        }
        drawPath(path, color, style = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun assignDeedLanes(deeds: List<TimelineDeed>): Map<String, Int> {
    val laneEnds = mutableListOf<Int>()
    val result = mutableMapOf<String, Int>()
    deeds.sortedWith(compareBy<TimelineDeed> { it.startMinute }.thenBy { it.ruling.priority }.thenBy { it.id }).forEach { deed ->
        val laneIndex = laneEnds.indexOfFirst { deed.startMinute >= it }
        val lane = if (laneIndex >= 0) laneIndex else laneEnds.size
        if (laneIndex >= 0) laneEnds[lane] = deed.endMinute else laneEnds += deed.endMinute
        result[deed.id] = lane
    }
    return result
}

private fun paletteAt(minute: Int): SkyPalette {
    val value = wrapMinute(minute)
    val index = paletteStops.indexOfLast { it.minute <= value }.coerceAtMost(paletteStops.lastIndex - 1)
    val from = paletteStops[index]
    val to = paletteStops[index + 1]
    val fraction = ((value - from.minute).toFloat() / (to.minute - from.minute).coerceAtLeast(1)).coerceIn(0f, 1f)
    return SkyPalette(
        top = androidx.compose.ui.graphics.lerp(from.palette.top, to.palette.top, fraction),
        middle = androidx.compose.ui.graphics.lerp(from.palette.middle, to.palette.middle, fraction),
        horizon = androidx.compose.ui.graphics.lerp(from.palette.horizon, to.palette.horizon, fraction),
        ink = androidx.compose.ui.graphics.lerp(from.palette.ink, to.palette.ink, fraction),
        muted = androidx.compose.ui.graphics.lerp(from.palette.muted, to.palette.muted, fraction),
        rail = androidx.compose.ui.graphics.lerp(from.palette.rail, to.palette.rail, fraction),
    )
}

private fun wheelDescription(deeds: List<TimelineDeed>, minute: Int): String {
    val active = activeDeedsAt(deeds, minute)
    val prayer = obligatoryPrayerContextAt(deeds, minute)
    val activeText = if (active.isEmpty()) "Tiada amalan berwaktu aktif" else active.joinToString { it.title }
    val prayerText = if (prayer.isActiveAt(minute)) "Solat aktif ${prayer.title}" else "Solat berikut ${prayer.title} pada ${formatMinute(prayer.startMinute)}"
    return "Roda waktu 24 jam. Leret mendatar atau laras untuk memilih masa. $activeText. $prayerText."
}

private fun momentSummaryDescription(
    minute: Int,
    active: List<TimelineDeed>,
    prayer: TimelineDeed,
    contextual: TimelineDeed,
    dayState: DayState,
): String {
    val activeText = if (active.isEmpty()) "Tiada amalan aktif. Amalan berikut ${contextual.title}" else "Aktif: ${active.joinToString { it.title }}"
    val prayerText = if (prayer.isActiveAt(minute)) "Solat aktif ${prayer.title}" else "Solat berikut ${prayer.title} pada ${formatMinute(prayer.startMinute)}"
    return "Ringkasan ${formatMinute(minute)}. $prayerText. $activeText. Keadaan hari: ${dayState.title}."
}

private fun shortDeedTitle(deed: TimelineDeed): String = deed.title
    .removePrefix("Solat ")
    .removePrefix("Baca ")
    .take(18)

private fun archetypeLabel(deed: TimelineDeed): String = when (deed.archetype) {
    RenderArchetype.Point -> "Tindakan sekitar ${deed.actMinutes} minit dalam ruang masa ini"
    RenderArchetype.Window -> "Boleh diamalkan sepanjang ruang masa ini"
    RenderArchetype.DaySpan -> "Amalan sepanjang hari"
    RenderArchetype.Anytime -> "Boleh dilakukan bila-bila masa"
}

private fun minuteAngle(minute: Int, selectedMinute: Int): Float {
    var delta = minute - selectedMinute
    if (delta > DayMinutes / 2) delta -= DayMinutes
    if (delta < -DayMinutes / 2) delta += DayMinutes
    return -90f + delta / 4f
}

private fun polar(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = degrees * PI.toFloat() / 180f
    return Offset(center.x + radius * cos(radians), center.y + radius * sin(radians))
}

private fun formatMinute(minute: Int): String {
    val wrapped = wrapMinute(minute)
    return "%02d:%02d".format(wrapped / 60, wrapped % 60)
}

private fun dayPart(minute: Int): String = when (wrapMinute(minute)) {
    in 0 until 5 * 60 + 59 -> "Malam, sebelum Subuh"
    in 5 * 60 + 59 until 7 * 60 + 11 -> "Subuh, menuju Syuruk"
    in 7 * 60 + 11 until 13 * 60 + 21 -> "Pagi, selepas Syuruk"
    in 13 * 60 + 21 until 16 * 60 + 45 -> "Petang, sebelum Asar"
    in 16 * 60 + 45 until 19 * 60 + 29 -> "Petang, menuju Maghrib"
    in 19 * 60 + 29 until 20 * 60 + 42 -> "Senja, menuju Isyak"
    else -> "Malam, selepas Isyak"
}

private fun wrapMinute(minute: Int): Int = ((minute % DayMinutes) + DayMinutes) % DayMinutes
private fun forwardMinuteDistance(from: Int, to: Int): Int = wrapMinute(to - from)
private fun circularMinuteDistance(a: Int, b: Int): Int = minOf(forwardMinuteDistance(a, b), forwardMinuteDistance(b, a))
private fun Color.toArgbCompat(): Int = android.graphics.Color.argb(
    (alpha * 255).roundToInt(),
    (red * 255).roundToInt(),
    (green * 255).roundToInt(),
    (blue * 255).roundToInt(),
)

@Preview(showBackground = true, showSystemUi = true, widthDp = 390, heightDp = 844)
@Composable
private fun TimelineScreenPreview390() {
    BMuslimTheme { TimelineScreen() }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 360, heightDp = 800)
@Composable
private fun TimelineScreenPreview360() {
    BMuslimTheme { TimelineScreen() }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    widthDp = 360,
    heightDp = 640,
    fontScale = 1.3f,
    name = "Compact 360x640 font 1.3",
)
@Composable
private fun TimelineScreenPreviewCompact() {
    BMuslimTheme { TimelineScreen() }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Before dawn 05:30")
@Composable
private fun TimelineScreenPreview0530() {
    BMuslimTheme { TimelineScreen(initialMinute = 5 * 60 + 30) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Morning zikir 06:20")
@Composable
private fun TimelineScreenPreview0620() {
    BMuslimTheme { TimelineScreen(initialMinute = 6 * 60 + 20) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Maghrib priority 19:45")
@Composable
private fun TimelineScreenPreview1945() {
    BMuslimTheme { TimelineScreen(initialMinute = 19 * 60 + 45) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Night reading 22:00")
@Composable
private fun TimelineScreenPreview2200() {
    BMuslimTheme { TimelineScreen(initialMinute = 22 * 60) }
}
