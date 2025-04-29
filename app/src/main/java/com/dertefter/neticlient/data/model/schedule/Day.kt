package com.dertefter.neticlient.data.model.schedule

import java.time.LocalDate

data class Day (
    val dayName: String,
    val times: List<Time>,
    val dayNumber: Int,
    var date: String? = null
){
    fun getDate(): LocalDate = LocalDate.parse(date)

    fun getAllLessons(): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        for (time in times){
            lessons.addAll(time.lessons)
        }
        return lessons.toList()
    }

}