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
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
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

    fun isVibrationEnabled(context: Context): Boolean {
        initialize(context)
        val enabled = getSharedPreferences(context).getBoolean(KEY_VIBRATION_ENABLED, true)
        Log.d(TAG, "isVibrationEnabled: $enabled")
        return enabled
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        initialize(context)
        val editor = getSharedPreferences(context).edit().putBoolean(KEY_VIBRATION_ENABLED, enabled)
        val success = editor.commit()
        val savedValue = getSharedPreferences(context).getBoolean(KEY_VIBRATION_ENABLED, true)
        Log.d(TAG, "setVibrationEnabled: set=$enabled, saved=$savedValue, success=$success")
        if (!success) {
            Toast.makeText(context, "Ошибка сохранения настройки вибрации", Toast.LENGTH_SHORT).show()
        }

        if (isReminderEnabled(context)) {
            val hour = getReminderHour(context)
            val minute = getReminderMinute(context)
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