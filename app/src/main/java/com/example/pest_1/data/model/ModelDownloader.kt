package com.example.pest_1.data.model

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * Handles downloading ONNX models from cloud storage
 */
class ModelDownloader(private val context: Context) {

    private val _downloadStatus = MutableStateFlow<Map<String, ModelStatus>>(emptyMap())
    val downloadStatus: StateFlow<Map<String, ModelStatus>> = _downloadStatus

    companion object {
        private const val TAG = "ModelDownloader"
        private const val CONNECT_TIMEOUT_MS = 30000
        private const val READ_TIMEOUT_MS = 60000
        private const val MAX_RETRIES = 3
    }

    /**
     * Download a model from URL and extract it to app's files directory
     */
    suspend fun downloadModel(modelInfo: ModelInfo): Result<File> = withContext(Dispatchers.IO) {
        if (modelInfo.downloadUrl == null) {
            return@withContext Result.failure(Exception("Model is bundled, no download needed"))
        }

        var retryCount = 0
        var lastException: Exception? = null

        while (retryCount < MAX_RETRIES) {
            try {
                Log.d(TAG, "Downloading model: ${modelInfo.id} (attempt ${retryCount + 1})")

                updateStatus(modelInfo.id, ModelStatus.Downloading(0))

                val modelDir = getModelDirectory(modelInfo.id)
                if (modelDir.exists()) {
                    modelDir.deleteRecursively()
                }
                modelDir.mkdirs()

                // Download zip file
                val zipFile = File(context.cacheDir, "${modelInfo.id}.zip")
                downloadFile(modelInfo.downloadUrl, zipFile) { progress ->
                    updateStatus(modelInfo.id, ModelStatus.Downloading(progress))
                }

                // Extract zip
                updateStatus(modelInfo.id, ModelStatus.Downloading(95))
                extractZip(zipFile, modelDir)

                // Cleanup
                zipFile.delete()

                // Verify model.onnx exists
                val modelFile = File(modelDir, "model.onnx")
                if (!modelFile.exists()) {
                    throw Exception("model.onnx not found after extraction")
                }

                updateStatus(modelInfo.id, ModelStatus.Downloaded)
                Log.d(TAG, "✓ Model downloaded successfully: ${modelInfo.id}")

                return@withContext Result.success(modelDir)

            } catch (e: Exception) {
                Log.e(TAG, "Download attempt ${retryCount + 1} failed for ${modelInfo.id}", e)
                lastException = e
                retryCount++

                if (retryCount < MAX_RETRIES) {
                    kotlinx.coroutines.delay(2000L * retryCount) // Exponential backoff
                }
            }
        }

        val error = "Failed to download model after $MAX_RETRIES attempts: ${lastException?.message}"
        updateStatus(modelInfo.id, ModelStatus.Error(error))
        Result.failure(lastException ?: Exception(error))
    }

    /**
     * Download file from URL with progress callback
     */
    private fun downloadFile(url: String, destination: File, onProgress: (Int) -> Unit) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS

        try {
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned HTTP ${connection.responseCode}")
            }

            val fileLength = connection.contentLength

            connection.inputStream.use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        if (fileLength > 0) {
                            val progress = ((totalBytesRead * 90) / fileLength).toInt()
                            onProgress(progress)
                        }
                    }

                    output.flush()
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Extract zip file to directory
     */
    private fun extractZip(zipFile: File, destination: File) {
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry

            while (entry != null) {
                val file = File(destination, entry.name)

                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    FileOutputStream(file).use { output ->
                        zis.copyTo(output)
                    }
                }

                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    /**
     * Check if model is already downloaded
     */
    fun isModelDownloaded(modelId: String): Boolean {
        val modelDir = getModelDirectory(modelId)
        val modelFile = File(modelDir, "model.onnx")
        return modelFile.exists()
    }

    /**
     * Delete downloaded model to free space
     */
    fun deleteModel(modelId: String): Boolean {
        val modelDir = getModelDirectory(modelId)
        return if (modelDir.exists()) {
            modelDir.deleteRecursively()
        } else {
            false
        }
    }

    /**
     * Get total size of downloaded models in MB
     */
    fun getDownloadedModelsSize(): Float {
        val modelsDir = File(context.filesDir, "models")
        return if (modelsDir.exists()) {
            modelsDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum() / (1024f * 1024f)
        } else {
            0f
        }
    }

    /**
     * Get directory for a specific model in app's files directory
     */
    private fun getModelDirectory(modelId: String): File {
        return File(context.filesDir, "models/$modelId")
    }

    /**
     * Update download status
     */
    private fun updateStatus(modelId: String, status: ModelStatus) {
        val currentMap = _downloadStatus.value.toMutableMap()
        currentMap[modelId] = status
        _downloadStatus.value = currentMap
    }
}

