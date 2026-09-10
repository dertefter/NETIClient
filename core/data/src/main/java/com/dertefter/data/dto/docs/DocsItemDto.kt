package com.dertefter.data.dto.docs


import kotlinx.serialization.Serializable

@Serializable
data class DocsItemDto(
    val type: String = "",
    val date: String? = null,
    val status: String? = null,
    val person: String? = null,
    val comment: String? = null,
    val number: String? = null
)
