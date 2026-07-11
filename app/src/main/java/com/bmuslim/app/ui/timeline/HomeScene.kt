package com.bmuslim.app.ui.timeline

enum class HomePresentation { Glance, Detail }

enum class RevealLayer { Wheel, Summary, Ledger, Navigation }

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

fun homeSceneGeometry(
    widthDp: Float,
    heightDp: Float,
    compact: Boolean,
    progress: Float,
): HomeSceneGeometry {
    val p = progress.coerceIn(0f, 1f)
    val glanceClearance = 14f
    val detailViewport = if (compact) 182f else 220f
    val detailTop = if (compact) 112f else 120f
    val detailCenter = detailViewport * .93f
    val detailOuter = minOf(widthDp * .445f, detailCenter - 15f)
    // The wheel does not scale during presentation changes; only its center and aperture move.
    val glanceOuter = detailOuter
    val glanceViewport = glanceOuter * 2f + glanceClearance * 2f
    val glanceGlobalCenter = heightDp * if (compact) .47f else .48f
    val glanceCenter = glanceOuter + glanceClearance
    val glanceTop = glanceGlobalCenter - glanceCenter
    return HomeSceneGeometry(
        progress = p,
        wheelTop = lerpFloat(glanceTop, detailTop, p),
        viewportHeight = lerpFloat(glanceViewport, detailViewport, p),
        centerY = lerpFloat(glanceCenter, detailCenter, p),
        outerRadius = detailOuter,
    )
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
