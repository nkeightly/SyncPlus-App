package com.syncplus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dataClasses.Category

class CategoryAdapter(
    options: FirestoreRecyclerOptions<Category>,
    private val onCategoryClickListener: OnCategoryClickListener
) : FirestoreRecyclerAdapter<Category, CategoryAdapter.ViewHolder>(options) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
        // Update visual state for all cards
        updateCardVisualState()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Category) {
        holder.bind(model)

        // Set selected state
        holder.cardView.setCardBackgroundColor(
            if (position == selectedPosition) {
                Color.parseColor("#FF9800") // Selected color
            } else {
                Color.WHITE // Transparent for unselected cards
            }
        )

        // Handle item click
        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition != selectedPosition) {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelected) // Update previously selected card
                notifyItemChanged(selectedPosition) // Update newly selected card
                val iconResId = model.iconResId // Get the icon resource ID
                onCategoryClickListener.onCategoryClick(adapterPosition, model, iconResId) // Pass position, category, and iconResId
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        private val categoryIconImageView: ImageView = itemView.findViewById(R.id.categoryIconImageView)
        val cardView: CardView = itemView.findViewById(R.id.categoryCardView)

        fun bind(category: Category) {
            categoryNameTextView.text = category.name
            categoryIconImageView.setImageResource(category.iconResId)
        }
    }

    interface OnCategoryClickListener {
        fun onCategoryClick(position: Int, category: Category, iconResId: Int)
    }

    private fun updateCardVisualState() {
        for (i in 0 until itemCount) {
            val viewHolder = ""//recyclerView.findViewHolderForAdapterPosition(i) as? ViewHolder
            viewHolder?.let {
               // it.cardView.setCardBackgroundColor(
                   // if (i == selectedPosition) {
                      //  Color.parseColor("#FF9800") // Selected color
                   // } else {
                     //   Color.WHITE // Transparent for unselected cards
                    //}
                //)
            }
        }
    }
}
