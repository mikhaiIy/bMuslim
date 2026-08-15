package com.bmuslim.app.ui.timeline

import kotlin.math.roundToInt

enum class HomePresentation { Glance, Detail }

enum class RevealLayer { Wheel, Summary, Ledger, Navigation }

enum class FontComposition { Regular, Comfortable, Large, Maximum }

enum class PrayerDisplayPolicy { ContextualRadial, ReducedRadial, AllRadial, FiveNameKey }

enum class WheelPrimitiveLayer {
    BroadRail,
    HourRail,
    PrayerRail,
    ContextualArc,
    CelestialRail,
    ActualNow,
    Keystone,
    Pointer,
    Leader,
    Readout,
}

enum class SemanticOwner { Wheel, Detail, None }

enum class DetailScrollPolicy { WeightedLedger, SingleOuterScroll }

data class PresentationHandoff(
    val wheelAlpha: Float,
    val detailAlpha: Float,
    val wheelVisible: Boolean,
    val detailVisible: Boolean,
    val semanticOwner: SemanticOwner,
    val detailInteractive: Boolean,
)

data class MaximumDetailScrollBudget(
    val viewportHeight: Float,
    val intrinsicContentHeight: Float,
    val sectionCount: Int,
    val weightedChildCount: Int,
    val policy: DetailScrollPolicy,
) {
    val requiresScroll: Boolean get() = intrinsicContentHeight > viewportHeight
    val allSectionsScrollReachable: Boolean
        get() = viewportHeight > 0f && sectionCount > 0 && weightedChildCount == 0 &&
            policy == DetailScrollPolicy.SingleOuterScroll
}

data class HomeSceneGeometry(
    val progress: Float,
    val wheelTop: Float,
    val viewportHeight: Float,
    val centerY: Float,
    val outerRadius: Float,
) {
    val clipBottom: Float get() = wheelTop + viewportHeight
    val globalCenterY: Float get() = wheelTop + centerY
    val lowerCropFraction: Float
        get() = ((centerY + outerRadius - viewportHeight) / (outerRadius * 2f)).coerceIn(0f, 1f)
}

data class SceneRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun overlaps(other: SceneRect): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

data class HeaderSafeGeometry(
    val clock: SceneRect,
    val apertureTop: Float,
    val clearance: Float,
) {
    val isSeparated: Boolean get() = clock.bottom + clearance <= apertureTop
}

data class ReadoutBayGeometry(
    val bounds: SceneRect,
    val leaderStartX: Float,
    val leaderBendX: Float,
    val leaderEndX: Float,
    val leaderY: Float,
    val external: Boolean,
)

data class WheelReadout(
    val primary: String,
    val secondary: String?,
    val prayerId: String?,
    val contextualId: String?,
)

fun homeSceneGeometry(
    widthDp: Float,
    heightDp: Float,
    compact: Boolean,
    progress: Float,
    maximumType: Boolean = false,
    measuredHeaderHeightDp: Float = 0f,
): HomeSceneGeometry {
    val p = progress.coerceIn(0f, 1f)
    val glanceClearance = 14f
    val wheelDetailViewport = when {
        maximumType -> 176f
        compact -> 182f
        else -> 220f
    }
    val maximumPrayerKeyBand = if (maximumType) 60f else 0f
    val detailViewport = wheelDetailViewport + maximumPrayerKeyBand
    // The aperture always remains below a dedicated header corridor.  This is deliberately
    // allocated here rather than relying on paint order to protect the clock.
    val designedDetailTop = when {
        maximumType -> 144f
        compact -> 112f
        else -> 136f
    }
    val detailTop = maxOf(designedDetailTop, measuredHeaderHeightDp + 8f)
    val wheelDetailCenter = wheelDetailViewport * .93f
    val detailCenter = wheelDetailCenter + maximumPrayerKeyBand
    val detailOuter = minOf(widthDp * .445f, wheelDetailCenter - 15f)
    // The wheel does not scale during presentation changes; only its center and aperture move.
    val glanceOuter = detailOuter
    val externalReadoutBand = if (maximumType) 84f else 72f
    val glanceViewport = glanceOuter * 2f + glanceClearance * 2f + externalReadoutBand
    val glanceGlobalCenter = heightDp * when {
        maximumType && compact -> .60f
        compact -> .47f
        else -> .48f
    }
    val glanceCenter = glanceOuter + glanceClearance + externalReadoutBand
    val glanceTop = maxOf(glanceGlobalCenter - glanceCenter, measuredHeaderHeightDp + 8f)
    return HomeSceneGeometry(
        progress = p,
        wheelTop = lerpFloat(glanceTop, detailTop, p),
        viewportHeight = lerpFloat(glanceViewport, detailViewport, p),
        centerY = lerpFloat(glanceCenter, detailCenter, p),
        outerRadius = detailOuter,
    )
}

fun headerSafeGeometry(scene: HomeSceneGeometry, measuredClock: SceneRect): HeaderSafeGeometry {
    val clock = measuredClock
    return HeaderSafeGeometry(clock = clock, apertureTop = scene.wheelTop, clearance = 8f)
}

fun requiredHeaderHeight(
    safeInsetTop: Float,
    measuredContentHeight: Float,
    verticalPadding: Float = 5f,
): Float = safeInsetTop + measuredContentHeight + verticalPadding * 2f

fun fontComposition(fontScale: Float): FontComposition = when {
    fontScale >= 1.8f -> FontComposition.Maximum
    fontScale >= 1.5f -> FontComposition.Large
    fontScale >= 1.25f -> FontComposition.Comfortable
    else -> FontComposition.Regular
}

fun detailScrollPolicy(composition: FontComposition): DetailScrollPolicy =
    if (composition == FontComposition.Maximum) {
        DetailScrollPolicy.SingleOuterScroll
    } else {
        DetailScrollPolicy.WeightedLedger
    }

fun maximumDetailScrollBudget(
    screenHeightDp: Float,
    geometry: HomeSceneGeometry,
    intrinsicSectionHeights: List<Float>,
): MaximumDetailScrollBudget = MaximumDetailScrollBudget(
    viewportHeight = (screenHeightDp - geometry.clipBottom).coerceAtLeast(0f),
    intrinsicContentHeight = intrinsicSectionHeights.sum(),
    sectionCount = intrinsicSectionHeights.size,
    weightedChildCount = 0,
    policy = DetailScrollPolicy.SingleOuterScroll,
)

fun prayerDisplayPolicy(
    composition: FontComposition,
    progress: Float,
): PrayerDisplayPolicy = when {
    composition == FontComposition.Maximum && progress >= .5f -> PrayerDisplayPolicy.FiveNameKey
    composition == FontComposition.Maximum -> PrayerDisplayPolicy.AllRadial
    composition == FontComposition.Large -> PrayerDisplayPolicy.ReducedRadial
    else -> PrayerDisplayPolicy.ContextualRadial
}

fun prayerLabelVisible(
    policy: PrayerDisplayPolicy,
    isGlance: Boolean,
    isActive: Boolean,
    isNext: Boolean,
): Boolean = when (policy) {
    PrayerDisplayPolicy.ContextualRadial -> isGlance || isActive || isNext
    PrayerDisplayPolicy.ReducedRadial -> isActive || isNext
    PrayerDisplayPolicy.AllRadial -> true
    PrayerDisplayPolicy.FiveNameKey -> false
}

fun prayerKeyTitles(prayers: List<TimelineDeed>): List<String> = prayers
    .filter { it.ruling == Ruling.Fardh }
    .sortedBy { it.startMinute }
    .map { it.title.removePrefix("Solat ") }

fun readoutBayGeometry(
    widthDp: Float,
    scene: HomeSceneGeometry,
    measuredWidthDp: Float,
    measuredHeightDp: Float,
    external: Boolean,
): ReadoutBayGeometry {
    val pointerY = scene.centerY - scene.outerRadius * .88f - 13f
    val wheelTopY = scene.centerY - scene.outerRadius
    val bayWidth = minOf(measuredWidthDp, widthDp - 32f)
    val left = (widthDp - bayWidth) / 2f
    val right = left + bayWidth
    val top = if (external) 8f else maxOf(6f, wheelTopY - measuredHeightDp - 12f)
    val bottom = top + measuredHeightDp
    val centerX = widthDp / 2f
    return ReadoutBayGeometry(
        bounds = SceneRect(left, top, right, bottom),
        leaderStartX = centerX,
        leaderBendX = centerX,
        leaderEndX = centerX,
        leaderY = bottom + 5f,
        external = external,
    )
}

fun wheelReadoutAt(
    deeds: List<TimelineDeed>,
    minute: Int,
    directManipulation: Boolean,
): WheelReadout? {
    val active = activeDeedsAt(deeds, minute)
    val prayer = active.firstOrNull { it.ruling == Ruling.Fardh }
    val contextual = active.firstOrNull { it.ruling != Ruling.Fardh }
    if (prayer == null && contextual == null) return null

    return if (prayer != null) {
        WheelReadout(
            primary = "${formatTimelineMinute(minute)} dipilih · Waktu ${prayer.title.removePrefix("Solat ")}",
            secondary = contextual?.let {
                if (directManipulation) it.title else "Amalan berkaitan · ${it.title}"
            },
            prayerId = prayer.id,
            contextualId = contextual?.id,
        )
    } else {
        WheelReadout(
            primary = "${formatTimelineMinute(minute)} dipilih",
            secondary = if (directManipulation) contextual!!.title else "Amalan berkaitan · ${contextual!!.title}",
            prayerId = null,
            contextualId = contextual.id,
        )
    }
}

fun presentationHandoff(
    progress: Float,
    settledPresentation: HomePresentation?,
): PresentationHandoff {
    val p = progress.coerceIn(0f, 1f)
    val wheelAlpha = (1f - ((p - .24f) / .18f)).coerceIn(0f, 1f)
    val detailAlpha = ((p - .30f) / .20f).coerceIn(0f, 1f)
    val owner = when (settledPresentation) {
        HomePresentation.Glance -> SemanticOwner.Wheel
        HomePresentation.Detail -> SemanticOwner.Detail
        null -> SemanticOwner.None
    }
    return PresentationHandoff(
        wheelAlpha = wheelAlpha,
        detailAlpha = detailAlpha,
        wheelVisible = wheelAlpha > .001f,
        detailVisible = detailAlpha > .001f,
        semanticOwner = owner,
        detailInteractive = owner == SemanticOwner.Detail,
    )
}

fun quantizeAccessibleMinute(requested: Float): Int =
    ((requested.coerceIn(0f, 1435f) / 5f).roundToInt() * 5).coerceIn(0, 1435)

fun quantizeCircularMinute(minute: Int): Int = wrapTimelineMinute(((minute + 2) / 5) * 5)

fun primitiveIsOutsideExclusion(bounds: SceneRect, exclusion: SceneRect?): Boolean =
    exclusion == null || !bounds.overlaps(exclusion)

fun readoutMayClipPrimitiveLayer(layer: WheelPrimitiveLayer): Boolean = when (layer) {
    WheelPrimitiveLayer.BroadRail,
    WheelPrimitiveLayer.HourRail,
    WheelPrimitiveLayer.PrayerRail,
    WheelPrimitiveLayer.ContextualArc,
    WheelPrimitiveLayer.CelestialRail,
    WheelPrimitiveLayer.ActualNow,
    WheelPrimitiveLayer.Keystone,
    WheelPrimitiveLayer.Pointer,
    WheelPrimitiveLayer.Leader,
    WheelPrimitiveLayer.Readout,
    -> false
}

fun returnToNowVisible(handoff: PresentationHandoff, minuteDistance: Int): Boolean =
    handoff.detailInteractive && minuteDistance > 4

fun formatTimelineMinute(minute: Int): String {
    val wrapped = wrapTimelineMinute(minute)
    return "%02d:%02d".format(wrapped / 60, wrapped % 60)
}

fun wrapTimelineMinute(minute: Int): Int {
    val dayMinutes = 24 * 60
    return ((minute % dayMinutes) + dayMinutes) % dayMinutes
}

internal fun timelineMinuteAngle(minute: Int, selectedMinute: Int): Float {
    val dayMinutes = 24 * 60
    var delta = minute - selectedMinute
    if (delta > dayMinutes / 2) delta -= dayMinutes
    if (delta < -dayMinutes / 2) delta += dayMinutes
    return -90f + delta / 4f
}

fun revealFraction(layer: RevealLayer, progress: Float): Float {
    val (start, end) = when (layer) {
        RevealLayer.Wheel -> 0f to .58f
        RevealLayer.Summary -> .34f to .68f
        RevealLayer.Ledger -> .46f to .84f
        RevealLayer.Navigation -> .76f to 1f
    }
    return ((progress - start) / (end - start)).coerceIn(0f, 1f)
}

data class CompactDetailBudget(
    val clipBottom: Float,
    val remainingHeight: Float,
    val contextHeight: Float,
    val closeHeight: Float,
    val navigationHeight: Float,
    val ledgerHeight: Float,
)

fun compactDetailBudget(heightDp: Float): CompactDetailBudget {
    val geometry = homeSceneGeometry(360f, heightDp, compact = true, progress = 1f)
    val remaining = heightDp - geometry.clipBottom
    val context = 58f
    val close = 48f
    val navigation = 62f
    return CompactDetailBudget(
        clipBottom = geometry.clipBottom,
        remainingHeight = remaining,
        contextHeight = context,
        closeHeight = close,
        navigationHeight = navigation,
        ledgerHeight = remaining - context - close - navigation,
    )
}

fun compactDetailRegionsFit(heightDp: Float): Boolean = compactDetailBudget(heightDp).ledgerHeight >= 140f

fun detailContentTop(screenHeightDp: Float, geometry: HomeSceneGeometry): Float {
    val reveal = ((geometry.progress - .34f) / .66f).coerceIn(0f, 1f)
    return geometry.clipBottom + (screenHeightDp - geometry.clipBottom) * (1f - reveal)
}

private fun lerpFloat(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction
