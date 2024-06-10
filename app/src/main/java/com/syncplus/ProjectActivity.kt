package com.syncplus

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.syncplus.R.drawable.ic_check
import com.syncplus.R.drawable.pattern_blue
import com.syncplus.R.drawable.pattern_doiley
import com.syncplus.R.drawable.pattern_floral
import com.syncplus.R.drawable.pattern_gold
import com.syncplus.R.drawable.pattern_lv
import com.syncplus.R.drawable.pattern_mix
import com.syncplus.R.drawable.pattern_nude_pink
import com.syncplus.R.drawable.pattern_orange
import com.syncplus.R.drawable.pattern_pink
import com.syncplus.R.drawable.pattern_purple
import com.syncplus.R.drawable.pattern_red
import com.syncplus.R.drawable.pattern_snow
import com.syncplus.R.drawable.pattern_texture
import com.syncplus.R.drawable.pattern_texture_purple
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProjectActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var createProjectButton: Button
    private lateinit var imageView: ImageView
    private lateinit var imageViewIC: ImageView
    private val calendar: Calendar = Calendar.getInstance()
    private var selectedTheme = pattern_lv
    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String
    private lateinit var categoryName: String
    private lateinit var imageUri: String
    private lateinit var maxHours: String
    private lateinit var minHours: String
    private lateinit var imagePath: String
    private var iconResId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)

        // Initialize EditText fields
        startDateEditText = findViewById(R.id.startDateEditText)
        endDateEditText = findViewById(R.id.endDateEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        titleEditText = findViewById(R.id.titleEditText)

        // Set click listeners to show date picker dialog
        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }

        // Find other views by their IDs
        imageView = findViewById(R.id.imageViewI)
        createProjectButton = findViewById(R.id.button)

        imageView.setOnClickListener {
            showImageSelectorDialog()
        }

        // Set click listener for Daily Page button
        val dailyPageButton: Button = findViewById(R.id.dailyGoalButton)
        dailyPageButton.setOnClickListener {
            navigateToDailyGoalsPage()
        }

        // Set click listener for Create Category button
        val createCategoryButton: Button = findViewById(R.id.createCategoryButton)
        createCategoryButton.setOnClickListener {
            navigateToCategoryActivity()
        }

        createProjectButton.setOnClickListener { createProject() }

        // Retrieve data passed from other activities
        val categoryName = intent.getStringExtra("categoryName")
        val minHours = intent.getStringExtra("minHours")
        val maxHours = intent.getStringExtra("maxHours")
        val tags = intent.getStringExtra("tags")
        val dates = intent.getStringExtra("dates")

        // Check if user has existing projects
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        checkExistingProjects()

        // Find the button by its ID and set click listener
        val saveProjectButton = findViewById<Button>(R.id.button)
        saveProjectButton.setOnClickListener {
            handleSave()
        }
    }

    private fun checkExistingProjects() {
        val projectCount = sharedPreferences.getInt("$userId-projectCount", 0)
        if (projectCount == 0) {
            // User has no projects, guide them through creating
            guideUserToCreateProjects()
        } else {
            // User has existing projects, load data or continue as needed
            // For example, load project themes, categories, etc.
            loadExistingData()
        }
    }

    private fun guideUserToCreateProjects() {
        // Show dialogs or UI elements to guide the user
        // For example, show instructions, dialogs for creating projects, categories, and daily goals
        Toast.makeText(this, "Welcome! Let's create your first project.", Toast.LENGTH_SHORT).show()
    }

    private fun loadExistingData() {
        // Load existing data such as project themes, categories, etc.
        // For example, retrieve data from SharedPreferences or Firestore
        val userName = sharedPreferences.getString("$userId-name", "") ?: ""
        Toast.makeText(this, "Welcome back, $userName!", Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Update the EditText with the selected date in the desired format "Mar-27-2024"
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

    private fun showImageSelectorDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.image_selector_dialog)
        dialog.setCancelable(true)

        val themeSpinner = dialog.findViewById<Spinner>(R.id.themeSpinner)
        val okButton = dialog.findViewById<Button>(R.id.okButton)
        val attachImageButton = dialog.findViewById<Button>(R.id.attachImageButton)
        val imageView = dialog.findViewById<ImageView>(R.id.imageView)

        val themeNames = resources.getStringArray(R.array.project_themes)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = adapter

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTheme = getThemeResourceByName(themeNames[position])
                imageView.setImageResource(selectedTheme)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        okButton.setOnClickListener {
            imageViewIC = findViewById(R.id.imageViewI)
            imageViewIC.setBackgroundResource(ic_check)
            dialog.dismiss()
            saveProjectThemeToFirestore()
        }

        attachImageButton.setOnClickListener {
            openImagePicker()
        }

        dialog.show()
    }




    private fun saveProjectThemeToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val projectId = db.collection("users").document(userId).collection("projects").document().id

        // Storing currentProjectId in SharedPreferences
        val sharedPreferences = getSharedPreferences("ProjectData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("currentProjectId", projectId)
        editor.apply()


        val project = hashMapOf(
            "projectId" to projectId,
            "categoryName" to categoryName,
            "description" to descriptionEditText.text.toString(),
            "title" to titleEditText.text.toString(),
            "theme" to selectedTheme,
            "startDate" to startDateEditText.text.toString(),
            "endDate" to endDateEditText.text.toString(),
            "iconResId" to iconResId,
            "imageUri" to imageUri,
            "minHours" to minHours,
            "maxHours" to maxHours,
            "imagePath" to imagePath
        )

        db.collection("users").document(userId).collection("projects").document(projectId).set(project)
            .addOnSuccessListener {
                Toast.makeText(this, "Theme data saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save theme data: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        // Update project count in SharedPreferences
        val projectCount = sharedPreferences.getInt("$userId-projectCount", 0)
        sharedPreferences.edit().putInt("$userId-projectCount", projectCount + 1).apply()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }
    fun Uri.getDataPath(contentResolver: ContentResolver): String? {
        var path: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(this, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        cursor?.close()
        return path
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_PICKER_REQUEST_CODE -> {
                // Handle image picker result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Get the selected image URI
                    val selectedImageUri = data.data
                    val contentResolver = contentResolver
                    val Path = selectedImageUri?.getDataPath(contentResolver)
                    imagePath = Path.toString()
                    // Pass the selected image URI to the createProject method
                    imageUri = selectedImageUri.toString()
                    saveImageToFirestore(imageUri, imagePath)

                    createProject(selectedImageUri)
                }
            }
            CATEGORY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    categoryName = data.getStringExtra("categoryName").toString()
                    iconResId = data.getIntExtra("iconResId",0)// Get the icon resource ID
                    if (categoryName != null) {
                        saveCategoryToFirestore(categoryName, iconResId)
                    }
                }
            }
            DAILY_GOALS_REQUEST_CODE -> {
                // Handle daily goals result from DailyGoalsPage
                if (resultCode == Activity.RESULT_OK && data != null) {
                    minHours = data.getStringExtra("minHours").toString()
                    maxHours = data.getStringExtra("maxHours").toString()
                    val tags = data.getStringExtra("tags")
                    val dates = data.getStringExtra("dates")
                    // Handle the received daily goal details here
                    if(minHours != null && maxHours != null && tags != null && dates != null){
                        saveDailyGoalToFirestore(minHours, maxHours, tags, dates)
                    }
                }
            }
        }
    }

    private fun createProject(imageUri: Uri? = null) {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val startDate = startDateEditText.text.toString()
        val endDate = endDateEditText.text.toString()
        val categoryName = intent.getStringExtra("categoryName")
        val minHours = intent.getStringExtra("minHours")
        val maxHours = intent.getStringExtra("maxHours")
        val tags = intent.getStringExtra("tags")
        val dates = intent.getStringExtra("date")

        // Save the project to Firestore
        if (title.isNotEmpty() && description.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()
            && categoryName != null && minHours != null && maxHours != null && tags != null && dates != null
        ) {
            // Pass the imageUri to saveProjectToFirestore method
            saveProjectToFirestore(title, description, startDate, endDate, categoryName, minHours, maxHours, tags, dates, imageUri)
        } else {
            // Show a message indicating required fields are empty
            showToast("Please fill in all required fields.")
        }
    }

    private fun saveProjectToFirestore(
        title: String,
        description: String,
        startDate: String,
        endDate: String,
        categoryName: String,
        minHours: String,
        maxHours: String,
        tags: String,
        date: String,
        imageUri: Uri? // Updated to accept Uri directly
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val projectId = db.collection("users").document(userId).collection("projects").document().id

        val project = hashMapOf(
            "projectId" to projectId,
            "title" to title,
            "description" to description,
            "startDate" to startDate,
            "endDate" to endDate,
            "categoryName" to categoryName,
            "minHours" to minHours,
            "maxHours" to maxHours,
            "tags" to tags,
            "date" to date,
            "theme" to selectedTheme
        )

        // If imageUri is not null, add it to the project map as a string
        imageUri?.let {
            project["imageUri"] = imageUri.toString()
        }

        db.collection("users").document(userId).collection("projects").document(projectId).set(project)
            .addOnSuccessListener {
                showToast("Project saved successfully!")
                // Navigate to TimeTrackingService
                startActivity(Intent(this, LogActivity::class.java))
            }
            .addOnFailureListener {
                showToast("Failed to save project: ${it.message}")
            }

        // Update project count in SharedPreferences
        val projectCount = sharedPreferences.getInt("$userId-projectCount", 0)
        sharedPreferences.edit().putInt("$userId-projectCount", projectCount + 1).apply()
    }



    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun navigateToCategoryActivity() {
        val intent = Intent(this, CategoryActivity::class.java)
        startActivityForResult(intent, CATEGORY_REQUEST_CODE)
    }

    private fun navigateToDailyGoalsPage() {
        val intent = Intent(this, DailyGoalPage::class.java)
        startActivityForResult(intent, DAILY_GOALS_REQUEST_CODE)
    }

    private fun handleSave() {
        // Navigate to LogActivity
        val intent = Intent(this, LogActivity::class.java)
        startActivity(intent)
        finish() // Optionally finish the current activity if you don't want to return to it
    }
    private fun saveImageToFirestore(imageUri: String, imagePath:String){
        // Save the Image to Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val projectId = db.collection("users").document(userId).collection("projects").document().id
        val imageSelected = hashMapOf(
            "imageUri" to imageUri,
            "imagePath" to imagePath
        )
        db.collection("users").document(userId)
            .collection("projects").document(projectId)
            .collection("image").add(imageSelected)
            .addOnSuccessListener {
                showToast("Image saved successfully!")
            }
            .addOnFailureListener {
                showToast("Image haven't been saved successfully!")
            }

        val intent = Intent()
        intent.putExtra("imageUri", imageUri)
    }
    private fun saveDailyGoalToFirestore(minHours: String, maxHours: String, tags: String, dates: String) {
        // Save the daily goal to Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dailyGoal = hashMapOf(
            "minHours" to minHours,
            "maxHours" to maxHours,
            "tags" to tags,
            "dates" to dates
        )
        db.collection("users").document(userId)
            .collection("projects").document("projectId") // Use the project ID
            .collection("dailyGoals").add(dailyGoal)
            .addOnSuccessListener {
                showToast("Daily Goals saved successfully!")
            }
            .addOnFailureListener {
                showToast("Daily Goals haven't been saved successfully!")
            }
    }
    private fun saveCategoryToFirestore(categoryName: String, iconResId: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val projectId = db.collection("users").document(userId).collection("projects").document().id

        val category = hashMapOf(
            "categoryName" to categoryName,
            "iconResId" to iconResId // Save the icon resource ID
        )

        db.collection("users").document(userId)
            .collection("projects").document(projectId)
            .collection("categories").add(category)
            .addOnSuccessListener {
                showToast("Category saved successfully!")
            }
            .addOnFailureListener {
                showToast("Failed to save category: ${it.message}")
            }
    }

    private fun getThemeResourceByName(themeName: String): Int {
        return when (themeName) {
            "Louis Vuitton Style" -> pattern_lv
            "Nude Pink Pattern" -> pattern_nude_pink
            "Purple Texture" -> pattern_texture_purple
            "Doiley Pattern" -> pattern_doiley
            "Gold Pattern" -> pattern_gold
            "Floral Pattern" -> pattern_floral
            "Mix Pattern" -> pattern_mix
            "Snow Pattern" -> pattern_snow
            "Red Pattern" -> pattern_red
            "Orange Pattern" -> pattern_orange
            "Purple Pattern" -> pattern_purple
            "Texture Pattern" -> pattern_texture
            "Blue Pattern" -> pattern_blue
            "Pink Pattern" -> pattern_pink
            else -> pattern_lv // Default to LV pattern
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 1000
        private const val CATEGORY_REQUEST_CODE = 1001
        private const val DAILY_GOALS_REQUEST_CODE = 1002
    }
}