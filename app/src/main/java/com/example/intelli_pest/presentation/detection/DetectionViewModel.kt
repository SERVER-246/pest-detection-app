package com.example.intelli_pest.presentation.detection

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.usecase.DetectPestUseCase
import com.example.intelli_pest.domain.usecase.GetAvailableModelsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for pest detection process
 */
class DetectionViewModel(
    private val detectPestUseCase: DetectPestUseCase,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    init {
        loadSelectedModel()
    }

    private fun loadSelectedModel() {
        viewModelScope.launch {
            val models = getAvailableModelsUseCase().firstOrNull() ?: emptyList()
            val selectedModel = models.firstOrNull { it.isAvailable() }
            _uiState.update { it.copy(selectedModelId = selectedModel?.id ?: "resnet50") }
        }
    }

    fun detectPest(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetecting = true, error = null, capturedBitmap = bitmap) }

            when (val result = detectPestUseCase(bitmap, _uiState.value.selectedModelId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            detectionResult = result.data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already set to detecting
                }
            }
        }
    }

    fun clearResult() {
        _uiState.update {
            DetectionUiState(selectedModelId = it.selectedModelId)
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for detection
 */
data class DetectionUiState(
    val isDetecting: Boolean = false,
    val detectionResult: DetectionResult? = null,
    val capturedBitmap: Bitmap? = null,
    val error: String? = null,
    val selectedModelId: String = "resnet50"
)



