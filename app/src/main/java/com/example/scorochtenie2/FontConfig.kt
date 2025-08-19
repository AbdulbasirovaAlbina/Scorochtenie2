package com.example.scorochtenie2

object FontConfig {
    val fontSizeLabels = arrayOf("Маленький", "Средний", "Большой")

    fun getFontSizeMultiplier(fontSizeIndex: Int): Float {
        return when (fontSizeIndex) {
            0 -> 0.8f // Маленький
            1 -> 1.0f // Средний
            2 -> 1.1f // Большой
            else -> 1.0f
        }
    }

    const val BASE_TEXT_SIZE = 16f // Базовый размер шрифта в sp
}