package com.dertefter.neticlient.ui.schedule.week.day

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.WaveDrawable
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Day
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.databinding.ItemTimeBinding
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.LocalTime

class TimesAdapter(
    private val fragment: Fragment,
) : ListAdapter<UiTime, TimesAdapter.TimeViewHolder>(TimeDiffCallback()) {

    private var currentTime: LocalTime? = null
    private var currentDate: LocalDate? = null

    private var date: LocalDate? = null
    private var timesList: List<Time> = emptyList()

    fun setData(day: Day) {
        this.date = day.getDate()
        this.timesList = day.times
    }

    fun clearData() {
        val day = Day(
            dayName = "",
            times = emptyList(),
            dayNumber = 1,
            date = CurrentTimeObject.currentDateLiveData.value.toString()
        )
        this.date = day.getDate()
        this.timesList = day.times
    }


    fun updateTimeAndDate(currentTime: LocalTime, currentDate: LocalDate) {
        val newList = timesList.map { t ->
            val start = t.getTimeStart()!!
            val end   = t.getTimeEnd()!!
            val status: FutureOrPastOrNow
            val progress: Int

            status = when {
                date!! < currentDate         -> FutureOrPastOrNow.PAST
                date!! > currentDate         -> FutureOrPastOrNow.FUTURE
                start <= currentTime && end >= currentTime -> FutureOrPastOrNow.NOW
                start > currentTime          -> FutureOrPastOrNow.FUTURE
                else                         -> FutureOrPastOrNow.PAST
            }

            progress = when(status) {
                FutureOrPastOrNow.NOW -> {
                    val total = end.toSecondOfDay() - start.toSecondOfDay()
                    val passed = currentTime.toSecondOfDay() - start.toSecondOfDay()
                    ((passed.toFloat() / total.toFloat()) * 100).toInt()
                }
                FutureOrPastOrNow.PAST   -> 100
                FutureOrPastOrNow.FUTURE -> 0
            }

            UiTime(t, status, progress)
        }
        submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val uiTime = getItem(position)
        holder.bind(uiTime, fragment)
    }

    class TimeViewHolder(private val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {

        lateinit var lessonsAdapter: LessonsAdapter

        fun bind(uiTime: UiTime, fragment: Fragment) {

            binding.timeStart.text = uiTime.time.timeStart
            binding.timeEnd.text = uiTime.time.timeEnd
            lessonsAdapter = LessonsAdapter(uiTime.time.lessons, fragment, uiTime.time)
            binding.recylerView.adapter = lessonsAdapter
            binding.recylerView.layoutManager = LinearLayoutManager(itemView.context)
            when(uiTime.status) {
                FutureOrPastOrNow.FUTURE -> futureTime()
                FutureOrPastOrNow.PAST -> pastTime()
                FutureOrPastOrNow.NOW -> onGoingTime(uiTime.progress)
            }

            lessonsAdapter.updateFutureOrPastOrNow(uiTime.status)
        }

        fun pastTime(){
            itemView.alpha = 0.5f
            binding.timeStart.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSecondary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSecondary))
            binding.verticalProgressIndicator.progress = 100
            lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.PAST)
        }

        fun futureTime(){
            binding.timeStart.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurfaceVariant))
            binding.timeEnd.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurfaceVariant))
            itemView.alpha = 1f
            binding.verticalProgressIndicator.progress = 0
            lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.FUTURE)
        }

        fun onGoingTime(progress: Int){
            binding.timeStart.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorSecondary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(itemView, com.google.android.material.R.attr.colorOnSurfaceVariant))
            itemView.alpha = 1f

            lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.NOW)
        }

    }

    class TimeDiffCallback : DiffUtil.ItemCallback<UiTime>() {
        override fun areItemsTheSame(oldItem: UiTime, newItem: UiTime): Boolean {
            return oldItem.time.timeStart == newItem.time.timeStart
                    && oldItem.time.timeEnd   == newItem.time.timeEnd
        }
        override fun areContentsTheSame(oldItem: UiTime, newItem: UiTime): Boolean {
            return oldItem.status   == newItem.status
                    && oldItem.progress == newItem.progress && oldItem.time == newItem.time
        }
    }

}