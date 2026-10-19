package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HealthMetricRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    @Query("SELECT * FROM health_metrics ORDER BY recordedAt DESC LIMIT 1")
    fun getLatestHealthMetric(): Flow<HealthMetricRecord?>

    @Query("SELECT * FROM health_metrics ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatestHealthMetricOnce(): HealthMetricRecord?

    @Query("SELECT * FROM health_metrics ORDER BY recordedAt DESC")
    fun getAllHealthMetrics(): Flow<List<HealthMetricRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(record: HealthMetricRecord): Long

    @Query("DELETE FROM health_metrics")
    suspend fun clearMetrics()
}
