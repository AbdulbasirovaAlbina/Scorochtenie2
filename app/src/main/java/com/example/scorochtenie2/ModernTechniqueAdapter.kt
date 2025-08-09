package com.example.scorochtenie2

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
    val progress: Int = 0 // 0-100
)

class ModernTechniqueAdapter(private val techniques: List<ModernTechniqueItem>) :
    RecyclerView.Adapter<ModernTechniqueAdapter.ModernTechniqueViewHolder>() {

    class ModernTechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val title: TextView = itemView.findViewById(R.id.technique_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModernTechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technique_modern, parent, false)
        return ModernTechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModernTechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        holder.title.text = technique.title
        holder.icon.setImageResource(technique.iconResId)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TechniqueSettingsActivity::class.java)
            intent.putExtra("technique_name", technique.title)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = techniques.size
}
