package com.example.intelli_pest.domain.model

/**
 * Represents a pest detection result
 */
data class DetectionResult(
    val pestType: PestType,
    val confidence: Float,
    val imageUri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String,
    val processingTimeMs: Long = 0,
    val allPredictions: List<PestPrediction> = emptyList()
) {
    /**
     * Check if the confidence meets the minimum threshold
     */
    fun meetsThreshold(minConfidence: Float = 0.7f): Boolean {
        return confidence >= minConfidence
    }

    /**
     * Get confidence as percentage string
     */
    fun getConfidencePercentage(): String {
        return String.format("%.1f%%", confidence * 100)
    }
}

/**
 * Represents a single prediction with pest type and confidence
 */
data class PestPrediction(
    val pestType: PestType,
    val confidence: Float
) {
    fun getConfidencePercentage(): String {
        return String.format("%.1f%%", confidence * 100)
    }
}

