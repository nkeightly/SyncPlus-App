package com.syncplus

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dataClasses.Project

class NotificationDialog(context: Context, private val projects: List<Project>) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_notification)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewProjects)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ProjectAdapter(projects)

        Log.d("NotificationDialog", "Displaying ${projects.size} projects in the notification dialog")
    }
}
