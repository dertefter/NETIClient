package com.dertefter.neticlient.data.model.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

@Parcelize
data class Time(
    val timeStart: String,
    val timeEnd: String,
    var lessons: List<Lesson>
) : Parcelable {
    fun getTimeStart(): LocalTime = LocalTime.parse(timeStart)
    fun getTimeEnd(): LocalTime = LocalTime.parse(timeEnd)
}
