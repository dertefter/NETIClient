package com.dertefter.neticlient.ui.schedule.week.day

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.databinding.ItemTimeBinding
import com.google.android.material.color.MaterialColors
import java.time.LocalTime

class DayRecyclerViewAdapter(
    private var timeList: List<Time> = emptyList(),
    private var weekNumber: Int = 0,
    private var dayNumber: Int = 0,
    val fragment: DayFragment
) : RecyclerView.Adapter<DayRecyclerViewAdapter.TimeViewHolder>() {

    override fun onViewRecycled(holder: TimeViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    fun setData(timeList: List<Time>, weekNumber: Int, dayNumber: Int) {
        this.weekNumber = weekNumber
        this.dayNumber = dayNumber

        val filteredTimeList = timeList.mapNotNull { timeItem ->
            val filteredLessons = timeItem.lessons.filter { lesson ->
                when (lesson.trigger) {
                    LessonTrigger.ALL -> true
                    LessonTrigger.EVEN -> weekNumber % 2 == 0
                    LessonTrigger.ODD -> weekNumber % 2 != 0
                    LessonTrigger.CUSTOM -> lesson.triggerWeeks.contains(weekNumber)
                }
            }

            if (filteredLessons.isNotEmpty()) {
                timeItem.copy(lessons = filteredLessons)  // Создаём новый объект
            } else {
                null  // Исключаем из списка
            }
        }

        this.timeList = filteredTimeList

        fragment.setVisibleNoLessons(filteredTimeList.isEmpty())

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = ItemTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val timeItem = timeList[position]

        holder.bind(timeItem, weekNumber, dayNumber, fragment)
    }

    override fun getItemCount(): Int = timeList.size



    class TimeViewHolder(private val binding: ItemTimeBinding) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var observer:  Observer<LocalTime>

        fun lessonInPast() {
            itemView.alpha = 0.5f
            binding.progressBar.apply {
                visibility = View.VISIBLE
                progress = 100f
            }
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorPrimary))
        }

        fun lessonIsNow(p: Float) {
            itemView.alpha = 1f
            binding.progressBar.apply {
                visibility = View.VISIBLE
                progress = p
            }
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorPrimary))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurface))
        }

        fun lessonInFuture() {
            itemView.alpha = 1f
            binding.progressBar.visibility = View.INVISIBLE
            binding.timeStart.setTextColor(MaterialColors.getColor(binding.timeStart, com.google.android.material.R.attr.colorOnSurface))
            binding.timeEnd.setTextColor(MaterialColors.getColor(binding.timeEnd, com.google.android.material.R.attr.colorOnSurface))
        }

        fun bind(
            timeItem: Time,
            weekNumber: Int,
            dayNumber: Int,
            fragment: DayFragment
        ) {
            val adapter = TimeRecyclerViewAdapter(timeItem.lessons, fragment, timeItem)
            binding.recylerView.adapter = adapter
            binding.recylerView.layoutManager = LinearLayoutManager(itemView.context)

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
                        adapter.setIsNow(false)
                    } else if (currentDay < dayNumber) {
                        lessonInFuture()
                        adapter.setIsNow(false)
                    } else {
                        if (currentTime.isBefore(timeEnd) && currentTime.isAfter(timeStart)){
                            val totalDuration = timeEnd.toSecondOfDay() - timeStart.toSecondOfDay()
                            val currentProgress = currentTime.toSecondOfDay() - timeStart.toSecondOfDay()
                            val progressPercentage = (currentProgress.toFloat() / totalDuration.toFloat()) * 100
                            lessonIsNow(progressPercentage)
                            adapter.setIsNow(true)
                        } else {
                            if (currentTime.isAfter(timeEnd)){
                                lessonInPast()
                                adapter.setIsNow(false)
                            } else if (currentTime.isBefore(timeStart)){
                                lessonInFuture()
                                adapter.setIsNow(false)
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