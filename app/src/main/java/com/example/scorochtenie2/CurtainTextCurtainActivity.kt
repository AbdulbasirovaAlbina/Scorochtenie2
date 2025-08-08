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

class CurtainTextCurtainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var guideView: CurtainOverlayView
    private lateinit var timerView: TextView
    private val technique: Technique = CurtainTextCurtainTechnique()
    private var durationPerWord: Long = 400L
    private var selectedTextIndex: Int = 0
    private var techniqueName: String = "Зашумленный текст"
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
        setContentView(R.layout.activity_curtain_text)

        // Init texts
        TextResources.initialize(this)

        techniqueName = intent.getStringExtra("technique_name") ?: techniqueName
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

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            technique.cancelAnimation()
            stopTimer()
            saveTime(techniqueName, System.currentTimeMillis() - startTime)
            finish()
        }

        startTimer()
        
        // Добавляем небольшую задержку для правильной инициализации layout
        textView.post {
            technique.startAnimation(
                textView = textView,
                guideView = guideView,
                durationPerWord = durationPerWord,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    stopTimer()
                    saveTime(techniqueName, System.currentTimeMillis() - startTime)
                    showTestFragment()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
        stopTimer()
        saveTime(techniqueName, System.currentTimeMillis() - startTime)
        guideView.stop()
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
        findViewById<View>(R.id.scroll_view).visibility = View.GONE
        findViewById<View>(R.id.guide_view).visibility = View.GONE
        findViewById<View>(R.id.test_fragment_container).visibility = View.VISIBLE

        val testFragment = TestFragment.newInstance(selectedTextIndex, techniqueName, durationPerWord)
        supportFragmentManager.beginTransaction()
            .replace(R.id.test_fragment_container, testFragment)
            .commit()
    }
}


