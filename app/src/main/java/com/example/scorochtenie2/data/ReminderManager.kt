package com.example.scorochtenie2

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

object ReminderManager {
    private const val PREF_NAME = "ReminderSettings"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val TAG = "ReminderManager"

    private var isInitialized = false

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun initialize(context: Context) {
        if (!isInitialized) {
            ReminderService.initialize(context)
            isInitialized = true
            Log.d(TAG, "ReminderManager initialized")
        }
    }

    fun isReminderEnabled(context: Context): Boolean {
        initialize(context)
        val enabled = getSharedPreferences(context).getBoolean(KEY_REMINDER_ENABLED, false)
        Log.d(TAG, "isReminderEnabled: $enabled")
        return enabled
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        initialize(context)
        val editor = getSharedPreferences(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled)
        val success = editor.commit()
        val savedValue = getSharedPreferences(context).getBoolean(KEY_REMINDER_ENABLED, false)
        Log.d(TAG, "setReminderEnabled: set=$enabled, saved=$savedValue, success=$success")
        if (!success) {
            Toast.makeText(context, "Ошибка сохранения настройки напоминаний", Toast.LENGTH_SHORT).show()
        }

        if (enabled) {
            val hour = getReminderHour(context)
            val minute = getReminderMinute(context)
            ReminderService.scheduleReminder(context, hour, minute)
        } else {
            ReminderService.cancelReminder(context)
        }
    }

    fun getReminderHour(context: Context): Int {
        initialize(context)
        val hour = getSharedPreferences(context).getInt(KEY_REMINDER_HOUR, 9)
        Log.d(TAG, "getReminderHour: $hour")
        return hour
    }

    fun getReminderMinute(context: Context): Int {
        initialize(context)
        val minute = getSharedPreferences(context).getInt(KEY_REMINDER_MINUTE, 0)
        Log.d(TAG, "getReminderMinute: $minute")
        return minute
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        initialize(context)
        val editor = getSharedPreferences(context).edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
        val success = editor.commit()
        val savedHour = getSharedPreferences(context).getInt(KEY_REMINDER_HOUR, 9)
        val savedMinute = getSharedPreferences(context).getInt(KEY_REMINDER_MINUTE, 0)
        Log.d(TAG, "setReminderTime: set=$hour:$minute, saved=$savedHour:$savedMinute, success=$success")
        if (!success) {
            Toast.makeText(context, "Ошибка сохранения времени напоминания", Toast.LENGTH_SHORT).show()
        }

        if (isReminderEnabled(context)) {
            ReminderService.scheduleReminder(context, hour, minute)
        }
    }



    fun getReminderTimeString(context: Context): String {
        initialize(context)
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        val timeString = String.format("%02d:%02d", hour, minute)
        Log.d(TAG, "getReminderTimeString: $timeString")
        return timeString
    }

    fun getReminderTimeFormatted(context: Context): String {
        initialize(context)
        val timeString = getReminderTimeString(context)
        Log.d(TAG, "getReminderTimeFormatted: $timeString")
        return timeString
    }
}

// Merged: Keep BroadcastReceiver in the same file to consolidate reminder logic without breaking APIs
class ReminderService : android.content.BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reading_reminder_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_SHOW_REMINDER = "show_reminder"
        private const val TAG = "ReminderService"

        fun initialize(context: android.content.Context) {
            createNotificationChannel(context)
        }

        fun scheduleReminder(context: android.content.Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, ReminderService::class.java).apply {
                action = ACTION_SHOW_REMINDER
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
            )

            alarmManager.cancel(pendingIntent)
            android.util.Log.d(TAG, "Scheduling reminder for $hour:$minute")

            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                android.util.Log.w(TAG, "Cannot schedule exact alarms. Permission denied.")
                return
            }

            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            android.util.Log.d(TAG, "Reminder scheduled at ${calendar.time}")
        }

        fun cancelReminder(context: android.content.Context) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(context, ReminderService::class.java).apply {
                action = ACTION_SHOW_REMINDER
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
            )

            alarmManager.cancel(pendingIntent)
            android.util.Log.d(TAG, "Reminder cancelled")
        }

        private fun createNotificationChannel(context: android.content.Context) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    val name = "Напоминания о чтении"
                    val descriptionText = "Канал для напоминаний о ежедневных занятиях чтением"
                    val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
                    val channel = android.app.NotificationChannel(CHANNEL_ID, name, importance).apply {
                        description = descriptionText
                    }

                    notificationManager.createNotificationChannel(channel)
                    android.util.Log.d(TAG, "Notification channel created")
                } else {
                    android.util.Log.d(TAG, "Notification channel already exists")
                }
            }
        }

        fun sendTestNotification(context: android.content.Context) {
            android.util.Log.d(TAG, "sendTestNotification called")
            val service = ReminderService()
            service.showReminderNotification(context.applicationContext)
        }
    }

    override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
        when (intent.action) {
            ACTION_SHOW_REMINDER -> {
                showReminderNotification(context)
                val hour = ReminderManager.getReminderHour(context)
                val minute = ReminderManager.getReminderMinute(context)
                scheduleReminder(context, hour, minute)
            }
        }
    }

    fun showReminderNotification(context: android.content.Context) {
        val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) android.app.PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Время для чтения! 📚")
            .setContentText("Не забудьте потренироваться в скорочтении")
            .setSmallIcon(R.drawable.ic_practice)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
