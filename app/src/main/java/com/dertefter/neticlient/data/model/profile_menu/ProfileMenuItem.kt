package com.dertefter.neticlient.data.model.profile_menu

data class ProfileMenuItem(
    val title: String? = null,
    val iconResId: Int? = null,
    val navigateToDestination: Int? = null,
    val enabled: Boolean = true
)