package com.dertefter.neticlient.common.calendar_view

data class Event(
    val year: Int,
    val month: Int,
    val day: Int,
    val data: Any? = null
)
