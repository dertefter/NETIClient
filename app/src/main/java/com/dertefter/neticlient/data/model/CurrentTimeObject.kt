package com.dertefter.neticlient.data.model

import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import java.time.LocalTime

object CurrentTimeObject {
    val currentTimeLiveData = MutableLiveData<LocalTime>()
    val currentDayLiveData = MutableLiveData<Int>()
    val currentDateLiveData = MutableLiveData<LocalDate>()
}