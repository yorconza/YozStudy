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
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvSubject: TextView = view.findViewById(R.id.tvItemSubject)
        val tvTopic: TextView = view.findViewById(R.id.tvItemTopic)
        val tvDateTime: TextView = view.findViewById(R.id.tvItemDateTime)
        val tvDuration: TextView = view.findViewById(R.id.tvItemDuration)
        val tvPlace: TextView = view.findViewById(R.id.tvItemPlace)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_block, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val item = items[position]
        val block = item.block
        val context = holder.itemView.context

        // 1. Título principal (Topic / Description del formulario, ej: "Class")
        holder.tvTitle.text = block.topic.ifBlank { "Sin título" }

        // 2. Materia (si existe) o Prioridad si no se indicó materia
        holder.tvSubject.text = item.subject?.name?.takeIf { it.isNotBlank() }
            ?: "Prioridad: ${block.priority.name}"

        // 3. Prioridad en la tercera línea para no repetir block.topic
        holder.tvTopic.text = "Prioridad: ${block.priority.name}"

        // 4. Fecha y duración
        val dateFormat = SimpleDateFormat("E dd/MM · HH:mm", Locale.getDefault())
        holder.tvDateTime.text = dateFormat.format(Date(block.startAtMillis))
        holder.tvDuration.text = "${block.durationMinutes} min"

        // 5. Lugar de estudio
        holder.tvPlace.text = item.place?.name?.takeIf { it.isNotBlank() } ?: "Sin lugar"

        // 6. Estado
        holder.tvStatus.text = if (block.status == BlockStatus.DONE) {
            context.getString(R.string.status_done)
        } else {
            context.getString(R.string.status_pending)
        }

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