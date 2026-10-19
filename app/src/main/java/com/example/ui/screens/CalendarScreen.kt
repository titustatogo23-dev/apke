package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.ClinicSlot
import com.example.data.model.PatientProfile
import com.example.ui.CalendarUiState
import com.example.ui.components.INDONESIAN_MONTHS
import com.example.ui.components.InteractiveCalendar
import com.example.ui.components.QueuePassDialog
import com.example.ui.components.QueueTicketCard
import com.example.ui.theme.HealthGreenContainer
import com.example.ui.theme.HealthGreenOnPrimary
import com.example.ui.theme.HealthGreenPrimary
import com.example.ui.theme.HealthSecondary

val CLINIC_POLIS = listOf(
    "Semua Poli",
    "Poli Umum",
    "Poli Gigi & Mulut",
    "Poli KIA (Ibu & Anak)",
    "Poli Penyakit Dalam",
    "Poli Anak",
    "Poli Mata"
)

val HEALTH_FACILITIES = listOf(
    "Puskesmas Kecamatan Sehat",
    "RSUD Kota Madani"
)

@Composable
fun CalendarScreen(
    calendarState: CalendarUiState,
    allAppointments: List<Appointment>,
    patientProfile: PatientProfile?,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onMonthChange: (delta: Int) -> Unit,
    onPoliSelected: (String) -> Unit,
    onFaskesSelected: (String) -> Unit,
    onSelectSlot: (ClinicSlot) -> Unit,
    onConfirmBooking: (paymentMethod: String, cardNumber: String) -> Unit,
    onDismissBookingSlot: () -> Unit,
    onShowQueuePass: (Appointment) -> Unit,
    onDismissQueuePass: () -> Unit,
    onCancelAppointment: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Confirmed appointments for the chosen calendar day
    val dayAppointments = allAppointments.filter {
        it.year == calendarState.year &&
                it.month == calendarState.month &&
                it.day == calendarState.selectedDay &&
                it.status != "DIBATALKAN"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Interactive Calendar Header & Grid
        item {
            InteractiveCalendar(
                year = calendarState.year,
                month = calendarState.month,
                selectedDay = calendarState.selectedDay,
                appointments = allAppointments,
                onDateSelected = onDateSelected,
                onMonthChange = onMonthChange
            )
        }

        // Section 2: Selected Date Banner & Faskes Switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Tanggal Terpilih",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "${calendarState.selectedDay} ${INDONESIAN_MONTHS.getOrElse(calendarState.month - 1) { "" }} ${calendarState.year}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreenPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = HealthGreenContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = HealthGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${calendarState.availableSlots.size} Slot Terbuka",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthGreenPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Faskes Switcher Tabs
                    Text(
                        text = "Fasilitas Kesehatan Tujuan:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HEALTH_FACILITIES.forEach { faskes ->
                            val isSelected = calendarState.selectedFaskes == faskes
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) HealthGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onFaskesSelected(faskes) }
                                    .testTag("faskes_chip_${faskes.take(8)}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (faskes.contains("Puskesmas")) "Puskesmas" else "RSUD",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Confirmed Appointments for the chosen date (if any)
        if (dayAppointments.isNotEmpty()) {
            item {
                Text(
                    text = "Janji Temu Terkonfirmasi Anda (${dayAppointments.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HealthGreenPrimary
                )
            }

            items(dayAppointments, key = { it.id }) { appointment ->
                QueueTicketCard(
                    appointment = appointment,
                    onShowDetail = { onShowQueuePass(appointment) },
                    onCancel = { onCancelAppointment(appointment.id) }
                )
            }
        }

        // Section 4: Polyclinic Category Filter Chips
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = HealthGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pilih Poliklinik & Spesialisasi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CLINIC_POLIS.forEach { poli ->
                        val isSelected = calendarState.selectedPoli == poli
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPoliSelected(poli) },
                            label = { Text(poli) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HealthGreenContainer,
                                selectedLabelColor = HealthGreenPrimary
                            ),
                            modifier = Modifier.testTag("filter_poli_${poli.replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // Section 5: Available Clinic Slots for Date
        item {
            Text(
                text = "Pilih Slot Waktu Kunjungan Tersedia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (calendarState.availableSlots.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada jadwal dokter untuk poli ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(calendarState.availableSlots, key = { it.id + it.poliName }) { slot ->
                ClinicSlotCard(
                    slot = slot,
                    onSelect = { onSelectSlot(slot) }
                )
            }
        }

        // Bottom breathing space
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Modal Confirmation Dialog when a slot is tapped
    calendarState.selectedSlot?.let { slot ->
        BookingConfirmationDialog(
            slot = slot,
            year = calendarState.year,
            month = calendarState.month,
            day = calendarState.selectedDay,
            patientProfile = patientProfile,
            initialPaymentMethod = calendarState.paymentMethod,
            initialCardNumber = calendarState.cardNumber,
            onConfirm = onConfirmBooking,
            onDismiss = onDismissBookingSlot
        )
    }

    // Modal Queue Pass Dialog after booking or when user taps "Kartu QR"
    val activePass = calendarState.bookingSuccessAppointment ?: calendarState.activePassAppointment
    activePass?.let { appointment ->
        QueuePassDialog(
            appointment = appointment,
            onDismiss = {
                if (calendarState.bookingSuccessAppointment != null) {
                    onDismissBookingSlot()
                } else {
                    onDismissQueuePass()
                }
            }
        )
    }
}

@Composable
fun ClinicSlotCard(
    slot: ClinicSlot,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotaFraction = slot.bookedQuota.toFloat() / slot.totalQuota.toFloat()
    val isAlmostFull = slot.remainingQuota <= 3

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clinic_slot_card_${slot.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = slot.poliName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = HealthGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = slot.doctorName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HealthGreenContainer
                ) {
                    Text(
                        text = slot.roomNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreenPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time & Session Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${slot.timeSlot} • Sesi ${slot.sessionName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthGreenPrimary
                    )
                }

                Text(
                    text = "Sisa Kuota: ${slot.remainingQuota}/${slot.totalQuota}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlmostFull) Color(0xFFE65100) else HealthSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { quotaFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (isAlmostFull) Color(0xFFE65100) else HealthGreenPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSelect,
                enabled = slot.isAvailable,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_select_slot_${slot.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary)
            ) {
                Text(
                    text = if (slot.isAvailable) "Pilih Slot Ini" else "Kuota Penuh",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
