package com.example.intelli_pest

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.intelli_pest.ml.ImagePreprocessor
import com.example.intelli_pest.ml.ImageValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for image processing components
 * These tests run on an Android device/emulator
 */
@RunWith(AndroidJUnit4::class)
class ImageProcessingInstrumentedTest {

    private lateinit var imageValidator: ImageValidator
    private lateinit var imagePreprocessor: ImagePreprocessor

    @Before
    fun setup() {
        imageValidator = ImageValidator()
        imagePreprocessor = ImagePreprocessor()
    }

    // ==================== ImageValidator Tests ====================

    @Test
    fun testValidationWithGreenImage() {
        // Create a green-dominant bitmap (simulating crop image)
        val greenBitmap = createColoredBitmap(200, 200, Color.rgb(50, 150, 50))
        val result = imageValidator.isValidSugarcaneCropImage(greenBitmap)
        assertTrue("Green image should be valid", result)
    }

    @Test
    fun testValidationWithMixedColorImage() {
        // Create an image with mixed colors
        val bitmap = createVariedBitmap(200, 200)
        // Should not crash and should return a result
        val result = imageValidator.isValidSugarcaneCropImage(bitmap)
        assertNotNull("Result should not be null", result)
    }

    @Test
    fun testValidationWithVerySmallImage() {
        val smallBitmap = createColoredBitmap(50, 50, Color.GREEN)
        // Should not crash
        val result = imageValidator.isValidSugarcaneCropImage(smallBitmap)
        assertNotNull("Result should not be null", result)
    }

    @Test
    fun testValidationConfidenceRange() {
        val bitmap = createColoredBitmap(200, 200, Color.rgb(80, 120, 60))
        val confidence = imageValidator.getValidationConfidence(bitmap)
        assertTrue("Confidence should be between 0 and 1", confidence in 0f..1f)
    }

    @Test
    fun testValidationWithDarkImage() {
        val darkBitmap = createColoredBitmap(200, 200, Color.rgb(10, 10, 10))
        // Should handle dark images gracefully
        val result = imageValidator.isValidSugarcaneCropImage(darkBitmap)
        assertNotNull("Result should not be null for dark image", result)
    }

    @Test
    fun testValidationWithBrightImage() {
        val brightBitmap = createColoredBitmap(200, 200, Color.rgb(250, 250, 250))
        // Should handle bright images gracefully
        val result = imageValidator.isValidSugarcaneCropImage(brightBitmap)
        assertNotNull("Result should not be null for bright image", result)
    }

    // ==================== ImagePreprocessor Tests ====================

    @Test
    fun testPreprocessImageOutputSize() {
        val bitmap = createColoredBitmap(300, 300, Color.GRAY)
        val result = imagePreprocessor.preprocessImage(bitmap)

        // Expected size: 224 * 224 * 3 = 150528
        assertEquals("Output array should be 150528 floats", 150528, result.size)
    }

    @Test
    fun testPreprocessImageWithPortraitImage() {
        val bitmap = createColoredBitmap(200, 400, Color.GRAY)
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Output should still be 150528 floats", 150528, result.size)
    }

    @Test
    fun testPreprocessImageWithLandscapeImage() {
        val bitmap = createColoredBitmap(400, 200, Color.GRAY)
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Output should still be 150528 floats", 150528, result.size)
    }

    @Test
    fun testPreprocessImageWithSquareImage() {
        val bitmap = createColoredBitmap(224, 224, Color.GRAY)
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Output should be 150528 floats", 150528, result.size)
    }

    @Test
    fun testPreprocessImageNormalizationRange() {
        val bitmap = createColoredBitmap(224, 224, Color.GRAY)
        val result = imagePreprocessor.preprocessImage(bitmap)

        // Normalized values should be in reasonable range for ImageNet normalization
        val minVal = result.minOrNull() ?: 0f
        val maxVal = result.maxOrNull() ?: 0f

        assertTrue("Min value should be > -10", minVal > -10f)
        assertTrue("Max value should be < 10", maxVal < 10f)
    }

    @Test
    fun testImageQualityWithGoodImage() {
        val bitmap = createVariedBitmap(300, 300)
        val result = imagePreprocessor.isImageQualitySufficient(bitmap)
        assertTrue("Good quality image should pass", result)
    }

    @Test
    fun testImageQualityWithTooSmallImage() {
        val bitmap = createColoredBitmap(30, 30, Color.GRAY)
        val result = imagePreprocessor.isImageQualitySufficient(bitmap)
        assertFalse("Too small image should fail", result)
    }

    @Test
    fun testFloatArrayToByteBuffer() {
        val floatArray = floatArrayOf(1.0f, 2.0f, 3.0f)
        val byteBuffer = imagePreprocessor.floatArrayToByteBuffer(floatArray)

        // Each float is 4 bytes
        assertEquals("ByteBuffer should have correct capacity", 12, byteBuffer.capacity())
    }

    // ==================== Hardware Bitmap Handling Tests ====================

    @Test
    fun testPreprocessImageWithSoftwareBitmap() {
        // ARGB_8888 is a software bitmap config
        val bitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        for (x in 0 until 224) {
            for (y in 0 until 224) {
                bitmap.setPixel(x, y, Color.rgb(100, 150, 100))
            }
        }

        // Should not crash
        val result = imagePreprocessor.preprocessImage(bitmap)
        assertEquals("Should process successfully", 150528, result.size)
    }

    @Test
    fun testValidatorWithSoftwareBitmap() {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        for (x in 0 until 200) {
            for (y in 0 until 200) {
                bitmap.setPixel(x, y, Color.rgb(50, 150, 50))
            }
        }

        // Should not crash
        val result = imageValidator.isValidSugarcaneCropImage(bitmap)
        assertNotNull("Should return result", result)
    }

    // ==================== Helper Methods ====================

    private fun createColoredBitmap(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }

    private fun createVariedBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                // Add variation to simulate real image
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

