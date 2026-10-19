package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queueCode: String, // e.g. "UM-014", "GG-003", "KIA-008"
    val bookingCode: String, // e.g. "SATU-RME-84912"
    val patientNik: String,
    val patientName: String,
    val faskesName: String, // "Puskesmas Kecamatan Sehat", "RSUD Kota Madani"
    val poliName: String, // "Poli Umum", "Poli Gigi & Mulut", "Poli KIA", etc.
    val doctorName: String,
    val year: Int,
    val month: Int, // 1 - 12
    val day: Int,   // 1 - 31
    val timeSlot: String, // e.g. "08:00 - 10:00 (Sesi Pagi)"
    val paymentMethod: String, // "Umum / Mandiri", "BPJS Kesehatan", "Asuransi Swasta"
    val insuranceNumber: String,
    val status: String = "TERKONFIRMASI", // "TERKONFIRMASI", "SELESAI", "DIBATALKAN"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
