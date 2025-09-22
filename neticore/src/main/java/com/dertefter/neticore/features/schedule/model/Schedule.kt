package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class Schedule (
    val weeks: List<Week>
) : Parcelable {
    fun getWeek(weekNumber: Int): Week? {
        return weeks.find { it.weekNumber == weekNumber }
    }

    fun findNextDayWithLessonsAfter(date: LocalDate?, time: LocalTime?): Day? {
        if (time == null || date == null) return null
        val futureDays = mutableListOf<Day>()
        for (week in weeks) {
            for (day in week.days) {
                val dayDate = day.date?.let { LocalDate.parse(it) } ?: continue
                if (dayDate.isBefore(date)) continue
                if (dayDate.isEqual(date)) {
                    val hasFutureLessons = day.times.any { LocalTime.parse(it.timeEnd).isAfter(time) && it.lessons.isNotEmpty() }
                    if (hasFutureLessons) {
                        return day
                    }
                } else {
                    if (day.getAllLessons().isNotEmpty()) {
                        futureDays.add(day)
                    }
                }
            }
        }

        return futureDays.sortedBy { it.getDate() }.firstOrNull()
    }

    fun getDayForDate(date: LocalDate): Day? {
        for (week in weeks) {
            for (day in week.days){
                if (day.getDate() == date){
                    return day
                }
            }
        }
        return null
    }
}