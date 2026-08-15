package com.bmuslim.app.research

internal enum class ResearchCadence { DAILY_CONTEXT, WEEKLY_CONTEXT, MONTHLY_CONTEXT }
internal enum class ResearchEvidenceRole { PRIMARY, CORROBORATIVE, VARIATION }

internal data class ResearchAssessment(
    val kind: String,
    val label: String,
    val grader: String?,
)

internal data class ResearchEvidence(
    val id: String,
    val collection: String,
    val reference: String,
    val canonicalUrl: String,
    val assessment: ResearchAssessment,
    val supportsClaimTypes: Set<String>,
    val role: ResearchEvidenceRole = ResearchEvidenceRole.PRIMARY,
)

internal data class ResearchDeed(
    val id: String,
    val cadence: ResearchCadence,
    val title: String,
    val instruction: String,
    val sourceTimePhrase: String,
    val resolutionStatus: String,
    val unsupportedInferences: List<String>,
    val recurrenceKind: String,
    val recurrenceWeekdays: List<String>,
    val expressionKind: String,
    val expressionPrecision: String,
    val expressionEvent: String?,
    val semanticPeriods: List<String>,
    val prayerScope: String? = null,
    val lunarDateSet: List<Int> = emptyList(),
    val temporalPolicyRequirement: String? = null,
    val evidence: List<ResearchEvidence>,
    val actionVariationEvidence: List<ResearchEvidence> = emptyList(),
    val timingVariationEvidence: List<ResearchEvidence> = emptyList(),
) {
    val variationEvidence: List<ResearchEvidence>
        get() = actionVariationEvidence + timingVariationEvidence
}

internal data class ResearchCatalog(
    val catalogId: String,
    val contentVersion: String,
    val lifecycle: String,
    val deeds: List<ResearchDeed>,
    val evidenceCount: Int,
) {
    val dailyDeeds: List<ResearchDeed> get() = deeds.filter { it.cadence == ResearchCadence.DAILY_CONTEXT }
    val weeklyDeeds: List<ResearchDeed> get() = deeds.filter { it.cadence == ResearchCadence.WEEKLY_CONTEXT }
    val monthlyDeeds: List<ResearchDeed> get() = deeds.filter { it.cadence == ResearchCadence.MONTHLY_CONTEXT }
}

internal sealed interface ResearchCatalogResult {
    data class Ready(val catalog: ResearchCatalog) : ResearchCatalogResult
    data object Rejected : ResearchCatalogResult
}
