package com.example.intelli_pest.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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

    companion object {
        private const val TAG = "PestDetectionRepo"
    }

    override suspend fun detectPest(bitmap: Bitmap, modelId: String): Resource<DetectionResult> {
        Log.d(TAG, "detectPest() start | model=$modelId | bitmap=${bitmap.width}x${bitmap.height} config=${bitmap.config}")
        return try {
            val modelPath = resolveModelPath(modelId)
                ?: return Resource.Error("Model '$modelId' not available. Download required.")
            Log.d(TAG, "Model resolved | path=$modelPath")

            if (!ensureModelLoaded(modelPath)) {
                Log.e(TAG, "Model load failed | path=$modelPath")
                return Resource.Error("Failed to load model from $modelPath")
            }

            val threshold = preferencesManager.getConfidenceThreshold().firstOrNull() ?: 0.7f
            Log.d(TAG, "Confidence threshold=$threshold")

            val result = inferenceEngine.detectPest(bitmap, modelId, threshold)
            if (result == null) {
                Log.e(TAG, "Inference returned null result")
                return Resource.Error("Detection pipeline returned empty result")
            }

            val imageUri = saveImage(bitmap)
            val updatedResult = result.copy(imageUri = imageUri)
            Log.d(TAG, "Detection success | pest=${updatedResult.pestType} | confidence=${updatedResult.confidence} | uri=$imageUri")

            if (updatedResult.meetsThreshold(threshold)) {
                saveDetectionResult(updatedResult)
            } else {
                Log.w(TAG, "Detection below threshold; skipping history save")
            }

            Resource.Success(updatedResult)
        } catch (e: Exception) {
            Log.e(TAG, "detectPest() error", e)
            Resource.Error("Detection error: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    private suspend fun resolveModelPath(modelId: String): String? {
        return when {
            modelFileManager.isModelBundled(modelId) -> modelFileManager.getBundledModelPath(modelId)
            modelFileManager.isModelDownloaded(modelId) -> modelFileManager.getModelPath(modelId)
            else -> null
        }
    }

    private suspend fun ensureModelLoaded(modelPath: String): Boolean {
        val alreadyLoaded = inferenceEngine.isModelLoaded() && inferenceEngine.getCurrentModelPath() == modelPath
        if (alreadyLoaded) return true
        Log.d(TAG, "Loading model into memory | path=$modelPath")
        return inferenceEngine.loadModel(modelPath)
    }

    override suspend fun validateImage(bitmap: Bitmap): Resource<Boolean> {
        return try {
            val isValid = inferenceEngine.validateImage(bitmap)
            val qualityGood = inferenceEngine.checkImageQuality(bitmap)
            Log.d(TAG, "validateImage() | valid=$isValid quality=$qualityGood")

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
        Log.d(TAG, "downloadModel() start | model=$modelId")
        try {
            emit(Resource.Loading)

            // TODO: Implement actual download from server
            // For now, simulate download
            val models = modelFileManager.getAllAvailableModels()
            val model = models.find { it.id == modelId }

            if (model?.downloadUrl == null) {
                Log.e(TAG, "downloadModel() missing URL | model=$modelId")
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
            Log.e(TAG, "downloadModel() error", e)
            emit(Resource.Error("Download failed: ${e.message}", e))
        }
    }

    override suspend fun deleteModel(modelId: String): Resource<Boolean> {
        return try {
            val deleted = modelFileManager.deleteModel(modelId)
            if (deleted) {
                Log.d(TAG, "deleteModel() success | model=$modelId")
                Resource.Success(true)
            } else {
                Resource.Error("Failed to delete model")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteModel() error", e)
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
            Log.d(TAG, "saveDetectionResult() success | id=${result.imageUri}")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "saveDetectionResult() error", e)
            Resource.Error("Failed to save result: ${e.message}", e)
        }
    }

    override suspend fun clearHistory(): Resource<Unit> {
        return try {
            database.detectionHistoryDao().clearAllDetections()
            Log.d(TAG, "clearHistory() success")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "clearHistory() error", e)
            Resource.Error("Failed to clear history: ${e.message}", e)
        }
    }

    override suspend fun getConfidenceThreshold(): Float {
        return preferencesManager.getConfidenceThreshold().firstOrNull() ?: 0.7f
    }

    override suspend fun setConfidenceThreshold(threshold: Float): Resource<Unit> {
        return try {
            preferencesManager.setConfidenceThreshold(threshold)
            Log.d(TAG, "setConfidenceThreshold() | value=$threshold")
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "setConfidenceThreshold() error", e)
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

            val uri = Uri.fromFile(imageFile).toString()
            Log.d(TAG, "saveImage() success | uri=$uri")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "saveImage() error", e)
            ""
        }
    }
}
