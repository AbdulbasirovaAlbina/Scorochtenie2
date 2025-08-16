package com.example.scorochtenie2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class TechniqueSelectorAdapter(
    private val techniques: List<TechniqueItem>,
    private val onTechniqueSelected: (TechniqueItem) -> Unit
) : RecyclerView.Adapter<TechniqueSelectorAdapter.TechniqueViewHolder>() {

    private var selectedPosition = 0

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val iconContainer: View = itemView.findViewById(R.id.technique_icon_container)
        val card: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technique_selector, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.icon.setImageResource(technique.iconResId)

        // Специальная обработка для иконки "Все техники"
        if (technique.title == "Все техники") {
            holder.icon.setColorFilter(android.graphics.Color.WHITE)
            // Устанавливаем специальный background для иконки "Все техники"
            holder.iconContainer.background = holder.itemView.context.getDrawable(R.drawable.technique_icon_all_techniques_bg)
        } else {
            holder.icon.clearColorFilter()
            holder.iconContainer.background = holder.itemView.context.getDrawable(R.drawable.technique_icon_modern_bg)
        }

        // Обновляем внешний вид в зависимости от выбора
        holder.card.isSelected = (position == selectedPosition)
        if (position == selectedPosition) {
            holder.card.elevation = 8f
        } else {
            holder.card.elevation = 4f
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onTechniqueSelected(technique)
        }
    }

    override fun getItemCount(): Int = techniques.size

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
} 