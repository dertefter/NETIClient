package com.dertefter.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class LksDto(
    val title: String = "",
    val subtitle: String? = null,
    val id: Int? = null,
    val isSelected: Boolean = false
)
