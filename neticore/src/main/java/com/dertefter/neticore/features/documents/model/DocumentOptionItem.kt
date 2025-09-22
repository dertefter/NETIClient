package com.dertefter.neticore.features.documents.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentOptionItem(
    val text: String,
    val value: String
): Parcelable
