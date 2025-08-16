package com.example.scorochtenie2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class TestResult(
    val techniqueName: String,
    val comprehension: Int, // Процент понимания (0-100)
    val readingTimeSeconds: Int, // Время чтения в секундах
    val timestamp: Long,
    val date: String, // Дата в формате "yyyy-MM-dd"
    val textIndex: Int = 0 // Индекс прочитанного текста (0, 1, 2)
)

object TestResultManager {
    private const val PREF_NAME = "TestResults"
    private const val KEY_RESULTS = "test_results"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveTestResult(context: Context, techniqueName: String, comprehension: Int, readingTimeSeconds: Int = 0, textIndex: Int = 0) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val currentResults = getTestResults(context).toMutableList()
        val newResult = TestResult(
            techniqueName = techniqueName,
            comprehension = comprehension,
            readingTimeSeconds = readingTimeSeconds,
            timestamp = System.currentTimeMillis(),
            date = dateFormat.format(Date()),
            textIndex = textIndex
        )

        currentResults.add(newResult)

        val json = gson.toJson(currentResults)
        editor.putString(KEY_RESULTS, json)
        editor.apply()
    }

    fun getTestResults(context: Context): List<TestResult> {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(KEY_RESULTS, "[]")

        val type = object : TypeToken<List<TestResult>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getTechniqueResults(context: Context, techniqueName: String): List<TestResult> {
        return getTestResults(context).filter { it.techniqueName == techniqueName }
    }

    fun getTechniqueStats(context: Context, techniqueName: String): TechniqueStats {
        val results = getTechniqueResults(context, techniqueName)

        if (results.isEmpty()) {
            return TechniqueStats(
                usesCount = 0,
                uniqueTextsCount = 0,
                avgComprehension = 0,
                totalReadingTimeSeconds = 0,
                avgReadingTimeSeconds = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = results.size
        // Считаем только тексты с 100% результатом как завершенные
        val uniqueTextsCount = getCompletedTextsCount(context, techniqueName)
        val avgComprehension = results.map { it.comprehension }.average().toInt()
        val totalReadingTimeSeconds = results.sumOf { it.readingTimeSeconds }
        val avgReadingTimeSeconds = if (usesCount > 0) totalReadingTimeSeconds / usesCount else 0

        // Получаем данные за текущую неделю (с понедельника по воскресенье)
        val calendar = Calendar.getInstance()
        val dailyComprehension = mutableListOf<Int>()
        
        // Находим понедельник текущей недели
        // Пример: если сегодня среда (Calendar.WEDNESDAY = 4), то daysFromMonday = 2
        // Значит нужно отступить на 2 дня назад, чтобы попасть на понедельник
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = when (currentDayOfWeek) {
            Calendar.SUNDAY -> 6      // Воскресенье - отступаем на 6 дней назад
            Calendar.MONDAY -> 0      // Понедельник - не отступаем
            Calendar.TUESDAY -> 1     // Вторник - отступаем на 1 день назад
            Calendar.WEDNESDAY -> 2   // Среда - отступаем на 2 дня назад
            Calendar.THURSDAY -> 3    // Четверг - отступаем на 3 дня назад
            Calendar.FRIDAY -> 4      // Пятница - отступаем на 4 дня назад
            Calendar.SATURDAY -> 5    // Суббота - отступаем на 5 дней назад
            else -> 0
        }
        
        // Переходим к понедельнику текущей недели
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        
        // Получаем данные для каждого дня недели (пн-вс)
        for (i in 0..6) {
            val targetDate = dateFormat.format(calendar.time)
            val dayResults = results.filter { it.date == targetDate }
            val dayAvg = if (dayResults.isNotEmpty()) {
                dayResults.map { it.comprehension }.average().toInt()
            } else {
                0
            }
            dailyComprehension.add(dayAvg)
            
            // Переходим к следующему дню
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return TechniqueStats(
            usesCount = usesCount,
            uniqueTextsCount = uniqueTextsCount,
            avgComprehension = avgComprehension,
            totalReadingTimeSeconds = totalReadingTimeSeconds,
            avgReadingTimeSeconds = avgReadingTimeSeconds,
            dailyComprehension = dailyComprehension
        )
    }

    // Функция для получения количества завершенных текстов (100% результат)
    fun getCompletedTextsCount(context: Context, techniqueName: String): Int {
        val results = getTechniqueResults(context, techniqueName)
        // Считаем только тексты с 100% результатом
        return results.filter { it.comprehension == 100 }
            .map { it.textIndex }
            .distinct()
            .size
    }

    // Функция для получения списка завершенных текстов
    fun getCompletedTexts(context: Context, techniqueName: String): List<Int> {
        val results = getTechniqueResults(context, techniqueName)
        return results.filter { it.comprehension == 100 }
            .map { it.textIndex }
            .distinct()
    }

    // Функция для проверки, завершен ли конкретный текст
    fun isTextCompleted(context: Context, techniqueName: String, textIndex: Int): Boolean {
        val results = getTechniqueResults(context, techniqueName)
        return results.any { it.textIndex == textIndex && it.comprehension == 100 }
    }

    // Функция для получения доступных текстов по длине
    fun getAvailableTextsByLength(context: Context, techniqueName: String, textLength: String): List<Int> {
        val completedTexts = getCompletedTexts(context, techniqueName)
        
        val availableRange = when (textLength) {
            "Короткий" -> 0..2
            "Средний" -> 3..5
            "Длинный" -> 6..8
            else -> 3..5
        }
        
        return availableRange.filter { textIndex -> 
            !completedTexts.contains(textIndex) 
        }
    }

    // Функция для проверки, есть ли доступные тексты для данной длины
    fun hasAvailableTexts(context: Context, techniqueName: String, textLength: String): Boolean {
        return getAvailableTextsByLength(context, techniqueName, textLength).isNotEmpty()
    }

    // Функция для проверки полного завершения техники (9/9)
    fun isTechniqueFullyCompleted(context: Context, techniqueName: String): Boolean {
        val completedCount = getCompletedTextsCount(context, techniqueName)
        return completedCount >= 9
    }

    fun getAllTechniquesStats(context: Context): TechniqueStats {
        val allResults = getTestResults(context)

        if (allResults.isEmpty()) {
            return TechniqueStats(
                usesCount = 0,
                uniqueTextsCount = 0,
                avgComprehension = 0,
                totalReadingTimeSeconds = 0,
                avgReadingTimeSeconds = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = allResults.size
        val uniqueTextsCount = allResults.map { it.textIndex }.distinct().size // Количество уникальных текстов
        val avgComprehension = allResults.map { it.comprehension }.average().toInt()
        val totalReadingTimeSeconds = allResults.sumOf { it.readingTimeSeconds }
        val avgReadingTimeSeconds = if (usesCount > 0) totalReadingTimeSeconds / usesCount else 0

        // Получаем данные за текущую неделю (с понедельника по воскресенье) для всех техник
        val calendar = Calendar.getInstance()
        val dailyComprehension = mutableListOf<Int>()
        
        // Находим понедельник текущей недели
        // Пример: если сегодня среда (Calendar.WEDNESDAY = 4), то daysFromMonday = 2
        // Значит нужно отступить на 2 дня назад, чтобы попасть на понедельник
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = when (currentDayOfWeek) {
            Calendar.SUNDAY -> 6      // Воскресенье - отступаем на 6 дней назад
            Calendar.MONDAY -> 0      // Понедельник - не отступаем
            Calendar.TUESDAY -> 1     // Вторник - отступаем на 1 день назад
            Calendar.WEDNESDAY -> 2   // Среда - отступаем на 2 дня назад
            Calendar.THURSDAY -> 3    // Четверг - отступаем на 3 дня назад
            Calendar.FRIDAY -> 4      // Пятница - отступаем на 4 дня назад
            Calendar.SATURDAY -> 5    // Суббота - отступаем на 5 дней назад
            else -> 0
        }
        
        // Переходим к понедельнику текущей недели
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        
        // Получаем данные для каждого дня недели (пн-вс)
        for (i in 0..6) {
            val targetDate = dateFormat.format(calendar.time)
            val dayResults = allResults.filter { it.date == targetDate }
            val dayAvg = if (dayResults.isNotEmpty()) {
                dayResults.map { it.comprehension }.average().toInt()
            } else {
                0
            }
            dailyComprehension.add(dayAvg)
            
            // Переходим к следующему дню
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return TechniqueStats(
            usesCount = usesCount,
            uniqueTextsCount = uniqueTextsCount,
            avgComprehension = avgComprehension,
            totalReadingTimeSeconds = totalReadingTimeSeconds,
            avgReadingTimeSeconds = avgReadingTimeSeconds,
            dailyComprehension = dailyComprehension
        )
    }

    fun clearAllProgress(context: Context) {
        // Очищаем все результаты тестов
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        
        // Очищаем все связанные настройки прогресса
        val techniqueTimesPref = context.getSharedPreferences("TechniqueTimes", Context.MODE_PRIVATE)
        with(techniqueTimesPref.edit()) {
            clear()
            apply()
        }
        
        // Очищаем настройки техник (если есть)
        val techniqueSettingsPref = context.getSharedPreferences("TechniqueSettings", Context.MODE_PRIVATE)
        with(techniqueSettingsPref.edit()) {
            clear()
            apply()
        }
        
        // Очищаем статистику обучения (если есть)
        val learningStatsPref = context.getSharedPreferences("LearningStats", Context.MODE_PRIVATE)
        with(learningStatsPref.edit()) {
            clear()
            apply()
        }
        
        // Очищаем все другие возможные настройки прогресса
        val allPrefs = listOf(
            "TestResults",
            "TechniqueTimes", 
            "TechniqueSettings",
            "LearningStats",
            "ProgressData",
            "UserProgress"
        )
        
        allPrefs.forEach { prefName ->
            val pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            with(pref.edit()) {
                clear()
                apply()
            }
        }
    }
}

data class TechniqueStats(
    val usesCount: Int,
    val uniqueTextsCount: Int, // Количество уникальных прочитанных текстов
    val avgComprehension: Int,
    val totalReadingTimeSeconds: Int, // Общее время чтения в секундах
    val avgReadingTimeSeconds: Int,   // Среднее время чтения в секундах
    val dailyComprehension: List<Int>
)