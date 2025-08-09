package com.example.scorochtenie2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProgressFragment : Fragment() {

    private lateinit var techniqueSelector: RecyclerView
    private lateinit var progressContainer: ViewGroup
    private lateinit var techniqueNameText: TextView
    private lateinit var usesCountText: TextView
    private lateinit var avgComprehensionText: TextView
    private lateinit var comprehensionRateText: TextView
    private lateinit var totalTimeText: TextView
    private lateinit var avgTimeText: TextView
    private lateinit var graphView: ProgressGraphView
    private lateinit var techniqueSelectorAdapter: TechniqueSelectorAdapter

    private val techniques = listOf(
        TechniqueItem("Все техники", R.drawable.ic_progress),
        TechniqueItem("Чтение блоками", R.drawable.ic_block_reading),
        TechniqueItem("Чтение по диагонали", R.drawable.ic_diagonal_reading),
        TechniqueItem("Метод указки", R.drawable.ic_pointer_method),
        TechniqueItem("Предложения наоборот", R.drawable.ic_sentence_reverse),
        TechniqueItem("Слова наоборот", R.drawable.ic_word_reverse),
        TechniqueItem("Зашумленный текст", R.drawable.ic_noisy_text),
        TechniqueItem("Частично скрытые строки", R.drawable.ic_partially_hidden_lines)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)
        
        initViews(view)
        setupTechniqueSelector()
        loadTechniqueProgress("Все техники")
        
        return view
    }

    private fun initViews(view: View) {
        techniqueSelector = view.findViewById(R.id.technique_selector)
        progressContainer = view.findViewById(R.id.progress_container)
        
        // Находим элементы статистики в item_technique_progress
        val progressItemView = layoutInflater.inflate(R.layout.item_technique_progress, progressContainer, false)
        techniqueNameText = progressItemView.findViewById(R.id.technique_name)
        usesCountText = progressItemView.findViewById(R.id.uses_count)
        avgComprehensionText = progressItemView.findViewById(R.id.avg_comprehension)
        comprehensionRateText = progressItemView.findViewById(R.id.comprehension_rate)
        totalTimeText = progressItemView.findViewById(R.id.total_time)
        avgTimeText = progressItemView.findViewById(R.id.avg_time)
        graphView = progressItemView.findViewById(R.id.graph_container)
        
        // Добавляем элемент статистики в контейнер
        progressContainer.addView(progressItemView)
    }

    private fun setupTechniqueSelector() {
        techniqueSelectorAdapter = TechniqueSelectorAdapter(techniques) { technique ->
            loadTechniqueProgress(technique.title)
        }
        
        techniqueSelector.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        techniqueSelector.adapter = techniqueSelectorAdapter
    }

    private fun loadTechniqueProgress(techniqueName: String) {
        val stats = if (techniqueName == "Все техники") {
            TestResultManager.getAllTechniquesStats(requireContext())
        } else {
            TestResultManager.getTechniqueStats(requireContext(), techniqueName)
        }
        
        updateProgressDisplay(techniqueName, stats)
    }

    private fun updateProgressDisplay(techniqueName: String, stats: TechniqueStats) {
        techniqueNameText.text = techniqueName
        usesCountText.text = stats.usesCount.toString()
        avgComprehensionText.text = "${stats.avgComprehension}%"
        comprehensionRateText.text = "${stats.avgComprehension}%"
        
        // Отображаем время чтения в удобном формате
        totalTimeText.text = formatTime(stats.totalReadingTimeSeconds)
        avgTimeText.text = formatTime(stats.avgReadingTimeSeconds)
        
        // Обновляем график с реальными данными по дням
        graphView.setData(stats.dailyComprehension)
    }
    
    private fun formatTime(seconds: Int): String {
        return when {
            seconds < 60 -> "${seconds} сек"
            seconds < 3600 -> "${seconds / 60} мин ${seconds % 60} сек"
            else -> "${seconds / 3600} ч ${(seconds % 3600) / 60} мин"
        }
    }
} 