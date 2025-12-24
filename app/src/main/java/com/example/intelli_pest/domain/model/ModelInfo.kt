package com.example.intelli_pest.domain.model

import java.util.Locale

/**
 * Represents information about an AI model
 * Supports both TFLite and ONNX runtime formats
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
    val lastUpdated: Long = System.currentTimeMillis(),
    // Runtime-specific availability
    val hasTFLite: Boolean = true,
    val hasONNX: Boolean = false,
    val tfliteBundled: Boolean = false,
    val onnxBundled: Boolean = false
) {
    /**
     * Get accuracy as percentage string
     */
    fun getAccuracyPercentage(): String {
        return String.format(Locale.US, "%.1f%%", accuracy * 100)
    }

    /**
     * Get size formatted string
     */
    fun getSizeFormatted(): String {
        return String.format(Locale.US, "%.1f MB", sizeInMb)
    }

    /**
     * Check if model is available for use (any runtime)
     */
    fun isAvailable(): Boolean {
        return isDownloaded || isBundled
    }

    /**
     * Check if model is available for TFLite runtime
     */
    fun isAvailableForTFLite(): Boolean {
        return hasTFLite && (tfliteBundled || isDownloaded)
    }

    /**
     * Check if model is available for ONNX runtime
     */
    fun isAvailableForONNX(): Boolean {
        return hasONNX && (onnxBundled || isDownloaded)
    }

    /**
     * Get runtime availability description
     */
    fun getRuntimeAvailability(): String {
        val runtimes = mutableListOf<String>()
        if (hasTFLite) runtimes.add(if (tfliteBundled) "TFLite ✓" else "TFLite")
        if (hasONNX) runtimes.add(if (onnxBundled) "ONNX ✓" else "ONNX")
        return runtimes.joinToString(" | ")
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

