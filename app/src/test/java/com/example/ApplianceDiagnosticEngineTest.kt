package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ApplianceDiagnosticEngineService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ApplianceDiagnosticEngineTest {

    private lateinit var service: ApplianceDiagnosticEngineService

    @Before
    fun setup() {
        service = ApplianceDiagnosticEngineService(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun fallbackAssessment_returnsValidCompleteSchema() {
        val result = service.generateFallbackAssessment(
            applianceUri = "content://media/external/images/1",
            damageUri = null
        )

        assertNotNull(result.appliance)
        assertTrue(result.appliance.type.isNotBlank())
        assertTrue(listOf("low", "medium", "high").contains(result.appliance.confidence.lowercase()))

        assertNotNull(result.damage)
        assertTrue(result.damage.component.isNotBlank())
        assertTrue(result.damage.damageType.isNotBlank())
        assertTrue(listOf("minor", "moderate", "severe", "none").contains(result.damage.severity.lowercase()))
        assertTrue(result.damage.description.isNotBlank())

        assertNotNull(result.costEstimate)
        assertEquals("INR", result.costEstimate.currency)
        assertTrue(result.costEstimate.min > 0)
        assertTrue(result.costEstimate.max >= result.costEstimate.min)
        assertTrue(result.costEstimate.basis.isNotBlank())

        assertTrue(result.notes.isNotBlank())
    }

    @Test
    fun fallbackAssessment_noPhotos_returnsValidNonEmptyAssessment() {
        val result = service.generateFallbackAssessment(null, null)

        assertNotNull(result.appliance)
        assertFalse(result.appliance.type.isEmpty())
        assertFalse(result.damage.detected)
        assertEquals("none", result.damage.component)
        assertEquals("none", result.damage.severity)
        assertTrue(result.damage.description.contains("no visible external damage detected", ignoreCase = true))
    }
}
