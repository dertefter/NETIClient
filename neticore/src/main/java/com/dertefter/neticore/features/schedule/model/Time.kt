package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Time(
    val timeStart: String,
    val timeEnd: String,
    val date: String,
    var lessons: List<Lesson>
) : Parcelable {
    fun getTimeStart(): LocalTime = LocalTime.parse(timeStart)
    fun getTimeEnd(): LocalTime = LocalTime.parse(timeEnd)

    fun getLocalDate(): LocalDate {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(date, dateFormatter)
    }
}
