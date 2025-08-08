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
            TechniqueItem("Чтение блоками", R.drawable.ic_practice),
            TechniqueItem("Чтение по диагонали", R.drawable.ic_diagonal),
            TechniqueItem("Метод указки", R.drawable.ic_settings),
            TechniqueItem("Предложения наоборот", R.drawable.ic_progress),
            TechniqueItem("Слова наоборот", R.drawable.ic_learning),
            TechniqueItem("Зашумленный текст", R.drawable.ic_home),

            TechniqueItem("Частично скрытые строки", R.drawable.ic_diagonal)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = TechniqueAdapter(techniques)
    }
}
