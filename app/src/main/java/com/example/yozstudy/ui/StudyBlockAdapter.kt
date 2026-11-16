package com.example.yozstudy.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yozstudy.R
import com.example.yozstudy.domain.model.BlockStatus
import com.example.yozstudy.domain.model.StudyBlockWithDetails
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyBlockAdapter(
    private var items: List<StudyBlockWithDetails>,
    private val onBlockClick: (StudyBlockWithDetails) -> Unit
) : RecyclerView.Adapter<StudyBlockAdapter.BlockViewHolder>() {

    class BlockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubject: TextView = view.findViewById(R.id.tvItemSubject)
        val tvTopic: TextView = view.findViewById(R.id.tvItemTopic)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvDuration: TextView = view.findViewById(R.id.tvItemDuration)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_block_card, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val item = items[position]
        val block = item.block

        // Access the subject name securely from item.subject
        holder.tvSubject.text = item.subject?.name ?: "No subject"
        holder.tvTopic.text = block.topic

        // Format date and duration
        val dateFormat = SimpleDateFormat("E dd/MM · HH:mm", Locale.getDefault())
        holder.tvDateTime.text = dateFormat.format(Date(block.startAtMillis))
        holder.tvDuration.text = "${block.durationMinutes} min"

        // Set status text
        holder.tvStatus.text = if (block.status == BlockStatus.DONE) "Done" else "Pending"

        // Handle card click to open detail screen
        holder.itemView.setOnClickListener {
            onBlockClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<StudyBlockWithDetails>) {
        items = newItems
        notifyDataSetChanged()
    }
}