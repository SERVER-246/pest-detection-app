package com.example.intelli_pest.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore for app preferences
 */
class PreferencesManager(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

    companion object {
        private val CONFIDENCE_THRESHOLD = floatPreferencesKey("confidence_threshold")
        private val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        private val FIRST_LAUNCH = stringPreferencesKey("first_launch")
        private val TRACKING_MODE_ENABLED = booleanPreferencesKey("tracking_mode_enabled")
        private val ML_RUNTIME = stringPreferencesKey("ml_runtime") // "tflite" or "onnx"
    }

    // ML Runtime enum for type safety
    enum class MLRuntime(val value: String) {
        TFLITE("tflite"),
        ONNX("onnx");

        companion object {
            fun fromValue(value: String): MLRuntime {
                return entries.find { it.value == value } ?: TFLITE
            }
        }
    }

    /**
     * Get confidence threshold
     */
    fun getConfidenceThreshold(): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[CONFIDENCE_THRESHOLD] ?: 0.7f
        }
    }

    /**
     * Set confidence threshold
     */
    suspend fun setConfidenceThreshold(threshold: Float) {
        context.dataStore.edit { preferences ->
            preferences[CONFIDENCE_THRESHOLD] = threshold
        }
    }

    /**
     * Get selected model ID
     */
    fun getSelectedModelId(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[SELECTED_MODEL_ID] ?: "super_ensemble"
        }
    }

    /**
     * Set selected model ID
     */
    suspend fun setSelectedModelId(modelId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_ID] = modelId
        }
    }

    /**
     * Check if first launch
     */
    fun isFirstLaunch(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            (preferences[FIRST_LAUNCH] ?: "notcompleted") == "notcompleted"
        }
    }

    /**
     * Mark first launch completed
     */
    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = "completed"
        }
    }

    /**
     * Get tracking mode enabled
     */
    fun isTrackingModeEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[TRACKING_MODE_ENABLED] ?: true // Default to enabled
        }
    }

    /**
     * Set tracking mode enabled
     */
    suspend fun setTrackingModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TRACKING_MODE_ENABLED] = enabled
        }
    }

    /**
     * Get tracking mode enabled synchronously (for AppLogger)
     */
    suspend fun isTrackingModeEnabledSync(): Boolean {
        return context.dataStore.data.first()[TRACKING_MODE_ENABLED] ?: true // Default to enabled
    }

    /**
     * Get ML runtime preference
     */
    fun getMLRuntime(): Flow<MLRuntime> {
        return context.dataStore.data.map { preferences ->
            MLRuntime.fromValue(preferences[ML_RUNTIME] ?: MLRuntime.TFLITE.value)
        }
    }

    /**
     * Set ML runtime preference
     */
    suspend fun setMLRuntime(runtime: MLRuntime) {
        context.dataStore.edit { preferences ->
            preferences[ML_RUNTIME] = runtime.value
        }
    }

    /**
     * Get ML runtime synchronously
     */
    suspend fun getMLRuntimeSync(): MLRuntime {
        val value = context.dataStore.data.first()[ML_RUNTIME] ?: MLRuntime.TFLITE.value
        return MLRuntime.fromValue(value)
    }
}

