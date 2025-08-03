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
            Technique("Чтение блоками", R.drawable.ic_practice),
            Technique("Чтение по диагонали", R.drawable.ic_diagonal),
            Technique("Метод указки", R.drawable.ic_settings),
            Technique("Предложения наоборот", R.drawable.ic_progress),
            Technique("Слова наоборот", R.drawable.ic_learning),
            Technique("Текст за шторкой", R.drawable.ic_home),
            Technique("Зашумленный текст", R.drawable.ic_practice),
            Technique("Частично скрытые строки", R.drawable.ic_diagonal)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = TechniqueAdapter(techniques)
    }
}

data class Technique(val title: String, val iconResId: Int)

