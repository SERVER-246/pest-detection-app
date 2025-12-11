package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import com.example.intelli_pest.domain.model.DetectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main inference engine that manages model loading and prediction
 */
class InferenceEngine(
    private val context: Context
) {
    private val imagePreprocessor = ImagePreprocessor()
    private val tfliteModelWrapper = TFLiteModelWrapper(context, imagePreprocessor)
    private val imageValidator = ImageValidator()

    /**
     * Load a model for inference
     */
    suspend fun loadModel(modelPath: String): Boolean {
        return tfliteModelWrapper.initializeModel(modelPath)
    }

    /**
     * Perform pest detection on an image
     */
    suspend fun detectPest(
        bitmap: Bitmap,
        modelId: String,
        confidenceThreshold: Float = 0.7f
    ): DetectionResult? = withContext(Dispatchers.Default) {
        try {
            val startTime = System.currentTimeMillis()

            // Run inference
            val predictions = tfliteModelWrapper.runInference(bitmap) ?: return@withContext null

            // Get top prediction
            val topPrediction = predictions
                .sortedByDescending { it.confidence }
                .firstOrNull { it.confidence >= confidenceThreshold }
                ?: return@withContext null

            val processingTime = System.currentTimeMillis() - startTime

            // Create detection result
            DetectionResult(
                pestType = topPrediction.pestType,
                confidence = topPrediction.confidence,
                imageUri = "", // Will be set by repository
                modelUsed = modelId,
                processingTimeMs = processingTime,
                allPredictions = predictions
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Validate if image is suitable for pest detection
     */
    suspend fun validateImage(bitmap: Bitmap): Boolean = withContext(Dispatchers.Default) {
        imageValidator.isValidSugarcaneCropImage(bitmap)
    }

    /**
     * Check if image quality is sufficient
     */
    fun checkImageQuality(bitmap: Bitmap): Boolean {
        return imagePreprocessor.isImageQualitySufficient(bitmap)
    }

    /**
     * Release resources
     */
    fun release() {
        tfliteModelWrapper.closeSession()
    }

    /**
     * Check if model is loaded
     */
    fun isModelLoaded(): Boolean {
        return tfliteModelWrapper.isModelLoaded()
    }

    /**
     * Get current model path
     */
    fun getCurrentModelPath(): String? {
        return tfliteModelWrapper.getCurrentModelPath()
    }
}
