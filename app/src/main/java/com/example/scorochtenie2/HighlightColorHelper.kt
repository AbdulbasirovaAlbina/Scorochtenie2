package com.example.scorochtenie2

import android.content.Context
import android.content.res.Configuration
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan

object HighlightColorHelper {
    
    /**
     * Получает цвет фона для выделения на основе настроек пользователя и темы
     */
    fun getHighlightBackgroundColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("technique_settings", Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt("highlight_color_index", 0)
        
        val isDarkTheme = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        
        return HighlightColorConfig.getBackgroundColor(colorIndex, isDarkTheme)
    }
    
    /**
     * Получает цвет текста для выделения на основе настроек пользователя
     */
    fun getHighlightTextColor(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("technique_settings", Context.MODE_PRIVATE)
        val colorIndex = sharedPreferences.getInt("highlight_color_index", 0)
        
        return HighlightColorConfig.getTextColor(colorIndex)
    }
    
    /**
     * Применяет выделение к spannable тексту с учетом настроек пользователя
     */
    fun applyHighlight(
        context: Context,
        spannable: android.text.SpannableString,
        startIndex: Int,
        endIndex: Int
    ) {
        // Удаляем старые spans в указанном диапазоне
        val existingBackgroundSpans = spannable.getSpans(startIndex, endIndex, BackgroundColorSpan::class.java)
        for (span in existingBackgroundSpans) {
            spannable.removeSpan(span)
        }
        val existingForegroundSpans = spannable.getSpans(startIndex, endIndex, ForegroundColorSpan::class.java)
        for (span in existingForegroundSpans) {
            spannable.removeSpan(span)
        }
        
        // Применяем новое выделение
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
    
    /**
     * Удаляет все выделения из spannable текста
     */
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
