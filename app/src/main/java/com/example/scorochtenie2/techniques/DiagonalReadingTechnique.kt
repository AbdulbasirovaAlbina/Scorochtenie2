package com.example.scorochtenie2

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
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

    

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        try {
            this.selectedTextIndex = selectedTextIndex
            fullText = if (selectedTextIndex == -1) {
                TextResources.getDemoTextForTechnique(displayName)
            } else {
                TextResources.getTexts()["Чтение по диагонали"]?.getOrNull(selectedTextIndex)?.text ?: ""
            }.replace("\n", " ")
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
            guideView.visibility = View.VISIBLE

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

        if (currentPosition >= fullText.length) {

            guideView.visibility = View.INVISIBLE
            animator?.cancel()
            clearHighlight(textView)
            if (isAnimationActive) onAnimationEnd()
            return
        }

        if (currentPosition < 0) {

            currentPosition = 0
        }

        textView.text = ""
        clearHighlight(textView)
        val remainingText = fullText.substring(currentPosition)
        textView.text = remainingText

        textView.post {
            if (!isAnimationActive) return@post

            val layout = textView.layout
            if (layout == null) {

                handler.postDelayed({
                    showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                }, 50)
                return@post
            }

            val visibleHeight = (textView.height - textView.totalPaddingTop - textView.totalPaddingBottom).toFloat()
            val lineCount = layout.lineCount
            val contentHeight = if (lineCount > 0) layout.getLineBottom(lineCount - 1).toFloat() else 0f

            if (contentHeight <= visibleHeight + 1f) {
                val partText = fullText.substring(currentPosition).trim()

                textView.text = partText
                textView.visibility = View.VISIBLE
                val parent = textView.parent as View
                val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
                if (diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                    diagonalLineView.requestLayout()
                    diagonalLineView.invalidate()
                    startDiagonalAnimation(textView, guideView, fullText.length, partText, wordDurationMs, onAnimationEnd)
                } else {

                    if (isAnimationActive) onAnimationEnd()
                }
                return@post
            }

            var lastVisibleLine = -1
            for (i in 0 until lineCount) {
                val lineBottom = layout.getLineBottom(i)
                if (lineBottom <= visibleHeight) {
                    lastVisibleLine = i
                } else {
                    break
                }
            }

            if (lastVisibleLine < 0) lastVisibleLine = 0


            var breakPosition = if (lineCount > 0) {
                val remainingLength = remainingText.length
                val nextLineIndex = (lastVisibleLine + 1).coerceAtMost(lineCount - 1)
                val startOfNextLine = layout.getLineStart(nextLineIndex)
                startOfNextLine.coerceIn(0, remainingLength)
            } else {
                fullText.length - currentPosition
            }

            breakPosition = (currentPosition + breakPosition).coerceAtMost(fullText.length)
            if (breakPosition <= currentPosition) {

                breakPosition = fullText.length
            }

            val partText = fullText.substring(currentPosition, breakPosition)
            if (partText.isEmpty()) {

                currentPosition = breakPosition
                showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
                return@post
            }


            textView.text = partText
            textView.visibility = View.VISIBLE

            handler.post {
                if (!isAnimationActive) return@post
                val parent = textView.parent as View
                val diagonalLineView = parent.findViewById<DiagonalLineView>(R.id.diagonal_line_view)
                if (diagonalLineView != null) {
                    diagonalLineView.visibility = View.VISIBLE
                    diagonalLineView.requestLayout()
                    diagonalLineView.invalidate()

                    startDiagonalAnimation(textView, guideView, breakPosition, partText, wordDurationMs, onAnimationEnd)
                } else {

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

            handler.postDelayed({
                if (isAnimationActive) startDiagonalAnimation(textView, guideView, newPosition, partText, wordDurationMs, onAnimationEnd)
            }, 50)
            return
        }

        val width = textView.width.toFloat()
        val visibleHeight = textView.height.toFloat()
        val totalLines = layout.lineCount

        val animationHeight = if (totalLines > 1) {
            layout.getLineTop(totalLines - 1).toFloat().coerceAtMost(visibleHeight)
        } else {
            visibleHeight
        }


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

        if (currentLine == totalLines - 1 || currentLine <= lastLine) {
            return currentLine
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
            HighlightColorConfig.applyHighlight(
                textView.context,
                spannable,
                start,
                end
            )
            textView.text = spannable

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