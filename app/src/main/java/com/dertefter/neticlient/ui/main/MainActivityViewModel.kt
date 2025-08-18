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


    var lastShownAuthState: AuthState? = null


    fun startUpdatingTime() {
        viewModelScope.launch {
            while (true) {
                updateTime()
                delay(1000)
            }
        }
        viewModelScope.launch {
            while (true) {
                updateDay()
                delay(1000)
            }
        }
    }

    private fun updateTime() {
        CurrentTimeObject.currentTimeLiveData.postValue(LocalTime.now())
    }

    private fun updateDay() {
        val now = LocalDate.now()
        CurrentTimeObject.currentDayLiveData.postValue(now.dayOfWeek.value)
        CurrentTimeObject.currentDateLiveData.postValue(now)
    }
}