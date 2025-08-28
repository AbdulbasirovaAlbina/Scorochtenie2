package com.example.scorochtenie2

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class LearningTechniqueItem(
    val title: String,
    val category: String,
    val description: String,
    val benefits: String,
    val difficulty: Int, // 1-5 stars
    val iconResId: Int,
    val practiceClass: Class<*>
)

class LearningTechniqueAdapter(private val techniques: List<LearningTechniqueItem>) :
    RecyclerView.Adapter<LearningTechniqueAdapter.LearningTechniqueViewHolder>() {

    class LearningTechniqueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.technique_icon)
        val title: TextView = itemView.findViewById(R.id.technique_title)
        val category: TextView = itemView.findViewById(R.id.technique_category)
        val description: TextView = itemView.findViewById(R.id.technique_description)
        val benefits: TextView = itemView.findViewById(R.id.technique_benefits)
        val difficultyStars: LinearLayout = itemView.findViewById(R.id.difficulty_stars)
        val practiceButton: Button = itemView.findViewById(R.id.practice_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearningTechniqueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_technique_learning, parent, false)
        return LearningTechniqueViewHolder(view)
    }

    override fun onBindViewHolder(holder: LearningTechniqueViewHolder, position: Int) {
        val technique = techniques[position]
        
        holder.title.text = technique.title
        holder.category.text = technique.category
        holder.description.text = technique.description
        holder.benefits.text = technique.benefits
        holder.icon.setImageResource(technique.iconResId)

        // Clear existing stars
        holder.difficultyStars.removeAllViews()
        
        // Add difficulty stars
        for (i in 1..5) {
            val star = ImageView(holder.itemView.context)
            val size = (16 * holder.itemView.context.resources.displayMetrics.density).toInt()
            val layoutParams = LinearLayout.LayoutParams(size, size)
            layoutParams.setMargins(0, 0, 8, 0)
            star.layoutParams = layoutParams
            
            if (i <= technique.difficulty) {
                star.setImageResource(R.drawable.ic_star_filled)
            } else {
                star.setImageResource(R.drawable.ic_star_empty)
            }
            
            holder.difficultyStars.addView(star)
        }

        holder.practiceButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TechniqueDemoActivity::class.java)
            intent.putExtra("technique_name", technique.title)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = techniques.size
}