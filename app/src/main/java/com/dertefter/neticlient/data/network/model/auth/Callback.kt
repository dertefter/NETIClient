package com.dertefter.neticlient.data.network.model.auth


data class Callback(
    val type: String,
    val output: List<Output>,
    val input: List<Input>
)
