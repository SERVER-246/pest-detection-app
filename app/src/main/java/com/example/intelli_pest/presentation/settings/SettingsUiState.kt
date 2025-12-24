package com.example.intelli_pest.presentation.settings

import com.example.intelli_pest.data.source.local.PreferencesManager.MLRuntime

/**
 * UI State for Settings Screen
 */
data class SettingsUiState(
    val trackingModeEnabled: Boolean = true, // Default to enabled for debugging
    val mlRuntime: MLRuntime = MLRuntime.ONNX, // Default to ONNX (recommended)
    val confidenceThreshold: Float = 0.7f,
    val isLoading: Boolean = false,
    val message: String? = null,
    val logFilePath: String? = null,
    val appVersion: String = "1.0.0"
)

