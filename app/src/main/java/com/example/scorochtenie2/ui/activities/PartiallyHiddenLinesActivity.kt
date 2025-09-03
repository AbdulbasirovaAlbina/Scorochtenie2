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

class PartiallyHiddenLinesActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var guideView: View
    private lateinit var timerView: TextView
    private val technique: Technique = PartiallyHiddenLinesTechnique()
    private var durationPerWord: Long = 400L
    private var selectedTextIndex: Int = 0
    private var techniqueName: String = "Частично скрытые строки"
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
        setContentView(R.layout.activity_partially_hidden_lines)


        TextResources.initialize(this)


        techniqueName = intent.getStringExtra("technique_name") ?: "Частично скрытые строки"
        run {
            val speedIndex = intent.getIntExtra("speed", 1)
            val wpm = SpeedConfig.getWpmForTechnique(techniqueName, speedIndex)
            durationPerWord = (60_000L / wpm).coerceAtLeast(50L)
        }
        val textLength = intent.getStringExtra("text_length") ?: "Средний"
        

        val availableTexts = TestResultManager.getAvailableTextsByLength(this, techniqueName, textLength)
        
        if (availableTexts.isEmpty()) {

            showCompletionDialog(textLength)
            return
        }
        

        selectedTextIndex = availableTexts.random()
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

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
        stopTimer()
        saveTime(techniqueName, System.currentTimeMillis() - startTime)

        if (guideView is PartiallyHiddenLinesView) {
            (guideView as PartiallyHiddenLinesView).hideMask()
        }

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

    private fun showCompletionDialog(textLength: String) {
        val message = "Все тексты длины \"$textLength\" завершены. Пожалуйста, выберите другую длину."
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Все тексты завершены")
            .setMessage(message)
            .setPositiveButton("ОК") { _, _ ->

                finish()
            }
            .create()
        dialog.show()
    }
}
