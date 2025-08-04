package com.example.scorochtenie2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LearningFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_learning, container, false)

        val techniques = listOf(
            TechniqueItem("Чтение блоками", R.drawable.ic_practice),
            TechniqueItem("Чтение по диагонали", R.drawable.ic_diagonal),
            TechniqueItem("Метод указки", R.drawable.ic_settings),
            TechniqueItem("Предложения наоборот", R.drawable.ic_progress),
            TechniqueItem("Слова наоборот", R.drawable.ic_learning),
            TechniqueItem("Текст за шторкой", R.drawable.ic_home),
            TechniqueItem("Зашумленный текст", R.drawable.ic_practice),
            TechniqueItem("Частично скрытые строки", R.drawable.ic_diagonal)
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = TechniqueAdapter(techniques)

        return view
    }
}