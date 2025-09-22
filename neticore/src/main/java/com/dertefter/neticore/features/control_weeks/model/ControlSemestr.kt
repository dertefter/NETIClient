package com.dertefter.neticore.features.control_weeks.model

data class ControlSemestr(
    val title: String,
    var items: List<ControlWeek>
)
