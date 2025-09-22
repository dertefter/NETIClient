package com.dertefter.neticore.features.documents.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentRequestItem(
    val is_avail: String?,
    val need_appl: String?,
    val need_pay: String?,
    val need_verify: String?,
    val text_comm: String?,
    val text_doc: String?,
): Parcelable
