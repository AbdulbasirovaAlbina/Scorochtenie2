package com.example.scorochtenie2

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.animation.addListener

class DiagonalReadingTechnique : Technique("Чтение по диагонали", "Чтение по диагонали") {
    private var currentPosition = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false

    override val description: SpannableString
        get() {
            val text = "Чтение по диагонали — это способ быстрого ознакомления с текстом, при котором взгляд скользит сверху вниз по диагонали, захватывая общую структуру и главные элементы.\n" +
                    "Вместо того чтобы читать каждое слово, вы охватываете страницу бегло, выхватывая смысловые опоры — такие как начальные и конечные слова абзацев, цифры или повторы.\n" +
                    "Этот метод позволяет быстро получить общее представление о содержании и решить, стоит ли читать подробнее."
            val spannable = SpannableString(text)
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0,
                name.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                text.indexOf("сверху вниз по диагонали"),
                text.indexOf("сверху вниз по диагонали") + "сверху вниз по диагонали".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                text.indexOf("смысловые опоры"),
                text.indexOf("смысловые опоры") + "смысловые опоры".length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                text.indexOf("начальные и конечные"),
                text.indexOf("начальные и конечные") + "начальные и конечные".length,
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
            fullText = TextResources.getDiagonalTexts().getOrNull(selectedTextIndex)?.text?.replace("\n", " ") ?: ""
            if (fullText.isEmpty()) {
                textView.text = "Текст недоступен"
                onAnimationEnd()
                return
            }

            currentPosition = 0
            isAnimationActive = true

            val safeDurationPerWord = if (durationPerWord <= 0) SpeedConfig.getDurationPerWord(1) else durationPerWord
            val wordDurationMs = (60_000 / safeDurationPerWord).coerceAtLeast(50L)

            textView.gravity = android.view.Gravity.TOP
            textView.isSingleLine = false
            textView.maxLines = Int.MAX_VALUE
            // Для диагонального чтения направляющая линия должна быть видима
            // Если нам передали линию как guideView (в демо), покажем её.
            // В обычной активности линия находится и показывается отдельно.
            guideView.visibility = View.VISIBLE

            handler.post {
                if (isAnimationActive) {
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }
            }
        } catch (e: Exception) {
            Log.e("DiagonalReading", "Animation start error: ${e.message}")
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

        if (currentPosition >= fullText.length) {
            Log.d("DiagonalReading", "Text ended at position: $currentPosition")
            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            clearHighlight(textView)
            if (isAnimationActive) onAnimationEnd()
            return
        }

        // Устанавливаем полный текст временно, чтобы определить layout
        textView.text = fullText.substring(currentPosition)
        // Ждём, пока layout станет доступен
        textView.post {
            if (!isAnimationActive) return@post

            val layout = textView.layout
            if (layout == null) {
                Log.e("DiagonalReading", "Layout is null, retrying...")
                handler.postDelayed({
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }, 50)
                return@post
            }

            val visibleHeight = textView.height.toFloat()
            val lineCount = layout.lineCount
            val lineHeight = if (lineCount > 0) {
                (layout.getLineBottom(0) - layout.getLineTop(0)).toFloat()
            } else {
                textView.textSize
            }
            Log.d("DiagonalReading", "Line count: $lineCount, Visible height: $visibleHeight, Line height: $lineHeight")

            // Проверяем, помещается ли весь текст на одну страницу
            val maxVisibleLines = (visibleHeight / lineHeight).toInt().coerceAtLeast(1)
            if (lineCount <= maxVisibleLines) {
                // Если весь текст помещается, отображаем его целиком
                val partText = fullText.substring(currentPosition).trim()
                Log.d("DiagonalReading", "Text fits on one page (lines: $lineCount, max: $maxVisibleLines), displaying: ${partText.take(50)}... (length: ${partText.length})")
                textView.text = partText
                textView.visibility = View.VISIBLE
                val parent = textView.parent as View
                val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
                if (diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                    diagonalLineView.requestLayout()
                    diagonalLineView.invalidate()
                    Log.d("DiagonalReading", "Starting animation for final text length: ${partText.length}")
                    startDiagonalAnimation(textView, guideView, fullText.length, partText, wordDurationMs, onAnimationEnd)
                } else {
                    Log.e("DiagonalReading", "DiagonalLineView not found")
                    if (isAnimationActive) onAnimationEnd()
                }
                return@post
            }

            // Находим последнюю видимую строку
            var lastVisibleLine = 0
            for (i in 0 until lineCount) {
                if (layout.getLineTop(i) < visibleHeight) {
                    lastVisibleLine = i
                } else {
                    break
                }
            }
            lastVisibleLine = if (lastVisibleLine > 0) lastVisibleLine else 0

            // Находим конец последней видимой строки
            var breakPosition = if (lineCount > 0) {
                var endOffset = layout.getLineEnd(lastVisibleLine)
                // Корректируем до конца последнего слова на строке
                var adjustedEnd = endOffset
                if (adjustedEnd < fullText.length) {
                    // Ищем конец слова
                    while (adjustedEnd < fullText.length && !fullText[adjustedEnd].isWhitespace()) {
                        adjustedEnd++
                    }
                    // Если достигли конца текста, включаем его
                    if (adjustedEnd >= fullText.length) {
                        adjustedEnd = fullText.length
                    } else {
                        // Проверяем, не слишком ли короткий остаток
                        val remainingAfterAdjusted = fullText.length - (currentPosition + adjustedEnd)
                        if (remainingAfterAdjusted <= 50) {
                            adjustedEnd = fullText.length
                        }
                    }
                }
                adjustedEnd.coerceAtMost(fullText.length)
            } else {
                fullText.length
            }

            // Корректируем breakPosition относительно currentPosition
            breakPosition = (currentPosition + breakPosition).coerceAtMost(fullText.length)
            Log.d("DiagonalReading", "Current position: $currentPosition, Break position: $breakPosition, Last visible line: $lastVisibleLine")

            val partText = fullText.substring(currentPosition, breakPosition).trim()
            if (partText.isEmpty()) {
                Log.d("DiagonalReading", "Empty part text, advancing position")
                currentPosition = breakPosition
                showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                return@post
            }

            // Логируем текст последней видимой строки
            val lastLineText = if (lineCount > 0) {
                textView.text.subSequence(layout.getLineStart(lastVisibleLine), layout.getLineEnd(lastVisibleLine)).toString().trim()
            } else {
                "No lines"
            }
            Log.d("DiagonalReading", "Last visible line text: $lastLineText")

            textView.text = partText
            textView.visibility = View.VISIBLE
            Log.d("DiagonalReading", "Displaying partText: ${partText.take(50)}... (length: ${partText.length})")

            handler.post {
                if (!isAnimationActive) return@post
                val parent = textView.parent as View
                val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
                if (diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                    diagonalLineView.requestLayout()
                    diagonalLineView.invalidate()
                    Log.d("DiagonalReading", "Starting animation for partText length: ${partText.length}")
                    startDiagonalAnimation(textView, guideView, breakPosition, partText, wordDurationMs, onAnimationEnd)
                } else {
                    Log.e("DiagonalReading", "DiagonalLineView not found")
                    if (isAnimationActive) onAnimationEnd()
                }
            }
        }
    }

    private fun startDiagonalAnimation(
        textView: TextView,
        guideView: View,
        newPosition: Int,
        partText: String,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) return

        animator?.cancel()

        val wordCount = partText.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val totalDuration = wordCount * wordDurationMs

        val layout = textView.layout
        if (layout == null) {
            Log.e("DiagonalReading", "Layout is null in animation, retrying...")
            handler.postDelayed({
                if (isAnimationActive) startDiagonalAnimation(textView, guideView, newPosition, partText, wordDurationMs, onAnimationEnd)
            }, 50)
            return
        }

        val width = textView.width.toFloat()
        val visibleHeight = textView.height.toFloat()
        val totalLines = layout.lineCount
        // Используем полную высоту TextView для анимации
        val animationHeight = if (totalLines > 0) {
            layout.getLineBottom(totalLines - 1).toFloat().coerceAtMost(visibleHeight)
        } else {
            visibleHeight
        }
        Log.d("DiagonalReading", "Animation: Width=$width, Height=$animationHeight, Lines=$totalLines")

        // В демо guideView = DiagonalLineView, не скрываем его
        if (guideView !is DiagonalLineView) {
            guideView.visibility = View.INVISIBLE
        }

        val initialLine = highlightWordAtPosition(textView, 0f, 0f, -1)

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration
            interpolator = LinearInterpolator()
            var lastLine = initialLine

            addUpdateListener { animation ->
                if (!isAnimationActive) return@addUpdateListener
                val fraction = animation.animatedValue as Float
                val y = fraction * animationHeight
                val x = fraction * width

                val currentLine = highlightWordAtPosition(textView, x, y, lastLine)
                if (currentLine != -1) lastLine = currentLine
            }
            addListener(
                onEnd = {
                    if (!isAnimationActive) return@addListener
                    Log.d("DiagonalReading", "Animation ended, advancing to position: $newPosition")
                    clearHighlight(textView)
                    guideView.visibility = View.INVISIBLE
                    currentPosition = newPosition
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }
            )
            start()
        }
    }

    private fun highlightWordAtPosition(textView: TextView, x: Float, y: Float, lastLine: Int): Int {
        if (!isAnimationActive) return -1

        val layout = textView.layout ?: return -1
        val visibleHeight = textView.height.toFloat()

        val adjustedY = y.coerceIn(0f, visibleHeight)
        val currentLine = layout.getLineForVertical(adjustedY.toInt())

        val totalLines = layout.lineCount
        // Подсвечиваем даже последнюю строку
        if (currentLine >= totalLines) {
            return totalLines - 1
        }

        val diagonalSlope = visibleHeight / textView.width.toFloat()
        val expectedX = adjustedY / diagonalSlope

        var closestOffset = -1
        var minDistance = Float.MAX_VALUE

        for (offset in layout.getLineStart(currentLine) until layout.getLineEnd(currentLine)) {
            if (textView.text[offset].isWhitespace()) continue

            val charLeft = layout.getPrimaryHorizontal(offset)
            val charRight = if (offset + 1 < textView.text.length) layout.getPrimaryHorizontal(offset + 1) else charLeft
            val charX = (charLeft + charRight) / 2

            val distance = kotlin.math.abs(charX - expectedX)
            if (distance < minDistance) {
                minDistance = distance
                closestOffset = offset
            }
        }

        if (closestOffset != -1) {
            val text = textView.text.toString()
            var start = closestOffset
            var end = closestOffset

            while (start > 0 && !text[start - 1].isWhitespace()) start--
            while (end < text.length && !text[end].isWhitespace()) end++

            val spannable = SpannableString(text)
            val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
            for (span in existingSpans) {
                spannable.removeSpan(span)
            }
            spannable.setSpan(
                BackgroundColorSpan(Color.YELLOW),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.text = spannable
            Log.d("DiagonalReading", "Highlighted word: ${text.substring(start, end)}")
        }

        return currentLine
    }

    private fun clearHighlight(textView: TextView) {
        if (!isAnimationActive) return

        val text = textView.text.toString()
        val spannable = SpannableString(text)
        val existingSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in existingSpans) {
            spannable.removeSpan(span)
        }
        textView.text = spannable
    }

    override fun cancelAnimation() {
        isAnimationActive = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}