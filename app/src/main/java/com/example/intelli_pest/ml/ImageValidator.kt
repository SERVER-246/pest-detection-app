package com.example.intelli_pest.ml

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build

/**
 * Validates if an image is suitable for pest detection
 * Filters out unrelated images (non-sugarcane crops)
 */
class ImageValidator {

    /**
     * Convert hardware bitmap to software bitmap for pixel access
     * Returns the original bitmap if conversion fails
     */
    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                bitmap.config == Bitmap.Config.HARDWARE) {
                // Convert HARDWARE bitmap to ARGB_8888 for pixel access
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else if (bitmap.config == null) {
                // Handle null config case
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            // If conversion fails, return original
            bitmap
        }
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
            Color.GRAY
        }
    }

    /**
     * Check if image appears to be a sugarcane crop
     * Returns true by default on any error to avoid blocking valid images
     */
    fun isValidSugarcaneCropImage(bitmap: Bitmap): Boolean {
        return try {
            // Always convert to software bitmap first
            val softwareBitmap = ensureSoftwareBitmap(bitmap)

            // If we still can't access pixels, just accept the image
            if (!canAccessPixels(softwareBitmap)) {
                return true
            }

            // Multiple validation checks with safe pixel access
            val hasGreenContent = checkGreenContentSafe(softwareBitmap)
            val hasProperColorDistribution = checkColorDistributionSafe(softwareBitmap)
            val hasTextureVariation = checkTextureVariationSafe(softwareBitmap)
            val qualityCheck = checkBasicQualitySafe(softwareBitmap)

            // Image is valid if it passes at least 2 checks
            val checksPassedCount = listOf(
                hasGreenContent,
                hasProperColorDistribution,
                hasTextureVariation,
                qualityCheck
            ).count { it }

            checksPassedCount >= 2
        } catch (e: Exception) {
            // On ANY error, accept the image and let the model decide
            true
        }
    }

    /**
     * Check if we can actually access pixels on this bitmap
     */
    private fun canAccessPixels(bitmap: Bitmap): Boolean {
        return try {
            // Try to access a single pixel
            if (bitmap.width > 0 && bitmap.height > 0) {
                bitmap.getPixel(0, 0)
                true
            } else {
                false
            }
        } catch (e: Exception) {
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

                    if (g > r * 0.8 && g > b * 0.8 && g > 30) {
                        greenPixelCount++
                    }
                    totalSamples++
                }
            }

            if (totalSamples == 0) return true
            val greenPercentage = greenPixelCount.toFloat() / totalSamples
            greenPercentage >= 0.10f // Very lenient: 10%
        } catch (e: Exception) {
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
            // Just check it's not completely black or white
            val avgBrightness = (redSum + greenSum + blueSum) / (3 * totalSamples)
            avgBrightness in 10..245
        } catch (e: Exception) {
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

            stdDev > 5.0 // Very lenient threshold
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Safe version of basic quality check
     */
    private fun checkBasicQualitySafe(bitmap: Bitmap): Boolean {
        return try {
            // Minimum resolution check
            if (bitmap.width < 50 || bitmap.height < 50) {
                return false
            }

            // Check center brightness
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            val centerPixel = safeGetPixel(bitmap, centerX, centerY)
            val brightness = (Color.red(centerPixel) + Color.green(centerPixel) + Color.blue(centerPixel)) / 3

            brightness in 5..250
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Get validation confidence score (0.0 to 1.0)
     */
    fun getValidationConfidence(bitmap: Bitmap): Float {
        return try {
            val softwareBitmap = ensureSoftwareBitmap(bitmap)

            if (!canAccessPixels(softwareBitmap)) {
                return 0.75f // Return moderate confidence if can't access pixels
            }

            val checks = listOf(
                checkGreenContentSafe(softwareBitmap),
                checkColorDistributionSafe(softwareBitmap),
                checkTextureVariationSafe(softwareBitmap),
                checkBasicQualitySafe(softwareBitmap)
            )

            checks.count { it }.toFloat() / checks.size
        } catch (e: Exception) {
            0.75f
        }
    }
}

