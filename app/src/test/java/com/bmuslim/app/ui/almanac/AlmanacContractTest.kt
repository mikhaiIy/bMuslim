package com.bmuslim.app.ui.almanac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AlmanacContractTest {
    private val points = listOf(
        AlmanacPoint("imsak", "Imsak", 345, AlmanacVisualRole.Secondary, "Imsak 05:45"),
        AlmanacPoint("subuh", "Subuh", 355, AlmanacVisualRole.Primary, "Subuh 05:55"),
        AlmanacPoint("syuruk", "Syuruk", 435, AlmanacVisualRole.Secondary, "Syuruk 07:15"),
        AlmanacPoint("dhuha", "Dhuha", 460, AlmanacVisualRole.Secondary, "Dhuha 07:40"),
        AlmanacPoint("zohor", "Zohor", 800, AlmanacVisualRole.Primary, "Zohor 13:20"),
        AlmanacPoint("asar", "Asar", 1005, AlmanacVisualRole.Primary, "Asar 16:45"),
        AlmanacPoint("maghrib", "Maghrib", 1168, AlmanacVisualRole.Primary, "Maghrib 19:28"),
        AlmanacPoint("isyak", "Isyak", 1242, AlmanacVisualRole.Primary, "Isyak 20:42"),
    )

    @Test fun nonCircularSelectionHasNoTomorrowWrap() {
        val before = NonCircularPointSelection.select(points, 300)
        assertNull(before.previous)
        assertEquals("imsak", before.next?.id)
        val after = NonCircularPointSelection.select(points, 1380)
        assertEquals("isyak", after.previous?.id)
        assertNull(after.next)
        assertTrue(after.dayComplete)
    }

    @Test fun keyedReplacementAtomicallyResetsMotionDragScrubAndFocus() {
        val dirty = AlmanacShellState("old", 300, 620, AlmanacPresentation.Folio, true, true, AlmanacPresentation.Folio)
        val replaced = reduceAlmanacState(dirty, AlmanacIntent.ReplaceModel("new", 625))
        assertEquals("new", replaced.modelKey)
        assertEquals(625, replaced.selectedMinute)
        assertEquals(AlmanacPresentation.Glance, replaced.presentation)
        assertFalse(replaced.hasScrubbed)
        assertFalse(replaced.dragging)
        assertNull(replaced.pendingFocus)
    }

    @Test fun liveFollowFreezesOnScrubAndReturnImmediatelyResumes() {
        var state = AlmanacShellState("key", 600, 600)
        state = reduceAlmanacState(state, AlmanacIntent.Tick(601))
        assertEquals(601, state.selectedMinute)
        state = reduceAlmanacState(state, AlmanacIntent.Scrub(300))
        state = reduceAlmanacState(state, AlmanacIntent.Tick(602))
        assertEquals(300, state.selectedMinute)
        state = reduceAlmanacState(state, AlmanacIntent.ReturnToNow)
        assertEquals(602, state.selectedMinute)
        assertFalse(state.hasScrubbed)
        assertEquals(603, reduceAlmanacState(state, AlmanacIntent.Tick(603)).selectedMinute)
    }

    @Test fun staleFocusSettlementCannotClearNewModelIntent() {
        val state = AlmanacShellState("new", 600, 600, pendingFocus = AlmanacPresentation.Glance)
        assertEquals(state, reduceAlmanacState(state, AlmanacIntent.FocusSettled("old")))
        assertNull(reduceAlmanacState(state, AlmanacIntent.FocusSettled("new")).pendingFocus)
    }

    @Test fun handoffGivesSemanticsOnlyToSettledDestination() {
        assertEquals(AlmanacSemanticOwner.Wheel, presentationHandoff(0f, AlmanacPresentation.Glance).semanticOwner)
        assertEquals(AlmanacSemanticOwner.None, presentationHandoff(.5f, null).semanticOwner)
        assertEquals(AlmanacSemanticOwner.Folio, presentationHandoff(1f, AlmanacPresentation.Folio).semanticOwner)
    }

    @Test fun measuredHourScaleHasTwentyFourTicksAndOnlyQuarterDayLabels() {
        val scale = almanacHourScale()

        assertEquals((0 until 24).toList(), scale.map { it.hour })
        assertEquals((0 until ALMANAC_DAY_MINUTES step 60).toList(), scale.map { it.minute })
        assertEquals(listOf("00", "06", "12", "18"), scale.mapNotNull { it.label })
        assertEquals(listOf(0, 6, 12, 18), scale.filter { it.label != null }.map { it.hour })
        assertTrue(scale.filter { it.label != null }.all { it.tickLengthDp == 6f })
        assertTrue(scale.filter { it.label == null }.all { it.tickLengthDp == 3f })
    }

    @Test fun wheelInteractionBelongsOnlyToSettledGlance() {
        assertTrue(wheelOwnsInteraction(presentationHandoff(0f, AlmanacPresentation.Glance)))
        assertFalse(wheelOwnsInteraction(presentationHandoff(.2f, null)))
        assertFalse(wheelOwnsInteraction(presentationHandoff(.5f, null)))
        assertFalse(wheelOwnsInteraction(presentationHandoff(.8f, null)))
        assertFalse(wheelOwnsInteraction(presentationHandoff(1f, AlmanacPresentation.Folio)))
    }

    @Test fun maximumEventKeyFadesWithWheelAndDropsSemanticsWithoutOwnership() {
        val settledWheel = maximumEventKeyPresentation(
            AlmanacFontComposition.Maximum,
            presentationHandoff(0f, AlmanacPresentation.Glance),
        )
        assertEquals(1f, settledWheel.visualAlpha, .001f)
        assertTrue(settledWheel.ownsSemantics)

        val transition = maximumEventKeyPresentation(
            AlmanacFontComposition.Maximum,
            presentationHandoff(.30f, null),
        )
        assertTrue(transition.visualAlpha in .001f..<1f)
        assertFalse(transition.ownsSemantics)

        val settledFolio = maximumEventKeyPresentation(
            AlmanacFontComposition.Maximum,
            presentationHandoff(1f, AlmanacPresentation.Folio),
        )
        assertEquals(0f, settledFolio.visualAlpha, .001f)
        assertFalse(settledFolio.ownsSemantics)

        val regular = maximumEventKeyPresentation(
            AlmanacFontComposition.Regular,
            presentationHandoff(0f, AlmanacPresentation.Glance),
        )
        assertEquals(0f, regular.visualAlpha, .001f)
        assertFalse(regular.ownsSemantics)
    }

    @Test fun quietGripVisibilityBandAndOwnershipFollowSettledWheelOnly() {
        val scene = almanacSceneGeometry(360f, 640f, compact = true, progress = 0f, maximumType = true)
        val settledWheel = quietGripPresentation(
            scene,
            presentationHandoff(0f, AlmanacPresentation.Glance),
            learned = false,
            dragging = false,
            composition = AlmanacFontComposition.Maximum,
        )
        assertTrue(settledWheel.visible)
        assertFalse(settledWheel.ownsSemantics)
        assertTrue(settledWheel.lineTopDp >= 108f)
        assertTrue(settledWheel.lineTopDp + settledWheel.lineHeightDp <= scene.viewportHeight)
        assertTrue(settledWheel.notchYDp <= scene.viewportHeight)

        val touching = quietGripPresentation(
            scene,
            presentationHandoff(0f, AlmanacPresentation.Glance),
            learned = false,
            dragging = true,
            composition = AlmanacFontComposition.Maximum,
        )
        assertTrue(touching.visible)
        assertTrue(touching.visualAlpha < settledWheel.visualAlpha)

        assertFalse(quietGripPresentation(scene, presentationHandoff(.3f, null), false, false).visible)
        assertFalse(quietGripPresentation(scene, presentationHandoff(0f, AlmanacPresentation.Glance), true, false).visible)
        assertFalse(quietGripPresentation(scene, presentationHandoff(1f, AlmanacPresentation.Folio), false, false).visible)
        assertTrue(wheelOpenActionVisible(presentationHandoff(0f, AlmanacPresentation.Glance), dragging = false))
        assertFalse(wheelOpenActionVisible(presentationHandoff(0f, AlmanacPresentation.Glance), dragging = true))
        assertFalse(wheelOpenActionVisible(presentationHandoff(.3f, null), dragging = false))
    }

    @Test fun nonMaximumQuietGripClearsLowestWheelBoundAndPrecedesCta() {
        val cases = listOf(
            almanacSceneGeometry(390f, 844f, compact = false, progress = 0f) to AlmanacFontComposition.Regular,
            almanacSceneGeometry(360f, 640f, compact = true, progress = 0f) to AlmanacFontComposition.Regular,
            almanacSceneGeometry(360f, 640f, compact = true, progress = 0f, largeType = true) to AlmanacFontComposition.Comfortable,
            almanacSceneGeometry(360f, 640f, compact = true, progress = 0f, largeType = true) to AlmanacFontComposition.Large,
        )

        cases.forEach { (scene, composition) ->
            val hint = quietGripPresentation(
                scene,
                presentationHandoff(0f, AlmanacPresentation.Glance),
                learned = false,
                dragging = false,
                composition = composition,
            )
            val lowestWheelBound = scene.centerY + scene.outerRadius
            val globalHintBottom = scene.wheelTop + hint.lineTopDp + hint.lineHeightDp

            assertEquals(lowestWheelBound + 8f, hint.lineTopDp, .001f)
            assertTrue(hint.lineTopDp + hint.lineHeightDp <= scene.viewportHeight)
            assertTrue(openActionTopDp(scene, returnToNowVisible = false) >= globalHintBottom + 8f)
        }
    }

    @Test fun quietGripRetiresOnlyAfterCompletedQuantizedChange() {
        val unchanged = beginQuietGripGesture(600)
        assertFalse(quietGripShouldRetire(unchanged, AlmanacQuietGripGestureEnd.Completed))
        assertFalse(quietGripShouldRetire(unchanged, AlmanacQuietGripGestureEnd.Cancelled))

        val belowStep = recordQuietGripQuantizedMinute(unchanged, 600)
        assertFalse(quietGripShouldRetire(belowStep, AlmanacQuietGripGestureEnd.Completed))

        val changed = recordQuietGripQuantizedMinute(unchanged, 605)
        assertTrue(quietGripShouldRetire(changed, AlmanacQuietGripGestureEnd.Completed))
        assertFalse(quietGripShouldRetire(changed, AlmanacQuietGripGestureEnd.Cancelled))

        val crossedMidnight = recordQuietGripQuantizedMinute(beginQuietGripGesture(1435), 0)
        assertTrue(quietGripShouldRetire(crossedMidnight, AlmanacQuietGripGestureEnd.Completed))
    }

    @Test fun geometryKeepsConstantRadiusAndCompactApertureUsable() {
        val glance = almanacSceneGeometry(360f, 640f, true, 0f)
        val folio = almanacSceneGeometry(360f, 640f, true, 1f)
        assertEquals(glance.outerRadius, folio.outerRadius, 0f)
        assertTrue(folio.globalCenterY < glance.globalCenterY)
        assertTrue(640f - folio.clipBottom > 300f)
    }

    @Test fun returnToNowAndOpenActionStayOutsideDialAndDisjoint() {
        listOf(
            almanacSceneGeometry(390f, 844f, compact = false, progress = 0f),
            almanacSceneGeometry(360f, 640f, compact = true, progress = 0f),
            almanacSceneGeometry(360f, maximumGlanceContentHeightDp(640f), compact = true, progress = 0f, maximumType = true),
        ).forEach { scene ->
            val returnTop = returnToNowTopDp(scene)
            assertTrue(returnTop >= scene.clipBottom)
            assertTrue(openActionTopDp(scene, returnToNowVisible = true) >= returnTop + 48f + 8f)
            assertEquals(scene.clipBottom + 16f, openActionTopDp(scene, returnToNowVisible = false), .001f)
        }
    }

    @Test fun maximumGlanceBandsAreMeasuredAndDisjointAt360By640TwoX() {
        val contentHeight = maximumGlanceContentHeightDp(640f)
        val scene = almanacSceneGeometry(360f, contentHeight, compact = true, progress = 0f, maximumType = true)
        val readout = readoutBayGeometry(360f, scene, 320f, 64f, external = true)
        val quietGrip = quietGripPresentation(
            scene,
            presentationHandoff(0f, AlmanacPresentation.Glance),
            learned = false,
            dragging = false,
            composition = AlmanacFontComposition.Maximum,
        )

        assertEquals(780f, contentHeight, .001f)
        assertTrue(readout.bounds.bottom <= ALMANAC_MAXIMUM_KEY_TOP_DP)
        assertTrue(ALMANAC_MAXIMUM_KEY_TOP_DP + ALMANAC_MAXIMUM_KEY_HEIGHT_DP <= scene.centerY - scene.outerRadius)
        assertTrue(scene.centerY + scene.outerRadius <= quietGrip.lineTopDp)
        assertTrue(quietGrip.lineTopDp + quietGrip.lineHeightDp <= scene.viewportHeight)
        assertTrue(openActionTopDp(scene, returnToNowVisible = false) + 64f <= contentHeight)
    }

    @Test fun rolesAndCollisionOwnerAreGeneric() {
        assertEquals(5, points.count { it.role == AlmanacVisualRole.Primary })
        assertEquals(3, points.count { it.role == AlmanacVisualRole.Secondary })
        val first = AlmanacLabelCandidate("selected", AlmanacLabelBounds(-5f, 2f, 75f, 20f))
        val second = AlmanacLabelCandidate("next", AlmanacLabelBounds(20f, 2f, 90f, 20f))
        val result = resolveLabelPlacement(first, second, 100f, 50f, 8f)
        assertEquals(listOf("selected"), result.map { it.id })
        assertTrue(result.single().bounds.isInside(100f, 50f, 8f))
    }

    @Test fun centeredReadoutOwnsNegativeSpaceOnlyWhileItIsVisible() {
        val scene = almanacSceneGeometry(390f, 844f, false, .25f)
        val bay = readoutBayGeometry(390f, scene, 132f, 42f, false)
        assertTrue(bay.bounds.left >= 16f)
        assertEquals(195f, (bay.bounds.left + bay.bounds.right) / 2f, .001f)
        assertEquals(195f, bay.leaderStartX, .001f)
        assertEquals(bay.leaderStartX, bay.leaderEndX, .001f)
        assertEquals(scene.centerY - scene.outerRadius - 12f, bay.bounds.bottom, .001f)
        assertFalse(primitiveIsOutsideExclusion(bay.bounds, readoutExclusionAtAlpha(bay, 1f)))
        assertNull(readoutExclusionAtAlpha(bay, .998f))
        assertNull(readoutExclusionAtAlpha(bay, .5f))
        assertNull(readoutExclusionAtAlpha(bay, 0f))
        assertFalse(AlmanacPrimitiveLayer.entries.any(::readoutMayClipPrimitiveLayer))

        val maximum = almanacSceneGeometry(360f, 640f, true, 1f, maximumType = true)
        val external = readoutBayGeometry(360f, maximum, 280f, 64f, true)
        assertTrue(external.external)
        assertEquals(40f, external.bounds.left, 0f)
        assertNull(readoutExclusionAtAlpha(external, 1f))
        assertTrue(external.bounds.bottom < maximum.centerY - maximum.outerRadius)
    }

    @Test fun readoutReservationEndsAsSoonAsTheOpenTransitionStartsFading() {
        val scene = almanacSceneGeometry(390f, 844f, false, 0f)
        val bay = readoutBayGeometry(390f, scene, 198f, 42f, false)
        val progressSamples = listOf(0f, .24f, .25f, .30f, .36f, .40f, .42f, 1f)

        progressSamples.forEach { progress ->
            val alpha = presentationHandoff(progress, null).wheelAlpha
            val exclusion = readoutExclusionAtAlpha(bay, alpha)
            if (progress <= .24f) assertEquals(bay.bounds, exclusion) else assertNull(exclusion)
        }
    }

    @Test fun adaptiveLabelsReplaceRadialCopyWithCompleteMaximumEventKey() {
        val selection = NonCircularPointSelection.select(points, 600)
        assertEquals(listOf("subuh", "zohor", "asar", "maghrib", "isyak"), radialLabelIds(points, selection, AlmanacFontComposition.Regular, glance = true))
        assertEquals(listOf("zohor"), radialLabelIds(points, selection, AlmanacFontComposition.Regular, glance = false))
        assertTrue(radialLabelIds(points, selection, AlmanacFontComposition.Large, glance = true).isEmpty())
        assertTrue(radialLabelIds(points, selection, AlmanacFontComposition.Maximum, glance = false).isEmpty())
        assertTrue(radialLabelIds(points, selection, AlmanacFontComposition.Maximum, glance = true).isEmpty())
        assertEquals("Subuh", radialLabelText(points.first { it.id == "subuh" }))
        assertEquals("Imsak", radialLabelText(points.first { it.id == "imsak" }))
        assertEquals(
            listOf("subuh", "zohor", "asar", "maghrib", "isyak"),
            radialLabelIds(points, NonCircularPointSelection.select(points, 300), AlmanacFontComposition.Regular, glance = true),
        )
        assertEquals(8, externalEventKey(points).size)
        assertEquals("Imsak 05:45", externalEventKey(points).first())
    }

    @Test fun secondaryMarkersRemainDistinctWithoutColour() {
        assertEquals(AlmanacPointMarker.ImsakNotches, pointMarker(points.first { it.id == "imsak" }))
        assertEquals(AlmanacPointMarker.PrimaryPrayer, pointMarker(points.first { it.id == "subuh" }))
        assertEquals(AlmanacPointMarker.SyurukHorizon, pointMarker(points.first { it.id == "syuruk" }))
        assertEquals(AlmanacPointMarker.DhuhaDiamond, pointMarker(points.first { it.id == "dhuha" }))
    }

    @Test fun sharedApertureGeometryPreservesOriginalEndpointsAndMonotonicMotion() {
        val regular = almanacSceneGeometry(390f, 844f, false, 1f)
        val compact = almanacSceneGeometry(360f, 640f, true, 1f)
        assertEquals(220f, regular.viewportHeight, .001f)
        assertEquals(220f * .93f, regular.centerY, .001f)
        assertEquals(182f, compact.viewportHeight, .001f)
        assertEquals(182f * .93f, compact.centerY, .001f)
        var priorCenter = Float.POSITIVE_INFINITY
        var priorCrop = -1f
        val radius = almanacSceneGeometry(390f, 844f, false, 0f).outerRadius
        for (step in 0..100) {
            val geometry = almanacSceneGeometry(390f, 844f, false, step / 100f)
            assertTrue(geometry.globalCenterY <= priorCenter)
            assertTrue(geometry.lowerCropFraction >= priorCrop)
            assertEquals(radius, geometry.outerRadius, 0f)
            priorCenter = geometry.globalCenterY
            priorCrop = geometry.lowerCropFraction
        }
    }

    @Test fun sharedRevealAndQuantizationRetainDemoAndProductionPolicies() {
        assertEquals(1f, revealFraction(AlmanacRevealLayer.Wheel, .6f), .001f)
        assertTrue(revealFraction(AlmanacRevealLayer.Summary, .6f) > revealFraction(AlmanacRevealLayer.Body, .6f))
        assertEquals(0f, revealFraction(AlmanacRevealLayer.Navigation, .6f), .001f)
        assertEquals(1435, quantizeAccessibleMinute(1439f, 5))
        assertEquals(0, quantizeMinute(1439, 5))
        assertEquals(1439, quantizeAccessibleMinute(1439f, 1))
        assertEquals(0f..1439f, accessibleMinuteRange())
        assertEquals(1438, accessibleMinuteSteps(1))
    }

    @Test fun intervalVisualGrammarHasFiveDistinctNonColourPrimitives() {
        assertEquals(AlmanacIntervalPrimitive.SolidArc, intervalPrimitive(AlmanacIntervalVisual.PublishedWindow))
        assertEquals(
            AlmanacIntervalPrimitive.ContinuousArcWithHollowBead,
            intervalPrimitive(AlmanacIntervalVisual.NamedPeriodEstimate),
        )
        assertEquals(
            AlmanacIntervalPrimitive.StripedArcWithHollowBead,
            intervalPrimitive(AlmanacIntervalVisual.NightSegmentEstimate),
        )
        assertEquals(
            AlmanacIntervalPrimitive.HatchedEnvelopeWithTerminal,
            intervalPrimitive(AlmanacIntervalVisual.SearchEnvelope),
        )
        assertEquals(AlmanacIntervalPrimitive.HollowEventTab, intervalPrimitive(AlmanacIntervalVisual.EventTrigger))
    }

    @Test fun adaptiveSemanticLanesAndCountedGlyphsFollowTypeScaleContracts() {
        assertEquals(3, semanticLaneBudget(AlmanacFontComposition.Regular))
        assertEquals(2, semanticLaneBudget(AlmanacFontComposition.Comfortable))
        assertEquals(2, semanticLaneBudget(AlmanacFontComposition.Large))
        assertEquals(1, semanticLaneBudget(AlmanacFontComposition.Maximum))

        listOf(
            AlmanacFontComposition.Regular to listOf(14f to 9f),
            AlmanacFontComposition.Comfortable to listOf(16f to 12f),
            AlmanacFontComposition.Large to listOf(16f to 12f),
            AlmanacFontComposition.Maximum to listOf(18f to 14f),
        ).forEach { (composition, expected) ->
            val size = eventGlyphSize(composition)
            assertEquals(expected.first().first, size.widthDp, .001f)
            assertEquals(expected.first().second, size.heightDp, .001f)
            val budget = semanticLaneBudget(composition)
            val scene = almanacSceneGeometry(
                360f,
                640f,
                compact = true,
                progress = 0f,
                maximumType = composition == AlmanacFontComposition.Maximum,
                largeType = composition == AlmanacFontComposition.Comfortable || composition == AlmanacFontComposition.Large,
            )
            val radii = (1..budget).map { adaptiveIntervalLaneRadius(scene.outerRadius, it, composition) }
            assertTrue(radii.all { it >= scene.outerRadius * .90f })
            assertTrue(radii.all { it <= scene.outerRadius - 3f })
            assertTrue(radii.zipWithNext().all { (first, second) -> second - first >= 6f })
        }
    }

    @Test fun coverageUnionKeepsExactPointerSemanticsAndNoInventedGap() {
        val aggregate = AlmanacInterval(
            id = "overflow",
            startMinute = 100,
            endMinute = 400,
            markerMinute = 100,
            lane = 3,
            visual = AlmanacIntervalVisual.SearchEnvelope,
            coverageSegments = listOf(
                AlmanacCoverageSegment(100, 150),
                AlmanacCoverageSegment(300, 400),
            ),
        )

        assertTrue(intervalContains(aggregate, 120))
        assertFalse(intervalContains(aggregate, 200))
        assertTrue(intervalContains(aggregate, 350))
        assertEquals(100, intervalAnchorMinute(aggregate))
    }

    @Test fun pointerIntersectionIncludesEventToleranceAndCrossMidnightWindows() {
        val event = AlmanacInterval(
            id = "event",
            startMinute = 800,
            endMinute = 800,
            markerMinute = 800,
            visual = AlmanacIntervalVisual.EventTrigger,
        )
        val night = AlmanacInterval(
            id = "night",
            startMinute = 1320,
            endMinute = 300,
            markerMinute = 1320,
            lane = 1,
            visual = AlmanacIntervalVisual.NightSegmentEstimate,
        )

        assertTrue(event in intersectingIntervalsAt(listOf(event, night), 790))
        assertFalse(intervalSemanticallyContains(event, 790))
        assertTrue(intervalSemanticallyContains(event, 800))
        assertTrue(event in intersectingIntervalsAt(listOf(event, night), 810))
        assertFalse(event in intersectingIntervalsAt(listOf(event, night), 811))
        assertTrue(night in intersectingIntervalsAt(listOf(event, night), 30))
        assertFalse(night in intersectingIntervalsAt(listOf(event, night), 600))
    }

    @Test fun occurrenceWordingSeparatesApprovedActiveFromResearchGeometry() {
        val direct = AlmanacInterval("direct", 500, 700, 500, truthClass = AlmanacIntervalTruthClass.Direct)
        val draft = AlmanacInterval("draft", 500, 700, 500, truthClass = AlmanacIntervalTruthClass.ProvisionalResearch)
        val proxy = AlmanacInterval("proxy", 500, 700, 500, truthClass = AlmanacIntervalTruthClass.ScheduleContextProxy)
        val search = AlmanacInterval(
            "search", 500, 700, 700,
            visual = AlmanacIntervalVisual.SearchEnvelope,
            truthClass = AlmanacIntervalTruthClass.ProvisionalResearch,
        )
        assertEquals(AlmanacIntervalOccurrence.Active, intervalOccurrence(direct, 600))
        assertEquals(AlmanacIntervalOccurrence.PolicyWindow, intervalOccurrence(direct.copy(id = "policy", truthClass = AlmanacIntervalTruthClass.PolicyMapped), 600))
        assertEquals(AlmanacIntervalOccurrence.DraftWindow, intervalOccurrence(draft, 600))
        assertEquals(AlmanacIntervalOccurrence.ScheduleContext, intervalOccurrence(proxy, 600))
        assertEquals(AlmanacIntervalOccurrence.SearchRegion, intervalOccurrence(search, 600))
        assertEquals(AlmanacIntervalOccurrence.UpcomingDraftWindow, intervalOccurrence(draft, 400))
        assertEquals(AlmanacIntervalOccurrence.UpcomingSearchRegion, intervalOccurrence(search, 400))
        val proxyTrigger = proxy.copy(id = "proxy-trigger", startMinute = 600, endMinute = 600, markerMinute = 600, visual = AlmanacIntervalVisual.EventTrigger)
        assertEquals(AlmanacIntervalOccurrence.ScheduleContext, intervalOccurrence(proxyTrigger, 600))
        assertEquals(AlmanacIntervalOccurrence.UpcomingScheduleContext, intervalOccurrence(proxyTrigger, 590))
    }

    @Test fun maximumCompositionKeepsWheelAndActionInsideCompactScreen() {
        val contentHeight = maximumGlanceContentHeightDp(640f)
        val maximum = almanacSceneGeometry(360f, contentHeight, compact = true, progress = 0f, maximumType = true)
        assertTrue(openActionTopDp(maximum, returnToNowVisible = false) + 64f <= contentHeight)
    }

    @Test fun pointerSelectionUsesStableLaneThenIdOrderingForOverlaps() {
        val laterLane = AlmanacInterval("z", 500, 700, 500, lane = 2)
        val firstById = AlmanacInterval("a", 550, 650, 550, lane = 1)
        val secondById = AlmanacInterval("b", 550, 650, 550, lane = 1)

        assertEquals(
            listOf("a", "b", "z"),
            intersectingIntervalsAt(listOf(laterLane, secondById, firstById), 600).map { it.id },
        )
        assertEquals("a", selectIntervalForMinute(listOf(laterLane, secondById, firstById), 600)?.id)
    }

    @Test fun pointerSelectionFallsForwardButNeverCallsPastDeedNext() {
        val morning = AlmanacInterval("morning", 360, 420, 360, lane = 2)
        val evening = AlmanacInterval("evening", 1000, 1100, 1000, lane = 1)

        assertEquals("evening", selectIntervalForMinute(listOf(morning, evening), 800)?.id)
        assertNull(selectIntervalForMinute(listOf(morning, evening), 1200))
        assertNull(selectIntervalForMinute(emptyList(), 800))
    }

    @Test fun accessibilityEventActionsAreStrictlyPreviousAndNext() {
        assertEquals("subuh", previousPoint(points, 400)?.id)
        assertEquals("syuruk", nextPoint(points, 400)?.id)
        assertNull(previousPoint(points, 300))
        assertNull(nextPoint(points, 1380))
    }

    @Test fun threeContextLanesStayInsideWheelAtCompactAndMaximumGeometry() {
        listOf(
            almanacSceneGeometry(360f, 640f, compact = true, progress = 0f),
            almanacSceneGeometry(360f, 640f, compact = true, progress = 1f, maximumType = true),
        ).forEach { scene ->
            val secondary = scene.outerRadius * .88f
            val radii = (0..3).map { intervalLaneRadius(secondary, scene.outerRadius, it) }
            assertEquals(secondary, radii.first(), .001f)
            assertTrue(radii.zipWithNext().all { (first, second) -> first <= second })
            assertTrue(radii.last() <= scene.outerRadius - 4f)
        }
    }

    @Test fun allVisibleMeasuredLabelsUseOneBoundsCollisionAndPriorityPipeline() {
        val width = 390f
        val height = 400f
        val exclusion = AlmanacLabelBounds(225f, 34f, 374f, 92f)
        fun candidates(selectedMinute: Int) = points.map { point ->
            val radius = if (point.role == AlmanacVisualRole.Primary) 126f else 146f
            val radians = minuteAngle(point.minute, selectedMinute) * PI.toFloat() / 180f
            val x = width / 2f + cos(radians) * (radius - 20f)
            val y = 220f + sin(radians) * (radius - 20f)
            AlmanacLabelCandidate(point.id, AlmanacLabelBounds(x - 34f, y - 7f, x + 34f, y + 7f))
        }
        val cases = listOf(
            300 to listOf("imsak"),
            720 to listOf("dhuha", "zohor"),
            1380 to listOf("isyak"),
        )
        cases.forEach { (minute, expectedPriority) ->
            val selection = NonCircularPointSelection.select(points, minute)
            val priority = listOfNotNull(selection.exact?.id ?: selection.previous?.id, selection.next?.id) + points.map { it.id }
            val painted = resolveAllLabelPlacements(candidates(minute), priority, width, height, 8f, exclusion)
            assertTrue("priority owner at $minute", painted.map { it.id }.take(expectedPriority.size) == expectedPriority)
            assertTrue("renderer retains visible labels at $minute", painted.isNotEmpty())
            assertTrue(painted.all { it.bounds.isInside(width, height, 8f) })
            assertTrue(painted.none { it.bounds.intersects(exclusion) })
            painted.forEachIndexed { index, candidate ->
                assertTrue(painted.drop(index + 1).none { candidate.bounds.intersects(it.bounds) })
            }
        }
    }
}
