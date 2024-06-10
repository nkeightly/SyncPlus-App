import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.syncplus.R

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // Example usage after creating a project
        val projectName = "My Project"
        val projectIconResId = R.drawable.project_icon
        addProjectWidget(projectName, projectIconResId)
    }

    // Method to add a project widget dynamically
    private fun addProjectWidget(projectName: String, projectIconResId: Int) {
        val projectContainer: LinearLayout = findViewById(R.id.projectContainer)

        // Create a new project widget
        //val projectWidget = layoutInflater.inflate(R.layout.project_widget_layout, null)
        //val projectNameTextView: TextView = projectWidget.findViewById(R.id.projectName)
        //val projectIconImageView: ImageView = projectWidget.findViewById(R.id.projectIcon)

        // Set project name and icon
        //projectNameTextView.text = projectName
        //projectIconImageView.setImageResource(projectIconResId)

        // Set layout params for the widget
        //val layoutParams = LinearLayout.LayoutParams(
            //resources.getDimensionPixelSize(R.dimen.project_widget_width),
            //resources.getDimensionPixelSize(R.dimen.project_widget_height)
        //)
        //projectWidget.layoutParams = layoutParams

        // Add the project widget to the container
        //projectContainer.addView(projectWidget)
    }
}

