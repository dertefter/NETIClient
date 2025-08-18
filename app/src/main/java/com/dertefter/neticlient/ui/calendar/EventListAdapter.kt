package com.dertefter.neticlient.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticlient.databinding.ItemCalendarEventBinding

class EventListAdapter(
    private val onItemClick: (CalendarEvent) -> Unit
) : ListAdapter<CalendarEvent, EventListAdapter.EventViewHolder>(DiffCallback) {

    inner class EventViewHolder(
        private val binding: ItemCalendarEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: CalendarEvent) {
            binding.title.text = event.title
            binding.root.setOnClickListener { onItemClick(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CalendarEvent>() {
            override fun areItemsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean {
                return oldItem.link == newItem.link && oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean {
                return oldItem == newItem
            }
        }
    }
}
