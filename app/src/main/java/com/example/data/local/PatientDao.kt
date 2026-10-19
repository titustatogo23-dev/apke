package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PatientProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patient_profile LIMIT 1")
    fun getPatientProfile(): Flow<PatientProfile?>

    @Query("SELECT * FROM patient_profile LIMIT 1")
    suspend fun getPatientProfileOnce(): PatientProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PatientProfile)

    @Query("DELETE FROM patient_profile")
    suspend fun clearProfile()
}
