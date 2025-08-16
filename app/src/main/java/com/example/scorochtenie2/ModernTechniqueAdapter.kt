package com.example.scorochtenie2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ModernTechniqueItem(
    val title: String,
    val iconResId: Int,
    val progress: Int = 0 // Не используется, оставлено для совместимости
)

class ModernTechniqueAdapter(private val techniques: List<ModernTechniqueItem>) :
    RecyclerView.Adapter<ModernTechniqueAdapter.ModernTechniqueViewHolder>() {

    private lateinit var context: Context

    class ModernTechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val title: TextView = itemView.findViewById(R.id.technique_title)
        val progressText: TextView = itemView.findViewById(R.id.progress_text)
        val progressBar: View = itemView.findViewById(R.id.technique_progress_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModernTechniqueViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_technique_modern, parent, false)
        return ModernTechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModernTechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.icon.setImageResource(technique.iconResId)

        // Разбиваем название на слова
        val words = technique.title.split(" ")
        // Если ровно два слова, добавляем перенос строки, иначе оставляем как есть
        holder.title.text = if (words.size == 2) {
            words.joinToString("\n")
        } else {
            technique.title
        }

        // Получаем статистику для техники
        val stats = TestResultManager.getTechniqueStats(context, technique.title)
        val completedTexts = stats.uniqueTextsCount // Количество уникальных прочитанных текстов
        val totalTexts = 9 // У каждой техники теперь 9 текстов
        
        // Показываем прогресс: количество пройденных текстов из 9
        holder.progressText.text = "$completedTexts/$totalTexts"
        
        // Обновляем шкалу прогресса (максимум 9 текстов)
        val progressPercent = (completedTexts.toFloat() / totalTexts).coerceAtMost(1.0f)
        holder.progressBar.post {
            val width = (holder.progressBar.parent as View).width
            val progressWidth = (width * progressPercent).toInt()
            holder.progressBar.layoutParams.width = progressWidth
            holder.progressBar.requestLayout()
        }

        holder.itemView.setOnClickListener {
            // Проверяем, завершена ли техника полностью (9/9)
            if (TestResultManager.isTechniqueFullyCompleted(context, technique.title)) {
                // Техника полностью завершена - показываем тост
                android.widget.Toast.makeText(
                    context,
                    "🎉 Техника \"${technique.title}\" полностью освоена! Все тексты пройдены на 100%",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                // Техника не завершена - переходим в настройки
                val intent = Intent(context, TechniqueSettingsActivity::class.java)
                intent.putExtra("technique_name", technique.title)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = techniques.size
}