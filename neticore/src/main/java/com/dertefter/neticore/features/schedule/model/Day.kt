package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class Day (
    val dayName: String,
    val times: List<Time>,
    val dayNumber: Int,
    var date: String? = null,
    var weekNumber: Int? = null
) : Parcelable {
    fun getDate(): LocalDate? {
        return try{
            LocalDate.parse(date)
        } catch (e: Exception){
            null
        }
    }

    fun getAllLessons(): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        for (time in times){
            lessons.addAll(time.lessons)
        }
        return lessons.toList()
    }


    fun findNextLesson(date: LocalDate?, localTime: LocalTime?): Lesson? {
        if (localTime == null || date == null) return null
        for (time in times){
            if (time.getLocalDate().isAfter(date)) return time.lessons.first()
            else if (time.getTimeStart().isBefore(localTime) && time.getTimeEnd().isAfter(localTime)) return  time.lessons.first()
            else if (time.getTimeStart().isAfter(localTime)) return time.lessons.first()
        }
        return null
    }



}