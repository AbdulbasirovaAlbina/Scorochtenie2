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
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.animation.addListener

class CurtainTextCurtainTechnique : Technique("Зашумленный текст", "Зашумленный текст") {

    private var isRunning = false
    private var currentWordIndex = 0
    private var fullText: String = ""
    private var currentWords: List<String> = emptyList()
    private var animator: ValueAnimator? = null
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
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

        // Загружаем текст
        fullText = if (selectedTextIndex == -1) {
            TextResources.getDemoTextForTechnique(displayName)
        } else {
            TextResources.getOtherTexts()[displayName]?.getOrNull(selectedTextIndex)?.text ?: ""
        }.replace("\n", " ")
        if (fullText.isEmpty()) {
            Log.e("CurtainText", "Text is empty for index $selectedTextIndex")
            textView.text = "Текст недоступен"
            onAnimationEnd()
            return
        }

        currentWords = fullText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        Log.d("CurtainText", "Total words: ${currentWords.size}")

        // Настройка TextView
        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.text = fullText

        // Находим ScrollView
        scrollView = textView.parent as? ScrollView
        lastScrollY = 0

        if (guideView is CurtainOverlayView) {
            guideView.visibility = View.VISIBLE
            // Запускаем подсветку слов
            startWordHighlighting(textView, durationPerWord, onAnimationEnd)
            // Запускаем анимацию шторки
            guideView.start(durationPerWord, currentWords.size) {
                // Не вызываем onAnimationEnd здесь, так как подсветка слов управляет завершением
                Log.d("CurtainText", "Curtain animation completed")
            }
        } else {
            Log.e("CurtainText", "guideView is not CurtainOverlayView")
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
            textView.text = fullText // Убираем подсветку
            if (isRunning) {
                Log.d("CurtainText", "Word highlighting completed at index $currentWordIndex")
                isRunning = false
                onAnimationEnd()
            }
            return
        }

        // Проверяем layout
        val layout = textView.layout
        if (layout == null) {
            Log.w("CurtainText", "Layout not ready, retrying")
            handler.postDelayed({
                if (isRunning) highlightNextWord(textView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        // Подсвечиваем текущее слово
        highlightCurrentWord(textView)

        // Прокрутка ScrollView
        scrollToCurrentWord(textView, wordDurationMs)

        // Переходим к следующему слову
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
        HighlightColorHelper.clearHighlights(spannable)

        // Подсвечиваем текущее слово
        var startIndex = 0
        var wordCount = 0

        currentWords.forEach { word ->
            if (wordCount == currentWordIndex) {
                val endIndex = startIndex + word.length
                if (startIndex < fullText.length && endIndex <= fullText.length) {
                    HighlightColorHelper.applyHighlight(
                        textView.context,
                        spannable,
                        startIndex,
                        endIndex
                    )
                    Log.d("CurtainText", "Highlighted word at index: $currentWordIndex, word: '$word'")
                }
            }
            startIndex += word.length
            if (startIndex < fullText.length && fullText[startIndex] == ' ') {
                startIndex++
            }
            wordCount++
        }

        textView.text = spannable
    }

    private fun scrollToCurrentWord(textView: TextView, wordDurationMs: Long) {
        if (!isRunning) return

        val layout = textView.layout ?: return
        val wordStartIndex = getWordStartIndex(currentWordIndex, fullText)
        if (wordStartIndex < 0 || wordStartIndex >= fullText.length) {
            Log.w("CurtainText", "Invalid word start index: $wordStartIndex")
            return
        }

        val startLine = layout.getLineForOffset(wordStartIndex)
        val lineTopPosition = layout.getLineTop(startLine)
        val lineBottomPosition = layout.getLineBottom(startLine)

        scrollView?.let { sv ->
            handler.post {
                if (!isRunning) return@post
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = wordDurationMs / 2
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                    Log.d("CurtainText", "Scrolled to Y: $targetScrollY")
                                }
                            )
                            start()
                        }
                    }
                }
            }
        }
    }

    private fun getWordStartIndex(wordIndex: Int, text: String): Int {
        var startIndex = 0
        var count = 0
        text.split("\\s+".toRegex()).forEachIndexed { index, word ->
            if (count == wordIndex) {
                return startIndex
            }
            startIndex += word.length
            if (startIndex < text.length && text[startIndex] == ' ') {
                startIndex++
            }
            count++
        }
        return startIndex
    }

    override fun cancelAnimation() {
        isRunning = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}