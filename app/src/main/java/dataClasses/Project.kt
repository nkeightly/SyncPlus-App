package dataClasses

data class Project(
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val categoryName: String = "",
    val minHours: String = "",
    val maxHours: String = "",
    val tags: List<String> = emptyList(),
    val date: List<String> = emptyList(),
    val theme: Long = 0L,
    val imageUri: String? = null,
    val imagePath: String = "",
    val iconResId: Int = 0
)
