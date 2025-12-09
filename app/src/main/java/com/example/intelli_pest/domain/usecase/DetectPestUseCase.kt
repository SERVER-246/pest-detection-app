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
        // First validate the image
        return when (val validationResult = repository.validateImage(bitmap)) {
            is Resource.Success -> {
                if (validationResult.data) {
                    // Image is valid, proceed with detection
                    repository.detectPest(bitmap, modelId)
                } else {
                    Resource.Error("Image does not appear to be a sugarcane crop image. Please capture a clear image of the crop.")
                }
            }
            is Resource.Error -> {
                Resource.Error("Failed to validate image: ${validationResult.message}")
            }
            is Resource.Loading -> Resource.Loading
        }
    }
}

