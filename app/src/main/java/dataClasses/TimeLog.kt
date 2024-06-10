package dataClasses

import com.google.firebase.Timestamp

data class TimeLog(
    val timeLogId: String = "",
    val projectId: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val elapsedTimeHours: Double = 0.0,
    val timestamp: Timestamp? = null // Use Firestore's Timestamp
) {
    constructor() : this("", "", 0, 0, 0.0)
}
