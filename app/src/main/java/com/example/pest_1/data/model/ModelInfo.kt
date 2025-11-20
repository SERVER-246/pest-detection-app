package com.example.pest_1.data.model

/**
 * Represents metadata about an ONNX model
 */
data class ModelInfo(
    val id: String,                    // e.g., "mobilenet_v2"
    val displayName: String,           // e.g., "MobileNet V2 (98.74%)"
    val accuracy: Float,               // e.g., 98.74f
    val sizeInMB: Float,              // e.g., 14.2f
    val inferenceTimeMs: Int,          // e.g., 150 (average)
    val isEnsemble: Boolean = false,
    val isDefault: Boolean = false,    // True if bundled in APK
    val downloadUrl: String? = null    // Null if bundled in APK
)

/**
 * Model download status
 */
sealed class ModelStatus {
    object NotDownloaded : ModelStatus()
    data class Downloading(val progress: Int) : ModelStatus()
    object Downloaded : ModelStatus()
    object Bundled : ModelStatus()  // Included in APK
    data class Error(val message: String) : ModelStatus()
}

/**
 * Predefined models configuration
 */
object ModelCatalog {
    // Base URL for model downloads - CHANGE THIS to your cloud storage URL
    private const val MODEL_BASE_URL = "https://your-storage-url.com/models"

    val models = listOf(
        ModelInfo(
            id = "mobilenet_v2",
            displayName = "MobileNet V2 (98.74%)",
            accuracy = 98.74f,
            sizeInMB = 14.2f,
            inferenceTimeMs = 150,
            isDefault = true,  // Bundled in APK
            downloadUrl = null
        ),
        ModelInfo(
            id = "darknet53",
            displayName = "DarkNet53 (99.38%)",
            accuracy = 99.38f,
            sizeInMB = 162.5f,
            inferenceTimeMs = 450,
            downloadUrl = "$MODEL_BASE_URL/darknet53.zip"
        ),
        ModelInfo(
            id = "resnet50",
            displayName = "ResNet50 (98.74%)",
            accuracy = 98.74f,
            sizeInMB = 97.8f,
            inferenceTimeMs = 300,
            downloadUrl = "$MODEL_BASE_URL/resnet50.zip"
        ),
        ModelInfo(
            id = "yolo11n-cls",
            displayName = "YOLO11n (98.80%)",
            accuracy = 98.80f,
            sizeInMB = 6.5f,
            inferenceTimeMs = 120,
            downloadUrl = "$MODEL_BASE_URL/yolo11n-cls.zip"
        ),
        ModelInfo(
            id = "inception_v3",
            displayName = "Inception V3 (98.58%)",
            accuracy = 98.58f,
            sizeInMB = 91.2f,
            inferenceTimeMs = 350,
            downloadUrl = "$MODEL_BASE_URL/inception_v3.zip"
        ),
        ModelInfo(
            id = "efficientnet_b0",
            displayName = "EfficientNet B0 (98.50%)",
            accuracy = 98.50f,
            sizeInMB = 20.3f,
            inferenceTimeMs = 180,
            downloadUrl = "$MODEL_BASE_URL/efficientnet_b0.zip"
        ),
        ModelInfo(
            id = "alexnet",
            displayName = "AlexNet (98.03%)",
            accuracy = 98.03f,
            sizeInMB = 233.1f,
            inferenceTimeMs = 200,
            downloadUrl = "$MODEL_BASE_URL/alexnet.zip"
        ),
        ModelInfo(
            id = "ensemble_attention",
            displayName = "Ensemble (Attention) (99.88%)",
            accuracy = 99.88f,
            sizeInMB = 145.0f,
            inferenceTimeMs = 800,
            isEnsemble = true,
            downloadUrl = "$MODEL_BASE_URL/ensemble_attention.zip"
        ),
        ModelInfo(
            id = "ensemble_cross",
            displayName = "Ensemble (Cross) (99.79%)",
            accuracy = 99.79f,
            sizeInMB = 152.0f,
            inferenceTimeMs = 850,
            isEnsemble = true,
            downloadUrl = "$MODEL_BASE_URL/ensemble_cross.zip"
        ),
        ModelInfo(
            id = "ensemble_concat",
            displayName = "Ensemble (Concat) (99.76%)",
            accuracy = 99.76f,
            sizeInMB = 148.0f,
            inferenceTimeMs = 820,
            isEnsemble = true,
            downloadUrl = "$MODEL_BASE_URL/ensemble_concat.zip"
        ),
        ModelInfo(
            id = "super_ensemble",
            displayName = "Super Ensemble (99.96%)",
            accuracy = 99.96f,
            sizeInMB = 280.0f,
            inferenceTimeMs = 1500,
            isEnsemble = true,
            downloadUrl = "$MODEL_BASE_URL/super_ensemble.zip"
        )
    )

    fun getModelById(id: String): ModelInfo? = models.find { it.id == id }

    fun getDefaultModel(): ModelInfo = models.first { it.isDefault }
}

