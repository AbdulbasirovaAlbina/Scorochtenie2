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
    private val totalTexts = 3 // Общее количество текстов для каждой техники

    class ModernTechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val title: TextView = itemView.findViewById(R.id.technique_title)
        val progressText: TextView = itemView.findViewById(R.id.progress_text)
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
        val completedTests = stats.usesCount
        holder.progressText.text = "$completedTests/$totalTexts"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, TechniqueSettingsActivity::class.java)
            intent.putExtra("technique_name", technique.title)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = techniques.size
}