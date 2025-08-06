package com.example.scorochtenie2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TechniqueSelectorAdapter(
    private val techniques: List<TechniqueItem>,
    private val onTechniqueSelected: (TechniqueItem) -> Unit
) : RecyclerView.Adapter<TechniqueSelectorAdapter.TechniqueViewHolder>() {

    private var selectedPosition = 0

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val name: TextView = itemView.findViewById(R.id.technique_name)
        val card: View = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technique_selector, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.name.text = technique.title
        holder.icon.setImageResource(technique.iconResId)

        // Обновляем внешний вид в зависимости от выбора
        if (position == selectedPosition) {
            holder.card.alpha = 1.0f
            holder.card.elevation = 8f
        } else {
            holder.card.alpha = 0.7f
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