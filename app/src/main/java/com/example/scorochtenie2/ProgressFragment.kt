package com.example.scorochtenie2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject

class ProgressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProgressData()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на вкладку
        loadProgressData()
    }

    private fun loadProgressData() {
        val view = view ?: return
        val container = view.findViewById<LinearLayout>(R.id.progress_container)
        
        // Очищаем контейнер
        container.removeAllViews()
        
        val sharedPreferences = requireContext().getSharedPreferences("TestResults", Context.MODE_PRIVATE)
        val allResults = mutableListOf<TechniqueResult>()
        
        // Получаем все результаты тестов (ключи вида result_<techniqueName>_<timestamp>)
        val allKeys = sharedPreferences.all.keys.filter { it.startsWith("result_") }
        
        if (allKeys.isEmpty()) {
            // Нет данных - показываем сообщение
            val noDataView = layoutInflater.inflate(R.layout.item_no_progress, container, false)
            container.addView(noDataView)
            return
        }
        
        // Парсим результаты
        allKeys.forEach { key ->
            // Извлекаем название техники (между result_ и последним _timestamp)
            val techniqueName = key.removePrefix("result_").substringBeforeLast("_")
            val resultJson = sharedPreferences.getString(key, null)
            
            if (resultJson != null) {
                try {
                    val jsonObject = JSONObject(resultJson)
                    val result = TechniqueResult(
                        techniqueName = techniqueName,
                        score = jsonObject.getInt("score"),
                        totalQuestions = jsonObject.getInt("totalQuestions"),
                        durationPerWord = jsonObject.getLong("durationPerWord"),
                        timestamp = jsonObject.getLong("timestamp")
                    )
                    allResults.add(result)
                } catch (e: Exception) {
                    // Игнорируем некорректные данные
                }
            }
        }
        
        // Группируем результаты по техникам
        val techniqueGroups = allResults.groupBy { it.techniqueName }
        
        // Создаем карточки для каждой техники
        techniqueGroups.forEach { (techniqueName, results) ->
            val bestResult = results.maxByOrNull { it.score } ?: results.first()
            val techniqueCard = createTechniqueCard(techniqueName, results, bestResult)
            container.addView(techniqueCard)
        }
    }
    
    private fun createTechniqueCard(techniqueName: String, allResults: List<TechniqueResult>, bestResult: TechniqueResult): View {
        val cardView = layoutInflater.inflate(R.layout.item_technique_progress, null)
        
        // Заполняем данные
        cardView.findViewById<TextView>(R.id.technique_name).text = techniqueName
        
        // Статистика использования (количество тестов)
        cardView.findViewById<TextView>(R.id.uses_count).text = allResults.size.toString()
        
        // Среднее понимание (средний процент правильных ответов)
        val avgComprehension = allResults.map { 
            ((it.score.toFloat() / it.totalQuestions.toFloat()) * 100).toInt() 
        }.average().toInt()
        cardView.findViewById<TextView>(R.id.avg_comprehension).text = "$avgComprehension%"
        
        // Основной показатель понимания (лучший результат)
        val bestComprehension = ((bestResult.score.toFloat() / bestResult.totalQuestions.toFloat()) * 100).toInt()
        cardView.findViewById<TextView>(R.id.comprehension_rate).text = "$bestComprehension%"
        
        // Показываем улучшение
        val improvementText = cardView.findViewById<TextView>(R.id.improvement_text)
        if (allResults.size > 1) {
            val firstResult = allResults.minByOrNull { it.timestamp }
            val firstComprehension = firstResult?.let { 
                ((it.score.toFloat() / it.totalQuestions.toFloat()) * 100).toInt() 
            } ?: bestComprehension
            val improvement = bestComprehension - firstComprehension
            if (improvement > 0) {
                improvementText.text = "Все время +$improvement%"
                improvementText.setTextColor(resources.getColor(R.color.green_primary, null))
            } else if (improvement < 0) {
                improvementText.text = "Все время $improvement%"
                improvementText.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
            } else {
                improvementText.text = "Все время +0%"
                improvementText.setTextColor(resources.getColor(R.color.green_primary, null))
            }
        } else {
            improvementText.text = "Все время +0%"
            improvementText.setTextColor(resources.getColor(R.color.green_primary, null))
        }
        
        // График
        val graphView = cardView.findViewById<ProgressGraphView>(R.id.graph_container)
        if (allResults.size > 1) {
            // Сортируем результаты по времени и берем последние 7
            val sortedResults = allResults.sortedBy { it.timestamp }.takeLast(7)
            val graphData = sortedResults.map { 
                ((it.score.toFloat() / it.totalQuestions.toFloat()) * 100).toInt() 
            }
            graphView.setData(graphData)
        } else {
            // Если только один результат, показываем его как стабильную линию
            val comprehensionRate = ((bestResult.score.toFloat() / bestResult.totalQuestions.toFloat()) * 100).toInt()
            graphView.setData(List(7) { comprehensionRate })
        }
        
        return cardView
    }
    
    data class TechniqueResult(
        val techniqueName: String,
        val score: Int,
        val totalQuestions: Int,
        val durationPerWord: Long,
        val timestamp: Long
    )
} 