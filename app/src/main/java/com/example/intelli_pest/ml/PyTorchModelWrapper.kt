package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.intelli_pest.domain.model.PestPrediction
import com.example.intelli_pest.domain.model.PestType
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Wrapper for PyTorch Mobile model inference
 * Uses student_model.pt for inference
 */
class PyTorchModelWrapper(
    private val context: Context,
    @Suppress("UNUSED_PARAMETER") imagePreprocessor: ImagePreprocessor // Kept for API consistency with ONNX wrapper
) {
    private var module: Module? = null
    private var currentModelPath: String? = null

    companion object {
        private const val TAG = "PyTorchModelWrapper"
        private const val INPUT_SIZE = 256  // Model input size

        // ImageNet normalization values
        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    /**
     * Initialize PyTorch model from assets or file
     */
    suspend fun initializeModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            AppLogger.logModelOperation("PyTorch_Initialize_Model", modelPath, "STARTED", "Loading PyTorch model")
            Log.d(TAG, "======= PYTORCH MODEL INITIALIZATION START =======")
            Log.d(TAG, "initializeModel() | path=$modelPath")

            // Close existing module if any
            if (module != null) {
                AppLogger.logDebug("PyTorchWrapper", "Closing_Existing", "Closing existing PyTorch module")
                closeSession()
            }

            // Load model - need to copy from assets to cache first
            val modelFile = if (modelPath.startsWith("models/")) {
                copyAssetToCache(modelPath)
            } else {
                File(modelPath)
            }

            if (!modelFile.exists()) {
                AppLogger.logError("PyTorchWrapper", "Model_Not_Found", "Model file not found: ${modelFile.absolutePath}")
                return@withContext false
            }

            AppLogger.logInfo("PyTorchWrapper", "Loading_Module", "Loading PyTorch module from: ${modelFile.absolutePath}")
            module = Module.load(modelFile.absolutePath)
            currentModelPath = modelPath

            AppLogger.logModelOperation("PyTorch_Initialize_Model", modelPath, "SUCCESS", "PyTorch model loaded successfully")
            Log.d(TAG, "✅ PyTorch Model initialized successfully | path=$modelPath")
            Log.d(TAG, "======= PYTORCH MODEL INITIALIZATION END =======")
            true

        } catch (e: Exception) {
            AppLogger.logModelOperation("PyTorch_Initialize_Model", modelPath, "FAILED", "Failed to load: ${e.message}", e)
            Log.e(TAG, "❌ PyTorch initializeModel() FAILED | path=$modelPath", e)
            false
        }
    }

    /**
     * Copy asset file to cache directory for PyTorch loading
     */
    private fun copyAssetToCache(assetPath: String): File {
        val fileName = File(assetPath).name
        val cacheFile = File(context.cacheDir, fileName)

        if (cacheFile.exists() && cacheFile.length() > 0) {
            Log.d(TAG, "Using cached model: ${cacheFile.absolutePath}")
            return cacheFile
        }

        Log.d(TAG, "Copying model from assets to cache...")
        context.assets.open(assetPath).use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }
        Log.d(TAG, "Model copied to: ${cacheFile.absolutePath} (${cacheFile.length()} bytes)")
        return cacheFile
    }

    /**
     * Run inference on a bitmap image
     */
    suspend fun runInference(bitmap: Bitmap): List<PestPrediction>? = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "======= PYTORCH INFERENCE START =======")
            val pytorchModule = module ?: run {
                Log.e(TAG, "❌ PyTorch runInference() FAILED | module not initialized")
                return@withContext null
            }

            Log.d(TAG, "PyTorch Module ready | model=$currentModelPath")
            Log.d(TAG, "Input bitmap | size=${bitmap.width}x${bitmap.height}, config=${bitmap.config}")

            // Resize bitmap to model input size
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

            // Convert bitmap to tensor using TensorImageUtils
            Log.d(TAG, "Converting bitmap to tensor...")
            val startPreprocess = System.currentTimeMillis()
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                MEAN,
                STD
            )
            val preprocessTime = System.currentTimeMillis() - startPreprocess
            Log.d(TAG, "✅ Preprocessing complete | time=${preprocessTime}ms")

            // Run inference
            Log.d(TAG, "Running PyTorch inference...")
            val startInference = System.currentTimeMillis()
            val outputTensor = pytorchModule.forward(IValue.from(inputTensor)).toTensor()
            val inferenceTime = System.currentTimeMillis() - startInference
            Log.d(TAG, "✅ PyTorch Inference complete | time=${inferenceTime}ms")

            // Get output scores
            val scores = outputTensor.dataAsFloatArray
            Log.d(TAG, "Output scores size: ${scores.size}")

            // Apply softmax to get probabilities
            val probabilities = softmax(scores)

            // Map to predictions - filter out null pest types
            val predictions = mutableListOf<PestPrediction>()
            probabilities.forEachIndexed { index, prob ->
                val pestType = PestType.fromIndex(index)
                if (pestType != null) {
                    predictions.add(PestPrediction(pestType, prob))
                }
            }

            // Log top predictions
            val sorted = predictions.sortedByDescending { it.confidence }
            sorted.take(3).forEachIndexed { i, pred ->
                Log.d(TAG, "Top $i: ${pred.pestType.displayName} = ${String.format(Locale.US, "%.2f%%", pred.confidence * 100)}")
            }

            AppLogger.logResponse("PyTorchWrapper", "Inference_Complete",
                "Predictions: ${predictions.size}, Top: ${sorted.firstOrNull()?.pestType?.displayName ?: "None"}")

            Log.d(TAG, "======= PYTORCH INFERENCE END =======")
            predictions

        } catch (e: Exception) {
            AppLogger.logError("PyTorchWrapper", "Inference_Failed", e, "PyTorch inference error")
            Log.e(TAG, "❌ PyTorch inference failed", e)
            null
        }
    }

    /**
     * Apply softmax to convert logits to probabilities
     */
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expValues = logits.map { kotlin.math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExp = expValues.sum()
        return expValues.map { it / sumExp }.toFloatArray()
    }

    /**
     * Close the PyTorch module and release resources
     */
    fun closeSession() {
        try {
            module?.destroy()
            module = null
            currentModelPath = null
            AppLogger.logInfo("PyTorchWrapper", "Session_Closed", "PyTorch module closed")
            Log.d(TAG, "PyTorch module closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing PyTorch module", e)
        }
    }

    /**
     * Check if model is loaded
     */
    fun isModelLoaded(): Boolean {
        return module != null
    }

    /**
     * Get current model path
     */
    fun getCurrentModelPath(): String? {
        return currentModelPath
    }
}

