package com.dertefter.neticlient.common.calendar_view

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.recyclerview.widget.RecyclerView
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.google.android.material.color.MaterialColors
import java.util.Calendar

class MonthAdapter(
    private val events: List<CalendarEvent>,
    private val baseCalendar: Calendar,
    private val today: Calendar,
    var selectedDate: Calendar?,
    private val onDateClickListener: (Calendar) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    inner class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gridDays: GridLayout = itemView.findViewById(R.id.gridDays)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        return MonthViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val calendar = baseCalendar.clone() as Calendar
        calendar.add(Calendar.MONTH, position - (Int.MAX_VALUE / 2))
        generateDays(holder.gridDays, calendar)
    }

    private fun generateDays(grid: GridLayout, calendar: Calendar) {
        grid.removeAllViews()

        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCalendar.firstDayOfWeek // ← зависит от локали (понедельник или воскресенье)
        val dayOfWeekOfFirstDay = tempCalendar.get(Calendar.DAY_OF_WEEK)

        // Кол-во пустых ячеек перед началом месяца
        var emptyCells = (7 + dayOfWeekOfFirstDay - firstDayOfWeek) % 7
        if (emptyCells == 0) emptyCells = 0 // не добавляем лишнюю строку

        repeat(emptyCells) {
            Log.e("grid is", grid.toString())
            addEmptyDay(grid)
        }

        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            addDay(grid, day, calendar)
        }
    }


    private fun addDay(grid: GridLayout, day: Int, monthCalendar: Calendar) {
        val inflater = LayoutInflater.from(grid.context)
        val dayView = inflater.inflate(R.layout.item_day, grid, false) as FrameLayout

        val tvDay = dayView.findViewById<TextView>(R.id.tvDay)
        tvDay.text = day.toString()

        // Создаем календарь для конкретного дня
        val dayCalendar = monthCalendar.clone() as Calendar
        dayCalendar.set(Calendar.DAY_OF_MONTH, day)

        // Проверка на сегодня
        val isToday = isSameDay(dayCalendar, today)
        // Проверка на выбранный день
        val isSelected = selectedDate?.let { isSameDay(dayCalendar, it) } ?: false

        // Управление индикаторами
        val indicatorToday = dayView.findViewById<View>(R.id.indicator_today_selected)
        val eventIndicator = dayView.findViewById<View>(R.id.eventIndicator)

        indicatorToday.visibility = if (isToday || isSelected) View.VISIBLE else View.GONE
        if (isToday) {
            val tvColor = MaterialColors.getColor(grid.context, com.google.android.material.R.attr.colorOnSecondary,
                Color.CYAN)
            val bgColor = MaterialColors.getColor(grid.context, com.google.android.material.R.attr.colorSecondary,
                Color.CYAN)

            tvDay.setTextColor(tvColor)
            indicatorToday.backgroundTintList = ColorStateList.valueOf(bgColor)
        }
        else if (isSelected) {

            val tvColor = MaterialColors.getColor(grid.context, com.google.android.material.R.attr.colorOnPrimaryContainer,
                Color.CYAN)
            val bgColor = MaterialColors.getColor(grid.context, com.google.android.material.R.attr.colorPrimaryContainer,
                Color.CYAN)

            tvDay.setTextColor(tvColor)
            indicatorToday.backgroundTintList = ColorStateList.valueOf(bgColor)
        }  else {
            val tvColor = MaterialColors.getColor(grid.context, com.google.android.material.R.attr.colorOnSurface,
                Color.CYAN)
            tvDay.setTextColor(tvColor)
        }

        val dayEvents = events.any {
            it.getDate().year == dayCalendar.get(Calendar.YEAR) &&
                    (it.getDate().monthValue) == (dayCalendar.get(Calendar.MONTH) + 1) &&
                    it.getDate().dayOfMonth == dayCalendar.get(Calendar.DAY_OF_MONTH)
        }

        eventIndicator.visibility = if (dayEvents) View.VISIBLE else View.GONE

        // Обработка клика
        dayView.setOnClickListener {
            onDateClickListener.invoke(dayCalendar)
        }

        grid.addView(dayView)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun addEmptyDay(grid: GridLayout) {
        val inflater = LayoutInflater.from(grid.context)
        val emptyDayView = inflater.inflate(R.layout.item_day, grid, false) as FrameLayout

        // Очищаем текст и скрываем индикаторы
        val tvDay = emptyDayView.findViewById<TextView>(R.id.tvDay)
        tvDay.text = ""

        emptyDayView.findViewById<View>(R.id.indicator_today_selected).visibility = View.GONE
        emptyDayView.findViewById<View>(R.id.eventIndicator).visibility = View.GONE

        // Отключаем клики
        emptyDayView.isClickable = false

        grid.addView(emptyDayView)
    }

    override fun getItemCount() = Int.MAX_VALUE
}