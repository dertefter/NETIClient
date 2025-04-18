package com.dertefter.neticlient.data.model.schedule

data class Lesson(
    val title: String,
    val type: String,
    val aud: String,
    val personIds: MutableList<String>,
    val trigger: LessonTrigger,
    val triggerWeeks: MutableList<Int>,
    val timeStart: String,
    val timeEnd: String
)
