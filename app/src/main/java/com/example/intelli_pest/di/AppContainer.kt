package com.example.intelli_pest.di

import android.content.Context
import com.example.intelli_pest.data.repository.PestDetectionRepositoryImpl
import com.example.intelli_pest.data.source.local.AppDatabase
import com.example.intelli_pest.data.source.local.ModelFileManager
import com.example.intelli_pest.data.source.local.PreferencesManager
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import com.example.intelli_pest.domain.usecase.DetectPestUseCase
import com.example.intelli_pest.domain.usecase.DownloadModelUseCase
import com.example.intelli_pest.domain.usecase.GetAvailableModelsUseCase
import com.example.intelli_pest.domain.usecase.GetDetectionHistoryUseCase
import com.example.intelli_pest.ml.InferenceEngine
import com.example.intelli_pest.presentation.detection.DetectionViewModel
import com.example.intelli_pest.presentation.main.MainViewModel
import com.example.intelli_pest.presentation.settings.SettingsViewModel

/**
 * Simple dependency injection container
 */
object AppContainer {
    private var repository: PestDetectionRepository? = null
    private var detectPestUseCase: DetectPestUseCase? = null
    private var getAvailableModelsUseCase: GetAvailableModelsUseCase? = null
    private var getDetectionHistoryUseCase: GetDetectionHistoryUseCase? = null
    private var downloadModelUseCase: DownloadModelUseCase? = null
    private var preferencesManager: PreferencesManager? = null
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val database = AppDatabase.getDatabase(context)
        preferencesManager = PreferencesManager(context)
        val modelFileManager = ModelFileManager(context)
        val inferenceEngine = InferenceEngine(context, preferencesManager!!)

        repository = PestDetectionRepositoryImpl(
            context = context,
            database = database,
            preferencesManager = preferencesManager!!,
            modelFileManager = modelFileManager,
            inferenceEngine = inferenceEngine
        )

        detectPestUseCase = DetectPestUseCase(repository!!)
        getAvailableModelsUseCase = GetAvailableModelsUseCase(repository!!)
        getDetectionHistoryUseCase = GetDetectionHistoryUseCase(repository!!)
        downloadModelUseCase = DownloadModelUseCase(repository!!)
    }

    fun provideDetectionViewModel(): DetectionViewModel {
        return DetectionViewModel(
            detectPestUseCase = detectPestUseCase!!,
            preferencesManager = preferencesManager!!
        )
    }

    fun provideMainViewModel(): MainViewModel {
        return MainViewModel(
            getAvailableModelsUseCase = getAvailableModelsUseCase!!,
            getDetectionHistoryUseCase = getDetectionHistoryUseCase!!,
            preferencesManager = preferencesManager!!
        )
    }

    fun provideSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(
            preferencesManager = preferencesManager!!,
            context = appContext!!
        )
    }
}

