package com.dertefter.neticlient.ui.calendar

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.common.item_decoration.VerticalSpaceItemDecoration
import com.dertefter.neticlient.data.model.CurrentTimeObject
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticore.features.schedule.model.Schedule
import com.dertefter.neticlient.databinding.ItemCalendarDayItemBinding
import com.dertefter.neticlient.ui.schedule.week.day.TimesAdapter
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class CalendarDayListAdapter(
    private val onItemClick: (CalendarEventItemUI) -> Unit,
    val fragment: Fragment,
) : ListAdapter<CalendarEventItemUI, CalendarDayListAdapter.EventViewHolder>(DiffCallback()) {


    private class DiffCallback : DiffUtil.ItemCallback<CalendarEventItemUI>() {
        override fun areItemsTheSame(oldItem: CalendarEventItemUI, newItem: CalendarEventItemUI): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarEventItemUI, newItem: CalendarEventItemUI): Boolean {
            return oldItem == newItem
        }
    }

    private var dates: List<LocalDate>? = null
    private var schedule: Schedule? = null

    private var filterEvents = true
    private var filterSchedule = true


    private var events: List<CalendarEvent>? = null

    fun submitData(days: List<CalendarEventItemUI>) {
        submitList(days)
    }

    fun updateSchedule(schedule: Schedule?){
        this.schedule = schedule
        update()
    }

    fun findPositionByDate(calendar: Calendar): Int {
        val localDate = calendar.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return currentList.indexOfFirst { it.date == localDate }
    }

    fun updateDates(dates: List<LocalDate>?){
        this.dates = dates
        update()
    }

    fun updateFilterEvents(b: Boolean){
        this.filterEvents = b
        update()
    }

    fun updateFilterSchedule(b: Boolean){
        this.filterSchedule = b
        update()
    }

    fun updateEvents(events: List<CalendarEvent>?){
        this.events = events
        update()
    }

    fun update(){
        val combinded = combinde()
        submitData(combinded)
    }

    fun combinde(): List<CalendarEventItemUI> {
        val out = mutableListOf<CalendarEventItemUI>()
        if (dates != null) {
            for (date in dates){
                val scheduleDay = if (schedule != null && filterSchedule){
                     schedule!!.getDayForDate(date)
                } else { null }

                val events = if (filterEvents) events?.filter {
                    it.getDate() == date
                } else {
                    emptyList()
                }

                out.add(
                    CalendarEventItemUI(
                        date,
                        scheduleDay = scheduleDay,
                        events
                    )
                )

            }
        }
        return out
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemCalendarDayItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemCalendarDayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(it: CalendarEventItemUI) {
            val dayOfWeekFormatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())
            val dayOfMonthFormatter = DateTimeFormatter.ofPattern("d")

            val shortDayOfWeek = it.date.format(dayOfWeekFormatter).lowercase()
            val dayOfMonth = it.date.format(dayOfMonthFormatter)

            binding.dayOfMonth.text = dayOfMonth
            binding.dayShortName.text = shortDayOfWeek

            if (it.date == LocalDate.now()){
                val cardBg = MaterialColors.getColor(binding.root.context, com.google.android.material.R.attr.colorSecondaryContainer, Color.GRAY)
                val titleColor = MaterialColors.getColor(binding.root.context, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.GRAY)
                binding.container.backgroundTintList = (ColorStateList.valueOf(cardBg))
                binding.dayOfMonth.setTextColor(titleColor)
                binding.dayShortName.setTextColor(titleColor)

            }else{
                val cardBg = MaterialColors.getColor(binding.root.context, com.google.android.material.R.attr.colorSurfaceContainer, Color.GRAY)
                val titleColor = MaterialColors.getColor(binding.root.context, com.google.android.material.R.attr.colorOnSurface, Color.GRAY)
                binding.container.backgroundTintList = (ColorStateList.valueOf(cardBg))
                binding.dayOfMonth.setTextColor(titleColor)
                binding.dayShortName.setTextColor(titleColor)
            }


            val eventsAdapter = EventListAdapter{ it ->
                val link = it.link
                Log.e("lll", link)
                if (link.contains("/news/news_more?idnews=")){
                    val id  = link.replace("/news/news_more?idnews=", "")
                    (fragment as CalendarFragment).openNewsDetail(
                        id
                    )
                } else if (link.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW,
                        link.toUri())
                    fragment.startActivity(intent)
                }



            }

            binding.events.adapter = eventsAdapter
            binding.events.layoutManager = LinearLayoutManager(fragment.requireContext())

            if (binding.events.itemDecorationCount == 0){
                binding.events.addItemDecoration(
                    VerticalSpaceItemDecoration( R.dimen.margin_max, R.dimen.margin_micro)
                )
            }

            binding.scheduleTimes.layoutManager = LinearLayoutManager(fragment.requireContext())

            val day = it.scheduleDay
            val events = it.events


            if (events != null){
                eventsAdapter.submitList(events)
            }
        }
    }
}
