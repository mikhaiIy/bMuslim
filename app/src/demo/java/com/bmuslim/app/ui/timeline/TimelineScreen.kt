package com.bmuslim.app.ui.timeline

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.focusable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Constraints
import com.bmuslim.app.ui.theme.BMuslimTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch

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

private data class HeaderMeasurements(
    val requiredHeightDp: Float,
    val clockRect: SceneRect,
)

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
    initialPresentation: HomePresentation = HomePresentation.Glance,
) {
    val deeds = remember { sampleDeeds() }
    val dayStates = remember { sampleDayStates() }
    var completedIds by rememberSaveable(
        stateSaver = listSaver(save = { it }, restore = { it }),
    ) { mutableStateOf(emptyList<String>()) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(quantizeCircularMinute(initialMinute)) }
    var presentation by rememberSaveable { mutableStateOf(initialPresentation) }
    var pendingFocus by remember { mutableStateOf<HomePresentation?>(null) }
    val active = remember(deeds, selectedMinute) { activeDeedsAt(deeds, selectedMinute) }
    val prayer = remember(deeds, selectedMinute) { obligatoryPrayerContextAt(deeds, selectedMinute) }
    val next = remember(deeds, selectedMinute) { nextDeedAt(deeds, selectedMinute) }
    val contextual = remember(deeds, selectedMinute) { contextualDeedAt(deeds, selectedMinute) }
    val primary = active.firstOrNull() ?: next
    val palette = remember(selectedMinute) { paletteAt(selectedMinute) }
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    val expansion = remember { Animatable(if (presentation == HomePresentation.Detail) 1f else 0f) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val openFocus = remember { FocusRequester() }
    val summaryFocus = remember { FocusRequester() }
    val duration = if (animationsEnabled) 180 else 0
    val top by animateColorAsState(palette.top, tween(duration), label = "sky top")
    val middle by animateColorAsState(palette.middle, tween(duration), label = "sky middle")
    val horizon by animateColorAsState(palette.horizon, tween(duration), label = "sky horizon")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, middle, horizon))),
    ) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val compact = maxHeight <= 700.dp
        val composition = fontComposition(density.fontScale)
        val maximumType = composition == FontComposition.Maximum
        val screenWidthDp = maxWidth.value
        val screenHeightDp = maxHeight.value
        val compactHeader = compact || maximumType
        val statusInsetPx = WindowInsets.statusBars.getTop(density)
        val headerMeasurements = remember(
            selectedMinute,
            compactHeader,
            density.density,
            density.fontScale,
            screenWidthDp,
            statusInsetPx,
        ) {
            val brandStyle = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = if (compactHeader) 17.sp else 23.sp,
                lineHeight = if (compactHeader) 19.sp else 25.sp,
            )
            val dateStyle = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = if (compactHeader) 9.sp else 10.sp,
                lineHeight = if (compactHeader) 11.sp else 12.sp,
            )
            val clockStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = if (compactHeader) 28.sp else 38.sp,
                lineHeight = if (compactHeader) 31.sp else 40.sp,
                letterSpacing = if (compactHeader) (-1.2).sp else (-1.4).sp,
            )
            val dayStyle = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compactHeader) 9.sp else 10.sp,
                lineHeight = if (compactHeader) 11.sp else 12.sp,
            )
            val brand = textMeasurer.measure("bMuslim", brandStyle)
            val date = textMeasurer.measure(
                if (compactHeader) "Khamis, 2 Julai 2026\n17 Muharram 1448 H" else "Khamis, 2 Julai 2026 · 17 Muharram 1448 H",
                dateStyle,
                constraints = Constraints(maxWidth = with(density) { (screenWidthDp - 32f).dp.roundToPx() }),
            )
            val clock = textMeasurer.measure(formatMinute(selectedMinute), clockStyle)
            val day = textMeasurer.measure(dayPart(selectedMinute), dayStyle)
            val contentHeightPx = if (compactHeader) {
                maxOf(brand.size.height + date.size.height, clock.size.height + day.size.height)
            } else {
                brand.size.height + date.size.height + clock.size.height + day.size.height
            }
            val requiredDp = requiredHeaderHeight(
                safeInsetTop = with(density) { statusInsetPx.toDp().value },
                measuredContentHeight = with(density) { contentHeightPx.toDp().value },
            )
            val clockTopPx = statusInsetPx + with(density) { 5.dp.roundToPx() } +
                if (compactHeader) 0 else brand.size.height + date.size.height
            val clockLeftPx = if (compactHeader) {
                with(density) { (screenWidthDp - 16f).dp.roundToPx() } - clock.size.width
            } else {
                (with(density) { screenWidthDp.dp.roundToPx() } - clock.size.width) / 2
            }
            HeaderMeasurements(
                requiredHeightDp = requiredDp,
                clockRect = SceneRect(
                    with(density) { clockLeftPx.toDp().value },
                    with(density) { clockTopPx.toDp().value },
                    with(density) { (clockLeftPx + clock.size.width).toDp().value },
                    with(density) { (clockTopPx + clock.size.height).toDp().value },
                ),
            )
        }
        val scene = homeSceneGeometry(
            screenWidthDp,
            screenHeightDp,
            compact,
            expansion.value,
            maximumType,
            measuredHeaderHeightDp = headerMeasurements.requiredHeightDp,
        )
        val progress = expansion.value
        val detailSettled = presentation == HomePresentation.Detail && progress >= .999f
        val glanceSettled = presentation == HomePresentation.Glance && progress <= .001f
        val handoff = presentationHandoff(
            progress,
            settledPresentation = when {
                detailSettled -> HomePresentation.Detail
                glanceSettled -> HomePresentation.Glance
                else -> null
            },
        )
        check(headerSafeGeometry(scene, headerMeasurements.clockRect).isSeparated) {
            "Measured header clock must remain above the wheel aperture"
        }
        LaunchedEffect(pendingFocus, detailSettled, glanceSettled) {
            val destination = pendingFocus ?: return@LaunchedEffect
            val settled = when (destination) {
                HomePresentation.Detail -> detailSettled
                HomePresentation.Glance -> glanceSettled
            }
            if (!settled) return@LaunchedEffect
            withFrameNanos { }
            when (destination) {
                HomePresentation.Detail -> {
                    summaryFocus.requestFocus()
                }
                HomePresentation.Glance -> {
                    openFocus.requestFocus()
                    view.announceForAccessibility("Paparan amalan ditutup")
                }
            }
            pendingFocus = null
        }
        val openPresentation: () -> Unit = {
            if (presentation != HomePresentation.Detail) {
                presentation = HomePresentation.Detail
                pendingFocus = HomePresentation.Detail
                scope.launch {
                    if (animationsEnabled) {
                        expansion.animateTo(
                            1f,
                            tween(620, easing = CubicBezierEasing(.2f, 0f, 0f, 1f)),
                        )
                    } else {
                        expansion.snapTo(1f)
                    }
                }
            }
            Unit
        }
        val closePresentation: () -> Unit = {
            if (presentation != HomePresentation.Glance) {
                presentation = HomePresentation.Glance
                pendingFocus = HomePresentation.Glance
                scope.launch {
                    if (animationsEnabled) {
                        expansion.animateTo(
                            0f,
                            tween(460, easing = CubicBezierEasing(.2f, 0f, 0f, 1f)),
                        )
                    } else {
                        expansion.snapTo(0f)
                    }
                }
            }
            Unit
        }
        BackHandler(enabled = presentation == HomePresentation.Detail || progress > 0f) {
            closePresentation()
        }

        Box(Modifier.fillMaxSize()) {
            AlmanacHeader(
                selectedMinute = selectedMinute,
                ink = palette.ink,
                muted = palette.muted,
                compact = compactHeader,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scene.wheelTop.dp)
                    .clearAndSetSemantics { },
            )
            TimeInstrument(
                deeds = deeds,
                selectedMinute = selectedMinute,
                palette = palette,
                scene = scene,
                widthDp = screenWidthDp,
                composition = composition,
                handoff = handoff,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scene.viewportHeight.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
                    .offset(y = scene.wheelTop.dp)
                    .clipToBounds()
                    .testTag("timeline-wheel"),
                onSelectedMinute = { selectedMinute = quantizeCircularMinute(it) },
                onReturnToNow = { selectedMinute = NowMinute },
            )

            if (handoff.semanticOwner == SemanticOwner.Wheel) {
                val actionReveal = ((.30f - progress) / .30f).coerceIn(0f, 1f)
                OpenDeedsAction(
                    selectedMinute = selectedMinute,
                    active = active,
                    next = next,
                    settled = glanceSettled,
                    onOpen = openPresentation,
                    focusRequester = openFocus,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (scene.clipBottom + 16f + (1f - actionReveal) * 8f).dp)
                        .alpha(actionReveal)
                )
            }

            if (handoff.detailVisible) {
                val contentTop = detailContentTop(screenHeightDp, scene)
                val contentHeight = (screenHeightDp - contentTop).coerceAtLeast(0f)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(contentHeight.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = contentTop.dp)
                        .alpha(handoff.detailAlpha),
                    color = PaperBright,
                    contentColor = PaperInk,
                ) {
                    if (detailScrollPolicy(composition) == DetailScrollPolicy.SingleOuterScroll) {
                        val flowSemantics = if (handoff.detailInteractive) Modifier else Modifier.clearAndSetSemantics { }
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(flowSemantics)
                                .testTag("maximum-detail-flow"),
                            userScrollEnabled = handoff.detailInteractive,
                        ) {
                            if (revealFraction(RevealLayer.Summary, progress) > .05f) {
                                item {
                                    val summaryModifier = if (handoff.detailInteractive) {
                                        Modifier
                                            .alpha(revealFraction(RevealLayer.Summary, progress))
                                            .focusRequester(summaryFocus)
                                            .focusable()
                                            .testTag("moment-summary")
                                    } else {
                                        Modifier
                                            .alpha(revealFraction(RevealLayer.Summary, progress))
                                            .clearAndSetSemantics { }
                                            .testTag("moment-summary-transition")
                                    }
                                    AdaptiveContextDisclosure(
                                        selectedMinute = selectedMinute,
                                        prayer = prayer,
                                        contextual = contextual,
                                        dayState = dayStates.first(),
                                        modifier = summaryModifier,
                                    )
                                }
                            }
                            if (revealFraction(RevealLayer.Ledger, progress) > .05f) {
                                item {
                                    val ledgerSemantics = if (handoff.detailInteractive) Modifier else Modifier.clearAndSetSemantics { }
                                    LivingLedger(
                                        deed = primary,
                                        isActive = primary in active,
                                        isDone = primary.id in completedIds,
                                        animationsEnabled = animationsEnabled,
                                        compact = true,
                                        internallyScrollable = false,
                                        onToggle = {
                                            completedIds = if (primary.id in completedIds) completedIds - primary.id else completedIds + primary.id
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .alpha(revealFraction(RevealLayer.Ledger, progress))
                                            .then(ledgerSemantics)
                                            .testTag("deed-ledger"),
                                    )
                                }
                            }
                            if (progress > .70f && handoff.detailInteractive) {
                                item {
                                    CloseDeedsButton(
                                        onClose = closePresentation,
                                        modifier = Modifier.alpha(((progress - .70f) / .18f).coerceIn(0f, 1f)),
                                    )
                                }
                            }
                            if (revealFraction(RevealLayer.Navigation, progress) > .05f && handoff.detailInteractive) {
                                item {
                                    FolioNavigation(
                                        modifier = Modifier.alpha(revealFraction(RevealLayer.Navigation, progress)),
                                    )
                                }
                            }
                        }
                    } else Column(Modifier.fillMaxSize()) {
                        if (revealFraction(RevealLayer.Summary, progress) > .05f) {
                            val summaryModifier = if (handoff.detailInteractive) {
                                Modifier
                                    .alpha(revealFraction(RevealLayer.Summary, progress))
                                    .focusRequester(summaryFocus)
                                    .focusable()
                                    .testTag("moment-summary")
                            } else {
                                Modifier
                                    .alpha(revealFraction(RevealLayer.Summary, progress))
                                    .clearAndSetSemantics { }
                                    .testTag("moment-summary-transition")
                            }
                            if (compact) {
                                CompactContextStrip(
                                    selectedMinute = selectedMinute,
                                    active = active,
                                    prayer = prayer,
                                    contextual = contextual,
                                    dayState = dayStates.first(),
                                    modifier = summaryModifier,
                                )
                            } else {
                                Column(summaryModifier) {
                                    MomentSummary(
                                        selectedMinute = selectedMinute,
                                        active = active,
                                        prayer = prayer,
                                        contextual = contextual,
                                        dayState = dayStates.first(),
                                    )
                                    ActiveDeedIndex(active = active, selectedMinute = selectedMinute)
                                }
                            }
                        }
                        if (revealFraction(RevealLayer.Ledger, progress) > .05f) {
                            val ledgerSemantics = if (handoff.detailInteractive) Modifier else Modifier.clearAndSetSemantics { }
                            LivingLedger(
                                deed = primary,
                                isActive = primary in active,
                                isDone = primary.id in completedIds,
                                animationsEnabled = animationsEnabled,
                                compact = compact,
                                internallyScrollable = true,
                                onToggle = {
                                    completedIds = if (primary.id in completedIds) completedIds - primary.id else completedIds + primary.id
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(revealFraction(RevealLayer.Ledger, progress))
                                    .then(ledgerSemantics)
                                    .testTag("deed-ledger"),
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                        if (progress > .70f && handoff.detailInteractive) {
                            CloseDeedsButton(
                                onClose = closePresentation,
                                modifier = Modifier.alpha(((progress - .70f) / .18f).coerceIn(0f, 1f)),
                            )
                        }
                        if (revealFraction(RevealLayer.Navigation, progress) > .05f && handoff.detailInteractive) {
                            FolioNavigation(
                                modifier = Modifier.alpha(revealFraction(RevealLayer.Navigation, progress)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlmanacHeader(
    selectedMinute: Int,
    ink: Color,
    muted: Color,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (compact) {
        Row(
            modifier = modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text("bMuslim", color = ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 19.sp)
                Text("Khamis, 2 Julai 2026", color = muted, fontWeight = FontWeight.Medium, fontSize = 9.sp, lineHeight = 11.sp)
                Text("17 Muharram 1448 H", color = muted, fontSize = 9.sp, lineHeight = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatMinute(selectedMinute),
                    color = ink,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    letterSpacing = (-1.2).sp,
                )
                Text(dayPart(selectedMinute), color = muted, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        Column(
            modifier = modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("bMuslim", color = ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 25.sp)
            Text("Khamis, 2 Julai 2026 · 17 Muharram 1448 H", color = muted, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1)
            Text(
                formatMinute(selectedMinute),
                color = ink,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 38.sp,
                lineHeight = 40.sp,
                letterSpacing = (-1.4).sp,
            )
            Text(dayPart(selectedMinute), color = muted, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TimeInstrument(
    deeds: List<TimelineDeed>,
    selectedMinute: Int,
    palette: SkyPalette,
    scene: HomeSceneGeometry,
    widthDp: Float,
    composition: FontComposition,
    handoff: PresentationHandoff,
    modifier: Modifier,
    onSelectedMinute: (Int) -> Unit,
    onReturnToNow: () -> Unit,
) {
    var dragMinute by remember { mutableIntStateOf(selectedMinute) }
    var rawDragMinute by remember { mutableStateOf(selectedMinute.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val currentSelectedMinute by rememberUpdatedState(selectedMinute)
    val currentOnSelectedMinute by rememberUpdatedState(onSelectedMinute)
    val readout = remember(deeds, selectedMinute, isDragging) {
        wheelReadoutAt(deeds, selectedMinute, directManipulation = isDragging)
    }
    val externalReadout = composition == FontComposition.Maximum
    val readoutMaxWidthPx = with(density) {
        (if (externalReadout) widthDp - 32f else widthDp / 2f - 46f).dp.roundToPx()
    }.coerceAtLeast(1)
    val primaryLayout = remember(readout?.primary, composition, readoutMaxWidthPx, density.fontScale) {
        readout?.let {
            textMeasurer.measure(
                it.primary,
                TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold),
                constraints = Constraints(maxWidth = readoutMaxWidthPx),
            )
        }
    }
    val secondaryLayout = remember(readout?.secondary, composition, readoutMaxWidthPx, density.fontScale) {
        readout?.secondary?.let {
            textMeasurer.measure(
                it,
                TextStyle(fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Medium),
                constraints = Constraints(maxWidth = readoutMaxWidthPx),
            )
        }
    }
    val measuredReadoutWidthDp = with(density) {
        maxOf(primaryLayout?.size?.width ?: 0, secondaryLayout?.size?.width ?: 0).toDp().value + 8f
    }
    val measuredReadoutHeightDp = with(density) {
        ((primaryLayout?.size?.height ?: 0) + (secondaryLayout?.size?.height ?: 0)).toDp().value + 6f
    }
    val readoutBay = remember(scene, widthDp, externalReadout, measuredReadoutWidthDp, measuredReadoutHeightDp) {
        readoutBayGeometry(
            widthDp = widthDp,
            scene = scene,
            measuredWidthDp = measuredReadoutWidthDp,
            measuredHeightDp = measuredReadoutHeightDp,
            external = externalReadout,
        )
    }
    val hourLayouts = remember(composition, density.fontScale) {
        (0 until 24 step 3).associate { hour ->
            val label = hour.toString().padStart(2, '0')
            label to textMeasurer.measure(label, TextStyle(fontSize = 8.5.sp, lineHeight = 10.sp))
        }
    }
    val prayers = remember(deeds) { deeds.filter { it.ruling == Ruling.Fardh } }
    val activePrayer = remember(prayers, selectedMinute) { activeDeedsAt(prayers, selectedMinute).firstOrNull() }
    val nextPrayer = remember(deeds, selectedMinute) { obligatoryPrayerContextAt(deeds, selectedMinute) }
    val glanceLabels = scene.progress < .15f
    val prayerLayouts = remember(prayers, activePrayer?.id, nextPrayer.id, glanceLabels, composition, density.fontScale) {
        prayers.associate { prayer ->
            val activePrayerLabel = prayer.id == activePrayer?.id
            val label = if (glanceLabels) {
                "${prayer.title.removePrefix("Solat ")} ${formatMinute(prayer.startMinute)}"
            } else if (activePrayerLabel) {
                prayer.title.removePrefix("Solat ")
            } else {
                "${prayer.title.removePrefix("Solat ")} ${formatMinute(prayer.startMinute)}"
            }
            prayer.id to textMeasurer.measure(
                label,
                TextStyle(fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold),
            )
        }
    }
    val prayerKeyLayout = remember(prayers, composition, widthDp, density.fontScale) {
        if (composition == FontComposition.Maximum) {
            textMeasurer.measure(
                prayerKeyTitles(prayers).joinToString(" · "),
                TextStyle(fontSize = 8.5.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold),
                constraints = Constraints(maxWidth = with(density) { (widthDp - 32f).dp.roundToPx() }),
            )
        } else {
            null
        }
    }
    Box(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (handoff.semanticOwner != SemanticOwner.Wheel) {
                        Modifier.clearAndSetSemantics { }
                    } else {
                        Modifier.semantics {
                            contentDescription = "Roda waktu 24 jam. Leret mendatar atau laras untuk memilih masa."
                            stateDescription = wheelStateDescription(deeds, selectedMinute)
                            progressBarRangeInfo = ProgressBarRangeInfo(selectedMinute.coerceIn(0, 1435).toFloat(), 0f..1435f, 286)
                            setProgress { requested ->
                                onSelectedMinute(quantizeAccessibleMinute(requested))
                                true
                            }
                        }
                    },
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragMinute = currentSelectedMinute
                            rawDragMinute = currentSelectedMinute.toFloat()
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            rawDragMinute -= dragAmount * 3f
                            while (rawDragMinute < 0f) rawDragMinute += DayMinutes
                            while (rawDragMinute >= DayMinutes) rawDragMinute -= DayMinutes
                            dragMinute = quantizeCircularMinute(rawDragMinute.roundToInt())
                            currentOnSelectedMinute(dragMinute)
                        },
                        onDragEnd = {
                            isDragging = false
                            dragMinute = quantizeCircularMinute(dragMinute)
                            currentOnSelectedMinute(dragMinute)
                        },
                        onDragCancel = { isDragging = false },
                    )
                },
        ) {
            drawTimeInstrument(
                deeds = deeds,
                selectedMinute = selectedMinute,
                palette = palette,
                scene = scene,
                isDragging = isDragging,
                readoutBay = if (handoff.wheelVisible && readout != null) readoutBay else null,
                primaryReadout = primaryLayout,
                secondaryReadout = secondaryLayout,
                hourLayouts = hourLayouts,
                prayerLayouts = prayerLayouts,
                prayerKeyLayout = prayerKeyLayout,
                composition = composition,
                readoutAlpha = handoff.wheelAlpha,
            )
        }

        if (returnToNowVisible(handoff, circularMinuteDistance(selectedMinute, NowMinute))) {
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
    scene: HomeSceneGeometry,
    isDragging: Boolean,
    readoutBay: ReadoutBayGeometry?,
    primaryReadout: TextLayoutResult?,
    secondaryReadout: TextLayoutResult?,
    hourLayouts: Map<String, TextLayoutResult>,
    prayerLayouts: Map<String, TextLayoutResult>,
    prayerKeyLayout: TextLayoutResult?,
    composition: FontComposition,
    readoutAlpha: Float,
) {
    val center = Offset(size.width / 2f, scene.centerY.dp.toPx())
    val celestialRadius = scene.outerRadius.dp.toPx()
    val hourRadius = celestialRadius * .64f
    val prayerRadius = celestialRadius * .775f
    val deedBaseRadius = celestialRadius * .88f
    val laneStep = 10.dp.toPx()
    val prayerPolicy = prayerDisplayPolicy(composition, scene.progress)
    val exclusion = readoutBay?.takeUnless { it.external }?.bounds
    fun allowed(left: Float, top: Float, right: Float, bottom: Float): Boolean =
        primitiveIsOutsideExclusion(
            SceneRect(left / density, top / density, right / density, bottom / density),
            exclusion,
        )
    fun allowed(point: Offset, radius: Float): Boolean =
        allowed(point.x - radius, point.y - radius, point.x + radius, point.y + radius)

    val drawNonOwnerLayers: DrawScope.() -> Unit = {
    drawCircle(
        color = palette.rail.copy(alpha = .06f),
        radius = deedBaseRadius + 10.dp.toPx(),
        center = center,
        style = Stroke(18.dp.toPx()),
    )

    // Layer 1: the neutral 24-hour rail.
    drawCircle(palette.rail.copy(alpha = .42f), hourRadius, center, style = Stroke(1.dp.toPx()))
    for (hour in 0 until 24) {
        val angle = timelineMinuteAngle(hour * 60, selectedMinute)
        val major = hour % 3 == 0
        val outer = polar(center, hourRadius, angle)
        val inner = polar(center, hourRadius - if (major) 10.dp.toPx() else 5.dp.toPx(), angle)
        if (allowed(minOf(inner.x, outer.x) - 2.dp.toPx(), minOf(inner.y, outer.y) - 2.dp.toPx(), maxOf(inner.x, outer.x) + 2.dp.toPx(), maxOf(inner.y, outer.y) + 2.dp.toPx())) {
            drawLine(
                palette.rail.copy(alpha = if (major) .86f else .38f),
                inner,
                outer,
                if (major) 1.3.dp.toPx() else .8.dp.toPx(),
            )
        }
        val hourLabelInset = 18.dp.toPx()
        val showHour = major && when (composition) {
            FontComposition.Regular, FontComposition.Comfortable -> true
            FontComposition.Large, FontComposition.Maximum -> hour % 6 == 0
        }
        if (showHour && outer.x in hourLabelInset..(size.width - hourLabelInset) && outer.y in 5.dp.toPx()..size.height) {
            val layout = hourLayouts.getValue(hour.toString().padStart(2, '0'))
            val labelCenter = polar(center, hourRadius - 19.dp.toPx(), angle)
            val topLeft = Offset(labelCenter.x - layout.size.width / 2f, labelCenter.y - layout.size.height / 2f)
            if (allowed(topLeft.x, topLeft.y, topLeft.x + layout.size.width, topLeft.y + layout.size.height)) {
                drawText(layout, color = palette.muted, topLeft = topLeft)
            }
        }
    }

    // Layer 2: obligatory prayer context.
    drawCircle(palette.rail.copy(alpha = .55f), prayerRadius, center, style = Stroke(1.2.dp.toPx()))
    val prayers = deeds.filter { it.ruling == Ruling.Fardh }
    val activePrayer = activeDeedsAt(prayers, selectedMinute).firstOrNull()
    val nextPrayer = obligatoryPrayerContextAt(deeds, selectedMinute)
    val glance = scene.progress < .15f
    prayers.forEach { prayer ->
        val angle = timelineMinuteAngle(prayer.startMinute, selectedMinute)
        val point = polar(center, prayerRadius, angle)
        val active = prayer.id == activePrayer?.id
        val next = !active && prayer.id == nextPrayer.id
        val nodeRadius = if (isDragging && active) 14.dp.toPx() else if (active) 11.dp.toPx() else 6.dp.toPx()
        if (allowed(point, nodeRadius)) {
            if (active) drawCircle(Saffron.copy(alpha = if (isDragging) .30f else .22f), if (isDragging) 14.dp.toPx() else 11.dp.toPx(), point)
            if (next) drawCircle(palette.ink.copy(alpha = .20f), 9.dp.toPx(), point, style = Stroke(1.dp.toPx()))
            drawCircle(if (active) Saffron else Paper, if (active) 6.dp.toPx() else 4.8.dp.toPx(), point)
            drawCircle(if (active) DeepPine else Pine, if (active) 4.dp.toPx() else 3.1.dp.toPx(), point)
        }
        val prayerLabelInset = 42.dp.toPx()
        val showPrayer = prayerLabelVisible(prayerPolicy, glance, active, next)
        if (showPrayer && point.x in prayerLabelInset..(size.width - prayerLabelInset) && point.y in 12.dp.toPx()..size.height) {
            val layout = prayerLayouts.getValue(prayer.id)
            val labelCenter = polar(center, prayerRadius - 19.dp.toPx(), angle)
            val topLeft = Offset(labelCenter.x - layout.size.width / 2f, labelCenter.y - layout.size.height / 2f)
            if (allowed(topLeft.x, topLeft.y, topLeft.x + layout.size.width, topLeft.y + layout.size.height)) {
                drawText(layout, color = if (active) Saffron else palette.ink, topLeft = topLeft)
            }
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
        val bead = polar(center, radius, timelineMinuteAngle(deed.actMinute, selectedMinute))
        val beadRadius = if (active) 4.dp.toPx() else 2.3.dp.toPx()
        if (allowed(bead, beadRadius)) drawCircle(if (active) Saffron else palette.rail.copy(alpha = .7f), beadRadius, bead)
    }
    // The celestial body shares one concentric rail and never changes orbit radius.
    drawCircle(palette.rail.copy(alpha = .22f), celestialRadius, center, style = Stroke(.8.dp.toPx()))
    drawCelestial(center, celestialRadius, selectedMinute, palette, exclusion)

    // Actual-now and the glance keystone are context marks, not readout owners.
    val nowAngle = timelineMinuteAngle(NowMinute, selectedMinute)
    val nowOuter = polar(center, hourRadius, nowAngle)
    val nowInner = polar(center, hourRadius - 9.dp.toPx(), nowAngle)
    drawLine(Paper, nowInner, nowOuter, 3.dp.toPx(), StrokeCap.Round)
    drawCircle(DeepPine, 2.2.dp.toPx(), nowOuter)
    if (glance) drawKeystone(center, celestialRadius, palette)
    }

    if (exclusion != null) {
        val cutout = Path().apply {
            addRect(
                androidx.compose.ui.geometry.Rect(
                    exclusion.left.dp.toPx(),
                    exclusion.top.dp.toPx(),
                    exclusion.right.dp.toPx(),
                    exclusion.bottom.dp.toPx(),
                ),
            )
        }
        clipPath(cutout, clipOp = ClipOp.Difference) { drawNonOwnerLayers() }
    } else {
        drawNonOwnerLayers()
    }

    if (prayerPolicy == PrayerDisplayPolicy.FiveNameKey) {
        prayerKeyLayout?.let { key ->
            drawText(
                key,
                color = palette.muted,
                topLeft = Offset(16.dp.toPx(), 8.dp.toPx()),
            )
        }
    }

    // Fixed selected-time pointer terminates precisely on the prayer rail.
    val pointerTop = Offset(center.x, center.y - deedBaseRadius - 13.dp.toPx())
    val pointerBottom = Offset(center.x, center.y - prayerRadius)
    drawLine(Saffron, pointerTop, pointerBottom, 2.dp.toPx(), StrokeCap.Round)
    drawCircle(Saffron, 4.dp.toPx(), pointerBottom)

    readoutBay?.let { bay ->
        val leaderStart = Offset(bay.leaderStartX.dp.toPx(), pointerTop.y)
        val leaderBend = Offset(bay.leaderBendX.dp.toPx(), bay.leaderY.dp.toPx())
        val leaderEnd = Offset(bay.leaderEndX.dp.toPx(), bay.leaderY.dp.toPx())
        val leader = Path().apply {
            moveTo(leaderStart.x, leaderStart.y)
            lineTo(leaderBend.x, leaderBend.y)
            lineTo(leaderEnd.x, leaderEnd.y)
        }
        drawPath(leader, Saffron.copy(alpha = .74f * readoutAlpha), style = Stroke(.9.dp.toPx(), cap = StrokeCap.Round))
        primaryReadout?.let { layout ->
            val topLeft = Offset((bay.bounds.left + 4f).dp.toPx(), (bay.bounds.top + 3f).dp.toPx())
            drawText(layout, color = palette.ink.copy(alpha = readoutAlpha), topLeft = topLeft)
            secondaryReadout?.let { secondary ->
                drawText(
                    secondary,
                    color = palette.muted.copy(alpha = readoutAlpha),
                    topLeft = Offset(topLeft.x, topLeft.y + layout.size.height),
                )
            }
        }
    }

}

private fun DrawScope.drawKeystone(center: Offset, radius: Float, palette: SkyPalette) {
    val y = center.y + radius - 4.dp.toPx()
    val half = 21.dp.toPx()
    val depth = 13.dp.toPx()
    val path = Path().apply {
        moveTo(center.x - half, y)
        lineTo(center.x - half * .72f, y + depth)
        lineTo(center.x + half * .72f, y + depth)
        lineTo(center.x + half, y)
        close()
    }
    drawPath(path, palette.middle)
    drawPath(path, Saffron.copy(alpha = .72f), style = Stroke(1.dp.toPx()))
    drawLine(Saffron.copy(alpha = .8f), Offset(center.x, y + 2.dp.toPx()), Offset(center.x, y + 9.dp.toPx()), 1.dp.toPx())
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
        startAngle = timelineMinuteAngle(startMinute, selectedMinute),
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
    exclusion: SceneRect?,
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
    val exclusionRadius = 13.dp.toPx()
    if (!primitiveIsOutsideExclusion(
            SceneRect(
                (point.x - exclusionRadius) / density,
                (point.y - exclusionRadius) / density,
                (point.x + exclusionRadius) / density,
                (point.y + exclusionRadius) / density,
            ),
            exclusion,
        )
    ) return
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
                    accent = contextualIsActive && !prayer.isActiveAt(selectedMinute),
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
    modifier: Modifier = Modifier,
) {
    val contextualIsActive = contextual.isActiveAt(selectedMinute)
    Surface(color = PaperBright, contentColor = PaperInk) {
        Row(
            modifier = modifier
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
                accent = contextualIsActive && !prayer.isActiveAt(selectedMinute),
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
private fun AdaptiveContextDisclosure(
    selectedMinute: Int,
    prayer: TimelineDeed,
    contextual: TimelineDeed,
    dayState: DayState,
    modifier: Modifier = Modifier,
) {
    val prayerIsActive = prayer.isActiveAt(selectedMinute)
    val contextualIsActive = contextual.isActiveAt(selectedMinute)
    Surface(color = PaperBright, contentColor = PaperInk) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = momentSummaryDescription(
                        selectedMinute,
                        active = listOfNotNull(
                            prayer.takeIf { prayerIsActive },
                            contextual.takeIf { contextualIsActive },
                        ),
                        prayer = prayer,
                        contextual = contextual,
                        dayState = dayState,
                    )
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Text(
                if (prayerIsActive) prayer.title.removePrefix("Solat ") else "Solat berikut · ${prayer.title.removePrefix("Solat ")}",
                color = Pine,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 25.sp,
            )
            Text(
                if (prayerIsActive) "Waktu ${prayer.title.removePrefix("Solat ")} · ${formatMinute(selectedMinute)}" else formatMinute(prayer.startMinute),
                color = PaperInk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            )
            Text(
                if (contextualIsActive) "Amalan berkaitan · ${contextual.title}" else "Amalan berikut · ${contextual.title}",
                color = PaperMuted,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                "Khamis, 2 Julai 2026 · 17 Muharram 1448 H · ${dayState.title}",
                color = PaperMuted,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
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
    internallyScrollable: Boolean,
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
        Box(if (internallyScrollable) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
            Canvas(Modifier.matchParentSize()) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (internallyScrollable) {
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        },
                    )
                    .padding(start = 29.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp),
            ) {
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
            if (compact) {
                CompletionAction(deed, isDone, completion, onToggle)
            }
                Text("PANDUAN RINGKAS", color = PaperMuted, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .6.sp)
                Text(deed.description, color = PaperInk, fontSize = 13.sp, lineHeight = 19.sp)
                Text(archetypeLabel(deed), color = PaperMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            if (deed.arabic != null) {
                Column {
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
                CompletionAction(deed, isDone, completion, onToggle)
            }
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
private fun FolioNavigation(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.testTag("bottom-navigation"), color = PaperBright, contentColor = PaperInk) {
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
private fun OpenDeedsAction(
    selectedMinute: Int,
    active: List<TimelineDeed>,
    next: TimelineDeed,
    settled: Boolean,
    onOpen: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    if (settled) {
        Button(
            onClick = onOpen,
            modifier = modifier
                .height(48.dp)
                .focusRequester(focusRequester)
                .testTag("open-deeds")
                .semantics {
                    role = Role.Button
                    contentDescription = openActionDescription(active, next)
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Saffron),
            border = BorderStroke(1.dp, Saffron.copy(alpha = .65f)),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 22.dp),
        ) {
            Text("Amalan pada ${formatMinute(selectedMinute)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    } else {
        Surface(
            modifier = modifier.height(48.dp).clearAndSetSemantics { },
            color = Color.Transparent,
            contentColor = Saffron,
            border = BorderStroke(1.dp, Saffron.copy(alpha = .65f)),
            shape = RoundedCornerShape(4.dp),
        ) {
            Box(Modifier.padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
                Text("Amalan pada ${formatMinute(selectedMinute)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CloseDeedsButton(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClose,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("close-deeds")
            .semantics {
                role = Role.Button
                contentDescription = "Tutup amalan dan kembali ke roda penuh"
            },
        colors = ButtonDefaults.buttonColors(containerColor = DeepPine, contentColor = Saffron),
        shape = RoundedCornerShape(0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Canvas(Modifier.size(16.dp)) {
            drawLine(Saffron, Offset(size.width * .2f, size.height * .38f), Offset(size.width * .5f, size.height * .67f), 1.7.dp.toPx(), StrokeCap.Round)
            drawLine(Saffron, Offset(size.width * .5f, size.height * .67f), Offset(size.width * .8f, size.height * .38f), 1.7.dp.toPx(), StrokeCap.Round)
        }
        Spacer(Modifier.width(9.dp))
        Text("Tutup amalan", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
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

internal fun wheelStateDescription(deeds: List<TimelineDeed>, minute: Int): String {
    val active = activeDeedsAt(deeds, minute)
    val prayer = obligatoryPrayerContextAt(deeds, minute)
    val contextual = active.filter { it.ruling != Ruling.Fardh }
    val prayerText = if (prayer.isActiveAt(minute)) {
        "Waktu ${prayer.title.removePrefix("Solat ")}"
    } else {
        "Solat berikut ${prayer.title.removePrefix("Solat ")} pada ${formatMinute(prayer.startMinute)}"
    }
    val contextualText = if (contextual.isEmpty()) {
        "Tiada amalan berkaitan yang aktif"
    } else {
        "Amalan berkaitan ${contextual.joinToString { it.title }}"
    }
    return "Masa dipilih ${formatMinute(minute)}. $prayerText. $contextualText."
}

internal fun openActionDescription(
    active: List<TimelineDeed>,
    next: TimelineDeed,
): String {
    val context = active.firstOrNull()?.title ?: "amalan berikut ${next.title}"
    return "Buka butiran amalan. $context."
}

private fun momentSummaryDescription(
    minute: Int,
    active: List<TimelineDeed>,
    prayer: TimelineDeed,
    contextual: TimelineDeed,
    dayState: DayState,
): String {
    val contextualActive = active.filter { it.ruling != Ruling.Fardh }
    val activeText = if (contextualActive.isEmpty()) {
        "Amalan berikut ${contextual.title}"
    } else {
        "Amalan berkaitan ${contextualActive.joinToString { it.title }}"
    }
    val prayerText = if (prayer.isActiveAt(minute)) {
        "Waktu ${prayer.title.removePrefix("Solat ")}"
    } else {
        "Solat berikut ${prayer.title.removePrefix("Solat ")} pada ${formatMinute(prayer.startMinute)}"
    }
    return "Masa dipilih ${formatMinute(minute)}. $prayerText. $activeText. Keadaan hari: ${dayState.title}."
}

private fun archetypeLabel(deed: TimelineDeed): String = when (deed.archetype) {
    RenderArchetype.Point -> "Tindakan sekitar ${deed.actMinutes} minit dalam ruang masa ini"
    RenderArchetype.Window -> "Boleh diamalkan sepanjang ruang masa ini"
    RenderArchetype.DaySpan -> "Amalan sepanjang hari"
    RenderArchetype.Anytime -> "Boleh dilakukan bila-bila masa"
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
    BMuslimTheme { TimelineScreen(initialPresentation = HomePresentation.Detail) }
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
    BMuslimTheme { TimelineScreen(initialMinute = 19 * 60 + 45, initialPresentation = HomePresentation.Detail) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Night reading 22:00")
@Composable
private fun TimelineScreenPreview2200() {
    BMuslimTheme { TimelineScreen(initialMinute = 22 * 60) }
}
