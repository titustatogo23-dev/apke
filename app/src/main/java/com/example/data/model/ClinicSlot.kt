package com.example.data.model

data class ClinicSlot(
    val id: String,
    val poliName: String,
    val doctorName: String,
    val faskesName: String,
    val timeSlot: String,
    val sessionName: String, // "Pagi", "Siang", "Sore"
    val totalQuota: Int,
    val bookedQuota: Int,
    val roomNumber: String
) {
    val remainingQuota: Int get() = (totalQuota - bookedQuota).coerceAtLeast(0)
    val isAvailable: Boolean get() = remainingQuota > 0
}
