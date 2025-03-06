package com.dertefter.neticlient.data.network.model.auth

data class ResponseFromAuthIdRequest(
    val authId: String,
    val template: String,
    val stage: String,
    val header: String,
    val callbacks: List<Callback>
)
