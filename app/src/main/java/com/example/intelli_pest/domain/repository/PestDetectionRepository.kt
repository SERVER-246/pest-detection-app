package com.example.intelli_pest.domain.repository

import android.graphics.Bitmap
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.ModelInfo
import com.example.intelli_pest.domain.model.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for pest detection operations
 */
interface PestDetectionRepository {
    /**
     * Detect pest in the given image
     */
    suspend fun detectPest(
        bitmap: Bitmap,
        modelId: String = "super_ensemble"
    ): Resource<DetectionResult>

    /**
     * Validate if image is suitable for pest detection
     */
    suspend fun validateImage(bitmap: Bitmap): Resource<Boolean>

    /**
     * Get all available models
     */
    fun getAvailableModels(): Flow<List<ModelInfo>>

    /**
     * Download a model
     */
    suspend fun downloadModel(modelId: String): Flow<Resource<Float>>

    /**
     * Delete a downloaded model
     */
    suspend fun deleteModel(modelId: String): Resource<Boolean>

    /**
     * Get detection history
     */
    fun getDetectionHistory(): Flow<List<DetectionResult>>

    /**
     * Save detection result to history
     */
    suspend fun saveDetectionResult(result: DetectionResult): Resource<Unit>

    /**
     * Clear detection history
     */
    suspend fun clearHistory(): Resource<Unit>

    /**
     * Get current confidence threshold
     */
    suspend fun getConfidenceThreshold(): Float

    /**
     * Set confidence threshold
     */
    suspend fun setConfidenceThreshold(threshold: Float): Resource<Unit>
}

