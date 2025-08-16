package com.example.scorochtenie2

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.app.TimePickerDialog

class ReminderSettingsActivity : AppCompatActivity() {

    private lateinit var reminderSwitch: SwitchCompat
    private lateinit var reminderTimeText: TextView
    private val TAG = "ReminderSettingsActivity"

    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            checkAndRequestExactAlarmPermission()
        } else {
            reminderSwitch.isChecked = false
            ReminderManager.setReminderEnabled(this, false)
            Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_reminder_setting)

        reminderSwitch = findViewById(R.id.reminder_switch)
        reminderTimeText = findViewById(R.id.reminder_time_text)

        loadSettings()
        Log.d(TAG, "onCreate: reminderSwitch=${reminderSwitch.isChecked}, time=${reminderTimeText.text}")

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "reminderSwitch changed: $isChecked")
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@setOnCheckedChangeListener
                    }
                }
                checkAndRequestExactAlarmPermission()
            } else {
                updateReminderSettings()
            }
        }



        reminderTimeText.setOnClickListener {
            val hour = ReminderManager.getReminderHour(this)
            val minute = ReminderManager.getReminderMinute(this)
            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                ReminderManager.setReminderTime(this, selectedHour, selectedMinute)
                reminderTimeText.text = ReminderManager.getReminderTimeString(this)
                Log.d(TAG, "Time selected: $selectedHour:$selectedMinute")
            }, hour, minute, true).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
    }

    private fun loadSettings() {
        val isReminderEnabled = ReminderManager.isReminderEnabled(this)
        reminderSwitch.isChecked = isReminderEnabled
        reminderTimeText.text = ReminderManager.getReminderTimeString(this)
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Пожалуйста, разрешите точные будильники в настройках приложения", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }
        updateReminderSettings()
    }

    private fun updateReminderSettings() {
        ReminderManager.setReminderEnabled(this, reminderSwitch.isChecked)
        reminderTimeText.text = ReminderManager.getReminderTimeString(this)
        Log.d(TAG, "updateReminderSettings: reminderSwitch=${reminderSwitch.isChecked}, time=${reminderTimeText.text}")
    }
}