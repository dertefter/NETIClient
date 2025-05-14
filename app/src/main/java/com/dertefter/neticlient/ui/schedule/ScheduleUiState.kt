package com.dertefter.neticlient.ui.schedule

import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.network.model.ResponseType

data class ScheduleUiState(
    val responseType: ResponseType = ResponseType.LOADING,
    val group: String? = null,
    val schedule: Schedule? = null
)