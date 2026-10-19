package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Amlodipine", "Metformin"
    val dosage: String, // e.g. "5 mg", "1 Tablet"
    val frequency: String, // e.g. "1x Sehari", "2x Sehari", "3x Sehari"
    val scheduleTimes: String, // comma-separated times, e.g. "08:00" or "08:00, 20:00"
    val instruction: String, // "Sesudah Makan", "Sebelum Makan", "Bersama Makanan"
    val isReminderActive: Boolean = true,
    val isTakenToday: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
