package com.example.intelli_pest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.intelli_pest.domain.model.DetectionResult
import com.example.intelli_pest.domain.model.PestType

/**
 * Room entity for storing detection history
 */
@Entity(tableName = "detection_history")
data class DetectionResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pestTypeName: String,
    val confidence: Float,
    val imageUri: String,
    val timestamp: Long,
    val modelUsed: String,
    val processingTimeMs: Long
) {
    fun toDomain(): DetectionResult {
        return DetectionResult(
            pestType = PestType.fromDisplayName(pestTypeName) ?: PestType.HEALTHY,
            confidence = confidence,
            imageUri = imageUri,
            timestamp = timestamp,
            modelUsed = modelUsed,
            processingTimeMs = processingTimeMs
        )
    }

    companion object {
        fun fromDomain(result: DetectionResult): DetectionResultEntity {
            return DetectionResultEntity(
                pestTypeName = result.pestType.displayName,
                confidence = result.confidence,
                imageUri = result.imageUri,
                timestamp = result.timestamp,
                modelUsed = result.modelUsed,
                processingTimeMs = result.processingTimeMs
            )
        }
    }
}

