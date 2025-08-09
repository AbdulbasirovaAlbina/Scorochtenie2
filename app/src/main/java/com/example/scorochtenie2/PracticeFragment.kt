package com.example.scorochtenie2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PracticeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_practice, container, false)

        val techniques = listOf(
            ModernTechniqueItem(
                "Чтение блоками",
                R.drawable.ic_block_reading
            ),
            ModernTechniqueItem(
                "Чтение по диагонали",
                R.drawable.ic_diagonal_reading
            ),
            ModernTechniqueItem(
                "Метод указки",
                R.drawable.ic_pointer_method
            ),
            ModernTechniqueItem(
                "Предложения наоборот",
                R.drawable.ic_sentence_reverse
            ),
            ModernTechniqueItem(
                "Слова наоборот",
                R.drawable.ic_word_reverse
            ),
            ModernTechniqueItem(
                "Зашумленный текст",
                R.drawable.ic_noisy_text
            ),
            ModernTechniqueItem(
                "Частично скрытые строки",
                R.drawable.ic_partially_hidden_lines
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ModernTechniqueAdapter(techniques)

        return view
    }
}

