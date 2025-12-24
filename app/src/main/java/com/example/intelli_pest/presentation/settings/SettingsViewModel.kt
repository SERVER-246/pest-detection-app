package com.example.intelli_pest.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.data.source.local.PreferencesManager.MLRuntime
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for Settings Screen
 */
class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load tracking mode
            preferencesManager.isTrackingModeEnabled().collect { enabled ->
                _uiState.update { it.copy(trackingModeEnabled = enabled) }
                AppLogger.setTrackingMode(enabled)
            }
        }

        viewModelScope.launch {
            // Load ML runtime
            preferencesManager.getMLRuntime().collect { runtime ->
                _uiState.update { it.copy(mlRuntime = runtime) }
            }
        }

        viewModelScope.launch {
            // Load confidence threshold
            preferencesManager.getConfidenceThreshold().collect { threshold ->
                _uiState.update { it.copy(confidenceThreshold = threshold) }
            }
        }

        // Get app version
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _uiState.update { it.copy(appVersion = packageInfo.versionName ?: "1.0.0") }
        } catch (_: Exception) {
            // Use default version
        }
    }

    fun setTrackingMode(enabled: Boolean) {
        viewModelScope.launch {
            AppLogger.logAction("Settings", "Tracking_Mode_Changed", "Enabled: $enabled")
            preferencesManager.setTrackingModeEnabled(enabled)
            AppLogger.setTrackingMode(enabled)
            _uiState.update { it.copy(trackingModeEnabled = enabled) }
        }
    }

    fun setMLRuntime(runtime: MLRuntime) {
        viewModelScope.launch {
            AppLogger.logAction("Settings", "ML_Runtime_Changed", "Runtime: ${runtime.value}")
            preferencesManager.setMLRuntime(runtime)
            val runtimeName = when (runtime) {
                MLRuntime.ONNX -> "ONNX Runtime"
                MLRuntime.TFLITE -> "PyTorch Mobile" // TFLITE enum repurposed for PyTorch
            }
            _uiState.update {
                it.copy(
                    mlRuntime = runtime,
                    message = "ML Runtime changed to $runtimeName. Changes applied immediately."
                )
            }
        }
    }

    fun setConfidenceThreshold(threshold: Float) {
        viewModelScope.launch {
            AppLogger.logAction("Settings", "Confidence_Threshold_Changed", "Threshold: $threshold")
            preferencesManager.setConfidenceThreshold(threshold)
            _uiState.update { it.copy(confidenceThreshold = threshold) }
        }
    }

    fun downloadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            AppLogger.logAction("Settings", "Download_Logs_Clicked", "Attempting to download logs")

            val filePath = AppLogger.downloadLogsToAppStorage()

            if (filePath != null) {
                AppLogger.logResponse("Settings", "Download_Logs_Success", "Logs saved to: $filePath")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        logFilePath = filePath,
                        message = "Logs saved to: $filePath"
                    )
                }
            } else {
                AppLogger.logError("Settings", "Download_Logs_Failed", "Failed to download logs")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Failed to download logs"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearLogFilePath() {
        _uiState.update { it.copy(logFilePath = null) }
    }

    fun shareLogs(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            AppLogger.logAction("Settings", "Share_Logs_Clicked", "Attempting to share logs")

            try {
                val logFile = AppLogger.getShareableLogFile()
                if (logFile != null && logFile.exists()) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        logFile
                    )
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Intelli-PEST App Logs")
                        putExtra(android.content.Intent.EXTRA_TEXT, "Please find attached the application logs for debugging.")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Logs"))
                    AppLogger.logResponse("Settings", "Share_Logs_Success", "Share intent launched")
                    _uiState.update { it.copy(isLoading = false, message = "Logs shared successfully") }
                } else {
                    AppLogger.logError("Settings", "Share_Logs_Failed", "Log file is null or doesn't exist")
                    _uiState.update { it.copy(isLoading = false, message = "No logs available to share") }
                }
            } catch (e: Exception) {
                AppLogger.logError("Settings", "Share_Logs_Error", e, "Exception while sharing logs")
                _uiState.update { it.copy(isLoading = false, message = "Failed to share logs: ${e.message}") }
            }
        }
    }
}

