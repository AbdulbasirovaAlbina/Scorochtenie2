package com.example.scorochtenie2

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.animation.addListener

class CurtainTextCurtainTechnique : Technique("Зашумленный текст", "Зашумленный текст") {

    private var isRunning = false
    private var currentWordIndex = 0
    private var fullText: String = ""
    private var currentWords: List<String> = emptyList()
    private var animator: ValueAnimator? = null
    private val handler = Handler(Looper.getMainLooper())

    override val description: SpannableString
        get() {
            val text = "Зашумленный текст — техника, в которой поверх текста сверху вниз движутся полупрозрачные цветные полосы. Они ограничивают повторные возвраты взгляда и помогают удерживать ритм.\n" +
                    "Сосредоточьтесь на чтении видимых областей и двигайтесь вперёд вместе с движением шторки."
            return SpannableString(text).apply {
                setSpan(StyleSpan(android.graphics.Typeface.BOLD), 0, name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val key1 = "полупрозрачные цветные полосы"
                val i1 = text.indexOf(key1)
                if (i1 >= 0) setSpan(StyleSpan(android.graphics.Typeface.BOLD), i1, i1 + key1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        isRunning = true
        currentWordIndex = 0

        val text = TextResources.getOtherTexts()[displayName]?.getOrNull(selectedTextIndex)?.text ?: ""
        fullText = text
        currentWords = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        
        textView.text = text

        // Настройка TextView для прокрутки
        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE

        if (guideView is CurtainOverlayView) {
            guideView.visibility = View.VISIBLE
            
            // Запускаем подсветку слов
            startWordHighlighting(textView, durationPerWord, onAnimationEnd)
            
            guideView.start(durationPerWord, currentWords.size) {
                if (isRunning) onAnimationEnd()
            }
        } else {
            // If guideView is not our overlay, just end immediately
            onAnimationEnd()
        }
    }

    private fun startWordHighlighting(
        textView: TextView,
        durationPerWord: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isRunning) return

        val wordDurationMs = (60_000 / durationPerWord).coerceAtLeast(50L)
        
        handler.post {
            if (isRunning) {
                highlightNextWord(textView, wordDurationMs, onAnimationEnd)
            }
        }
    }

    private fun highlightNextWord(
        textView: TextView,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isRunning) return

        if (currentWordIndex >= currentWords.size) {
            // Анимация завершена
            if (isRunning) onAnimationEnd()
            Log.d("CurtainText", "Word highlighting completed")
            return
        }

        // Подсвечиваем текущее слово
        highlightCurrentWord(textView)
        
        // Переходим к следующему слову через заданное время
        handler.postDelayed({
            if (isRunning) {
                currentWordIndex++
                highlightNextWord(textView, wordDurationMs, onAnimationEnd)
            }
        }, wordDurationMs)
    }

    private fun highlightCurrentWord(textView: TextView) {
        if (!isRunning) return

        val spannable = SpannableString(fullText)
        
        // Удаляем предыдущие подсветки
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        // Подсвечиваем текущее слово
        var startIndex = 0
        var wordCount = 0

        currentWords.forEach { word ->
            if (wordCount == currentWordIndex) {
                val endIndex = startIndex + word.length
                spannable.setSpan(
                    BackgroundColorSpan(Color.YELLOW),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                Log.d("CurtainText", "Highlighted word at index: $currentWordIndex, word: '$word'")
            }
            startIndex += word.length
            if (startIndex < fullText.length && fullText[startIndex] == ' ') {
                startIndex++
            }
            wordCount++
        }

        textView.text = spannable
    }

    override fun cancelAnimation() {
        isRunning = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}


