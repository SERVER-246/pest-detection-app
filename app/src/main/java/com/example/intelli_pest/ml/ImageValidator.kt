package com.example.intelli_pest.ml

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

/**
 * Validates if an image is suitable for pest detection
 * Filters out unrelated images (non-sugarcane crops)
 */
class ImageValidator {

    companion object {
        private const val TAG = "ImageValidator"
    }

    /**
     * Safely get pixel from bitmap, returns default color on failure
     */
    private fun safeGetPixel(bitmap: Bitmap, x: Int, y: Int): Int {
        return try {
            if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                bitmap.getPixel(x, y)
            } else {
                Color.GRAY
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get pixel at ($x, $y)", e)
            Color.GRAY
        }
    }

    /**
     * Check if image appears to be a sugarcane crop
     * Returns true by default on any error to avoid blocking valid images
     */
    fun isValidSugarcaneCropImage(bitmap: Bitmap): Boolean {
        // This function now assumes it receives a software bitmap.
        // The conversion is handled at the source (Camera/Gallery).
        return try {
            Log.d(TAG, "======= IMAGE VALIDATION START =======")
            Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")

            // If we still can't access pixels, just accept the image
            if (!canAccessPixels(bitmap)) {
                Log.w(TAG, "⚠️ Cannot access pixels, accepting image by default")
                return true
            }

            // Multiple validation checks with safe pixel access
            val hasGreenContent = checkGreenContentSafe(bitmap)
            val hasProperColorDistribution = checkColorDistributionSafe(bitmap)
            val hasTextureVariation = checkTextureVariationSafe(bitmap)
            val qualityCheck = checkBasicQualitySafe(bitmap)

            Log.d(TAG, "Validation checks:")
            Log.d(TAG, "  - Green content: $hasGreenContent")
            Log.d(TAG, "  - Color distribution: $hasProperColorDistribution")
            Log.d(TAG, "  - Texture variation: $hasTextureVariation")
            Log.d(TAG, "  - Quality: $qualityCheck")

            // Image is valid if it passes at least 2 checks (improved from 1)
            val checksPassedCount = listOf(
                hasGreenContent,
                hasProperColorDistribution,
                hasTextureVariation,
                qualityCheck
            ).count { it }

            val isValid = checksPassedCount >= 2

            Log.d(TAG, "Checks passed: $checksPassedCount/4")
            if (isValid) {
                Log.d(TAG, "✅ Image validation PASSED")
            } else {
                Log.w(TAG, "❌ Image validation FAILED - Image may not be a sugarcane crop")
            }
            Log.d(TAG, "======= IMAGE VALIDATION END =======")

            isValid
        } catch (e: Exception) {
            Log.e(TAG, "❌ Image validation error, accepting by default", e)
            // On ANY error, accept the image and let the model decide
            true
        }
    }

    /**
     * Check if we can actually access pixels on this bitmap
     */
    private fun canAccessPixels(bitmap: Bitmap): Boolean {
        return try {
            if (bitmap.width > 0 && bitmap.height > 0) {
                bitmap.getPixel(0, 0)
                Log.d(TAG, "✅ Pixel access successful")
                true
            } else {
                Log.w(TAG, "❌ Invalid bitmap dimensions")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Cannot access pixels", e)
            false
        }
    }

    /**
     * Safe version of green content check
     */
    private fun checkGreenContentSafe(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 20
            var greenPixelCount = 0
            var totalSamples = 0

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = safeGetPixel(bitmap, x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    if (g > r * 0.7 && g > b * 0.7 && g > 20) {
                        greenPixelCount++
                    }
                    totalSamples++
                }
            }

            if (totalSamples == 0) return true
            val greenPercentage = greenPixelCount.toFloat() / totalSamples
            Log.d(TAG, "Green content: ${String.format("%.2f%%", greenPercentage * 100)}")
            greenPercentage >= 0.10f // 10% green content required
        } catch (e: Exception) {
            Log.e(TAG, "Error checking green content", e)
            true
        }
    }

    /**
     * Safe version of color distribution check
     */
    private fun checkColorDistributionSafe(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 20
            var redSum = 0L
            var greenSum = 0L
            var blueSum = 0L
            var totalSamples = 0

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = safeGetPixel(bitmap, x, y)
                    redSum += Color.red(pixel)
                    greenSum += Color.green(pixel)
                    blueSum += Color.blue(pixel)
                    totalSamples++
                }
            }

            if (totalSamples == 0) return true
            // Just check it's not completely black
            val avgBrightness = (redSum + greenSum + blueSum) / (3 * totalSamples)
            avgBrightness > 5
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Safe version of texture variation check
     */
    private fun checkTextureVariationSafe(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 30
            val brightnessValues = mutableListOf<Int>()

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = safeGetPixel(bitmap, x, y)
                    val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    brightnessValues.add(brightness)
                }
            }

            if (brightnessValues.size < 2) return true

            val mean = brightnessValues.average()
            val variance = brightnessValues.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)

            stdDev > 3.0 // Very lenient threshold
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Safe version of basic quality check
     */
    private fun checkBasicQualitySafe(bitmap: Bitmap): Boolean {
        return try {
            // Minimum resolution check - very lenient
            if (bitmap.width < 10 || bitmap.height < 10) {
                return false
            }
            true // Accept most images
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Get validation confidence score (0.0 to 1.0)
     */
    fun getValidationConfidence(bitmap: Bitmap): Float {
        return try {
            // The bitmap is now assumed to be software-based.
            if (!canAccessPixels(bitmap)) {
                return 0.75f
            }

            val checks = listOf(
                checkGreenContentSafe(bitmap),
                checkColorDistributionSafe(bitmap),
                checkTextureVariationSafe(bitmap),
                checkBasicQualitySafe(bitmap)
            )

            checks.count { it }.toFloat() / checks.size
        } catch (_: Exception) {
            0.75f
        }
    }
}

