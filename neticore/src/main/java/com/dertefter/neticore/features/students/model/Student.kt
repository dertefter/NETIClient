package com.dertefter.neticore.features.students.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class Student(
    @SerializedName("ID")
    val id: Int,

    @SerializedName("ID_CARD")
    val idCard: Int,

    @SerializedName("SURNAME")
    val surname: String,

    @SerializedName("NAME")
    val name: String,

    @SerializedName("PATRONYMIC")
    val patronymic: String,

    @SerializedName("PHOTO")
    val photo: String?,

    @SerializedName("BIRTHDAY")
    val birthday: String?,

    @SerializedName("CIPHER")
    val cipher: String,

    @SerializedName("IS_GROUP_MONITOR")
    val isGroupMonitor: Int,

    @SerializedName("IS_GROUP_MONITOR_DEPUTY")
    val isGroupMonitorDeputy: Int,

    @SerializedName("EMAIL")
    val email: String?,

    @SerializedName("PHONE")
    val phone: String?,

    @SerializedName("DISPACE_PAGE_LINK")
    val dispacePageLink: String?,

    @SerializedName("ADDRESS")
    val address: String?,

    @SerializedName("SNILS")
    val snils: String,

    @SerializedName("VK")
    val vk: String?,

    @SerializedName("TELEGRAM")
    val telegram: String?,

    @SerializedName("LEADER_ID")
    val leaderId: Int?
) : Parcelable
{

    fun getFullName(): String = "$surname $name $patronymic"

    fun getShortName(): String = "$surname ${name.first()}. ${patronymic.first()}."

    fun isMonitor(): Boolean = isGroupMonitor == 1

    fun isMonitorDeputy(): Boolean = isGroupMonitorDeputy == 1

    fun hasPhoto(): Boolean = !photo.isNullOrEmpty()
}

