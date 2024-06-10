package com.syncplus

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dataClasses.Project
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class ProjectAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private val patternImages = listOf(
        R.drawable.pattern_blue,
        R.drawable.pattern_doiley,
        R.drawable.pattern_floral,
        R.drawable.pattern_gold,
        R.drawable.pattern_lv,
        R.drawable.pattern_mix,
        R.drawable.pattern_nude_pink,
        R.drawable.pattern_orange,
        R.drawable.pattern_pink,
        R.drawable.pattern_purple,
        R.drawable.pattern_red,
        R.drawable.pattern_snow,
        R.drawable.pattern_texture,
        R.drawable.pattern_texture_purple
    )

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectImage: ImageView = itemView.findViewById(R.id.projectImage)
        val projectName: TextView = itemView.findViewById(R.id.projectName)
        val projectTimeLeft: TextView = itemView.findViewById(R.id.projectTimeLeft)
        val dailyGoals: TextView = itemView.findViewById(R.id.dailyGoals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.title

        // Load image using Picasso or any image loading library
        Picasso.get().load(project.imagePath).into(holder.projectImage, object : com.squareup.picasso.Callback {
            override fun onSuccess() {
                // Do nothing if the image loads successfully
            }

            override fun onError(e: Exception?) {
                // Randomly select a pattern image in case of an error
                val randomPattern = patternImages[Random.nextInt(patternImages.size)]
                holder.projectImage.setImageResource(randomPattern)
            }
        })

        // Calculate time left
        val timeLeft = calculateTimeLeft(project.endDate)
        holder.projectTimeLeft.text = "Time left: $timeLeft"

        // Display daily goals
        holder.dailyGoals.text = "Min: ${project.minHours} - Max: ${project.maxHours}"
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    private fun calculateTimeLeft(endDate: String): String {
        val dateFormat = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        return try {
            val end = dateFormat.parse(endDate)
            val now = Calendar.getInstance().time
            val diff = end.time - now.time
            val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
            "$daysLeft days"
        } catch (e: Exception) {
            Log.e("ProjectAdapter", "Error parsing date", e)
            "N/A"
        }
    }
}
