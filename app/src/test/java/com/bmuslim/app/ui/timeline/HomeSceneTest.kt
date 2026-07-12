package com.bmuslim.app.ui.timeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun clockInkStaysSeparatedFromApertureAcrossTheWholeTransition() {
        for (fontScale in listOf(1f, 1.3f, 1.6f, 2f)) {
            for (inset in listOf(0f, 24f, 36f)) {
                for (compact in listOf(false, true)) {
                    val width = if (compact) 360f else 390f
                    val height = if (compact) 640f else 844f
                    val measuredContent = (if (compact || fontScale >= 1.8f) 70f else 104f) * fontScale
                    val measuredHeader = requiredHeaderHeight(inset, measuredContent)
                    val measuredClock = SceneRect(24f, inset + 5f, width - 24f, inset + measuredContent)
                    for (step in 0..100) {
                        val progress = step / 100f
                        val scene = homeSceneGeometry(
                            width,
                            height,
                            compact,
                            progress,
                            maximumType = fontScale >= 1.8f,
                            measuredHeaderHeightDp = measuredHeader,
                        )
                        val safe = headerSafeGeometry(scene, measuredClock)
                        assertTrue("clock/aperture at $fontScale/$inset/$compact/$progress", safe.isSeparated)
                        assertFalse(safe.clock.overlaps(SceneRect(0f, safe.apertureTop, width, height)))
                    }
                }
            }
        }
    }

    @Test
    fun protectedReadoutPlacementIsStableAcrossTouchAndDirection() {
        val scene = homeSceneGeometry(390f, 844f, compact = false, progress = .25f)
        val settled = readoutBayGeometry(390f, scene, 132f, 42f, external = false)
        val touching = readoutBayGeometry(390f, scene, 132f, 42f, external = false)
        assertEquals(settled, touching)
        assertTrue(settled.bounds.left >= 16f)
        assertTrue(settled.bounds.right <= 390f - 16f)
        assertTrue(settled.leaderStartX < settled.leaderEndX)

        val maximumScene = homeSceneGeometry(360f, 640f, true, 0f, maximumType = true)
        val maximumType = readoutBayGeometry(360f, maximumScene, 280f, 64f, external = true)
        assertEquals(16f, maximumType.bounds.left, 0f)
        assertEquals(280f, maximumType.bounds.width, 0f)
        assertTrue(maximumType.leaderEndX - maximumType.leaderStartX <= 24f)
        assertTrue(maximumType.bounds.bottom < maximumScene.centerY - maximumScene.outerRadius)
    }

    @Test
    fun disclosureHandoffIsDeterministicInBothDirections() {
        var previousWheel = 1f
        var previousDetail = 0f
        for (step in 0..100) {
            val p = step / 100f
            val settled = when (step) {
                0 -> HomePresentation.Glance
                100 -> HomePresentation.Detail
                else -> null
            }
            val state = presentationHandoff(p, settled)
            assertTrue(state.wheelAlpha <= previousWheel)
            assertTrue(state.detailAlpha >= previousDetail)
            if (step in 1..99) assertEquals(SemanticOwner.None, state.semanticOwner)
            previousWheel = state.wheelAlpha
            previousDetail = state.detailAlpha
        }
        assertEquals(SemanticOwner.Wheel, presentationHandoff(0f, HomePresentation.Glance).semanticOwner)
        assertEquals(SemanticOwner.Detail, presentationHandoff(1f, HomePresentation.Detail).semanticOwner)

        var reverseWheel = 0f
        var reverseDetail = 1f
        for (step in 100 downTo 0) {
            val state = presentationHandoff(step / 100f, if (step == 0) HomePresentation.Glance else null)
            assertTrue(state.wheelAlpha >= reverseWheel)
            assertTrue(state.detailAlpha <= reverseDetail)
            if (step > 0) assertEquals(SemanticOwner.None, state.semanticOwner)
            reverseWheel = state.wheelAlpha
            reverseDetail = state.detailAlpha
        }
        assertEquals(SemanticOwner.None, presentationHandoff(.63f, null).semanticOwner)
        assertEquals(SemanticOwner.None, presentationHandoff(.37f, null).semanticOwner)
    }

    @Test
    fun accessibilityQuantizationNeverWrapsMaximumIntoMidnight() {
        assertEquals(1435, quantizeAccessibleMinute(1439f))
        assertEquals(1435, quantizeAccessibleMinute(1435f))
        assertEquals(0, quantizeAccessibleMinute(0f))
        assertEquals(0, quantizeCircularMinute(1439))
        assertEquals(1435, quantizeCircularMinute(1434))
        for (minute in 0..1439) {
            val accessible = quantizeAccessibleMinute(minute.toFloat())
            assertTrue(accessible in 0..1435)
            assertEquals(0, accessible % 5)
            assertEquals(0, quantizeCircularMinute(minute) % 5)
        }
    }

    @Test
    fun maximumTypeReallocatesHeaderAndWheelInsteadOfScalingTheRegularComposition() {
        val regular = homeSceneGeometry(360f, 640f, compact = true, progress = 1f)
        val adaptive = homeSceneGeometry(360f, 640f, compact = true, progress = 1f, maximumType = true)
        assertTrue(adaptive.wheelTop > regular.wheelTop)
        assertTrue(adaptive.outerRadius < regular.outerRadius)
        val measuredClock = SceneRect(194f, 30f, 344f, 132f)
        assertTrue(headerSafeGeometry(adaptive, measuredClock).isSeparated)

        val glance = homeSceneGeometry(360f, 640f, compact = true, progress = 0f, maximumType = true)
        assertTrue(adaptive.globalCenterY < glance.globalCenterY)
    }

    @Test
    fun fontCompositionCoversEveryLockedAdaptationLevel() {
        assertEquals(FontComposition.Regular, fontComposition(1f))
        assertEquals(FontComposition.Comfortable, fontComposition(1.3f))
        assertEquals(FontComposition.Large, fontComposition(1.6f))
        assertEquals(FontComposition.Maximum, fontComposition(2f))
    }

    @Test
    fun exclusionRejectsOnlyIntersectingPrimitiveBounds() {
        val exclusion = SceneRect(210f, 30f, 350f, 80f)
        assertFalse(primitiveIsOutsideExclusion(SceneRect(220f, 40f, 230f, 50f), exclusion))
        assertTrue(primitiveIsOutsideExclusion(SceneRect(180f, 40f, 200f, 50f), exclusion))
        assertTrue(primitiveIsOutsideExclusion(SceneRect(220f, 81f, 230f, 90f), exclusion))
    }

    @Test
    fun everyNonOwnerWheelLayerUsesTheMeasuredNegativeSpaceClip() {
        val nonOwners = listOf(
            WheelPrimitiveLayer.BroadRail,
            WheelPrimitiveLayer.HourRail,
            WheelPrimitiveLayer.PrayerRail,
            WheelPrimitiveLayer.ContextualArc,
            WheelPrimitiveLayer.CelestialRail,
            WheelPrimitiveLayer.ActualNow,
            WheelPrimitiveLayer.Keystone,
        )
        assertTrue(nonOwners.all(::primitiveLayerUsesNegativeSpace))
        assertFalse(primitiveLayerUsesNegativeSpace(WheelPrimitiveLayer.Pointer))
        assertFalse(primitiveLayerUsesNegativeSpace(WheelPrimitiveLayer.Leader))
        assertFalse(primitiveLayerUsesNegativeSpace(WheelPrimitiveLayer.Readout))
    }

    @Test
    fun returnToNowIsStructurallyLimitedToSettledDetail() {
        assertFalse(returnToNowVisible(presentationHandoff(.96f, null), 30))
        assertFalse(returnToNowVisible(presentationHandoff(1f, HomePresentation.Detail), 4))
        assertTrue(returnToNowVisible(presentationHandoff(1f, HomePresentation.Detail), 30))
        assertFalse(returnToNowVisible(presentationHandoff(0f, HomePresentation.Glance), 30))
    }

    @Test
    fun maximumCompactDetailUsesOneScrollReachableIntrinsicFlowAtEveryInset() {
        assertEquals(DetailScrollPolicy.SingleOuterScroll, detailScrollPolicy(FontComposition.Maximum))
        assertEquals(DetailScrollPolicy.WeightedLedger, detailScrollPolicy(FontComposition.Large))

        for (inset in listOf(0f, 24f, 36f)) {
            val measuredHeader = requiredHeaderHeight(
                safeInsetTop = inset,
                measuredContentHeight = 100f,
            )
            val scene = homeSceneGeometry(
                widthDp = 360f,
                heightDp = 640f,
                compact = true,
                progress = 1f,
                maximumType = true,
                measuredHeaderHeightDp = measuredHeader,
            )
            val budget = maximumDetailScrollBudget(
                screenHeightDp = 640f,
                geometry = scene,
                intrinsicSectionHeights = listOf(
                    175f, // complete adaptive fardhu/context/date disclosure
                    650f, // complete intrinsic ledger including Arabic/action/source
                    48f, // close control
                    62f + inset, // navigation plus conservative system inset
                ),
            )
            assertTrue("positive outer viewport at inset $inset", budget.viewportHeight > 0f)
            assertEquals(4, budget.sectionCount)
            assertEquals(0, budget.weightedChildCount)
            assertTrue(budget.requiresScroll)
            assertTrue(budget.allSectionsScrollReachable)
        }
    }
}
