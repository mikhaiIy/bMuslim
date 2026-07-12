package com.bmuslim.app

import org.junit.Assert.assertTrue
import org.junit.Test

class DemoDisclosureTest {
    @Test
    fun disclosureNamesSampleDataAndRejectsLiveGuidanceFraming() {
        assertTrue(DemoDisclosureText.contains("data sampel"))
        assertTrue(DemoDisclosureText.contains("bukan panduan langsung"))
    }
}
