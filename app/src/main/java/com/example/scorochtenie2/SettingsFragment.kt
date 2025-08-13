package com.example.scorochtenie2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

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
        // Очищаем SharedPreferences для времени техник
        val techniqueTimesPref = requireActivity().getSharedPreferences("TechniqueTimes", Context.MODE_PRIVATE)
        with(techniqueTimesPref.edit()) {
            clear()
            apply()
        }

        // Очищаем статистику тестов
        TestResultManager.clearAllProgress(requireContext())
    }
}