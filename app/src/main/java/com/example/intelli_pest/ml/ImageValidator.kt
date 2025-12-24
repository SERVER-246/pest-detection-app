package com.example.intelli_pest.ml

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.example.intelli_pest.util.AppLogger
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Validates if an image is suitable for pest detection
 * Implements multi-metric validation: blur, contrast, green ratio, brightness
 * Configurable thresholds for tuning without code changes
 */
class ImageValidator {

    companion object {
        private const val TAG = "ImageValidator"

        // Configurable thresholds - can be tuned without code changes
        const val BLUR_THRESHOLD = 100.0  // Laplacian variance threshold (lower = blurrier)
        const val CONTRAST_THRESHOLD = 30.0  // Minimum contrast threshold
        const val GREEN_RATIO_THRESHOLD = 0.08f  // Minimum green ratio (8%)
        const val MIN_BRIGHTNESS = 20  // Minimum average brightness
        const val MAX_BRIGHTNESS = 240  // Maximum average brightness (avoid overexposed)
        const val MIN_IMAGE_SIZE = 100  // Minimum dimension in pixels
    }

    /**
     * Image validation result with detailed metrics
     */
    data class ValidationResult(
        val isValid: Boolean,
        val blurScore: Double,
        val contrastScore: Double,
        val greenRatio: Float,
        val brightness: Int,
        val reason: String? = null
    )

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
            Color.GRAY
        }
    }

    /**
     * Main validation function - checks multiple metrics
     * Returns true only if image passes all critical checks
     */
    fun isValidSugarcaneCropImage(bitmap: Bitmap): Boolean {
        val result = validateImageWithMetrics(bitmap)
        return result.isValid
    }

    /**
     * Validate image and return detailed metrics
     */
    fun validateImageWithMetrics(bitmap: Bitmap): ValidationResult {
        return try {
            Log.d(TAG, "======= IMAGE VALIDATION START =======")
            Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}, config: ${bitmap.config}")

            // Check minimum size
            if (bitmap.width < MIN_IMAGE_SIZE || bitmap.height < MIN_IMAGE_SIZE) {
                val reason = "Image too small: ${bitmap.width}x${bitmap.height}"
                Log.w(TAG, "❌ $reason")
                AppLogger.logWarning("ImageValidator", "Size_Check_Failed", reason)
                return ValidationResult(
                    isValid = false,
                    blurScore = 0.0,
                    contrastScore = 0.0,
                    greenRatio = 0f,
                    brightness = 0,
                    reason = reason
                )
            }

            // Check if we can access pixels
            if (!canAccessPixels(bitmap)) {
                Log.w(TAG, "⚠️ Cannot access pixels, accepting image by default")
                return ValidationResult(
                    isValid = true,
                    blurScore = 0.0,
                    contrastScore = 0.0,
                    greenRatio = 0f,
                    brightness = 128,
                    reason = "Pixel access unavailable"
                )
            }

            // Calculate all metrics
            val blurScore = calculateBlurScore(bitmap)
            val contrastScore = calculateContrastScore(bitmap)
            val greenRatio = calculateGreenRatio(bitmap)
            val brightness = calculateAverageBrightness(bitmap)

            // Log all metrics
            AppLogger.logDebug("ImageValidator", "Metrics",
                "blur=${"%.2f".format(blurScore)}, contrast=${"%.2f".format(contrastScore)}, " +
                "greenRatio=${"%.2f".format(greenRatio * 100)}%, brightness=$brightness")

            Log.d(TAG, "Validation metrics:")
            Log.d(TAG, "  - Blur score: ${"%.2f".format(blurScore)} (threshold: $BLUR_THRESHOLD)")
            Log.d(TAG, "  - Contrast: ${"%.2f".format(contrastScore)} (threshold: $CONTRAST_THRESHOLD)")
            Log.d(TAG, "  - Green ratio: ${"%.2f%%".format(greenRatio * 100)} (threshold: ${GREEN_RATIO_THRESHOLD * 100}%)")
            Log.d(TAG, "  - Brightness: $brightness (range: $MIN_BRIGHTNESS-$MAX_BRIGHTNESS)")

            // Validate each metric
            val reasons = mutableListOf<String>()

            // Check blur (low score = blurry)
            if (blurScore < BLUR_THRESHOLD) {
                reasons.add("Image appears blurry (score: ${"%.1f".format(blurScore)})")
            }

            // Check contrast
            if (contrastScore < CONTRAST_THRESHOLD) {
                reasons.add("Image has low contrast")
            }

            // Check green content (plant matter indicator)
            if (greenRatio < GREEN_RATIO_THRESHOLD) {
                reasons.add("Image doesn't appear to contain plant matter")
            }

            // Check brightness
            if (brightness < MIN_BRIGHTNESS) {
                reasons.add("Image is too dark")
            } else if (brightness > MAX_BRIGHTNESS) {
                reasons.add("Image is overexposed")
            }

            val isValid = reasons.isEmpty()
            val reason = if (reasons.isNotEmpty()) reasons.joinToString("; ") else null

            if (isValid) {
                Log.d(TAG, "✅ Image validation PASSED")
                AppLogger.logResponse("ImageValidator", "Validation_Passed",
                    "All checks passed: blur=${"%.1f".format(blurScore)}, green=${"%.1f%%".format(greenRatio * 100)}")
            } else {
                Log.w(TAG, "❌ Image validation FAILED: $reason")
                AppLogger.logWarning("ImageValidator", "Validation_Failed", reason ?: "Unknown reason")
            }

            Log.d(TAG, "======= IMAGE VALIDATION END =======")

            ValidationResult(
                isValid = isValid,
                blurScore = blurScore,
                contrastScore = contrastScore,
                greenRatio = greenRatio,
                brightness = brightness,
                reason = reason
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Image validation error", e)
            AppLogger.logError("ImageValidator", "Validation_Error", e, "Exception during validation")
            // On error, be permissive but log it
            ValidationResult(
                isValid = true,
                blurScore = 0.0,
                contrastScore = 0.0,
                greenRatio = 0f,
                brightness = 128,
                reason = "Validation error: ${e.message}"
            )
        }
    }

    /**
     * Check if we can actually access pixels on this bitmap
     */
    private fun canAccessPixels(bitmap: Bitmap): Boolean {
        return try {
            if (bitmap.width > 0 && bitmap.height > 0) {
                bitmap.getPixel(0, 0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cannot access pixels", e)
            false
        }
    }

    /**
     * Calculate blur score using Laplacian variance approximation
     * Higher score = sharper image
     */
    private fun calculateBlurScore(bitmap: Bitmap): Double {
        return try {
            val sampleStep = maxOf(bitmap.width / 50, 10)
            var variance = 0.0
            var count = 0

            // Sample center region for blur detection
            val startX = bitmap.width / 4
            val endX = bitmap.width * 3 / 4
            val startY = bitmap.height / 4
            val endY = bitmap.height * 3 / 4

            for (y in startY until endY step sampleStep) {
                for (x in startX until endX step sampleStep) {
                    if (x > 0 && x < bitmap.width - 1 && y > 0 && y < bitmap.height - 1) {
                        // Laplacian kernel approximation
                        val center = getGrayscale(safeGetPixel(bitmap, x, y))
                        val left = getGrayscale(safeGetPixel(bitmap, x - 1, y))
                        val right = getGrayscale(safeGetPixel(bitmap, x + 1, y))
                        val top = getGrayscale(safeGetPixel(bitmap, x, y - 1))
                        val bottom = getGrayscale(safeGetPixel(bitmap, x, y + 1))

                        val laplacian = abs(4 * center - left - right - top - bottom)
                        variance += laplacian * laplacian
                        count++
                    }
                }
            }

            if (count > 0) sqrt(variance / count) else 0.0
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating blur score", e)
            BLUR_THRESHOLD + 1 // Pass on error
        }
    }

    /**
     * Calculate contrast score
     */
    private fun calculateContrastScore(bitmap: Bitmap): Double {
        return try {
            val sampleStep = maxOf(bitmap.width / 30, 15)
            var minBrightness = 255
            var maxBrightness = 0

            for (y in 0 until bitmap.height step sampleStep) {
                for (x in 0 until bitmap.width step sampleStep) {
                    val gray = getGrayscale(safeGetPixel(bitmap, x, y))
                    if (gray < minBrightness) minBrightness = gray
                    if (gray > maxBrightness) maxBrightness = gray
                }
            }

            (maxBrightness - minBrightness).toDouble()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating contrast", e)
            CONTRAST_THRESHOLD + 1 // Pass on error
        }
    }

    /**
     * Calculate green ratio (indicator of plant content)
     */
    private fun calculateGreenRatio(bitmap: Bitmap): Float {
        return try {
            val sampleStep = maxOf(bitmap.width / 40, 12)
            var greenPixelCount = 0
            var totalSamples = 0

            for (y in 0 until bitmap.height step sampleStep) {
                for (x in 0 until bitmap.width step sampleStep) {
                    val pixel = safeGetPixel(bitmap, x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    // Check if pixel is green-dominant (plant indicator)
                    // Green should be higher than both red and blue
                    if (g > r && g > b && g > 50) {
                        greenPixelCount++
                    }
                    // Also count brown/green mixed (diseased plant matter)
                    else if (g > 40 && r > 40 && g >= r * 0.7 && g > b) {
                        greenPixelCount++
                    }
                    totalSamples++
                }
            }

            if (totalSamples == 0) 0f else greenPixelCount.toFloat() / totalSamples
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating green ratio", e)
            GREEN_RATIO_THRESHOLD + 0.01f // Pass on error
        }
    }

    /**
     * Calculate average brightness
     */
    private fun calculateAverageBrightness(bitmap: Bitmap): Int {
        return try {
            val sampleStep = maxOf(bitmap.width / 30, 15)
            var totalBrightness = 0L
            var count = 0

            for (y in 0 until bitmap.height step sampleStep) {
                for (x in 0 until bitmap.width step sampleStep) {
                    totalBrightness += getGrayscale(safeGetPixel(bitmap, x, y))
                    count++
                }
            }

            if (count > 0) (totalBrightness / count).toInt() else 128
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating brightness", e)
            128 // Middle value on error
        }
    }

    /**
     * Convert pixel to grayscale value
     */
    private fun getGrayscale(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}

