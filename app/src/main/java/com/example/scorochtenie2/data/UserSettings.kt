package com.example.scorochtenie2

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan


object FontConfig {
    val fontSizeLabels = arrayOf("Маленький", "Средний", "Большой")

    fun getFontSizeMultiplier(fontSizeIndex: Int): Float {
        return when (fontSizeIndex) {
            0 -> 0.8f
            1 -> 1.0f
            2 -> 1.1f
            else -> 1.0f
        }
    }

    const val BASE_TEXT_SIZE = 16f
}

object SpeedConfig {
    val speedLabels = arrayOf("Медленно", "Средне", "Быстро")

    private val defaultSpeedsWpm = longArrayOf(200L, 400L, 600L)

    private val techniqueSpeedsWpm: Map<String, LongArray> = mapOf(
        "Чтение по диагонали" to longArrayOf(350L, 280L, 180L),
        "Чтение блоками" to longArrayOf(200L, 150L, 80L),
        "Предложения наоборот" to longArrayOf(500L, 350L, 250L),
        "Слова наоборот" to longArrayOf(20000L, 12000L, 5000L),
        "Метод указки" to longArrayOf(350L, 280L, 180L),
        "Частично скрытые строки" to longArrayOf(350L, 280L, 180L),
        "Зашумленный текст" to longArrayOf(350L, 280L, 180L)
    )

    fun getDurationPerWord(speedIndex: Int): Long {
        return defaultSpeedsWpm.getOrElse(speedIndex.coerceIn(0, defaultSpeedsWpm.lastIndex)) { defaultSpeedsWpm[1] }
    }

    fun getWpmForTechnique(techniqueName: String, speedIndex: Int): Long {
        val speeds = techniqueSpeedsWpm[techniqueName] ?: defaultSpeedsWpm
        val idx = speedIndex.coerceIn(0, speeds.lastIndex)
        return speeds[idx]
    }
}

object HighlightColorConfig {
    data class HighlightColor(
        val name: String,
        val lightBackgroundColor: Int,
        val darkBackgroundColor: Int,
        val textColor: Int = Color.BLACK
    )

    val colors = listOf(
        HighlightColor(
            name = "Желтый",
            lightBackgroundColor = Color.YELLOW,
            darkBackgroundColor = Color.parseColor("#FFD700"),
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Зеленый",
            lightBackgroundColor = Color.parseColor("#90EE90"),
            darkBackgroundColor = Color.parseColor("#32CD32"),
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Голубой",
            lightBackgroundColor = Color.parseColor("#87CEEB"),
            darkBackgroundColor = Color.parseColor("#00BFFF"),
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Розовый",
            lightBackgroundColor = Color.parseColor("#FFB6C1"),
            darkBackgroundColor = Color.parseColor("#FF69B4"),
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Оранжевый",
            lightBackgroundColor = Color.parseColor("#FFA500"),
            darkBackgroundColor = Color.parseColor("#FF8C00"),
            textColor = Color.BLACK
        )
    )

    fun getBackgroundColor(colorIndex: Int, isDarkTheme: Boolean): Int {
        if (colorIndex < 0 || colorIndex >= colors.size) return colors[0].lightBackgroundColor
        return if (isDarkTheme) colors[colorIndex].darkBackgroundColor else colors[colorIndex].lightBackgroundColor
    }

    fun getTextColor(colorIndex: Int): Int {
        if (colorIndex < 0 || colorIndex >= colors.size) return colors[0].textColor
        return colors[colorIndex].textColor
    }

    fun getHighlightBackgroundColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("technique_settings", Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt("highlight_color_index", 0)
        val isDarkTheme = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        return getBackgroundColor(colorIndex, isDarkTheme)
    }

    fun getHighlightTextColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("technique_settings", Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt("highlight_color_index", 0)
        return getTextColor(colorIndex)
    }

    fun applyHighlight(
        context: Context,
        spannable: android.text.SpannableString,
        startIndex: Int,
        endIndex: Int
    ) {
        val existingBackgroundSpans = spannable.getSpans(startIndex, endIndex, BackgroundColorSpan::class.java)
        for (span in existingBackgroundSpans) {
            spannable.removeSpan(span)
        }
        val existingForegroundSpans = spannable.getSpans(startIndex, endIndex, ForegroundColorSpan::class.java)
        for (span in existingForegroundSpans) {
            spannable.removeSpan(span)
        }

        val backgroundColor = getHighlightBackgroundColor(context)
        val textColor = getHighlightTextColor(context)

        spannable.setSpan(
            BackgroundColorSpan(backgroundColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    fun clearHighlights(spannable: android.text.SpannableString) {
        val backgroundSpans = spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        for (span in backgroundSpans) {
            spannable.removeSpan(span)
        }
        val foregroundSpans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
        for (span in foregroundSpans) {
            spannable.removeSpan(span)
        }
    }
}


