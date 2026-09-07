package com.dertefter.data.dto.messsages

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class MessageDto(
    @SerializedName("DATE_READ") @SerialName("DATE_READ") val dateRead: String? = null,

    @SerializedName("DATE_SENT") @SerialName("DATE_SENT") val dateSent: String?,

    @SerializedName("FIO_AUTHOR") @SerialName("FIO_AUTHOR") val fioAuthor: String,

    @SerializedName("ID") @SerialName("ID") val id: Long,

    @SerializedName("IDCATEGORY") @SerialName("IDCATEGORY") val idCategory: Int? = null,

    @SerializedName("ID_AUTHOR") @SerialName("ID_AUTHOR") val idAuthor: Long? = null,

    @SerializedName("ID_STUDENT") @SerialName("ID_STUDENT") val idStudent: Long? = null,

    @SerializedName("IS_DELETED") @SerialName("IS_DELETED") val isDeleted: Int? = 0,
    @SerializedName("IS_READ") @SerialName("IS_READ") val isRead: Int? = 0,

    @SerializedName("MESSAGE_URL") @SerialName("MESSAGE_URL") val messageUrl: String? = null,

    @SerializedName("PORTRAIT_URL") @SerialName("PORTRAIT_URL") val portraitUrl: String? = null,

    @SerializedName("SENDER_TYPE") @SerialName("SENDER_TYPE") val senderType: Int = 1000,

    @SerializedName("SNAME") @SerialName("SNAME") val sname: String? = null,

    @SerializedName("TEXT") @SerialName("TEXT") val text: String,

    @SerializedName("TITTLE") @SerialName("TITTLE") val title: String,

    @SerializedName("WITH_BLOCK") @SerialName("WITH_BLOCK") val withBlock: Int? = null,

    @SerializedName("WITH_POPUP") @SerialName("WITH_POPUP") val withPopup: Int? = null
) {
    fun getLocalDateTime(): LocalDateTime {
        val dateString = dateSent ?: return LocalDateTime.now()

        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: Exception) {
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd.MM.yy"))
                    .atStartOfDay()
            } catch (_: Exception) {
                LocalDateTime.of(1970, 1, 1, 0, 0)
            }
        }
    }
}
