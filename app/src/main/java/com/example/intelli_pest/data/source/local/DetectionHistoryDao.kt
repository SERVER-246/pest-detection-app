package com.example.intelli_pest.data.source.local

import androidx.room.*
import com.example.intelli_pest.data.model.DetectionResultEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for detection history
 */
@Dao
interface DetectionHistoryDao {

    @Query("SELECT * FROM detection_history ORDER BY timestamp DESC")
    fun getAllDetections(): Flow<List<DetectionResultEntity>>

    @Query("SELECT * FROM detection_history WHERE id = :id")
    suspend fun getDetectionById(id: Long): DetectionResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetection(detection: DetectionResultEntity): Long

    @Delete
    suspend fun deleteDetection(detection: DetectionResultEntity)

    @Query("DELETE FROM detection_history")
    suspend fun clearAllDetections()

    @Query("SELECT COUNT(*) FROM detection_history")
    suspend fun getDetectionCount(): Int
}

