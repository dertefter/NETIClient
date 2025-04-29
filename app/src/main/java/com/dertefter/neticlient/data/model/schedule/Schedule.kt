package com.dertefter.neticlient.data.model.schedule

import java.time.LocalDate
import java.time.LocalTime

data class Schedule (
    val weeks: List<Week>
){
    fun getWeek(weekNumber: Int): Week? {
        return weeks.find { it.weekNumber == weekNumber }
    }

    fun findNextDayWithLessonsAfter(weekNumber: Int, date: LocalDate, time: LocalTime): Pair<Int, Int>? {
        val futureDays = mutableListOf<Pair<Int, Day>>()
        for (week in weeks) {
            if (week.weekNumber < weekNumber) continue

            for (day in week.days) {
                val dayDate = day.date?.let { LocalDate.parse(it) } ?: continue
                if (dayDate.isBefore(date)) continue
                if (dayDate.isEqual(date)) {
                    val hasFutureLessons = day.times.any { LocalTime.parse(it.timeEnd).isAfter(time) && it.lessons.isNotEmpty() }
                    if (hasFutureLessons) {
                        return Pair(day.dayNumber, week.weekNumber)
                    }
                } else {
                    if (day.getAllLessons().isNotEmpty()) {
                        futureDays.add(Pair(week.weekNumber, day))
                    }
                }
            }
        }

        return futureDays
            .sortedBy { LocalDate.parse(it.second.date) }
            .firstOrNull()
            ?.let { Pair(it.second.dayNumber, it.first) }
    }



}