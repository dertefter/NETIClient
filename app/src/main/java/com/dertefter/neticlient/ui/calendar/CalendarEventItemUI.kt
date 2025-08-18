package com.dertefter.neticlient.ui.calendar

import com.dertefter.neticlient.data.model.calendar.CalendarEvent
import com.dertefter.neticlient.data.model.schedule.Day
import java.time.LocalDate

data class CalendarEventItemUI(
    val date: LocalDate,
    val scheduleDay: Day?,
    val events: List<CalendarEvent>?
)
