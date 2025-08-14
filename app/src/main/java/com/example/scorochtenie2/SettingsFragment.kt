package com.example.scorochtenie2

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import java.util.*

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        setupThemeSwitch(view)
        setupGoogleSignIn(view)
        setupOtherSettings(view)
        setupClearProgress(view)
        setupReminderSettings(view)

        return view
    }

    private fun setupThemeSwitch(view: View) {
        val themeSwitch = view.findViewById<Switch>(R.id.theme_switch)

        // Загружаем текущую тему
        val sharedPref = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)
        themeSwitch.isChecked = isDarkTheme

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Сохраняем настройку темы
            with(sharedPref.edit()) {
                putBoolean("dark_theme", isChecked)
                apply()
            }

            // Применяем тему
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            
            // Перезапускаем активность для применения темы
            requireActivity().recreate()
        }
    }

    private fun setupGoogleSignIn(view: View) {
        val googleSignInButton = view.findViewById<LinearLayout>(R.id.google_signin_layout)
        googleSignInButton.setOnClickListener {
            Toast.makeText(context, "Вход через Google (в разработке)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOtherSettings(view: View) {
        val supportLayout = view.findViewById<LinearLayout>(R.id.support_layout)

        supportLayout.setOnClickListener {
            Toast.makeText(context, "Поддержка", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClearProgress(view: View) {
        val clearProgressLayout = view.findViewById<LinearLayout>(R.id.clear_progress_layout)
        clearProgressLayout.setOnClickListener {
            showClearProgressDialog()
        }
    }

    private fun showClearProgressDialog() {
        context?.let { ctx ->
            AlertDialog.Builder(ctx)
                .setTitle("Очистить прогресс")
                .setMessage("Вы уверены, что хотите удалить весь прогресс? Это действие нельзя отменить.")
                .setPositiveButton("Очистить") { _, _ ->
                    clearAllProgress()
                    Toast.makeText(context, "Прогресс успешно очищен", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

    private fun clearAllProgress() {
        // Очищаем всю статистику и прогресс
        TestResultManager.clearAllProgress(requireContext())
        
        // Показываем подробное сообщение о том, что было очищено
        Toast.makeText(
            context, 
            "Весь прогресс очищен! Теперь вы можете начать заново.", 
            Toast.LENGTH_LONG
        ).show()
        
        // Обновляем UI для всех фрагментов
        val currentFragment = parentFragmentManager.findFragmentById(R.id.fragment_container)
        
        // Перезагружаем текущий фрагмент для обновления UI
        when (currentFragment) {
            is ProgressFragment -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProgressFragment())
                    .commit()
            }
            is PracticeFragment -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PracticeFragment())
                    .commit()
            }
            is HomeFragment -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }
    }
    
    private fun setupReminderSettings(view: View) {
        // Находим элементы в include layout
        val reminderSettingsView = view.findViewById<View>(R.id.reminder_settings)
        val reminderSwitch = reminderSettingsView.findViewById<SwitchCompat>(R.id.reminder_switch)
        val reminderTimeText = reminderSettingsView.findViewById<TextView>(R.id.reminder_time_text)
        
        // Загружаем текущие настройки напоминаний
        val isEnabled = ReminderManager.isReminderEnabled(requireContext())
        reminderSwitch.isChecked = isEnabled
        
        // Устанавливаем текущее время напоминания
        reminderTimeText.text = ReminderManager.getReminderTimeFormatted(requireContext())
        
        // Обработчик переключателя
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            ReminderManager.setReminderEnabled(requireContext(), isChecked)
            
            if (isChecked) {
                Toast.makeText(context, "Напоминания включены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Напоминания отключены", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Обработчик выбора времени
        reminderTimeText.setOnClickListener {
            showTimePickerDialog(reminderTimeText)
        }
    }
    
    private fun showTimePickerDialog(timeText: TextView) {
        val calendar = Calendar.getInstance()
        val currentHour = ReminderManager.getReminderHour(requireContext())
        val currentMinute = ReminderManager.getReminderMinute(requireContext())
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                ReminderManager.setReminderTime(requireContext(), hourOfDay, minute)
                timeText.text = ReminderManager.getReminderTimeFormatted(requireContext())
                
                Toast.makeText(
                    context,
                    "Время напоминания установлено на ${ReminderManager.getReminderTimeFormatted(requireContext())}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            currentHour,
            currentMinute,
            true // 24-часовой формат
        )
        
        timePickerDialog.setTitle("Выберите время напоминания")
        timePickerDialog.show()
    }
}