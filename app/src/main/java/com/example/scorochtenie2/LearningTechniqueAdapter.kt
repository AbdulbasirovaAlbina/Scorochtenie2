package com.example.scorochtenie2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class LearningTechniqueAdapter(private val techniques: List<Technique>) : 
    RecyclerView.Adapter<LearningTechniqueAdapter.TechniqueViewHolder>() {

    class TechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val title: TextView = itemView.findViewById(R.id.technique_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technique, parent, false)
        return TechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: TechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.title.text = technique.title
        holder.icon.setImageResource(technique.iconResId)
        
        holder.itemView.setOnClickListener {
            // Для обучения просто показываем toast (позже здесь будет другая логика)
            Toast.makeText(holder.itemView.context, 
                "Обучение: ${technique.title}", 
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = techniques.size
}
