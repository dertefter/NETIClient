package com.dertefter.data.dto.sessia_results

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessiaResultDto(

    @SerializedName("CONFIRM_DATE")
    @SerialName("CONFIRM_DATE")
    val confirmDate: String? = null,

    @SerializedName("CREDITS")
    @SerialName("CREDITS")
    val credits: Int? = null,

    @SerializedName("DISMISS_DATE")
    @SerialName("DISMISS_DATE")
    val dismissDate: String? = null,

    @SerializedName("EUROPEAN_MARK_NUMBER")
    @SerialName("EUROPEAN_MARK_NUMBER")
    val europeanMarkNumber: Int? = null,

    @SerializedName("EUROPEAN_MARK_STRING")
    @SerialName("EUROPEAN_MARK_STRING")
    val europeanMarkString: String? = null,

    @SerializedName("EXAM_DATE")
    @SerialName("EXAM_DATE")
    val examDate: String? = null,

    @SerializedName("FIO_PRACT")
    @SerialName("FIO_PRACT")
    val fioPract: String? = null,

    @SerializedName("HOURS")
    @SerialName("HOURS")
    val hours: Int? = null,

    @SerializedName("IS_ZACH_DIFF")
    @SerialName("IS_ZACH_DIFF")
    val isZachDiff: Int? = null,

    @SerializedName("KP_THEME")
    @SerialName("KP_THEME")
    val kpTheme: String? = null,

    @SerializedName("MARK")
    @SerialName("MARK")
    val mark: Int? = null,

    @SerializedName("MARK_NAME")
    @SerialName("MARK_NAME")
    val markName: String? = null,

    @SerializedName("NAME")
    @SerialName("NAME")
    val name: String? = null,

    @SerializedName("PLACE_PRACT")
    @SerialName("PLACE_PRACT")
    val placePract: String? = null,

    @SerializedName("SCORE")
    @SerialName("SCORE")
    val score: Int? = null,

    @SerializedName("SEMESTER")
    @SerialName("SEMESTER")
    val semester: Int = 0,

    @SerializedName("TYPE_NAME")
    @SerialName("TYPE_NAME")
    val typeName: String? = null
)
