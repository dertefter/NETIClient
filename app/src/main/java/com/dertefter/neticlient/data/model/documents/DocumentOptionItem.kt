package com.dertefter.neticlient.data.model.documents

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentOptionItem(
    val text: String,
    val value: String
): Parcelable
