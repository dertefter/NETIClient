package com.dertefter.neticlient.data.model.messages

data class Message(
    val id: String,
    val title: String,
    val send_by: String,
    val text: String,
    val is_new: Boolean,
    val date: String
)
