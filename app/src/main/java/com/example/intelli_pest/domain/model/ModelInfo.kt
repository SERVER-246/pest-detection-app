package com.example.intelli_pest.domain.model

/**
 * Represents information about an AI model
 */
data class ModelInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val description: String,
    val accuracy: Float,
    val inferenceSpeedMs: Int,
    val sizeInMb: Float,
    val isDownloaded: Boolean = false,
    val isBundled: Boolean = false,
    val downloadUrl: String? = null,
    val version: String = "1.0.0",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Get accuracy as percentage string
     */
    fun getAccuracyPercentage(): String {
        return String.format("%.1f%%", accuracy * 100)
    }

    /**
     * Get size formatted string
     */
    fun getSizeFormatted(): String {
        return String.format("%.1f MB", sizeInMb)
    }

    /**
     * Check if model is available for use
     */
    fun isAvailable(): Boolean {
        return isDownloaded || isBundled
    }

    companion object {
        // Model IDs
        const val SUPER_ENSEMBLE = "super_ensemble"
        const val MODEL_1 = "mobilenet_v3"
        const val MODEL_2 = "efficientnet_b0"
        const val MODEL_3 = "resnet50"
        const val MODEL_4 = "densenet121"
        const val MODEL_5 = "squeezenet"
        const val MODEL_6 = "shufflenet_v2"
        const val MODEL_7 = "mnasnet"
        const val MODEL_8 = "resnext50"
        const val MODEL_9 = "wide_resnet50"
        const val MODEL_10 = "regnet_y_400mf"
        const val MODEL_11 = "efficientnet_v2_s"
    }
}

