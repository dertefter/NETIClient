package com.dertefter.neticlient.data.model.schedule

data class LessonDetail (
    val lesson: Lesson,
    val time: Time? = null,
    val futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE
)

enum class FutureOrPastOrNow {
    FUTURE,
    PAST,
    NOW
}