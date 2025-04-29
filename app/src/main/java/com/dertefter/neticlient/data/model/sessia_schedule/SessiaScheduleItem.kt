package com.dertefter.neticlient.data.model.sessia_schedule

data class SessiaScheduleItem(
    val name: String,
    val time: String,
    val date: String,
    val type: String,
    val aud: String,
    val personIds: List<String>,
    val dayName: String
)
