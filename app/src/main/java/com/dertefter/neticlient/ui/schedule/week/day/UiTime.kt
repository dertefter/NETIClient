package com.dertefter.neticlient.ui.schedule.week.day

import com.dertefter.neticlient.data.model.schedule.FutureOrPastOrNow
import com.dertefter.neticlient.data.model.schedule.Time

data class UiTime(
    val time: Time,
    val status: FutureOrPastOrNow,
    val progress: Int
)
