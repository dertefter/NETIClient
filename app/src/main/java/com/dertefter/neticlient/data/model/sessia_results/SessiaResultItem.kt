package com.dertefter.neticlient.data.model.sessia_results

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessiaResultItem(
    val title: String,
    val date: String?,
    val score: String?,
    val score_five: String,
    val score_ects: String,
    val personWhoPassId: String?,
    val personWhoTeacherId: String?
): Parcelable
