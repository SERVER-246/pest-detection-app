package com.example.intelli_pest.domain.usecase

import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.repository.PestDetectionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting detection history
 */
class GetDetectionHistoryUseCase(
    private val repository: PestDetectionRepository
) {
    operator fun invoke(): Flow<List<DetectionResult>> {
        return repository.getDetectionHistory()
    }
}

