package com.example.intelli_pest

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for domain models and utilities
 * Note: Bitmap-based tests require instrumented tests (androidTest)
 */
class DomainModelTests {

    @Test
    fun `test pest type from index returns correct type`() {
        val pestType = com.example.intelli_pest.domain.model.PestType.fromIndex(0)
        assertEquals("Index 0 should be ARMYWORM",
            com.example.intelli_pest.domain.model.PestType.ARMYWORM, pestType)
    }

    @Test
    fun `test pest type from index returns null for invalid index`() {
        val pestType = com.example.intelli_pest.domain.model.PestType.fromIndex(100)
        assertNull("Invalid index should return null", pestType)
    }

    @Test
    fun `test pest type from index covers all types`() {
        // Test all 11 pest types
        for (i in 0..10) {
            val pestType = com.example.intelli_pest.domain.model.PestType.fromIndex(i)
            assertNotNull("Index $i should return a valid pest type", pestType)
        }
    }

    @Test
    fun `test pest type healthy detection`() {
        val healthy = com.example.intelli_pest.domain.model.PestType.fromIndex(1)
        assertEquals("Index 1 should be HEALTHY",
            com.example.intelli_pest.domain.model.PestType.HEALTHY, healthy)
    }

    @Test
    fun `test pest prediction confidence percentage`() {
        val prediction = com.example.intelli_pest.domain.model.PestPrediction(
            pestType = com.example.intelli_pest.domain.model.PestType.HEALTHY,
            confidence = 0.956f
        )
        val percentage = prediction.getConfidencePercentage()
        assertTrue("Percentage should contain 95", percentage.contains("95"))
    }

    @Test
    fun `test detection result meets threshold`() {
        val result = com.example.intelli_pest.domain.model.DetectionResult(
            pestType = com.example.intelli_pest.domain.model.PestType.ARMYWORM,
            confidence = 0.85f,
            imageUri = "test://image",
            modelUsed = "test_model",
            processingTimeMs = 100
        )

        assertTrue("0.85 should meet 0.7 threshold", result.meetsThreshold(0.7f))
        assertFalse("0.85 should not meet 0.9 threshold", result.meetsThreshold(0.9f))
    }

    @Test
    fun `test detection result confidence percentage`() {
        val result = com.example.intelli_pest.domain.model.DetectionResult(
            pestType = com.example.intelli_pest.domain.model.PestType.TERMITE,
            confidence = 0.75f,
            imageUri = "",
            modelUsed = "super_ensemble",
            processingTimeMs = 450
        )

        val percentage = result.getConfidencePercentage()
        assertEquals("Percentage should be 75.0%", "75.0%", percentage)
    }

    @Test
    fun `test model info properties`() {
        val modelInfo = com.example.intelli_pest.domain.model.ModelInfo(
            id = "test_model",
            name = "test_model",
            displayName = "Test Model",
            description = "A test model",
            accuracy = 0.95f,
            inferenceSpeedMs = 200,
            sizeInMb = 100f,
            isDownloaded = false,
            isBundled = true
        )

        assertEquals("ID should match", "test_model", modelInfo.id)
        assertTrue("Should be bundled", modelInfo.isBundled)
        assertFalse("Should not be downloaded", modelInfo.isDownloaded)
    }

    @Test
    fun `test resource success state`() {
        val resource = com.example.intelli_pest.domain.model.Resource.Success("test data")
        assertTrue("Should be success", resource is com.example.intelli_pest.domain.model.Resource.Success)
        assertEquals("Data should match", "test data", resource.data)
    }

    @Test
    fun `test resource error state`() {
        val resource: com.example.intelli_pest.domain.model.Resource<String> =
            com.example.intelli_pest.domain.model.Resource.Error("Error message")
        assertTrue("Should be error", resource is com.example.intelli_pest.domain.model.Resource.Error)
        assertEquals("Message should match", "Error message",
            (resource as com.example.intelli_pest.domain.model.Resource.Error).message)
    }

    @Test
    fun `test resource loading state`() {
        val resource = com.example.intelli_pest.domain.model.Resource.Loading
        assertTrue("Should be loading", resource is com.example.intelli_pest.domain.model.Resource.Loading)
    }

    @Test
    fun `test all pest types have display names`() {
        com.example.intelli_pest.domain.model.PestType.entries.forEach { pestType ->
            assertNotNull("Display name should not be null", pestType.displayName)
            assertTrue("Display name should not be empty", pestType.displayName.isNotEmpty())
        }
    }

    @Test
    fun `test all pest types have descriptions`() {
        com.example.intelli_pest.domain.model.PestType.entries.forEach { pestType ->
            assertNotNull("Description should not be null", pestType.description)
            assertTrue("Description should not be empty", pestType.description.isNotEmpty())
        }
    }
}

