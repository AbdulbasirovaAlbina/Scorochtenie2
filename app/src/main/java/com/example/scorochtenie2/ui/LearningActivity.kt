package com.example.scorochtenie2

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        // Setup back button
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        val techniques = listOf(
            ModernTechniqueItem(title = "Чтение блоками", iconResId = R.drawable.ic_block_reading),
            ModernTechniqueItem(title = "Чтение по диагонали", iconResId = R.drawable.ic_diagonal_reading),
            ModernTechniqueItem(title = "Метод указки", iconResId = R.drawable.ic_pointer_method),
            ModernTechniqueItem(title = "Предложения наоборот", iconResId = R.drawable.ic_sentence_reverse),
            ModernTechniqueItem(title = "Слова наоборот", iconResId = R.drawable.ic_word_reverse),
            ModernTechniqueItem(title = "Зашумленный текст", iconResId = R.drawable.ic_noisy_text),
            ModernTechniqueItem(title = "Частично скрытые строки", iconResId = R.drawable.ic_partially_hidden_lines)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = ModernTechniqueAdapter(techniques)
    }
}
