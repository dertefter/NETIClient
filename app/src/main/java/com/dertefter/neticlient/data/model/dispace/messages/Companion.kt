package com.dertefter.neticlient.data.model.dispace.messages

import kotlinx.serialization.Serializable

@Serializable
data class Companion(
    val companionId: String,
    val surname: String,
    val name: String,
    val patronymic: String,
    val date: String,
    val groupTitle: String,
    val isNew: String,
    val isRead: String,
    val lastId: String,
    val lastIsMine: String,
    val lastMsg: String,
    val lastMsgDate: String,
    val lastMsgId: String,
    val photo: String,
    val userId: String,
    val userOnline: String,
    val wColor: String,
    val wTitle: String,
    val workspaceId: String
)