import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syncplus.R
import dataClasses.Project

class TimesheetProjectAdapter(private val projects: MutableList<Project>) :
    RecyclerView.Adapter<TimesheetProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_entry, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]

        // Load category icon based on icon resource ID
        holder.view.findViewById<ImageView>(R.id.categoryIconImageView)
            .setImageResource(project.iconResId)

        // Set project name
        holder.view.findViewById<TextView>(R.id.projectNameTextView).text = project.title

        // Set dates
        val datesTextView = holder.view.findViewById<TextView>(R.id.datesTextView)
        val startDate = project.startDate
        val endDate = project.endDate
        val datesText = "Start Date: $startDate - End Date: $endDate"
        datesTextView.text = datesText

        // Set hours left
        holder.view.findViewById<TextView>(R.id.hoursLeftTextView)
            .text = "Hours Left: ${project.maxHours}"

        // Set info icon click listener
        holder.view.findViewById<ImageView>(R.id.infoIconImageView).setOnClickListener {
            showProjectInfoDialog(holder.view.context, project)
        }
    }

    private fun loadFromFile(context: Context, filePath: String, imageView: ImageView) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            Glide.with(context)
                .load(filePath)
                .error(R.drawable.ic_write)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_write)
        }
    }

    private fun showProjectInfoDialog(context: Context, project: Project) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_project_info)

        val projectImageView = dialog.findViewById<ImageView>(R.id.projectImageView)
        val categoryInfoTextView = dialog.findViewById<TextView>(R.id.categoryInfoTextView)

        // Load image using Glide from file path
        loadFromFile(context, project.imagePath, projectImageView)

        // Set category info
        categoryInfoTextView.text = project.categoryName

        dialog.show()
    }

    override fun getItemCount(): Int = projects.size

    fun updateProjects(newProjects: List<Project>) {
        projects.clear()
        projects.addAll(newProjects)
        notifyDataSetChanged()
    }
}
