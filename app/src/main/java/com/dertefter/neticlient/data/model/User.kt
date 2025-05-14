package com.dertefter.neticlient.data.model

data class User(
    val login: String,
    val password: String,
    val name: String,
    val group: String? = null,
    val profilePicPath: String?
)