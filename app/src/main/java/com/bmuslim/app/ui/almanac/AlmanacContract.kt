package com.bmuslim.app.ui.almanac

import kotlin.math.abs
import kotlin.math.roundToInt

const val ALMANAC_DAY_MINUTES = 24 * 60
const val ALMANAC_LAST_MINUTE = ALMANAC_DAY_MINUTES - 1
const val ALMANAC_MAXIMUM_KEY_TOP_DP = 78f
const val ALMANAC_MAXIMUM_KEY_HEIGHT_DP = 104f

enum class AlmanacPresentation { Glance, Folio }
enum class AlmanacVisualRole { Primary, Secondary, Context }
enum class AlmanacPointMarker { PrimaryPrayer, Secondary, ImsakNotches, SyurukHorizon, DhuhaDiamond }
enum class AlmanacSemanticOwner { Wheel, Folio, None }
enum class AlmanacFontComposition { Regular, Comfortable, Large, Maximum }
enum class AlmanacDetailScrollPolicy { WeightedBody, SingleOuterScroll }
enum class AlmanacRevealLayer { Wheel, Summary, Body, Navigation }
enum class AlmanacPrimitiveLayer { BroadRail, HourRail, EventRail, ContextualArc, CelestialRail, ActualNow, Keystone, Pointer, Leader, Readout }
enum class AlmanacIntervalVisual {
    PublishedWindow,
    NamedPeriodEstimate,
    NightSegmentEstimate,
    SearchEnvelope,
    EventTrigger,
}

enum class AlmanacIntervalTruthClass {
    Direct,
    PolicyMapped,
    ProvisionalResearch,
    ScheduleContextProxy,
    EventRequired,
    Unresolved,
}

enum class AlmanacIntervalPrimitive {
    SolidArc,
    ContinuousArcWithHollowBead,
    StripedArcWithHollowBead,
    HatchedEnvelopeWithTerminal,
    HollowEventTab,
}

enum class AlmanacIntervalOccurrence {
    Active,
    PolicyWindow,
    DraftWindow,
    ScheduleContext,
    SearchRegion,
    Trigger,
    Upcoming,
    UpcomingPolicyWindow,
    UpcomingDraftWindow,
    UpcomingScheduleContext,
    UpcomingSearchRegion,
    UpcomingTrigger,
    Unresolved,
}

data class AlmanacPoint(
    val id: String,
    val title: String,
    val minute: Int,
    val role: AlmanacVisualRole,
    val accessibilityText: String,
)

data class AlmanacCoverageSegment(
    val startMinute: Int,
    val endMinute: Int,
) {
    init {
        require(startMinute in 0 until ALMANAC_DAY_MINUTES)
        require(endMinute in 0 until ALMANAC_DAY_MINUTES)
    }
}

/** Optional visual input. A point is never expanded into an interval by shared code. */
data class AlmanacInterval(
    val id: String,
    val startMinute: Int,
    val endMinute: Int,
    val markerMinute: Int,
    val emphasized: Boolean = false,
    val lane: Int = 0,
    val visual: AlmanacIntervalVisual = AlmanacIntervalVisual.PublishedWindow,
    val truthClass: AlmanacIntervalTruthClass = AlmanacIntervalTruthClass.PolicyMapped,
    val deedId: String? = null,
    val accessibilityText: String? = null,
    val title: String? = null,
    val timingLabel: String? = null,
    val groupKey: String? = null,
    val memberCount: Int = 1,
    val memberIds: List<String> = emptyList(),
    val memberTitles: List<String> = emptyList(),
    val coverageSegments: List<AlmanacCoverageSegment> = emptyList(),
    val isOverflowAggregate: Boolean = false,
    /** Optional exact coverage for aggregate members; the Canvas may still draw [coverageSegments]. */
    val memberCoverage: Map<String, List<AlmanacCoverageSegment>> = emptyMap(),
)

data class AlmanacHeader(
    val brand: String,
    val dateLine: String,
    val secondaryDateLine: String? = null,
    val timeContext: (Int, Boolean) -> String,
)

data class AlmanacCopy(
    val wheelDescription: String,
    val wheelHint: String,
    val openLabel: (Int) -> String,
    val openDescription: (Int) -> String,
    val closeLabel: String,
    val closeDescription: String,
    val closedAnnouncement: String,
    val returnToNow: String,
    val todayLabel: String,
    val plannerLabel: String,
    val settingsLabel: String,
    val unavailableLabel: String,
)

data class AlmanacSelection(
    val previous: AlmanacPoint?,
    val next: AlmanacPoint?,
    val exact: AlmanacPoint?,
    val dayComplete: Boolean,
)

fun interface AlmanacSelectionPolicy {
    fun select(points: List<AlmanacPoint>, minute: Int): AlmanacSelection
}

val NonCircularPointSelection = AlmanacSelectionPolicy { points, requested ->
    val minute = normalizeMinute(requested)
    val sorted = points.sortedBy(AlmanacPoint::minute)
    val exact = sorted.firstOrNull { it.minute == minute }
    val previous = sorted.lastOrNull { it.minute <= minute }
    val next = sorted.firstOrNull { it.minute > minute }
    AlmanacSelection(previous, next, exact, next == null)
}

fun previousPoint(points: List<AlmanacPoint>, requestedMinute: Int): AlmanacPoint? =
    points.filter { it.minute < normalizeMinute(requestedMinute) }
        .maxWithOrNull(compareBy<AlmanacPoint> { it.minute }.thenBy { it.id })

fun nextPoint(points: List<AlmanacPoint>, requestedMinute: Int): AlmanacPoint? =
    points.filter { it.minute > normalizeMinute(requestedMinute) }
        .minWithOrNull(compareBy<AlmanacPoint> { it.minute }.thenBy { it.id })

data class SharedAlmanacModel(
    val modelKey: String,
    val header: AlmanacHeader,
    val points: List<AlmanacPoint>,
    val intervals: List<AlmanacInterval> = emptyList(),
    val initialMinute: Int,
    val actualNowMinute: Int,
    val followsNow: Boolean,
    val minuteStep: Int,
    val sunriseMinute: Int,
    val sunsetMinute: Int,
    val copy: AlmanacCopy,
    val selectionPolicy: AlmanacSelectionPolicy,
    val wheelReadout: (AlmanacSelection, Int, Boolean) -> Pair<String, String?>,
    val wheelStateDescription: (AlmanacSelection, Int, Boolean) -> String,
) {
    init {
        require(points.map(AlmanacPoint::id).distinct().size == points.size)
        require(points.all { it.minute in 0 until ALMANAC_DAY_MINUTES })
        require(intervals.map(AlmanacInterval::id).distinct().size == intervals.size)
        require(intervals.all { it.lane in 0..3 })
        require(intervals.all { it.startMinute in 0 until ALMANAC_DAY_MINUTES })
        require(intervals.all { it.endMinute in 0 until ALMANAC_DAY_MINUTES })
        require(intervals.all { it.markerMinute in 0 until ALMANAC_DAY_MINUTES })
        require(intervals.all { it.memberCount > 0 })
        require(intervals.all { it.memberIds.distinct().size == it.memberIds.size })
        require(intervals.all { it.memberTitles.size <= it.memberCount })
        require(intervals.all { it.coverageSegments.all { segment -> segment.startMinute in 0 until ALMANAC_DAY_MINUTES && segment.endMinute in 0 until ALMANAC_DAY_MINUTES } })
        require(intervals.all { interval -> interval.memberCoverage.keys.all { it in interval.memberIds } })
        require(intervals.all { interval -> interval.memberCoverage.values.flatten().all { segment -> segment.startMinute in 0 until ALMANAC_DAY_MINUTES && segment.endMinute in 0 until ALMANAC_DAY_MINUTES } })
        require(minuteStep > 0)
    }
}

data class AlmanacShellState(
    val modelKey: String,
    val selectedMinute: Int,
    val actualNowMinute: Int,
    val presentation: AlmanacPresentation = AlmanacPresentation.Glance,
    val hasScrubbed: Boolean = false,
    val dragging: Boolean = false,
    val pendingFocus: AlmanacPresentation? = null,
)

sealed interface AlmanacIntent {
    data class ReplaceModel(val modelKey: String, val actualNowMinute: Int) : AlmanacIntent
    data class Tick(val actualNowMinute: Int) : AlmanacIntent
    data class Scrub(val minute: Int) : AlmanacIntent
    data object DragStarted : AlmanacIntent
    data object DragEnded : AlmanacIntent
    data object Open : AlmanacIntent
    data object Close : AlmanacIntent
    data object ReturnToNow : AlmanacIntent
    data class FocusSettled(val modelKey: String) : AlmanacIntent
}

fun reduceAlmanacState(state: AlmanacShellState, intent: AlmanacIntent): AlmanacShellState = when (intent) {
    is AlmanacIntent.ReplaceModel -> AlmanacShellState(
        modelKey = intent.modelKey,
        selectedMinute = normalizeMinute(intent.actualNowMinute),
        actualNowMinute = normalizeMinute(intent.actualNowMinute),
    )
    is AlmanacIntent.Tick -> state.copy(
        actualNowMinute = normalizeMinute(intent.actualNowMinute),
        selectedMinute = if (!state.hasScrubbed && !state.dragging) normalizeMinute(intent.actualNowMinute) else state.selectedMinute,
    )
    is AlmanacIntent.Scrub -> state.copy(selectedMinute = normalizeMinute(intent.minute), hasScrubbed = true)
    AlmanacIntent.DragStarted -> state.copy(dragging = true)
    AlmanacIntent.DragEnded -> state.copy(dragging = false)
    AlmanacIntent.Open -> state.copy(presentation = AlmanacPresentation.Folio, pendingFocus = AlmanacPresentation.Folio)
    AlmanacIntent.Close -> state.copy(presentation = AlmanacPresentation.Glance, dragging = false, pendingFocus = AlmanacPresentation.Glance)
    AlmanacIntent.ReturnToNow -> state.copy(selectedMinute = state.actualNowMinute, hasScrubbed = false, dragging = false)
    is AlmanacIntent.FocusSettled -> if (intent.modelKey == state.modelKey) state.copy(pendingFocus = null) else state
}

data class AlmanacPresentationHandoff(
    val wheelAlpha: Float,
    val folioAlpha: Float,
    val wheelVisible: Boolean,
    val folioVisible: Boolean,
    val semanticOwner: AlmanacSemanticOwner,
)

data class AlmanacHourScaleMark(
    val hour: Int,
    val minute: Int,
    val label: String?,
    val tickLengthDp: Float,
)

data class AlmanacMaximumEventKeyPresentation(
    val visualAlpha: Float,
    val ownsSemantics: Boolean,
)

data class AlmanacQuietGripPresentation(
    val visible: Boolean,
    val lineTopDp: Float,
    val lineHeightDp: Float,
    val notchYDp: Float,
    val visualAlpha: Float,
    val ownsSemantics: Boolean = false,
)

data class AlmanacQuietGripGesture(
    val startMinute: Int,
    val changedQuantizedMinute: Boolean = false,
)

enum class AlmanacQuietGripGestureEnd { Completed, Cancelled }

fun presentationHandoff(progress: Float, settled: AlmanacPresentation?): AlmanacPresentationHandoff {
    val p = progress.coerceIn(0f, 1f)
    val wheelAlpha = (1f - ((p - .24f) / .18f)).coerceIn(0f, 1f)
    val folioAlpha = ((p - .30f) / .20f).coerceIn(0f, 1f)
    val owner = when (settled) {
        AlmanacPresentation.Glance -> AlmanacSemanticOwner.Wheel
        AlmanacPresentation.Folio -> AlmanacSemanticOwner.Folio
        null -> AlmanacSemanticOwner.None
    }
    return AlmanacPresentationHandoff(wheelAlpha, folioAlpha, wheelAlpha > .001f, folioAlpha > .001f, owner)
}

/** The wheel accepts direct or accessibility input only at its fully settled destination. */
fun wheelOwnsInteraction(handoff: AlmanacPresentationHandoff): Boolean =
    handoff.semanticOwner == AlmanacSemanticOwner.Wheel

fun wheelOpenActionVisible(handoff: AlmanacPresentationHandoff, dragging: Boolean): Boolean =
    wheelOwnsInteraction(handoff) && !dragging

/**
 * A calm exact clock scale: every hour is measurable, while only quarter-day anchors carry copy.
 * These marks stay independent from flavor events and rotate with the wheel's selected minute.
 */
fun almanacHourScale(): List<AlmanacHourScaleMark> = (0 until 24).map { hour ->
    val labelled = hour % 6 == 0
    AlmanacHourScaleMark(
        hour = hour,
        minute = hour * 60,
        label = if (labelled) "%02d".format(hour) else null,
        tickLengthDp = if (labelled) 6f else 3f,
    )
}

/** Maximum-type key follows the wheel fade, but joins the semantics tree only under wheel ownership. */
fun maximumEventKeyPresentation(
    composition: AlmanacFontComposition,
    handoff: AlmanacPresentationHandoff,
): AlmanacMaximumEventKeyPresentation = if (composition == AlmanacFontComposition.Maximum) {
    AlmanacMaximumEventKeyPresentation(
        visualAlpha = handoff.wheelAlpha,
        ownsSemantics = handoff.semanticOwner == AlmanacSemanticOwner.Wheel,
    )
} else {
    AlmanacMaximumEventKeyPresentation(visualAlpha = 0f, ownsSemantics = false)
}

fun quietGripPresentation(
    scene: AlmanacSceneGeometry,
    handoff: AlmanacPresentationHandoff,
    learned: Boolean,
    dragging: Boolean,
    composition: AlmanacFontComposition = AlmanacFontComposition.Regular,
): AlmanacQuietGripPresentation {
    val lineHeight = when (composition) {
        AlmanacFontComposition.Regular -> 18f
        AlmanacFontComposition.Comfortable -> 22f
        AlmanacFontComposition.Large -> 24f
        AlmanacFontComposition.Maximum -> 28f
    }
    val intendedTop = scene.centerY + scene.outerRadius +
        if (composition == AlmanacFontComposition.Maximum) 12f else 8f
    val lineTop = intendedTop
        .coerceIn(0f, (scene.viewportHeight - lineHeight).coerceAtLeast(0f))
    return AlmanacQuietGripPresentation(
        visible = !learned && wheelOwnsInteraction(handoff),
        lineTopDp = lineTop,
        lineHeightDp = lineHeight,
        notchYDp = (scene.viewportHeight - 14f).coerceAtLeast(lineTop + lineHeight),
        visualAlpha = if (dragging) .48f else .82f,
    )
}

fun beginQuietGripGesture(selectedMinute: Int) = AlmanacQuietGripGesture(normalizeMinute(selectedMinute))

fun recordQuietGripQuantizedMinute(
    gesture: AlmanacQuietGripGesture,
    quantizedMinute: Int,
): AlmanacQuietGripGesture = gesture.copy(
    changedQuantizedMinute = gesture.changedQuantizedMinute || normalizeMinute(quantizedMinute) != gesture.startMinute,
)

fun quietGripShouldRetire(
    gesture: AlmanacQuietGripGesture,
    end: AlmanacQuietGripGestureEnd,
): Boolean = end == AlmanacQuietGripGestureEnd.Completed && gesture.changedQuantizedMinute

data class AlmanacSceneGeometry(
    val progress: Float,
    val wheelTop: Float,
    val viewportHeight: Float,
    val centerY: Float,
    val outerRadius: Float,
) {
    val clipBottom get() = wheelTop + viewportHeight
    val globalCenterY get() = wheelTop + centerY
    val lowerCropFraction get() = ((centerY + outerRadius - viewportHeight) / (outerRadius * 2f)).coerceIn(0f, 1f)
}

fun almanacSceneGeometry(
    widthDp: Float,
    heightDp: Float,
    compact: Boolean,
    progress: Float,
    maximumType: Boolean = false,
    measuredHeaderHeightDp: Float = 0f,
    largeType: Boolean = false,
): AlmanacSceneGeometry {
    val p = progress.coerceIn(0f, 1f)
    val wheelViewport = when { maximumType -> 176f; compact -> 182f; else -> 220f }
    val keyBand = if (maximumType) 60f else 0f
    val detailViewport = wheelViewport + keyBand
    val detailTop = maxOf(when { maximumType -> 144f; compact -> 112f; else -> 136f }, measuredHeaderHeightDp + 8f)
    val wheelCenter = wheelViewport * .93f
    val detailCenter = wheelCenter + keyBand
    val widthFraction = when {
        maximumType -> .205f
        largeType && compact -> .35f
        compact -> .425f
        else -> .445f
    }
    val outer = minOf(widthDp * widthFraction, wheelCenter - 15f)
    val readoutBand = if (maximumType) 200f else 72f
    val glanceCenter = outer + 14f + readoutBand
    val glanceViewport = if (maximumType) glanceCenter + outer + 68f else outer * 2f + 48f + readoutBand
    val glanceGlobalCenter = heightDp * if (maximumType && compact) .60f else if (compact) .47f else .48f
    val glanceTop = maxOf(glanceGlobalCenter - glanceCenter, measuredHeaderHeightDp + 8f)
    fun lerp(a: Float, b: Float) = a + (b - a) * p
    return AlmanacSceneGeometry(p, lerp(glanceTop, detailTop), lerp(glanceViewport, detailViewport), lerp(glanceCenter, detailCenter), outer)
}

fun maximumGlanceContentHeightDp(screenHeightDp: Float): Float = maxOf(screenHeightDp, 780f)

/** Places the tertiary action immediately after, and never inside, the clipped dial field. */
fun returnToNowTopDp(scene: AlmanacSceneGeometry, targetHeightDp: Float = 48f): Float {
    require(targetHeightDp > 0f)
    return scene.clipBottom + 4f
}

fun openActionTopDp(scene: AlmanacSceneGeometry, returnToNowVisible: Boolean): Float =
    scene.clipBottom + if (returnToNowVisible) 60f else 16f

fun almanacFontComposition(scale: Float) = when {
    scale >= 1.8f -> AlmanacFontComposition.Maximum
    scale >= 1.5f -> AlmanacFontComposition.Large
    scale >= 1.25f -> AlmanacFontComposition.Comfortable
    else -> AlmanacFontComposition.Regular
}

fun almanacDetailScrollPolicy(composition: AlmanacFontComposition) =
    if (composition == AlmanacFontComposition.Maximum) AlmanacDetailScrollPolicy.SingleOuterScroll else AlmanacDetailScrollPolicy.WeightedBody

fun radialLabelIds(points: List<AlmanacPoint>, selection: AlmanacSelection, composition: AlmanacFontComposition, glance: Boolean): List<String> = when {
    composition == AlmanacFontComposition.Maximum || composition == AlmanacFontComposition.Large -> emptyList()
    glance -> points.filter { it.role == AlmanacVisualRole.Primary }.map(AlmanacPoint::id)
    else -> points.filter {
        it.role == AlmanacVisualRole.Primary &&
            (it.id == selection.previous?.id || it.id == selection.exact?.id || it.id == selection.next?.id)
    }.map(AlmanacPoint::id)
}

fun radialLabelText(point: AlmanacPoint): String? = point.title

private fun AlmanacPoint.canonicalId(): String = id.lowercase().substringAfterLast('.')

fun pointMarker(point: AlmanacPoint): AlmanacPointMarker = when {
    point.role == AlmanacVisualRole.Primary -> AlmanacPointMarker.PrimaryPrayer
    point.canonicalId() == "imsak" -> AlmanacPointMarker.ImsakNotches
    point.canonicalId() == "syuruk" || point.canonicalId() == "sunrise" -> AlmanacPointMarker.SyurukHorizon
    point.canonicalId() == "dhuha" -> AlmanacPointMarker.DhuhaDiamond
    else -> AlmanacPointMarker.Secondary
}

fun externalEventKey(points: List<AlmanacPoint>): List<String> = points.map { "${it.title} ${formatMinute(it.minute)}" }

fun revealFraction(layer: AlmanacRevealLayer, progress: Float): Float {
    val (start, end) = when (layer) {
        AlmanacRevealLayer.Wheel -> 0f to .58f
        AlmanacRevealLayer.Summary -> .34f to .68f
        AlmanacRevealLayer.Body -> .46f to .84f
        AlmanacRevealLayer.Navigation -> .76f to 1f
    }
    return ((progress - start) / (end - start)).coerceIn(0f, 1f)
}

fun normalizeMinute(minute: Int): Int = ((minute % ALMANAC_DAY_MINUTES) + ALMANAC_DAY_MINUTES) % ALMANAC_DAY_MINUTES

fun quantizeMinute(minute: Int, step: Int): Int = normalizeMinute(((minute + step / 2) / step) * step)

fun quantizeAccessibleMinute(requested: Float, step: Int): Int =
    ((requested.coerceIn(0f, (ALMANAC_DAY_MINUTES - step).toFloat()) / step).roundToInt() * step)
        .coerceIn(0, ALMANAC_DAY_MINUTES - step)

fun accessibleMinuteRange() = 0f..ALMANAC_LAST_MINUTE.toFloat()

fun accessibleMinuteSteps(step: Int): Int = ((ALMANAC_LAST_MINUTE / step) - 1).coerceAtLeast(0)

fun minuteAngle(eventMinute: Int, selectedMinute: Int): Float {
    var delta = normalizeMinute(eventMinute) - normalizeMinute(selectedMinute)
    if (delta > ALMANAC_DAY_MINUTES / 2) delta -= ALMANAC_DAY_MINUTES
    if (delta < -ALMANAC_DAY_MINUTES / 2) delta += ALMANAC_DAY_MINUTES
    return -90f + delta / 4f
}

fun formatMinute(minute: Int): String = "%02d:%02d".format(normalizeMinute(minute) / 60, normalizeMinute(minute) % 60)

fun minuteDistance(a: Int, b: Int): Int {
    val d = abs(normalizeMinute(a) - normalizeMinute(b))
    return minOf(d, ALMANAC_DAY_MINUTES - d)
}

fun intervalContains(interval: AlmanacInterval, minute: Int): Boolean {
    val m = normalizeMinute(minute)
    if (interval.coverageSegments.isNotEmpty()) {
        return interval.coverageSegments.any { segment ->
            if (segment.startMinute <= segment.endMinute) m in segment.startMinute..segment.endMinute
            else m >= segment.startMinute || m <= segment.endMinute
        }
    }
    return if (interval.startMinute <= interval.endMinute) m in interval.startMinute..interval.endMinute
    else m >= interval.startMinute || m <= interval.endMinute
}

fun intervalVisuallyContains(interval: AlmanacInterval, minute: Int): Boolean =
    if (interval.visual == AlmanacIntervalVisual.EventTrigger) {
        minuteDistance(interval.markerMinute, minute) <= 10
    } else {
        intervalContains(interval, minute)
    }

fun intervalSemanticallyContains(interval: AlmanacInterval, minute: Int): Boolean =
    if (interval.visual == AlmanacIntervalVisual.EventTrigger) {
        normalizeMinute(minute) == interval.markerMinute
    } else {
        intervalContains(interval, minute)
    }

private fun AlmanacCoverageSegment.contains(minute: Int): Boolean {
    val selected = normalizeMinute(minute)
    return if (startMinute <= endMinute) selected in startMinute..endMinute
    else selected >= startMinute || selected <= endMinute
}

/** Exact members represented at a selected minute; aggregate union coverage remains drawable. */
fun AlmanacInterval.memberIdsAt(minute: Int): List<String> {
    if (!intervalSemanticallyContains(this, minute)) return emptyList()
    val ids = memberIds.ifEmpty { listOf(id) }
    return if (memberCoverage.isEmpty()) ids else ids.filter { memberCoverage[it].orEmpty().any { segment -> segment.contains(minute) } }
}

fun AlmanacInterval.memberCoversAt(memberId: String, minute: Int): Boolean = memberId in memberIdsAt(minute)

fun AlmanacInterval.memberCountAt(minute: Int): Int = memberIdsAt(minute).size

fun AlmanacInterval.memberTitlesAt(minute: Int): List<String> {
    val activeIds = memberIdsAt(minute)
    if (activeIds.isEmpty()) return emptyList()
    if (memberIds.isEmpty()) return listOfNotNull(title)
    val titlesById = memberIds.zip(memberTitles).toMap()
    val activeTitles = activeIds.mapNotNull(titlesById::get)
    return activeTitles.ifEmpty { if (activeIds.size == 1) listOfNotNull(title) else emptyList() }
}

fun intervalAnchorMinute(interval: AlmanacInterval): Int =
    if (interval.visual == AlmanacIntervalVisual.EventTrigger) interval.markerMinute
    else interval.coverageSegments.firstOrNull()?.startMinute ?: interval.startMinute

fun intervalOccurrence(interval: AlmanacInterval, minute: Int): AlmanacIntervalOccurrence {
    if (!intervalSemanticallyContains(interval, minute)) return when {
        interval.visual == AlmanacIntervalVisual.SearchEnvelope -> AlmanacIntervalOccurrence.UpcomingSearchRegion
        interval.truthClass == AlmanacIntervalTruthClass.ScheduleContextProxy -> AlmanacIntervalOccurrence.UpcomingScheduleContext
        interval.visual == AlmanacIntervalVisual.EventTrigger || interval.truthClass == AlmanacIntervalTruthClass.EventRequired ->
            AlmanacIntervalOccurrence.UpcomingTrigger
        interval.truthClass == AlmanacIntervalTruthClass.ProvisionalResearch -> AlmanacIntervalOccurrence.UpcomingDraftWindow
        interval.truthClass == AlmanacIntervalTruthClass.PolicyMapped -> AlmanacIntervalOccurrence.UpcomingPolicyWindow
        else -> AlmanacIntervalOccurrence.Upcoming
    }
    if (interval.truthClass == AlmanacIntervalTruthClass.ScheduleContextProxy) return AlmanacIntervalOccurrence.ScheduleContext
    if (interval.visual == AlmanacIntervalVisual.SearchEnvelope) return AlmanacIntervalOccurrence.SearchRegion
    if (interval.visual == AlmanacIntervalVisual.EventTrigger) return AlmanacIntervalOccurrence.Trigger
    return when (interval.truthClass) {
        AlmanacIntervalTruthClass.Direct -> AlmanacIntervalOccurrence.Active
        AlmanacIntervalTruthClass.PolicyMapped -> AlmanacIntervalOccurrence.PolicyWindow
        AlmanacIntervalTruthClass.ProvisionalResearch -> AlmanacIntervalOccurrence.DraftWindow
        AlmanacIntervalTruthClass.ScheduleContextProxy -> AlmanacIntervalOccurrence.ScheduleContext
        AlmanacIntervalTruthClass.EventRequired -> AlmanacIntervalOccurrence.Trigger
        AlmanacIntervalTruthClass.Unresolved -> AlmanacIntervalOccurrence.Unresolved
    }
}

fun intersectingIntervalsAt(intervals: List<AlmanacInterval>, minute: Int): List<AlmanacInterval> =
    intervals.filter { intervalVisuallyContains(it, minute) }
        .sortedWith(compareBy<AlmanacInterval> { it.lane }.thenBy { it.id })

fun semanticallyIntersectingIntervalsAt(intervals: List<AlmanacInterval>, minute: Int): List<AlmanacInterval> =
    intervals.filter { intervalSemanticallyContains(it, minute) }
        .sortedWith(compareBy<AlmanacInterval> { it.lane }.thenBy { it.id })

/** Stable display ordering only; it never represents religious priority or recommendation. */
fun selectIntervalForMinute(intervals: List<AlmanacInterval>, minute: Int): AlmanacInterval? =
    intervals.filter { intervalSemanticallyContains(it, minute) }
        .sortedWith(compareBy<AlmanacInterval> { it.lane }.thenBy { it.id })
        .firstOrNull()
        ?: intervals.filter { intervalAnchorMinute(it) > normalizeMinute(minute) }
            .minWithOrNull(compareBy<AlmanacInterval> { intervalAnchorMinute(it) }.thenBy { it.lane }.thenBy { it.id })

fun intervalPrimitive(visual: AlmanacIntervalVisual): AlmanacIntervalPrimitive = when (visual) {
    AlmanacIntervalVisual.PublishedWindow -> AlmanacIntervalPrimitive.SolidArc
    AlmanacIntervalVisual.NamedPeriodEstimate -> AlmanacIntervalPrimitive.ContinuousArcWithHollowBead
    AlmanacIntervalVisual.NightSegmentEstimate -> AlmanacIntervalPrimitive.StripedArcWithHollowBead
    AlmanacIntervalVisual.SearchEnvelope -> AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal
    AlmanacIntervalVisual.EventTrigger -> AlmanacIntervalPrimitive.HollowEventTab
}

fun intervalLaneRadius(secondaryRadius: Float, outerRadius: Float, lane: Int): Float {
    require(lane in 0..3)
    if (lane == 0) return secondaryRadius
    val available = (outerRadius - secondaryRadius - 4f).coerceAtLeast(0f)
    return secondaryRadius + available * lane / 3f
}

fun semanticLaneBudget(composition: AlmanacFontComposition): Int = when (composition) {
    AlmanacFontComposition.Regular -> 3
    AlmanacFontComposition.Comfortable,
    AlmanacFontComposition.Large,
    -> 2
    AlmanacFontComposition.Maximum -> 1
}

data class AlmanacEventGlyphSize(
    val widthDp: Float,
    val heightDp: Float,
)

fun eventGlyphSize(composition: AlmanacFontComposition): AlmanacEventGlyphSize = when (composition) {
    AlmanacFontComposition.Regular -> AlmanacEventGlyphSize(14f, 9f)
    AlmanacFontComposition.Comfortable,
    AlmanacFontComposition.Large,
    -> AlmanacEventGlyphSize(16f, 12f)
    AlmanacFontComposition.Maximum -> AlmanacEventGlyphSize(18f, 14f)
}

/** Places semantic lanes in the judge-locked outer band, with a six-dp gap where two or more exist. */
fun adaptiveIntervalLaneRadius(
    outerRadius: Float,
    lane: Int,
    composition: AlmanacFontComposition,
): Float {
    require(lane in 1..3)
    val budget = semanticLaneBudget(composition)
    require(lane <= budget)
    val inner = outerRadius * .90f
    val outer = (outerRadius - 3f).coerceAtLeast(inner)
    val fraction = if (budget == 1) 0f else (lane - 1).toFloat() / (budget - 1).toFloat()
    return inner + (outer - inner) * fraction
}

data class AlmanacLabelBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
    fun intersects(other: AlmanacLabelBounds) = left < other.right && right > other.left && top < other.bottom && bottom > other.top
    fun isInside(width: Float, height: Float, padding: Float) = left >= padding && top >= padding && right <= width - padding && bottom <= height - padding
}

data class AlmanacLabelCandidate(val id: String, val bounds: AlmanacLabelBounds)

data class AlmanacSceneRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    fun overlaps(other: AlmanacSceneRect) = left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

data class AlmanacReadoutBay(
    val bounds: AlmanacSceneRect,
    val leaderStartX: Float,
    val leaderBendX: Float,
    val leaderEndX: Float,
    val leaderY: Float,
    val external: Boolean,
)

fun readoutBayGeometry(widthDp: Float, scene: AlmanacSceneGeometry, measuredWidthDp: Float, measuredHeightDp: Float, external: Boolean): AlmanacReadoutBay {
    val pointerY = scene.centerY - scene.outerRadius * .88f - 13f
    val wheelTopY = scene.centerY - scene.outerRadius
    val bayWidth = minOf(measuredWidthDp, widthDp - 32f)
    val left = (widthDp - bayWidth) / 2f
    val right = left + bayWidth
    val top = if (external) 8f else maxOf(6f, wheelTopY - measuredHeightDp - 12f)
    val bottom = top + measuredHeightDp
    val centerX = widthDp / 2f
    return AlmanacReadoutBay(
        bounds = AlmanacSceneRect(left, top, right, bottom),
        leaderStartX = centerX,
        leaderBendX = centerX,
        leaderEndX = centerX,
        leaderY = bottom + 5f,
        external = external,
    )
}

fun readoutExclusionAtAlpha(readoutBay: AlmanacReadoutBay, alpha: Float): AlmanacSceneRect? =
    readoutBay.bounds.takeIf { !readoutBay.external && alpha >= .999f }

fun primitiveIsOutsideExclusion(bounds: AlmanacSceneRect, exclusion: AlmanacSceneRect?) = exclusion == null || !bounds.overlaps(exclusion)

fun readoutMayClipPrimitiveLayer(layer: AlmanacPrimitiveLayer): Boolean = when (layer) {
    AlmanacPrimitiveLayer.BroadRail,
    AlmanacPrimitiveLayer.HourRail,
    AlmanacPrimitiveLayer.EventRail,
    AlmanacPrimitiveLayer.ContextualArc,
    AlmanacPrimitiveLayer.CelestialRail,
    AlmanacPrimitiveLayer.ActualNow,
    AlmanacPrimitiveLayer.Keystone,
    AlmanacPrimitiveLayer.Pointer,
    AlmanacPrimitiveLayer.Leader,
    AlmanacPrimitiveLayer.Readout,
    -> false
}

fun resolveLabelPlacement(selected: AlmanacLabelCandidate?, next: AlmanacLabelCandidate?, width: Float, height: Float, padding: Float): List<AlmanacLabelCandidate> {
    fun clamp(c: AlmanacLabelCandidate): AlmanacLabelCandidate {
        val left = c.bounds.left.coerceIn(padding, (width - padding - c.bounds.width).coerceAtLeast(padding))
        val top = c.bounds.top.coerceIn(padding, (height - padding - c.bounds.height).coerceAtLeast(padding))
        return c.copy(bounds = AlmanacLabelBounds(left, top, left + c.bounds.width, top + c.bounds.height))
    }
    val owner = selected?.let(::clamp)
    val follower = next?.let(::clamp)?.takeIf { owner == null || !owner.bounds.intersects(it.bounds) }
    return listOfNotNull(owner, follower)
}

/**
 * Places every measured candidate through one deterministic collision policy. Callers provide
 * exact/previous, next, then stable event order as [priorityIds]. Lower-priority labels relocate
 * around their measured anchor or are rejected; unmanaged fallback painting is forbidden.
 */
fun resolveAllLabelPlacements(
    candidates: List<AlmanacLabelCandidate>,
    priorityIds: List<String>,
    width: Float,
    height: Float,
    padding: Float,
    exclusion: AlmanacLabelBounds? = null,
): List<AlmanacLabelCandidate> {
    val rank = priorityIds.distinct().withIndex().associate { it.value to it.index }
    val ordered = candidates.mapIndexed { index, candidate -> index to candidate }.sortedWith(compareBy<Pair<Int, AlmanacLabelCandidate>>(
        { rank[it.second.id] ?: Int.MAX_VALUE },
        { it.first },
    )).map { it.second }
    val accepted = mutableListOf<AlmanacLabelCandidate>()
    fun clamp(c: AlmanacLabelCandidate, dx: Float, dy: Float): AlmanacLabelCandidate {
        val left = (c.bounds.left + dx).coerceIn(padding, (width - padding - c.bounds.width).coerceAtLeast(padding))
        val top = (c.bounds.top + dy).coerceIn(padding, (height - padding - c.bounds.height).coerceAtLeast(padding))
        return c.copy(bounds = AlmanacLabelBounds(left, top, left + c.bounds.width, top + c.bounds.height))
    }
    ordered.forEach { candidate ->
        val h = candidate.bounds.height + 3f
        val w = candidate.bounds.width * .55f + 3f
        val offsets = listOf(
            0f to 0f, 0f to -h, 0f to h, -w to 0f, w to 0f,
            -w to -h, w to -h, -w to h, w to h, 0f to (-2f * h), 0f to (2f * h),
        )
        val placed = offsets.asSequence().map { (dx, dy) -> clamp(candidate, dx, dy) }.firstOrNull { proposal ->
            (exclusion == null || !proposal.bounds.intersects(exclusion)) && accepted.none { it.bounds.intersects(proposal.bounds) }
        }
        if (placed != null) accepted += placed
    }
    return accepted
}
