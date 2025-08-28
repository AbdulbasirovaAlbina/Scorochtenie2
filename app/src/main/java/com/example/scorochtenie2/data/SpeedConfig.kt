package com.example.scorochtenie2

object SpeedConfig {
    // Отображаемые ярлыки скоростей (индексы 0..2)
    val speedLabels = arrayOf("Медленно", "Средне", "Быстро")

    // Базовые значения (слов в минуту) на случай отсутствия индивидуальной настройки
    private val defaultSpeedsWpm = longArrayOf(200L, 400L, 600L)

    // Индивидуальные скорости по техникам (слов в минуту) для каждого уровня скорости
    // Изменяйте значения здесь — это единственная точка конфигурации
    private val techniqueSpeedsWpm: Map<String, LongArray> = mapOf(
        "Чтение по диагонали" to longArrayOf(350L, 280L, 180L),
        "Чтение блоками" to longArrayOf(200L, 150L, 80L),
        "Предложения наоборот" to longArrayOf(500L, 350L, 250L),
        "Слова наоборот" to longArrayOf(20000L, 12000L, 5000L),
        "Метод указки" to longArrayOf(350L, 280L, 180L),
        "Частично скрытые строки" to longArrayOf(350L, 280L, 180L),
        "Зашумленный текст" to longArrayOf(350L, 280L, 180L)
    )

    // Старый метод – оставлен для обратной совместимости (использует дефолтные значения)
    fun getDurationPerWord(speedIndex: Int): Long {
        return defaultSpeedsWpm.getOrElse(speedIndex.coerceIn(0, defaultSpeedsWpm.lastIndex)) { defaultSpeedsWpm[1] }
    }

    // Новый метод: получить скорость (слов в минуту) с учётом техники и выбранного уровня
    fun getWpmForTechnique(techniqueName: String, speedIndex: Int): Long {
        val speeds = techniqueSpeedsWpm[techniqueName] ?: defaultSpeedsWpm
        val idx = speedIndex.coerceIn(0, speeds.lastIndex)
        return speeds[idx]
    }
}