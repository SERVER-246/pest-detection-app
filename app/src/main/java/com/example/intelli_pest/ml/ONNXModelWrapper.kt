package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.intelli_pest.domain.model.PestPrediction
import com.example.intelli_pest.domain.model.PestType
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer

/**
 * Wrapper for ONNX Runtime model inference
 */
class ONNXModelWrapper(
    private val context: Context,
    private val imagePreprocessor: ImagePreprocessor
) {
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var currentModelPath: String? = null
    private var modelInputSize: Int = 224 // Default, will be updated from model metadata

    companion object {
        private const val TAG = "ONNXModelWrapper"
        private const val MAX_RETRY_ATTEMPTS = 2
    }

    /**
     * Initialize ONNX Runtime session with a model file
     */
    suspend fun initializeModel(modelPath: String) = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRY_ATTEMPTS) {
            try {
                AppLogger.logModelOperation("ONNX_Initialize_Model", modelPath, "STARTED", "Attempt $attempt of $MAX_RETRY_ATTEMPTS")
                Log.d(TAG, "======= ONNX MODEL INITIALIZATION START (Attempt $attempt) =======")
                Log.d(TAG, "initializeModel() | path=$modelPath")

                // Close existing session if any
                if (ortSession != null) {
                    AppLogger.logDebug("ONNXWrapper", "Closing_Existing", "Closing existing ONNX session")
                    Log.d(TAG, "Closing existing ONNX session")
                    closeSession()
                }

                // Initialize ORT environment if needed
                if (ortEnvironment == null) {
                    ortEnvironment = OrtEnvironment.getEnvironment()
                    Log.d(TAG, "ONNX Runtime environment created")
                }

                // Load model
                val modelBytes = if (modelPath.startsWith("models/")) {
                    Log.d(TAG, "Loading ONNX model from assets: $modelPath")
                    loadModelFromAssets(modelPath)
                } else {
                    Log.d(TAG, "Loading ONNX model from file: $modelPath")
                    loadModelFromFile(modelPath)
                }

                AppLogger.logResponse("ONNXWrapper", "Model_Loaded", "Model bytes size: ${modelBytes.size}")
                Log.d(TAG, "ONNX model loaded | size=${modelBytes.size} bytes")

                // Create session options
                val sessionOptions = OrtSession.SessionOptions().apply {
                    setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
                    setIntraOpNumThreads(4)
                }

                // Create session from bytes
                ortSession = ortEnvironment?.createSession(modelBytes, sessionOptions)
                currentModelPath = modelPath

                // Verify session is working
                verifySession()

                AppLogger.logModelOperation("ONNX_Initialize_Model", modelPath, "SUCCESS", "ONNX model initialized successfully on attempt $attempt")
                Log.d(TAG, "✅ ONNX Model initialized successfully | path=$modelPath")
                Log.d(TAG, "======= ONNX MODEL INITIALIZATION END =======")
                return@withContext true

            } catch (e: Exception) {
                lastException = e
                AppLogger.logWarning("ONNXWrapper", "Init_Attempt_Failed", "Attempt $attempt failed: ${e.message}")
                Log.w(TAG, "Attempt $attempt failed: ${e.message}")

                // Clean up before retry
                try {
                    ortSession?.close()
                    ortSession = null
                } catch (_: Exception) {}

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    Log.d(TAG, "Retrying ONNX model initialization...")
                }
            }
        }

        // All attempts failed
        AppLogger.logModelOperation("ONNX_Initialize_Model", modelPath, "FAILED", "All $MAX_RETRY_ATTEMPTS attempts failed: ${lastException?.message}", lastException)
        Log.e(TAG, "❌ ONNX initializeModel() FAILED after $MAX_RETRY_ATTEMPTS attempts | path=$modelPath", lastException)
        false
    }

    /**
     * Verify that the ONNX session is properly initialized and detect input dimensions
     */
    private fun verifySession() {
        val session = ortSession ?: throw IllegalStateException("ONNX Session is null after initialization")

        val inputInfo = session.inputInfo
        val outputInfo = session.outputInfo

        if (inputInfo.isEmpty() || outputInfo.isEmpty()) {
            throw IllegalStateException("Invalid ONNX model: no inputs or outputs")
        }

        // Detect input dimensions from first input tensor
        try {
            val firstInput = inputInfo.values.first()
            val tensorInfo = firstInput.info

            // Get shape from TensorInfo - it returns NodeInfo containing TensorInfo
            if (tensorInfo is ai.onnxruntime.TensorInfo) {
                val shape = tensorInfo.shape

                // Shape is typically [batch, channels, height, width] - channels first
                // For our models: [1, 3, H, W]
                if (shape.size >= 4) {
                    val height = shape[2].toInt()
                    val width = shape[3].toInt()

                    if (height == width && height > 0) {
                        modelInputSize = height
                        Log.d(TAG, "Detected ONNX model input size: ${modelInputSize}x${modelInputSize}")
                        AppLogger.logDebug("ONNXWrapper", "Input_Size_Detected", "Model expects ${modelInputSize}x${modelInputSize} input")
                    } else if (height > 0 && width > 0) {
                        // Use height as default if non-square
                        modelInputSize = height
                        Log.w(TAG, "Non-square dimensions: ${height}x${width}, using height: $height")
                    } else {
                        Log.w(TAG, "Invalid dimensions, using default 256")
                        modelInputSize = 256
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not detect input dimensions: ${e.message}, using default 256")
            modelInputSize = 256
        }

        Log.d(TAG, "ONNX Session verified - Inputs: ${inputInfo.keys}, Outputs: ${outputInfo.keys}")
        AppLogger.logDebug("ONNXWrapper", "Session_Verified", "Inputs: ${inputInfo.keys}, Outputs: ${outputInfo.keys}")
    }

    /**
     * Load ONNX model from assets folder
     */
    private fun loadModelFromAssets(assetPath: String): ByteArray {
        Log.d(TAG, "loadModelFromAssets() | assetPath=$assetPath")

        // Copy to cache first for reliable loading
        val modelFileName = File(assetPath).name
        val tempFile = File(context.cacheDir, modelFileName)

        if (tempFile.exists() && tempFile.length() > 0) {
            Log.d(TAG, "Using cached ONNX model: ${tempFile.absolutePath}")
            return tempFile.readBytes()
        }

        // Copy from assets to cache
        Log.d(TAG, "Copying ONNX model from assets to cache...")
        context.assets.open(assetPath).use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Log.d(TAG, "ONNX model copied to cache: ${tempFile.absolutePath} (${tempFile.length()} bytes)")
        return tempFile.readBytes()
    }

    /**
     * Load ONNX model from file system
     */
    private fun loadModelFromFile(filePath: String): ByteArray {
        Log.d(TAG, "loadModelFromFile() | filePath=$filePath")

        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "❌ ONNX Model file does not exist: $filePath")
            throw IllegalArgumentException("ONNX Model file not found: $filePath")
        }

        Log.d(TAG, "File exists | size=${file.length()} bytes")
        return file.readBytes()
    }

    /**
     * Run inference on a bitmap image
     */
    suspend fun runInference(bitmap: Bitmap): List<PestPrediction>? = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "======= ONNX INFERENCE START =======")
            val session = ortSession ?: run {
                Log.e(TAG, "❌ ONNX runInference() FAILED | session not initialized")
                return@withContext null
            }

            val env = ortEnvironment ?: run {
                Log.e(TAG, "❌ ONNX runInference() FAILED | environment not initialized")
                return@withContext null
            }

            Log.d(TAG, "ONNX Session ready | model=$currentModelPath")
            Log.d(TAG, "Input bitmap | size=${bitmap.width}x${bitmap.height}, config=${bitmap.config}")
            Log.d(TAG, "Model expects input size: ${modelInputSize}x${modelInputSize}")

            // Preprocess image with the correct size for this model
            Log.d(TAG, "Preprocessing image...")
            val startPreprocess = System.currentTimeMillis()
            val inputArray = imagePreprocessor.preprocessImage(bitmap, modelInputSize)
            val preprocessTime = System.currentTimeMillis() - startPreprocess
            Log.d(TAG, "✅ Preprocessing complete | time=${preprocessTime}ms")

            // Create ONNX tensor - ONNX uses channels-first format [1, 3, H, W]
            val inputShape = longArrayOf(1, 3, modelInputSize.toLong(), modelInputSize.toLong())
            val floatBuffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(env, floatBuffer, inputShape)

            // Get input name
            val inputName = session.inputInfo.keys.first()
            Log.d(TAG, "Running ONNX inference with input: $inputName")

            // Run inference
            val startInference = System.currentTimeMillis()
            val results = session.run(mapOf(inputName to inputTensor))
            val inferenceTime = System.currentTimeMillis() - startInference
            Log.d(TAG, "✅ ONNX Inference complete | time=${inferenceTime}ms")

            // Get output
            val outputName = session.outputInfo.keys.first()
            val outputResult = results[outputName]

            if (outputResult == null) {
                Log.e(TAG, "❌ ONNX output is null")
                inputTensor.close()
                results.close()
                return@withContext null
            }

            // Parse output - OnnxValue.get() returns the raw tensor as Optional<OnnxTensor>
            val outputArray: FloatArray = try {
                // Get the OnnxTensor from the result set
                val onnxTensor = results.get(outputName)
                if (onnxTensor.isPresent) {
                    val tensor = onnxTensor.get()
                    if (tensor is OnnxTensor) {
                        val floatBuffer = tensor.floatBuffer
                        val arr = FloatArray(floatBuffer.remaining())
                        floatBuffer.get(arr)
                        Log.d(TAG, "Output array size: ${arr.size}")
                        arr
                    } else {
                        Log.w(TAG, "Output is not OnnxTensor: ${tensor.javaClass.name}")
                        FloatArray(0)
                    }
                } else {
                    Log.w(TAG, "Output tensor not present")
                    FloatArray(0)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to parse output: ${e.message}", e)
                FloatArray(0)
            }

            Log.d(TAG, "Raw output size: ${outputArray.size}")

            // Clean up tensors
            inputTensor.close()
            results.close()

            // Apply softmax
            val probabilities = softmax(outputArray)
            Log.d(TAG, "Softmax applied, creating predictions...")

            // Create predictions
            val predictions = mutableListOf<PestPrediction>()
            for (i in probabilities.indices) {
                val pestType = PestType.fromIndex(i)
                if (pestType != null) {
                    predictions.add(PestPrediction(pestType, probabilities[i]))
                }
            }

            // Sort by confidence (descending)
            val sortedPredictions = predictions.sortedByDescending { it.confidence }

            Log.d(TAG, "✅ ONNX Inference complete | total_predictions=${sortedPredictions.size}")
            sortedPredictions.take(3).forEachIndexed { idx, pred ->
                Log.d(TAG, "  ${idx + 1}. ${pred.pestType.displayName}: ${pred.getConfidencePercentage()}")
            }
            Log.d(TAG, "======= ONNX INFERENCE END =======")

            return@withContext sortedPredictions

        } catch (e: Exception) {
            Log.e(TAG, "❌ ONNX runInference() FAILED", e)
            AppLogger.logError("ONNXWrapper", "Inference_Failed", e, "ONNX inference error")
            null
        }
    }

    /**
     * Apply softmax function to get probabilities
     */
    private fun softmax(input: FloatArray): FloatArray {
        if (input.isEmpty()) return floatArrayOf()
        val maxInput = input.maxOrNull() ?: 0f
        val expValues = input.map { Math.exp((it - maxInput).toDouble()).toFloat() }
        val sumExp = expValues.sum()
        return expValues.map { it / sumExp }.toFloatArray()
    }

    /**
     * Close the current session and release resources
     */
    fun closeSession() {
        Log.d(TAG, "closeSession()")
        try {
            ortSession?.close()
            ortSession = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ONNX session", e)
        }
    }

    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean {
        return ortSession != null
    }

    /**
     * Get the current model path
     */
    fun getCurrentModelPath(): String? {
        return currentModelPath
    }
}

