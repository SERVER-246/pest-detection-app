package com.example.intelli_pest.presentation.detection

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.usecase.DetectPestUseCase
import com.example.intelli_pest.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for pest detection process
 */
class DetectionViewModel(
    private val detectPestUseCase: DetectPestUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "DetectionViewModel"
    }

    private val _uiState = MutableStateFlow(DetectionUiState())
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    init {
        AppLogger.logInfo("DetectionViewModel", "ViewModel_Created", "DetectionViewModel initialized")
        loadSelectedModel()
    }

    private fun loadSelectedModel() {
        viewModelScope.launch {
            // Get model from preferences (set by MainViewModel)
            preferencesManager.getSelectedModelId().collect { savedModelId ->
                AppLogger.logInfo("DetectionViewModel", "Model_From_Prefs", "Saved model: $savedModelId")
                _uiState.update { it.copy(selectedModelId = savedModelId) }
            }
        }
    }

    fun detectPest(bitmap: Bitmap) {
        viewModelScope.launch {
            val modelId = _uiState.value.selectedModelId
            AppLogger.logAction("DetectionViewModel", "Detection_Started", "Model: $modelId, Bitmap: ${bitmap.width}x${bitmap.height}, Config: ${bitmap.config}")
            Log.d(TAG, "======= DETECTION START =======")
            Log.d(TAG, "Model ID: $modelId")
            Log.d(TAG, "Bitmap: ${bitmap.width}x${bitmap.height}, config=${bitmap.config}")

            _uiState.update { it.copy(isDetecting = true, error = null, capturedBitmap = bitmap) }
            AppLogger.logInfo("DetectionViewModel", "UI_State_Updated", "isDetecting=true, bitmap captured")

            when (val result = detectPestUseCase(bitmap, modelId)) {
                is Resource.Success -> {
                    val data = result.data
                    AppLogger.logResponse(
                        "DetectionViewModel",
                        "Detection_Success",
                        "Pest: ${data.pestType.displayName}, Confidence: ${data.getConfidencePercentage()}, Model: ${data.modelUsed}"
                    )
                    Log.d(TAG, "✅ Detection SUCCESS")
                    Log.d(TAG, "Result: ${data.pestType.displayName} (${data.getConfidencePercentage()})")
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            detectionResult = data,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    AppLogger.logError("DetectionViewModel", "Detection_Failed", result.message, "UseCase returned error")
                    Log.e(TAG, "❌ Detection FAILED: ${result.message}")
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            error = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    AppLogger.logInfo("DetectionViewModel", "Detection_Loading", "Detection in progress...")
                    Log.d(TAG, "Detection in progress...")
                }
            }
            Log.d(TAG, "======= DETECTION END =======")
        }
    }

    fun clearResult() {
        AppLogger.logAction("DetectionViewModel", "Clear_Result", "Clearing detection result")
        _uiState.update {
            DetectionUiState(selectedModelId = it.selectedModelId)
        }
    }

    fun dismissError() {
        AppLogger.logAction("DetectionViewModel", "Dismiss_Error", "User dismissed error")
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
    val selectedModelId: String = "mobilenet_v2" // Default to bundled model
)



