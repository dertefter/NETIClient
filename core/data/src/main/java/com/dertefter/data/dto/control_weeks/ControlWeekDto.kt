package com.dertefter.data.dto.control_weeks

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ControlWeekDto(

    @SerializedName("CONTROL_WEEK_TITLE")
    @SerialName("CONTROL_WEEK_TITLE")
    val title: String = "",

    @SerializedName("CONTROL_WEEK_SEM")
    @SerialName("CONTROL_WEEK_SEM")
    val semester: String = "",

    @SerializedName("CONTROL_WEEK_NUMBER")
    @SerialName("CONTROL_WEEK_NUMBER")
    val week: String = "",

    @SerializedName("CONTROL_WEEK_VALUE")
    @SerialName("CONTROL_WEEK_VALUE")
    val value: String = ""
)
