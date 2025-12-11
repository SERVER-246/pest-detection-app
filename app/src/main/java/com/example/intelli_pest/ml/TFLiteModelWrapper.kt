package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.intelli_pest.domain.model.PestPrediction
import com.example.intelli_pest.domain.model.PestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Wrapper for TensorFlow Lite model inference
 */
class TFLiteModelWrapper(
    private val context: Context,
    private val imagePreprocessor: ImagePreprocessor
) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var currentModelPath: String? = null

    companion object {
        private const val TAG = "TFLiteModelWrapper"
        private const val NUM_THREADS = 4
    }

    /**
     * Initialize TensorFlow Lite interpreter with a model file
     */
    suspend fun initializeModel(modelPath: String) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "initializeModel() | path=$modelPath")

            // Close existing interpreter if any
            closeSession()

            // Load model
            val modelBuffer = if (modelPath.startsWith("models/")) {
                // Load from assets
                loadModelFromAssets(modelPath)
            } else {
                // Load from file
                loadModelFromFile(modelPath)
            }

            // Configure interpreter options
            val options = Interpreter.Options().apply {
                setNumThreads(NUM_THREADS)

                // Try to use GPU delegate if available
                val compatList = CompatibilityList()
                if (compatList.isDelegateSupportedOnThisDevice) {
                    try {
                        gpuDelegate = GpuDelegate(compatList.bestOptionsForThisDevice)
                        addDelegate(gpuDelegate)
                        Log.d(TAG, "GPU delegate enabled")
                    } catch (e: Exception) {
                        Log.w(TAG, "GPU delegate failed, using CPU", e)
                    }
                } else {
                    Log.d(TAG, "GPU not supported, using CPU")
                }
            }

            // Create interpreter
            interpreter = Interpreter(modelBuffer, options)
            currentModelPath = modelPath

            Log.d(TAG, "Model initialized successfully | path=$modelPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "initializeModel() failed", e)
            false
        }
    }

    /**
     * Load model from assets folder
     */
    private fun loadModelFromAssets(assetPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(assetPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Load model from file system
     */
    private fun loadModelFromFile(filePath: String): MappedByteBuffer {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
    }

    /**
     * Run inference on a bitmap image
     */
    suspend fun runInference(bitmap: Bitmap): List<PestPrediction>? = withContext(Dispatchers.Default) {
        try {
            val currentInterpreter = interpreter ?: run {
                Log.e(TAG, "runInference() | interpreter not initialized")
                return@withContext null
            }

            Log.d(TAG, "runInference() start | bitmap=${bitmap.width}x${bitmap.height}")

            // Preprocess image
            val inputArray = imagePreprocessor.preprocessImage(bitmap)

            // Create input buffer [1, 224, 224, 3] - TFLite uses channels-last format
            val inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4).apply {
                order(ByteOrder.nativeOrder())
                rewind()

                // Convert from channels-first [1, 3, 224, 224] to channels-last [1, 224, 224, 3]
                for (h in 0 until 224) {
                    for (w in 0 until 224) {
                        val baseIdx = h * 224 + w
                        putFloat(inputArray[baseIdx])                     // R
                        putFloat(inputArray[224 * 224 + baseIdx])        // G
                        putFloat(inputArray[2 * 224 * 224 + baseIdx])    // B
                    }
                }
                rewind()
            }

            // Prepare output buffer
            val outputShape = currentInterpreter.getOutputTensor(0).shape()
            val numClasses = outputShape[outputShape.lastIndex]
            val outputBuffer = ByteBuffer.allocateDirect(numClasses * 4).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference
            currentInterpreter.run(inputBuffer, outputBuffer)

            // Parse output
            outputBuffer.rewind()
            val outputArray = FloatArray(numClasses)
            outputBuffer.asFloatBuffer().get(outputArray)

            Log.d(TAG, "runInference() | raw output size=$numClasses")

            // Apply softmax
            val probabilities = softmax(outputArray)

            // Create predictions
            val predictions = mutableListOf<PestPrediction>()
            for (i in probabilities.indices) {
                val pestType = PestType.fromIndex(i)
                if (pestType != null) {
                    predictions.add(PestPrediction(pestType, probabilities[i]))
                }
            }

            Log.d(TAG, "runInference() complete | predictions=${predictions.size}")

            // Sort by confidence (descending)
            predictions.sortedByDescending { it.confidence }

        } catch (e: Exception) {
            Log.e(TAG, "runInference() error", e)
            null
        }
    }

    /**
     * Apply softmax function to get probabilities
     */
    private fun softmax(input: FloatArray): FloatArray {
        val maxInput = input.maxOrNull() ?: 0f
        val expValues = input.map { Math.exp((it - maxInput).toDouble()).toFloat() }
        val sumExp = expValues.sum()
        return expValues.map { it / sumExp }.toFloatArray()
    }

    /**
     * Close the current interpreter and release resources
     */
    fun closeSession() {
        Log.d(TAG, "closeSession()")
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }

    /**
     * Check if a model is currently loaded
     */
    fun isModelLoaded(): Boolean {
        return interpreter != null
    }

    /**
     * Get the current model path
     */
    fun getCurrentModelPath(): String? {
        return currentModelPath
    }
}



