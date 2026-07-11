package com.bmuslim.app.ui.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSceneTest {
    @Test
    fun glanceClipContainsCompleteOuterWheel() {
        val geometry = homeSceneGeometry(390f, 844f, compact = false, progress = 0f)
        assertTrue(geometry.centerY - geometry.outerRadius >= 8f)
        assertTrue(geometry.viewportHeight - geometry.centerY - geometry.outerRadius >= 8f)
        assertTrue(geometry.outerRadius <= 390f / 2f - 16f)
    }

    @Test
    fun detailEndpointsMatchLockedViewports() {
        val regular = homeSceneGeometry(390f, 844f, compact = false, progress = 1f)
        val compact = homeSceneGeometry(360f, 640f, compact = true, progress = 1f)
        assertEquals(220f, regular.viewportHeight, .001f)
        assertEquals(220f * .93f, regular.centerY, .001f)
        assertEquals(182f, compact.viewportHeight, .001f)
        assertEquals(182f * .93f, compact.centerY, .001f)
    }

    @Test
    fun transitionMovesCenterUpAndMeaningfullyCropsLowerWheel() {
        val glance = homeSceneGeometry(390f, 844f, compact = false, progress = 0f)
        val transition = homeSceneGeometry(390f, 844f, compact = false, progress = .6f)
        assertTrue(transition.globalCenterY < glance.globalCenterY)
        assertTrue(transition.lowerCropFraction >= .25f)
        assertTrue(transition.lowerCropFraction <= .35f)
    }

    @Test
    fun detailContentFollowsButNeverOverlapsTheAperture() {
        var previousTop = Float.POSITIVE_INFINITY
        for (step in 0..20) {
            val geometry = homeSceneGeometry(390f, 844f, false, step / 20f)
            val top = detailContentTop(844f, geometry)
            assertTrue(top >= geometry.clipBottom)
            if (step <= 6) assertEquals(844f, top, .001f)
            if (step >= 7) assertTrue(top <= previousTop)
            previousTop = top
        }
        val detail = homeSceneGeometry(390f, 844f, false, 1f)
        assertEquals(detail.clipBottom, detailContentTop(844f, detail), .001f)
    }

    @Test
    fun rendererAngleIsIndependentOfPresentationGeometry() {
        val glance = homeSceneGeometry(390f, 844f, false, 0f)
        assertEquals(-90f, timelineMinuteAngle(15 * 60, 15 * 60), 0f)
        val prayerAngle = timelineMinuteAngle(16 * 60 + 45, 15 * 60)
        for (step in 0..20) {
            val progress = step / 20f
            homeSceneGeometry(390f, 844f, false, progress)
            assertEquals(prayerAngle, timelineMinuteAngle(16 * 60 + 45, 15 * 60), 0f)
            assertEquals(glance.outerRadius, homeSceneGeometry(390f, 844f, false, progress).outerRadius, 0f)
        }
    }

    @Test
    fun geometryMovesAndCropsMonotonically() {
        var previousCenter = Float.POSITIVE_INFINITY
        var previousCrop = -1f
        val radius = homeSceneGeometry(390f, 844f, false, 0f).outerRadius
        for (step in 0..100) {
            val geometry = homeSceneGeometry(390f, 844f, false, step / 100f)
            assertTrue(geometry.globalCenterY <= previousCenter)
            assertTrue(geometry.lowerCropFraction >= previousCrop)
            assertEquals(radius, geometry.outerRadius, 0f)
            previousCenter = geometry.globalCenterY
            previousCrop = geometry.lowerCropFraction
        }
    }

    @Test
    fun revealOrderIsWheelThenSummaryThenLedgerThenNavigation() {
        assertEquals(1f, revealFraction(RevealLayer.Wheel, .6f), .001f)
        assertTrue(revealFraction(RevealLayer.Summary, .6f) > revealFraction(RevealLayer.Ledger, .6f))
        assertEquals(0f, revealFraction(RevealLayer.Navigation, .6f), .001f)
    }

    @Test
    fun compactRegionsLeaveAUsableLedgerViewport() {
        assertTrue(compactDetailRegionsFit(640f))
        val budget = compactDetailBudget(640f)
        assertEquals(294f, budget.clipBottom, .001f)
        assertEquals(346f, budget.remainingHeight, .001f)
        assertEquals(58f, budget.contextHeight, 0f)
        assertEquals(48f, budget.closeHeight, 0f)
        assertEquals(62f, budget.navigationHeight, 0f)
        assertTrue(budget.ledgerHeight > 0f)
        assertTrue(budget.ledgerHeight >= 140f)
    }
}
