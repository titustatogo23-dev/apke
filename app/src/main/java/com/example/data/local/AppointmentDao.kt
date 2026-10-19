package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY year DESC, month DESC, day DESC, id DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE year = :year AND month = :month AND day = :day ORDER BY id ASC")
    fun getAppointmentsForDate(year: Int, month: Int, day: Int): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = 'TERKONFIRMASI' ORDER BY year ASC, month ASC, day ASC LIMIT 5")
    fun getUpcomingAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Long)
}
