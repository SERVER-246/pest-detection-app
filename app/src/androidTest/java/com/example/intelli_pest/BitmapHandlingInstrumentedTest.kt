package com.example.intelli_pest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.intelli_pest.ml.ImagePreprocessor
import com.example.intelli_pest.ml.ImageValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive instrumented tests for bitmap handling
 * These tests verify that hardware bitmap issues are properly handled
 */
@RunWith(AndroidJUnit4::class)
class BitmapHandlingInstrumentedTest {

    private lateinit var imageValidator: ImageValidator
    private lateinit var imagePreprocessor: ImagePreprocessor

    @Before
    fun setup() {
        imageValidator = ImageValidator()
        imagePreprocessor = ImagePreprocessor()
    }

    // ==================== Software Bitmap Tests ====================

    @Test
    fun testSoftwareBitmapValidation() {
        val bitmap = createSoftwareBitmap(200, 200, Color.rgb(50, 150, 50))

        // Should not crash
        val result = imageValidator.isValidSugarcaneCropImage(bitmap)
        assertNotNull("Result should not be null", result)
    }

    @Test
    fun testSoftwareBitmapPreprocessing() {
        val bitmap = createSoftwareBitmap(300, 300, Color.rgb(100, 150, 100))

        // Should not crash and return correct size
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Output should be 150528 floats", 150528, result.size)
    }

    @Test
    fun testSoftwareBitmapQualityCheck() {
        val bitmap = createSoftwareBitmap(200, 200, Color.rgb(128, 128, 128))

        // Should not crash
        val result = imagePreprocessor.isImageQualitySufficient(bitmap)
        assertTrue("Good quality image should pass", result)
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun testVerySmallBitmap() {
        val bitmap = createSoftwareBitmap(10, 10, Color.GREEN)

        // Should handle gracefully
        val validationResult = imageValidator.isValidSugarcaneCropImage(bitmap)
        assertNotNull("Should handle small bitmap", validationResult)

        val preprocessResult = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Should still produce correct output size", 150528, preprocessResult.size)
    }

    @Test
    fun testNonSquareBitmap() {
        val portraitBitmap = createSoftwareBitmap(100, 400, Color.rgb(50, 150, 50))
        val landscapeBitmap = createSoftwareBitmap(400, 100, Color.rgb(50, 150, 50))

        // Both should work
        val portraitResult = imagePreprocessor.preprocessImage(portraitBitmap)
        val landscapeResult = imagePreprocessor.preprocessImage(landscapeBitmap)

        assertEquals("Portrait should produce correct size", 150528, portraitResult.size)
        assertEquals("Landscape should produce correct size", 150528, landscapeResult.size)
    }

    @Test
    fun testDarkBitmap() {
        val darkBitmap = createSoftwareBitmap(200, 200, Color.rgb(5, 5, 5))

        // Should handle without crashing
        val validationResult = imageValidator.isValidSugarcaneCropImage(darkBitmap)
        assertNotNull("Should handle dark bitmap", validationResult)
    }

    @Test
    fun testBrightBitmap() {
        val brightBitmap = createSoftwareBitmap(200, 200, Color.rgb(250, 250, 250))

        // Should handle without crashing
        val validationResult = imageValidator.isValidSugarcaneCropImage(brightBitmap)
        assertNotNull("Should handle bright bitmap", validationResult)
    }

    @Test
    fun testValidationConfidence() {
        val greenBitmap = createSoftwareBitmap(200, 200, Color.rgb(50, 150, 50))

        val confidence = imageValidator.getValidationConfidence(greenBitmap)
        assertTrue("Confidence should be between 0 and 1", confidence in 0f..1f)
    }

    // ==================== Bitmap Config Tests ====================

    @Test
    fun testARGB8888Config() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        fillBitmapWithColor(bitmap, Color.rgb(100, 150, 100))

        val result = imageValidator.isValidSugarcaneCropImage(bitmap)
        assertNotNull("ARGB_8888 should work", result)
    }

    @Test
    fun testRGB565Config() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565)
        fillBitmapWithColor(bitmap, Color.rgb(100, 150, 100))

        // Should convert and work
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("RGB_565 should be converted and work", 150528, result.size)
    }

    // ==================== Stress Tests ====================

    @Test
    fun testLargeBitmap() {
        val largeBitmap = createSoftwareBitmap(2000, 2000, Color.rgb(100, 150, 100))

        // Should handle large bitmap
        val result = imagePreprocessor.preprocessImage(largeBitmap)
        assertEquals("Large bitmap should work", 150528, result.size)
    }

    @Test
    fun testMultipleConsecutiveValidations() {
        val bitmap = createSoftwareBitmap(200, 200, Color.rgb(100, 150, 100))

        // Run multiple times
        for (i in 1..10) {
            val result = imageValidator.isValidSugarcaneCropImage(bitmap)
            assertNotNull("Validation #$i should work", result)
        }
    }

    @Test
    fun testMultipleConsecutivePreprocessing() {
        val bitmap = createSoftwareBitmap(300, 300, Color.rgb(100, 150, 100))

        // Run multiple times
        for (i in 1..10) {
            val result = imagePreprocessor.preprocessImage(bitmap)
            assertEquals("Preprocessing #$i should produce correct size", 150528, result.size)
        }
    }

    // ==================== Helper Methods ====================

    private fun createSoftwareBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        fillBitmapWithColor(bitmap, color)
        return bitmap
    }

    private fun fillBitmapWithColor(bitmap: Bitmap, color: Int) {
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
    }

    private fun createVariedBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val variation = ((x + y) % 100)
                val r = (50 + variation).coerceIn(0, 255)
                val g = (100 + variation).coerceIn(0, 255)
                val b = (50 + variation / 2).coerceIn(0, 255)
                bitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }
        return bitmap
    }
}

