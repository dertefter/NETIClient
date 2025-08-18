package com.dertefter.neticlient.data.model.sessia_results

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaResults(
    val srScore: Float? = null,
    val semestrs: List<SessiaResultSemestr>?
): Parcelable
