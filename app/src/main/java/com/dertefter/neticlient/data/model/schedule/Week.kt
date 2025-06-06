package com.dertefter.neticlient.data.model.schedule

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Week(
    val weekNumber: Int,
    val days: List<Day>
) : Parcelable {
    fun getDay(dayNumber: Int): Day? {
        return days.find{it.dayNumber == dayNumber}
    }
}
