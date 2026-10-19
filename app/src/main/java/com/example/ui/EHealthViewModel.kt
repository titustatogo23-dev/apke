package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Appointment
import com.example.data.model.ClinicSlot
import com.example.data.model.PatientProfile
import com.example.data.repository.EHealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class EHealthScreen {
    DASHBOARD,
    CALENDAR,
    BOOKING,
    RME_REGISTRATION,
    MEDICATION_TRACKER,
    SYMPTOM_CHECKER
}

data class SymptomCheckerUiState(
    val symptomInput: String = "",
    val selectedDuration: String = "1-3 Hari",
    val selectedQuickSymptoms: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val analysisResult: com.example.data.api.SymptomAnalysisResult? = null,
    val errorMessage: String? = null
)

data class CalendarUiState(
    val year: Int,
    val month: Int, // 1 - 12
    val selectedDay: Int,
    val selectedPoli: String = "Semua Poli",
    val selectedFaskes: String = "Puskesmas Kecamatan Sehat",
    val availableSlots: List<ClinicSlot> = emptyList(),
    val selectedSlot: ClinicSlot? = null,
    val paymentMethod: String = "BPJS Kesehatan",
    val cardNumber: String = "",
    val bookingSuccessAppointment: Appointment? = null,
    val activePassAppointment: Appointment? = null
)

data class RmeFormState(
    val nik: String = "",
    val fullName: String = "",
    val birthPlace: String = "",
    val birthDate: String = "1995-05-12",
    val gender: String = "L",
    val bloodType: String = "O",
    val whatsapp: String = "",
    val address: String = "",
    val diseases: Set<String> = emptySet(),
    val hasDrugAllergy: Boolean = false,
    val drugAllergyDetail: String = "",
    val paymentMethod: String = "BPJS",
    val cardNumber: String = "",
    val currentStep: Int = 0,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EHealthViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = EHealthRepository(
        database.patientDao(),
        database.appointmentDao(),
        database.healthMetricDao(),
        database.medicationDao()
    )

    private val _currentScreen = MutableStateFlow(EHealthScreen.DASHBOARD)
    val currentScreen: StateFlow<EHealthScreen> = _currentScreen.asStateFlow()

    private val todayCal = Calendar.getInstance()
    private val initialYear = todayCal.get(Calendar.YEAR)
    private val initialMonth = todayCal.get(Calendar.MONTH) + 1
    private val initialDay = todayCal.get(Calendar.DAY_OF_MONTH)

    private val _calendarState = MutableStateFlow(
        CalendarUiState(
            year = initialYear,
            month = initialMonth,
            selectedDay = initialDay
        )
    )
    val calendarState: StateFlow<CalendarUiState> = _calendarState.asStateFlow()

    private val _rmeForm = MutableStateFlow(RmeFormState())
    val rmeForm: StateFlow<RmeFormState> = _rmeForm.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val patientProfile: StateFlow<PatientProfile?> = repository.patientProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAppointments: StateFlow<List<Appointment>> = repository.upcomingAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestHealthMetric: StateFlow<com.example.data.model.HealthMetricRecord?> = repository.latestHealthMetric
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMedications: StateFlow<List<com.example.data.model.MedicationRecord>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _symptomCheckerState = MutableStateFlow(SymptomCheckerUiState())
    val symptomCheckerState: StateFlow<SymptomCheckerUiState> = _symptomCheckerState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshSlots()
            syncRmeFormWithProfile()
        }
    }

    private suspend fun syncRmeFormWithProfile() {
        val profile = repository.getPatientProfileOnce()
        if (profile != null) {
            val diseaseSet = profile.chronicDiseases
                .split(",")
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
                .toSet()

            _rmeForm.value = RmeFormState(
                nik = profile.nik,
                fullName = profile.fullName,
                birthPlace = profile.birthPlace,
                birthDate = profile.birthDate,
                gender = profile.gender,
                bloodType = profile.bloodType,
                whatsapp = profile.whatsapp,
                address = profile.address,
                diseases = diseaseSet,
                hasDrugAllergy = profile.hasDrugAllergy,
                drugAllergyDetail = profile.drugAllergyDetail,
                paymentMethod = profile.paymentMethod,
                cardNumber = profile.insuranceCardNumber
            )

            _calendarState.value = _calendarState.value.copy(
                paymentMethod = if (profile.paymentMethod.contains("BPJS")) "BPJS Kesehatan" else "Umum / Mandiri",
                cardNumber = profile.insuranceCardNumber
            )
        }
    }

    fun navigateTo(screen: EHealthScreen) {
        _currentScreen.value = screen
    }

    fun selectDate(year: Int, month: Int, day: Int) {
        _calendarState.value = _calendarState.value.copy(
            year = year,
            month = month,
            selectedDay = day,
            selectedSlot = null
        )
        refreshSlots()
    }

    fun changeMonth(delta: Int) {
        var newMonth = _calendarState.value.month + delta
        var newYear = _calendarState.value.year
        if (newMonth < 1) {
            newMonth = 12
            newYear -= 1
        } else if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, newYear)
        cal.set(Calendar.MONTH, newMonth - 1)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val newDay = _calendarState.value.selectedDay.coerceAtMost(maxDay)

        _calendarState.value = _calendarState.value.copy(
            year = newYear,
            month = newMonth,
            selectedDay = newDay,
            selectedSlot = null
        )
        refreshSlots()
    }

    fun filterPoli(poli: String) {
        _calendarState.value = _calendarState.value.copy(
            selectedPoli = poli,
            selectedSlot = null
        )
        refreshSlots()
    }

    fun selectFaskes(faskes: String) {
        _calendarState.value = _calendarState.value.copy(
            selectedFaskes = faskes,
            selectedSlot = null
        )
        refreshSlots()
    }

    fun selectSlot(slot: ClinicSlot) {
        _calendarState.value = _calendarState.value.copy(selectedSlot = slot)
    }

    fun setCalendarPayment(method: String, number: String) {
        _calendarState.value = _calendarState.value.copy(
            paymentMethod = method,
            cardNumber = number
        )
    }

    fun dismissBookingSuccess() {
        _calendarState.value = _calendarState.value.copy(bookingSuccessAppointment = null)
    }

    fun showQueuePass(appointment: Appointment) {
        _calendarState.value = _calendarState.value.copy(activePassAppointment = appointment)
    }

    fun dismissQueuePass() {
        _calendarState.value = _calendarState.value.copy(activePassAppointment = null)
    }

    fun bookSelectedSlot() {
        val state = _calendarState.value
        val slot = state.selectedSlot ?: return
        val currentProfile = patientProfile.value

        val nik = currentProfile?.nik ?: _rmeForm.value.nik.ifEmpty { "3201081508920003" }
        val name = currentProfile?.fullName ?: _rmeForm.value.fullName.ifEmpty { "Pasien SATUSEHAT" }

        viewModelScope.launch {
            val appointment = repository.bookAppointment(
                faskesName = state.selectedFaskes,
                poliName = slot.poliName,
                doctorName = slot.doctorName,
                year = state.year,
                month = state.month,
                day = state.selectedDay,
                timeSlot = "${slot.timeSlot} (${slot.sessionName})",
                paymentMethod = state.paymentMethod,
                insuranceNumber = state.cardNumber,
                patientNik = nik,
                patientName = name
            )

            _calendarState.value = _calendarState.value.copy(
                bookingSuccessAppointment = appointment,
                selectedSlot = null
            )
            refreshSlots()
            _snackbarMessage.value = "Janji Temu Berhasil Didaftarkan! No Antrean: ${appointment.queueCode}"
        }
    }

    fun cancelAppointment(id: Long) {
        viewModelScope.launch {
            repository.cancelAppointment(id)
            _snackbarMessage.value = "Janji temu berhasil dibatalkan"
            if (_calendarState.value.activePassAppointment?.id == id) {
                _calendarState.value = _calendarState.value.copy(activePassAppointment = null)
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun refreshSlots() {
        val state = _calendarState.value
        val slots = repository.getAvailableSlotsForDate(
            faskesName = state.selectedFaskes,
            selectedPoli = state.selectedPoli,
            year = state.year,
            month = state.month,
            day = state.selectedDay
        )
        _calendarState.value = _calendarState.value.copy(availableSlots = slots)
    }

    // RME Form handling
    fun updateRmeForm(
        nik: String? = null,
        fullName: String? = null,
        birthPlace: String? = null,
        birthDate: String? = null,
        gender: String? = null,
        bloodType: String? = null,
        whatsapp: String? = null,
        address: String? = null,
        hasDrugAllergy: Boolean? = null,
        drugAllergyDetail: String? = null,
        paymentMethod: String? = null,
        cardNumber: String? = null,
        currentStep: Int? = null
    ) {
        _rmeForm.value = _rmeForm.value.copy(
            nik = nik ?: _rmeForm.value.nik,
            fullName = fullName ?: _rmeForm.value.fullName,
            birthPlace = birthPlace ?: _rmeForm.value.birthPlace,
            birthDate = birthDate ?: _rmeForm.value.birthDate,
            gender = gender ?: _rmeForm.value.gender,
            bloodType = bloodType ?: _rmeForm.value.bloodType,
            whatsapp = whatsapp ?: _rmeForm.value.whatsapp,
            address = address ?: _rmeForm.value.address,
            hasDrugAllergy = hasDrugAllergy ?: _rmeForm.value.hasDrugAllergy,
            drugAllergyDetail = drugAllergyDetail ?: _rmeForm.value.drugAllergyDetail,
            paymentMethod = paymentMethod ?: _rmeForm.value.paymentMethod,
            cardNumber = cardNumber ?: _rmeForm.value.cardNumber,
            currentStep = currentStep ?: _rmeForm.value.currentStep,
            errorMessage = null
        )
    }

    fun toggleDisease(disease: String) {
        val current = _rmeForm.value.diseases.toMutableSet()
        if (current.contains(disease)) {
            current.remove(disease)
        } else {
            current.add(disease)
        }
        _rmeForm.value = _rmeForm.value.copy(diseases = current)
    }

    fun saveRmeProfile() {
        val form = _rmeForm.value
        if (form.nik.length != 16) {
            _rmeForm.value = form.copy(errorMessage = "Nomor Induk Kependudukan (NIK) harus 16 digit angka.")
            return
        }
        if (form.fullName.isBlank()) {
            _rmeForm.value = form.copy(errorMessage = "Nama lengkap pasien wajib diisi.")
            return
        }
        if (form.whatsapp.isBlank()) {
            _rmeForm.value = form.copy(errorMessage = "Nomor WhatsApp aktif wajib diisi untuk notifikasi antrean.")
            return
        }

        viewModelScope.launch {
            val satusehatId = "SATU-${form.nik.take(6)}-${(1000..9999).random()}"
            val profile = PatientProfile(
                nik = form.nik,
                fullName = form.fullName,
                birthPlace = form.birthPlace,
                birthDate = form.birthDate,
                gender = form.gender,
                bloodType = form.bloodType,
                whatsapp = form.whatsapp,
                address = form.address,
                chronicDiseases = form.diseases.joinToString(", "),
                hasDrugAllergy = form.hasDrugAllergy,
                drugAllergyDetail = if (form.hasDrugAllergy) form.drugAllergyDetail else "",
                paymentMethod = form.paymentMethod,
                insuranceCardNumber = form.cardNumber,
                satusehatId = satusehatId
            )
            repository.savePatientProfile(profile)
            _rmeForm.value = form.copy(isSavedSuccess = true, errorMessage = null)
            _snackbarMessage.value = "Data Pasien RME SATUSEHAT Berhasil Disimpan!"
        }
    }

    fun resetSaveSuccess() {
        _rmeForm.value = _rmeForm.value.copy(isSavedSuccess = false)
    }

    // Medication Tracker Methods
    fun addMedication(
        name: String,
        dosage: String,
        frequency: String,
        times: String,
        instruction: String,
        reminder: Boolean,
        notes: String,
        context: android.content.Context
    ) {
        if (name.isBlank()) {
            _snackbarMessage.value = "Nama obat tidak boleh kosong"
            return
        }

        viewModelScope.launch {
            val record = com.example.data.model.MedicationRecord(
                name = name.trim(),
                dosage = dosage.trim().ifEmpty { "1 Dosis" },
                frequency = frequency,
                scheduleTimes = times.trim().ifEmpty { "08:00" },
                instruction = instruction,
                isReminderActive = reminder,
                isTakenToday = false,
                notes = notes.trim()
            )
            val id = repository.addMedication(record)
            if (reminder) {
                com.example.util.MedicationNotificationHelper.scheduleMedicationAlarm(
                    context,
                    record.copy(id = id)
                )
                com.example.util.MedicationNotificationHelper.showMedicationNotification(
                    context = context,
                    notificationId = id.toInt(),
                    medName = record.name,
                    dosage = record.dosage,
                    instruction = record.instruction,
                    time = record.scheduleTimes
                )
            }
            _snackbarMessage.value = "Jadwal obat '${record.name}' berhasil disimpan!"
        }
    }

    fun toggleMedicationTaken(id: Long, taken: Boolean) {
        viewModelScope.launch {
            repository.updateMedicationTaken(id, taken)
            _snackbarMessage.value = if (taken) "Obat ditandai sudah diminum hari ini!" else "Status obat diperbarui"
        }
    }

    fun toggleMedicationReminder(id: Long, active: Boolean, context: android.content.Context) {
        viewModelScope.launch {
            repository.updateMedicationReminder(id, active)
            _snackbarMessage.value = if (active) "Pengingat obat diaktifkan" else "Pengingat dinonaktifkan"
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedication(id)
            _snackbarMessage.value = "Jadwal obat berhasil dihapus"
        }
    }

    fun triggerTestMedicationNotification(context: android.content.Context, medication: com.example.data.model.MedicationRecord) {
        com.example.util.MedicationNotificationHelper.showMedicationNotification(
            context = context,
            notificationId = medication.id.toInt(),
            medName = medication.name,
            dosage = medication.dosage,
            instruction = medication.instruction,
            time = medication.scheduleTimes
        )
        _snackbarMessage.value = "Notifikasi pengingat untuk '${medication.name}' dikirim!"
    }

    // Symptom Checker Methods (Gemini API)
    fun updateSymptomInput(input: String) {
        _symptomCheckerState.value = _symptomCheckerState.value.copy(
            symptomInput = input,
            errorMessage = null
        )
    }

    fun toggleQuickSymptom(symptom: String) {
        val current = _symptomCheckerState.value.selectedQuickSymptoms.toMutableSet()
        if (current.contains(symptom)) {
            current.remove(symptom)
        } else {
            current.add(symptom)
        }
        _symptomCheckerState.value = _symptomCheckerState.value.copy(
            selectedQuickSymptoms = current,
            errorMessage = null
        )
    }

    fun setSymptomDuration(duration: String) {
        _symptomCheckerState.value = _symptomCheckerState.value.copy(selectedDuration = duration)
    }

    fun analyzeSymptoms() {
        val state = _symptomCheckerState.value
        val allSymptoms = buildList {
            if (state.symptomInput.isNotBlank()) add(state.symptomInput.trim())
            addAll(state.selectedQuickSymptoms)
        }.joinToString(", ")

        if (allSymptoms.isBlank()) {
            _symptomCheckerState.value = state.copy(errorMessage = "Silakan masukkan atau pilih gejala yang Anda rasakan.")
            return
        }

        val profile = patientProfile.value
        val chronicConditions = profile?.chronicDiseases.orEmpty()
        val allergies = if (profile?.hasDrugAllergy == true) profile.drugAllergyDetail else ""

        _symptomCheckerState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = com.example.data.api.GeminiSymptomService.analyzeSymptoms(
                symptomsText = allSymptoms,
                duration = state.selectedDuration,
                patientConditions = chronicConditions,
                patientAllergies = allergies
            )

            result.onSuccess { analysis ->
                _symptomCheckerState.value = _symptomCheckerState.value.copy(
                    isLoading = false,
                    analysisResult = analysis,
                    errorMessage = null
                )
                _snackbarMessage.value = "Analisis gejala selesai dari Gemini AI"
            }.onFailure { err ->
                _symptomCheckerState.value = _symptomCheckerState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal menganalisis gejala: ${err.message}"
                )
            }
        }
    }

    fun clearSymptomAnalysis() {
        _symptomCheckerState.value = SymptomCheckerUiState()
    }
}
