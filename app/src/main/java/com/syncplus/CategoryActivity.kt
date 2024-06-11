package com.syncplus

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.gridlayout.widget.GridLayout
import com.google.firebase.firestore.FirebaseFirestore
import dataClasses.Category

class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryGridLayout: GridLayout
    private var selectedPosition: Int = -1
    private lateinit var categoryName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Setup GridLayout
        categoryGridLayout = findViewById(R.id.categoryGridLayout)

        // Initialize Firestore
        val firestore = FirebaseFirestore.getInstance()
        val query = firestore.collection("categories")

        // Set click listeners for hardcoded cards
        setHardcodedCardListeners()

        // Find the button by its ID and set click listener
        val createCategoryButton = findViewById<Button>(R.id.createCategoryButton)
        createCategoryButton.setOnClickListener {
            showAddCategoryDialog()
        }

        // Listen for changes in Firestore to dynamically add cards
        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                categoryGridLayout.removeAllViews()
                setHardcodedCardListeners()
                for (doc in snapshot.documents) {
                    val category = doc.toObject(Category::class.java)
                    if (category != null) {
                        addCategoryCard(category)
                    }
                }
            }
        }
    }

    private fun addCategoryCard(category: Category) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.category_card_item, null) as CardView

        val categoryIconImageView = cardView.findViewById<ImageView>(R.id.categoryIconImageView)
        val categoryNameTextView = cardView.findViewById<TextView>(R.id.categoryNameTextView)

        categoryIconImageView.setImageResource(category.iconResId)
        categoryNameTextView.text = category.name

        // Set card properties
        cardView.layoutParams = GridLayout.LayoutParams().apply {
            width = resources.getDimensionPixelSize(R.dimen.card_width)
            height = resources.getDimensionPixelSize(R.dimen.card_height)
            setMargins(16, 16, 16, 16)
        }
        cardView.radius = resources.getDimension(R.dimen.card_corner_radius)
        cardView.cardElevation = resources.getDimension(R.dimen.card_elevation)

        // Set card click listener
        cardView.setOnClickListener {
            selectedPosition = categoryGridLayout.indexOfChild(cardView)
            updateCardVisualState()
            handleCategorySelection(category.iconResId, category.name)
        }

        categoryGridLayout.addView(cardView)
    }

    private fun showAddCategoryDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_category_dialog)

        val categoryIconSpinner = dialog.findViewById<Spinner>(R.id.category_icon_spinner)
        val categoryNameInput = dialog.findViewById<EditText>(R.id.category_name_input)
        val addCategoryButton = dialog.findViewById<Button>(R.id.add_category_button)
        val infoButton = dialog.findViewById<ImageView>(R.id.icon_info_button)

        // Set up category icon spinner with your icon options
        val iconAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.project_icons,
            android.R.layout.simple_spinner_item
        )
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryIconSpinner.adapter = iconAdapter

        // Set up a custom adapter for icon spinner to show icons
        val iconDrawableIds = resources.obtainTypedArray(R.array.project_icons)
        val iconNames = resources.getStringArray(R.array.project_icons)
        val customIconAdapter = IconSpinnerAdapter(this, iconDrawableIds, iconNames)
        categoryIconSpinner.adapter = customIconAdapter

        // Set click listener for info button
        infoButton.setOnClickListener {
            // Show additional information or perform an action when info button is clicked
            Toast.makeText(this, "Select an Icon; this icon will be your Category Icon.", Toast.LENGTH_SHORT).show()
        }

        addCategoryButton.setOnClickListener {
            categoryName = categoryNameInput.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                val iconPosition = categoryIconSpinner.selectedItemPosition
                val iconResId = getIconDrawable(iconPosition)

                // Create a new category object
                val newCategory = Category(categoryName, iconResId)

                // Add the category to Firestore
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("categories")
                    .add(newCategory)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Category added with ID: ${documentReference.id}")
                        // Save the selected category
                        selectedPosition = categoryGridLayout.childCount - 1
                        updateCardVisualState()
                        addCategoryCard(newCategory)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding category", e)
                        // Handle failure, e.g., show an error message
                    }

                dialog.dismiss() // Close the dialog after adding category
            } else {
                Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun getIconDrawable(position: Int): Int {
        return when (position) {
            0 -> R.drawable.ic_running
            1 -> R.drawable.ic_video
            2 -> R.drawable.ic_budget
            3 -> R.drawable.ic_money
            4 -> R.drawable.ic_confetti
            5 -> R.drawable.ic_hands
            6 -> R.drawable.ic_plant
            7 -> R.drawable.ic_success
            8 -> R.drawable.ic_thinking
            9 -> R.drawable.ic_people
            10 -> R.drawable.ic_design
            11 -> R.drawable.ic_work
            12 -> R.drawable.ic_notebook
            13 -> R.drawable.ic_tracking
            14 -> R.drawable.ic_travel
            15 -> R.drawable.ic_c
            16 -> R.drawable.ic_code
            else -> R.drawable.ic_default_icon
        }
    }

    private fun handleCategorySelection(iconResId: Int, categoryName: String) {
        // Navigate back to ProjectActivity
        val intent = Intent()
        intent.putExtra("categoryName", categoryName)
        intent.putExtra("iconResId", iconResId) // Pass the iconResId
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setHardcodedCardListeners() {
        val cards = listOf(
            findViewById<CardView>(R.id.card1),
            findViewById<CardView>(R.id.card2),
            findViewById<CardView>(R.id.card3),
            findViewById<CardView>(R.id.card4),
            findViewById<CardView>(R.id.card5),
            findViewById<CardView>(R.id.card6)
        )

        val hardcodedCardClickListener = { view: CardView ->
            val position = cards.indexOf(view)
            if (position != -1) {
                selectedPosition = position
                updateCardVisualState()
                handleCategorySelection(getIconDrawable(position), getCategoryName(position))
            }
        }

        cards.forEach { cardView ->
            cardView?.setOnClickListener { hardcodedCardClickListener(cardView) }
        }

        // Initial visual state setup
        updateCardVisualState()
    }

    private fun updateCardVisualState() {
        for (i in 0 until categoryGridLayout.childCount) {
            val cardView = categoryGridLayout.getChildAt(i) as CardView
            cardView.setCardBackgroundColor(
                if (i == selectedPosition) {
                    Color.parseColor("#FF9800") // Selected color
                } else {
                    Color.WHITE // Transparent for unselected cards
                }
            )
        }
    }

    private fun getCategoryName(position: Int): String {
        return when (position) {
            0 -> "Design"
            1 -> "Remote Work"
            2 -> "Coding"
            3 -> "Management"
            4 -> "Personal"
            5 -> "Collaboration"
            else -> "Default"
        }
    }
}
