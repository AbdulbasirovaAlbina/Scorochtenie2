package com.example.scorochtenie2

import android.graphics.Color

object HighlightColorConfig {
    
    data class HighlightColor(
        val name: String,
        val lightBackgroundColor: Int,
        val darkBackgroundColor: Int,
        val textColor: Int = Color.BLACK // Черный текст по умолчанию для лучшей читаемости
    )
    
    val colors = listOf(
        HighlightColor(
            name = "Желтый",
            lightBackgroundColor = Color.YELLOW,
            darkBackgroundColor = Color.parseColor("#FFD700"), // Более яркий желтый для темной темы
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Зеленый",
            lightBackgroundColor = Color.parseColor("#90EE90"), // Светло-зеленый
            darkBackgroundColor = Color.parseColor("#32CD32"), // Лайм зеленый
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Голубой",
            lightBackgroundColor = Color.parseColor("#87CEEB"), // Небесно-голубой
            darkBackgroundColor = Color.parseColor("#00BFFF"), // Ярко-голубой
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Розовый",
            lightBackgroundColor = Color.parseColor("#FFB6C1"), // Светло-розовый
            darkBackgroundColor = Color.parseColor("#FF69B4"), // Ярко-розовый
            textColor = Color.BLACK
        ),
        HighlightColor(
            name = "Оранжевый",
            lightBackgroundColor = Color.parseColor("#FFA500"), // Оранжевый
            darkBackgroundColor = Color.parseColor("#FF8C00"), // Темно-оранжевый
            textColor = Color.BLACK
        )
    )
    
    val colorLabels = colors.map { it.name }
    
    fun getBackgroundColor(colorIndex: Int, isDarkTheme: Boolean): Int {
        if (colorIndex < 0 || colorIndex >= colors.size) return colors[0].lightBackgroundColor
        
        return if (isDarkTheme) {
            colors[colorIndex].darkBackgroundColor
        } else {
            colors[colorIndex].lightBackgroundColor
        }
    }
    
    fun getTextColor(colorIndex: Int): Int {
        if (colorIndex < 0 || colorIndex >= colors.size) return colors[0].textColor
        return colors[colorIndex].textColor
    }
    
    fun getColorIndex(colorName: String): Int {
        return colors.indexOfFirst { it.name == colorName }.takeIf { it != -1 } ?: 0
    }
}
