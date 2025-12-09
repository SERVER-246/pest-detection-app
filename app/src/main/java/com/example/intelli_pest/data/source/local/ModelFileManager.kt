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
     * Check if model file exists
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
     * Get all available models (both bundled and downloaded)
     * Updated to match actual model files in assets
     */
    suspend fun getAllAvailableModels(): List<ModelInfo> = withContext(Dispatchers.IO) {
        val models = mutableListOf<ModelInfo>()

        // 1. Super Ensemble Model (Main - bundled)
        models.add(
            ModelInfo(
                id = "super_ensemble",
                name = "super_ensemble",
                displayName = "Super Ensemble",
                description = "High accuracy ensemble model combining multiple architectures",
                accuracy = 0.96f,
                inferenceSpeedMs = 450,
                sizeInMb = 85f,
                isDownloaded = isModelDownloaded("super_ensemble"),
                isBundled = isModelBundled("super_ensemble"),
                version = "1.0.0"
            )
        )

        // 2. AlexNet
        models.add(
            ModelInfo(
                id = "alexnet",
                name = "alexnet",
                displayName = "AlexNet",
                description = "Classic deep CNN architecture",
                accuracy = 0.88f,
                inferenceSpeedMs = 200,
                sizeInMb = 240f,
                isDownloaded = isModelDownloaded("alexnet"),
                isBundled = isModelBundled("alexnet"),
                downloadUrl = null
            )
        )

        // 3. Attention Fusion
        models.add(
            ModelInfo(
                id = "attention_fusion",
                name = "attention_fusion",
                displayName = "Attention Fusion",
                description = "Attention-based fusion model for enhanced feature learning",
                accuracy = 0.94f,
                inferenceSpeedMs = 280,
                sizeInMb = 90f,
                isDownloaded = isModelDownloaded("attention_fusion"),
                isBundled = isModelBundled("attention_fusion"),
                downloadUrl = null
            )
        )

        // 4. Concatenation Fusion
        models.add(
            ModelInfo(
                id = "concatination_fusion",
                name = "concatination_fusion",
                displayName = "Concatenation Fusion",
                description = "Multi-model fusion using concatenation",
                accuracy = 0.93f,
                inferenceSpeedMs = 250,
                sizeInMb = 95f,
                isDownloaded = isModelDownloaded("concatination_fusion"),
                isBundled = isModelBundled("concatination_fusion"),
                downloadUrl = null
            )
        )

        // 5. Cross-Attention Fusion
        models.add(
            ModelInfo(
                id = "cross_attention_fusion",
                name = "cross_attention_fusion",
                displayName = "Cross-Attention Fusion",
                description = "Advanced fusion with cross-attention mechanism",
                accuracy = 0.95f,
                inferenceSpeedMs = 320,
                sizeInMb = 100f,
                isDownloaded = isModelDownloaded("cross_attention_fusion"),
                isBundled = isModelBundled("cross_attention_fusion"),
                downloadUrl = null
            )
        )

        // 6. DarkNet-53
        models.add(
            ModelInfo(
                id = "darknet53",
                name = "darknet53",
                displayName = "DarkNet-53",
                description = "Powerful backbone from YOLO architecture",
                accuracy = 0.92f,
                inferenceSpeedMs = 300,
                sizeInMb = 160f,
                isDownloaded = isModelDownloaded("darknet53"),
                isBundled = isModelBundled("darknet53"),
                downloadUrl = null
            )
        )

        // 7. EfficientNet B0 (note: filename has typo efficentnet)
        models.add(
            ModelInfo(
                id = "efficentnet_b0",
                name = "efficentnet_b0",
                displayName = "EfficientNet B0",
                description = "Balanced accuracy and efficiency",
                accuracy = 0.91f,
                inferenceSpeedMs = 120,
                sizeInMb = 20f,
                isDownloaded = isModelDownloaded("efficentnet_b0"),
                isBundled = isModelBundled("efficentnet_b0"),
                downloadUrl = null
            )
        )

        // 8. Inception V3
        models.add(
            ModelInfo(
                id = "inception_v3",
                name = "inception_v3",
                displayName = "Inception V3",
                description = "Google's inception architecture with auxiliary classifiers",
                accuracy = 0.92f,
                inferenceSpeedMs = 220,
                sizeInMb = 90f,
                isDownloaded = isModelDownloaded("inception_v3"),
                isBundled = isModelBundled("inception_v3"),
                downloadUrl = null
            )
        )

        // 9. MobileNet V2
        models.add(
            ModelInfo(
                id = "mobilenet_v2",
                name = "mobilenet_v2",
                displayName = "MobileNet V2",
                description = "Lightweight model optimized for mobile devices",
                accuracy = 0.89f,
                inferenceSpeedMs = 80,
                sizeInMb = 14f,
                isDownloaded = isModelDownloaded("mobilenet_v2"),
                isBundled = isModelBundled("mobilenet_v2"),
                downloadUrl = null
            )
        )

        // 10. ResNet-50
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
                isBundled = isModelBundled("resnet50"),
                downloadUrl = null
            )
        )

        // 11. YOLO 11n
        models.add(
            ModelInfo(
                id = "yolo_11n",
                name = "yolo_11n",
                displayName = "YOLO 11 Nano",
                description = "Ultra-fast YOLO object detection variant",
                accuracy = 0.87f,
                inferenceSpeedMs = 50,
                sizeInMb = 8f,
                isDownloaded = isModelDownloaded("yolo_11n"),
                isBundled = isModelBundled("yolo_11n"),
                downloadUrl = null
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
}

