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
    val timestamp: Long,
    val date: String // Дата в формате "yyyy-MM-dd"
)

object TestResultManager {
    private const val PREF_NAME = "TestResults"
    private const val KEY_RESULTS = "test_results"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveTestResult(context: Context, techniqueName: String, comprehension: Int) {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        
        val currentResults = getTestResults(context).toMutableList()
        val newResult = TestResult(
            techniqueName = techniqueName,
            comprehension = comprehension,
            timestamp = System.currentTimeMillis(),
            date = dateFormat.format(Date())
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
                avgComprehension = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = results.size
        val avgComprehension = results.map { it.comprehension }.average().toInt()
        
        // Получаем данные за последние 7 дней
        val calendar = Calendar.getInstance()
        val dailyComprehension = mutableListOf<Int>()
        
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val targetDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, i)
            
            val dayResults = results.filter { it.date == targetDate }
            val dayAvg = if (dayResults.isNotEmpty()) {
                dayResults.map { it.comprehension }.average().toInt()
            } else {
                0
            }
            dailyComprehension.add(dayAvg)
        }

        return TechniqueStats(
            usesCount = usesCount,
            avgComprehension = avgComprehension,
            dailyComprehension = dailyComprehension
        )
    }

    fun getAllTechniquesStats(context: Context): TechniqueStats {
        val allResults = getTestResults(context)
        
        if (allResults.isEmpty()) {
            return TechniqueStats(
                usesCount = 0,
                avgComprehension = 0,
                dailyComprehension = List(7) { 0 }
            )
        }

        val usesCount = allResults.size
        val avgComprehension = allResults.map { it.comprehension }.average().toInt()
        
        // Получаем данные за последние 7 дней для всех техник
        val calendar = Calendar.getInstance()
        val dailyComprehension = mutableListOf<Int>()
        
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val targetDate = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, i)
            
            val dayResults = allResults.filter { it.date == targetDate }
            val dayAvg = if (dayResults.isNotEmpty()) {
                dayResults.map { it.comprehension }.average().toInt()
            } else {
                0
            }
            dailyComprehension.add(dayAvg)
        }

        return TechniqueStats(
            usesCount = usesCount,
            avgComprehension = avgComprehension,
            dailyComprehension = dailyComprehension
        )
    }
}

data class TechniqueStats(
    val usesCount: Int,
    val avgComprehension: Int,
    val dailyComprehension: List<Int>
) 