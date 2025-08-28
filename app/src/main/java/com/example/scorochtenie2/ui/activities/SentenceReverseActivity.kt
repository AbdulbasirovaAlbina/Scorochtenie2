package com.example.scorochtenie2

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SentenceReverseActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var guideView: View
    private lateinit var timerView: TextView
    private val technique: Technique = SentenceReverseTechnique()
    private var durationPerWord: Long = 400L
    private var selectedTextIndex: Int = 0
    private var techniqueName: String = "Предложения наоборот"
    private var startTime: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                timerView.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique)

        TextResources.initialize(this)

        // Получение параметров из Intent
        techniqueName = intent.getStringExtra("technique_name") ?: "Предложения наоборот"
        run {
            val speedIndex = intent.getIntExtra("speed", 1)
            val wpm = SpeedConfig.getWpmForTechnique(techniqueName, speedIndex)
            durationPerWord = (60_000L / wpm).coerceAtLeast(50L)
        }
        val textLength = intent.getStringExtra("text_length") ?: "Средний"
        
        // Проверяем, есть ли доступные тексты для выбранной длины
        val availableTexts = TestResultManager.getAvailableTextsByLength(this, techniqueName, textLength)
        
        if (availableTexts.isEmpty()) {
            // Все тексты данной длины завершены
            showCompletionDialog(textLength)
            return
        }
        
        // Выбираем случайный доступный текст
        selectedTextIndex = availableTexts.random()
        val fontSizeMultiplier = FontConfig.getFontSizeMultiplier(intent.getIntExtra("font_size", 1))

        findViewById<TextView>(R.id.toolbar_title).text = techniqueName
        textView = findViewById(R.id.text_view)
        guideView = findViewById(R.id.guide_view)
        timerView = findViewById(R.id.timer_view)
        textView.textSize = FontConfig.BASE_TEXT_SIZE * fontSizeMultiplier

        // Переключаем видимость контейнеров
        findViewById<View>(R.id.diagonal_container).visibility = View.GONE
        findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            technique.cancelAnimation()
            stopTimer()
            saveTime(techniqueName, System.currentTimeMillis() - startTime)
            finish()
        }

        startTimer()
        technique.startAnimation(
            textView = textView,
            guideView = guideView,
            durationPerWord = durationPerWord,
            selectedTextIndex = selectedTextIndex,
            onAnimationEnd = {
                stopTimer()
                saveTime(techniqueName, System.currentTimeMillis() - startTime)
                // Запускаем тест после завершения анимации
                showTestFragment()
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
        stopTimer()
        saveTime(intent.getStringExtra("technique_name") ?: "Предложения наоборот", System.currentTimeMillis() - startTime)
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacks(timerRunnable)
    }

    private fun saveTime(techniqueName: String, elapsedTime: Long) {
        val sharedPreferences = getSharedPreferences("TechniqueTimes", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(techniqueName, elapsedTime)
        editor.apply()
    }

    private fun showTestFragment() {
        // Скрываем контейнеры с текстом и показываем контейнер для теста
        findViewById<View>(R.id.scroll_container).visibility = View.GONE
        findViewById<View>(R.id.diagonal_container).visibility = View.GONE
        findViewById<View>(R.id.test_fragment_container).visibility = View.VISIBLE

        // Запускаем тест
        val testFragment = TestFragment.newInstance(selectedTextIndex, techniqueName, durationPerWord)
        supportFragmentManager.beginTransaction()
            .replace(R.id.test_fragment_container, testFragment)
            .commit()
    }

    private fun showCompletionDialog(textLength: String) {
        val message = "Все тексты длины \"$textLength\" завершены. Пожалуйста, выберите другую длину."
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Все тексты завершены")
            .setMessage(message)
            .setPositiveButton("ОК") { _, _ ->
                // Закрываем текущую активность
                finish()
            }
            .create()
        dialog.show()
    }
}