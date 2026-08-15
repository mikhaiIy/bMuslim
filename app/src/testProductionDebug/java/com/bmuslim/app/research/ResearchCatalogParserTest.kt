package com.bmuslim.app.research

import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchCatalogParserTest {
    private val parser = ResearchCatalogParser()
    private val canonical by lazy(::readCanonicalCatalog)

    @Test
    fun canonicalDraftLoadsAllReviewedResearchCandidates() {
        val result = parser.parse(canonical)

        assertTrue(result is ResearchCatalogResult.Ready)
        val catalog = (result as ResearchCatalogResult.Ready).catalog
        assertEquals("amuslim.starter", catalog.catalogId)
        assertEquals("0.2.0-draft", catalog.contentVersion)
        assertEquals("IN_REVIEW", catalog.lifecycle)
        assertEquals(11, catalog.deeds.size)
        assertEquals(7, catalog.dailyDeeds.size)
        assertEquals(3, catalog.weeklyDeeds.size)
        assertEquals(1, catalog.monthlyDeeds.size)
        assertEquals(14, catalog.evidenceCount)
        assertTrue(catalog.deeds.all { it.evidence.isNotEmpty() })
        assertTrue(catalog.deeds.all { it.unsupportedInferences.isNotEmpty() })
        val duha = catalog.deeds.single { it.id == "deed.daily.duha-two-rakah" }
        assertEquals(
            listOf("evidence.muslim.719a"),
            duha.actionVariationEvidence.map { it.id },
        )
        assertTrue(duha.actionVariationEvidence.all { it.role == ResearchEvidenceRole.VARIATION })
        assertEquals(
            listOf("evidence.muslim.748b"),
            duha.timingVariationEvidence.map { it.id },
        )
        assertTrue(duha.timingVariationEvidence.all { it.role == ResearchEvidenceRole.VARIATION })
        assertEquals(
            listOf("evidence.muslim.719a", "evidence.muslim.748b"),
            duha.variationEvidence.map { it.id },
        )
        val postPrayerHelp = catalog.deeds.single { it.id == "deed.daily.post-prayer-help-in-worship" }
        assertEquals("PRESCRIBED_ONLY", postPrayerHelp.prayerScope)
        assertEquals("Memohon pertolongan untuk mengingati, bersyukur dan beribadah", postPrayerHelp.title)
        assertEquals(
            "Mohon pertolongan Allah untuk mengingati-Nya, bersyukur kepada-Nya dan beribadah kepada-Nya dengan baik selepas selesai solat fardu.",
            postPrayerHelp.instruction,
        )
        assertEquals(listOf(13, 14, 15), catalog.monthlyDeeds.single().lunarDateSet)
    }

    @Test
    fun duplicateKeysAreRejected() {
        assertRejected(canonical.replaceFirst("\"schemaVersion\": 2,", "\"schemaVersion\": 2,\n  \"schemaVersion\": 2,"))
    }

    @Test
    fun nonIntegralSchemaVersionIsRejected() {
        assertRejected(canonical.replaceFirst("\"schemaVersion\": 2", "\"schemaVersion\": 1.5"))
    }

    @Test
    fun v1DraftIsRejectedByTheV2Parser() {
        assertRejected(canonical.replaceFirst("\"schemaVersion\": 2", "\"schemaVersion\": 1"))
    }

    @Test
    fun unknownFieldsAreRejected() {
        assertRejected(canonical.replaceFirst("{", "{\n  \"unexpected\": true,"))
    }

    @Test
    fun nonEmptyApprovalsAreRejected() {
        assertRejected(canonical.replace("\"approvals\": []", "\"approvals\": [{\"reviewer\": \"untrusted\"}]"))
    }

    @Test
    fun nonReviewLifecycleIsRejected() {
        assertRejected(canonical.replaceFirst("\"lifecycle\": \"IN_REVIEW\"", "\"lifecycle\": \"PUBLISHED\""))
    }

    @Test
    fun missingMalayLocaleIsRejected() {
        assertRejected(
            canonical.replaceFirst(
                Regex("\\\"supportedLocales\\\"\\s*:\\s*\\[\\s*\\\"ms-MY\\\",\\s*\\\"en-GB\\\"\\s*]"),
                "\"supportedLocales\": [\"en-GB\"]",
            ),
        )
    }

    @Test
    fun danglingEvidenceReferenceIsRejected() {
        assertRejected(canonical.replaceFirst(Regex("\"evidenceIds\"\\s*:\\s*\\[\\s*\"evidence\\.[^\"]+\""), "\"evidenceIds\": [\"evidence.missing\""))
    }

    @Test
    fun unsupportedClaimTypeIsRejected() {
        assertRejected(canonical.replaceFirst("\"claimType\": \"ACT\"", "\"claimType\": \"OTHER\""))
    }

    @Test
    fun unsafeEvidenceUrlIsRejected() {
        assertRejected(canonical.replaceFirst(Regex("https://[^\"]+"), "http://example.invalid/source"))
    }

    @Test
    fun wrongHttpsEvidenceHostIsRejected() {
        assertRejected(canonical.replaceFirst("https://sunnah.com/abudawud:5082", "https://evil.example/abudawud:5082"))
    }

    @Test
    fun mismatchedEvidenceReferencePathIsRejected() {
        assertRejected(canonical.replaceFirst("https://sunnah.com/abudawud:5082", "https://sunnah.com/abudawud:9999"))
    }

    @Test
    fun expandedEvidenceUsagePermissionIsRejected() {
        assertRejected(canonical.replaceFirst("LINK_AND_PARAPHRASE_ONLY", "COPY_FULL_TEXT"))
    }

    @Test
    fun trailingJsonIsRejected() {
        assertRejected("$canonical\n{}")
    }

    @Test
    fun mismatchedPersonalEventsAreRejected() {
        assertRejected(
            canonical.replaceFirst(
                Regex("\\\"relation\\\"\\s*:\\s*\\\"AT\\\",\\s*\\\"event\\\"\\s*:\\s*\\\"INTEND_SLEEP_AT_NIGHT\\\""),
                "\"relation\": \"AT\", \"event\": \"INTEND_SLEEP\"",
            ),
        )
    }

    @Test
    fun unresolvedTimingCannotClaimDirectEventResolution() {
        assertRejected(
            canonical.replaceFirst(
                "\"resolutionStatus\": \"REQUIRES_REVIEWED_TEMPORAL_POLICY\"",
                "\"resolutionStatus\": \"DIRECT_EVENT_ONLY\"",
            ),
        )
    }

    @Test
    fun directEventCannotClaimUnresolvedTemporalPolicy() {
        assertRejected(
            canonical.replaceFirst(
                "\"resolutionStatus\": \"DIRECT_EVENT_ONLY\"",
                "\"resolutionStatus\": \"REQUIRES_REVIEWED_TEMPORAL_POLICY\"",
            ),
        )
    }

    @Test
    fun duplicateEvidenceClaimTypesAreRejected() {
        assertRejected(
            canonical.replaceFirst(
                Regex("\\\"supportsClaimTypes\\\"\\s*:\\s*\\[\\s*\\\"ACT\\\",\\s*\\\"TIMING\\\"\\s*]"),
                "\"supportsClaimTypes\": [\"ACT\", \"ACT\", \"TIMING\"]",
            ),
        )
    }

    @Test
    fun timingOnlyVariationCannotBeLinkedToActionClaim() {
        assertRejected(
            canonical.replace(
                "\"variationEvidenceIds\": [\n          \"evidence.muslim.719a\"\n        ]",
                "\"variationEvidenceIds\": [\n          \"evidence.muslim.748b\"\n        ]",
            ),
        )
    }

    @Test
    fun actionOnlyVariationCannotBeLinkedToTimingClaim() {
        assertRejected(
            canonical.replace(
                "\"variationEvidenceIds\": [\n          \"evidence.muslim.748b\"\n        ]",
                "\"variationEvidenceIds\": [\n          \"evidence.muslim.719a\"\n        ]",
            ),
        )
    }

    @Test
    fun emptySemanticPeriodsAreRejected() {
        assertRejected(
            canonical.replaceFirst(
                Regex("\\\"periods\\\"\\s*:\\s*\\[\\s*\\\"MORNING\\\",\\s*\\\"EVENING\\\"\\s*]"),
                "\"periods\": []",
            ),
        )
    }

    @Test
    fun repositoryRejectsOversizedAssets() {
        val bytes = ByteArray(ResearchCatalogRepository.MAX_BYTES + 1) { ' '.code.toByte() }

        val result = ResearchCatalogRepository().load(ByteArrayInputStream(bytes))

        assertTrue(result is ResearchCatalogResult.Rejected)
    }

    @Test
    fun repositoryRejectsMalformedUtf8() {
        val malformed = byteArrayOf(
            '{'.code.toByte(), '"'.code.toByte(), 'x'.code.toByte(), '"'.code.toByte(),
            ':'.code.toByte(), '"'.code.toByte(), 0xC3.toByte(), 0x28, '"'.code.toByte(), '}'.code.toByte(),
        )

        val result = ResearchCatalogRepository().load(ByteArrayInputStream(malformed))

        assertTrue(result is ResearchCatalogResult.Rejected)
    }

    private fun assertRejected(value: String) {
        assertTrue(parser.parse(value) is ResearchCatalogResult.Rejected)
    }

    private fun readCanonicalCatalog(): String {
        val candidates = listOf(
            File("datasets/amuslim/starter-v0.2/catalog.draft.json"),
            File("../datasets/amuslim/starter-v0.2/catalog.draft.json"),
        )
        return candidates.firstOrNull(File::isFile)?.readText()
            ?: error("Canonical aMuslim starter catalog was not found")
    }
}
