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

class SentenceReverseActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var guideView: View
    private lateinit var timerView: TextView
    private val technique: Technique = SentenceReverseTechnique()
    private var durationPerWord: Long = 400L
    private var selectedTextIndex: Int = 0
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

        val techniqueName = intent.getStringExtra("technique_name") ?: "Предложения наоборот"
        durationPerWord = SpeedConfig.getDurationPerWord(intent.getIntExtra("speed", 1))
        selectedTextIndex = when (intent.getStringExtra("text_length")) {
            "Короткий" -> 0
            "Средний" -> 1
            "Длинный" -> 2
            else -> 1
        }
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
                finish()
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
}