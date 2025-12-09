package com.example.intelli_pest.domain.usecase

import com.example.intelli_pest.domain.model.Resource
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for downloading models
 */
class DownloadModelUseCase(
    private val repository: PestDetectionRepository
) {
    suspend operator fun invoke(modelId: String): Flow<Resource<Float>> {
        return repository.downloadModel(modelId)
    }
}

