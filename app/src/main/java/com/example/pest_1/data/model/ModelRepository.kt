package com.example.pest_1.data.model

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository for managing model access from both assets and downloaded files
 */
class ModelRepository(private val context: Context) {

    private val modelDownloader = ModelDownloader(context)

    companion object {
        private const val TAG = "ModelRepository"
    }

    /**
     * Get the path to a model's directory
     * Returns path from assets or downloaded files
     */
    suspend fun getModelPath(modelInfo: ModelInfo): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (modelInfo.isDefault) {
                // Model is bundled in assets
                Log.d(TAG, "Using bundled model: ${modelInfo.id}")
                return@withContext Result.success("models/${modelInfo.id}")
            }

            // Check if model is downloaded
            val downloadedDir = File(context.filesDir, "models/${modelInfo.id}")
            val modelFile = File(downloadedDir, "model.onnx")

            if (modelFile.exists()) {
                Log.d(TAG, "Using downloaded model: ${modelInfo.id}")
                return@withContext Result.success(downloadedDir.absolutePath)
            }

            // Model not available
            Log.w(TAG, "Model not available: ${modelInfo.id}")
            Result.failure(Exception("Model not downloaded. Please download first."))

        } catch (e: Exception) {
            Log.e(TAG, "Error getting model path: ${modelInfo.id}", e)
            Result.failure(e)
        }
    }

    /**
     * Ensure model is available (download if needed)
     */
    suspend fun ensureModelAvailable(modelInfo: ModelInfo): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (modelInfo.isDefault) {
                // Bundled model - always available
                return@withContext Result.success("models/${modelInfo.id}")
            }

            // Check if already downloaded
            if (modelDownloader.isModelDownloaded(modelInfo.id)) {
                val downloadedDir = File(context.filesDir, "models/${modelInfo.id}")
                return@withContext Result.success(downloadedDir.absolutePath)
            }

            // Need to download
            Log.d(TAG, "Downloading model: ${modelInfo.id}")
            val downloadResult = modelDownloader.downloadModel(modelInfo)

            if (downloadResult.isSuccess) {
                val dir = downloadResult.getOrNull()!!
                Result.success(dir.absolutePath)
            } else {
                Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring model availability: ${modelInfo.id}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if model is available locally
     */
    fun isModelAvailable(modelInfo: ModelInfo): Boolean {
        return if (modelInfo.isDefault) {
            // Check if exists in assets
            try {
                context.assets.list("models/${modelInfo.id}")?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }
        } else {
            modelDownloader.isModelDownloaded(modelInfo.id)
        }
    }

    /**
     * Get model status
     */
    fun getModelStatus(modelInfo: ModelInfo): ModelStatus {
        return when {
            modelInfo.isDefault -> ModelStatus.Bundled
            modelDownloader.isModelDownloaded(modelInfo.id) -> ModelStatus.Downloaded
            else -> ModelStatus.NotDownloaded
        }
    }

    /**
     * Delete a downloaded model
     */
    suspend fun deleteModel(modelInfo: ModelInfo): Boolean = withContext(Dispatchers.IO) {
        if (modelInfo.isDefault) {
            Log.w(TAG, "Cannot delete bundled model: ${modelInfo.id}")
            return@withContext false
        }

        modelDownloader.deleteModel(modelInfo.id)
    }

    /**
     * Get total storage used by downloaded models
     */
    fun getStorageUsed(): Float {
        return modelDownloader.getDownloadedModelsSize()
    }

    /**
     * Get all available models with their status
     */
    fun getAllModelsWithStatus(): List<Pair<ModelInfo, ModelStatus>> {
        return ModelCatalog.models.map { model ->
            model to getModelStatus(model)
        }
    }

    /**
     * Get download progress for a model
     */
    fun getDownloadStatus() = modelDownloader.downloadStatus
}

