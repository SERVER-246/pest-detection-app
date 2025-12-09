package com.example.intelli_pest.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelli_pest.domain.usecase.GetAvailableModelsUseCase
import com.example.intelli_pest.domain.usecase.GetDetectionHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for main screen
 */
class MainViewModel(
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val getDetectionHistoryUseCase: GetDetectionHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load available models
            launch {
                getAvailableModelsUseCase().collect { models ->
                    val selectedModel = models.firstOrNull { it.isAvailable() }
                    _uiState.update { it.copy(selectedModel = selectedModel) }
                }
            }

            // Load recent detections
            launch {
                getDetectionHistoryUseCase().collect { history ->
                    _uiState.update { it.copy(recentDetections = history.take(5)) }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(showOnboarding = false) }
    }
}

