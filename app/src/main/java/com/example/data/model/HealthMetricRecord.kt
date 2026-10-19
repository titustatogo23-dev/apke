package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_metrics")
data class HealthMetricRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientNik: String,
    val systolicBp: Int, // e.g. 118
    val diastolicBp: Int, // e.g. 78
    val bpStatus: String = "Normal", // "Normal", "Pra-Hipertensi", "Hipertensi"
    val heartRate: Int, // e.g. 74 bpm
    val bloodGlucose: Int, // e.g. 96 mg/dL
    val cholesterolTotal: Int, // e.g. 175 mg/dL
    val hemoglobin: Double, // e.g. 14.2 g/dL
    val labName: String, // e.g. "Laboratorium Puskesmas Kecamatan Sehat"
    val labDate: String, // e.g. "15 Okt 2026"
    val labSummary: String, // e.g. "Profil Lipid & Darah Rutin Terkonfirmasi Normal"
    val recordedAt: Long = System.currentTimeMillis()
)
