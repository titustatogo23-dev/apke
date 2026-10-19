package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MedicationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY id DESC")
    fun getAllMedications(): Flow<List<MedicationRecord>>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getMedicationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationRecord): Long

    @Query("UPDATE medications SET isTakenToday = :isTaken WHERE id = :id")
    suspend fun updateTakenStatus(id: Long, isTaken: Boolean)

    @Query("UPDATE medications SET isReminderActive = :isActive WHERE id = :id")
    suspend fun updateReminderStatus(id: Long, isActive: Boolean)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedication(id: Long)
}
