package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getLongExtra("med_id", 1L).toInt()
        val medName = intent.getStringExtra("med_name") ?: "Obat Anda"
        val medDosage = intent.getStringExtra("med_dosage") ?: "1 Dosis"
        val medInstruction = intent.getStringExtra("med_instruction") ?: "Sesuai resep"
        val medTime = intent.getStringExtra("med_time") ?: "Sekarang"

        MedicationNotificationHelper.showMedicationNotification(
            context = context,
            notificationId = medId,
            medName = medName,
            dosage = medDosage,
            instruction = medInstruction,
            time = medTime
        )
    }
}
