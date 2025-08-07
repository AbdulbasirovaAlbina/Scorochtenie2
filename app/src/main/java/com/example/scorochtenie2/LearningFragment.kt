package com.example.scorochtenie2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LearningFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_learning, container, false)

        val learningTechniques = listOf(
            LearningTechniqueItem(
                title = "Чтение блоками",
                category = "Структурирование",
                description = "Метод чтения, при котором текст воспринимается целыми смысловыми блоками, а не отдельными словами. Это позволяет значительно увеличить скорость чтения и улучшить понимание контекста.",
                benefits = "• Увеличение скорости чтения в 2-3 раза\n• Лучшее понимание структуры текста\n• Снижение утомляемости глаз\n• Развитие периферического зрения",
                difficulty = 3,
                iconResId = R.drawable.ic_practice,
                practiceClass = BlockReadingActivity::class.java
            ),
            LearningTechniqueItem(
                title = "Чтение по диагонали",
                category = "Сканирование",
                description = "Техника быстрого просмотра текста по диагонали для выделения ключевых слов и фраз. Особенно эффективна для поиска конкретной информации в больших объемах текста.",
                benefits = "• Быстрый поиск нужной информации\n• Эффективное ознакомление с новым материалом\n• Экономия времени при работе с документами\n• Развитие навыков селективного внимания",
                difficulty = 2,
                iconResId = R.drawable.ic_diagonal,
                practiceClass = DiagonalReadingActivity::class.java
            ),
            LearningTechniqueItem(
                title = "Метод указки",
                category = "Концентрация",
                description = "Использование пальца, ручки или указки для ведения взгляда по тексту. Этот простой метод помогает концентрировать внимание и контролировать скорость чтения.",
                benefits = "• Улучшение концентрации внимания\n• Предотвращение регрессии взгляда\n• Контроль скорости чтения\n• Снижение отвлекаемости",
                difficulty = 1,
                iconResId = R.drawable.ic_settings,
                practiceClass = PointerMethodActivity::class.java
            ),
            LearningTechniqueItem(
                title = "Предложения наоборот",
                category = "Гибкость мышления",
                description = "Упражнение на чтение предложений в обратном порядке слов. Развивает гибкость мышления, улучшает понимание структуры языка и тренирует внимание к деталям.",
                benefits = "• Развитие когнитивной гибкости\n• Улучшение внимания к деталям\n• Тренировка рабочей памяти\n• Понимание структуры предложений",
                difficulty = 4,
                iconResId = R.drawable.ic_progress,
                practiceClass = SentenceReverseActivity::class.java
            ),
            LearningTechniqueItem(
                title = "Слова наоборот",
                category = "Распознавание",
                description = "Чтение слов, написанных в обратном порядке букв. Тренирует быстрое распознавание слов, развивает навыки анализа и улучшает концентрацию внимания.",
                benefits = "• Улучшение распознавания слов\n• Развитие аналитических способностей\n• Тренировка концентрации\n• Повышение скорости обработки информации",
                difficulty = 3,
                iconResId = R.drawable.ic_learning,
                practiceClass = WordReverseActivity::class.java
            ),
            LearningTechniqueItem(
                title = "Частично скрытые строки",
                category = "Периферическое зрение",
                description = "Техника чтения с частично скрытой нижней частью строк. Развивает способность распознавать слова по их верхней части, тренирует периферическое зрение и ускоряет процесс чтения.",
                benefits = "• Развитие периферического зрения\n• Ускорение распознавания слов\n• Тренировка концентрации внимания\n• Повышение скорости чтения",
                difficulty = 3,
                iconResId = R.drawable.ic_learning,
                practiceClass = PartiallyHiddenLinesActivity::class.java
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.techniques_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = LearningTechniqueAdapter(learningTechniques)

        return view
    }
}