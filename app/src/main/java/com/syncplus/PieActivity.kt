package com.syncplus

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dataClasses.Project
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PieActivity : AppCompatActivity() {
    private lateinit var linearContainer: LinearLayout
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var backButton: ImageView
    private val calendar = Calendar.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pie_activity)

        linearContainer = findViewById(R.id.linearContainer)
        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)

        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }
        backButton = findViewById(R.id.imageView_back)

        backButton.setOnClickListener {
            onBackPressed()
        }
        val viewGraphButton: Button = findViewById(R.id.viewGraphButton)
        viewGraphButton.setOnClickListener {
            val intent = Intent(this, LineGraphActivity::class.java)
            startActivity(intent)
        }

        val searchButton: Button = findViewById(R.id.searchButton)
        searchButton.setOnClickListener { fetchFilteredEntries() }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "${getMonthShort(selectedMonth)}-$selectedDay-$selectedYear"
                editText.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun getMonthShort(month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun fetchFilteredEntries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val startDateInput = startDateEditText.text.toString()
        val endDateInput = endDateEditText.text.toString()

        Log.d("PieActivity", "Filtering projects from $startDateInput to $endDateInput")

        db.collection("users").document(userId).collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                val projects = documents.documents
                for (document in projects) {
                    Log.d("PieActivity", "Project ID: ${document.id}, Start Date: ${document.getString("startDate")}, End Date: ${document.getString("endDate")}")
                }

                db.collection("users").document(userId).collection("projects")
                    .whereGreaterThanOrEqualTo("startDate", startDateInput)
                    .whereLessThanOrEqualTo("endDate", endDateInput)
                    .orderBy("startDate") // Order by startDate first
                    .orderBy("endDate")   // Then order by endDate
                    .get()
                    .addOnSuccessListener { filteredDocuments ->
                        Log.d("PieActivity", "Fetched ${filteredDocuments.size()} projects")
                        val filteredProjects = mutableListOf<Project>()
                        for (document in filteredDocuments) {
                            val projectName = document.getString("title") ?: ""
                            val categoryName = document.getString("categoryName") ?: ""
                            val projectStartDate = document.getString("startDate") ?: ""
                            val projectEndDate = document.getString("endDate") ?: ""
                            val minHours = document.getString("minHours") ?: ""
                            val maxHours = document.getString("maxHours") ?: ""
                            val imagePath = document.getString("imagePath") ?: ""
                            val categoryIconResId = document.getLong("iconResId")?.toInt() ?: 0

                            val project = Project("", projectName, "", projectStartDate, projectEndDate, categoryName, minHours, maxHours, mutableListOf(), mutableListOf(), 0L, "", imagePath, categoryIconResId)
                            filteredProjects.add(project)
                        }
                        if (filteredProjects.isEmpty()) {
                            showToast("No projects found in the specified date range")
                        }
                        updateUI(filteredProjects)
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to fetch filtered projects")
                        Log.e("PieActivity", "Error fetching filtered projects", e)
                    }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch projects")
                Log.e("PieActivity", "Error fetching projects", e)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUI(filteredProjects: List<Project>) {
        // Clear existing views
        linearContainer.removeAllViews()

        val hoursByCategory = mutableMapOf<String, Double>()
        for (project in filteredProjects) {
            val categoryName = project.categoryName ?: "Uncategorized"
            val minHours = parseTimeStringToHours(project.minHours)
            val maxHours = parseTimeStringToHours(project.maxHours)

            hoursByCategory[categoryName] = (hoursByCategory[categoryName] ?: 0.0) + maxHours

            // Display the data in the UI
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.setPadding(16, 16, 16, 16)

            val entryTextView = TextView(this)
            entryTextView.text = "$categoryName: ${hoursByCategory[categoryName]} hours"
            entryTextView.textSize = 18f
            entryTextView.setTextColor(ContextCompat.getColor(this, R.color.black))

            // Load custom font
            val customFont = ResourcesCompat.getFont(this, R.font.roboto_regular)
            entryTextView.typeface = customFont

            // Create a progress bar for visual representation
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            progressBar.max = (maxHours * 100).toInt()
            progressBar.progress = (hoursByCategory[categoryName]!!.toInt() * 100).toInt()

            val feedbackTextView = TextView(this)
            feedbackTextView.textSize = 14f

            if (hoursByCategory[categoryName]!! < minHours) {
                progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.progress_bar_low)
                feedbackTextView.text = "Below minimum goal"
                feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.red))
            } else if (hoursByCategory[categoryName]!! > maxHours) {
                progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.progress_bar_high)
                feedbackTextView.text = "Above maximum goal"
                feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            } else {
                progressBar.progressDrawable = ContextCompat.getDrawable(this, R.drawable.progress_bar_normal)
                feedbackTextView.text = "Within goal range"
                feedbackTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
            }

            container.addView(entryTextView)
            container.addView(progressBar)
            container.addView(feedbackTextView)

            linearContainer.addView(container)
        }

        displayProgressSummary(hoursByCategory)
    }

    private fun displayProgressSummary(hoursByCategory: Map<String, Double>) {
        val cardView = CardView(this)
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        cardView.radius = 12f
        cardView.cardElevation = 8f
        cardView.setContentPadding(32, 32, 32, 32)

        val currentMonth = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

        val summaryTextView = TextView(this)
        summaryTextView.text = "Your progress for $currentMonth:\n" +
                "- Green: Within goal range\n" +
                "- Orange: Above maximum goal\n" +
                "- Red: Below minimum goal"
        summaryTextView.textSize = 16f
        summaryTextView.setTextColor(ContextCompat.getColor(this, R.color.black))

        cardView.addView(summaryTextView)
        linearContainer.addView(cardView)
    }

    // Function to parse time string (HH:mm) to hours (Double)
    private fun parseTimeStringToHours(timeString: String?): Double {
        if (timeString.isNullOrEmpty()) return 0.0

        val parts = timeString.split(":")
        if (parts.size != 2) return 0.0

        val hours = parts[0].toDoubleOrNull() ?: 0.0
        val minutes = parts[1].toDoubleOrNull() ?: 0.0

        return hours + (minutes / 60.0) // Convert minutes to hours
    }
}
