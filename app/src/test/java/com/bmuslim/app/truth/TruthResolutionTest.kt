package com.bmuslim.app.truth

import java.lang.reflect.Modifier
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TruthResolutionTest {
    @Test
    fun fullyApprovedInputsAreTheOnlyReadyPath() {
        val resolution = resolve()

        assertTrue(resolution.isReady)
        assertTrue(resolution.blockers.isEmpty())
        assertEquals("act-1", resolution.readyPresentation?.primaryRecommendationId)
    }

    @Test
    fun productionDefaultsReturnTheThreeOrderedTopLevelBlockers() {
        val resolution = TruthResolution.resolve(
            TruthInputs(null, TemporalState.Unconfigured, null),
        )

        assertNull(resolution.readyPresentation)
        assertEquals(
            listOf(
                TruthBlocker.ContentMissing,
                TruthBlocker.TemporalProviderUnconfigured,
                TruthBlocker.RankingMissing,
            ),
            resolution.blockers.toList(),
        )
    }

    @Test
    fun syntheticContentSuppressesLifecycleAndClaimChecks() {
        val synthetic = validRelease().copy(
            fixtureClass = FixtureClass.SYNTHETIC,
            lifecycle = ContentLifecycle.DRAFT,
            orderedClaims = emptyList(),
            storedReleaseFingerprint = "mutated",
        )

        val blockers = resolve(release = synthetic).blockers
        assertEquals(setOf(TruthBlocker.SyntheticContentForbidden), blockers.filter(::contentBlocker).toSet())
    }

    @Test
    fun draftAndInReviewMapOnlyToNotPublished() {
        listOf(ContentLifecycle.DRAFT, ContentLifecycle.IN_REVIEW).forEach { lifecycle ->
            val blockers = resolve(
                release = validRelease().copy(lifecycle = lifecycle, storedReleaseFingerprint = "wrong"),
            ).blockers.filter(::contentBlocker)
            assertEquals(listOf(TruthBlocker.ContentNotPublished), blockers)
        }
    }

    @Test
    fun withdrawnMapsOnlyToWithdrawn() {
        val blockers = resolve(
            release = validRelease().copy(
                lifecycle = ContentLifecycle.WITHDRAWN,
                storedReleaseFingerprint = "wrong",
            ),
        ).blockers.filter(::contentBlocker)

        assertEquals(listOf(TruthBlocker.ContentWithdrawn), blockers)
    }

    @Test
    fun publishedContentReportsCompletenessAndApprovalFailuresTogether() {
        val release = validRelease()
        val brokenClaim = release.orderedClaims.single().copy(payload = "", evidenceIds = emptySet())
        val broken = release.copy(
            orderedClaims = listOf(brokenClaim),
            claimApprovals = emptyList(),
            storedReleaseFingerprint = "wrong",
        )

        assertEquals(
            listOf(TruthBlocker.ContentIncomplete, TruthBlocker.ContentApprovalMismatch),
            resolve(release = broken).blockers.filter(::contentBlocker),
        )
    }

    @Test
    fun payloadMutationInvalidatesClaimApprovalAndReleaseIdentity() {
        val release = validRelease()
        val mutated = release.copy(
            orderedClaims = listOf(release.orderedClaims.single().copy(payload = "changed")),
        )

        assertEquals(
            listOf(TruthBlocker.ContentApprovalMismatch),
            resolve(release = mutated).blockers.filter(::contentBlocker),
        )
    }

    @Test
    fun unresolvedOrUnsupportedEvidenceIsIncomplete() {
        val release = validRelease()
        val unsupported = release.copy(
            evidenceRecords = release.evidenceRecords.map { it.copy(supportedClaimTypes = setOf(ClaimType.TEXT)) },
        ).withCanonicalReleaseFingerprint()

        assertTrue(TruthBlocker.ContentIncomplete in resolve(release = unsupported).blockers)
    }

    @Test
    fun unknownAndDeniedUsePermissionsStayDistinct() {
        val unknown = resolve(temporal = validTemporal(PermissionState.UNKNOWN)).blockers
        val denied = resolve(temporal = validTemporal(PermissionState.DENIED)).blockers

        assertTrue(TruthBlocker.TemporalRightsUnknown in unknown)
        assertFalse(TruthBlocker.TemporalUseDenied in unknown)
        assertTrue(TruthBlocker.TemporalUseDenied in denied)
        assertFalse(TruthBlocker.TemporalRightsUnknown in denied)
    }

    @Test
    fun persistenceRightsAreCheckedOnlyWhenPersistenceIsRequested() {
        val unknown = validTemporal(persistencePermission = PermissionState.UNKNOWN)
        val denied = validTemporal(persistencePermission = PermissionState.DENIED)

        assertTrue(resolve(temporal = unknown).isReady)
        assertTrue(resolve(temporal = denied).isReady)
        assertEquals(
            setOf(TruthBlocker.TemporalPersistenceRightsUnknown),
            resolve(temporal = unknown, persistenceRequested = true).blockers,
        )
        assertEquals(
            setOf(TruthBlocker.TemporalPersistenceDenied),
            resolve(temporal = denied, persistenceRequested = true).blockers,
        )
    }

    @Test
    fun readyTemporalStateValidatesTermsKeyAndCompleteChronologyTogether() {
        val valid = validTemporal()
        val wrongKey = valid.requestedKey.copy(civilDate = valid.requestedKey.civilDate.plusDays(1))
        val broken = valid.copy(
            requestedKey = wrongKey,
            sourceContract = valid.sourceContract.copy(termsReference = ""),
            snapshot = valid.snapshot.copy(dateBearingValues = valid.snapshot.dateBearingValues.dropLast(1)),
        )

        assertEquals(
            listOf(
                TruthBlocker.TemporalContractReferenceMissing,
                TruthBlocker.TemporalKeyMismatch,
                TruthBlocker.TemporalIncomplete,
            ),
            resolve(temporal = broken).blockers.filter(::temporalBlocker),
        )
        assertNull(resolve(temporal = broken).readyPresentation)
    }

    @Test
    fun timeAscendingInputWithReversedSlotAssignmentsFailsCanonicalChronology() {
        val temporal = validTemporal()
        val reversedAssignments = temporal.snapshot.dateBearingValues.mapIndexed { index, value ->
            value.copy(slot = PrayerSlot.entries.reversed()[index])
        }
        val broken = temporal.copy(
            snapshot = temporal.snapshot.copy(dateBearingValues = reversedAssignments),
        )

        assertEquals(setOf(TruthBlocker.TemporalIncomplete), resolve(temporal = broken).blockers)
    }

    @Test
    fun validSlotAssignmentsPassInArbitraryInputOrder() {
        val temporal = validTemporal()
        val values = temporal.snapshot.dateBearingValues
        val arbitraryOrder = listOf(values[3], values[0], values[4], values[1], values[2])
        val reordered = temporal.copy(
            snapshot = temporal.snapshot.copy(dateBearingValues = arbitraryOrder),
        )

        assertTrue(resolve(temporal = reordered).isReady)
    }

    @Test
    fun provenanceMustMatchThePermittedContract() {
        val temporal = validTemporal()
        val broken = temporal.copy(
            snapshot = temporal.snapshot.copy(
                provenance = temporal.snapshot.provenance.copy(contractVersion = "other"),
            ),
        )

        assertTrue(TruthBlocker.TemporalIncomplete in resolve(temporal = broken).blockers)
    }

    @Test
    fun unconfiguredTemporalSuppressesAllTemporalChildChecks() {
        val temporalBlockers = resolve(temporal = TemporalState.Unconfigured).blockers.filter(::temporalBlocker)
        assertEquals(listOf(TruthBlocker.TemporalProviderUnconfigured), temporalBlockers)
    }

    @Test
    fun missingAndUnreviewedRankingNeverProduceAPrimaryRecommendation() {
        val missing = resolve(policy = null)
        val valid = validPolicy()
        val unreviewed = resolve(
            policy = valid.copy(
                exactWording = "changed",
                policyApproval = valid.policyApproval.copy(decision = ReviewDecision.REJECTED),
            ),
        )

        assertEquals(setOf(TruthBlocker.RankingMissing), missing.blockers)
        assertEquals(setOf(TruthBlocker.RankingUnreviewed), unreviewed.blockers)
        assertNull(missing.readyPresentation)
        assertNull(unreviewed.readyPresentation)
    }

    @Test
    fun rankingCandidatesMustBelongToTheSameApprovedRelease() {
        val policy = validPolicy(candidateIds = listOf("outside-release"))

        assertEquals(setOf(TruthBlocker.RankingUnreviewed), resolve(policy = policy).blockers)
    }

    @Test
    fun allApplicableBlockersAreReturnedInStableEnumOrder() {
        val release = validRelease().let { valid ->
            valid.copy(
                orderedClaims = listOf(valid.orderedClaims.single().copy(payload = "")),
                storedReleaseFingerprint = "wrong",
            )
        }
        val temporal = validTemporal(
            usePermission = PermissionState.UNKNOWN,
            persistencePermission = PermissionState.UNKNOWN,
        ).let { valid ->
            valid.copy(
                requestedKey = valid.requestedKey.copy(civilDate = valid.requestedKey.civilDate.plusDays(1)),
                sourceContract = valid.sourceContract.copy(termsReference = ""),
                snapshot = valid.snapshot.copy(dateBearingValues = emptyList()),
            )
        }
        val policy = validPolicy().copy(exactWording = "")

        val blockers = resolve(
            release = release,
            temporal = temporal,
            policy = policy,
            persistenceRequested = true,
        ).blockers.toList()

        assertEquals(blockers.sortedBy { it.ordinal }, blockers)
        assertEquals(
            listOf(
                TruthBlocker.ContentIncomplete,
                TruthBlocker.ContentApprovalMismatch,
                TruthBlocker.TemporalRightsUnknown,
                TruthBlocker.TemporalPersistenceRightsUnknown,
                TruthBlocker.TemporalContractReferenceMissing,
                TruthBlocker.TemporalKeyMismatch,
                TruthBlocker.TemporalIncomplete,
                TruthBlocker.RankingUnreviewed,
            ),
            blockers,
        )
    }

    @Test
    fun fingerprintsAreCanonicalAndCoverLockedPayloads() {
        val claim = validRelease().orderedClaims.single()
        assertEquals(
            TruthFingerprints.claim(claim),
            TruthFingerprints.claim(claim.copy(evidenceIds = claim.evidenceIds.reversed().toSet())),
        )
        assertNotEquals(TruthFingerprints.claim(claim), TruthFingerprints.claim(claim.copy(payload = "other")))

        val release = validRelease()
        assertNotEquals(
            TruthFingerprints.release(release),
            TruthFingerprints.release(release.copy(orderedClaims = release.orderedClaims + claim.copy(id = "act-2"))),
        )

        val policy = validPolicy()
        assertNotEquals(
            TruthFingerprints.ranking(policy),
            TruthFingerprints.ranking(policy.copy(scope = "other")),
        )
    }

    @Test
    fun resolutionHasNoPublicConstructor() {
        val constructors = TruthResolution::class.java.declaredConstructors
        assertTrue(constructors.any { Modifier.isPrivate(it.modifiers) })
        assertTrue(
            constructors.filter { Modifier.isPublic(it.modifiers) }.all { constructor ->
                constructor.parameterTypes.lastOrNull()?.name == "kotlin.jvm.internal.DefaultConstructorMarker"
            },
        )
    }

    private fun resolve(
        release: ContentRelease? = validRelease(),
        temporal: TemporalState = validTemporal(),
        policy: RankingPolicy? = validPolicy(),
        persistenceRequested: Boolean = false,
    ): TruthResolution = TruthResolution.resolve(
        TruthInputs(release, temporal, policy, persistenceRequested),
    )

    private fun validRelease(): ContentRelease {
        val claim = AtomicClaim(
            id = "act-1",
            type = ClaimType.ACT,
            payload = "reviewed payload",
            storedFingerprint = "",
            evidenceIds = setOf("evidence-1"),
        ).let { it.copy(storedFingerprint = TruthFingerprints.claim(it)) }
        val release = ContentRelease(
            id = "release-1",
            fixtureClass = FixtureClass.PRODUCTION,
            lifecycle = ContentLifecycle.PUBLISHED,
            orderedClaims = listOf(claim),
            evidenceRecords = listOf(
                EvidenceRecord("evidence-1", "controlled-source-reference", setOf(ClaimType.ACT)),
            ),
            claimApprovals = listOf(
                ClaimApproval(claim.id, claim.storedFingerprint, ReviewDecision.APPROVED, "reviewer-1"),
            ),
            storedReleaseFingerprint = "",
        )
        return release.withCanonicalReleaseFingerprint()
    }

    private fun ContentRelease.withCanonicalReleaseFingerprint(): ContentRelease =
        copy(storedReleaseFingerprint = TruthFingerprints.release(this))

    private fun validTemporal(
        usePermission: PermissionState = PermissionState.PERMITTED,
        persistencePermission: PermissionState = PermissionState.PERMITTED,
    ): TemporalState.Ready {
        val date = LocalDate.of(2031, 1, 15)
        val key = PrayerDayKey("ZONE-A", date)
        val contract = SourceContract(
            providerId = "provider-1",
            contractVersion = "contract-1",
            termsReference = "terms-1",
            usePermission = usePermission,
            persistencePermission = persistencePermission,
        )
        return TemporalState.Ready(
            requestedKey = key,
            sourceContract = contract,
            snapshot = PrayerDaySnapshot(
                exactKey = key,
                scheduleZoneId = "Region/City",
                dateBearingValues = PrayerSlot.entries.mapIndexed { index, slot ->
                    DateBearingPrayerValue(slot, date, LocalTime.of(5 + index * 3, index))
                },
                provenance = TemporalProvenance(
                    providerId = contract.providerId,
                    contractVersion = contract.contractVersion,
                    sourceReference = "source-1",
                ),
            ),
        )
    }

    private fun validPolicy(candidateIds: List<String> = listOf("act-1")): RankingPolicy {
        val policy = RankingPolicy(
            orderedCandidateIds = candidateIds,
            exactWording = "reviewed wording",
            scope = "scope-1",
            storedPolicyFingerprint = "",
            policyApproval = PolicyApproval("", ReviewDecision.APPROVED, "reviewer-2"),
        )
        val fingerprint = TruthFingerprints.ranking(policy)
        return policy.copy(
            storedPolicyFingerprint = fingerprint,
            policyApproval = policy.policyApproval.copy(reviewedPolicyFingerprint = fingerprint),
        )
    }

    private fun contentBlocker(blocker: TruthBlocker): Boolean =
        blocker.ordinal in TruthBlocker.ContentMissing.ordinal..TruthBlocker.ContentWithdrawn.ordinal

    private fun temporalBlocker(blocker: TruthBlocker): Boolean =
        blocker.ordinal in TruthBlocker.TemporalProviderUnconfigured.ordinal..TruthBlocker.TemporalIncomplete.ordinal
}
