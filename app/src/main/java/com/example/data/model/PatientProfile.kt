package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patient_profile")
data class PatientProfile(
    @PrimaryKey val nik: String,
    val fullName: String,
    val birthPlace: String,
    val birthDate: String,
    val gender: String, // "L" or "P"
    val bloodType: String, // "-", "A", "B", "AB", "O"
    val whatsapp: String,
    val address: String,
    val chronicDiseases: String, // comma-separated e.g. "Diabetes, Hipertensi"
    val hasDrugAllergy: Boolean,
    val drugAllergyDetail: String,
    val paymentMethod: String, // "Umum", "BPJS", "Asuransi"
    val insuranceCardNumber: String,
    val satusehatId: String,
    val updatedAt: Long = System.currentTimeMillis()
)
