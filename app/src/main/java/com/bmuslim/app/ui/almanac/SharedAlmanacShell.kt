package com.bmuslim.app.ui.almanac

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

private val Pine = Color(0xFF0B4938)
private val DeepPine = Color(0xFF052B23)
private val Saffron = Color(0xFFE4A43A)
private val Paper = Color(0xFFF4F2E9)
private val PaperBright = Color(0xFFFAF8F0)
private val PaperInk = Color(0xFF102A21)
private val PaperMuted = Color(0xFF5B7067)

private data class Sky(val top: Color, val middle: Color, val horizon: Color, val ink: Color, val muted: Color, val rail: Color)
private val Night = Sky(Color(0xFF031D18), Color(0xFF062A24), Color(0xFF123A30), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))
private val Dawn = Sky(Color(0xFF102B2B), Color(0xFF1E4540), Color(0xFF365D55), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))
private val Day = Sky(Color(0xFF173C32), Color(0xFF315B4E), Color(0xFF426A59), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))
private val Dusk = Sky(Color(0xFF061F1D), Color(0xFF123832), Color(0xFF514D39), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))
private val Sunrise = Sky(Color(0xFF173D38), Color(0xFF356158), Color(0xFF4E6A5B), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))
private val LateDay = Sky(Color(0xFF15382F), Color(0xFF365D4C), Color(0xFF626246), Color(0xFFF7F5EC), Color(0xFFDCE2DE), Color(0xFFB8C9C0))

data class SharedAlmanacSnapshot(
    val selectedMinute: Int,
    val actualNowMinute: Int,
    val hasScrubbed: Boolean,
    val selection: AlmanacSelection,
    val points: List<AlmanacPoint>,
    val intervals: List<AlmanacInterval> = emptyList(),
    val compact: Boolean = false,
    val fontComposition: AlmanacFontComposition = AlmanacFontComposition.Regular,
)

/** Canonical flavor-neutral almanac renderer used by both production and demo. */
@Composable
fun SharedAlmanacShell(
    model: SharedAlmanacModel,
    modifier: Modifier = Modifier,
    initialPresentation: AlmanacPresentation = AlmanacPresentation.Glance,
    summary: @Composable (SharedAlmanacSnapshot, Modifier) -> Unit,
    body: @Composable (SharedAlmanacSnapshot, Boolean, Modifier) -> Unit,
) {
    val animationsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    var state by remember(model.modelKey) {
        mutableStateOf(
            AlmanacShellState(
                modelKey = model.modelKey,
                selectedMinute = model.initialMinute,
                actualNowMinute = model.actualNowMinute,
                presentation = initialPresentation,
            ),
        )
    }
    val expansion = remember(model.modelKey) { Animatable(if (initialPresentation == AlmanacPresentation.Folio) 1f else 0f) }
    val scope = rememberCoroutineScope()
    var motionJob by remember { mutableStateOf<Job?>(null) }
    var installedKey by remember { mutableStateOf(model.modelKey) }
    val openFocus = remember(model.modelKey) { FocusRequester() }
    val folioFocus = remember(model.modelKey) { FocusRequester() }
    val view = LocalView.current
    val context = LocalContext.current
    val wheelLearningStore = remember(context) { AlmanacWheelLearningStore(context) }
    var quietGripLearned by remember { mutableStateOf(wheelLearningStore.isQuietGripLearned()) }

    LaunchedEffect(model.modelKey) {
        if (installedKey != model.modelKey) {
            motionJob?.cancel()
            state = reduceAlmanacState(state, AlmanacIntent.ReplaceModel(model.modelKey, model.actualNowMinute))
            expansion.snapTo(0f)
            installedKey = model.modelKey
        }
    }
    LaunchedEffect(model.actualNowMinute, model.followsNow) {
        if (model.followsNow) state = reduceAlmanacState(state, AlmanacIntent.Tick(model.actualNowMinute))
    }

    val selection = remember(model.points, state.selectedMinute) { model.selectionPolicy.select(model.points, state.selectedMinute) }
    val snapshot = SharedAlmanacSnapshot(state.selectedMinute, state.actualNowMinute, state.hasScrubbed, selection, model.points, model.intervals)
    val sky = remember(state.selectedMinute, model.sunriseMinute, model.sunsetMinute) {
        skyAt(state.selectedMinute, model.sunriseMinute, model.sunsetMinute)
    }
    val duration = if (animationsEnabled) 180 else 0
    val top by animateColorAsState(sky.top, tween(duration), label = "shared sky top")
    val middle by animateColorAsState(sky.middle, tween(duration), label = "shared sky middle")
    val horizon by animateColorAsState(sky.horizon, tween(duration), label = "shared sky horizon")

    fun open() {
        if (state.presentation == AlmanacPresentation.Folio) return
        state = reduceAlmanacState(state, AlmanacIntent.Open)
        val ownerKey = state.modelKey
        motionJob?.cancel()
        motionJob = scope.launch {
            if (animationsEnabled) expansion.animateTo(1f, tween(620, easing = CubicBezierEasing(.2f, 0f, 0f, 1f))) else expansion.snapTo(1f)
            if (state.modelKey == ownerKey) {
                withFrameNanos { }
                folioFocus.requestFocus()
                state = reduceAlmanacState(state, AlmanacIntent.FocusSettled(ownerKey))
            }
        }
    }
    fun close() {
        if (state.presentation == AlmanacPresentation.Glance && expansion.value == 0f) return
        state = reduceAlmanacState(state, AlmanacIntent.Close)
        val ownerKey = state.modelKey
        motionJob?.cancel()
        motionJob = scope.launch {
            if (animationsEnabled) expansion.animateTo(0f, tween(460, easing = CubicBezierEasing(.2f, 0f, 0f, 1f))) else expansion.snapTo(0f)
            if (state.modelKey == ownerKey) {
                withFrameNanos { }
                openFocus.requestFocus()
                view.announceForAccessibility(model.copy.closedAnnouncement)
                state = reduceAlmanacState(state, AlmanacIntent.FocusSettled(ownerKey))
            }
        }
    }
    BackHandler(enabled = state.presentation == AlmanacPresentation.Folio || expansion.value > 0f) { close() }

    BoxWithConstraints(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, middle, horizon)))) {
        val density = LocalDensity.current
        val compact = maxHeight <= 700.dp
        val screenWidthDp = maxWidth.value
        val screenHeightDp = maxHeight.value
        val composition = almanacFontComposition(density.fontScale)
        val layoutSnapshot = snapshot.copy(compact = compact, fontComposition = composition)
        val maximum = composition == AlmanacFontComposition.Maximum
        val rootContentHeightDp = if (maximum) maximumGlanceContentHeightDp(screenHeightDp) else screenHeightDp
        val maximumScrollState = rememberScrollState()
        val compactHeader = compact || maximum
        val statusInset = with(density) { WindowInsets.statusBars.getTop(density).toDp().value }
        val measuredHeader = statusInset + if (compactHeader) 74f * density.fontScale.coerceAtMost(1.45f) else 108f * density.fontScale.coerceAtMost(1.25f)
        val scene = almanacSceneGeometry(
            screenWidthDp,
            rootContentHeightDp,
            compact,
            expansion.value,
            maximum,
            measuredHeader,
            largeType = composition == AlmanacFontComposition.Large || composition == AlmanacFontComposition.Comfortable,
        )
        val detailSettled = state.presentation == AlmanacPresentation.Folio && expansion.value >= .999f
        val glanceSettled = state.presentation == AlmanacPresentation.Glance && expansion.value <= .001f
        val handoff = presentationHandoff(expansion.value, when { detailSettled -> AlmanacPresentation.Folio; glanceSettled -> AlmanacPresentation.Glance; else -> null })

        Column(
            Modifier
                .fillMaxSize()
                .then(
                    if (maximum && handoff.semanticOwner == AlmanacSemanticOwner.Wheel) {
                        Modifier.verticalScroll(maximumScrollState)
                    } else Modifier,
                ),
        ) {
        Box(Modifier.fillMaxWidth().height(rootContentHeightDp.dp)) {
            SharedHeader(
                model.header,
                state.actualNowMinute,
                sky.ink,
                sky.muted,
                compactHeader,
                showClock = !maximum,
                Modifier.fillMaxWidth().height(scene.wheelTop.dp).clearAndSetSemantics { },
            )
            SharedWheel(
                model = model,
                state = state,
                selection = selection,
                scene = scene,
                widthDp = screenWidthDp,
                composition = composition,
                handoff = handoff,
                quietGripLearned = quietGripLearned,
                modifier = Modifier.fillMaxWidth().height(scene.viewportHeight.dp).align(Alignment.TopCenter).offset(y = scene.wheelTop.dp).clipToBounds().testTag("almanac-wheel"),
                onIntent = { state = reduceAlmanacState(state, it) },
                onQuietGripLearned = {
                    if (!quietGripLearned) {
                        quietGripLearned = true
                        wheelLearningStore.markQuietGripLearned()
                    }
                },
            )

            val returnToNowVisible = handoff.semanticOwner != AlmanacSemanticOwner.None &&
                state.hasScrubbed && minuteDistance(state.selectedMinute, state.actualNowMinute) >= model.minuteStep
            if (returnToNowVisible) {
                Button(
                    onClick = { state = reduceAlmanacState(state, AlmanacIntent.ReturnToNow) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = returnToNowTopDp(scene).dp)
                        .height(48.dp)
                        .testTag("return-to-now"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = sky.ink.copy(alpha = .82f)),
                    shape = RoundedCornerShape(0.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) { Text(model.copy.returnToNow, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
            }

            if (wheelOpenActionVisible(handoff, state.dragging)) {
                Button(
                    onClick = ::open,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = openActionTopDp(scene, returnToNowVisible).dp).heightIn(min = 48.dp).focusRequester(openFocus).testTag("open-almanac").semantics {
                        role = Role.Button
                        contentDescription = model.copy.openDescription(state.selectedMinute)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Saffron),
                    border = BorderStroke(1.dp, Saffron.copy(alpha = .65f)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp),
                ) { Text(model.copy.openLabel(state.selectedMinute), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) }
            }

            if (handoff.folioVisible) {
                val topDp = scene.clipBottom + (screenHeightDp - scene.clipBottom) * (1f - ((expansion.value - .34f) / .66f).coerceIn(0f, 1f))
                Surface(
                    modifier = Modifier.fillMaxWidth().height((screenHeightDp - topDp).coerceAtLeast(0f).dp).align(Alignment.TopCenter).offset(y = topDp.dp).alpha(handoff.folioAlpha),
                    color = PaperBright,
                    contentColor = PaperInk,
                ) {
                    val interactive = handoff.semanticOwner == AlmanacSemanticOwner.Folio
                    if (almanacDetailScrollPolicy(composition) == AlmanacDetailScrollPolicy.SingleOuterScroll) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().then(if (interactive) Modifier else Modifier.clearAndSetSemantics { }).testTag("maximum-detail-flow"),
                            userScrollEnabled = interactive,
                        ) {
                            item {
                                summary(layoutSnapshot, Modifier.fillMaxWidth().alpha(revealFraction(AlmanacRevealLayer.Summary, expansion.value)).focusRequester(folioFocus).focusable().testTag("almanac-summary"))
                            }
                            item {
                                body(layoutSnapshot, false, Modifier.fillMaxWidth().alpha(revealFraction(AlmanacRevealLayer.Body, expansion.value)).testTag("almanac-body"))
                            }
                            if (interactive) item { CloseAction(model.copy, ::close) }
                            if (interactive) item { SharedNavigation(model.copy, Modifier.alpha(revealFraction(AlmanacRevealLayer.Navigation, expansion.value))) }
                        }
                    } else {
                        Column(Modifier.fillMaxSize().then(if (interactive) Modifier else Modifier.clearAndSetSemantics { })) {
                            summary(layoutSnapshot, Modifier.fillMaxWidth().alpha(revealFraction(AlmanacRevealLayer.Summary, expansion.value)).focusRequester(folioFocus).focusable().testTag("almanac-summary"))
                            body(layoutSnapshot, true, Modifier.fillMaxWidth().weight(1f).alpha(revealFraction(AlmanacRevealLayer.Body, expansion.value)).testTag("almanac-body"))
                            if (interactive) CloseAction(model.copy, ::close)
                            if (interactive) SharedNavigation(model.copy, Modifier.alpha(revealFraction(AlmanacRevealLayer.Navigation, expansion.value)))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun SharedHeader(header: AlmanacHeader, minute: Int, ink: Color, muted: Color, compact: Boolean, showClock: Boolean, modifier: Modifier) {
    if (compact) Row(modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 5.dp), Arrangement.SpaceBetween, Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(header.brand, color = ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 19.sp)
            Text(header.dateLine, color = muted, fontWeight = FontWeight.Medium, fontSize = 9.sp, lineHeight = 11.sp)
            header.secondaryDateLine?.let { Text(it, color = muted, fontSize = 9.sp, lineHeight = 11.sp) }
        }
        if (showClock) Column(horizontalAlignment = Alignment.End) {
            Text(formatMinute(minute), color = ink, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 31.sp, letterSpacing = (-1.2).sp)
            Text(header.timeContext(minute, false), color = muted, fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    } else Column(modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(header.brand, color = ink, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 25.sp)
        Text(listOfNotNull(header.dateLine, header.secondaryDateLine).joinToString(" · "), color = muted, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1)
        if (showClock) {
            Text(formatMinute(minute), color = ink, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 38.sp, lineHeight = 40.sp, letterSpacing = (-1.4).sp)
            Text(header.timeContext(minute, false), color = muted, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SharedWheel(
    model: SharedAlmanacModel,
    state: AlmanacShellState,
    selection: AlmanacSelection,
    scene: AlmanacSceneGeometry,
    widthDp: Float,
    composition: AlmanacFontComposition,
    handoff: AlmanacPresentationHandoff,
    quietGripLearned: Boolean,
    modifier: Modifier,
    onIntent: (AlmanacIntent) -> Unit,
    onQuietGripLearned: () -> Unit,
) {
    var rawDrag by remember(model.modelKey) { mutableFloatStateOf(state.selectedMinute.toFloat()) }
    val currentSelectedMinute by rememberUpdatedState(state.selectedMinute)
    val textMeasurer = rememberTextMeasurer()
    val glance = scene.progress < .15f
    val visiblePoints = remember(model.points, selection, composition, glance) {
        val ids = radialLabelIds(model.points, selection, composition, glance).toSet()
        model.points.filter { it.id in ids }
    }
    val labels = remember(visiblePoints, composition) {
        visiblePoints.mapNotNull { point ->
            radialLabelText(point)?.let { point.id to textMeasurer.measure(it, TextStyle(fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold)) }
        }.toMap()
    }
    val readout = model.wheelReadout(selection, state.selectedMinute, state.dragging)
    val density = LocalDensity.current
    val readoutTextWidthDp = if (composition == AlmanacFontComposition.Maximum) {
        (widthDp - 40f).coerceAtLeast(160f)
    } else {
        minOf(190f, widthDp - 40f)
    }
    val readoutWidthPx = with(density) { readoutTextWidthDp.dp.roundToPx() }
    val primaryReadout = remember(readout.first, readoutWidthPx, composition) {
        textMeasurer.measure(
            readout.first,
            TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
            overflow = TextOverflow.Ellipsis,
            softWrap = composition != AlmanacFontComposition.Maximum,
            maxLines = if (composition == AlmanacFontComposition.Maximum) 1 else Int.MAX_VALUE,
            constraints = Constraints.fixedWidth(readoutWidthPx),
        )
    }
    val secondaryReadout = remember(readout.second, readoutWidthPx, composition) {
        readout.second?.let {
            textMeasurer.measure(
                it,
                TextStyle(fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
                overflow = TextOverflow.Ellipsis,
                softWrap = composition != AlmanacFontComposition.Maximum,
                maxLines = if (composition == AlmanacFontComposition.Maximum) 1 else Int.MAX_VALUE,
                constraints = Constraints.fixedWidth(readoutWidthPx),
            )
        }
    }
    val readoutBay = remember(scene, composition, primaryReadout, secondaryReadout) {
        readoutBayGeometry(
            widthDp = widthDp,
            scene = scene,
            measuredWidthDp = with(density) { maxOf(primaryReadout.size.width, secondaryReadout?.size?.width ?: 0).toDp().value + 8f },
            measuredHeightDp = with(density) { (primaryReadout.size.height + (secondaryReadout?.size?.height ?: 0)).toDp().value + 6f },
            external = composition == AlmanacFontComposition.Maximum,
        )
    }
    val hourScale = remember { almanacHourScale() }
    val hourLabels = remember(hourScale) {
        hourScale.mapNotNull { mark ->
            mark.label?.let { label ->
                mark.hour to textMeasurer.measure(
                    label,
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 7.sp,
                        lineHeight = 8.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }.toMap()
    }
    val eventCountLayouts = remember(model.intervals, composition, state.selectedMinute) {
        model.intervals
            .mapNotNull { interval ->
                val count = if (intervalSemanticallyContains(interval, state.selectedMinute)) {
                    interval.memberCountAt(state.selectedMinute)
                } else {
                    interval.memberCount
                }
                interval.takeIf { it.visual == AlmanacIntervalVisual.EventTrigger && count > 1 }?.let { it to count }
            }
            .associate { interval ->
                interval.first.id to textMeasurer.measure(
                    interval.second.toString(),
                    TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = when (composition) {
                            AlmanacFontComposition.Regular -> 6.sp
                            AlmanacFontComposition.Comfortable,
                            AlmanacFontComposition.Large,
                            -> 7.sp
                            AlmanacFontComposition.Maximum -> 8.sp
                        },
                        lineHeight = 8.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
    }
    val interactionEnabled = wheelOwnsInteraction(handoff)
    val maximumKey = maximumEventKeyPresentation(composition, handoff)
    val quietGrip = quietGripPresentation(scene, handoff, quietGripLearned, state.dragging, composition)
    Box(modifier) {
        Canvas(
            Modifier.fillMaxSize().then(
                if (!interactionEnabled) Modifier.clearAndSetSemantics { }
                else Modifier.semantics {
                    contentDescription = model.copy.wheelDescription
                    stateDescription = model.wheelStateDescription(
                        selection,
                        state.selectedMinute,
                        state.hasScrubbed || state.dragging,
                    )
                    progressBarRangeInfo = ProgressBarRangeInfo(state.selectedMinute.toFloat(), accessibleMinuteRange(), accessibleMinuteSteps(model.minuteStep))
                    setProgress { requested -> onIntent(AlmanacIntent.Scrub(quantizeAccessibleMinute(requested, model.minuteStep))); true }
                    customActions = listOfNotNull(
                        previousPoint(model.points, state.selectedMinute)?.let { point ->
                            CustomAccessibilityAction("Peristiwa sebelumnya: ${point.title}") {
                                onIntent(AlmanacIntent.Scrub(point.minute)); true
                            }
                        },
                        nextPoint(model.points, state.selectedMinute)?.let { point ->
                            CustomAccessibilityAction("Peristiwa seterusnya: ${point.title}") {
                                onIntent(AlmanacIntent.Scrub(point.minute)); true
                            }
                        },
                    )
                },
            ).then(
                if (interactionEnabled) {
                    Modifier.pointerInput(model.modelKey, interactionEnabled) {
                        var learningGesture = beginQuietGripGesture(currentSelectedMinute)
                        detectHorizontalDragGestures(
                            onDragStart = {
                                rawDrag = currentSelectedMinute.toFloat()
                                learningGesture = beginQuietGripGesture(currentSelectedMinute)
                                onIntent(AlmanacIntent.DragStarted)
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                rawDrag = normalizeMinute((rawDrag - amount * 3f).roundToInt()).toFloat()
                                val quantizedMinute = quantizeMinute(rawDrag.roundToInt(), model.minuteStep)
                                learningGesture = recordQuietGripQuantizedMinute(learningGesture, quantizedMinute)
                                onIntent(AlmanacIntent.Scrub(quantizedMinute))
                            },
                            onDragEnd = {
                                onIntent(AlmanacIntent.DragEnded)
                                if (quietGripShouldRetire(learningGesture, AlmanacQuietGripGestureEnd.Completed)) {
                                    onQuietGripLearned()
                                }
                            },
                            onDragCancel = { onIntent(AlmanacIntent.DragEnded) },
                        )
                    }
                } else {
                    Modifier
                },
            ),
        ) {
            drawInstrument(
                model,
                state,
                selection,
                scene,
                labels,
                hourScale,
                hourLabels,
                eventCountLayouts,
                readoutBay,
                primaryReadout,
                secondaryReadout,
                handoff.wheelAlpha,
                composition,
                showKeystone = !quietGrip.visible,
            )
        }
        if (quietGrip.visible) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .alpha(quietGrip.visualAlpha)
                    .clearAndSetSemantics { },
            ) {
                val center = Offset(size.width / 2f, scene.centerY.dp.toPx())
                listOf(72f, 102f).forEach { angle ->
                    drawArc(
                        color = Saffron,
                        startAngle = angle,
                        sweepAngle = 6f,
                        useCenter = false,
                        topLeft = Offset(center.x - scene.outerRadius.dp.toPx(), center.y - scene.outerRadius.dp.toPx()),
                        size = Size(scene.outerRadius.dp.toPx() * 2f, scene.outerRadius.dp.toPx() * 2f),
                        style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
            Text(
                text = model.copy.wheelHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .offset(y = quietGrip.lineTopDp.dp)
                    .height(quietGrip.lineHeightDp.dp)
                    .alpha(quietGrip.visualAlpha)
                    .clearAndSetSemantics { }
                    .testTag("quiet-grip-hint"),
                color = skyAt(state.selectedMinute, model.sunriseMinute, model.sunsetMinute).ink,
                fontSize = if (composition == AlmanacFontComposition.Maximum) 7.5.sp else 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
        if (maximumKey.visualAlpha > .001f) {
            val entries = externalEventKey(model.points)
            val split = (entries.size + 1) / 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = ALMANAC_MAXIMUM_KEY_TOP_DP.dp)
                    .height(ALMANAC_MAXIMUM_KEY_HEIGHT_DP.dp)
                    .padding(horizontal = 16.dp)
                    .alpha(maximumKey.visualAlpha)
                    .then(
                        if (maximumKey.ownsSemantics) Modifier.semantics {
                            contentDescription = model.points.joinToString(". ") { it.accessibilityText }
                        } else Modifier.clearAndSetSemantics { },
                    )
                    .testTag("maximum-event-key"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                listOf(entries.take(split), entries.drop(split)).forEach { columnEntries ->
                    Column(Modifier.weight(1f)) {
                        columnEntries.forEach { entry ->
                            Text(
                                entry,
                                color = skyAt(state.selectedMinute, model.sunriseMinute, model.sunsetMinute).ink.copy(alpha = .82f),
                                fontSize = 7.5.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawInstrument(
    model: SharedAlmanacModel,
    state: AlmanacShellState,
    selection: AlmanacSelection,
    scene: AlmanacSceneGeometry,
    labels: Map<String, androidx.compose.ui.text.TextLayoutResult>,
    hourScale: List<AlmanacHourScaleMark>,
    hourLabels: Map<Int, androidx.compose.ui.text.TextLayoutResult>,
    eventCountLayouts: Map<String, androidx.compose.ui.text.TextLayoutResult>,
    readoutBay: AlmanacReadoutBay,
    primaryReadout: androidx.compose.ui.text.TextLayoutResult,
    secondaryReadout: androidx.compose.ui.text.TextLayoutResult?,
    alpha: Float,
    composition: AlmanacFontComposition,
    showKeystone: Boolean,
) {
    val center = Offset(size.width / 2f, scene.centerY.dp.toPx())
    val celestialRadius = scene.outerRadius.dp.toPx()
    val primaryRadius = celestialRadius * .775f
    val secondaryRadius = celestialRadius * .88f
    val hourRadius = celestialRadius * .63f
    val sky = skyAt(state.selectedMinute, model.sunriseMinute, model.sunsetMinute)
    val exclusion = readoutExclusionAtAlpha(readoutBay, alpha)
    val drawNonOwner: DrawScope.() -> Unit = {
    drawCircle(sky.rail.copy(alpha = .14f), hourRadius, center, style = Stroke(.7.dp.toPx()))
    hourScale.forEach { mark ->
        val angle = minuteAngle(mark.minute, state.selectedMinute)
        val outer = polar(center, hourRadius, angle)
        val inner = polar(center, hourRadius - mark.tickLengthDp.dp.toPx(), angle)
        drawLine(
            color = sky.rail.copy(alpha = if (mark.label == null) .32f else .58f),
            start = inner,
            end = outer,
            strokeWidth = if (mark.label == null) .7.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        hourLabels[mark.hour]?.let { layout ->
            val labelCenter = polar(center, hourRadius - 13.dp.toPx(), angle)
            drawText(
                layout,
                color = sky.muted.copy(alpha = .64f),
                topLeft = Offset(
                    labelCenter.x - layout.size.width / 2f,
                    labelCenter.y - layout.size.height / 2f,
                ),
            )
        }
    }
    // These two rails encode event category; intervals remain continuous bars on their own lanes.
    drawCircle(sky.rail.copy(alpha = .55f), primaryRadius, center, style = Stroke(1.2.dp.toPx()))
    drawCircle(sky.rail.copy(alpha = .28f), secondaryRadius, center, style = Stroke(.9.dp.toPx()))
    model.intervals.forEach { interval ->
        val radius = if (interval.visual == AlmanacIntervalVisual.EventTrigger) {
            celestialRadius * .86f
        } else {
            adaptiveIntervalLaneRadius(scene.outerRadius, interval.lane, composition).dp.toPx()
        }
        val primitive = intervalPrimitive(interval.visual)
        val exactIntersection = intervalSemanticallyContains(interval, state.selectedMinute)
        val selectedInterval = interval.emphasized
        val intervalColor = when {
            selectedInterval -> Saffron.copy(alpha = .92f)
            interval.visual == AlmanacIntervalVisual.EventTrigger && exactIntersection -> Saffron.copy(alpha = .86f)
            interval.visual == AlmanacIntervalVisual.EventTrigger -> sky.rail.copy(alpha = .62f)
            interval.visual != AlmanacIntervalVisual.PublishedWindow -> sky.rail.copy(alpha = .62f)
            else -> sky.rail.copy(alpha = if (interval.emphasized) .52f else .28f)
        }
        if (primitive == AlmanacIntervalPrimitive.HollowEventTab) {
            drawHollowEventTab(
                center = center,
                radius = radius,
                angle = minuteAngle(interval.markerMinute, state.selectedMinute),
                color = intervalColor,
                emphasized = interval.emphasized,
                composition = composition,
                countLayout = eventCountLayouts[interval.id],
            )
        } else {
            val coverage = interval.coverageSegments.ifEmpty {
                listOf(AlmanacCoverageSegment(interval.startMinute, interval.endMinute))
            }
            coverage.forEach { segment ->
                val startAngle = minuteAngle(segment.startMinute, state.selectedMinute)
                val sweepAngle = forwardDistance(segment.startMinute, segment.endMinute) / 4f
                drawArc(
                    color = intervalColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(
                        width = when {
                            selectedInterval -> 7.dp.toPx()
                            interval.emphasized -> 5.5.dp.toPx()
                            primitive == AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal -> 6.dp.toPx()
                            else -> 4.dp.toPx()
                        },
                        cap = StrokeCap.Round,
                    ),
                )
                if (primitive == AlmanacIntervalPrimitive.StripedArcWithHollowBead ||
                    primitive == AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal
                ) {
                    drawIntervalHatching(
                        center = center,
                        radius = radius,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        color = intervalColor,
                        spacingDegrees = if (primitive == AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal) 5f else 8f,
                    )
                }
                val segmentMarkerMinute = if (primitive == AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal) {
                    segment.endMinute
                } else {
                    interval.markerMinute
                }
                val markerPosition = polar(center, radius, minuteAngle(segmentMarkerMinute, state.selectedMinute))
                when (primitive) {
                    AlmanacIntervalPrimitive.SolidArc -> drawCircle(
                        intervalColor,
                        2.7.dp.toPx(),
                        markerPosition,
                    )
                    AlmanacIntervalPrimitive.ContinuousArcWithHollowBead,
                    AlmanacIntervalPrimitive.StripedArcWithHollowBead -> drawCircle(
                        intervalColor,
                        3.2.dp.toPx(),
                        markerPosition,
                        style = Stroke(1.4.dp.toPx()),
                    )
                    AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal -> drawTerminalDiamond(
                        markerPosition,
                        intervalColor,
                        interval.emphasized,
                    )
                    AlmanacIntervalPrimitive.HollowEventTab -> Unit
                }
            }
        }
        if (exactIntersection) drawExactContactTick(center, radius, state.selectedMinute)
    }
    val measuredCandidates = model.points.mapNotNull { point ->
        labelCandidate(point, labels[point.id], center, if (point.role == AlmanacVisualRole.Primary) primaryRadius else secondaryRadius, state.selectedMinute)
    }
    val priorityIds = listOfNotNull(selection.exact?.id ?: selection.previous?.id, selection.next?.id) + model.points.map(AlmanacPoint::id)
    val labelExclusion = exclusion?.let { AlmanacLabelBounds(it.left.dp.toPx(), it.top.dp.toPx(), it.right.dp.toPx(), it.bottom.dp.toPx()) }
    val placements = resolveAllLabelPlacements(
        measuredCandidates, priorityIds, size.width, size.height, 8.dp.toPx(), labelExclusion,
    ).associateBy { it.id }
    model.points.forEach { point ->
        val primary = point.role == AlmanacVisualRole.Primary
        val radius = if (primary) primaryRadius else secondaryRadius
        val pos = polar(center, radius, minuteAngle(point.minute, state.selectedMinute))
        val emphasized = point.id == selection.previous?.id || point.id == selection.next?.id || point.id == selection.exact?.id
        if (emphasized) drawCircle(Saffron.copy(alpha = .20f), if (primary) 11.dp.toPx() else 9.dp.toPx(), pos)
        drawPointMarker(pointMarker(point), pos, if (emphasized) Saffron else Paper, emphasized)
        labels[point.id]?.let { layout ->
            val top = placements[point.id]?.bounds?.let { Offset(it.left, it.top) } ?: return@let
            val bounds = AlmanacSceneRect(top.x / density, top.y / density, (top.x + layout.size.width) / density, (top.y + layout.size.height) / density)
            if (primitiveIsOutsideExclusion(bounds, exclusion)) drawText(layout, if (emphasized) Saffron else sky.ink.copy(alpha = alpha), top)
        }
    }
    drawCircle(sky.rail.copy(alpha = .22f), celestialRadius, center, style = Stroke(.8.dp.toPx()))
    val nowAngle = minuteAngle(state.actualNowMinute, state.selectedMinute)
    val nowRadius = primaryRadius - 18.dp.toPx()
    val nowOuter = polar(center, nowRadius, nowAngle)
    val nowInner = polar(center, nowRadius - 9.dp.toPx(), nowAngle)
    drawLine(Paper, nowInner, nowOuter, 3.dp.toPx(), StrokeCap.Round)
    drawCircle(DeepPine, 2.2.dp.toPx(), nowOuter)
    if (scene.progress < .15f && showKeystone) drawKeystone(center, celestialRadius, sky)
    }
    drawNonOwner()
    val pointerBottom = Offset(center.x, center.y - primaryRadius)
    val pointerTop = Offset(center.x, center.y - secondaryRadius - 13.dp.toPx())
    drawLine(Saffron, pointerTop, pointerBottom, if (state.dragging) 2.8.dp.toPx() else 2.dp.toPx(), StrokeCap.Round)
    drawCircle(Saffron, 4.dp.toPx(), pointerBottom)
    val leader = Path().apply {
        moveTo(readoutBay.leaderStartX.dp.toPx(), pointerTop.y)
        lineTo(readoutBay.leaderBendX.dp.toPx(), readoutBay.leaderY.dp.toPx())
        lineTo(readoutBay.leaderEndX.dp.toPx(), readoutBay.leaderY.dp.toPx())
    }
    drawPath(leader, Saffron.copy(alpha = .74f * alpha), style = Stroke(.9.dp.toPx(), cap = StrokeCap.Round))
    val readoutTop = Offset((readoutBay.bounds.left + 4f).dp.toPx(), (readoutBay.bounds.top + 3f).dp.toPx())
    drawText(primaryReadout, color = sky.ink.copy(alpha = alpha), topLeft = readoutTop)
    secondaryReadout?.let { drawText(it, color = sky.muted.copy(alpha = alpha), topLeft = Offset(readoutTop.x, readoutTop.y + primaryReadout.size.height)) }
}

private fun DrawScope.drawPointMarker(
    marker: AlmanacPointMarker,
    position: Offset,
    color: Color,
    emphasized: Boolean,
) {
    val stroke = if (emphasized) 2.dp.toPx() else 1.35.dp.toPx()
    when (marker) {
        AlmanacPointMarker.PrimaryPrayer -> {
            drawCircle(color, 5.5.dp.toPx(), position)
            drawCircle(DeepPine, 3.1.dp.toPx(), position)
        }
        AlmanacPointMarker.Secondary -> drawCircle(color, 4.2.dp.toPx(), position, style = Stroke(stroke))
        AlmanacPointMarker.ImsakNotches -> {
            val half = 4.5.dp.toPx()
            drawLine(color, Offset(position.x - 2.4.dp.toPx(), position.y - half), Offset(position.x - 2.4.dp.toPx(), position.y + half), stroke, StrokeCap.Round)
            drawLine(color, Offset(position.x + 2.4.dp.toPx(), position.y - half), Offset(position.x + 2.4.dp.toPx(), position.y + half), stroke, StrokeCap.Round)
        }
        AlmanacPointMarker.SyurukHorizon -> {
            drawCircle(color, 4.dp.toPx(), position, style = Stroke(stroke))
            drawLine(color, Offset(position.x - 7.dp.toPx(), position.y), Offset(position.x + 7.dp.toPx(), position.y), stroke)
        }
        AlmanacPointMarker.DhuhaDiamond -> {
            val half = 5.dp.toPx()
            val diamond = Path().apply {
                moveTo(position.x, position.y - half)
                lineTo(position.x + half, position.y)
                lineTo(position.x, position.y + half)
                lineTo(position.x - half, position.y)
                close()
            }
            drawPath(diamond, color, style = Stroke(stroke))
        }
    }
}

private fun DrawScope.drawHollowEventTab(
    center: Offset,
    radius: Float,
    angle: Float,
    color: Color,
    emphasized: Boolean,
    composition: AlmanacFontComposition,
    countLayout: androidx.compose.ui.text.TextLayoutResult?,
) {
    val position = polar(center, radius, angle)
    val glyphSize = eventGlyphSize(composition)
    val widthDp = glyphSize.widthDp
    val heightDp = glyphSize.heightDp
    val width = widthDp.dp.toPx()
    val height = heightDp.dp.toPx()
    rotate(angle + 90f, pivot = position) {
        drawRect(
            color = color,
            topLeft = Offset(position.x - width / 2f, position.y - height / 2f),
            size = Size(width, height),
            style = Stroke(if (emphasized) 2.2.dp.toPx() else 1.5.dp.toPx()),
        )
    }
    countLayout?.let { layout ->
        drawText(
            layout,
            color = color,
            topLeft = Offset(position.x - layout.size.width / 2f, position.y - layout.size.height / 2f),
        )
    }
}

private fun DrawScope.drawExactContactTick(
    center: Offset,
    radius: Float,
    selectedMinute: Int,
) {
    val angle = minuteAngle(selectedMinute, selectedMinute)
    drawLine(
        color = Saffron.copy(alpha = .92f),
        start = polar(center, radius - 5.dp.toPx(), angle),
        end = polar(center, radius + 5.dp.toPx(), angle),
        strokeWidth = 1.1.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawIntervalHatching(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color,
    spacingDegrees: Float,
) {
    var offset = spacingDegrees / 2f
    while (offset < sweepAngle) {
        val angle = startAngle + offset
        drawLine(
            color = color.copy(alpha = .82f),
            start = polar(center, radius - 4.dp.toPx(), angle),
            end = polar(center, radius + 4.dp.toPx(), angle),
            strokeWidth = .9.dp.toPx(),
        )
        offset += spacingDegrees
    }
}

private fun DrawScope.drawTerminalDiamond(position: Offset, color: Color, emphasized: Boolean) {
    val half = if (emphasized) 5.dp.toPx() else 4.dp.toPx()
    val diamond = Path().apply {
        moveTo(position.x, position.y - half)
        lineTo(position.x + half, position.y)
        lineTo(position.x, position.y + half)
        lineTo(position.x - half, position.y)
        close()
    }
    drawPath(diamond, color, style = Stroke(if (emphasized) 2.dp.toPx() else 1.3.dp.toPx()))
}

private fun DrawScope.labelCandidate(point: AlmanacPoint, layout: androidx.compose.ui.text.TextLayoutResult?, center: Offset, radius: Float, selectedMinute: Int): AlmanacLabelCandidate? {
    layout ?: return null
    val position = polar(center, radius - 20.dp.toPx(), minuteAngle(point.minute, selectedMinute))
    return AlmanacLabelCandidate(point.id, AlmanacLabelBounds(position.x - layout.size.width / 2f, position.y - layout.size.height / 2f, position.x + layout.size.width / 2f, position.y + layout.size.height / 2f))
}

private fun DrawScope.drawKeystone(center: Offset, radius: Float, sky: Sky) {
    val y = center.y + radius - 4.dp.toPx()
    val half = 21.dp.toPx()
    val depth = 13.dp.toPx()
    val path = Path().apply { moveTo(center.x - half, y); lineTo(center.x - half * .72f, y + depth); lineTo(center.x + half * .72f, y + depth); lineTo(center.x + half, y); close() }
    drawPath(path, sky.middle)
    drawPath(path, Saffron.copy(alpha = .72f), style = Stroke(1.dp.toPx()))
    drawLine(Saffron.copy(alpha = .8f), Offset(center.x, y + 2.dp.toPx()), Offset(center.x, y + 9.dp.toPx()), 1.dp.toPx())
}

@Composable
private fun CloseAction(copy: AlmanacCopy, onClose: () -> Unit) {
    Button(
        onClick = onClose,
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("close-almanac").semantics { role = Role.Button; contentDescription = copy.closeDescription },
        colors = ButtonDefaults.buttonColors(containerColor = DeepPine, contentColor = Saffron),
        shape = RoundedCornerShape(0.dp),
    ) { Text(copy.closeLabel, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) }
}

@Composable
private fun SharedNavigation(copy: AlmanacCopy, modifier: Modifier = Modifier) {
    Surface(modifier.testTag("bottom-navigation"), color = DeepPine, contentColor = PaperInk) {
        Column {
            Row(Modifier.fillMaxWidth().height(62.dp).background(PaperBright).padding(horizontal = 18.dp), Arrangement.SpaceAround, Alignment.CenterVertically) {
                NavText(copy.todayLabel, true, true)
                NavText(copy.plannerLabel, false, false)
                NavText(copy.settingsLabel, false, false)
            }
            Spacer(Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}

@Composable
private fun NavText(label: String, active: Boolean, enabled: Boolean) {
    Column(Modifier.size(width = 92.dp, height = 56.dp).semantics {
        contentDescription = if (enabled) label else "$label, belum tersedia"
        stateDescription = if (active) "Halaman semasa" else "Tidak aktif"
    }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(20.dp).background(if (active) Pine else PaperMuted.copy(alpha = .35f), CircleShape))
        Spacer(Modifier.height(4.dp))
        Text(label, color = if (active) Pine else PaperMuted, fontSize = 10.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.width(18.dp).height(2.dp).background(if (active) Saffron else Color.Transparent))
    }
}

private fun skyAt(minute: Int, sunrise: Int, sunset: Int): Sky {
    val value = normalizeMinute(minute)
    val stops = listOf(
        0 to Night,
        (sunrise - 90).coerceAtLeast(1) to Dawn,
        sunrise to Sunrise,
        (sunrise + 110).coerceAtMost(sunset - 2) to Day,
        (sunset - 165).coerceAtLeast(sunrise + 111) to LateDay,
        sunset to Dusk,
        (sunset + 120).coerceAtMost(1439) to Night,
        1440 to Night,
    ).distinctBy { it.first }.sortedBy { it.first }
    val index = stops.indexOfLast { it.first <= value }.coerceIn(0, stops.lastIndex - 1)
    val from = stops[index]
    val to = stops[index + 1]
    val fraction = ((value - from.first).toFloat() / (to.first - from.first).coerceAtLeast(1)).coerceIn(0f, 1f)
    fun color(a: Color, b: Color) = androidx.compose.ui.graphics.lerp(a, b, fraction)
    return Sky(color(from.second.top, to.second.top), color(from.second.middle, to.second.middle), color(from.second.horizon, to.second.horizon), color(from.second.ink, to.second.ink), color(from.second.muted, to.second.muted), color(from.second.rail, to.second.rail))
}

private fun polar(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = degrees * PI.toFloat() / 180f
    return Offset(center.x + radius * cos(radians), center.y + radius * sin(radians))
}

private fun forwardDistance(from: Int, to: Int) = normalizeMinute(to - from).coerceAtLeast(1)
