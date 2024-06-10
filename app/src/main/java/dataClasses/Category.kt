package dataClasses

import com.google.firebase.firestore.DocumentSnapshot

data class Category(
    val name: String = "",
    val iconResId: Int = 0, // Assuming you have an icon resource ID for each category
    val documentId: String = "" // Document ID in Firestore

) {
    companion object {
        // Convert a DocumentSnapshot to a Category object
        fun fromSnapshot(snapshot: DocumentSnapshot): Category {
            return Category(
                snapshot.getString("categoryName") ?: "",
                snapshot.getLong("iconResId")?.toInt() ?: 0,
                snapshot.id
            )
        }
    }
}
