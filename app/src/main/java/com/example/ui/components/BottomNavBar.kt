package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.EHealthScreen
import com.example.ui.theme.HealthGreenContainer
import com.example.ui.theme.HealthGreenOnContainer
import com.example.ui.theme.HealthGreenPrimary

@Composable
fun BottomNavBar(
    currentScreen: EHealthScreen,
    onNavigate: (EHealthScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("main_bottom_nav_bar"),
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == EHealthScreen.DASHBOARD,
            onClick = { onNavigate(EHealthScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == EHealthScreen.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Beranda"
                )
            },
            label = { Text("Beranda") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HealthGreenOnContainer,
                selectedTextColor = HealthGreenPrimary,
                indicatorColor = HealthGreenContainer
            ),
            modifier = Modifier.testTag("nav_item_dashboard")
        )

        NavigationBarItem(
            selected = currentScreen == EHealthScreen.CALENDAR,
            onClick = { onNavigate(EHealthScreen.CALENDAR) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == EHealthScreen.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Kalender Janji"
                )
            },
            label = { Text("Kalender") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HealthGreenOnContainer,
                selectedTextColor = HealthGreenPrimary,
                indicatorColor = HealthGreenContainer
            ),
            modifier = Modifier.testTag("nav_item_calendar")
        )

        NavigationBarItem(
            selected = currentScreen == EHealthScreen.SYMPTOM_CHECKER,
            onClick = { onNavigate(EHealthScreen.SYMPTOM_CHECKER) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == EHealthScreen.SYMPTOM_CHECKER) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                    contentDescription = "Cek Gejala AI"
                )
            },
            label = { Text("Cek Gejala") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HealthGreenOnContainer,
                selectedTextColor = HealthGreenPrimary,
                indicatorColor = HealthGreenContainer
            ),
            modifier = Modifier.testTag("nav_item_symptom_checker")
        )

        NavigationBarItem(
            selected = currentScreen == EHealthScreen.MEDICATION_TRACKER,
            onClick = { onNavigate(EHealthScreen.MEDICATION_TRACKER) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == EHealthScreen.MEDICATION_TRACKER) Icons.Filled.Medication else Icons.Outlined.Medication,
                    contentDescription = "Pengingat Obat"
                )
            },
            label = { Text("Obat") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HealthGreenOnContainer,
                selectedTextColor = HealthGreenPrimary,
                indicatorColor = HealthGreenContainer
            ),
            modifier = Modifier.testTag("nav_item_medication")
        )

        NavigationBarItem(
            selected = currentScreen == EHealthScreen.RME_REGISTRATION,
            onClick = { onNavigate(EHealthScreen.RME_REGISTRATION) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == EHealthScreen.RME_REGISTRATION) Icons.Filled.PersonOutline else Icons.Outlined.PersonOutline,
                    contentDescription = "Rekam Medis Pasien"
                )
            },
            label = { Text("RME") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = HealthGreenOnContainer,
                selectedTextColor = HealthGreenPrimary,
                indicatorColor = HealthGreenContainer
            ),
            modifier = Modifier.testTag("nav_item_rme")
        )
    }
}

private val Int.dp get() = androidx.compose.ui.unit.Dp(this.toFloat())
