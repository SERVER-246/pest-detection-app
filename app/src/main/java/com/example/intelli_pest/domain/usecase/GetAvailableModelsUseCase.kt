package com.example.intelli_pest.domain.usecase

import com.example.intelli_pest.domain.model.ModelInfo
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting available models
 */
class GetAvailableModelsUseCase(
    private val repository: PestDetectionRepository
) {
    operator fun invoke(): Flow<List<ModelInfo>> {
        return repository.getAvailableModels()
    }
}

