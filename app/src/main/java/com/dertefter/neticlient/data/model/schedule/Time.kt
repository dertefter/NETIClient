package com.dertefter.neticlient.data.model.schedule

import java.time.LocalTime

data class Time(
    val timeStart: String,
    val timeEnd: String,
    var lessons: List<Lesson>
) {
    fun getTimeStart(): LocalTime = LocalTime.parse(timeStart)
    fun getTimeEnd(): LocalTime = LocalTime.parse(timeEnd)
}
