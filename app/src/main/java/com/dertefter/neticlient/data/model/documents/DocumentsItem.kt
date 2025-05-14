package com.dertefter.neticlient.data.model.documents

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentsItem(
    val type: String?,
    val date: String?,
    val status: String?,
    val person: String?,
    val comment: String?,
    val number: String?
): Parcelable
