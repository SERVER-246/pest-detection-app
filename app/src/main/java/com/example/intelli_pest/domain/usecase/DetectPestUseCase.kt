package com.example.intelli_pest.domain.usecase

import android.graphics.Bitmap
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.repository.PestDetectionRepository

/**
 * Use case for detecting pests in images
 */
class DetectPestUseCase(
    private val repository: PestDetectionRepository
) {
    suspend operator fun invoke(
        bitmap: Bitmap,
        modelId: String = "super_ensemble"
    ): Resource<DetectionResult> {
        // Try validation but don't block on failure
        val validationResult = try {
            repository.validateImage(bitmap)
        } catch (e: Exception) {
            // If validation crashes, proceed anyway
            Resource.Success(true)
        }

        return when (validationResult) {
            is Resource.Success -> {
                // Proceed with detection regardless of validation result
                // The model will handle invalid images appropriately
                try {
                    repository.detectPest(bitmap, modelId)
                } catch (e: Exception) {
                    Resource.Error("Detection failed: ${e.message}")
                }
            }
            is Resource.Error -> {
                // Validation had an error, but try detection anyway
                try {
                    repository.detectPest(bitmap, modelId)
                } catch (e: Exception) {
                    Resource.Error("Detection failed: ${e.message}")
                }
            }
            is Resource.Loading -> Resource.Loading
        }
    }
}

