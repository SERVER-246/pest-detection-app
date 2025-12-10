package com.example.intelli_pest.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.intelli_pest.data.model.DetectionResultEntity
import com.example.intelli_pest.data.source.local.AppDatabase
import com.example.intelli_pest.data.source.local.ModelFileManager
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.ModelInfo
import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import com.example.intelli_pest.ml.InferenceEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Implementation of PestDetectionRepository
 */
class PestDetectionRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager,
    private val modelFileManager: ModelFileManager,
    private val inferenceEngine: InferenceEngine
) : PestDetectionRepository {

    override suspend fun detectPest(bitmap: Bitmap, modelId: String): Resource<DetectionResult> {
        return try {
            // Get model path
            val modelPath = if (modelFileManager.isModelBundled(modelId)) {
                modelFileManager.getBundledModelPath(modelId)
            } else if (modelFileManager.isModelDownloaded(modelId)) {
                modelFileManager.getModelPath(modelId)
            } else {
                return Resource.Error("Model not available. Please download it first.")
            }

            // Load model if not already loaded
            if (!inferenceEngine.isModelLoaded() || inferenceEngine.getCurrentModelPath() != modelPath) {
                val loaded = inferenceEngine.loadModel(modelPath)
                if (!loaded) {
                    return Resource.Error("Failed to load model")
                }
            }

            // Get confidence threshold
            val threshold = preferencesManager.getConfidenceThreshold().firstOrNull() ?: 0.7f

            // Perform detection
            val result = inferenceEngine.detectPest(bitmap, modelId, threshold)

            if (result != null) {
                // Save image and update result
                val imageUri = saveImage(bitmap)
                val updatedResult = result.copy(imageUri = imageUri)

                // Save to history if confidence meets threshold
                if (updatedResult.meetsThreshold(threshold)) {
                    saveDetectionResult(updatedResult)
                }

                Resource.Success(updatedResult)
            } else {
                Resource.Error("Failed to perform detection")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Detection error: ${e.message}", e)
        }
    }

    override suspend fun validateImage(bitmap: Bitmap): Resource<Boolean> {
        return try {
            val isValid = inferenceEngine.validateImage(bitmap)
            val qualityGood = inferenceEngine.checkImageQuality(bitmap)

            if (!qualityGood) {
                // Still allow detection, just warn about quality
                Resource.Success(true)
            } else {
                Resource.Success(isValid)
            }
        } catch (e: Exception) {
            // On ANY validation error, allow the image through
            // Let the model decide if it can process it
            Resource.Success(true)
        }
    }

    override fun getAvailableModels(): Flow<List<ModelInfo>> = flow {
        val models = modelFileManager.getAllAvailableModels()
        emit(models)
    }

    override suspend fun downloadModel(modelId: String): Flow<Resource<Float>> = flow {
        try {
            emit(Resource.Loading)

            // TODO: Implement actual download from server
            // For now, simulate download
            val models = modelFileManager.getAllAvailableModels()
            val model = models.find { it.id == modelId }

            if (model?.downloadUrl == null) {
                emit(Resource.Error("Download URL not available"))
                return@flow
            }

            // Simulate download progress
            for (progress in 0..100 step 10) {
                kotlinx.coroutines.delay(200)
                emit(Resource.Success(progress / 100f))
            }

            emit(Resource.Success(1f))
        } catch (e: Exception) {
            emit(Resource.Error("Download failed: ${e.message}", e))
        }
    }

    override suspend fun deleteModel(modelId: String): Resource<Boolean> {
        return try {
            val deleted = modelFileManager.deleteModel(modelId)
            if (deleted) {
                Resource.Success(true)
            } else {
                Resource.Error("Failed to delete model")
            }
        } catch (e: Exception) {
            Resource.Error("Error deleting model: ${e.message}", e)
        }
    }

    override fun getDetectionHistory(): Flow<List<DetectionResult>> {
        return database.detectionHistoryDao().getAllDetections()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveDetectionResult(result: DetectionResult): Resource<Unit> {
        return try {
            val entity = DetectionResultEntity.fromDomain(result)
            database.detectionHistoryDao().insertDetection(entity)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save result: ${e.message}", e)
        }
    }

    override suspend fun clearHistory(): Resource<Unit> {
        return try {
            database.detectionHistoryDao().clearAllDetections()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to clear history: ${e.message}", e)
        }
    }

    override suspend fun getConfidenceThreshold(): Float {
        return preferencesManager.getConfidenceThreshold().firstOrNull() ?: 0.7f
    }

    override suspend fun setConfidenceThreshold(threshold: Float): Resource<Unit> {
        return try {
            preferencesManager.setConfidenceThreshold(threshold)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to set threshold: ${e.message}", e)
        }
    }

    /**
     * Save bitmap to internal storage and return URI
     */
    private suspend fun saveImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, "detection_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val imageFile = File(imagesDir, "detection_${System.currentTimeMillis()}.jpg")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            Uri.fromFile(imageFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}

