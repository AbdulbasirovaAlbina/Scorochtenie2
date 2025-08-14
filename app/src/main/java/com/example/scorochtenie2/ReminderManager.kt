package com.example.scorochtenie2

import android.content.Context
import android.content.SharedPreferences

object ReminderManager {
    private const val PREF_NAME = "ReminderSettings"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun isReminderEnabled(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_REMINDER_ENABLED, false)
    }
    
    fun setReminderEnabled(context: Context, enabled: Boolean) {
        getSharedPreferences(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        
        if (enabled) {
            val hour = getReminderHour(context)
            val minute = getReminderMinute(context)
            ReminderService.scheduleReminder(context, hour, minute)
        } else {
            ReminderService.cancelReminder(context)
        }
    }
    
    fun getReminderHour(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_REMINDER_HOUR, 9) // По умолчанию 9:00
    }
    
    fun getReminderMinute(context: Context): Int {
        return getSharedPreferences(context).getInt(KEY_REMINDER_MINUTE, 0)
    }
    
    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        getSharedPreferences(context).edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
        
        // Если напоминания включены, обновляем расписание
        if (isReminderEnabled(context)) {
            ReminderService.scheduleReminder(context, hour, minute)
        }
    }
    
    fun getReminderTimeString(context: Context): String {
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getReminderTimeFormatted(context: Context): String {
        val hour = getReminderHour(context)
        val minute = getReminderMinute(context)
        
        return when {
            hour == 0 -> "12:${String.format("%02d", minute)} AM"
            hour < 12 -> "${hour}:${String.format("%02d", minute)} AM"
            hour == 12 -> "12:${String.format("%02d", minute)} PM"
            else -> "${hour - 12}:${String.format("%02d", minute)} PM"
        }
    }
}

