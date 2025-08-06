package com.example.scorochtenie2

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TechniqueDemoActivity : AppCompatActivity() {

    private lateinit var technique: Technique
    private lateinit var textView: TextView
    private lateinit var guideView: View
    private lateinit var diagonalTextView: TextView
    private lateinit var diagonalLineView: DiagonalLineView
    private val selectedTextIndex = 1 // средний текст для демонстрации
    private val demoSpeed = 150L // самая медленная скорость для демонстрации

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique)

        // Инициализация TextResources
        TextResources.initialize(this)

        val techniqueName = intent.getStringExtra("technique_name") ?: "Неизвестная техника"
        
        // Инициализация UI
        findViewById<TextView>(R.id.toolbar_title).text = "Демонстрация: $techniqueName"
        findViewById<TextView>(R.id.timer_view).visibility = View.GONE // скрываем таймер в демо режиме
        
        textView = findViewById(R.id.text_view)
        guideView = findViewById(R.id.guide_view)
        diagonalTextView = findViewById(R.id.diagonal_text_view)
        diagonalLineView = findViewById(R.id.diagonal_line_view)

        // Создаем технику на основе названия
        technique = createTechnique(techniqueName)

        // Back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            technique.cancelAnimation()
            finish()
        }

        // Настраиваем контейнеры в зависимости от техники
        setupContainers(techniqueName)

        // Запускаем демонстрацию
        startDemo()
    }

    private fun createTechnique(techniqueName: String): Technique {
        return when (techniqueName) {
            "Чтение блоками" -> BlockReadingTechnique()
            "Чтение по диагонали" -> DiagonalReadingTechnique()
            "Метод указки" -> PointerMethodTechnique()
            "Предложения наоборот" -> SentenceReverseTechnique()
            "Слова наоборот" -> WordReverseTechnique()
            else -> PointerMethodTechnique() // default technique
        }
    }

    private fun setupContainers(techniqueName: String) {
        when (techniqueName) {
            "Чтение по диагонали" -> {
                findViewById<View>(R.id.scroll_container).visibility = View.GONE
                findViewById<View>(R.id.diagonal_container).visibility = View.VISIBLE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
            }
            else -> {
                findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE
                findViewById<View>(R.id.diagonal_container).visibility = View.GONE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
            }
        }
    }

    private fun startDemo() {
        if (technique is DiagonalReadingTechnique) {
            // Для диагонального чтения используем специальный textView
            technique.startAnimation(
                textView = diagonalTextView,
                guideView = diagonalLineView,
                durationPerWord = demoSpeed,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    // Закрываем демонстрацию после завершения
                    finish()
                }
            )
        } else {
            // Для остальных техник используем обычный textView
            technique.startAnimation(
                textView = textView,
                guideView = guideView,
                durationPerWord = demoSpeed,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    // Закрываем демонстрацию после завершения
                    finish()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
    }
}