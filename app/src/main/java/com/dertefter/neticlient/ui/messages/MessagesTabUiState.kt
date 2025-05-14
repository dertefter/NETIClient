package com.dertefter.neticlient.ui.messages

import com.dertefter.neticlient.data.model.messages.Message
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.sessia_schedule.SessiaScheduleItem
import com.dertefter.neticlient.data.network.model.ResponseType

data class MessagesTabUiState(
    val responseType: ResponseType = ResponseType.LOADING,
    val messages: List<Message>? = null
)