package com.syncplus

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

abstract class FirestoreAdapter<VH : RecyclerView.ViewHolder>(
    private val query: Query
) : RecyclerView.Adapter<VH>() {

    private var listener: ListenerRegistration? = null
    private val snapshots = mutableListOf<DocumentSnapshot>()

    init {
        startListening()
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract override fun onBindViewHolder(holder: VH, position: Int)

    override fun getItemCount(): Int = snapshots.size

    private fun startListening() {
        listener = query.addSnapshotListener { snapshots, exception ->
            if (exception != null) {
                // Handle error
                return@addSnapshotListener
            }

            snapshots?.let {
                this.snapshots.clear()
                this.snapshots.addAll(it.documents)
                notifyDataSetChanged()
            }
        }
    }

    private fun stopListening() {
        listener?.remove()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        stopListening()
    }

    fun getSnapshot(position: Int): DocumentSnapshot {
        return snapshots[position]
    }
}
