package com.dertefter.neticlient.ui.schedule.week.day

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.GridSpacingItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.databinding.ItemTimeBinding
import com.google.android.material.color.MaterialColors
import java.time.LocalTime

class TimesAdapter(
    private var timeList: List<Time> = emptyList(),
    private var weekNumber: Int = 0,
    private var dayNumber: Int = 0,
    val fragment: DayFragment,
    var isLegendary: Boolean = false
) : RecyclerView.Adapter<TimesAdapter.TimeViewHolder>() {


    fun putLegendary(new: Boolean){
        isLegendary = new
        notifyDataSetChanged()
    }

    override fun onViewRecycled(holder: TimeViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun setData(timeList: List<Time>, weekNumber: Int, dayNumber: Int) {
        this.weekNumber = weekNumber
        this.dayNumber = dayNumber
        this.timeList = timeList

        fragment.setVisibleNoLessons(timeList.isEmpty())

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val timeItem = timeList[position]

        holder.bind(timeItem, weekNumber, dayNumber, fragment, isLegendary)
    }

    override fun getItemCount(): Int = timeList.size



    class TimeViewHolder(private val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var observer:  Observer<LocalTime>

        fun lessonInPast() {
            itemView.alpha = 0.5f
            binding.progressBar?.apply {
                visibility = View.VISIBLE
                progress = 100f
            }
            binding.horizontalProgress?.progress = 100
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorPrimary))
        }

        fun lessonIsNow(p: Float) {
            itemView.alpha = 1f
            binding.progressBar?.apply {
                visibility = View.VISIBLE
                progress = p
            }
            binding.horizontalProgress?.progress = p.toInt()
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurfaceVariant))
        }

        fun lessonInFuture() {
            itemView.alpha = 1f
            binding.progressBar?.visibility = View.INVISIBLE
            binding.horizontalProgress?.progress = 0
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorOnSurfaceVariant))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurfaceVariant))
        }

        fun bind(
            timeItem: Time,
            weekNumber: Int,
            dayNumber: Int,
            fragment: DayFragment,
            isLegendary: Boolean
        ) {

            if (isLegendary){
                binding.timeContainer.visibility = View.GONE
            } else {
                binding.timeContainer.visibility = View.VISIBLE
            }

            val lessonsAdapter = LessonsAdapter(timeItem.lessons, fragment, timeItem, isLegendary)
            binding.recylerView.adapter = lessonsAdapter
            binding.recylerView.layoutManager = LinearLayoutManager(itemView.context)

            for (decorationIndex in 0 until binding.recylerView.itemDecorationCount){
                binding.recylerView.removeItemDecorationAt(decorationIndex)
            }

            binding.timeStart.text = timeItem.timeStart
            binding.timeEnd.text = timeItem.timeEnd

            val timeStart = timeItem.getTimeStart()
            val timeEnd = timeItem.getTimeEnd()

            observer = Observer<LocalTime> { currentTime ->
                val currentDay = CurrentTimeObject.currentDayLiveData.value ?: return@Observer
                val currentWeek = CurrentTimeObject.currentWeekLiveData.value ?: return@Observer
                if (currentWeek > weekNumber){
                    lessonInPast()
                } else if (currentWeek < weekNumber) {
                    lessonInFuture()
                } else {
                    if (currentDay > dayNumber){
                        lessonInPast()
                        lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.PAST)
                    } else if (currentDay < dayNumber) {
                        lessonInFuture()
                        lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.FUTURE)
                    } else {
                        if (currentTime.isBefore(timeEnd) && currentTime.isAfter(timeStart)){
                            val totalDuration = timeEnd.toSecondOfDay() - timeStart.toSecondOfDay()
                            val currentProgress = currentTime.toSecondOfDay() - timeStart.toSecondOfDay()
                            val progressPercentage = (currentProgress.toFloat() / totalDuration.toFloat()) * 100
                            lessonIsNow(progressPercentage)
                            lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.NOW)
                        } else {
                            if (currentTime.isAfter(timeEnd)){
                                lessonInPast()
                                lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.PAST)
                            } else if (currentTime.isBefore(timeStart)){
                                lessonInFuture()
                                lessonsAdapter.updateFutureOrPastOrNow(FutureOrPastOrNow.FUTURE)
                            }
                        }

                    }
                }
            }

            CurrentTimeObject.currentTimeLiveData.observeForever(observer)
        }

        fun unbind() {
            CurrentTimeObject.currentTimeLiveData.removeObserver(observer)
        }
    }

}