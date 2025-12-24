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
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

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
        private const val MIN_CONFIDENCE_FOR_VALID_IMAGE = 0.3f  // Minimum confidence to consider image valid
        private const val MAX_ENTROPY_FOR_VALID_IMAGE = 2.0f    // Maximum entropy (randomness) in predictions
    }

    override suspend fun detectPest(bitmap: Bitmap, modelId: String): Resource<DetectionResult> {
        AppLogger.logAction("Repository", "DetectPest_Called", "Model: $modelId, Bitmap: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}")
        Log.d(TAG, "detectPest() start | model=$modelId | bitmap=${bitmap.width}x${bitmap.height} config=${bitmap.config}")
        return try {
            // Step 1: Validate image first
            AppLogger.logInfo("Repository", "Validating_Image", "Checking if image is suitable for pest detection")
            val validationResult = validateImage(bitmap)
            if (validationResult is Resource.Success && validationResult.data == false) {
                AppLogger.logWarning("Repository", "Image_Validation_Failed", "Image does not appear to be a sugarcane crop")
                // Don't block, just log - we'll check model confidence later
            }

            val modelPath = resolveModelPath(modelId)
            if (modelPath == null) {
                AppLogger.logError("Repository", "Model_Not_Available", "Model '$modelId' not available - download required")
                return Resource.Error("Model '$modelId' not available. Download required.")
            }
            AppLogger.logInfo("Repository", "Model_Path_Resolved", "Path: $modelPath")
            Log.d(TAG, "Model resolved | path=$modelPath")

            if (!ensureModelLoaded(modelPath)) {
                AppLogger.logError("Repository", "Model_Load_Failed", "Failed to load model from: $modelPath")
                Log.e(TAG, "Model load failed | path=$modelPath")
                return Resource.Error("Failed to load model from $modelPath")
            }
            AppLogger.logResponse("Repository", "Model_Loaded", "Model successfully loaded: $modelPath")

            val threshold = preferencesManager.getConfidenceThreshold().firstOrNull() ?: 0.7f
            Log.d(TAG, "Confidence threshold=$threshold")

            // Step 2: Run inference
            val result = inferenceEngine.detectPest(bitmap, modelId, threshold)
            if (result == null) {
                AppLogger.logError("Repository", "Inference_Null", "Inference returned null result")
                Log.e(TAG, "Inference returned null result")
                return Resource.Error("Detection pipeline returned empty result")
            }

            // Step 3: Check if image is unrelated based on model predictions
            val unrelatedCheck = isUnrelatedImage(result)
            if (unrelatedCheck.first) {
                AppLogger.logWarning("Repository", "Unrelated_Image_Detected", unrelatedCheck.second)
                return Resource.Error("This image doesn't appear to be a sugarcane crop. ${unrelatedCheck.second}")
            }

            val imageUri = saveImage(bitmap)
            val updatedResult = result.copy(imageUri = imageUri)
            AppLogger.logResponse("Repository", "Detection_Success", "Pest: ${updatedResult.pestType.displayName}, Confidence: ${updatedResult.getConfidencePercentage()}")
            Log.d(TAG, "Detection success | pest=${updatedResult.pestType} | confidence=${updatedResult.confidence} | uri=$imageUri")

            if (updatedResult.meetsThreshold(threshold)) {
                saveDetectionResult(updatedResult)
            } else {
                Log.w(TAG, "Detection below threshold; skipping history save")
            }

            Resource.Success(updatedResult)
        } catch (e: Exception) {
            AppLogger.logError("Repository", "DetectPest_Error", e, "Exception during detection")
            Log.e(TAG, "detectPest() error", e)
            Resource.Error("Detection error: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    /**
     * Check if the image is unrelated to sugarcane crops based on model predictions
     * Returns Pair(isUnrelated, reason)
     */
    private fun isUnrelatedImage(result: DetectionResult): Pair<Boolean, String> {
        val allPredictions = result.allPredictions

        // If no predictions, likely an issue
        if (allPredictions.isEmpty()) {
            return Pair(true, "No predictions available")
        }

        // Get top prediction confidence
        val topConfidence = result.confidence

        // Check 1: If top confidence is very low, image might be unrelated
        if (topConfidence < MIN_CONFIDENCE_FOR_VALID_IMAGE) {
            AppLogger.logDebug("Repository", "Low_Confidence_Check", "Top confidence $topConfidence < $MIN_CONFIDENCE_FOR_VALID_IMAGE")
            return Pair(true, "Model confidence too low (${String.format(Locale.US, "%.1f%%", topConfidence * 100)}). Please capture a clearer image of sugarcane.")
        }

        // Check 2: Calculate entropy of predictions - high entropy means uncertain/random predictions
        val entropy = calculateEntropy(allPredictions.map { it.confidence })
        AppLogger.logDebug("Repository", "Entropy_Check", "Prediction entropy: $entropy")

        if (entropy > MAX_ENTROPY_FOR_VALID_IMAGE && topConfidence < 0.5f) {
            return Pair(true, "Image content is unclear. Please capture a focused image of sugarcane crop.")
        }

        // Check 3: If all predictions have very similar (uniform) confidence, image is likely unrelated
        val confidences = allPredictions.map { it.confidence }
        val maxConf = confidences.maxOrNull() ?: 0f
        val minConf = confidences.minOrNull() ?: 0f
        val range = maxConf - minConf

        // Only check uniform distribution if confidence is still relatively low
        if (range < 0.1f && maxConf < 0.4f) {
            AppLogger.logDebug("Repository", "Uniform_Distribution_Check", "Uniform distribution detected, range: $range")
            return Pair(true, "Cannot identify sugarcane crop in this image. Please try a different image.")
        }

        return Pair(false, "")
    }

    /**
     * Calculate Shannon entropy of confidence distribution
     */
    private fun calculateEntropy(confidences: List<Float>): Float {
        val sum = confidences.sum()
        if (sum == 0f) return 0f

        return confidences
            .map { it / sum }
            .filter { it > 0 }
            .map { -it * kotlin.math.ln(it) }
            .sum()
    }

    private suspend fun resolveModelPath(modelId: String): String? {
        Log.d(TAG, "======= RESOLVE MODEL PATH START =======")
        Log.d(TAG, "Model ID: $modelId")

        // Get current runtime to determine which model file to use
        val runtime = preferencesManager.getMLRuntimeSync()
        val path = when (runtime) {
            PreferencesManager.MLRuntime.ONNX -> "models/student_model.onnx"
            PreferencesManager.MLRuntime.TFLITE -> "models/student_model.pt" // TFLITE enum repurposed for PyTorch
        }

        Log.d(TAG, "Runtime: ${runtime.value}, Model path: $path")

        // Verify model exists
        return try {
            context.assets.open(path).use { it.available() }
            Log.d(TAG, "Model found: $path")
            path
        } catch (e: Exception) {
            Log.e(TAG, "Model not found: $path - ${e.message}")
            null
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
