package com.dertefter.neticlient.data.model.calendar

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class CalendarEvent(
    val title: String,
    val link: String,
    val date: String
) : Parcelable {
    fun getDate(): LocalDate {
        return LocalDate.parse(date)
    }
}

