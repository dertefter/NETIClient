package com.dertefter.neticlient.data.model.schedule

data class Week(
    val weekNumber: Int,
    val days: List<Day>,
    val isCurrent: Boolean = false
)
