package com.example.intelli_pest.data.source.local

import android.content.Context
import com.example.intelli_pest.domain.model.ModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages model files storage and access
 */
class ModelFileManager(private val context: Context) {

    private val modelsDir = File(context.filesDir, "models")

    companion object {
        // GitHub release URL for model downloads
        private const val GITHUB_MODELS_URL = "https://github.com/SERVER-246/pest-detection-app/releases/download/v1.0.0-models"
    }

    init {
        // Create models directory if it doesn't exist
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
    }

    /**
     * Get path for a model file
     */
    fun getModelPath(modelId: String): String {
        return File(modelsDir, "$modelId.onnx").absolutePath
    }

    /**
     * Check if model file exists in downloaded folder
     */
    fun isModelDownloaded(modelId: String): Boolean {
        val modelFile = File(modelsDir, "$modelId.onnx")
        return modelFile.exists() && modelFile.length() > 0
    }

    /**
     * Get bundled model path from assets
     */
    fun getBundledModelPath(modelId: String): String {
        return "models/$modelId.onnx"
    }

    /**
     * Check if model is bundled in assets
     */
    suspend fun isModelBundled(modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.assets.open("models/$modelId.onnx").use {
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get model file size in MB
     */
    fun getModelFileSize(modelId: String): Float {
        val modelFile = File(modelsDir, "$modelId.onnx")
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
        val modelFile = File(modelsDir, "$modelId.onnx")
        return if (modelFile.exists()) {
            modelFile.delete()
        } else {
            false
        }
    }

    /**
     * Get all available models (bundled + downloadable)
     */
    suspend fun getAllAvailableModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        val models = mutableListOf<ModelInfo>()

        // 1. Super Ensemble Model (BUNDLED - included in APK)
        models.add(
            ModelInfo(
                id = "super_ensemble",
                name = "super_ensemble",
                displayName = "Super Ensemble",
                description = "High accuracy ensemble model - INCLUDED",
                accuracy = 0.96f,
                inferenceSpeedMs = 450,
                sizeInMb = 544f,
                isDownloaded = isModelDownloaded("super_ensemble"),
                isBundled = isModelBundled("super_ensemble"),
                version = "1.0.0",
                downloadUrl = null // Bundled, no download needed
            )
        )

        // 2. AlexNet (Downloadable)
        models.add(
            ModelInfo(
                id = "alexnet",
                name = "alexnet",
                displayName = "AlexNet",
                description = "Classic deep CNN architecture",
                accuracy = 0.88f,
                inferenceSpeedMs = 200,
                sizeInMb = 172f,
                isDownloaded = isModelDownloaded("alexnet"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/alexnet.onnx"
            )
        )

        // 3. Attention Fusion (Downloadable)
        models.add(
            ModelInfo(
                id = "attention_fusion",
                name = "attention_fusion",
                displayName = "Attention Fusion",
                description = "Attention-based fusion model for enhanced feature learning",
                accuracy = 0.94f,
                inferenceSpeedMs = 280,
                sizeInMb = 371f,
                isDownloaded = isModelDownloaded("attention_fusion"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/attention_fusion.onnx"
            )
        )

        // 4. Concatenation Fusion (Downloadable)
        models.add(
            ModelInfo(
                id = "concatination_fusion",
                name = "concatination_fusion",
                displayName = "Concatenation Fusion",
                description = "Multi-model fusion using concatenation",
                accuracy = 0.93f,
                inferenceSpeedMs = 250,
                sizeInMb = 373f,
                isDownloaded = isModelDownloaded("concatination_fusion"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/concatination_fusion.onnx"
            )
        )

        // 5. Cross-Attention Fusion (Downloadable)
        models.add(
            ModelInfo(
                id = "cross_attention_fusion",
                name = "cross_attention_fusion",
                displayName = "Cross-Attention Fusion",
                description = "Advanced fusion with cross-attention mechanism",
                accuracy = 0.95f,
                inferenceSpeedMs = 320,
                sizeInMb = 399f,
                isDownloaded = isModelDownloaded("cross_attention_fusion"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/cross_attention_fusion.onnx"
            )
        )

        // 6. DarkNet-53 (Downloadable)
        models.add(
            ModelInfo(
                id = "darknet53",
                name = "darknet53",
                displayName = "DarkNet-53",
                description = "Powerful backbone from YOLO architecture",
                accuracy = 0.92f,
                inferenceSpeedMs = 300,
                sizeInMb = 81f,
                isDownloaded = isModelDownloaded("darknet53"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/darknet53.onnx"
            )
        )

        // 7. EfficientNet B0 (Downloadable)
        models.add(
            ModelInfo(
                id = "efficientnet_b0",
                name = "efficientnet_b0",
                displayName = "EfficientNet B0",
                description = "Balanced accuracy and efficiency",
                accuracy = 0.91f,
                inferenceSpeedMs = 120,
                sizeInMb = 18f,
                isDownloaded = isModelDownloaded("efficientnet_b0"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/efficientnet_b0.onnx"
            )
        )

        // 8. Inception V3 (Downloadable)
        models.add(
            ModelInfo(
                id = "inception_v3",
                name = "inception_v3",
                displayName = "Inception V3",
                description = "Google's inception architecture",
                accuracy = 0.92f,
                inferenceSpeedMs = 220,
                sizeInMb = 91f,
                isDownloaded = isModelDownloaded("inception_v3"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/inception_v3.onnx"
            )
        )

        // 9. MobileNet V2 (Downloadable)
        models.add(
            ModelInfo(
                id = "mobilenet_v2",
                name = "mobilenet_v2",
                displayName = "MobileNet V2",
                description = "Lightweight model optimized for mobile devices",
                accuracy = 0.89f,
                inferenceSpeedMs = 80,
                sizeInMb = 12f,
                isDownloaded = isModelDownloaded("mobilenet_v2"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/mobilenet_v2.onnx"
            )
        )

        // 10. ResNet-50 (Downloadable)
        models.add(
            ModelInfo(
                id = "resnet50",
                name = "resnet50",
                displayName = "ResNet-50",
                description = "Deep residual network with high accuracy",
                accuracy = 0.93f,
                inferenceSpeedMs = 200,
                sizeInMb = 98f,
                isDownloaded = isModelDownloaded("resnet50"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/resnet50.onnx"
            )
        )

        // 11. YOLO 11n (Downloadable)
        models.add(
            ModelInfo(
                id = "yolo_11n",
                name = "yolo_11n",
                displayName = "YOLO 11 Nano",
                description = "Ultra-fast detection variant",
                accuracy = 0.87f,
                inferenceSpeedMs = 50,
                sizeInMb = 18f,
                isDownloaded = isModelDownloaded("yolo_11n"),
                isBundled = false,
                downloadUrl = "$GITHUB_MODELS_URL/yolo_11n.onnx"
            )
        )

        models
    }

    /**
     * Save downloaded model
     */
    suspend fun saveModel(modelId: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelsDir, "$modelId.onnx")
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
            val inputStream = context.assets.open("models/$modelId.onnx")
            val outputFile = File(modelsDir, "$modelId.onnx")
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

