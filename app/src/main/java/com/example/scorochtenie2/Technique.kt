package com.example.scorochtenie2

import android.text.SpannableString
import android.view.View
import android.widget.TextView

abstract class Technique(val name: String, val displayName: String) {
    abstract val description: SpannableString
    abstract fun startAnimation(
        textView: TextView,
        guideView: View,
        durationPerWord: Long,
        selectedTextIndex: Int,
        onAnimationEnd: () -> Unit
    )
    open fun cancelAnimation() {}
}