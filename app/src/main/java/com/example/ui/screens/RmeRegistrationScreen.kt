package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RmeFormState
import com.example.ui.theme.HealthGreenContainer
import com.example.ui.theme.HealthGreenOnPrimary
import com.example.ui.theme.HealthGreenPrimary
import com.example.ui.theme.HealthSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RmeRegistrationScreen(
    formState: RmeFormState,
    onUpdateForm: (
        nik: String?, fullName: String?, birthPlace: String?, birthDate: String?,
        gender: String?, bloodType: String?, whatsapp: String?, address: String?,
        hasDrugAllergy: Boolean?, drugAllergyDetail: String?, paymentMethod: String?,
        cardNumber: String?, currentStep: Int?
    ) -> Unit,
    onToggleDisease: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabTitles = listOf(
        "1. Data Pasien (RME)",
        "2. Riwayat Kesehatan",
        "3. Penjamin & Faskes"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rme_registration_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner Title
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HealthGreenContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = HealthGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pendaftaran Rekam Medis Elektronik (RME)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Standar Integrasi Satu Data Kesehatan (SATUSEHAT Kemenkes RI)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        // Module Tabs
        item {
            TabRow(
                selectedTabIndex = formState.currentStep,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HealthGreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[formState.currentStep]),
                        color = HealthGreenPrimary
                    )
                },
                modifier = Modifier.testTag("rme_tab_row")
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = formState.currentStep == index,
                        onClick = { onUpdateForm(null, null, null, null, null, null, null, null, null, null, null, null, index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (formState.currentStep == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_module_$index")
                    )
                }
            }
        }

        // Error message card
        formState.errorMessage?.let { error ->
            item {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Form Card Content by Tab Step
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    when (formState.currentStep) {
                        0 -> {
                            // MODUL 1: DATA PASIEN (RME)
                            Text(
                                text = "MODUL 1: Identitas & Data Kependudukan Pasien",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // NIK (16 digit)
                            OutlinedTextField(
                                value = formState.nik,
                                onValueChange = { if (it.length <= 16) onUpdateForm(it, null, null, null, null, null, null, null, null, null, null, null, null) },
                                label = { Text("Nomor Induk Kependudukan (NIK) *") },
                                placeholder = { Text("16 Digit NIK KTP") },
                                supportingText = { Text("${formState.nik.length}/16 Digit") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("input_rme_nik")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Nama Lengkap
                            OutlinedTextField(
                                value = formState.fullName,
                                onValueChange = { onUpdateForm(null, it, null, null, null, null, null, null, null, null, null, null, null) },
                                label = { Text("Nama Lengkap Pasien *") },
                                placeholder = { Text("Nama tanpa gelar sesuai KTP") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_rme_fullname")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tempat & Tanggal Lahir
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = formState.birthPlace,
                                    onValueChange = { onUpdateForm(null, null, it, null, null, null, null, null, null, null, null, null, null) },
                                    label = { Text("Tempat Lahir") },
                                    placeholder = { Text("Kota Lahir") },
                                    modifier = Modifier.weight(1f).testTag("input_rme_birthplace")
                                )

                                OutlinedTextField(
                                    value = formState.birthDate,
                                    onValueChange = { onUpdateForm(null, null, null, it, null, null, null, null, null, null, null, null, null) },
                                    label = { Text("Tanggal Lahir *") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    modifier = Modifier.weight(1f).testTag("input_rme_birthdate")
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Jenis Kelamin & Golongan Darah
                            Text(
                                text = "Jenis Kelamin *",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        onUpdateForm(null, null, null, null, "L", null, null, null, null, null, null, null, null)
                                    }
                                ) {
                                    RadioButton(
                                        selected = formState.gender == "L",
                                        onClick = { onUpdateForm(null, null, null, null, "L", null, null, null, null, null, null, null, null) },
                                        colors = RadioButtonDefaults.colors(selectedColor = HealthGreenPrimary)
                                    )
                                    Text("Laki-laki")
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        onUpdateForm(null, null, null, null, "P", null, null, null, null, null, null, null, null)
                                    }
                                ) {
                                    RadioButton(
                                        selected = formState.gender == "P",
                                        onClick = { onUpdateForm(null, null, null, null, "P", null, null, null, null, null, null, null, null) },
                                        colors = RadioButtonDefaults.colors(selectedColor = HealthGreenPrimary)
                                    )
                                    Text("Perempuan")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Golongan Darah
                            Text(
                                text = "Golongan Darah",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val bloodTypes = listOf("-", "A", "B", "AB", "O")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                bloodTypes.forEach { type ->
                                    val isSelected = formState.bloodType == type
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) HealthGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onUpdateForm(null, null, null, null, null, type, null, null, null, null, null, null, null)
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (type == "-") "N/A" else type,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Nomor WhatsApp
                            OutlinedTextField(
                                value = formState.whatsapp,
                                onValueChange = { onUpdateForm(null, null, null, null, null, null, it, null, null, null, null, null, null) },
                                label = { Text("Nomor WhatsApp Aktif *") },
                                placeholder = { Text("Contoh: 08123456789") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth().testTag("input_rme_whatsapp")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Alamat Domisili
                            OutlinedTextField(
                                value = formState.address,
                                onValueChange = { onUpdateForm(null, null, null, null, null, null, null, it, null, null, null, null, null) },
                                label = { Text("Alamat Lengkap Domisili") },
                                placeholder = { Text("Nama Jalan, RT/RW, Kelurahan, Kecamatan") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().testTag("input_rme_address")
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { onUpdateForm(null, null, null, null, null, null, null, null, null, null, null, null, 1) },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_next_step_1"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary)
                            ) {
                                Text("Lanjut ke Riwayat Kesehatan ->")
                            }
                        }

                        1 -> {
                            // MODUL 2: RIWAYAT KESEHATAN
                            Text(
                                text = "MODUL 2: Rekam Riwayat Kesehatan Mandiri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Apakah Anda memiliki riwayat penyakit berikut? (Bisa pilih lebih dari satu)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val diseasesList = listOf(
                                "diabetes" to "Diabetes Mellitus",
                                "hipertensi" to "Hipertensi (Tekanan Darah Tinggi)",
                                "asma" to "Asma / Saluran Pernafasan",
                                "jantung" to "Penyakit Jantung"
                            )

                            diseasesList.forEach { (key, label) ->
                                val isChecked = formState.diseases.contains(key)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleDisease(key) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleDisease(key) },
                                        colors = CheckboxDefaults.colors(checkedColor = HealthGreenPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Alergi Obat conditional section
                            Text(
                                text = "Apakah Anda memiliki Alergi Obat? *",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        onUpdateForm(null, null, null, null, null, null, null, null, false, null, null, null, null)
                                    }
                                ) {
                                    RadioButton(
                                        selected = !formState.hasDrugAllergy,
                                        onClick = { onUpdateForm(null, null, null, null, null, null, null, null, false, null, null, null, null) },
                                        colors = RadioButtonDefaults.colors(selectedColor = HealthGreenPrimary)
                                    )
                                    Text("Tidak")
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        onUpdateForm(null, null, null, null, null, null, null, null, true, null, null, null, null)
                                    }
                                ) {
                                    RadioButton(
                                        selected = formState.hasDrugAllergy,
                                        onClick = { onUpdateForm(null, null, null, null, null, null, null, null, true, null, null, null, null) },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFC62828))
                                    )
                                    Text("Ya, Memiliki Alergi")
                                }
                            }

                            // Conditional input for drug allergy details
                            if (formState.hasDrugAllergy) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFC62828),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Sebutkan nama/jenis obat yang memicu alergi Anda:",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC62828)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = formState.drugAllergyDetail,
                                            onValueChange = { onUpdateForm(null, null, null, null, null, null, null, null, null, it, null, null, null) },
                                            placeholder = { Text("Misal: Paracetamol, Amoxicillin, Aspirin") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("input_drug_allergy_detail")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onUpdateForm(null, null, null, null, null, null, null, null, null, null, null, null, 0) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text("<- Kembali", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Button(
                                    onClick = { onUpdateForm(null, null, null, null, null, null, null, null, null, null, null, null, 2) },
                                    modifier = Modifier.weight(1f).height(48.dp).testTag("btn_next_step_2"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary)
                                ) {
                                    Text("Lanjut ke Penjamin ->")
                                }
                            }
                        }

                        2 -> {
                            // MODUL 3: PENJAMINAN & FASKES TUJUAN
                            Text(
                                text = "MODUL 3: Penjaminan Biaya & Faskes Favorit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HealthGreenPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Metode Penjaminan Biaya *",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val paymentMethods = listOf("BPJS", "Umum", "Asuransi")
                            paymentMethods.forEach { method ->
                                val label = when (method) {
                                    "BPJS" -> "BPJS Kesehatan"
                                    "Umum" -> "Umum / Mandiri"
                                    else -> "Asuransi Swasta"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onUpdateForm(null, null, null, null, null, null, null, null, null, null, method, null, null)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = formState.paymentMethod == method,
                                        onClick = {
                                            onUpdateForm(null, null, null, null, null, null, null, null, null, null, method, null, null)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = HealthGreenPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Conditional Card Number Input if BPJS or Asuransi
                            if (formState.paymentMethod != "Umum") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = formState.cardNumber,
                                    onValueChange = { onUpdateForm(null, null, null, null, null, null, null, null, null, null, null, it, null) },
                                    label = { Text("Nomor Kartu ${if (formState.paymentMethod == "BPJS") "BPJS Kesehatan" else "Asuransi"} *") },
                                    placeholder = { Text("Masukkan nomor kartu aktif (13 digit)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("input_rme_card_number")
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(20.dp))

                            // Submit & Save Button
                            Button(
                                onClick = onSaveProfile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_save_rme_profile"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HealthGreenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simpan Data RME SATUSEHAT",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (formState.isSavedSuccess) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onNavigateToCalendar,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_go_to_calendar_after_save"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HealthSecondary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buka Kalender & Pilih Jadwal Dokter",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom space
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
