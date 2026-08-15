package com.bmuslim.app.research

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.IOException
import java.io.StringReader
import java.math.BigDecimal
import java.net.URI

internal class ResearchCatalogParser {
    fun parse(body: String): ResearchCatalogResult = try {
        val rootElement = JsonReader(StringReader(body)).use { reader ->
            reader.strictness = Strictness.STRICT
            val element = reader.readUniqueElement()
            requireCatalog(reader.peek() == JsonToken.END_DOCUMENT)
            element
        }
        ResearchCatalogResult.Ready(parseCatalog(rootElement))
    } catch (_: CatalogRejected) {
        ResearchCatalogResult.Rejected
    } catch (_: IOException) {
        ResearchCatalogResult.Rejected
    } catch (_: RuntimeException) {
        ResearchCatalogResult.Rejected
    }

    private fun parseCatalog(element: JsonElement): ResearchCatalog {
        val root = element.exactObject(ROOT_FIELDS)
        requireCatalog(root.requiredInt("schemaVersion") == 2)
        requireCatalog(root.requiredString("\$schema") == "./catalog.schema.json")
        requireCatalog(root.requiredString("catalogId") == CATALOG_ID)
        requireCatalog(root.requiredString("contentVersion") == CONTENT_VERSION)
        requireCatalog(root.requiredString("purpose") == "RESEARCH_CANDIDATE")
        requireCatalog(root.requiredString("lifecycle") == "IN_REVIEW")
        requireCatalog(root.requiredString("defaultLocale") == "ms-MY")
        requireCatalog(root.requiredStringList("supportedLocales") == listOf("ms-MY", "en-GB"))
        validateAdmissionPolicy(root.required("researchAdmissionPolicy"))
        requireCatalog(root.requiredArray("approvals").size() == 0)

        val evidenceById = linkedMapOf<String, ResearchEvidence>()
        root.requiredArray("evidence").forEach { evidenceElement ->
            val evidence = parseEvidence(evidenceElement)
            requireCatalog(evidenceById.put(evidence.id, evidence) == null)
        }
        requireCatalog(evidenceById.size == 14)

        val seenEntryIds = mutableSetOf<String>()
        val seenClaimIds = mutableSetOf<String>()
        val referencedEvidence = mutableSetOf<String>()
        val entries = root.requiredArray("entries").map { entryElement ->
            parseEntry(entryElement, evidenceById, seenClaimIds, referencedEvidence).also {
                requireCatalog(seenEntryIds.add(it.id))
            }
        }
        requireCatalog(entries.map(ResearchDeed::id) == EXPECTED_ENTRY_IDS)
        requireCatalog(entries.count { it.cadence == ResearchCadence.DAILY_CONTEXT } == 7)
        requireCatalog(entries.count { it.cadence == ResearchCadence.WEEKLY_CONTEXT } == 3)
        requireCatalog(entries.count { it.cadence == ResearchCadence.MONTHLY_CONTEXT } == 1)
        requireCatalog(referencedEvidence == evidenceById.keys)

        return ResearchCatalog(
            catalogId = CATALOG_ID,
            contentVersion = CONTENT_VERSION,
            lifecycle = "IN_REVIEW",
            deeds = entries,
            evidenceCount = evidenceById.size,
        )
    }

    private fun validateAdmissionPolicy(element: JsonElement) {
        val policy = element.exactObject(ADMISSION_FIELDS)
        requireCatalog(
            policy.requiredStringList("acceptedCandidateAssessments") == listOf(
                "SAHIH_COLLECTION",
                "DISPLAYED_SAHIH_WITH_NAMED_GRADER",
                "DISPLAYED_HASAN_WITH_NAMED_GRADER",
            ),
        )
        requireCatalog(policy.requiredBoolean("publicationReviewRequired"))
    }

    private fun parseEvidence(element: JsonElement): ResearchEvidence {
        val value = element.exactObject(EVIDENCE_FIELDS)
        val id = value.requiredString("id")
        requireCatalog(id.startsWith("evidence."))
        requireCatalog(value.requiredString("sourceType") == "HADITH")
        requireCatalog(value.requiredString("attributionLevel") == "PROPHETIC")
        requireCatalog(value.requiredString("usagePermission") == "LINK_AND_PARAPHRASE_ONLY")
        val collection = value.requiredString("collection")
        val reference = value.requiredString("reference")
        val sourceSlug = SOURCE_SLUGS[collection] ?: rejectCatalog()
        requireCatalog(id == "evidence.$sourceSlug.$reference")
        val url = value.requiredString("canonicalUrl")
        requireCatalog(url.isCanonicalEvidenceUrl(sourceSlug, reference))
        val supportList = value.requiredStringList("supportsClaimTypes")
        requireCatalog(supportList.distinct().size == supportList.size)
        val supports = supportList.toSet()
        requireCatalog(supports.isNotEmpty() && supports.all { it in CLAIM_TYPES })
        value.requiredString("sourceSummary")

        val assessmentObject = value.required("reportedAssessment").exactObject(ASSESSMENT_FIELDS)
        val kind = assessmentObject.requiredString("kind")
        val label = assessmentObject.requiredString("label")
        val grader = assessmentObject.optionalString("grader")
        when (kind) {
            "DISPLAYED_GRADE" -> {
                requireCatalog(label in setOf("Sahih", "Hasan"))
                requireCatalog(!grader.isNullOrBlank())
            }
            "SAHIH_COLLECTION" -> requireCatalog(grader == null)
            else -> rejectCatalog()
        }
        return ResearchEvidence(
            id = id,
            collection = collection,
            reference = reference,
            canonicalUrl = url,
            assessment = ResearchAssessment(kind, label, grader),
            supportsClaimTypes = supports,
        )
    }

    private fun parseEntry(
        element: JsonElement,
        evidenceById: Map<String, ResearchEvidence>,
        seenClaimIds: MutableSet<String>,
        referencedEvidence: MutableSet<String>,
    ): ResearchDeed {
        val value = element.exactObject(ENTRY_FIELDS)
        val id = value.requiredString("id")
        val cadence = when {
            id.startsWith("deed.daily.") -> ResearchCadence.DAILY_CONTEXT
            id.startsWith("deed.weekly.") -> ResearchCadence.WEEKLY_CONTEXT
            id.startsWith("deed.monthly.") -> ResearchCadence.MONTHLY_CONTEXT
            else -> rejectCatalog()
        }
        requireCatalog(value.requiredString("lifecycle") == "IN_REVIEW")
        requireCatalog(value.requiredString("practiceKind") in PRACTICE_KINDS)
        requireCatalog(value.requiredString("completionMode") in COMPLETION_MODES)

        val action = parseClaim(value.required("actionClaim"), "ACT", seenClaimIds)
        val timing = parseClaim(value.required("timingClaim"), "TIMING", seenClaimIds)
        requireCatalog(action.id.startsWith("claim.${id.removePrefix("deed.")}."))
        requireCatalog(timing.id.startsWith("claim.${id.removePrefix("deed.")}."))
        val actionPayload = parseActionPayload(action.payload)
        val timingPayload = parseTimingPayload(timing.payload)

        validateEvidenceLinks(action, "ACT", evidenceById, referencedEvidence)
        validateEvidenceLinks(timing, "TIMING", evidenceById, referencedEvidence)
        validateEvidenceIds(action.variationEvidenceIds, "ACT", evidenceById, referencedEvidence)
        validateEvidenceIds(timing.variationEvidenceIds, "TIMING", evidenceById, referencedEvidence)
        val primaryIds = (action.evidenceIds + timing.evidenceIds).toSet()
        val variationIds = (action.variationEvidenceIds + timing.variationEvidenceIds).toSet()
        val orderedEvidenceIds = (
            action.evidenceIds + action.corroborativeEvidenceIds +
                action.variationEvidenceIds + timing.evidenceIds + timing.corroborativeEvidenceIds +
                timing.variationEvidenceIds
            ).distinct()
        val evidence = orderedEvidenceIds.map { evidenceId ->
            requireNotNull(evidenceById[evidenceId]).copy(
                role = when {
                    evidenceId in variationIds -> ResearchEvidenceRole.VARIATION
                    evidenceId in primaryIds -> ResearchEvidenceRole.PRIMARY
                    else -> ResearchEvidenceRole.CORROBORATIVE
                },
            )
        }
        val actionVariationEvidence = action.variationEvidenceIds.map { evidenceId ->
            requireNotNull(evidenceById[evidenceId]).copy(role = ResearchEvidenceRole.VARIATION)
        }
        val timingVariationEvidence = timing.variationEvidenceIds.map { evidenceId ->
            requireNotNull(evidenceById[evidenceId]).copy(role = ResearchEvidenceRole.VARIATION)
        }

        return ResearchDeed(
            id = id,
            cadence = cadence,
            title = actionPayload.title,
            instruction = actionPayload.instruction,
            sourceTimePhrase = timingPayload.sourceTimePhrase,
            resolutionStatus = timingPayload.resolutionStatus,
            unsupportedInferences = timingPayload.unsupportedInferences,
            recurrenceKind = timingPayload.recurrence.kind,
            recurrenceWeekdays = timingPayload.recurrence.weekdays,
            expressionKind = timingPayload.expression.kind,
            expressionPrecision = timingPayload.expression.precision,
            expressionEvent = timingPayload.expression.event,
            semanticPeriods = timingPayload.expression.periods,
            prayerScope = timingPayload.expression.prayerScope,
            lunarDateSet = timingPayload.expression.lunarDateSet,
            temporalPolicyRequirement = timingPayload.expression.policyRequirement,
            evidence = evidence,
            actionVariationEvidence = actionVariationEvidence,
            timingVariationEvidence = timingVariationEvidence,
        )
    }

    private fun parseClaim(
        element: JsonElement,
        expectedType: String,
        seenClaimIds: MutableSet<String>,
    ): ParsedClaim {
        val value = element.exactObject(CLAIM_REQUIRED_FIELDS, CLAIM_OPTIONAL_FIELDS)
        val id = value.requiredString("id")
        requireCatalog(id.startsWith("claim.") && seenClaimIds.add(id))
        requireCatalog(value.requiredString("claimType") == expectedType)
        requireCatalog(value.requiredString("reviewStatus") == "UNREVIEWED")
        val evidenceIds = value.requiredStringList("evidenceIds")
        val corroborative = value.optionalStringList("corroborativeEvidenceIds")
        val variation = value.optionalStringList("variationEvidenceIds")
        requireCatalog(evidenceIds.isNotEmpty() && evidenceIds.distinct().size == evidenceIds.size)
        requireCatalog(corroborative.distinct().size == corroborative.size)
        requireCatalog(variation.distinct().size == variation.size)
        requireCatalog(evidenceIds.intersect(corroborative.toSet()).isEmpty())
        requireCatalog(evidenceIds.intersect(variation.toSet()).isEmpty())
        requireCatalog(corroborative.intersect(variation.toSet()).isEmpty())
        return ParsedClaim(id, evidenceIds, corroborative, variation, value.required("payload"))
    }

    private fun parseActionPayload(element: JsonElement): ActionPayload {
        val value = element.exactObject(ACTION_REQUIRED_FIELDS, ACTION_OPTIONAL_FIELDS)
        val detailRequirement = value.optionalString("detailRequirement")
        if (detailRequirement != null) {
            requireCatalog(detailRequirement == "REQUIRES_APPROVED_TEXT_CLAIM")
        }
        return ActionPayload(
            title = value.required("title").malayText(),
            instruction = value.required("instruction").malayText(),
        )
    }

    private fun parseTimingPayload(element: JsonElement): TimingPayload {
        val value = element.exactObject(TIMING_FIELDS)
        val recurrence = parseRecurrence(value.required("recurrence"))
        val expression = parseExpression(value.required("expression"))
        val expressionMatches = if (recurrence.kind == "WEEKDAY_SET") {
            expression.kind in WEEKDAY_EXPRESSIONS
        } else {
            RECURRENCE_EXPRESSION_PAIRS[recurrence.kind] == expression.kind
        }
        requireCatalog(expressionMatches)
        if (recurrence.kind == "ON_PERSONAL_EVENT") {
            requireCatalog(recurrence.event == expression.event)
        }
        when (recurrence.kind) {
            "TWICE_DAILY_NAMED_PERIODS" ->
                requireCatalog(expression.periods == listOf("MORNING", "EVENING"))
            "SOURCE_RECURRENCE_UNSPECIFIED" ->
                requireCatalog(expression.periods == listOf("FORENOON"))
            "MONTHLY_CONTEXT" -> requireCatalog(expression.lunarDateSet == listOf(13, 14, 15))
        }
        when (expression.kind) {
            "FASTING_WEEKDAYS" -> {
                requireCatalog(recurrence.weekdays == listOf("MONDAY", "THURSDAY"))
                requireCatalog(recurrence.dateBasis == "SCHEDULE_ZONE_LOCAL_WEEKDAY")
            }
            "RELIGIOUS_WEEKDAY", "WEEKDAY_FINAL_PERIOD_AFTER_PRAYER" -> {
                requireCatalog(recurrence.weekdays == listOf("FRIDAY"))
                requireCatalog(recurrence.dateBasis == "RELIGIOUS_DAY_POLICY_REQUIRED")
            }
            "FASTING_LUNAR_DATES" -> {
                requireCatalog(recurrence.kind == "MONTHLY_CONTEXT")
                requireCatalog(recurrence.dateBasis == "RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED")
                requireCatalog(expression.lunarDateSet == listOf(13, 14, 15))
                requireCatalog(expression.policyRequirement == "REQUIRES_REVIEWED_CALENDAR_AND_ELIGIBILITY_POLICY")
            }
        }
        val status = value.requiredString("resolutionStatus")
        val expectedStatus = when {
            expression.kind == "FASTING_LUNAR_DATES" -> "REQUIRES_REVIEWED_CALENDAR_AND_ELIGIBILITY_POLICY"
            expression.precision == "DIRECT_TRIGGER" -> "DIRECT_EVENT_ONLY"
            else -> "REQUIRES_REVIEWED_TEMPORAL_POLICY"
        }
        requireCatalog(status == expectedStatus)
        val unsupported = value.requiredStringList("unsupportedInferences")
        requireCatalog(unsupported.isNotEmpty())
        return TimingPayload(
            sourceTimePhrase = value.required("sourceTimePhrase").malayText(),
            resolutionStatus = status,
            unsupportedInferences = unsupported,
            recurrence = recurrence,
            expression = expression,
        )
    }

    private fun parseRecurrence(element: JsonElement): RecurrencePayload {
        val initial = element.asRequiredObject()
        val kind = initial.requiredString("kind")
        val fields = when (kind) {
            "TWICE_DAILY_NAMED_PERIODS", "AFTER_EACH_PRAYER", "SOURCE_RECURRENCE_UNSPECIFIED", "EVERY_DAY", "MONTHLY_CONTEXT" ->
                setOf("kind", "dateBasis")
            "ON_PERSONAL_EVENT" -> setOf("kind", "event", "dateBasis")
            "WEEKDAY_SET" -> setOf("kind", "weekdays", "dateBasis")
            else -> rejectCatalog()
        }
        val value = element.exactObject(fields)
        when (kind) {
            "TWICE_DAILY_NAMED_PERIODS" ->
                requireCatalog(value.requiredString("dateBasis") == "SCHEDULE_ZONE_LOCAL_DATE")
            "AFTER_EACH_PRAYER" -> requireCatalog(value.requiredString("dateBasis") == "PERSONAL_EVENT_OCCURRENCE")
            "ON_PERSONAL_EVENT" -> {
                requireCatalog(value.requiredString("dateBasis") == "PERSONAL_EVENT_OCCURRENCE")
                requireCatalog(value.requiredString("event") in setOf("INTEND_SLEEP", "INTEND_SLEEP_AT_NIGHT"))
            }
            "SOURCE_RECURRENCE_UNSPECIFIED" ->
                requireCatalog(value.requiredString("dateBasis") == "SOURCE_RECURRENCE_UNSPECIFIED")
            "EVERY_DAY" -> requireCatalog(value.requiredString("dateBasis") == "NIGHT_OCCURRENCE")
            "MONTHLY_CONTEXT" -> requireCatalog(value.requiredString("dateBasis") == "RELIGIOUS_LUNAR_DATE_POLICY_REQUIRED")
            "WEEKDAY_SET" -> {
                val weekdays = value.requiredStringList("weekdays")
                requireCatalog(weekdays.isNotEmpty() && weekdays.distinct().size == weekdays.size)
                requireCatalog(weekdays.all { it in WEEKDAYS })
                requireCatalog(value.requiredString("dateBasis") in setOf("SCHEDULE_ZONE_LOCAL_WEEKDAY", "RELIGIOUS_DAY_POLICY_REQUIRED"))
            }
        }
        return RecurrencePayload(
            kind = kind,
            dateBasis = value.requiredString("dateBasis"),
            weekdays = if (kind == "WEEKDAY_SET") value.requiredStringList("weekdays") else emptyList(),
            event = if (kind == "ON_PERSONAL_EVENT") value.requiredString("event") else null,
        )
    }

    private fun parseExpression(element: JsonElement): ExpressionPayload {
        val value = element.exactObject(EXPRESSION_FIELDS)
        val kind = value.requiredString("kind")
        val parameters = value.required("parameters")
        when (kind) {
            "SEMANTIC_PERIODS" -> {
                val params = parameters.exactObject(setOf("periods"))
                val periods = params.requiredStringList("periods")
                requireCatalog(periods.isNotEmpty() && periods.distinct().size == periods.size)
                requireCatalog(periods.all { it in setOf("MORNING", "EVENING", "FORENOON") })
            }
            "AFTER_USER_EVENT" -> {
                val params = parameters.exactObject(setOf("event", "prayerScope"))
                requireCatalog(params.requiredString("event") == "PRAYER_COMPLETED")
                requireCatalog(params.requiredString("prayerScope") == "PRESCRIBED_ONLY")
            }
            "PERSONAL_EVENT" -> {
                val params = parameters.exactObject(setOf("relation", "event"))
                requireCatalog(params.requiredString("relation") in setOf("AT", "BEFORE"))
                requireCatalog(params.requiredString("event") in setOf("INTEND_SLEEP", "INTEND_SLEEP_AT_NIGHT"))
            }
            "NIGHT_SEGMENT" -> {
                val params = parameters.exactObject(setOf("segment", "nightBoundaryPolicy"))
                requireCatalog(params.requiredString("segment") == "LAST_THIRD")
                requireCatalog(params.requiredString("nightBoundaryPolicy") == "REQUIRES_MALAYSIAN_SCHOLAR_REVIEW")
            }
            "FASTING_WEEKDAYS" -> {
                val weekdays = parameters.exactObject(setOf("weekdays")).requiredStringList("weekdays")
                requireCatalog(weekdays == listOf("MONDAY", "THURSDAY"))
            }
            "RELIGIOUS_WEEKDAY" ->
                requireCatalog(parameters.exactObject(setOf("weekday")).requiredString("weekday") == "FRIDAY")
            "WEEKDAY_FINAL_PERIOD_AFTER_PRAYER" -> {
                val params = parameters.exactObject(setOf("weekday", "after", "focus", "endBoundaryPolicy"))
                requireCatalog(params.requiredString("weekday") == "FRIDAY")
                requireCatalog(params.requiredString("after") == "ASR")
                requireCatalog(params.requiredString("focus") == "FINAL_PORTION")
                requireCatalog(params.requiredString("endBoundaryPolicy") == "UNRESOLVED")
            }
            "FASTING_LUNAR_DATES" -> {
                val params = parameters.exactObject(setOf("dateSet", "eligibilityPolicy"))
                val dateSet = params.required("dateSet").exactObject(setOf("kind", "dates"))
                requireCatalog(dateSet.requiredString("kind") == "LUNAR_DATE_SET")
                requireCatalog(dateSet.requiredIntList("dates") == listOf(13, 14, 15))
                requireCatalog(params.requiredString("eligibilityPolicy") == "REQUIRES_REVIEWED_CALENDAR_AND_ELIGIBILITY_POLICY")
            }
            else -> rejectCatalog()
        }
        val precision = value.requiredString("precision")
        requireCatalog(precision == EXPRESSION_PRECISION[kind])
        return ExpressionPayload(
            kind = kind,
            precision = precision,
            event = if (kind == "PERSONAL_EVENT") {
                parameters.asJsonObject.requiredString("event")
            } else {
                null
            },
            periods = if (kind == "SEMANTIC_PERIODS") {
                parameters.asJsonObject.requiredStringList("periods")
            } else {
                emptyList()
            },
            prayerScope = if (kind == "AFTER_USER_EVENT") {
                parameters.asJsonObject.requiredString("prayerScope")
            } else null,
            lunarDateSet = if (kind == "FASTING_LUNAR_DATES") {
                parameters.asJsonObject.required("dateSet").asJsonObject.requiredIntList("dates")
            } else emptyList(),
            policyRequirement = if (kind == "FASTING_LUNAR_DATES") {
                parameters.asJsonObject.requiredString("eligibilityPolicy")
            } else null,
        )
    }

    private fun validateEvidenceLinks(
        claim: ParsedClaim,
        claimType: String,
        evidenceById: Map<String, ResearchEvidence>,
        referencedEvidence: MutableSet<String>,
    ) {
        validateEvidenceIds(claim.evidenceIds + claim.corroborativeEvidenceIds, claimType, evidenceById, referencedEvidence)
    }

    private fun validateEvidenceIds(
        ids: List<String>,
        claimType: String,
        evidenceById: Map<String, ResearchEvidence>,
        referencedEvidence: MutableSet<String>,
    ) {
        ids.forEach { evidenceId ->
            val evidence = evidenceById[evidenceId] ?: rejectCatalog()
            requireCatalog(claimType in evidence.supportsClaimTypes)
            referencedEvidence += evidenceId
        }
    }

    private fun JsonReader.readUniqueElement(): JsonElement = when (peek()) {
        JsonToken.BEGIN_OBJECT -> JsonObject().also { objectValue ->
            val seen = mutableSetOf<String>()
            beginObject()
            while (hasNext()) {
                val name = nextName()
                requireCatalog(seen.add(name))
                objectValue.add(name, readUniqueElement())
            }
            endObject()
        }
        JsonToken.BEGIN_ARRAY -> JsonArray().also { arrayValue ->
            beginArray()
            while (hasNext()) arrayValue.add(readUniqueElement())
            endArray()
        }
        JsonToken.STRING -> JsonPrimitive(nextString())
        JsonToken.NUMBER -> JsonPrimitive(BigDecimal(nextString()))
        JsonToken.BOOLEAN -> JsonPrimitive(nextBoolean())
        JsonToken.NULL -> JsonNull.INSTANCE.also { nextNull() }
        else -> rejectCatalog()
    }

    private fun JsonElement.exactObject(required: Set<String>, optional: Set<String> = emptySet()): JsonObject {
        val value = asRequiredObject()
        val keys = value.keySet()
        requireCatalog(keys.containsAll(required))
        requireCatalog(keys.all { it in required || it in optional })
        return value
    }

    private fun JsonElement.asRequiredObject(): JsonObject {
        requireCatalog(isJsonObject)
        return asJsonObject
    }

    private fun JsonElement.malayText(): String {
        val value = exactObject(setOf("ms-MY", "en-GB"))
        value.requiredString("en-GB")
        return value.requiredString("ms-MY")
    }

    private fun JsonObject.required(name: String): JsonElement = get(name) ?: rejectCatalog()

    private fun JsonObject.requiredString(name: String): String {
        val value = required(name)
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString.also { requireCatalog(it.isNotBlank()) }
    }

    private fun JsonObject.optionalString(name: String): String? {
        val value = get(name) ?: return null
        if (value.isJsonNull) return null
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString.also { requireCatalog(it.isNotBlank()) }
    }

    private fun JsonObject.requiredInt(name: String): Int {
        val value = required(name)
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isNumber)
        return try {
            value.asBigDecimal.intValueExact()
        } catch (_: ArithmeticException) {
            rejectCatalog()
        }
    }

    private fun JsonObject.requiredBoolean(name: String): Boolean {
        val value = required(name)
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isBoolean)
        return value.asBoolean
    }

    private fun JsonObject.requiredArray(name: String): JsonArray {
        val value = required(name)
        requireCatalog(value.isJsonArray)
        return value.asJsonArray
    }

    private fun JsonObject.requiredStringList(name: String): List<String> = requiredArray(name).map { value ->
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isString)
        value.asString.also { requireCatalog(it.isNotBlank()) }
    }

    private fun JsonObject.requiredIntList(name: String): List<Int> = requiredArray(name).map { value ->
        requireCatalog(value.isJsonPrimitive && value.asJsonPrimitive.isNumber)
        try {
            value.asBigDecimal.intValueExact()
        } catch (_: ArithmeticException) {
            rejectCatalog()
        }
    }

    private fun JsonObject.optionalStringList(name: String): List<String> =
        if (has(name)) requiredStringList(name) else emptyList()

    private fun String.isCanonicalEvidenceUrl(sourceSlug: String, reference: String): Boolean = try {
        val uri = URI(this)
        uri.scheme == "https" &&
            uri.host == "sunnah.com" &&
            uri.port == -1 &&
            uri.userInfo == null &&
            uri.query == null &&
            uri.fragment == null &&
            uri.path == "/$sourceSlug:$reference"
    } catch (_: RuntimeException) {
        false
    }

    private data class ParsedClaim(
        val id: String,
        val evidenceIds: List<String>,
        val corroborativeEvidenceIds: List<String>,
        val variationEvidenceIds: List<String>,
        val payload: JsonElement,
    )

    private data class ActionPayload(val title: String, val instruction: String)
    private data class RecurrencePayload(
        val kind: String,
        val dateBasis: String,
        val weekdays: List<String>,
        val event: String?,
    )
    private data class ExpressionPayload(
        val kind: String,
        val precision: String,
        val event: String?,
        val periods: List<String>,
        val prayerScope: String?,
        val lunarDateSet: List<Int>,
        val policyRequirement: String?,
    )
    private data class TimingPayload(
        val sourceTimePhrase: String,
        val resolutionStatus: String,
        val unsupportedInferences: List<String>,
        val recurrence: RecurrencePayload,
        val expression: ExpressionPayload,
    )

    companion object {
        private const val CATALOG_ID = "amuslim.starter"
        private const val CONTENT_VERSION = "0.2.0-draft"
        private val ROOT_FIELDS = setOf(
            "\$schema", "schemaVersion", "catalogId", "contentVersion", "purpose", "lifecycle",
            "defaultLocale", "supportedLocales", "researchAdmissionPolicy", "entries", "evidence", "approvals",
        )
        private val ADMISSION_FIELDS = setOf("acceptedCandidateAssessments", "publicationReviewRequired")
        private val EVIDENCE_FIELDS = setOf(
            "id", "sourceType", "attributionLevel", "collection", "reference", "canonicalUrl",
            "reportedAssessment", "supportsClaimTypes", "sourceSummary", "usagePermission",
        )
        private val ASSESSMENT_FIELDS = setOf("kind", "label", "grader")
        private val ENTRY_FIELDS = setOf(
            "id", "lifecycle", "practiceKind", "completionMode", "actionClaim", "timingClaim",
        )
        private val CLAIM_REQUIRED_FIELDS = setOf("id", "claimType", "reviewStatus", "evidenceIds", "payload")
        private val CLAIM_OPTIONAL_FIELDS = setOf("corroborativeEvidenceIds", "variationEvidenceIds")
        private val ACTION_REQUIRED_FIELDS = setOf("title", "instruction")
        private val ACTION_OPTIONAL_FIELDS = setOf("detailRequirement")
        private val TIMING_FIELDS = setOf(
            "sourceTimePhrase", "recurrence", "expression", "resolutionStatus", "unsupportedInferences",
        )
        private val EXPRESSION_FIELDS = setOf("kind", "parameters", "precision")
        private val CLAIM_TYPES = setOf("ACT", "TIMING")
        private val PRACTICE_KINDS = setOf("SALAH", "DHIKR", "QURAN", "FASTING", "DUA", "ADAB")
        private val COMPLETION_MODES = setOf("ONCE", "REPEATABLE_NO_TARGET", "PER_TRIGGER", "TWICE_PER_DAY")
        private val SOURCE_SLUGS = mapOf(
            "Sunan Abi Dawud" to "abudawud",
            "Sahih Muslim" to "muslim",
            "Sahih al-Bukhari" to "bukhari",
        )
        private val WEEKDAYS = setOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        private val RECURRENCE_EXPRESSION_PAIRS = mapOf(
            "TWICE_DAILY_NAMED_PERIODS" to "SEMANTIC_PERIODS",
            "AFTER_EACH_PRAYER" to "AFTER_USER_EVENT",
            "SOURCE_RECURRENCE_UNSPECIFIED" to "SEMANTIC_PERIODS",
            "ON_PERSONAL_EVENT" to "PERSONAL_EVENT",
            "EVERY_DAY" to "NIGHT_SEGMENT",
            "MONTHLY_CONTEXT" to "FASTING_LUNAR_DATES",
        )
        private val WEEKDAY_EXPRESSIONS = setOf(
            "FASTING_WEEKDAYS",
            "RELIGIOUS_WEEKDAY",
            "WEEKDAY_FINAL_PERIOD_AFTER_PRAYER",
        )
        private val EXPRESSION_PRECISION = mapOf(
            "SEMANTIC_PERIODS" to "UNRESOLVED",
            "AFTER_USER_EVENT" to "DIRECT_TRIGGER",
            "PERSONAL_EVENT" to "DIRECT_TRIGGER",
            "NIGHT_SEGMENT" to "UNRESOLVED",
            "FASTING_WEEKDAYS" to "BROAD_CONTEXT",
            "RELIGIOUS_WEEKDAY" to "UNRESOLVED",
            "WEEKDAY_FINAL_PERIOD_AFTER_PRAYER" to "BROAD_CONTEXT",
            "FASTING_LUNAR_DATES" to "BROAD_CONTEXT",
        )
        private val EXPECTED_ENTRY_IDS = listOf(
            "deed.daily.three-quls-morning-evening",
            "deed.daily.post-prayer-tasbih",
            "deed.daily.post-prayer-help-in-worship",
            "deed.daily.duha-two-rakah",
            "deed.daily.bedtime-three-quls",
            "deed.daily.last-third-dua-istighfar",
            "deed.daily.witr-before-sleep",
            "deed.weekly.fast-monday-thursday",
            "deed.weekly.friday-salawat",
            "deed.weekly.friday-final-period-dua",
            "deed.monthly.fast-white-days",
        )
    }
}

private class CatalogRejected : RuntimeException()
private fun requireCatalog(condition: Boolean) {
    if (!condition) throw CatalogRejected()
}
private fun rejectCatalog(): Nothing = throw CatalogRejected()
