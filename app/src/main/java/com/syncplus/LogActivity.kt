package com.syncplus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.concurrent.TimeUnit

class LogActivity : AppCompatActivity() {
    private var isTracking = false
    private var startTimeMillis: Long = 0
    private lateinit var handler: Handler
    private lateinit var db: FirebaseFirestore
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var chronometer: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log) // Using the same layout as LogActivity

        handler = Handler()
        db = FirebaseFirestore.getInstance()

        // Initialize views
        chronometer = findViewById(R.id.chronometer)
        btnStart = findViewById(R.id.startButton)
        btnStop = findViewById(R.id.saveButton)

        btnStart.setOnClickListener {
            startTracking()
        }

        btnStop.setOnClickListener {
            stopTracking()
        }

        initButtons()
    }

    private fun startTracking() {
        if (!isTracking) {
            isTracking = true
            startTimeMillis = SystemClock.elapsedRealtime()
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            showToast("Time log started")
        }
    }

    private fun stopTracking() {
        // Fetch currentProjectId from SharedPreferences
        val sharedPreferences = getSharedPreferences("ProjectData", Context.MODE_PRIVATE)
        val currentProjectId = sharedPreferences.getString("currentProjectId", null)

        if (isTracking && currentProjectId != null) {
            isTracking = false
            val elapsedTimeMillis = SystemClock.elapsedRealtime() - startTimeMillis
            val elapsedTimeHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            val timeLog = hashMapOf<String, Any?>(
                "startTimeMillis" to startTimeMillis,
                "elapsedTimeMillis" to elapsedTimeMillis,
                "elapsedTimeHours" to elapsedTimeHours,
                "timestamp" to Calendar.getInstance().time,
                "projectId" to currentProjectId  // Associate time log with current project ID
            )

            saveTimeLogToFirestore(currentProjectId, timeLog)
            chronometer.stop()
            showToast("Time log stopped")
        } else {
            showToast("No active tracking session or no project ID found")
        }
    }

    private fun saveTimeLogToFirestore(currentProjectId: String, timeLog: HashMap<String, Any?>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .collection("timeLogs").add(timeLog)
            .addOnSuccessListener {
                showToast("Time log saved successfully!")
            }
            .addOnFailureListener { e ->
                showToast("Failed to save time log: ${e.message}")
                Log.e("LogActivity", "Error saving time log", e)
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun initButtons() {
        val btnTimeTracking: Button = findViewById(R.id.btnTimeTracking)
        val btnProjectActivity: Button = findViewById(R.id.btnProjectActivity)

        btnTimeTracking.setOnClickListener {
            // Navigate to TimeTrackingService
            startActivity(Intent(applicationContext, TimeTrackingService::class.java))
        }

        btnProjectActivity.setOnClickListener {
            // Navigate to ProjectActivity
            startActivity(Intent(applicationContext, PieActivity::class.java))
        }
    }
}
