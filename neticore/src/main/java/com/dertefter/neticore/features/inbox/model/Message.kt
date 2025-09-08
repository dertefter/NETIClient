package com.dertefter.neticore.features.inbox.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
data class Message(
    @SerializedName("ID") val id: Int,
    @SerializedName("TITTLE") val title: String,
    @SerializedName("TEXT") val text: String,
    @SerializedName("FIO_AUTHOR") val fioAuthor: String,
    @SerializedName("ID_AUTHOR") val idAuthor: Int,
    @SerializedName("ID_STUDENT") val idStudent: Int,
    @SerializedName("IS_READ") val isRead: Int,
    @SerializedName("DATE_READ") val dateRead: String?,
    @SerializedName("IS_DELETED") val isDeleted: Int,
    @SerializedName("DATE_SENT") val dateSent: String,
    @SerializedName("SNAME") val sName: String,
    @SerializedName("IDCATEGORY") val idCategory: Int,
    @SerializedName("SENDER_TYPE") val senderType: Int,
    @SerializedName("PORTRAIT_URL") val portraitUrl: String?,
    @SerializedName("MESSAGE_URL") val messageUrl: String?,
    @SerializedName("WITH_POPUP") val withPopup: Int,
    @SerializedName("WITH_BLOCK") val withBlock: Int
) : Parcelable {

    fun getLocalDateTime(): LocalDateTime? {
        return LocalDateTime.parse(dateSent, DateTimeFormatter.ISO_DATE_TIME)
    }
}
