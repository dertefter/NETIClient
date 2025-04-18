package com.dertefter.neticlient.data.model.schedule

data class Week(
    val weekNumber: Int,
    val days: List<Day>
){
    fun getDay(dayNumber: Int): Day? {
        return days.find{it.dayNumber == dayNumber}
    }
}
