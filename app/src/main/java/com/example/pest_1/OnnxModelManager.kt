package com.example.pest_1

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import com.example.pest_1.domain.model.ClassPrediction
import com.example.pest_1.domain.model.PredictionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.exp


class OnnxModelManager(private val context: Context) {
    private var ortSession: OrtSession? = null
    private var classNames: List<String> = emptyList()
    private var classMapping: Map<Int, String> = emptyMap()
    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var imageSize = 256
    private var currentModelName = ""

    companion object {
        private const val TAG = "OnnxModelManager"
        private const val CONFIDENCE_THRESHOLD = 80.0f

        // ImageNet normalization values
        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    /**
     * Load a model from either assets or file system
     * @param modelPath: Can be "models/mobilenet_v2" (assets) or "/data/.../models/resnet50" (file path)
     */
    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading model from: $modelPath")

            // Release previous session
            ortSession?.close()
            ortSession = null

            // Determine if path is from assets or file system
            val modelBytes = if (modelPath.startsWith("/") || modelPath.contains(":\\")) {
                // Absolute file path
                val modelFile = File(modelPath, "model.onnx")
                if (!modelFile.exists()) {
                    throw Exception("Model file not found: ${modelFile.absolutePath}")
                }
                modelFile.readBytes()
            } else {
                // Assets path
                val assetPath = "$modelPath/model.onnx"
                context.assets.open(assetPath).use { it.readBytes() }
            }

            Log.d(TAG, "Model file size: ${modelBytes.size} bytes")

            val sessionOptions = OrtSession.SessionOptions()

            try {
                sessionOptions.setIntraOpNumThreads(4)
                sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            } catch (t: Throwable) {
                Log.w(TAG, "Could not set session options: ${t.message}")
            }

            // Create session from bytes
            ortSession = ortEnv.createSession(modelBytes, sessionOptions)
            currentModelName = modelPath

            // Log model input/output info
            logModelInfo()

            // Load metadata and labels
            loadMetadata(modelPath)
            loadClassNames(modelPath)

            Log.d(TAG, "✓ Model loaded successfully: $modelPath (imageSize=$imageSize, classes=${classNames.size})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to load model: $modelPath", e)
            false
        }
    }

    private fun logModelInfo() {
        try {
            val session = ortSession ?: return

            Log.d(TAG, "=== Model Info ===")
            Log.d(TAG, "Input names: ${session.inputNames}")
            Log.d(TAG, "Output names: ${session.outputNames}")

            session.inputInfo.forEach { (name, info) ->
                Log.d(TAG, "Input '$name': ${info.info}")
            }

            session.outputInfo.forEach { (name, info) ->
                Log.d(TAG, "Output '$name': ${info.info}")
            }
            Log.d(TAG, "==================")
        } catch (e: Exception) {
            Log.w(TAG, "Could not log model info: ${e.message}")
        }
    }

    private fun loadMetadata(modelPath: String) {
        try {
            val metadataJson = if (modelPath.startsWith("/") || modelPath.contains(":\\")) {
                // File system path
                File(modelPath, "metadata.json").readText()
            } else {
                // Assets path
                context.assets.open("$modelPath/metadata.json")
                    .bufferedReader().use { it.readText() }
            }
            val metadata = JSONObject(metadataJson)
            imageSize = metadata.optInt("image_size", 256)
            Log.d(TAG, "✓ Metadata loaded - imageSize=$imageSize")
        } catch (e: Exception) {
            Log.w(TAG, "⚠ No metadata.json - using default image size 256")
            imageSize = 256
        }
    }

    private fun loadClassNames(modelPath: String) {
        try {
            // Try class_mapping.json first
            val mappingStream = try {
                if (modelPath.startsWith("/") || modelPath.contains(":\\")) {
                    File(modelPath, "class_mapping.json").inputStream()
                } else {
                    context.assets.open("$modelPath/class_mapping.json")
                }
            } catch (e: Exception) {
                null
            }

            if (mappingStream != null) {
                val mappingJson = mappingStream.bufferedReader().use { it.readText() }
                val mappingObj = JSONObject(mappingJson)
                val temp = mutableMapOf<Int, String>()
                for (k in mappingObj.keys()) {
                    try {
                        val idx = k.toInt()
                        temp[idx] = mappingObj.getString(k)
                    } catch (t: Exception) {
                        Log.w(TAG, "Skipping non-integer key: $k")
                    }
                }
                classMapping = temp
                Log.d(TAG, "✓ Loaded class_mapping.json (${classMapping.size} entries)")
            }

            // Then try labels.txt
            val labelsStream = try {
                if (modelPath.startsWith("/") || modelPath.contains(":\\")) {
                    File(modelPath, "labels.txt").inputStream()
                } else {
                    context.assets.open("$modelPath/labels.txt")
                }
            } catch (e: Exception) {
                null
            }

            if (labelsStream != null) {
                val labelsText = labelsStream.bufferedReader().use { it.readText() }
                classNames = labelsText.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                Log.d(TAG, "✓ Loaded labels.txt (${classNames.size} classes)")
            }

            // Fallback to default classes
            if (classNames.isEmpty() && classMapping.isEmpty()) {
                classNames = listOf(
                    "Armyworm", "Healthy", "Internode borer", "Mealy bug",
                    "Pink borer", "Porcupine damage", "Rat damage", "Root borer",
                    "Stalk borer", "Termite", "Top borer"
                )
                Log.w(TAG, "⚠ Using fallback class names (${classNames.size})")
            }

            // If we have mapping but no classNames list, build from mapping
            if (classNames.isEmpty() && classMapping.isNotEmpty()) {
                val maxIdx = classMapping.keys.maxOrNull() ?: 0
                classNames = (0..maxIdx).map { classMapping[it] ?: "Unknown_$it" }
                Log.d(TAG, "✓ Built classNames from mapping (${classNames.size} classes)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to load class names", e)
            classNames = listOf(
                "Armyworm", "Healthy", "Internode borer", "Mealy bug",
                "Pink borer", "Porcupine damage", "Rat damage", "Root borer",
                "Stalk borer", "Termite", "Top borer"
            )
        }
    }

    /**
     * Classify a bitmap - FIXED VERSION
     */
    suspend fun classifyImage(bitmap: Bitmap): PredictionResult? = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        try {
            val session = ortSession ?: run {
                Log.e(TAG, "✗ No model session available")
                return@withContext null
            }

            Log.d(TAG, "→ Starting classification with model: $currentModelName")

            // Preprocess image
            val inputArray = preprocessImage(bitmap)
            val inputName = session.inputNames.firstOrNull() ?: run {
                Log.e(TAG, "✗ Model has no input names")
                return@withContext null
            }

            val shape = longArrayOf(1, 3, imageSize.toLong(), imageSize.toLong())
            Log.d(TAG, "Input tensor shape: [${shape.joinToString(", ")}]")

            // Create input tensor and run inference
            OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(inputArray), shape).use { inputTensor ->
                val inputs = mapOf(inputName to inputTensor)

                session.run(inputs).use { results ->
                    if (results.size() == 0) {
                        Log.e(TAG, "✗ No outputs returned by model")
                        return@withContext null
                    }

                    // Get the first (and usually only) output
                    val output = results.get(0)

                    Log.d(TAG, "Output type: ${output.value?.javaClass?.name}")

                    // Extract confidences from ONNX tensor
                    val confidences = extractConfidences(output.value)

                    if (confidences == null) {
                        Log.e(TAG, "✗ Failed to extract confidences from model output")
                        return@withContext null
                    }

                    Log.d(TAG, "Extracted ${confidences.size} confidence values")
                    Log.d(TAG, "Raw confidences sample: ${confidences.take(5).joinToString()}")

                    // Match output size to class count
                    val confidencesFixed = matchConfidencesToClasses(confidences)

                    // Check if already probabilities (sum close to 1.0) or need softmax
                    val sum = confidencesFixed.sum()
                    val needsSoftmax = sum < 0.9f || sum > 1.1f

                    Log.d(TAG, "Confidence sum: $sum, needsSoftmax: $needsSoftmax")

                    // Apply softmax if needed
                    val probabilities = if (needsSoftmax) {
                        softmax(confidencesFixed)
                    } else {
                        confidencesFixed
                    }

                    // Create predictions
                    val predictions = createPredictions(probabilities)

                    val top = predictions.firstOrNull() ?: run {
                        Log.e(TAG, "✗ No predictions generated")
                        return@withContext null
                    }

                    val meets = top.confidence >= CONFIDENCE_THRESHOLD
                    val inferenceTime = System.currentTimeMillis() - startTime

                    Log.d(TAG, "✓ Classification complete")
                    Log.d(TAG, "  Top prediction: ${top.className}")
                    Log.d(TAG, "  Confidence: ${String.format("%.2f", top.confidence)}%")
                    Log.d(TAG, "  Meets threshold ($CONFIDENCE_THRESHOLD%): $meets")
                    Log.d(TAG, "  Inference time: ${inferenceTime}ms")

                    return@withContext PredictionResult(
                        className = top.className,
                        confidence = top.confidence,
                        allPredictions = predictions.take(5),
                        meetsThreshold = meets,
                        inferenceTimeMs = inferenceTime
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Classification failed", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Extract confidence values from various ONNX output formats
     */
    private fun extractConfidences(rawOutput: Any?): FloatArray? {
        return try {
            when (rawOutput) {
                // Direct OnnxTensor
                is OnnxTensor -> {
                    Log.d(TAG, "Output is OnnxTensor")
                    val buffer = rawOutput.floatBuffer
                    FloatArray(buffer.remaining()) { buffer.get(it) }
                }

                // FloatArray (direct)
                is FloatArray -> {
                    Log.d(TAG, "Output is FloatArray (size: ${rawOutput.size})")
                    rawOutput
                }

                // Nested arrays (some models output this)
                is Array<*> -> {
                    Log.d(TAG, "Output is Array<*>, attempting to flatten")
                    val flattened = mutableListOf<Float>()
                    flattenArray(rawOutput, flattened)
                    if (flattened.isNotEmpty()) {
                        Log.d(TAG, "Flattened to ${flattened.size} values")
                        flattened.toFloatArray()
                    } else null
                }

                // List of numbers
                is List<*> -> {
                    Log.d(TAG, "Output is List<*>")
                    val nums = rawOutput.filterIsInstance<Number>().map { it.toFloat() }
                    if (nums.isNotEmpty()) nums.toFloatArray() else null
                }

                // FloatBuffer
                is FloatBuffer -> {
                    Log.d(TAG, "Output is FloatBuffer")
                    FloatArray(rawOutput.remaining()) { rawOutput.get(it) }
                }

                else -> {
                    Log.e(TAG, "Unhandled output type: ${rawOutput?.javaClass?.name}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting confidences", e)
            null
        }
    }

    /**
     * Match confidence array size to expected class count
     */
    private fun matchConfidencesToClasses(confidences: FloatArray): FloatArray {
        val expectedSize = classNames.size

        return when {
            confidences.size == expectedSize -> {
                Log.d(TAG, "✓ Output size matches class count ($expectedSize)")
                confidences
            }

            confidences.size > expectedSize -> {
                Log.w(TAG, "⚠ Output size ${confidences.size} > class count $expectedSize, truncating")
                confidences.sliceArray(0 until expectedSize)
            }

            else -> {
                Log.w(TAG, "⚠ Output size ${confidences.size} < class count $expectedSize, padding")
                val padded = FloatArray(expectedSize) { 0f }
                confidences.copyInto(padded, 0, 0, confidences.size)
                padded
            }
        }
    }

    /**
     * Create sorted predictions from softmax confidences
     */
    private fun createPredictions(softmaxed: FloatArray): List<ClassPrediction> {
        return softmaxed.mapIndexed { idx, conf ->
            val name = classMapping[idx]
                ?: classNames.getOrNull(idx)
                ?: "Unknown_$idx"
            ClassPrediction(name, conf * 100f)
        }.sortedByDescending { it.confidence }
    }

    /**
     * Flatten nested Array types
     */
    private fun flattenArray(arr: Array<*>, result: MutableList<Float>) {
        for (el in arr) {
            when (el) {
                is Array<*> -> flattenArray(el, result)
                is Number -> result.add(el.toFloat())
                is FloatArray -> el.forEach { result.add(it) }
                is DoubleArray -> el.forEach { result.add(it.toFloat()) }
                is IntArray -> el.forEach { result.add(it.toFloat()) }
            }
        }
    }

    /**
     * Preprocess image to model input format
     */
    private fun preprocessImage(bitmap: Bitmap): FloatArray {
        Log.d(TAG, "Preprocessing image: ${bitmap.width}x${bitmap.height} -> ${imageSize}x${imageSize}")

        // Resize to expected size
        val resized = try {
            bitmap.scale(imageSize, imageSize)
        } catch (t: Throwable) {
            Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)
        }

        val pixels = IntArray(imageSize * imageSize)
        resized.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        // Convert to normalized CHW format (Channels, Height, Width)
        val out = FloatArray(3 * imageSize * imageSize)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p shr 16) and 0xFF) / 255.0f
            val g = ((p shr 8) and 0xFF) / 255.0f
            val b = (p and 0xFF) / 255.0f

            // Apply ImageNet normalization
            out[i] = (r - MEAN[0]) / STD[0]
            out[imageSize * imageSize + i] = (g - MEAN[1]) / STD[1]
            out[2 * imageSize * imageSize + i] = (b - MEAN[2]) / STD[2]
        }

        return out
    }

    /**
     * Apply softmax activation
     */
    private fun softmax(input: FloatArray): FloatArray {
        if (input.isEmpty()) return FloatArray(0)

        val max = input.maxOrNull() ?: 0f
        val exps = input.map { exp((it - max).toDouble()).toFloat() }
        val sum = exps.sum()

        return if (sum == 0f || sum.isNaN()) {
            FloatArray(input.size) { 1f / input.size }
        } else {
            exps.map { it / sum }.toFloatArray()
        }
    }

    fun release() {
        try {
            ortSession?.close()
            Log.d(TAG, "✓ Session released")
        } catch (t: Throwable) {
            Log.w(TAG, "⚠ Error closing session: ${t.message}")
        } finally {
            ortSession = null
            currentModelName = ""
        }
    }

    fun getCurrentModelName(): String = currentModelName
}