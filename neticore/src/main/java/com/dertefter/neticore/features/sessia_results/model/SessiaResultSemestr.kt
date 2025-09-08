package com.dertefter.neticore.features.sessia_results.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaResultSemestr(
    val title: String,
    val items: List<SessiaResultItem>?,
    val srScoreSem: Float? = null
): Parcelable