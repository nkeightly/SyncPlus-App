package com.syncplus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class DailyGoalPage : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var minHoursEditText: EditText
    private lateinit var maxHoursEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var addDailyGoalBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_goal_page)

        calendarView = findViewById(R.id.DateWidget)
        calendarView.date = System.currentTimeMillis() // Set current date and disable date changes
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Shows  message if user tries to change the date
            Toast.makeText(
                this,
                "Date cannot be changed. Selected: $year/$month/$dayOfMonth",
                Toast.LENGTH_SHORT
            ).show()
        }

        minHoursEditText = findViewById(R.id.MinHours)
        maxHoursEditText = findViewById(R.id.MaxHours)
        tagsEditText = findViewById(R.id.TagsBox)

        addDailyGoalBtn = findViewById(R.id.AddDailyGoalBtn)
        addDailyGoalBtn.setOnClickListener {
            val minHours = minHoursEditText.text.toString()
            val maxHours = maxHoursEditText.text.toString()
            val tags = tagsEditText.text.toString()
            val dates = calendarView.date.toString()

            // Create a DailyGoal object
            val dailyGoal = DailyGoal(minHours, maxHours, tags, calendarView.date)

            // Save to Firebase Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("dailyGoals")
                .add(dailyGoal.toHashMap())
                .addOnSuccessListener { documentReference ->
                    // Displays the entered data
                    Toast.makeText(
                        this,
                        "Date: $dates, Min Hours: $minHours, Max Hours: $maxHours, Tags: $tags",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Take user back to activity_project
                    // Pass the data back to ProjectActivity
                    val intent = Intent()
                    intent.putExtra("minHours", minHours)
                    intent.putExtra("maxHours", maxHours)
                    intent.putExtra("tags", tags)
                    intent.putExtra("dates", dates)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error adding document: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}

// Data class for DailyGoal
data class DailyGoal(val minHours: String, val maxHours: String, val tags: String, val date: Long) {
    fun toHashMap(): Map<String, Any> {
        return mapOf(
            "minHours" to minHours,
            "maxHours" to maxHours,
            "tags" to tags,
            "date" to date
        )
    }
}