package com.example.scorochtenie2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class TechniqueSelectorAdapter(
    private val techniques: List<TechniqueItem>,
    private val onTechniqueSelected: (TechniqueItem) -> Unit
) : RecyclerView.Adapter<TechniqueSelectorAdapter.TechniqueViewHolder>() {

    private var selectedPosition = 0

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val iconContainer: View = itemView.findViewById(R.id.technique_icon_container)
        val title: TextView = itemView.findViewById(R.id.technique_title)
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
        holder.title.text = technique.title

        if (technique.title == "Все техники") {
            holder.icon.setColorFilter(android.graphics.Color.WHITE)
            holder.iconContainer.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.technique_icon_all_techniques_bg)
        } else {
            holder.icon.clearColorFilter()
            holder.iconContainer.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.technique_icon_modern_bg)
        }

        val currentPosition = holder.getAdapterPosition()
        holder.card.isSelected = (currentPosition == selectedPosition)
        if (currentPosition == selectedPosition) {
            holder.card.elevation = 8f
            holder.title.setTextColor(ResourcesCompat.getColor(holder.itemView.context.resources, R.color.primary_color, holder.itemView.context.theme))
        } else {
            holder.card.elevation = 4f
            holder.title.setTextColor(ResourcesCompat.getColor(holder.itemView.context.resources, R.color.text_color_secondary, holder.itemView.context.theme))
        }

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.getAdapterPosition()
            if (clickedPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = clickedPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onTechniqueSelected(technique)
            }
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