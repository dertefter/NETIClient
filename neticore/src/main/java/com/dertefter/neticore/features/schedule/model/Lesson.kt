package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Lesson(
    val title: String,
    val type: String,
    val aud: String,
    val personIds: MutableList<String>,
    val trigger: LessonTrigger,
    val triggerWeeks: MutableList<Int>,
    val timeStart: String,
    val timeEnd: String,
    var date: String, //2025-09-02
) : Parcelable {
    fun getTimeStart(): LocalTime? = LocalTime.parse(timeStart)
    fun getTimeEnd(): LocalTime? = LocalTime.parse(timeEnd)
    fun getLocalDate(): LocalDate {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.parse(date, dateFormatter)
    }
}
