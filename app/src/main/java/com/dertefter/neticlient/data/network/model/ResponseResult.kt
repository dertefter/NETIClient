package com.dertefter.neticlient.data.network.model

data class ResponseResult (
    val responseType: ResponseType,
    val message: String = "",
    val data: Any? = null
    )
