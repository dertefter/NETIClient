package com.dertefter.neticlient.data.model.schedule

data class Schedule (
    val weeks: List<Week>
){
    fun getWeek(weekNumber: Int): Week? {
        return weeks.find { it.weekNumber == weekNumber }
    }
}