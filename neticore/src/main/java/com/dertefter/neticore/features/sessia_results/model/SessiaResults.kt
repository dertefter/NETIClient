package com.dertefter.neticore.features.sessia_results.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaResults(
    val srScore: Float? = null,
    val semestrs: List<SessiaResultSemestr>?
): Parcelable