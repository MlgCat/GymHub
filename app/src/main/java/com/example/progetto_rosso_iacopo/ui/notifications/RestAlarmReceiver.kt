package com.example.progetto_rosso_iacopo.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.progetto_rosso_iacopo.R
import android.Manifest
import android.util.Log
import com.example.progetto_rosso_iacopo.ui.notifications.RestNotificationHandler
import kotlin.jvm.java

class RestAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val exerciseName = intent.getStringExtra("exercise_name") ?: "Esercizio"

        // Pulisci lo stato salvato: il recupero è concluso
        val prefs = context.getSharedPreferences("active-workout-state", Context.MODE_PRIVATE)
        prefs.edit().remove("timerEnd").apply()

        // Assicurati che il Notification Channel sia stato creato nel sistema
        NotificationHelper.createNotificationChannel(context)

        // Costruzione della notifica (richiede il CHANNEL_ID nel costruttore per API 26+)
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle("Tempo di recupero terminato!")
            .setContentText("Pronto per: $exerciseName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Attiva vibrazione e suono predefiniti
            .setAutoCancel(true)

        // Verifica permesso runtime (Android 13 / API 33+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context)
                .notify(NotificationHelper.NOTIFICATION_ID, builder.build())
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val handler : RestNotificationHandler = RestNotificationHandler(context)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "intent ricevuto")
            val prefs = context.getSharedPreferences("active-workout-state", Context.MODE_PRIVATE)
            val timerEnd = prefs.getLong("timerEnd", 0)
            if (timerEnd > System.currentTimeMillis()){
                val exerciseName = prefs.getString("exerciseName", "esercizo")
                handler.scheduleNotification(exerciseName, timerEnd)
                Log.d("BootReceiver", "notifica schedulata")
            }
        }
    }
}

object NotificationHelper {
    const val CHANNEL_ID = "rest_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Timer di recupero",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifiche di fine tempo di recupero tra gli esercizi"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}



