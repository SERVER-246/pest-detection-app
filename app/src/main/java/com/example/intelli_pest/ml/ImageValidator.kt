package com.example.intelli_pest.ml

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

/**
 * Validates if an image is suitable for pest detection
 * Filters out unrelated images (non-sugarcane crops)
 */
class ImageValidator {

    /**
     * Check if image appears to be a sugarcane crop
     */
    fun isValidSugarcaneCropImage(bitmap: Bitmap): Boolean {
        // Multiple validation checks
        val hasGreenContent = checkGreenContent(bitmap)
        val hasProperColorDistribution = checkColorDistribution(bitmap)
        val hasTextureVariation = checkTextureVariation(bitmap)
        val qualityCheck = checkBasicQuality(bitmap)

        // Image is valid if it passes most checks
        val checksPassedCount = listOf(
            hasGreenContent,
            hasProperColorDistribution,
            hasTextureVariation,
            qualityCheck
        ).count { it }

        return checksPassedCount >= 3
    }

    /**
     * Check if image has sufficient green content (plant material)
     */
    private fun checkGreenContent(bitmap: Bitmap): Boolean {
        val sampleSize = 20 // Sample every 20th pixel for performance
        var greenPixelCount = 0
        var totalSamples = 0

        for (y in 0 until bitmap.height step sampleSize) {
            for (x in 0 until bitmap.width step sampleSize) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                // Green-ish pixel detection
                if (g > r && g > b && g > 40) {
                    greenPixelCount++
                }
                totalSamples++
            }
        }

        val greenPercentage = greenPixelCount.toFloat() / totalSamples
        // At least 20% of sampled pixels should be green-ish
        return greenPercentage >= 0.20f
    }

    /**
     * Check if color distribution is suitable for crop images
     */
    private fun checkColorDistribution(bitmap: Bitmap): Boolean {
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

        val avgRed = redSum / totalSamples
        val avgGreen = greenSum / totalSamples
        val avgBlue = blueSum / totalSamples

        // For crop images, green should be reasonably high
        // and not overwhelmed by red or blue
        return avgGreen > 50 && avgGreen > avgRed * 0.7
    }

    /**
     * Check if image has texture variation (not blank or uniform)
     */
    private fun checkTextureVariation(bitmap: Bitmap): Boolean {
        val sampleSize = 30
        val brightnessValues = mutableListOf<Int>()

        for (y in 0 until bitmap.height step sampleSize) {
            for (x in 0 until bitmap.width step sampleSize) {
                val pixel = bitmap.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                brightnessValues.add(brightness)
            }
        }

        if (brightnessValues.isEmpty()) return false

        // Calculate standard deviation
        val mean = brightnessValues.average()
        val variance = brightnessValues.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)

        // Image should have some variation (not flat/uniform)
        return stdDev > 15.0
    }

    /**
     * Basic quality checks
     */
    private fun checkBasicQuality(bitmap: Bitmap): Boolean {
        // Minimum resolution
        if (bitmap.width < 100 || bitmap.height < 100) {
            return false
        }

        // Check if image is not completely dark or bright
        val centerPixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        val brightness = (Color.red(centerPixel) + Color.green(centerPixel) + Color.blue(centerPixel)) / 3

        return brightness in 20..235
    }

    /**
     * Get validation confidence score (0.0 to 1.0)
     */
    fun getValidationConfidence(bitmap: Bitmap): Float {
        val checks = listOf(
            checkGreenContent(bitmap),
            checkColorDistribution(bitmap),
            checkTextureVariation(bitmap),
            checkBasicQuality(bitmap)
        )

        return checks.count { it }.toFloat() / checks.size
    }
}

