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

class PartiallyHiddenLinesTechnique : Technique("Частично скрытые строки", "Частично скрытые строки") {
    private var currentWordIndex = 0
    private var selectedTextIndex = 0
    private var fullText: String = ""
    private var animator: ValueAnimator? = null
    private var currentPartWords: List<String> = emptyList()
    private var currentPartText: String = ""
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationActive = false
    private var hiddenLinesView: PartiallyHiddenLinesView? = null

    

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        this.selectedTextIndex = selectedTextIndex

        fullText = if (selectedTextIndex == -1) {
            TextResources.getDemoTextForTechnique(displayName)
        } else {
            TextResources.getTexts()[displayName]?.getOrNull(selectedTextIndex)?.text ?: ""
        }.replace("\n", " ")
        currentWordIndex = 0
        lastScrollY = 0
        isAnimationActive = true

        val safeDurationPerWord = if (durationPerWord <= 0) 400L else durationPerWord
        val wordDurationMs = (60_000 / safeDurationPerWord).coerceAtLeast(50L)

        scrollView = textView.parent as? ScrollView
        scrollView?.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            hiddenLinesView?.invalidate()
        }

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE


        if (guideView is PartiallyHiddenLinesView) {
            hiddenLinesView = guideView
            guideView.setTextView(textView)

        } else {

        }

        textView.requestLayout()


        handler.post {
            if (isAnimationActive) {

                showNextTextPart(textView, guideView, wordDurationMs, onAnimationEnd)
            } else {

            }
        }
    }

    private fun showNextTextPart(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) {

            return
        }

        currentPartText = fullText
        currentPartWords = currentPartText.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        currentWordIndex = 0

        textView.text = currentPartText

        if (isAnimationActive && guideView is PartiallyHiddenLinesView) {

            guideView.showMask()
            animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
        } else {

        }
    }

    private fun animateNextWord(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) {

            return
        }

        if (currentWordIndex >= currentPartWords.size) {
            animator?.cancel()
            textView.text = currentPartText
            if (isAnimationActive) onAnimationEnd()

            return
        }

        highlightWord(textView)
        startWordAnimation(textView, guideView, wordDurationMs, onAnimationEnd)
    }

    private fun highlightWord(textView: TextView) {
        if (!isAnimationActive) {

            return
        }

        val spannable = SpannableString(currentPartText)
        HighlightColorConfig.clearHighlights(spannable)

        var startIndex = 0
        var wordCount = 0

        currentPartWords.forEach { word ->
            if (wordCount == currentWordIndex) {
                val endIndex = startIndex + word.length
                HighlightColorConfig.applyHighlight(
                    textView.context,
                    spannable,
                    startIndex,
                    endIndex
                )
            }
            startIndex += word.length
            if (startIndex < currentPartText.length && currentPartText[startIndex] == ' ') {
                startIndex++
            }
            wordCount++
        }

        textView.text = spannable

    }

    private fun startWordAnimation(
        textView: TextView,
        guideView: View,
        wordDurationMs: Long,
        onAnimationEnd: () -> Unit
    ) {
        if (!isAnimationActive) {

            return
        }

        animator?.cancel()

        val layout = textView.layout
        if (layout == null) {

            handler.postDelayed({
                if (isAnimationActive) animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }

        val wordStartIndex = getWordStartIndex(currentWordIndex, currentPartText)
        val wordEndIndex = wordStartIndex + currentPartWords[currentWordIndex].length

        if (wordStartIndex < 0 || wordStartIndex >= currentPartText.length) {
            currentWordIndex++
            animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)

            return
        }

        val startLine = layout.getLineForOffset(wordStartIndex)
        val lineTopPosition = layout.getLineTop(startLine)
        val lineBottomPosition = layout.getLineBottom(startLine)


        scrollView?.let { sv ->
            handler.post {
                if (!isAnimationActive) return@post
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
                            start()
                        }
                        lastScrollY = targetScrollY

                    }
                }
            }
        }


        handler.postDelayed({
            if (isAnimationActive) {
                currentWordIndex++
                animateNextWord(textView, guideView, wordDurationMs, onAnimationEnd)
            }
        }, wordDurationMs)
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
        isAnimationActive = false
        animator?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}