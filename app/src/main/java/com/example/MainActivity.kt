package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.EHealthScreen
import com.example.ui.EHealthViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.components.HeaderBanner
import com.example.ui.components.MedicationTracker
import com.example.ui.components.SymptomChecker
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RmeRegistrationScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: EHealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EHealthApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun EHealthApp(viewModel: EHealthViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val calendarState by viewModel.calendarState.collectAsStateWithLifecycle()
    val rmeForm by viewModel.rmeForm.collectAsStateWithLifecycle()
    val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val upcomingAppointments by viewModel.upcomingAppointments.collectAsStateWithLifecycle()
    val latestHealthMetric by viewModel.latestHealthMetric.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()
    val symptomCheckerState by viewModel.symptomCheckerState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display reactive snackbars
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Handle system back navigation to Dashboard
    BackHandler(enabled = currentScreen != EHealthScreen.DASHBOARD) {
        viewModel.navigateTo(EHealthScreen.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HeaderBanner()
        },
        bottomBar = {
            BottomNavBar(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                EHealthScreen.DASHBOARD -> {
                    DashboardScreen(
                        patientProfile = patientProfile,
                        healthMetric = latestHealthMetric,
                        medications = allMedications,
                        upcomingAppointments = upcomingAppointments,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onShowQueuePass = { apt -> viewModel.showQueuePass(apt) },
                        onCancelAppointment = { id -> viewModel.cancelAppointment(id) },
                        activePassAppointment = calendarState.activePassAppointment,
                        onDismissQueuePass = { viewModel.dismissQueuePass() }
                    )
                }

                EHealthScreen.CALENDAR, EHealthScreen.BOOKING -> {
                    CalendarScreen(
                        calendarState = calendarState,
                        allAppointments = allAppointments,
                        patientProfile = patientProfile,
                        onDateSelected = { y, m, d -> viewModel.selectDate(y, m, d) },
                        onMonthChange = { delta -> viewModel.changeMonth(delta) },
                        onPoliSelected = { poli -> viewModel.filterPoli(poli) },
                        onFaskesSelected = { faskes -> viewModel.selectFaskes(faskes) },
                        onSelectSlot = { slot -> viewModel.selectSlot(slot) },
                        onConfirmBooking = { method, cardNo ->
                            viewModel.setCalendarPayment(method, cardNo)
                            viewModel.bookSelectedSlot()
                        },
                        onDismissBookingSlot = {
                            viewModel.dismissBookingSuccess()
                            viewModel.calendarState.value.selectedSlot?.let {
                                viewModel.selectDate(
                                    calendarState.year,
                                    calendarState.month,
                                    calendarState.selectedDay
                                )
                            }
                        },
                        onShowQueuePass = { apt -> viewModel.showQueuePass(apt) },
                        onDismissQueuePass = { viewModel.dismissQueuePass() },
                        onCancelAppointment = { id -> viewModel.cancelAppointment(id) }
                    )
                }

                EHealthScreen.SYMPTOM_CHECKER -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            SymptomChecker(
                                state = symptomCheckerState,
                                patientProfile = patientProfile,
                                onUpdateInput = { input -> viewModel.updateSymptomInput(input) },
                                onToggleQuickSymptom = { sym -> viewModel.toggleQuickSymptom(sym) },
                                onSetDuration = { dur -> viewModel.setSymptomDuration(dur) },
                                onAnalyze = { viewModel.analyzeSymptoms() },
                                onReset = { viewModel.clearSymptomAnalysis() },
                                onBookSuggestedPoli = { suggestedPoli ->
                                    val matchedPoli = when {
                                        suggestedPoli.contains("Dalam", ignoreCase = true) -> "Poli Penyakit Dalam"
                                        suggestedPoli.contains("Gigi", ignoreCase = true) -> "Poli Gigi & Mulut"
                                        suggestedPoli.contains("KIA", ignoreCase = true) || suggestedPoli.contains("Kandungan", ignoreCase = true) -> "Poli KIA (Ibu & Anak)"
                                        suggestedPoli.contains("Anak", ignoreCase = true) -> "Poli Anak"
                                        suggestedPoli.contains("Mata", ignoreCase = true) -> "Poli Mata"
                                        else -> "Poli Umum"
                                    }
                                    viewModel.filterPoli(matchedPoli)
                                    viewModel.navigateTo(EHealthScreen.CALENDAR)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }

                EHealthScreen.MEDICATION_TRACKER -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            MedicationTracker(
                                medications = allMedications,
                                onAddMedication = { name, dosage, freq, times, instr, reminder, notes, ctx ->
                                    viewModel.addMedication(name, dosage, freq, times, instr, reminder, notes, ctx)
                                },
                                onToggleTaken = { id, taken ->
                                    viewModel.toggleMedicationTaken(id, taken)
                                },
                                onToggleReminder = { id, active, ctx ->
                                    viewModel.toggleMedicationReminder(id, active, ctx)
                                },
                                onDeleteMedication = { id ->
                                    viewModel.deleteMedication(id)
                                },
                                onTestNotification = { ctx, med ->
                                    viewModel.triggerTestMedicationNotification(ctx, med)
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }

                EHealthScreen.RME_REGISTRATION -> {
                    RmeRegistrationScreen(
                        formState = rmeForm,
                        onUpdateForm = { nik, name, birthPlace, birthDate, gender, bloodType, wa, addr, allergy, allergyDetail, pay, cardNo, step ->
                            viewModel.updateRmeForm(
                                nik = nik,
                                fullName = name,
                                birthPlace = birthPlace,
                                birthDate = birthDate,
                                gender = gender,
                                bloodType = bloodType,
                                whatsapp = wa,
                                address = addr,
                                hasDrugAllergy = allergy,
                                drugAllergyDetail = allergyDetail,
                                paymentMethod = pay,
                                cardNumber = cardNo,
                                currentStep = step
                            )
                        },
                        onToggleDisease = { disease -> viewModel.toggleDisease(disease) },
                        onSaveProfile = { viewModel.saveRmeProfile() },
                        onNavigateToCalendar = {
                            viewModel.resetSaveSuccess()
                            viewModel.navigateTo(EHealthScreen.CALENDAR)
                        }
                    )
                }
            }
        }
    }
}
