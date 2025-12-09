package com.example.intelli_pest.ml

import android.content.Context
import android.graphics.Bitmap
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.intelli_pest.domain.model.PestPrediction
import com.example.intelli_pest.domain.model.PestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.FloatBuffer

/**
 * Wrapper for ONNX model inference
 */
class OnnxModelWrapper(
    private val context: Context,
    private val imagePreprocessor: ImagePreprocessor
) {
    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var currentModelPath: String? = null

    /**
     * Initialize ONNX Runtime with a model file
     */
    suspend fun initializeModel(modelPath: String) = withContext(Dispatchers.IO) {
        try {
            // Close existing session if any
            closeSession()

            // Create ORT environment
            ortEnvironment = OrtEnvironment.getEnvironment()

            // Load model
            val modelBytes = if (modelPath.startsWith("models/")) {
                // Load from assets
                context.assets.open(modelPath).readBytes()
            } else {
                // Load from file
                File(modelPath).readBytes()
            }

            ortSession = ortEnvironment?.createSession(modelBytes)
            currentModelPath = modelPath

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Run inference on a bitmap image
     */
    suspend fun runInference(bitmap: Bitmap): List<PestPrediction>? = withContext(Dispatchers.Default) {
        try {
            val session = ortSession ?: return@withContext null
            val env = ortEnvironment ?: return@withContext null

            // Preprocess image
            val inputArray = imagePreprocessor.preprocessImage(bitmap)

            // Create input tensor [1, 3, 224, 224]
            val shape = longArrayOf(1, 3, 224, 224)
            val floatBuffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(env, floatBuffer, shape)

            // Run inference
            val results = session.run(mapOf("input" to inputTensor))

            // Get output tensor
            val output = results[0].value as Array<*>
            val outputArray = (output[0] as FloatArray)

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

            // Sort by confidence (descending)
            predictions.sortedByDescending { it.confidence }

        } catch (e: Exception) {
            e.printStackTrace()
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
     * Close the current session and release resources
     */
    fun closeSession() {
        ortSession?.close()
        ortSession = null
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

