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
    private lateinit var curtainView: CurtainOverlayView
    private lateinit var diagonalTextView: TextView
    private lateinit var diagonalLineView: DiagonalLineView
    private val selectedTextIndex = -1
    private val demoSpeed = 120L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_technique)
        TextResources.initialize(this)
        val techniqueName = intent.getStringExtra("technique_name") ?: "Неизвестная техника"
        findViewById<TextView>(R.id.toolbar_title).text = "Демонстрация: $techniqueName"
        findViewById<TextView>(R.id.timer_view).visibility = View.GONE
        textView = findViewById(R.id.text_view)
        guideView = findViewById(R.id.guide_view)
        curtainView = findViewById(R.id.curtain_view)
        diagonalTextView = findViewById(R.id.diagonal_text_view)
        diagonalLineView = findViewById(R.id.diagonal_line_view)
        technique = createTechnique(techniqueName)
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            technique.cancelAnimation()
            finish()
        }
        setupContainers(techniqueName)
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
                PointerMethodTechnique()
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
                diagonalLineView.visibility = View.VISIBLE
                diagonalLineView.bringToFront()
                diagonalLineView.requestLayout()
                diagonalLineView.invalidate()
            }
            "Зашумленный текст" -> {
                findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE
                findViewById<View>(R.id.diagonal_container).visibility = View.GONE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
                findViewById<View>(R.id.guide_view).visibility = View.GONE
                curtainView.visibility = View.VISIBLE
                guideView = curtainView
            }
            else -> {
                findViewById<View>(R.id.scroll_container).visibility = View.VISIBLE
                findViewById<View>(R.id.diagonal_container).visibility = View.GONE
                findViewById<View>(R.id.test_fragment_container).visibility = View.GONE
                guideView.visibility = View.VISIBLE
            }
        }
    }

    private fun startDemo() {
        textView.requestLayout()
        val speedForTechnique: Long = when (technique) {
            is DiagonalReadingTechnique -> 165L
            is WordReverseTechnique -> 70L
            else -> demoSpeed
        }

        if (technique is DiagonalReadingTechnique) {
            diagonalTextView.requestLayout()
            technique.startAnimation(
                textView = diagonalTextView,
                guideView = diagonalLineView,
                durationPerWord = speedForTechnique,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    finish()
                }
            )
        } else {
            technique.startAnimation(
                textView = textView,
                guideView = guideView,
                durationPerWord = speedForTechnique,
                selectedTextIndex = selectedTextIndex,
                onAnimationEnd = {
                    finish()
                }
            )
            if (guideView is PartiallyHiddenLinesView) {
                (guideView as PartiallyHiddenLinesView).setTextView(textView)
                guideView.invalidate()
            } else if (guideView is CurtainOverlayView) {
            } else {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        technique.cancelAnimation()
    }
}