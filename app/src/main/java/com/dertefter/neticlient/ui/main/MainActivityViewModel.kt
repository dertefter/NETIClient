package com.dertefter.neticlient.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.model.AuthState
import com.dertefter.neticlient.data.model.CurrentTimeObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(): ViewModel() {

    fun startUpdatingTime() {
        viewModelScope.launch {
            while (true) {
                updateTime()
                delay(3000)
            }
        }
        viewModelScope.launch {
            while (true) {
                updateDay()
                delay(3000)
            }
        }
    }

    private fun updateTime() {
        CurrentTimeObject.setCurrentTime(LocalTime.now())
    }

    private fun updateDay() {
        val now = LocalDate.now()
        CurrentTimeObject.setCurrentDay(now.dayOfWeek.value)
        CurrentTimeObject.setCurrentDate(now)
    }
}