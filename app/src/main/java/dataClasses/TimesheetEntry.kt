package dataClasses

data class TimesheetEntry(
    val project: Project,
    val hoursLeft: String,
    val isSelected: Boolean
)