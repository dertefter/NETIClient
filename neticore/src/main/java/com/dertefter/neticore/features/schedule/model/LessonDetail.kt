package com.dertefter.neticore.features.schedule.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LessonDetail (
    val lesson: Lesson,
    val time: Time? = null,
    val futureOrPastOrNow: FutureOrPastOrNow = FutureOrPastOrNow.FUTURE
) : Parcelable

enum class FutureOrPastOrNow {
    FUTURE,
    PAST,
    NOW
}