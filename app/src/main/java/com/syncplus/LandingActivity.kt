package com.syncplus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dataClasses.Project

class LandingActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val createProjectCard: CardView = findViewById(R.id.cardViewCreateProject)
        createProjectCard.setOnClickListener { openProjectActivity() }

        val logAnalyticsCard: CardView = findViewById(R.id.cardViewLogAnalytics)
        logAnalyticsCard.setOnClickListener { openLogActivity() }

        val bellIcon: ImageView = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener { showNotification() }
    }

    private fun openProjectActivity() {
        val intent = Intent(this, ProjectActivity::class.java)
        startActivity(intent)
    }

    private fun openLogActivity() {
        val intent = Intent(this, LogActivity::class.java)
        startActivity(intent)
    }

    private fun showNotification() {
        if (userId == null) {
            Log.e("LandingActivity", "User ID is null")
            return
        }

        db.collection("users").document(userId).collection("projects")
            .get()
            .addOnSuccessListener { documents ->
                val projects = documents.toObjects(Project::class.java)
                NotificationDialog(this, projects).show()
            }
            .addOnFailureListener { e ->
                Log.e("LandingActivity", "Failed to fetch projects", e)
            }
    }
}
