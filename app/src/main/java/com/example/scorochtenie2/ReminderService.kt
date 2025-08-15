package com.example.scorochtenie2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class ReminderService : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reading_reminder_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_SHOW_REMINDER = "show_reminder"
        private const val TAG = "ReminderService"

        fun initialize(context: Context) {
            createNotificationChannel(context)
        }

        fun scheduleReminder(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderService::class.java).apply {
                action = ACTION_SHOW_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Scheduling reminder for $hour:$minute")

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Permission denied.")
                return
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Reminder scheduled at ${calendar.time}")
        }

        fun cancelReminder(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderService::class.java).apply {
                action = ACTION_SHOW_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Reminder cancelled")
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    val name = "Напоминания о чтении"
                    val descriptionText = "Канал для напоминаний о ежедневных занятиях чтением"
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                        description = descriptionText
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 1000)
                    }

                    notificationManager.createNotificationChannel(channel)
                    Log.d(TAG, "Notification channel created with vibration enabled, shouldVibrate=${notificationManager.getNotificationChannel(CHANNEL_ID)?.shouldVibrate()}")
                } else {
                    Log.d(TAG, "Notification channel already exists")
                }
            }
        }

        fun sendTestNotification(context: Context) {
            Log.d(TAG, "sendTestNotification called")
            val service = ReminderService()
            service.showReminderNotification(context.applicationContext)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SHOW_REMINDER -> {
                showReminderNotification(context)
                val hour = ReminderManager.getReminderHour(context)
                val minute = ReminderManager.getReminderMinute(context)
                scheduleReminder(context, hour, minute)
            }
        }
    }

    fun showReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val isVibrationEnabled = ReminderManager.isVibrationEnabled(context)
        Log.d(TAG, "showReminderNotification: vibration enabled = $isVibrationEnabled")

        val canVibrate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.hasVibrator() && notificationManager.getNotificationChannel(CHANNEL_ID)?.shouldVibrate() == true
        } else {
            vibrator.hasVibrator()
        }
        Log.d(TAG, "showReminderNotification: device can vibrate = $canVibrate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "showReminderNotification: channel vibration enabled = ${notificationManager.getNotificationChannel(CHANNEL_ID)?.shouldVibrate()}")
            Log.d(TAG, "showReminderNotification: has vibrator = ${vibrator.hasVibrator()}")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Время для чтения! 📚")
            .setContentText("Не забудьте потренироваться в скорочтении")
            .setSmallIcon(R.drawable.ic_practice)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (isVibrationEnabled && canVibrate) {
            notificationBuilder.setVibrate(longArrayOf(0, 1000))
            Log.d(TAG, "Notification with vibration triggered")
        } else {
            notificationBuilder.setVibrate(null)
            Log.d(TAG, "Notification without vibration triggered")
            if (!canVibrate) {
                notificationBuilder.setContentText("Не забудьте потренироваться в скорочтении (вибрация отключена в настройках устройства)")
            } else {
                notificationBuilder.setContentText("Не забудьте потренироваться в скорочтении (вибрация отключена)")
            }
        }

        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}