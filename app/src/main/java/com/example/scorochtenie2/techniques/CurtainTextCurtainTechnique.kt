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

class CurtainTextCurtainTechnique : Technique("Зашумленный текст", "Зашумленный текст") {

    private var isRunning = false
    private var currentWordIndex = 0
    private var fullText: String = ""
    private var currentWords: List<String> = emptyList()
    private var animator: ValueAnimator? = null
    private var scrollView: ScrollView? = null
    private var lastScrollY: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    

    override fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    ) {
        isRunning = true
        currentWordIndex = 0

        fullText = if (selectedTextIndex == -1) {
            TextResources.getDemoTextForTechnique(displayName)
        } else {
            TextResources.getTexts()[displayName]?.getOrNull(selectedTextIndex)?.text ?: ""
        }.replace("\n", " ")
        if (fullText.isEmpty()) {
            textView.text = "Текст недоступен"
            onAnimationEnd()
            return
        }

        currentWords = fullText.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        textView.gravity = android.view.Gravity.TOP
        textView.isSingleLine = false
        textView.maxLines = Int.MAX_VALUE
        textView.text = fullText

        scrollView = textView.parent as? ScrollView
        lastScrollY = 0

        if (guideView is CurtainOverlayView) {
            guideView.visibility = View.VISIBLE

            startWordHighlighting(textView, durationPerWord, onAnimationEnd)

            guideView.start(durationPerWord, currentWords.size) {

            }
        } else {

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
            textView.text = fullText
            if (isRunning) {
                isRunning = false
                onAnimationEnd()
            }
            return
        }


        val layout = textView.layout
        if (layout == null) {

            handler.postDelayed({
                if (isRunning) highlightNextWord(textView, wordDurationMs, onAnimationEnd)
            }, 200)
            return
        }


        highlightCurrentWord(textView)


        scrollToCurrentWord(textView, wordDurationMs)


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
        HighlightColorConfig.clearHighlights(spannable)


        var startIndex = 0
        var wordCount = 0

        currentWords.forEach { word ->
            if (wordCount == currentWordIndex) {
                val endIndex = startIndex + word.length
                if (startIndex < fullText.length && endIndex <= fullText.length) {
                    HighlightColorConfig.applyHighlight(
                        textView.context,
                        spannable,
                        startIndex,
                        endIndex
                    )
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