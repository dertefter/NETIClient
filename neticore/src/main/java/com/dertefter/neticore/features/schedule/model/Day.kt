package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Day (
    val dayName: String,
    val times: List<Time>,
    val dayNumber: Int,
    var date: String? = null
) : Parcelable {
    fun getDate(): LocalDate? {
        try{
            return LocalDate.parse(date)
        } catch (e: Exception){
            return null
        }
    }

    fun getAllLessons(): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        for (time in times){
            lessons.addAll(time.lessons)
        }
        return lessons.toList()
    }

}