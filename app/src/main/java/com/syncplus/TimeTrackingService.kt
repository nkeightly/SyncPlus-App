package com.syncplus

import TimesheetProjectAdapter
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dataClasses.Project
import dataClasses.TimesheetEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TimeTrackingService : AppCompatActivity() {
    private val REQUEST_CODE_STORAGE_PERMISSION = 1
    private lateinit var recyclerView: RecyclerView
    private lateinit var projectAdapter: TimesheetProjectAdapter
    private lateinit var entries: MutableList<TimesheetEntry>
    private val db = FirebaseFirestore.getInstance()
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheet)

        recyclerView = findViewById(R.id.recyclerViewProjects)
        entries = mutableListOf() // Initialize an empty list

        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)

        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }

        // Back navigation
        val backButton = findViewById<ImageView>(R.id.imageView_back)
        backButton.setOnClickListener { finish() }

        // Initialize and set up RecyclerView and adapter for projects
        projectAdapter = TimesheetProjectAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = projectAdapter

        // Fetch user's projects from Firestore
        fetchUserProjects()

        // Request storage permission
        requestStoragePermission()
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Storage permission is required to load images.", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun fetchUserProjects() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId).collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                var totalElapsedTimeHours = 0.0
                for (document in documents) {
                    val projectName = document.getString("title") ?: ""
                    val categoryName = document.getString("categoryName") ?: ""
                    val startDate = document.getString("startDate") ?: ""
                    val endDate = document.getString("endDate") ?: ""
                    val minHours = document.getString("minHours") ?: ""
                    val maxHours = document.getString("maxHours") ?: ""
                    val imagePath = document.getString("imagePath") ?: ""
                    val categoryIconResId = document.getLong("iconResId")?.toInt() ?: 0

                    db.collection("users").document(userId).collection("timeLogs")
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThanOrEqualTo("timestamp", endDate)
                        .get()
                        .addOnSuccessListener { timeLogDocuments ->
                            var projectElapsedTimeHours = 0.0
                            for (timeLogDocument in timeLogDocuments) {
                                val elapsedTimeMillis = timeLogDocument.getLong("elapsedTimeMillis") ?: 0
                                val elapsedTimeHours = elapsedTimeMillis / (1000 * 60 * 60).toDouble()
                                projectElapsedTimeHours += elapsedTimeHours
                            }
                            totalElapsedTimeHours += projectElapsedTimeHours
                            val hoursLeft = calculateHoursLeft(minHours, maxHours, projectElapsedTimeHours)

                            val project = Project("", projectName, "", startDate, endDate, categoryName, minHours, maxHours, mutableListOf(), mutableListOf(), 0L, "", imagePath, categoryIconResId)
                            val entry = TimesheetEntry(project, hoursLeft, true)
                            entries.add(entry)

                            projectAdapter.updateProjects(entries.map { it.project })
                        }
                        .addOnFailureListener { showToast("Cannot access Project") }
                }
            }
            .addOnFailureListener { showToast("Cannot access Project") }
    }

    private fun calculateHoursLeft(minHours: String, maxHours: String, totalElapsedTimeHours: Double): String {
        val minHoursDouble = minHours.toDoubleOrNull() ?: return "Invalid Min Hours"
        val maxHoursDouble = maxHours.toDoubleOrNull() ?: return "Invalid Max Hours"

        val hoursLeft = maxHoursDouble - totalElapsedTimeHours
        return if (hoursLeft < 0) "0" else String.format("%.2f", hoursLeft)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun fetchFilteredEntries(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val startDateInput = startDateEditText.text.toString()
        val endDateInput = endDateEditText.text.toString()

        Log.d("TimeTrackingService", "Filtering projects from $startDateInput to $endDateInput")

        db.collection("users").document(userId).collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                val projects = documents.documents
                for (document in projects) {
                    Log.d("TimeTrackingService", "Project ID: ${document.id}, Start Date: ${document.getString("startDate")}, End Date: ${document.getString("endDate")}")
                }

                db.collection("users").document(userId).collection("projects")
                    .whereGreaterThanOrEqualTo("startDate", startDateInput)
                    .whereLessThanOrEqualTo("endDate", endDateInput)
                    .orderBy("startDate") // Order by startDate first
                    .orderBy("endDate")   // Then order by endDate
                    .get()
                    .addOnSuccessListener { filteredDocuments ->
                        Log.d("TimeTrackingService", "Fetched ${filteredDocuments.size()} projects")
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
                        projectAdapter.updateProjects(filteredProjects)
                    }
                    .addOnFailureListener { e ->
                        showToast("Failed to fetch filtered projects")
                        Log.e("TimeTrackingService", "Error fetching filtered projects", e)
                    }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch projects")
                Log.e("TimeTrackingService", "Error fetching projects", e)
            }
    }
}
