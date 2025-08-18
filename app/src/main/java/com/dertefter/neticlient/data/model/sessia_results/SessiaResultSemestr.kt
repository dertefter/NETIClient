package com.dertefter.neticlient.data.model.sessia_results

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaResultSemestr(
    val title: String,
    val items: List<SessiaResultItem>?,
    val srScoreSem: Float? = null
): Parcelable
