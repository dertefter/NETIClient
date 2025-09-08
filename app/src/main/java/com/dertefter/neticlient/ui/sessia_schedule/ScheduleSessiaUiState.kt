package com.dertefter.neticlient.ui.sessia_schedule

import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.network.model.ResponseType

data class ScheduleSessiaUiState(
    val responseType: ResponseType = ResponseType.LOADING,
    val group: String? = null,
    val schedule: List<SessiaScheduleItem>? = null
)