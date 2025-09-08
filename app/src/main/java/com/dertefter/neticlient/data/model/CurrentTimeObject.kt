package com.dertefter.neticlient.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime

object CurrentTimeObject {
    private val _currentTimeFlow = MutableStateFlow<LocalTime?>(null)
    val currentTimeFlow: StateFlow<LocalTime?> = _currentTimeFlow

    private val _currentDayFlow = MutableStateFlow<Int?>(null)
    val currentDayFlow: StateFlow<Int?> = _currentDayFlow

    private val _currentDateFlow = MutableStateFlow<LocalDate?>(null)
    val currentDateFlow: StateFlow<LocalDate?> = _currentDateFlow

    fun setCurrentTime(time: LocalTime) {
        _currentTimeFlow.value = time
    }

    fun setCurrentDay(day: Int) {
        _currentDayFlow.value = day
    }

    fun setCurrentDate(date: LocalDate) {
        _currentDateFlow.value = date
    }
}