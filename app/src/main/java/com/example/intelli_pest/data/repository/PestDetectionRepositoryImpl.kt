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

        // Configurable thresholds for detection quality
        const val MIN_CONFIDENCE_THRESHOLD = 0.50f  // Minimum confidence to accept detection (50%)
        const val HIGH_CONFIDENCE_THRESHOLD = 0.70f // High confidence threshold for reliable detection
        const val MAX_ENTROPY_THRESHOLD = 1.8f      // Maximum entropy - reject if predictions are too uncertain
        const val LOW_CONFIDENCE_ENTROPY_THRESHOLD = 0.50f // If confidence below this AND entropy high, reject
    }

    override suspend fun detectPest(bitmap: Bitmap, modelId: String): Resource<DetectionResult> {
        AppLogger.logAction("Repository", "DetectPest_Called", "Model: $modelId, Bitmap: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}")
        Log.d(TAG, "detectPest() start | model=$modelId | bitmap=${bitmap.width}x${bitmap.height} config=${bitmap.config}")
        return try {
            // Step 1: Validate image quality and content
            AppLogger.logInfo("Repository", "Validating_Image", "Checking if image is suitable for pest detection")
            val validationResult = inferenceEngine.validateImageWithDetails(bitmap)

            if (!validationResult.isValid) {
                val reason = validationResult.reason ?: "Image quality insufficient for detection"
                AppLogger.logWarning("Repository", "Image_Validation_Failed", reason)
                return Resource.Error("Image validation failed: $reason")
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
     * Uses confidence and entropy thresholds to filter unreliable detections
     * Returns Pair(isUnrelated, reason)
     */
    private fun isUnrelatedImage(result: DetectionResult): Pair<Boolean, String> {
        val allPredictions = result.allPredictions

        // If no predictions, likely an issue
        if (allPredictions.isEmpty()) {
            AppLogger.logWarning("Repository", "No_Predictions", "No predictions available from model")
            return Pair(true, "No predictions available")
        }

        // Get top prediction confidence
        val topConfidence = result.confidence

        // Calculate entropy of predictions
        val entropy = calculateEntropy(allPredictions.map { it.confidence })

        AppLogger.logDebug("Repository", "Confidence_Entropy_Check",
            "Confidence: ${String.format(Locale.US, "%.1f%%", topConfidence * 100)}, Entropy: ${String.format(Locale.US, "%.2f", entropy)}")

        // Check 1: Absolute minimum confidence threshold
        if (topConfidence < MIN_CONFIDENCE_THRESHOLD) {
            val reason = "Detection confidence too low (${String.format(Locale.US, "%.1f%%", topConfidence * 100)}). " +
                        "Please capture a clearer, close-up image of the sugarcane damage."
            AppLogger.logWarning("Repository", "Low_Confidence_Rejected",
                "Confidence ${"%.1f%%".format(topConfidence * 100)} < ${MIN_CONFIDENCE_THRESHOLD * 100}%")
            return Pair(true, reason)
        }

        // Check 2: High entropy with moderate confidence indicates uncertainty
        if (entropy > MAX_ENTROPY_THRESHOLD && topConfidence < HIGH_CONFIDENCE_THRESHOLD) {
            val reason = "Detection is uncertain (entropy: ${String.format(Locale.US, "%.2f", entropy)}). " +
                        "Please try with a clearer image showing the pest damage more prominently."
            AppLogger.logWarning("Repository", "High_Entropy_Rejected",
                "Entropy ${"%.2f".format(entropy)} > $MAX_ENTROPY_THRESHOLD with low confidence")
            return Pair(true, reason)
        }

        // Check 3: Very low confidence combined with high entropy - definitely unrelated
        if (topConfidence < LOW_CONFIDENCE_ENTROPY_THRESHOLD && entropy > MAX_ENTROPY_THRESHOLD * 0.9) {
            val reason = "Cannot identify pest damage in this image. Please ensure the image shows sugarcane crop."
            AppLogger.logWarning("Repository", "Unrelated_Image",
                "Low confidence + high entropy indicates unrelated image")
            return Pair(true, reason)
        }

        // Check 4: Uniform distribution check - all predictions similar means model is guessing
        val confidences = allPredictions.map { it.confidence }
        val maxConf = confidences.maxOrNull() ?: 0f
        val minConf = confidences.minOrNull() ?: 0f
        val range = maxConf - minConf

        if (range < 0.15f && maxConf < 0.4f) {
            AppLogger.logWarning("Repository", "Uniform_Distribution",
                "Predictions are uniformly distributed (range: ${"%.2f".format(range)})")
            return Pair(true, "Cannot identify sugarcane crop in this image. The model cannot distinguish features.")
        }

        // Passed all checks
        AppLogger.logInfo("Repository", "Detection_Accepted",
            "Detection accepted: ${result.pestType.displayName} at ${String.format(Locale.US, "%.1f%%", topConfidence * 100)}")
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
