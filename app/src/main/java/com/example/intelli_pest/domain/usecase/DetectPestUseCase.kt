package com.example.intelli_pest.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import com.example.intelli_pest.util.BitmapUtils

/**
 * Use case for detecting pests in images
 */
class DetectPestUseCase(
    private val repository: PestDetectionRepository
) {
    private val TAG = "DetectPestUseCase"

    suspend operator fun invoke(
        bitmap: Bitmap,
        modelId: String = "super_ensemble"
    ): Resource<DetectionResult> {
        Log.d(TAG, "invoke() called | model=$modelId | bitmap=${bitmap.width}x${bitmap.height}, config=${bitmap.config}")

        return runCatching {
            val softwareBitmap = BitmapUtils.toSoftwareBitmap(bitmap)
            Log.d(TAG, "Bitmap converted to software config=${softwareBitmap.config}")

            repository.detectPest(softwareBitmap, modelId).also { result ->
                when (result) {
                    is Resource.Success -> Log.d(TAG, "Detection success | pest=${result.data.pestType} | confidence=${result.data.confidence}")
                    is Resource.Error -> Log.e(TAG, "Detection failed in repository | message=${result.message}", result.exception)
                    is Resource.Loading -> Log.d(TAG, "Detection still loading")
                }
            }
        }.getOrElse { throwable ->
            Log.e(TAG, "invoke() threw", throwable)
            Resource.Error(
                message = "Detection failed: ${throwable.localizedMessage ?: "Unknown error"}",
                exception = throwable as? Exception ?: Exception(throwable)
            )
        }
    }
}
