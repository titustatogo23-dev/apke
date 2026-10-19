package com.example.data.repository

import com.example.data.local.AppointmentDao
import com.example.data.local.HealthMetricDao
import com.example.data.local.MedicationDao
import com.example.data.local.PatientDao
import com.example.data.model.Appointment
import com.example.data.model.ClinicSlot
import com.example.data.model.HealthMetricRecord
import com.example.data.model.MedicationRecord
import com.example.data.model.PatientProfile
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class EHealthRepository(
    private val patientDao: PatientDao,
    private val appointmentDao: AppointmentDao,
    private val healthMetricDao: HealthMetricDao,
    private val medicationDao: MedicationDao
) {
    val patientProfile: Flow<PatientProfile?> = patientDao.getPatientProfile()
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
    val upcomingAppointments: Flow<List<Appointment>> = appointmentDao.getUpcomingAppointments()
    val latestHealthMetric: Flow<HealthMetricRecord?> = healthMetricDao.getLatestHealthMetric()
    val allMedications: Flow<List<MedicationRecord>> = medicationDao.getAllMedications()

    fun getAppointmentsForDate(year: Int, month: Int, day: Int): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForDate(year, month, day)
    }

    suspend fun savePatientProfile(profile: PatientProfile) {
        patientDao.insertOrUpdateProfile(profile)
    }

    suspend fun getPatientProfileOnce(): PatientProfile? {
        return patientDao.getPatientProfileOnce()
    }

    suspend fun saveHealthMetric(record: HealthMetricRecord): Long {
        return healthMetricDao.insertMetric(record)
    }

    suspend fun addMedication(medication: MedicationRecord): Long {
        return medicationDao.insertMedication(medication)
    }

    suspend fun updateMedicationTaken(id: Long, isTaken: Boolean) {
        medicationDao.updateTakenStatus(id, isTaken)
    }

    suspend fun updateMedicationReminder(id: Long, isActive: Boolean) {
        medicationDao.updateReminderStatus(id, isActive)
    }

    suspend fun deleteMedication(id: Long) {
        medicationDao.deleteMedication(id)
    }

    suspend fun bookAppointment(
        faskesName: String,
        poliName: String,
        doctorName: String,
        year: Int,
        month: Int,
        day: Int,
        timeSlot: String,
        paymentMethod: String,
        insuranceNumber: String,
        patientNik: String,
        patientName: String
    ): Appointment {
        val prefix = when {
            poliName.contains("Umum", ignoreCase = true) -> "UM"
            poliName.contains("Gigi", ignoreCase = true) -> "GG"
            poliName.contains("KIA", ignoreCase = true) -> "KIA"
            poliName.contains("Anak", ignoreCase = true) -> "ANK"
            poliName.contains("Dalam", ignoreCase = true) -> "INT"
            poliName.contains("Mata", ignoreCase = true) -> "MAT"
            else -> "POL"
        }
        val randomNum = (10..30).random()
        val queueCode = "$prefix-${String.format("%03d", randomNum)}"
        val bookingCode = "SATU-RME-${(10000..99999).random()}"

        val appointment = Appointment(
            queueCode = queueCode,
            bookingCode = bookingCode,
            patientNik = patientNik,
            patientName = patientName,
            faskesName = faskesName,
            poliName = poliName,
            doctorName = doctorName,
            year = year,
            month = month,
            day = day,
            timeSlot = timeSlot,
            paymentMethod = paymentMethod,
            insuranceNumber = insuranceNumber,
            status = "TERKONFIRMASI"
        )
        val id = appointmentDao.insertAppointment(appointment)
        return appointment.copy(id = id)
    }

    suspend fun cancelAppointment(id: Long) {
        appointmentDao.updateAppointmentStatus(id, "DIBATALKAN")
    }

    suspend fun completeAppointment(id: Long) {
        appointmentDao.updateAppointmentStatus(id, "SELESAI")
    }

    suspend fun seedInitialDataIfEmpty() {
        if (patientDao.getPatientProfileOnce() == null) {
            val initialProfile = PatientProfile(
                nik = "3276011508920003",
                fullName = "Rian Hidayat Pratama",
                birthPlace = "Bandung",
                birthDate = "1992-08-15",
                gender = "L",
                bloodType = "O",
                whatsapp = "081298765432",
                address = "Jl. Merdeka No. 45, RT 03/RW 04, Kel. Sehat Mandiri",
                chronicDiseases = "Hipertensi",
                hasDrugAllergy = true,
                drugAllergyDetail = "Amoxicillin, Penicillin",
                paymentMethod = "BPJS",
                insuranceCardNumber = "0001827394851",
                satusehatId = "SATU-3276-8821-99"
            )
            patientDao.insertOrUpdateProfile(initialProfile)

            // Seed sample upcoming appointment for current day or near future
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)

            val seedAppointment = Appointment(
                queueCode = "UM-007",
                bookingCode = "SATU-RME-77124",
                patientNik = initialProfile.nik,
                patientName = initialProfile.fullName,
                faskesName = "Puskesmas Kecamatan Sehat",
                poliName = "Poli Umum",
                doctorName = "dr. Farhan Nurcahyo",
                year = year,
                month = month,
                day = day,
                timeSlot = "08:00 - 10:00 (Sesi Pagi)",
                paymentMethod = "BPJS Kesehatan",
                insuranceNumber = initialProfile.insuranceCardNumber,
                status = "TERKONFIRMASI"
            )
            appointmentDao.insertAppointment(seedAppointment)
        }

        // Seed initial health metrics and lab results if none exist
        if (healthMetricDao.getLatestHealthMetricOnce() == null) {
            val seedMetric = HealthMetricRecord(
                patientNik = "3276011508920003",
                systolicBp = 118,
                diastolicBp = 78,
                bpStatus = "Normal",
                heartRate = 74,
                bloodGlucose = 96,
                cholesterolTotal = 175,
                hemoglobin = 14.2,
                labName = "Laboratorium Puskesmas Kecamatan Sehat",
                labDate = "15 Okt 2026",
                labSummary = "Profil Lipid & Darah Lengkap dalam Ambang Batas Normal"
            )
            healthMetricDao.insertMetric(seedMetric)
        }

        // Seed initial medications if none exist
        if (medicationDao.getMedicationCount() == 0) {
            medicationDao.insertMedication(
                MedicationRecord(
                    name = "Amlodipine",
                    dosage = "5 mg",
                    frequency = "1x Sehari",
                    scheduleTimes = "08:00",
                    instruction = "Sesudah Makan",
                    isReminderActive = true,
                    isTakenToday = true,
                    notes = "Untuk kontrol tekanan darah stabil"
                )
            )
            medicationDao.insertMedication(
                MedicationRecord(
                    name = "Vitamin B Kompleks",
                    dosage = "1 Tablet",
                    frequency = "1x Sehari",
                    scheduleTimes = "12:00",
                    instruction = "Sesudah Makan",
                    isReminderActive = true,
                    isTakenToday = false,
                    notes = "Suplemen stamina & saraf"
                )
            )
            medicationDao.insertMedication(
                MedicationRecord(
                    name = "Paracetamol (Bila Perlu)",
                    dosage = "500 mg",
                    frequency = "3x Sehari",
                    scheduleTimes = "08:00, 14:00, 20:00",
                    instruction = "Sesudah Makan",
                    isReminderActive = false,
                    isTakenToday = false,
                    notes = "Pereda demam atau nyeri ringan"
                )
            )
        }
    }

    fun getAvailableSlotsForDate(
        faskesName: String,
        selectedPoli: String,
        year: Int,
        month: Int,
        day: Int
    ): List<ClinicSlot> {
        val slots = mutableListOf<ClinicSlot>()
        val daySeed = (year * 10000 + month * 100 + day)

        val clinicDefinitions = listOf(
            Triple("Poli Umum", "dr. Farhan Nurcahyo", "R. 101"),
            Triple("Poli Umum", "dr. Kartika Dewi", "R. 102"),
            Triple("Poli Gigi & Mulut", "drg. Anindya Putri, Sp.KGA", "R. 104"),
            Triple("Poli KIA (Ibu & Anak)", "Bidan Rahmawati, S.Tr.Keb", "R. 105"),
            Triple("Poli Penyakit Dalam", "dr. Budi Santoso, Sp.PD", "R. 201"),
            Triple("Poli Anak", "dr. Hendra Wijaya, Sp.A", "R. 203"),
            Triple("Poli Mata", "dr. Silvia Wardani, Sp.M", "R. 206")
        )

        for ((poli, doctor, room) in clinicDefinitions) {
            if (selectedPoli != "Semua Poli" && selectedPoli != poli) continue

            // Morning Session
            val morningBooked = ((daySeed + poli.hashCode()) % 8).coerceAtLeast(1)
            slots.add(
                ClinicSlot(
                    id = "${poli}-pagi",
                    poliName = poli,
                    doctorName = doctor,
                    faskesName = faskesName,
                    timeSlot = "08:00 - 10:00",
                    sessionName = "Pagi",
                    totalQuota = 20,
                    bookedQuota = morningBooked,
                    roomNumber = room
                )
            )

            // Midday Session
            val midBooked = ((daySeed + doctor.hashCode()) % 10).coerceAtLeast(2)
            slots.add(
                ClinicSlot(
                    id = "${poli}-siang",
                    poliName = poli,
                    doctorName = doctor,
                    faskesName = faskesName,
                    timeSlot = "10:30 - 12:30",
                    sessionName = "Siang",
                    totalQuota = 15,
                    bookedQuota = midBooked,
                    roomNumber = room
                )
            )

            // Afternoon Session
            val aftBooked = ((daySeed + room.hashCode()) % 12).coerceAtLeast(3)
            slots.add(
                ClinicSlot(
                    id = "${poli}-sore",
                    poliName = poli,
                    doctorName = doctor,
                    faskesName = faskesName,
                    timeSlot = "13:30 - 15:30",
                    sessionName = "Sore",
                    totalQuota = 15,
                    bookedQuota = aftBooked,
                    roomNumber = room
                )
            )
        }

        return slots
    }
}
