package com.example.intelli_pest.presentation.main

import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.ModelInfo

/**
 * UI state for main screen
 */
data class MainUiState(
    val selectedModel: ModelInfo? = null,
    val recentDetections: List<DetectionResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showOnboarding: Boolean = false
)

