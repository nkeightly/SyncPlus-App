package com.syncplus

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dataClasses.TimeLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask

class LineGraphActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var applyButton: Button
    private lateinit var backButton: Button
    private lateinit var startDateTitle: TextView
    private lateinit var endDateTitle: TextView
    private lateinit var totalHoursTextView: TextView

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_graph)

        lineChart = findViewById(R.id.lineChart)
        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)
        applyButton = findViewById(R.id.applyButton)
        backButton = findViewById(R.id.backButton)
        startDateTitle = findViewById(R.id.startDateTitle)
        endDateTitle = findViewById(R.id.endDateTitle)
        totalHoursTextView = findViewById(R.id.totalHoursTextView)

        backButton.setOnClickListener {
            finish()
        }

        applyButton.setOnClickListener {
            fetchUserGoalsAndDisplayGraph()
        }

        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }
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

    private fun fetchUserGoalsAndDisplayGraph() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val projectsRef = db.collection("users").document(userId).collection("projects")
        projectsRef.get().addOnSuccessListener { projectsSnapshot ->
            val projects = projectsSnapshot.documents
            if (projects.isEmpty()) return@addOnSuccessListener

            val minHoursList = mutableListOf<Double>()
            val maxHoursList = mutableListOf<Double>()
            val timeLogs = mutableListOf<TimeLog>()

            projects.forEach { projectDoc ->
                val projectId = projectDoc.id
                val minHoursStr = projectDoc.getString("minHours")
                val maxHoursStr = projectDoc.getString("maxHours")
                val minHours = parseTimeStringToHours(minHoursStr)
                val maxHours = parseTimeStringToHours(maxHoursStr)

                minHoursList.add(minHours)
                maxHoursList.add(maxHours)

                db.collection("users").document(userId).collection("timeLogs")
                    .whereEqualTo("projectId", projectId).get().addOnSuccessListener { timeLogsSnapshot ->
                        for (timeLogDoc in timeLogsSnapshot.documents) {
                            val timeLog = timeLogDoc.toObject(TimeLog::class.java)
                            if (timeLog != null) {
                                val timestamp = timeLog.timestamp?.toDate()
                                if (timestamp != null) {
                                    if (isWithinSelectedDateRange(timestamp)) {
                                        timeLogs.add(timeLog)
                                    }
                                }
                            }
                        }
                        if (projects.indexOf(projectDoc) == projects.size - 1) {
                            displayGraph(projects, timeLogs, minHoursList, maxHoursList)
                        }
                    }
            }
        }
    }

    private fun isWithinSelectedDateRange(date: Date): Boolean {
        val startDate = parseDate(startDateEditText.text.toString())
        val endDate = parseDate(endDateEditText.text.toString())

        return !date.before(startDate) && !date.after(endDate)
    }

    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }

    private fun displayGraph(projects: List<DocumentSnapshot>, timeLogs: List<TimeLog>, minHoursList: List<Double>, maxHoursList: List<Double>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoursPerDay = mutableMapOf<String, Double>()
        val minHours = minHoursList.sum()
        val maxHours = maxHoursList.sum()

        timeLogs.forEach { timeLog ->
            val timestamp = timeLog.timestamp?.toDate()
            if (timestamp != null) {
                val date = dateFormat.format(timestamp)
                val hours = timeLog.elapsedTimeHours
                hoursPerDay[date] = (hoursPerDay[date] ?: 0.0) + hours
            }
        }

        val entries = hoursPerDay.entries.map { Entry(dateFormat.parse(it.key).time.toFloat(), it.value.toFloat()) }
        val dataSet = LineDataSet(entries, "Hours Worked").apply {
            color = ContextCompat.getColor(this@LineGraphActivity, R.color.defaultBlue)
            setDrawValues(false)
        }

        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(ContextCompat.getColor(this@LineGraphActivity, R.color.defaultBlue))

        val lineData = LineData(dataSet)

        // Add connector lines for multiple projects
        val projectEntries = mutableListOf<Entry>()
        projects.forEach { project ->
            val projectTimeLogs = timeLogs.filter { it.projectId == project.id }
            projectTimeLogs.forEach { timeLog ->
                val timestamp = timeLog.timestamp?.toDate()
                if (timestamp != null) {
                    val date = dateFormat.format(timestamp)
                    val hours = timeLog.elapsedTimeHours
                    projectEntries.add(Entry(dateFormat.parse(date).time.toFloat(), hours.toFloat()))
                }
            }
        }

        val projectDataSet = LineDataSet(projectEntries, "Project Connector").apply {
            color = ContextCompat.getColor(this@LineGraphActivity, R.color.black)
            setDrawValues(false)
        }

        lineData.addDataSet(projectDataSet)

        lineChart.data = lineData
        lineChart.invalidate() // Refresh the chart

        // Customize chart
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.axisLeft.setAxisMinimum(0f)
        lineChart.axisRight.isEnabled = false

        // Add description for lines
        val legend = lineChart.legend
        legend.isEnabled = true
        legend.setCustom(
            listOf(
                LegendEntry("Hours Worked", LegendForm.LINE, 10f, 2f, null, ContextCompat.getColor(this@LineGraphActivity, R.color.defaultBlue)),
                LegendEntry("Max Hours", LegendForm.LINE, 10f, 2f, null, ContextCompat.getColor(this@LineGraphActivity, R.color.green)),
                LegendEntry("Min Hours", LegendForm.LINE, 10f, 2f, null, ContextCompat.getColor(this@LineGraphActivity, R.color.red)),
                LegendEntry("Project Connector", LegendForm.LINE, 10f, 2f, null, ContextCompat.getColor(this@LineGraphActivity, R.color.black))
            )
        )

        // Set marker for showing min/max hours
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e == null) return

                val selectedDate = Date(e.x.toLong())
                val selectedDateStr = dateFormat.format(selectedDate)

                val minHoursEntry = Entry(e.x, minHours.toFloat())
                val maxHoursEntry = Entry(e.x, maxHours.toFloat())

                val minHoursDataSet = LineDataSet(listOf(minHoursEntry), "Min Hours").apply {
                    color = ContextCompat.getColor(this@LineGraphActivity, R.color.red)
                    setDrawValues(false)
                    setDrawCircles(true)
                    circleRadius = 5f
                    setCircleColor(ContextCompat.getColor(this@LineGraphActivity, R.color.red))
                }

                val maxHoursDataSet = LineDataSet(listOf(maxHoursEntry), "Max Hours").apply {
                    color = ContextCompat.getColor(this@LineGraphActivity, R.color.green)
                    setDrawValues(false)
                    setDrawCircles(true)
                    circleRadius = 5f
                    setCircleColor(ContextCompat.getColor(this@LineGraphActivity, R.color.green))
                }

                lineData.addDataSet(minHoursDataSet)
                lineData.addDataSet(maxHoursDataSet)
                lineChart.data = lineData
                lineChart.invalidate()

                Timer().schedule(timerTask {
                    lineData.removeDataSet(minHoursDataSet)
                    lineData.removeDataSet(maxHoursDataSet)
                    runOnUiThread {
                        lineChart.data = lineData
                        lineChart.invalidate()
                    }
                }, 6000)
            }

            override fun onNothingSelected() {
                // No action needed
            }
        })

        // Calculate and display total hours
        val totalHours = hoursPerDay.values.sum()
        totalHoursTextView.text = "Total Hours: $totalHours"

        // Hide Date Pickers, Titles, and Apply Button
        startDateEditText.visibility = View.GONE
        endDateEditText.visibility = View.GONE
        startDateTitle.visibility = View.GONE
        endDateTitle.visibility = View.GONE
        applyButton.visibility = View.GONE

        // Show Line Chart
        lineChart.visibility = View.VISIBLE
    }

    private fun parseTimeStringToHours(timeString: String?): Double {
        if (timeString.isNullOrEmpty()) return 0.0

        val parts = timeString.split(":")
        if (parts.size != 2) return 0.0

        val hours = parts[0].toDoubleOrNull() ?: 0.0
        val minutes = parts[1].toDoubleOrNull() ?: 0.0

        return hours + (minutes / 60.0)
    }
}
