package com.bmuslim.app.truth

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import java.util.Collections

enum class FixtureClass { SYNTHETIC, PRODUCTION }

enum class ContentLifecycle { DRAFT, IN_REVIEW, PUBLISHED, WITHDRAWN }

enum class ClaimType { ACT, TEXT, TRANSLATION, TIMING, RULING, VIRTUE, RANKING }

enum class ReviewDecision { APPROVED, REJECTED }

data class AtomicClaim(
    val id: String,
    val type: ClaimType,
    val payload: String,
    val storedFingerprint: String,
    val evidenceIds: Set<String>,
)

data class EvidenceRecord(
    val id: String,
    val sourceReference: String,
    val supportedClaimTypes: Set<ClaimType>,
)

data class ClaimApproval(
    val claimId: String,
    val reviewedFingerprint: String,
    val decision: ReviewDecision,
    val reviewerId: String,
)

data class ContentRelease(
    val id: String,
    val fixtureClass: FixtureClass,
    val lifecycle: ContentLifecycle,
    val orderedClaims: List<AtomicClaim>,
    val evidenceRecords: List<EvidenceRecord>,
    val claimApprovals: List<ClaimApproval>,
    val storedReleaseFingerprint: String,
)

enum class PermissionState { PERMITTED, DENIED, UNKNOWN }

data class SourceContract(
    val providerId: String,
    val contractVersion: String,
    val termsReference: String,
    val usePermission: PermissionState,
    val persistencePermission: PermissionState,
)

data class PrayerDayKey(
    val zoneCode: String,
    val civilDate: LocalDate,
)

enum class PrayerSlot { DAWN, NOON, AFTERNOON, SUNSET, NIGHT }

data class DateBearingPrayerValue(
    val slot: PrayerSlot,
    val civilDate: LocalDate,
    val time: LocalTime,
)

data class TemporalProvenance(
    val providerId: String,
    val contractVersion: String,
    val sourceReference: String,
)

data class PrayerDaySnapshot(
    val exactKey: PrayerDayKey,
    val scheduleZoneId: String,
    val dateBearingValues: List<DateBearingPrayerValue>,
    val provenance: TemporalProvenance,
)

sealed interface TemporalState {
    data object Unconfigured : TemporalState

    data class Ready(
        val requestedKey: PrayerDayKey,
        val snapshot: PrayerDaySnapshot,
        val sourceContract: SourceContract,
    ) : TemporalState
}

data class PolicyApproval(
    val reviewedPolicyFingerprint: String,
    val decision: ReviewDecision,
    val reviewerId: String,
)

data class RankingPolicy(
    val orderedCandidateIds: List<String>,
    val exactWording: String,
    val scope: String,
    val storedPolicyFingerprint: String,
    val policyApproval: PolicyApproval,
)

data class TruthInputs(
    val contentRelease: ContentRelease?,
    val temporalState: TemporalState,
    val rankingPolicy: RankingPolicy?,
    val persistenceRequested: Boolean = false,
)

enum class TruthBlocker {
    ContentMissing,
    SyntheticContentForbidden,
    ContentNotPublished,
    ContentIncomplete,
    ContentApprovalMismatch,
    ContentWithdrawn,
    TemporalProviderUnconfigured,
    TemporalRightsUnknown,
    TemporalUseDenied,
    TemporalPersistenceRightsUnknown,
    TemporalPersistenceDenied,
    TemporalContractReferenceMissing,
    TemporalKeyMismatch,
    TemporalIncomplete,
    RankingMissing,
    RankingUnreviewed,
}

data class ReadyPresentation(
    val contentReleaseId: String,
    val prayerDayKey: PrayerDayKey,
    val primaryRecommendationId: String,
    val exactWording: String,
)

class TruthResolution private constructor(
    val readyPresentation: ReadyPresentation?,
    val blockers: Set<TruthBlocker>,
) {
    val isReady: Boolean get() = readyPresentation != null

    companion object {
        fun resolve(inputs: TruthInputs): TruthResolution {
            val discovered = mutableSetOf<TruthBlocker>()
            validateContent(inputs.contentRelease, discovered)
            validateTemporal(inputs.temporalState, inputs.persistenceRequested, discovered)
            validateRanking(inputs.rankingPolicy, inputs.contentRelease, discovered)

            val ordered = TruthBlocker.entries.filterTo(linkedSetOf()) { it in discovered }
            if (ordered.isNotEmpty()) {
                return TruthResolution(
                    readyPresentation = null,
                    blockers = Collections.unmodifiableSet(ordered),
                )
            }

            val release = requireNotNull(inputs.contentRelease)
            val temporal = inputs.temporalState as TemporalState.Ready
            val ranking = requireNotNull(inputs.rankingPolicy)
            return TruthResolution(
                readyPresentation = ReadyPresentation(
                    contentReleaseId = release.id,
                    prayerDayKey = temporal.requestedKey,
                    primaryRecommendationId = ranking.orderedCandidateIds.first(),
                    exactWording = ranking.exactWording,
                ),
                blockers = emptySet(),
            )
        }

        private fun validateContent(
            release: ContentRelease?,
            blockers: MutableSet<TruthBlocker>,
        ) {
            if (release == null) {
                blockers += TruthBlocker.ContentMissing
                return
            }
            if (release.fixtureClass == FixtureClass.SYNTHETIC) {
                blockers += TruthBlocker.SyntheticContentForbidden
                return
            }
            when (release.lifecycle) {
                ContentLifecycle.WITHDRAWN -> {
                    blockers += TruthBlocker.ContentWithdrawn
                    return
                }
                ContentLifecycle.DRAFT, ContentLifecycle.IN_REVIEW -> {
                    blockers += TruthBlocker.ContentNotPublished
                    return
                }
                ContentLifecycle.PUBLISHED -> Unit
            }

            val claimIds = release.orderedClaims.map { it.id }
            val evidenceById = release.evidenceRecords.associateBy { it.id }
            val approvalsByClaim = release.claimApprovals.groupBy { it.claimId }
            val incomplete = release.id.isBlank() ||
                release.orderedClaims.isEmpty() ||
                claimIds.any(String::isBlank) ||
                claimIds.distinct().size != claimIds.size ||
                release.evidenceRecords.any {
                    it.id.isBlank() || it.sourceReference.isBlank() || it.supportedClaimTypes.isEmpty()
                } ||
                evidenceById.size != release.evidenceRecords.size ||
                release.claimApprovals.any { it.claimId.isBlank() || it.reviewerId.isBlank() } ||
                release.claimApprovals.map { it.claimId }.distinct().size != release.claimApprovals.size ||
                release.orderedClaims.any { claim ->
                    claim.payload.isBlank() ||
                        claim.evidenceIds.isEmpty() ||
                        claim.evidenceIds.any(String::isBlank) ||
                        claim.evidenceIds.any { evidenceId ->
                            evidenceById[evidenceId]?.supportedClaimTypes?.contains(claim.type) != true
                        }
                }
            if (incomplete) blockers += TruthBlocker.ContentIncomplete

            val approvalMismatch = release.orderedClaims.any { claim ->
                val canonical = TruthFingerprints.claim(claim)
                val approvals = approvalsByClaim[claim.id].orEmpty()
                claim.storedFingerprint != canonical ||
                    approvals.size != 1 ||
                    approvals.singleOrNull()?.let { approval ->
                        approval.decision != ReviewDecision.APPROVED ||
                            approval.reviewedFingerprint != canonical
                    } != false
            } || release.storedReleaseFingerprint != TruthFingerprints.release(release)
            if (approvalMismatch) blockers += TruthBlocker.ContentApprovalMismatch
        }

        private fun validateTemporal(
            temporalState: TemporalState,
            persistenceRequested: Boolean,
            blockers: MutableSet<TruthBlocker>,
        ) {
            if (temporalState is TemporalState.Unconfigured) {
                blockers += TruthBlocker.TemporalProviderUnconfigured
                return
            }
            temporalState as TemporalState.Ready
            val contract = temporalState.sourceContract
            when (contract.usePermission) {
                PermissionState.UNKNOWN -> blockers += TruthBlocker.TemporalRightsUnknown
                PermissionState.DENIED -> blockers += TruthBlocker.TemporalUseDenied
                PermissionState.PERMITTED -> Unit
            }
            if (persistenceRequested) {
                when (contract.persistencePermission) {
                    PermissionState.UNKNOWN -> blockers += TruthBlocker.TemporalPersistenceRightsUnknown
                    PermissionState.DENIED -> blockers += TruthBlocker.TemporalPersistenceDenied
                    PermissionState.PERMITTED -> Unit
                }
            }
            if (contract.termsReference.isBlank()) {
                blockers += TruthBlocker.TemporalContractReferenceMissing
            }

            val snapshot = temporalState.snapshot
            if (snapshot.exactKey != temporalState.requestedKey) {
                blockers += TruthBlocker.TemporalKeyMismatch
            }
            val values = snapshot.dateBearingValues
            val valuesBySlot = values.associateBy { it.slot }
            val completeSlots = valuesBySlot.keys == PrayerSlot.entries.toSet() &&
                values.size == PrayerSlot.entries.size
            val canonicalValues = PrayerSlot.entries.mapNotNull(valuesBySlot::get)
            val chronological = canonicalValues.zipWithNext().all { (earlier, later) ->
                earlier.time < later.time
            }
            val incomplete = temporalState.requestedKey.zoneCode.isBlank() ||
                snapshot.exactKey.zoneCode.isBlank() ||
                snapshot.scheduleZoneId.isBlank() ||
                contract.providerId.isBlank() ||
                contract.contractVersion.isBlank() ||
                snapshot.provenance.providerId.isBlank() ||
                snapshot.provenance.contractVersion.isBlank() ||
                snapshot.provenance.sourceReference.isBlank() ||
                snapshot.provenance.providerId != contract.providerId ||
                snapshot.provenance.contractVersion != contract.contractVersion ||
                !completeSlots ||
                values.any { it.civilDate != snapshot.exactKey.civilDate } ||
                !chronological
            if (incomplete) blockers += TruthBlocker.TemporalIncomplete
        }

        private fun validateRanking(
            policy: RankingPolicy?,
            release: ContentRelease?,
            blockers: MutableSet<TruthBlocker>,
        ) {
            if (policy == null) {
                blockers += TruthBlocker.RankingMissing
                return
            }
            val canonical = TruthFingerprints.ranking(policy)
            val releaseClaimIds = release?.orderedClaims?.mapTo(mutableSetOf()) { it.id }.orEmpty()
            val invalid = policy.orderedCandidateIds.isEmpty() ||
                policy.orderedCandidateIds.any(String::isBlank) ||
                policy.orderedCandidateIds.distinct().size != policy.orderedCandidateIds.size ||
                policy.orderedCandidateIds.any { it !in releaseClaimIds } ||
                policy.exactWording.isBlank() ||
                policy.scope.isBlank() ||
                policy.storedPolicyFingerprint != canonical ||
                policy.policyApproval.decision != ReviewDecision.APPROVED ||
                policy.policyApproval.reviewerId.isBlank() ||
                policy.policyApproval.reviewedPolicyFingerprint != canonical
            if (invalid) blockers += TruthBlocker.RankingUnreviewed
        }
    }
}

object TruthFingerprints {
    fun claim(claim: AtomicClaim): String = CanonicalHash()
        .field("claim-v1")
        .field(claim.id)
        .field(claim.type.name)
        .field(claim.payload)
        .fields(claim.evidenceIds.sorted())
        .finish()

    fun release(release: ContentRelease): String {
        val hash = CanonicalHash()
            .field("release-v1")
            .field(release.id)
            .field(release.fixtureClass.name)
            .field(release.lifecycle.name)
            .field(release.orderedClaims.size.toString())
        release.orderedClaims.forEach { hash.field(claim(it)) }
        val evidence = release.evidenceRecords.sortedBy { it.id }
        hash.field(evidence.size.toString())
        evidence.forEach { record ->
            hash.field(record.id)
                .field(record.sourceReference)
                .fields(record.supportedClaimTypes.map { it.name }.sorted())
        }
        return hash.finish()
    }

    fun ranking(policy: RankingPolicy): String = CanonicalHash()
        .field("ranking-v1")
        .fields(policy.orderedCandidateIds)
        .field(policy.exactWording)
        .field(policy.scope)
        .finish()
}

private class CanonicalHash {
    private val digest = MessageDigest.getInstance("SHA-256")

    fun field(value: String): CanonicalHash = apply {
        val bytes = value.toByteArray(Charsets.UTF_8)
        digest.update(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(bytes.size).array())
        digest.update(bytes)
    }

    fun fields(values: List<String>): CanonicalHash = apply {
        field(values.size.toString())
        values.forEach(::field)
    }

    fun finish(): String = digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
}
