package com.dertefter.neticlient.data.model.person

data class Person(
    val name: String,
    val avatarUrl: String?,
    val about_disc: String?,
    val phone: String?,
    val email: String?,
    val address: String?,
    val profiles: String?,
    val disceplines: String?,
    val hasTimetable: Boolean,
)
