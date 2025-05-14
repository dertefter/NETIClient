package com.dertefter.neticlient.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel() {
     val legendaryCardsState = MutableLiveData<Boolean>()
     var scheduleServiceState = MutableLiveData<Boolean>()
     var notifyFutureLessonsState = MutableLiveData<Boolean>()
     var verticalScheduleState= MutableLiveData<Boolean>()
     var cacheMessagesState= MutableLiveData<Boolean>()
     var dashboardTitle = MutableLiveData<String>()
    var notifyValue = MutableLiveData<Int>()

    init {
        loadAllSettings()
    }


    private fun loadAllSettings() {
       viewModelScope.launch {
           scheduleServiceState.postValue(settingsRepository.getScheduleService().first())
           notifyFutureLessonsState.postValue(settingsRepository.getNotifyFutureLessons().first())
           verticalScheduleState.postValue(settingsRepository.getVerticalSchedule().first())
           cacheMessagesState.postValue(settingsRepository.getCacheMessages().first())
           legendaryCardsState.postValue(settingsRepository.getLegendaryCards().first())
           dashboardTitle.postValue(settingsRepository.getDashboardTitle().first())
           notifyValue.postValue(settingsRepository.getNotifyValue().first())

       }
    }

    fun setNotifyValue(state: Int) {
        viewModelScope.launch {
            settingsRepository.setNotifyValue(state)
            notifyValue.value = state
        }
    }

    fun setDashboardTitle(state: String) {
        viewModelScope.launch {
            settingsRepository.setDashboardTitle(state)
            dashboardTitle.value = state
        }
    }


    fun setScheduleService(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScheduleService(state)
            scheduleServiceState.value = state
        }
    }

    fun setLegendaryCards(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setLegendaryCards(state)
            legendaryCardsState.value = state
        }
    }

    fun setNotifyFutureLessons(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifyFutureLessons(state)
            notifyFutureLessonsState.value = state
        }
    }

    fun setMaterialYou(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMaterialYou(state)
        }
    }

    fun setVerticalSchedule(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVerticalSchedule(state)
            verticalScheduleState.value = state
        }
    }

    fun setCacheMessages(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCacheMessages(state)
            cacheMessagesState.value = state
        }
    }



}