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

/**
 * Simple dependency injection container
 */
object AppContainer {
    private var repository: PestDetectionRepository? = null
    private var detectPestUseCase: DetectPestUseCase? = null
    private var getAvailableModelsUseCase: GetAvailableModelsUseCase? = null
    private var getDetectionHistoryUseCase: GetDetectionHistoryUseCase? = null
    private var downloadModelUseCase: DownloadModelUseCase? = null

    fun initialize(context: Context) {
        val database = AppDatabase.getDatabase(context)
        val preferencesManager = PreferencesManager(context)
        val modelFileManager = ModelFileManager(context)
        val inferenceEngine = InferenceEngine(context)

        repository = PestDetectionRepositoryImpl(
            context = context,
            database = database,
            preferencesManager = preferencesManager,
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
            getAvailableModelsUseCase = getAvailableModelsUseCase!!
        )
    }

    fun provideMainViewModel(): MainViewModel {
        return MainViewModel(
            getAvailableModelsUseCase = getAvailableModelsUseCase!!,
            getDetectionHistoryUseCase = getDetectionHistoryUseCase!!
        )
    }
}

