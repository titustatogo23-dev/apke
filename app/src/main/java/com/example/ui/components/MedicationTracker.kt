package com.example.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MedicationRecord
import com.example.ui.theme.HealthGreenContainer
import com.example.ui.theme.HealthGreenPrimary
import com.example.ui.theme.HealthSecondary

@Composable
fun MedicationTracker(
    medications: List<MedicationRecord>,
    onAddMedication: (
        name: String,
        dosage: String,
        frequency: String,
        times: String,
        instruction: String,
        reminder: Boolean,
        notes: String,
        context: Context
    ) -> Unit,
    onToggleTaken: (id: Long, taken: Boolean) -> Unit,
    onToggleReminder: (id: Long, active: Boolean, context: Context) -> Unit,
    onDeleteMedication: (id: Long) -> Unit,
    onTestNotification: (context: Context, medication: MedicationRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    // Android 13+ Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    val takenCount = medications.count { it.isTakenToday }
    val totalCount = medications.size
    val progress = if (totalCount > 0) takenCount.toFloat() / totalCount.toFloat() else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medication_tracker_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Daily Adherence Progress Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("card_medication_progress")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HealthGreenContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = HealthGreenPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pengingat Minum Obat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Jadwal Harian & Notifikasi Otomatis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            showAddDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary),
                        modifier = Modifier.testTag("btn_open_add_medication")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Adherence Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kepatuhan Hari Ini",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$takenCount dari $totalCount Obat Selesai (${(progress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = HealthGreenPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 2. Medication List Items
        if (medications.isEmpty()) {
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
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum Ada Jadwal Obat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tambahkan obat harian Anda untuk menerima notifikasi pengingat otomatis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tambah Jadwal Obat Baru")
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                medications.forEach { med ->
                    MedicationItemCard(
                        medication = med,
                        onToggleTaken = { onToggleTaken(med.id, !med.isTakenToday) },
                        onToggleReminder = { onToggleReminder(med.id, !med.isReminderActive, context) },
                        onDelete = { onDeleteMedication(med.id) },
                        onTestNotification = { onTestNotification(context, med) }
                    )
                }
            }
        }
    }

    // Modal Add Medication Dialog
    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, dosage, freq, times, instr, reminder, notes ->
                onAddMedication(name, dosage, freq, times, instr, reminder, notes, context)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MedicationItemCard(
    medication: MedicationRecord,
    onToggleTaken: () -> Unit,
    onToggleReminder: () -> Unit,
    onDelete: () -> Unit,
    onTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTaken = medication.isTakenToday

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("medication_card_${medication.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) Color(0xFFF1F8F4) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 1.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isTaken,
                        onCheckedChange = { onToggleTaken() },
                        colors = CheckboxDefaults.colors(checkedColor = HealthSecondary),
                        modifier = Modifier.testTag("checkbox_med_taken_${medication.id}")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = medication.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTaken) HealthSecondary else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HealthGreenContainer
                            ) {
                                Text(
                                    text = medication.dosage,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthGreenPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "${medication.frequency} • ${medication.instruction}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_med_${medication.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus Jadwal",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Schedule times and notification controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = HealthGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Jam: ${medication.scheduleTimes}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Test reminder trigger button
                    IconButton(
                        onClick = onTestNotification,
                        modifier = Modifier.size(34.dp).testTag("btn_test_notification_${medication.id}")
                    ) {
                        Icon(
                            imageVector = if (medication.isReminderActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Uji Notifikasi",
                            tint = if (medication.isReminderActive) HealthGreenPrimary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = medication.isReminderActive,
                        onCheckedChange = { onToggleReminder() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HealthGreenPrimary
                        ),
                        modifier = Modifier.testTag("switch_reminder_${medication.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        dosage: String,
        frequency: String,
        times: String,
        instruction: String,
        reminder: Boolean,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 Tablet") }
    var selectedFrequency by remember { mutableStateOf("1x Sehari") }
    var times by remember { mutableStateOf("08:00") }
    var instruction by remember { mutableStateOf("Sesudah Makan") }
    var reminder by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    val frequencies = listOf("1x Sehari", "2x Sehari", "3x Sehari", "Bila Perlu")
    val instructions = listOf("Sesudah Makan", "Sebelum Makan", "Bersama Makanan")
    val presetTimes = listOf("08:00", "12:00", "18:00", "20:00")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().testTag("dialog_add_medication")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tambah Jadwal Obat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HealthGreenPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("btn_close_add_med_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Obat *") },
                    placeholder = { Text("Misal: Amlodipine, Metformin, Paracetamol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_med_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dosage Field
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosis / Satuan *") },
                    placeholder = { Text("Misal: 5 mg, 500 mg, 1 Kapsul") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_med_dosage")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Frequency Chips
                Text(
                    text = "Frekuensi Konsumsi:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    frequencies.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedFrequency = freq
                                times = when (freq) {
                                    "1x Sehari" -> "08:00"
                                    "2x Sehari" -> "08:00, 20:00"
                                    "3x Sehari" -> "08:00, 14:00, 20:00"
                                    else -> "Bila Perlu"
                                }
                            },
                            label = { Text(freq) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HealthGreenContainer,
                                selectedLabelColor = HealthGreenPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Schedule Time Field
                OutlinedTextField(
                    value = times,
                    onValueChange = { times = it },
                    label = { Text("Jadwal Jam Minum (Format: JJ:MM)") },
                    placeholder = { Text("Misal: 08:00 atau 08:00, 20:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_med_times")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Instruction Radio Chips
                Text(
                    text = "Aturan Pakai:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    instructions.forEach { instr ->
                        val isSelected = instruction == instr
                        FilterChip(
                            selected = isSelected,
                            onClick = { instruction = instr },
                            label = { Text(instr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HealthGreenContainer,
                                selectedLabelColor = HealthGreenPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = HealthGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aktifkan Notifikasi Pengingat",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Switch(
                        checked = reminder,
                        onCheckedChange = { reminder = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = HealthGreenPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirm(name, dosage, selectedFrequency, times, instruction, reminder, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_medication"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary)
                ) {
                    Text("Simpan & Pasang Pengingat")
                }
            }
        }
    }
}
