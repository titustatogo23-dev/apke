package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Appointment
import com.example.data.model.HealthMetricRecord
import com.example.data.model.MedicationRecord
import com.example.data.model.PatientProfile
import com.example.ui.EHealthScreen
import com.example.ui.components.PatientDashboardComponent
import com.example.ui.components.QueuePassDialog
import com.example.ui.theme.HealthGreenContainer
import com.example.ui.theme.HealthGreenPrimary
import com.example.ui.theme.HealthSecondary

@Composable
fun DashboardScreen(
    patientProfile: PatientProfile?,
    healthMetric: HealthMetricRecord?,
    medications: List<MedicationRecord>,
    upcomingAppointments: List<Appointment>,
    onNavigate: (EHealthScreen) -> Unit,
    onShowQueuePass: (Appointment) -> Unit,
    onCancelAppointment: (Long) -> Unit,
    activePassAppointment: Appointment?,
    onDismissQueuePass: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Modular Patient Dashboard Component (incorporating HealthSummaryCard)
        item {
            PatientDashboardComponent(
                patientProfile = patientProfile,
                healthMetric = healthMetric,
                upcomingAppointments = upcomingAppointments,
                onShowTicketPass = onShowQueuePass,
                onCancelAppointment = onCancelAppointment,
                onScheduleNewAppointment = { onNavigate(EHealthScreen.CALENDAR) },
                onEditRmeProfile = { onNavigate(EHealthScreen.RME_REGISTRATION) }
            )
        }

        // Section: AI Symptom Checker Fast Access Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(EHealthScreen.SYMPTOM_CHECKER) }
                    .testTag("dashboard_card_symptom_checker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEDE7F6),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF673AB7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Pemeriksa Gejala Mandiri",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEDE7F6)
                                ) {
                                    Text(
                                        text = "Gemini AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF673AB7),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Analisis keluhan & saran rujukan poliklinik",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Buka Cek Gejala",
                        tint = Color(0xFF673AB7),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Section: Medication Tracker Fast Access Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(EHealthScreen.MEDICATION_TRACKER) }
                    .testTag("dashboard_card_medication_tracker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HealthGreenContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = HealthGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Pengingat Minum Obat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val takenCount = medications.count { it.isTakenToday }
                            Text(
                                text = if (medications.isNotEmpty()) "$takenCount dari ${medications.size} obat diminum hari ini" else "Atur jadwal minum obat Anda",
                                style = MaterialTheme.typography.bodySmall,
                                color = HealthGreenPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Buka Jadwal Obat",
                        tint = HealthGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Section: Polyclinic Fast Availability Info
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("card_polyclinic_status")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Antrean Poliklinik Puskesmas & RSUD Hari Ini",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val clinicsInfo = listOf(
                        "Poli Umum (dr. Farhan Nurcahyo)" to "Sisa 12 Kuota",
                        "Poli Gigi & Mulut (drg. Anindya)" to "Sisa 5 Kuota",
                        "Poli KIA / Ibu & Anak (Bidan Rahma)" to "Sisa 9 Kuota",
                        "Poli Penyakit Dalam (dr. Budi, Sp.PD)" to "Sisa 7 Kuota",
                        "Poli Anak (dr. Hendra, Sp.A)" to "Sisa 11 Kuota"
                    )

                    clinicsInfo.forEach { (clinic, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(HealthSecondary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = clinic,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = status,
                                style = MaterialTheme.typography.labelSmall,
                                color = HealthGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom breathing space
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Modal Queue Pass dialog if opened
    activePassAppointment?.let { appointment ->
        QueuePassDialog(
            appointment = appointment,
            onDismiss = onDismissQueuePass
        )
    }
}
