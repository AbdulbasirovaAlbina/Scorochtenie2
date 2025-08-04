package com.example.scorochtenie2

object SpeedConfig {
    val speedLabels = arrayOf("Медленно", "Средне", "Быстро")

    fun getDurationPerWord(speedIndex: Int): Long {
        return when (speedIndex) {
            0 -> 200L // Медленно
            1 -> 400L // Средне
            2 -> 600L // Быстро
            else -> 400L
        }
    }
}