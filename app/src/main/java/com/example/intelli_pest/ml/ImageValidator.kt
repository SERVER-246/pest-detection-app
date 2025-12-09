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
     */
    private fun ensureSoftwareBitmap(bitmap: Bitmap): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                   bitmap.config == Bitmap.Config.HARDWARE) {
            // Convert HARDWARE bitmap to ARGB_8888 for pixel access
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }
    }

    /**
     * Check if image appears to be a sugarcane crop
     */
    fun isValidSugarcaneCropImage(bitmap: Bitmap): Boolean {
        return try {
            val softwareBitmap = ensureSoftwareBitmap(bitmap)

            // Multiple validation checks
            val hasGreenContent = checkGreenContent(softwareBitmap)
            val hasProperColorDistribution = checkColorDistribution(softwareBitmap)
            val hasTextureVariation = checkTextureVariation(softwareBitmap)
            val qualityCheck = checkBasicQuality(softwareBitmap)

            // Image is valid if it passes most checks
            val checksPassedCount = listOf(
                hasGreenContent,
                hasProperColorDistribution,
                hasTextureVariation,
                qualityCheck
            ).count { it }

            checksPassedCount >= 2 // Reduced threshold for better acceptance
        } catch (e: Exception) {
            // If validation fails, accept the image (let the model decide)
            true
        }
    }

    /**
     * Check if image has sufficient green content (plant material)
     */
    private fun checkGreenContent(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 20 // Sample every 20th pixel for performance
            var greenPixelCount = 0
            var totalSamples = 0

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    // Green-ish pixel detection (more lenient)
                    if (g > r * 0.8 && g > b * 0.8 && g > 30) {
                        greenPixelCount++
                    }
                    totalSamples++
                }
            }

            if (totalSamples == 0) return true

            val greenPercentage = greenPixelCount.toFloat() / totalSamples
            // At least 15% of sampled pixels should be green-ish (reduced from 20%)
            greenPercentage >= 0.15f
        } catch (e: Exception) {
            true // Accept on error
        }
    }

    /**
     * Check if color distribution is suitable for crop images
     */
    private fun checkColorDistribution(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 20
            var redSum = 0L
            var greenSum = 0L
            var blueSum = 0L
            var totalSamples = 0

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    redSum += Color.red(pixel)
                    greenSum += Color.green(pixel)
                    blueSum += Color.blue(pixel)
                    totalSamples++
                }
            }

            if (totalSamples == 0) return true

            val avgRed = redSum / totalSamples
            val avgGreen = greenSum / totalSamples
            val avgBlue = blueSum / totalSamples

            // More lenient check - just ensure it's not obviously wrong
            avgGreen > 30 || avgRed > 30 || avgBlue > 30
        } catch (e: Exception) {
            true // Accept on error
        }
    }

    /**
     * Check if image has texture variation (not blank or uniform)
     */
    private fun checkTextureVariation(bitmap: Bitmap): Boolean {
        return try {
            val sampleSize = 30
            val brightnessValues = mutableListOf<Int>()

            for (y in 0 until bitmap.height step sampleSize) {
                for (x in 0 until bitmap.width step sampleSize) {
                    val pixel = bitmap.getPixel(x, y)
                    val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                    brightnessValues.add(brightness)
                }
            }

            if (brightnessValues.isEmpty()) return true

            // Calculate standard deviation
            val mean = brightnessValues.average()
            val variance = brightnessValues.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)

            // Image should have some variation (reduced threshold)
            stdDev > 10.0
        } catch (e: Exception) {
            true // Accept on error
        }
    }

    /**
     * Basic quality checks
     */
    private fun checkBasicQuality(bitmap: Bitmap): Boolean {
        return try {
            // Minimum resolution
            if (bitmap.width < 50 || bitmap.height < 50) {
                return false
            }

            // Check if image is not completely dark or bright
            val centerX = bitmap.width / 2
            val centerY = bitmap.height / 2
            val centerPixel = bitmap.getPixel(centerX, centerY)
            val brightness = (Color.red(centerPixel) + Color.green(centerPixel) + Color.blue(centerPixel)) / 3

            brightness in 10..245
        } catch (e: Exception) {
            true // Accept on error
        }
    }

    /**
     * Get validation confidence score (0.0 to 1.0)
     */
    fun getValidationConfidence(bitmap: Bitmap): Float {
        return try {
            val softwareBitmap = ensureSoftwareBitmap(bitmap)

            val checks = listOf(
                checkGreenContent(softwareBitmap),
                checkColorDistribution(softwareBitmap),
                checkTextureVariation(softwareBitmap),
                checkBasicQuality(softwareBitmap)
            )

            checks.count { it }.toFloat() / checks.size
        } catch (e: Exception) {
            0.75f // Return moderate confidence on error
        }
    }
}

