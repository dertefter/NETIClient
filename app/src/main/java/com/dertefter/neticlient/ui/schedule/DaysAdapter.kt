package com.dertefter.neticlient.ui.schedule

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.databinding.ItemDayBinding
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.dertefter.neticore.features.schedule.model.Day
import com.dertefter.neticore.features.schedule.model.Lesson

class DaysAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val onLessonClick: (Lesson) -> Unit,
    private val personDetailFeature: PersonDetailFeature
) : ListAdapter<Day, DaysAdapter.DayViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding, lifecycleOwner, onLessonClick, personDetailFeature)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemDayBinding,
        private val lifecycleOwner: LifecycleOwner,
        private val onLessonClick: (Lesson) -> Unit,
        private val personDetailFeature: PersonDetailFeature
    ) : RecyclerView.ViewHolder(binding.root) {

         private val scheduleTimesAdapter: TimesAdapter

        init {
            scheduleTimesAdapter = TimesAdapter(
                items = emptyList(),
                lifecycleOwner = lifecycleOwner,
                onLessonClick = onLessonClick,
                onCurrentTimeSlotFound = { position -> scrollToPosition(position) },
                onLatestPastTimeSlotFound = { position -> scrollToPosition(position) },
                onFirstFutureTimeSlotFound = { position -> scrollToPosition(position) },
                personDetailFeature = personDetailFeature
            )
            binding.recyclerView.adapter = scheduleTimesAdapter
            binding.recyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.recyclerView.addItemDecoration(
                VerticalSpaceItemDecoration()
            )
        }

        fun bind(day: Day) {
            Log.e("fun bind", day.toString())
            scheduleTimesAdapter.updateData(day.times)
            binding.emptyView.isGone = !day.times.isEmpty()
        }

        private fun scrollToPosition(position: Int) {
            (binding.recyclerView.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Day>() {
        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.dayNumber == newItem.dayNumber && oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem == newItem
        }
    }
}