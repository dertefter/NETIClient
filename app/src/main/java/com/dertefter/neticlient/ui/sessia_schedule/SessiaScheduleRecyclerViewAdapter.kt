package com.dertefter.neticlient.ui.sessia_schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem

class SessiaScheduleRecyclerViewAdapter(
    private val items: List<SessiaScheduleItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CONSULT = 0
        private const val VIEW_TYPE_EXAM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            "Консультация" -> VIEW_TYPE_CONSULT
            "Экзамен" -> VIEW_TYPE_EXAM
            else -> VIEW_TYPE_CONSULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CONSULT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sessia_schedule_consult, parent, false)
                ConsultViewHolder(view)
            }
            VIEW_TYPE_EXAM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sessia_schedule_consult, parent, false)
                ExamViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ConsultViewHolder -> holder.bind(item)
            is ExamViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class ConsultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val titleTextView: TextView = itemView.findViewById(R.id.title)
        private val typeTextView: TextView = itemView.findViewById(R.id.type)
        private val personLinkTextView: TextView = itemView.findViewById(R.id.personLink)

        fun bind(item: SessiaScheduleItem) {
            timeTextView.text = item.time
            dateTextView.text = item.date
            titleTextView.text = item.name
            typeTextView.text = item.type
            personLinkTextView.text = item.personLink
        }
    }

    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val titleTextView: TextView = itemView.findViewById(R.id.title)
        private val typeTextView: TextView = itemView.findViewById(R.id.type)
        private val personLinkTextView: TextView = itemView.findViewById(R.id.personLink)

        fun bind(item: SessiaScheduleItem) {
            timeTextView.text = item.time
            dateTextView.text = item.date
            titleTextView.text = item.name
            typeTextView.text = item.type
            personLinkTextView.text = item.personLink
        }
    }
}
