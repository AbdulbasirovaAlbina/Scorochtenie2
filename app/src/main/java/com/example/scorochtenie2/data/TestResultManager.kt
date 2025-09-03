package com.example.scorochtenie2

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class TestResult(
    val techniqueName: String,
    val comprehension: Int,
    val readingTimeSeconds: Int,
    val timestamp: Long,
    val date: String,
    val textIndex: Int = 0
)

data class TechniqueStats(
    val usesCount: Int,
    val uniqueTextsCount: Int,
    val avgComprehension: Int,
    val totalReadingTimeSeconds: Int,
    val avgReadingTimeSeconds: Int,
    val dailyComprehension: List<Int>
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
        val results = gson.fromJson<List<TestResult>>(json, type) ?: emptyList()
        return results
    }

    fun getTechniqueResults(context: Context, techniqueName: String): List<TestResult> {
        val results = getTestResults(context).filter { it.techniqueName == techniqueName }
        return results
    }

    fun getAllTechniqueResults(context: Context, techniqueName: String): List<TestResult> {
        val results = getTestResults(context).filter { it.techniqueName == techniqueName }
        return results
    }

    fun getTechniqueStats(context: Context, techniqueName: String, startDate: Calendar? = null): TechniqueStats {
        val results = getTechniqueResults(context, techniqueName)
        val filteredResults = filterResultsByPeriod(results, startDate)

        if (filteredResults.isEmpty()) {
            return TechniqueStats(
                usesCount = 0,
                uniqueTextsCount = 0,
                avgComprehension = 0,
                totalReadingTimeSeconds = 0,
                avgReadingTimeSeconds = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = filteredResults.size
        val uniqueTextsCount = filteredResults.filter { it.comprehension == 100 }
            .map { it.textIndex }
            .distinct()
            .size
        val avgComprehension = filteredResults.map { it.comprehension }.average().toInt()
        val totalReadingTimeSeconds = filteredResults.sumOf { it.readingTimeSeconds }
        val avgReadingTimeSeconds = if (usesCount > 0) totalReadingTimeSeconds / usesCount else 0
        val dailyComprehension = getDailyComprehension(filteredResults, startDate)

        return TechniqueStats(
            usesCount = usesCount,
            uniqueTextsCount = uniqueTextsCount,
            avgComprehension = avgComprehension,
            totalReadingTimeSeconds = totalReadingTimeSeconds,
            avgReadingTimeSeconds = avgReadingTimeSeconds,
            dailyComprehension = dailyComprehension
        )
    }

    fun getAllTechniquesStats(context: Context, startDate: Calendar? = null): TechniqueStats {
        val allResults = getTestResults(context)
        val filteredResults = filterResultsByPeriod(allResults, startDate)

        if (filteredResults.isEmpty()) {
            return TechniqueStats(
                usesCount = 0,
                uniqueTextsCount = 0,
                avgComprehension = 0,
                totalReadingTimeSeconds = 0,
                avgReadingTimeSeconds = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = filteredResults.size
        val uniqueTextsCount = filteredResults.map { it.textIndex }.distinct().size
        val avgComprehension = filteredResults.map { it.comprehension }.average().toInt()
        val totalReadingTimeSeconds = filteredResults.sumOf { it.readingTimeSeconds }
        val avgReadingTimeSeconds = if (usesCount > 0) totalReadingTimeSeconds / usesCount else 0
        val dailyComprehension = getDailyComprehension(filteredResults, startDate)

        return TechniqueStats(
            usesCount = usesCount,
            uniqueTextsCount = uniqueTextsCount,
            avgComprehension = avgComprehension,
            totalReadingTimeSeconds = totalReadingTimeSeconds,
            avgReadingTimeSeconds = avgReadingTimeSeconds,
            dailyComprehension = dailyComprehension
        )
    }

    private fun filterResultsByPeriod(results: List<TestResult>, startDate: Calendar?): List<TestResult> {
        val calendar = Calendar.getInstance()
        val start: Long
        val end: Long

        if (startDate == null) {
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = when (currentDayOfWeek) {
                Calendar.SUNDAY -> 6
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 0
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            start = calendar.timeInMillis
            calendar.timeInMillis = System.currentTimeMillis()
            end = calendar.timeInMillis
        } else {
            calendar.timeInMillis = startDate.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            end = calendar.timeInMillis
        }

        val filtered = results.filter { it.timestamp in start..end }
        return filtered
    }

    private fun getDailyComprehension(results: List<TestResult>, startDate: Calendar?): List<Int> {
        val dailyComprehension = mutableListOf<Int>()
        val calendar = Calendar.getInstance()

        if (startDate == null) {
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = when (currentDayOfWeek) {
                Calendar.SUNDAY -> 6
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 0
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        } else {
            calendar.timeInMillis = startDate.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        for (i in 0..6) {
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.timeInMillis - 1
            val dayResults = results.filter { it.timestamp in dayStart..dayEnd }
            val dayAvg = if (dayResults.isNotEmpty()) {
                dayResults.map { it.comprehension }.average().toInt()
            } else {
                0
            }
            dailyComprehension.add(dayAvg)
        }

        return dailyComprehension
    }

    fun getCompletedTextsCount(context: Context, techniqueName: String): Int {
        val results = getAllTechniqueResults(context, techniqueName)
        val count = results.filter { it.comprehension == 100 }
            .map { it.textIndex }
            .distinct()
            .size
        return count
    }

    fun getCompletedTexts(context: Context, techniqueName: String): List<Int> {
        val results = getAllTechniqueResults(context, techniqueName)
        val completedTexts = results.filter { it.comprehension == 100 }
            .map { it.textIndex }
            .distinct()
        return completedTexts
    }

    fun isTextCompleted(context: Context, techniqueName: String, textIndex: Int): Boolean {
        val results = getAllTechniqueResults(context, techniqueName)
        val isCompleted = results.any { it.textIndex == textIndex && it.comprehension == 100 }
        return isCompleted
    }

    fun getAvailableTextsByLength(context: Context, techniqueName: String, textLength: String): List<Int> {
        val completedTexts = getCompletedTexts(context, techniqueName)
        val availableRange = when (textLength) {
            "Короткий" -> 0..2
            "Средний" -> 3..5
            "Длинный" -> 6..8
            else -> 3..5
        }
        val availableTexts = availableRange.filter { !completedTexts.contains(it) }
        return availableTexts
    }

    fun hasAvailableTexts(context: Context, techniqueName: String, textLength: String): Boolean {
        val hasAvailable = getAvailableTextsByLength(context, techniqueName, textLength).isNotEmpty()
        return hasAvailable
    }

    fun isTechniqueFullyCompleted(context: Context, techniqueName: String): Boolean {
        val isFullyCompleted = getCompletedTextsCount(context, techniqueName) >= 9
        return isFullyCompleted
    }

    fun clearAllProgress(context: Context) {
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