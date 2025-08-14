package com.example.scorochtenie2

import android.content.Context
import android.os.Bundle
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем сохраненную тему до создания UI
        applyTheme()
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        
        // Инициализируем TextResources
        TextResources.initialize(this)
        
        // Создаем канал для уведомлений
        ReminderService.createNotificationChannel(this)
        
        // Вначале настраиваем нижнюю навигацию
        setupBottomNavigation()
        setupWindowInsets()

        // Затем загружаем стартовый фрагмент или вкладку из интента
        if (savedInstanceState == null) {
            val tabFromIntent = intent?.getIntExtra("tab", -1) ?: -1
            if (tabFromIntent in 0..4) {
                switchToTab(tabFromIntent)
            } else {
                // По умолчанию Домой
                switchToTab(0)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tabFromIntent = intent.getIntExtra("tab", -1)
        if (tabFromIntent in 0..4) {
            switchToTab(tabFromIntent)
        }
    }
    
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_learning -> {
                    loadFragment(LearningFragment())
                    true
                }
                R.id.navigation_practice -> {
                    loadFragment(PracticeFragment())
                    true
                }
                R.id.navigation_progress -> {
                    loadFragment(ProgressFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    private fun setupWindowInsets() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }
    
    private fun applyTheme() {
        val sharedPref = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val isDarkTheme = sharedPref.getBoolean("dark_theme", false)
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    fun switchToTab(tabIndex: Int) {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        when (tabIndex) {
            0 -> bottomNavigation.selectedItemId = R.id.navigation_home
            1 -> bottomNavigation.selectedItemId = R.id.navigation_learning
            2 -> bottomNavigation.selectedItemId = R.id.navigation_practice
            3 -> bottomNavigation.selectedItemId = R.id.navigation_progress
            4 -> bottomNavigation.selectedItemId = R.id.navigation_settings
        }
    }
}
