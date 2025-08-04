package com.example.scorochtenie2

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.animation.addListener
import kotlin.math.min

class BlockReadingTechnique : Technique("Чтение блоками", "Чтение блоками") {
    private var currentBlockIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private var currentPartText: String = ""
    private var lineCount: Int = 0
    private var lines: List<IntRange> = emptyList()
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false

    override val description: SpannableString
        get() {
            val text = "Чтение \"блоками\" — это техника скорочтения, при которой текст воспринимается не по отдельным словам, а целыми смысловыми фрагментами. Такой подход помогает быстрее обрабатывать информацию и лучше удерживать общий контекст.\n" +
                    "Сосредоточьтесь на восприятии сразу нескольких строк как единого блока — это развивает навык охватывать больше текста за раз и ускоряет чтение без потери понимания."
            val spannable = SpannableString(text)
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                text.indexOf("целыми смысловыми фрагментами"),
                text.indexOf("целыми смысловыми фрагментами") + "целыми смысловыми фрагментами".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                text.indexOf("сразу нескольких строк"),
                text.indexOf("сразу нескольких строк") + "сразу нескольких строк".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        try {
            this.selectedTextIndex = selectedTextIndex
            fullText = TextResources.getOtherTexts()[displayName]?.getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: ""
            if (fullText.isEmpty()) {
                textView.text = "Текст недоступен"
                onAnimationEnd()
                return
            }

            currentBlockIndex = 0
            lastScrollY = 0
            isAnimationActive = true

            val safeDurationPerWord = if (durationPerWord <= 0) SpeedConfig.getDurationPerWord(1) else durationPerWord
            val wordDurationMs = (60_000 / safeDurationPerWord).coerceAtLeast(50L)
            scrollView = textView.parent as? ScrollView
            textView.gravity = android.view.Gravity.TOP
            textView.isSingleLine = false
            textView.maxLines = Int.MAX_VALUE
            guideView.visibility = View.INVISIBLE // Устанавливаем guideView невидимым
            handler.post {
                if (isAnimationActive) {
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }
            }
        } catch (e: Exception) {
            textView.text = "Ошибка анимации"
            onAnimationEnd()
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        currentPartText = fullText
        textView.text = currentPartText

        handler.post {
            if (!isAnimationActive) return@post
            val layout = textView.layout
            if (layout == null) {
                textView.text = "Ошибка отображения текста"
                if (isAnimationActive) onAnimationEnd()
                return@post
            }
            lineCount = layout.lineCount
            lines = (0 until lineCount).map { line ->
                layout.getLineStart(line)..layout.getLineEnd(line)
            }
            currentBlockIndex = 0
            animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd)
        }
    }

    private fun animateNextBlock(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        if (currentBlockIndex * 2 >= lineCount) {
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            textView.text = currentPartText
            if (isAnimationActive) onAnimationEnd()
            return
        }

        val (wordCountInBlock, _, _) = highlightBlock(textView)
        startBlockAnimation(textView, guideView, wordDurationMs, wordCountInBlock, onAnimationEnd)
    }

    private fun highlightBlock(textView: TextView): Triple<Int, Int, Int> {
        if (!isAnimationActive) return Triple(0, 0, 0)

        val spannable = SpannableString(currentPartText)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = min(firstLineIndex + 1, lineCount - 1)
        val startIndex = lines[firstLineIndex].first
        val endIndex = lines[secondLineIndex].last

        val blockText = currentPartText.substring(startIndex, endIndex)
        val wordCountInBlock = blockText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        val firstLineText = currentPartText.substring(lines[firstLineIndex].first, lines[firstLineIndex].last)
        val firstLineWordCount = firstLineText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        val secondLineText = if (secondLineIndex > firstLineIndex) {
            currentPartText.substring(lines[secondLineIndex].first, lines[secondLineIndex].last)
        } else {
            ""
        }
        val secondLineWordCount = secondLineText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        if (startIndex < spannable.length && endIndex <= spannable.length) {
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
        return Triple(wordCountInBlock, firstLineWordCount, secondLineWordCount)
    }

    private fun startBlockAnimation(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        wordCountInBlock: Int,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {
            handler.postDelayed({
                if (isAnimationActive) animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        val firstLineIndex = currentBlockIndex * 2
        val secondLineIndex = min(firstLineIndex + 1, lineCount - 1)

        val blockDurationMs = (wordCountInBlock * wordDurationMs).coerceAtLeast(50L)

        scrollView?.let { sv ->
            handler.post {
                if (!isAnimationActive) return@post
                val scrollViewHeight = sv.height
                val currentScrollY = sv.scrollY
                val lineTopPosition = layout.getLineTop(firstLineIndex)
                val lineBottomPosition = layout.getLineBottom(secondLineIndex)

                val visibleTop = currentScrollY
                val visibleBottom = currentScrollY + scrollViewHeight * 2 / 3

                if (lineTopPosition < visibleTop || lineBottomPosition > visibleBottom) {
                    val targetScrollY = (lineTopPosition - scrollViewHeight / 3).coerceAtLeast(0).toInt()
                    if (targetScrollY != lastScrollY) {
                        ValueAnimator.ofInt(currentScrollY, targetScrollY).apply {
                            duration = blockDurationMs / 2
                            addUpdateListener { animation ->
                                val value = animation.animatedValue as Int
                                sv.scrollTo(0, value)
                            }
                            addListener(
                                onEnd = {
                                    lastScrollY = targetScrollY
                                }
                            )
                            start()
                        }
                    }
                }
            }
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = blockDurationMs
            addListener(
                onEnd = {
                    if (isAnimationActive) {
                        currentBlockIndex++
                        animateNextBlock(textView, guideView, wordDurationMs, onAnimationEnd)
                    }
                }
            )
            start()
        }
    }

    override fun cancelAnimation() {
        isAnimationActive = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}