package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.MedicationRecord
import java.util.Calendar

object MedicationNotificationHelper {
    const val CHANNEL_ID = "medication_reminders_channel"
    private const val CHANNEL_NAME = "Pengingat Minum Obat SATUSEHAT"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifikasi jadwal minum obat harian pasien"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMedicationNotification(
        context: Context,
        notificationId: Int,
        medName: String,
        dosage: String,
        instruction: String,
        time: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Waktunya Minum Obat: $medName")
            .setContentText("Dosis: $dosage • $instruction (Jadwal: $time)")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Waktunya meminum $medName ($dosage). Aturan pakai: $instruction.\nJaga kepatuhan konsumsi obat Anda untuk hasil pengobatan optimal.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleMedicationAlarm(context: Context, medication: MedicationRecord) {
        val times = medication.scheduleTimes.split(",").map { it.trim() }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        times.forEachIndexed { index, timeStr ->
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: 8
                val minute = parts[1].toIntOrNull() ?: 0

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                    putExtra("med_id", medication.id)
                    putExtra("med_name", medication.name)
                    putExtra("med_dosage", medication.dosage)
                    putExtra("med_instruction", medication.instruction)
                    putExtra("med_time", timeStr)
                }

                val requestCode = (medication.id * 100 + index).toInt()
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                } catch (e: Exception) {
                    // Safe catch if exact alarms need permission
                }
            }
        }
    }
}
