package com.example.scorochtenie2

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        val recyclerView = findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = TechniqueAdapter(techniques)
    }
}

data class TechniqueItem(val title: String, val iconResId: Int)

