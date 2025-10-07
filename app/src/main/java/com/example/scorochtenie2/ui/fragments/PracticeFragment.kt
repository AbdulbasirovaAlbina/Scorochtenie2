package com.example.scorochtenie2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class PracticeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_practice, container, false)

        val techniques = listOf(
            ModernTechniqueItem(
                title = "Чтение блоками",
                iconResId = R.drawable.ic_block_reading
            ),
            ModernTechniqueItem(
                title = "Чтение по диагонали",
                iconResId = R.drawable.ic_diagonal_reading
            ),
            ModernTechniqueItem(
                title = "Метод указки",
                iconResId = R.drawable.ic_pointer_method
            ),
            ModernTechniqueItem(
                title = "Предложения наоборот",
                iconResId = R.drawable.ic_sentence_reverse
            ),
            ModernTechniqueItem(
                title = "Слова наоборот",
                iconResId = R.drawable.ic_word_reverse
            ),
            ModernTechniqueItem(
                title = "Зашумленный текст",
                iconResId = R.drawable.ic_noisy_text
            ),
            ModernTechniqueItem(
                title = "Частично скрытые строки",
                iconResId = R.drawable.ic_partially_hidden_lines
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.techniques_grid)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ModernTechniqueAdapter(techniques)
        return view
    }
}