package com.dertefter.data.dto.schedule

import java.time.LocalDate

import kotlinx.serialization.Serializable

@Serializable
data class WeekBoundsDto(
    val startDateString: String = "",
    val weekNumber: Int = 0,
){
    fun getStartDate(): LocalDate {
        return LocalDate.parse(startDateString)
    }

    fun getEndDate(): LocalDate {
        return getStartDate().plusDays(6)
    }

}
