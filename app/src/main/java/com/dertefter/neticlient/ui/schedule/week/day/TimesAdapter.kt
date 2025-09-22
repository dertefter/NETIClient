package com.dertefter.neticlient.ui.schedule.week.day

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.databinding.ItemTimeBinding
import com.dertefter.neticore.features.person_detail.PersonDetailFeature
import com.dertefter.neticore.features.schedule.model.Lesson
import com.dertefter.neticore.features.schedule.model.Time
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TimesAdapter(
    private var items: List<Time>,
    val lifecycleOwner: LifecycleOwner,
    private val onLessonClick: (Lesson) -> Unit,
    private val onCurrentTimeSlotFound: (y: Int) -> Unit,
    private val onLatestPastTimeSlotFound: (y: Int) -> Unit,
    private val onFirstFutureTimeSlotFound: (y: Int) -> Unit,
    private val personDetailFeature: PersonDetailFeature
) : RecyclerView.Adapter<TimesAdapter.TimeViewHolder>() {

    private var recyclerView: RecyclerView? = null

    init {
        findBoundaryTimeSlots()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    private fun findBoundaryTimeSlots() {
        lifecycleOwner.lifecycleScope.launch {
            combine(
                CurrentTimeObject.currentTimeFlow,
                CurrentTimeObject.currentDateFlow
            ) { currentTime, currentDate ->
                Pair(currentTime, currentDate)
            }.collect { (currentTime, currentDate) ->
                if (currentTime != null && currentDate != null && items.isNotEmpty()) {
                    var latestPastIndex = -1
                    var firstFutureIndex = -1
                    var nowIndex = -1

                    fun setAsFutureIndex(index: Int) {
                        if (firstFutureIndex == -1) {
                            firstFutureIndex = index
                        }
                    }

                    fun setAsPastIndex(index: Int) {
                        latestPastIndex = index
                    }

                    fun setAsNowIndex(index: Int) {
                        nowIndex = index
                    }


                    for ((index, timeItem) in items.withIndex()) {
                        try {
                            val startTime = timeItem.getTimeStart()
                            val endTime = timeItem.getTimeEnd()
                            val itemDate = timeItem.getLocalDate()

                            if (currentDate.isBefore(timeItem.getLocalDate())) {
                                setAsFutureIndex(index)
                            } else if (currentDate.isAfter(timeItem.getLocalDate())) {
                                setAsPastIndex(index)
                            } else {
                                when {
                                    currentTime.isBefore(startTime) -> {
                                        setAsFutureIndex(index)
                                    }

                                    currentTime.isAfter(endTime) -> {
                                        setAsPastIndex(index)
                                    }

                                    else -> setAsNowIndex(index)

                                }
                            }

                        } catch (e: Exception) {
                            // Ignore items that can't be parsed
                        }
                    }

                    val latestPastTop = recyclerView
                        ?.findViewHolderForAdapterPosition(latestPastIndex)
                        ?.itemView
                        ?.top ?: -1
                    onLatestPastTimeSlotFound(latestPastTop)

                    val firstFutureTop = recyclerView
                        ?.findViewHolderForAdapterPosition(firstFutureIndex)
                        ?.itemView
                        ?.top ?: -1
                    onFirstFutureTimeSlotFound(firstFutureTop)

                    val nowTop = recyclerView
                        ?.findViewHolderForAdapterPosition(nowIndex)
                        ?.itemView
                        ?.top ?: -1
                    onCurrentTimeSlotFound(nowTop)
                }
            }
        }
    }


    inner class TimeViewHolder(val binding: ItemTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var timeUpdateJob: Job? = null

        fun bind(item: Time) {
            binding.timeStart.text = item.timeStart
            binding.timeEnd.text = item.timeEnd

            val corners = when (bindingAdapterPosition) {
                itemCount - 1 -> 2
                0 -> 0
                else -> 1
            }

            val adapter = LessonsAdapter(item.lessons, lifecycleOwner, corners, personDetailFeature, onLessonClick)
            binding.recylerView.adapter = adapter
            binding.recylerView.layoutManager = LinearLayoutManager(binding.root.context)

            subscribeToTimeUpdates(item)
        }

        private fun subscribeToTimeUpdates(timeItem: Time) {
            timeUpdateJob?.cancel()

            timeUpdateJob = lifecycleOwner.lifecycleScope.launch {
                combine(
                    CurrentTimeObject.currentTimeFlow,
                    CurrentTimeObject.currentDateFlow
                ) { currentTime, currentDate ->
                    Pair(currentTime, currentDate)
                }.collect { (currentTime, currentDate) ->
                    if (currentTime != null && currentDate != null) {
                        updateProgressBasedOnTime(timeItem, currentTime, currentDate)
                    }
                }
            }
        }


        private fun updateProgressBasedOnTime(
            timeItem: Time,
            currentTime: LocalTime,
            currentDate: LocalDate
        ) {
            try {
                val startTime = timeItem.getTimeStart()
                val endTime = timeItem.getTimeEnd()

                if (currentDate.isBefore(timeItem.getLocalDate())) {
                    onFuture()
                } else if (currentDate.isAfter(timeItem.getLocalDate())) {
                    onPast()
                } else {
                    when {
                        currentTime.isBefore(startTime) -> {
                            onFuture()
                        }

                        currentTime.isAfter(endTime) -> {
                            onPast()
                        }

                        else -> {
                            val progress = calculateProgress(startTime, endTime, currentTime)
                            onNow(progress)
                        }
                    }
                }

            } catch (e: Exception) {
                onFuture()
                Log.e("updateProgressBasedOnTime", e.stackTraceToString())
            }
        }

        private fun calculateProgress(
            startTime: LocalTime,
            endTime: LocalTime,
            currentTime: LocalTime
        ): Int {
            val totalMinutes = startTime.until(endTime, java.time.temporal.ChronoUnit.MINUTES)
            val elapsedMinutes =
                startTime.until(currentTime, java.time.temporal.ChronoUnit.MINUTES)

            return if (totalMinutes > 0) {
                ((elapsedMinutes.toDouble() / totalMinutes.toDouble()) * 100).toInt()
                    .coerceIn(0, 100)
            } else {
                0
            }
        }

        fun onPast() {
            binding.verticalProgressIndicator.setProgress(100, true)
            binding.verticalProgressIndicator.setIndicatorColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
            )
            binding.timeStart.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
            )
            binding.timeEnd.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
            )
        }

        fun onFuture() {
            binding.timeStart.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
            binding.timeEnd.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
            binding.verticalProgressIndicator.setProgress(0, true)
            binding.verticalProgressIndicator.setIndicatorColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSurfaceVariant
                )
            )
        }

        fun onNow(progress: Int) {
            binding.timeStart.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSecondary
                )
            )
            binding.timeEnd.setTextColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
            binding.verticalProgressIndicator.setIndicatorColor(
                MaterialColors.getColor(
                    itemView,
                    com.google.android.material.R.attr.colorSecondary
                )
            )
            binding.verticalProgressIndicator.setProgress(progress, true)
        }

        fun unbind() {
            timeUpdateJob?.cancel()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = ItemTimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun onViewRecycled(holder: TimeViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Time>) {
        items = newItems
        notifyDataSetChanged()
    }
}

