package com.example.progetto_rosso_iacopo.ui.notifications

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.jvm.java

class RestNotificationHandler (private val context: Context) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private fun pendingIntent(exerciseName: String): PendingIntent {
        val intent = Intent(context, RestAlarmReceiver::class.java).apply {
            putExtra("exercise-name", exerciseName)
        }
        return PendingIntent.getBroadcast(
            context, exerciseName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleNotification(exerciseName: String, end: Long){
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            fine,
            pendingIntent(nomeEsercizio)
    }
}