package com.example.scorochtenie2

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class PracticeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        // Setup back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val techniques = listOf(
            TechniqueItem("Чтение блоками", R.drawable.ic_block_reading),
            TechniqueItem("Чтение по диагонали", R.drawable.ic_diagonal_reading),
            TechniqueItem("Метод указки", R.drawable.ic_pointer_method),
            TechniqueItem("Предложения наоборот", R.drawable.ic_sentence_reverse),
            TechniqueItem("Слова наоборот", R.drawable.ic_word_reverse),
            TechniqueItem("Зашумленный текст", R.drawable.ic_noisy_text),
            TechniqueItem("Частично скрытые строки", R.drawable.ic_partially_hidden_lines)
        )

        // Добавляем тестовые данные для демонстрации (можно убрать в продакшене)
        addTestDataIfEmpty()

        // Настройка сетки техник
        val recyclerView = findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = TechniqueAdapter(techniques)
    }
    
    private fun addTestDataIfEmpty() {
        val existingResults = TestResultManager.getTestResults(this)
        if (existingResults.isEmpty()) {
            // Добавляем тестовые данные за последние 7 дней
            val calendar = Calendar.getInstance()
            val techniques = listOf(
                "Чтение блоками",
                "Чтение по диагонали",
                "Метод указки",
                "Предложения наоборот",
                "Слова наоборот",
                "Зашумленный текст",
                "Частично скрытые строки"
            )
            
            for (i in 6 downTo 0) {
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                val targetDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, i)
                
                // Добавляем 2-3 результата для каждого дня
                repeat((2..3).random()) {
                    val technique = techniques.random()
                    val comprehension = (60..95).random()
                    val readingTime = (30..180).random()
                    
                    TestResultManager.saveTestResult(
                        this,
                        technique,
                        comprehension,
                        readingTime
                    )
                }
            }
        }
    }
}

data class TechniqueItem(val title: String, val iconResId: Int)

