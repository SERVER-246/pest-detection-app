package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Main inference engine that manages model loading and prediction
 * Supports both ONNX Runtime and PyTorch Mobile
 * Uses student_model.onnx or student_model.pt based on selected runtime
 */
class InferenceEngine(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val imagePreprocessor = ImagePreprocessor()
    private val onnxModelWrapper = ONNXModelWrapper(context, imagePreprocessor)
    private val pytorchModelWrapper = PyTorchModelWrapper(context, imagePreprocessor)
    private val imageValidator = ImageValidator()

    // Fixed model paths - using student_model
    private val ONNX_MODEL_PATH = "models/student_model.onnx"
    private val PYTORCH_MODEL_PATH = "models/student_model.pt"

    private var currentRuntime: PreferencesManager.MLRuntime = PreferencesManager.MLRuntime.ONNX

    companion object {
        private const val TAG = "InferenceEngine"
    }

    /**
     * Load the student model for inference based on selected runtime
     * Supports ONNX and PyTorch runtimes
     */
    suspend fun loadModel(modelPath: String? = null): Boolean {
        // Get current runtime preference
        currentRuntime = preferencesManager.getMLRuntimeSync()

        AppLogger.logInfo("InferenceEngine", "LoadModel_Start",
            "Loading model with runtime: ${currentRuntime.value}")
        Log.d(TAG, "======= LOAD MODEL START =======")
        Log.d(TAG, "Selected runtime: ${currentRuntime.value}")

        return when (currentRuntime) {
            PreferencesManager.MLRuntime.ONNX -> {
                Log.d(TAG, "Loading ONNX model: $ONNX_MODEL_PATH")
                val result = onnxModelWrapper.initializeModel(ONNX_MODEL_PATH)
                if (result) {
                    AppLogger.logResponse("InferenceEngine", "LoadModel_Success", "ONNX model loaded successfully")
                } else {
                    AppLogger.logError("InferenceEngine", "LoadModel_Failed", "Failed to load ONNX model")
                }
                result
            }
            PreferencesManager.MLRuntime.TFLITE -> {
                // TFLITE enum is repurposed for PyTorch in this simplified version
                Log.d(TAG, "Loading PyTorch model: $PYTORCH_MODEL_PATH")
                val result = pytorchModelWrapper.initializeModel(PYTORCH_MODEL_PATH)
                if (result) {
                    AppLogger.logResponse("InferenceEngine", "LoadModel_Success", "PyTorch model loaded successfully")
                } else {
                    AppLogger.logError("InferenceEngine", "LoadModel_Failed", "Failed to load PyTorch model")
                }
                result
            }
        }
    }

    /**
     * Perform pest detection on an image using the currently loaded model
     */
    suspend fun detectPest(
        bitmap: Bitmap,
        modelId: String = "student_model",
        confidenceThreshold: Float = 0.7f
    ): DetectionResult? = withContext(Dispatchers.Default) {
        try {
            val runtimeName = if (currentRuntime == PreferencesManager.MLRuntime.ONNX) "ONNX" else "PyTorch"
            AppLogger.logAction("InferenceEngine", "DetectPest_Start",
                "Starting inference, Model: $modelId, Runtime: $runtimeName, Threshold: $confidenceThreshold")
            val startTime = System.currentTimeMillis()

            // Check if model is loaded
            if (!isModelLoaded()) {
                AppLogger.logError("InferenceEngine", "Model_Not_Loaded", "Cannot run inference - model not loaded")
                return@withContext null
            }

            // Run inference based on current runtime
            AppLogger.logInfo("InferenceEngine", "Running_Inference",
                "Bitmap: ${bitmap.width}x${bitmap.height}, Runtime: $runtimeName")

            val predictions = when (currentRuntime) {
                PreferencesManager.MLRuntime.ONNX -> onnxModelWrapper.runInference(bitmap)
                PreferencesManager.MLRuntime.TFLITE -> pytorchModelWrapper.runInference(bitmap)
            }

            if (predictions == null) {
                AppLogger.logError("InferenceEngine", "Inference_Null", "Model returned null predictions")
                return@withContext null
            }

            if (predictions.isEmpty()) {
                AppLogger.logError("InferenceEngine", "Inference_Empty", "Model returned empty predictions")
                return@withContext null
            }

            AppLogger.logResponse("InferenceEngine", "Inference_Complete", "Got ${predictions.size} predictions")

            // Sort and get all predictions for analysis
            val sortedPredictions = predictions.sortedByDescending { it.confidence }

            // Log top 3 predictions
            sortedPredictions.take(3).forEachIndexed { index, pred ->
                AppLogger.logDebug("InferenceEngine", "Prediction_$index",
                    "${pred.pestType.displayName}: ${String.format(Locale.US, "%.2f%%", pred.confidence * 100)}")
            }

            // Get top prediction
            val topPrediction = sortedPredictions.firstOrNull()

            if (topPrediction == null) {
                AppLogger.logError("InferenceEngine", "No_Predictions", "No predictions available after sorting")
                return@withContext null
            }

            val processingTime = System.currentTimeMillis() - startTime
            AppLogger.logResponse("InferenceEngine", "DetectPest_Success",
                "Top: ${topPrediction.pestType.displayName} (${String.format(Locale.US, "%.2f%%", topPrediction.confidence * 100)}), Time: ${processingTime}ms, Runtime: $runtimeName")

            // Create detection result with all predictions
            DetectionResult(
                pestType = topPrediction.pestType,
                confidence = topPrediction.confidence,
                imageUri = "",
                modelUsed = "student_model ($runtimeName)",
                processingTimeMs = processingTime,
                allPredictions = sortedPredictions
            )
        } catch (e: Exception) {
            AppLogger.logError("InferenceEngine", "DetectPest_Error", e, "Exception during inference")
            Log.e(TAG, "Detection failed", e)
            null
        }
    }

    /**
     * Validate if image is suitable for pest detection
     */
    suspend fun validateImage(bitmap: Bitmap): Boolean = withContext(Dispatchers.Default) {
        try {
            AppLogger.logAction("InferenceEngine", "ValidateImage_Start", "Validating image ${bitmap.width}x${bitmap.height}")
            val isValid = imageValidator.isValidSugarcaneCropImage(bitmap)
            AppLogger.logResponse("InferenceEngine", "ValidateImage_Result", "Is valid: $isValid")
            isValid
        } catch (e: Exception) {
            AppLogger.logError("InferenceEngine", "ValidateImage_Error", e, "Validation failed, assuming valid")
            true
        }
    }

    /**
     * Check if image quality is sufficient
     */
    fun checkImageQuality(bitmap: Bitmap): Boolean {
        return try {
            imagePreprocessor.isImageQualitySufficient(bitmap)
        } catch (e: Exception) {
            AppLogger.logWarning("InferenceEngine", "QualityCheck_Error", "Quality check failed: ${e.message}")
            true
        }
    }

    /**
     * Release resources for both runtimes
     */
    fun release() {
        AppLogger.logInfo("InferenceEngine", "Release", "Releasing inference engine resources")
        try {
            onnxModelWrapper.closeSession()
            pytorchModelWrapper.closeSession()
        } catch (e: Exception) {
            AppLogger.logWarning("InferenceEngine", "Release_Error", "Error releasing resources: ${e.message}")
        }
    }

    /**
     * Check if model is loaded for current runtime
     */
    fun isModelLoaded(): Boolean {
        return when (currentRuntime) {
            PreferencesManager.MLRuntime.ONNX -> onnxModelWrapper.isModelLoaded()
            PreferencesManager.MLRuntime.TFLITE -> pytorchModelWrapper.isModelLoaded()
        }
    }

    /**
     * Get current model path
     */
    fun getCurrentModelPath(): String? {
        return when (currentRuntime) {
            PreferencesManager.MLRuntime.ONNX -> onnxModelWrapper.getCurrentModelPath()
            PreferencesManager.MLRuntime.TFLITE -> pytorchModelWrapper.getCurrentModelPath()
        }
    }

    /**
     * Get current runtime being used
     */
    fun getCurrentRuntime(): PreferencesManager.MLRuntime {
        return currentRuntime
    }

    /**
     * Get runtime display name
     */
    fun getRuntimeDisplayName(): String {
        return when (currentRuntime) {
            PreferencesManager.MLRuntime.ONNX -> "ONNX Runtime"
            PreferencesManager.MLRuntime.TFLITE -> "PyTorch Mobile"
        }
    }
}

