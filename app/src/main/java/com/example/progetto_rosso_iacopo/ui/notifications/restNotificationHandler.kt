package com.example.progetto_rosso_iacopo.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import kotlin.jvm.java

class RestNotificationHandler (private val context: Context) {

    val alarmManager: AlarmManager? by lazy{context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager}
    private fun pendingIntent(exerciseName: String): PendingIntent {
        val intent = Intent(context, RestAlarmReceiver::class.java).apply {
            putExtra("exercise_name", exerciseName)
        }
        return PendingIntent.getBroadcast(
            context, exerciseName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleNotification(exerciseName: String?, end: Long?){
        if((end == null) || (exerciseName.isNullOrEmpty())) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager?.canScheduleExactAlarms() == true) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                end,
                pendingIntent(exerciseName)
            )
        }
    }

    fun getPermissionScheduleExactAlarms() {
    }

    fun deleteNotification(exerciseName: String?){
        if(exerciseName.isNullOrEmpty()){return}
        alarmManager?.cancel(pendingIntent(exerciseName))
    }
}