package com.example.intelli_pest.data.source.local

import android.content.Context
import android.util.Log
import com.example.intelli_pest.domain.model.ModelInfo
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages model files storage and access
 */
class ModelFileManager(private val context: Context) {

    private val modelsDir = File(context.filesDir, "models")

    companion object {
        private const val TAG = "ModelFileManager"
        // GitHub release URL for model downloads
        private const val GITHUB_MODELS_URL = "https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models"
    }

    init {
        // Create models directory if it doesn't exist
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }

        // Log available assets on init
        try {
            val assetModels = context.assets.list("models")
            AppLogger.logInfo("ModelFileManager", "Init", "Assets in models/: ${assetModels?.joinToString()}")
            Log.d(TAG, "Available assets in models/: ${assetModels?.joinToString()}")
        } catch (e: Exception) {
            AppLogger.logError("ModelFileManager", "Init_Error", e, "Failed to list assets")
            Log.e(TAG, "Failed to list assets", e)
        }
    }

    /**
     * Get path for a model file
     */
    fun getModelPath(modelId: String): String {
        return File(modelsDir, "$modelId.tflite").absolutePath
    }

    /**
     * Check if model file exists in downloaded folder
     */
    fun isModelDownloaded(modelId: String): Boolean {
        val tfliteFile = File(modelsDir, "$modelId.tflite")
        val onnxFile = File(modelsDir, "$modelId.onnx")
        val tfliteExists = tfliteFile.exists() && tfliteFile.length() > 0
        val onnxExists = onnxFile.exists() && onnxFile.length() > 0
        val exists = tfliteExists || onnxExists
        AppLogger.logDebug("ModelFileManager", "Check_Downloaded", "Model: $modelId, TFLite: $tfliteExists, ONNX: $onnxExists")
        return exists
    }

    /**
     * Get bundled model path from assets (supports both TFLite and ONNX)
     * TFLite models are in models/android_models/
     * ONNX models are in models/onnx_models/
     */
    fun getBundledModelPath(modelId: String, extension: String = "tflite"): String {
        val folder = if (extension == "tflite") "android_models" else "onnx_models"
        val path = "models/$folder/$modelId.$extension"
        AppLogger.logDebug("ModelFileManager", "Get_Bundled_Path", "Model: $modelId -> Path: $path")
        return path
    }

    /**
     * Check if model is bundled in assets (supports both formats)
     * Uses new folder structure: android_models for TFLite, onnx_models for ONNX
     */
    suspend fun isModelBundled(modelId: String, extension: String = "tflite"): Boolean = withContext(Dispatchers.IO) {
        try {
            val folder = if (extension == "tflite") "android_models" else "onnx_models"
            val assetPath = "models/$folder/$modelId.$extension"
            AppLogger.logDebug("ModelFileManager", "Check_Bundled_Start", "Checking: $assetPath")
            context.assets.open(assetPath).use { inputStream ->
                val size = inputStream.available()
                AppLogger.logResponse("ModelFileManager", "Check_Bundled", "Model: $modelId.$extension BUNDLED (size: $size bytes)")
                Log.d(TAG, "Model $modelId.$extension is BUNDLED, size: $size bytes")
                true
            }
        } catch (e: Exception) {
            AppLogger.logWarning("ModelFileManager", "Check_Bundled", "Model: $modelId.$extension NOT bundled - ${e.message}")
            Log.d(TAG, "Model $modelId.$extension is NOT bundled: ${e.message}")
            false
        }
    }

    /**
     * Check if TFLite model is bundled
     */
    suspend fun isTFLiteModelBundled(modelId: String): Boolean = isModelBundled(modelId, "tflite")

    /**
     * Check if ONNX model is bundled
     */
    suspend fun isONNXModelBundled(modelId: String): Boolean = isModelBundled(modelId, "onnx")

    /**
     * Get model file size in MB
     */
    fun getModelFileSize(modelId: String): Float {
        val modelFile = File(modelsDir, "$modelId.tflite")
        return if (modelFile.exists()) {
            modelFile.length() / (1024f * 1024f)
        } else {
            0f
        }
    }

    /**
     * Delete a model file
     */
    fun deleteModel(modelId: String): Boolean {
        val modelFile = File(modelsDir, "$modelId.tflite")
        return if (modelFile.exists()) {
            modelFile.delete()
        } else {
            false
        }
    }

    /**
     * Get all available models (bundled + downloadable) with runtime-specific availability
     */
    suspend fun getAllAvailableModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        val models = mutableListOf<ModelInfo>()

        // === BUNDLED MODELS ===
        // Check actual availability in assets for both formats

        // 1. MobileNet V2 (TFLite BUNDLED - 3.18 MB)
        val mobilenetTfliteBundled = isModelBundled("mobilenet_v2", "tflite")
        val mobilenetOnnxBundled = isModelBundled("mobilenet_v2", "onnx")
        models.add(
            ModelInfo(
                id = "mobilenet_v2",
                name = "mobilenet_v2",
                displayName = "MobileNet V2",
                description = "Lightweight model optimized for mobile",
                accuracy = 0.89f,
                inferenceSpeedMs = 80,
                sizeInMb = 3.18f,
                isDownloaded = isModelDownloaded("mobilenet_v2"),
                isBundled = mobilenetTfliteBundled || mobilenetOnnxBundled,
                version = "1.0.0",
                downloadUrl = null,
                hasTFLite = true,
                hasONNX = false, // No ONNX version available
                tfliteBundled = mobilenetTfliteBundled,
                onnxBundled = mobilenetOnnxBundled
            )
        )

        // 2. YOLO 11n-cls (TFLite BUNDLED - 5.11 MB)
        val yoloTfliteBundled = isModelBundled("yolo11n-cls", "tflite")
        val yoloOnnxBundled = isModelBundled("yolo_11n", "onnx")
        models.add(
            ModelInfo(
                id = "yolo11n-cls",
                name = "yolo11n-cls",
                displayName = "YOLO 11 Nano",
                description = "Ultra-fast classification variant",
                accuracy = 0.87f,
                inferenceSpeedMs = 50,
                sizeInMb = 5.11f,
                isDownloaded = isModelDownloaded("yolo11n-cls"),
                isBundled = yoloTfliteBundled || yoloOnnxBundled,
                version = "1.0.0",
                downloadUrl = null,
                hasTFLite = true,
                hasONNX = yoloOnnxBundled, // ONNX available if bundled
                tfliteBundled = yoloTfliteBundled,
                onnxBundled = yoloOnnxBundled
            )
        )

        // 3. EfficientNet B0 (TFLite & ONNX BUNDLED - 5.11 MB)
        val efficientTfliteBundled = isModelBundled("efficientnet_b0", "tflite")
        val efficientOnnxBundled = isModelBundled("efficientnet_b0", "onnx")
        models.add(
            ModelInfo(
                id = "efficientnet_b0",
                name = "efficientnet_b0",
                displayName = "EfficientNet B0",
                description = "Balanced accuracy and efficiency",
                accuracy = 0.91f,
                inferenceSpeedMs = 120,
                sizeInMb = 5.11f,
                isDownloaded = isModelDownloaded("efficientnet_b0"),
                isBundled = efficientTfliteBundled || efficientOnnxBundled,
                version = "1.0.0",
                downloadUrl = null,
                hasTFLite = true,
                hasONNX = efficientOnnxBundled, // ONNX available if bundled
                tfliteBundled = efficientTfliteBundled,
                onnxBundled = efficientOnnxBundled
            )
        )

        // 4. MobileNet V3 (ONNX BUNDLED)
        val mobilenetV3TfliteBundled = isModelBundled("mobilenet_v3", "tflite")
        val mobilenetV3OnnxBundled = isModelBundled("mobilenet_v3", "onnx")
        if (mobilenetV3TfliteBundled || mobilenetV3OnnxBundled) {
            models.add(
                ModelInfo(
                    id = "mobilenet_v3",
                    name = "mobilenet_v3",
                    displayName = "MobileNet V3",
                    description = "Latest MobileNet architecture",
                    accuracy = 0.90f,
                    inferenceSpeedMs = 70,
                    sizeInMb = 5.5f,
                    isDownloaded = isModelDownloaded("mobilenet_v3"),
                    isBundled = mobilenetV3TfliteBundled || mobilenetV3OnnxBundled,
                    version = "1.0.0",
                    downloadUrl = null,
                    hasTFLite = mobilenetV3TfliteBundled,
                    hasONNX = mobilenetV3OnnxBundled,
                    tfliteBundled = mobilenetV3TfliteBundled,
                    onnxBundled = mobilenetV3OnnxBundled
                )
            )
        }

        // 5. DarkNet-53 (TFLite BUNDLED - 20.46 MB)
        val darknetTfliteBundled = isModelBundled("darknet53", "tflite")
        val darknetOnnxBundled = isModelBundled("darknet53", "onnx")
        models.add(
            ModelInfo(
                id = "darknet53",
                name = "darknet53",
                displayName = "DarkNet-53",
                description = "Powerful backbone from YOLO",
                accuracy = 0.92f,
                inferenceSpeedMs = 300,
                sizeInMb = 20.46f,
                isDownloaded = isModelDownloaded("darknet53"),
                isBundled = darknetTfliteBundled || darknetOnnxBundled,
                version = "1.0.0",
                downloadUrl = null,
                hasTFLite = true,
                hasONNX = darknetOnnxBundled,
                tfliteBundled = darknetTfliteBundled,
                onnxBundled = darknetOnnxBundled
            )
        )

        // 6. Inception V3 (TFLite BUNDLED - 23.1 MB)
        val inceptionTfliteBundled = isModelBundled("inception_v3", "tflite")
        val inceptionOnnxBundled = isModelBundled("inception_v3", "onnx")
        models.add(
            ModelInfo(
                id = "inception_v3",
                name = "inception_v3",
                displayName = "Inception V3",
                description = "Google's inception architecture",
                accuracy = 0.92f,
                inferenceSpeedMs = 220,
                sizeInMb = 23.1f,
                isDownloaded = isModelDownloaded("inception_v3"),
                isBundled = inceptionTfliteBundled || inceptionOnnxBundled,
                version = "1.0.0",
                downloadUrl = null,
                hasTFLite = true,
                hasONNX = inceptionOnnxBundled,
                tfliteBundled = inceptionTfliteBundled,
                onnxBundled = inceptionOnnxBundled
            )
        )

        // === DOWNLOADABLE MODELS (available via GitHub) ===

        // 7. ResNet-50 (Downloadable - 24.83 MB)
        models.add(
            ModelInfo(
                id = "resnet50",
                name = "resnet50",
                displayName = "ResNet-50",
                description = "Deep residual network with high accuracy",
                accuracy = 0.93f,
                inferenceSpeedMs = 200,
                sizeInMb = 24.83f,
                isDownloaded = isModelDownloaded("resnet50"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/resnet50.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        // 8. Attention Fusion (Downloadable - 94.98 MB)
        models.add(
            ModelInfo(
                id = "ensemble_attention",
                name = "ensemble_attention",
                displayName = "Attention Ensemble",
                description = "Attention-based fusion model",
                accuracy = 0.94f,
                inferenceSpeedMs = 280,
                sizeInMb = 94.98f,
                isDownloaded = isModelDownloaded("ensemble_attention"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/ensemble_attention.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        // 9. Concatenation Fusion (Downloadable - 95.48 MB)
        models.add(
            ModelInfo(
                id = "ensemble_concat",
                name = "ensemble_concat",
                displayName = "Concatenation Ensemble",
                description = "Multi-model fusion using concatenation",
                accuracy = 0.93f,
                inferenceSpeedMs = 250,
                sizeInMb = 95.48f,
                isDownloaded = isModelDownloaded("ensemble_concat"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/ensemble_concat.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        // 10. Cross-Attention Fusion (Downloadable - 102.09 MB)
        models.add(
            ModelInfo(
                id = "ensemble_cross",
                name = "ensemble_cross",
                displayName = "Cross-Attention Ensemble",
                description = "Advanced fusion with cross-attention",
                accuracy = 0.95f,
                inferenceSpeedMs = 320,
                sizeInMb = 102.09f,
                isDownloaded = isModelDownloaded("ensemble_cross"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/ensemble_cross.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        // 11. Super Ensemble (Downloadable - 138.3 MB)
        models.add(
            ModelInfo(
                id = "super_ensemble",
                name = "super_ensemble",
                displayName = "Super Ensemble",
                description = "Highest accuracy ensemble model",
                accuracy = 0.96f,
                inferenceSpeedMs = 450,
                sizeInMb = 138.3f,
                isDownloaded = isModelDownloaded("super_ensemble"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/super_ensemble.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        // 12. AlexNet (Downloadable - 164.48 MB)
        models.add(
            ModelInfo(
                id = "alexnet",
                name = "alexnet",
                displayName = "AlexNet",
                description = "Classic deep CNN architecture",
                accuracy = 0.88f,
                inferenceSpeedMs = 200,
                sizeInMb = 164.48f,
                isDownloaded = isModelDownloaded("alexnet"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/alexnet.tflite",
                hasTFLite = true,
                hasONNX = false,
                tfliteBundled = false,
                onnxBundled = false
            )
        )

        models
    }

    /**
     * Get models available for TFLite runtime
     */
    suspend fun getTFLiteModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        getAllAvailableModels().filter { it.hasTFLite }
    }

    /**
     * Get models available for ONNX runtime
     */
    suspend fun getONNXModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        getAllAvailableModels().filter { it.hasONNX }
    }

    /**
     * Get the correct model path based on runtime
     */
    fun getModelPathForRuntime(modelId: String, runtime: String): String? {
        val extension = if (runtime == "onnx") "onnx" else "tflite"

        // Handle special case for YOLO model naming
        val actualModelId = when {
            modelId == "yolo11n-cls" && runtime == "onnx" -> "yolo_11n"
            else -> modelId
        }

        return "models/$actualModelId.$extension"
    }

    /**
     * Save downloaded model
     */
    suspend fun saveModel(modelId: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelsDir, "$modelId.tflite")
            modelFile.writeBytes(data)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Copy bundled model to internal storage if needed
     */
    suspend fun copyBundledModelToInternal(modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("models/$modelId.tflite")
            val outputFile = File(modelsDir, "$modelId.tflite")
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

