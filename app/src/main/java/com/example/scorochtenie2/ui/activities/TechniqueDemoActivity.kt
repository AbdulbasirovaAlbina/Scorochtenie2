package com.example.scorochtenie2

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TechniqueDemoActivity : AppCompatActivity() {

    private lateinit var technique: Technique
    private lateinit var textView: TextView
    private lateinit var guideView: View
    private lateinit var curtainView: CurtainOverlayView
    private lateinit var diagonalTextView: TextView
    private lateinit var diagonalLineView: DiagonalLineView
    private val selectedTextIndex = -1 // используем демонстрационный текст
    // Скорость демонстрации (слов в минуту). Меньше значение — медленнее показ.
    private val demoSpeed = 120L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique)

        // Инициализация TextResources
        TextResources.initialize(this)

        val techniqueName = intent.getStringExtra("technique_name") ?: "Неизвестная техника"
        Log.d("TechniqueDemo", "Starting demo for technique: $techniqueName")

        // Инициализация UI
        findViewById<TextView>(R.id.toolbar_title).text = "Демонстрация: $techniqueName"
        findViewById<TextView>(R.id.timer_view).visibility = View.GONE // скрываем таймер в демо режиме

        textView = findViewById(R.id.text_view)
        guideView = findViewById(R.id.guide_view)
        curtainView = findViewById(R.id.curtain_view)
        diagonalTextView = findViewById(R.id.diagonal_text_view)
        diagonalLineView = findViewById(R.id.diagonal_line_view)

        // Проверяем тип guideView
        Log.d("TechniqueDemo", "guideView type: ${guideView.javaClass.simpleName}, visibility: ${guideView.visibility}")

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
            "Частично скрытые строки" -> PartiallyHiddenLinesTechnique()
            "Зашумленный текст" -> CurtainTextCurtainTechnique()
            else -> {
                Log.w("TechniqueDemo", "Unknown technique: $techniqueName, falling back to PointerMethodTechnique")
                PointerMethodTechnique() // default technique
            }
        }
    }

    private fun setupContainers(techniqueName: String) {
        when (techniqueName) {
            "Чтение по диагонали" -> {
                findViewById<View>(R.id.scroll_container).visibility = View.GONE
                findViewById<View>(R.id.diagonal_container).visibility = View.VISIBLE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
                guideView.visibility = View.GONE
                // Явно показываем и выносим линию поверх текста в демо
                diagonalLineView.visibility = View.VISIBLE
                diagonalLineView.bringToFront()
                diagonalLineView.requestLayout()
                diagonalLineView.invalidate()
                Log.d("TechniqueDemo", "diagonal_container visible")
            }
            "Зашумленный текст" -> {
                findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE
                findViewById<View>(R.id.diagonal_container).visibility = View.GONE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
                // Use curtain overlay instead of default guideView
                findViewById<View>(R.id.guide_view).visibility = View.GONE
                curtainView.visibility = View.VISIBLE
                guideView = curtainView
                Log.d("TechniqueDemo", "scroll_container visible for curtain technique")
            }
            else -> {
                findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE
                findViewById<View>(R.id.diagonal_container).visibility = View.GONE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
                guideView.visibility = View.VISIBLE
                Log.d("TechniqueDemo", "scroll_container visible, guide_view visibility: ${guideView.visibility}, type: ${guideView.javaClass.simpleName}")
            }
        }
    }

    private fun startDemo() {
        // Принудительно запрашиваем layout для textView
        textView.requestLayout()
        Log.d("TechniqueDemo", "Requesting layout for textView")

        // Индивидуальная скорость для техник в демо-режиме
        val speedForTechnique: Long = when (technique) {
            is DiagonalReadingTechnique -> 165L // чуть быстрее, чем базовая демо-скорость
            is WordReverseTechnique -> 70L // помедленнее, чем базовая демо-скорость
            else -> demoSpeed
        }

        if (technique is DiagonalReadingTechnique) {
            // Для диагонального чтения используем специальный textView
            diagonalTextView.requestLayout()
            technique.startAnimation(
                textView = diagonalTextView,
                guideView = diagonalLineView,
                durationPerWord = speedForTechnique,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    Log.d("TechniqueDemo", "Demo finished for technique: ${technique.displayName}")
                    finish()
                }
            )
        } else {
            // Для остальных техник используем обычный textView
            technique.startAnimation(
                textView = textView,
                guideView = guideView,
                durationPerWord = speedForTechnique,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    Log.d("TechniqueDemo", "Demo finished for technique: ${technique.displayName}")
                    finish()
                }
            )
            // Принудительно обновляем guideView, если это PartiallyHiddenLinesView
            if (guideView is PartiallyHiddenLinesView) {
                Log.d("TechniqueDemo", "guideView is PartiallyHiddenLinesView, setting textView and invalidating")
                (guideView as PartiallyHiddenLinesView).setTextView(textView)
                guideView.invalidate()
            } else if (guideView is CurtainOverlayView) {
                Log.d("TechniqueDemo", "guideView is CurtainOverlayView")
            } else {
                Log.w("TechniqueDemo", "guideView is not PartiallyHiddenLinesView, type: ${guideView.javaClass.simpleName}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
        Log.d("TechniqueDemo", "Demo activity destroyed")
    }
}