package com.example.intelli_pest.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.domain.model.ModelInfo
import com.example.intelli_pest.domain.usecase.GetAvailableModelsUseCase
import com.example.intelli_pest.domain.usecase.GetDetectionHistoryUseCase
import com.example.intelli_pest.util.AppLogger
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
    private val getDetectionHistoryUseCase: GetDetectionHistoryUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels.asStateFlow()

    private val _selectedModelId = MutableStateFlow<String?>(null)
    val selectedModelId: StateFlow<String?> = _selectedModelId.asStateFlow()

    private val _currentRuntime = MutableStateFlow("tflite")
    val currentRuntime: StateFlow<String> = _currentRuntime.asStateFlow()

    init {
        loadData()
        loadRuntime()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load available models
            launch {
                getAvailableModelsUseCase().collect { models ->
                    Log.d(TAG, "Loaded ${models.size} models")
                    _availableModels.value = models

                    val selectedModel = models.firstOrNull { it.isAvailable() }
                    _selectedModelId.value = selectedModel?.id
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

    private fun loadRuntime() {
        viewModelScope.launch {
            preferencesManager.getMLRuntime().collect { runtime ->
                _currentRuntime.value = runtime.value
                Log.d(TAG, "Current runtime: ${runtime.value}")
            }
        }
    }

    fun selectModel(modelId: String) {
        Log.d(TAG, "Model selected: $modelId")
        AppLogger.logAction("MainViewModel", "Model_Selected", "Model: $modelId, Runtime: ${_currentRuntime.value}")
        _selectedModelId.value = modelId
        val selectedModel = _availableModels.value.find { it.id == modelId }
        _uiState.update { it.copy(selectedModel = selectedModel) }

        // Save preference
        viewModelScope.launch {
            preferencesManager.setSelectedModelId(modelId)
        }
    }

    fun setMLRuntime(runtime: String) {
        Log.d(TAG, "Setting ML runtime to: $runtime")
        AppLogger.logAction("MainViewModel", "Runtime_Changed", "New runtime: $runtime")

        viewModelScope.launch {
            val mlRuntime = PreferencesManager.MLRuntime.fromValue(runtime)
            preferencesManager.setMLRuntime(mlRuntime)
            _currentRuntime.value = runtime
        }
    }

    fun downloadModel(modelId: String) {
        Log.d(TAG, "Download requested for model: $modelId")
        AppLogger.logAction("MainViewModel", "Download_Requested", "Model: $modelId")
        // TODO: Implement model download functionality
        _uiState.update { it.copy(error = "Model download not yet implemented. Models available on GitHub releases.") }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(showOnboarding = false) }
    }
}



