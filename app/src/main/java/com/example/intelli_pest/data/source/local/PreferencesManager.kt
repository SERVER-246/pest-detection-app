package com.example.intelli_pest.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
}

