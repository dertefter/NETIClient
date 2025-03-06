package com.dertefter.neticlient.ui.settings

import androidx.core.graphics.Insets
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.data.repository.SettingsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
): ViewModel() {
     var scheduleServiceState= MutableLiveData<Boolean>()
     var notifyFutureLessonsState = MutableLiveData<Boolean>()
     var materialYouState = MutableLiveData<Boolean>()
     var verticalScheduleState= MutableLiveData<Boolean>()
     var cacheMessagesState= MutableLiveData<Boolean>()
    var insetsViewModel = MutableLiveData<IntArray>()

    init {
        loadAllSettings()
    }


    private fun loadAllSettings() {
       viewModelScope.launch {
           scheduleServiceState.postValue(settingsRepository.getScheduleService().first())
           notifyFutureLessonsState.postValue(settingsRepository.getNotifyFutureLessons().first())
           materialYouState.postValue(settingsRepository.getMaterialYou().first())
           verticalScheduleState.postValue(settingsRepository.getVerticalSchedule().first())
           cacheMessagesState.postValue(settingsRepository.getCacheMessages().first())
       }
    }

    fun setScheduleService(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScheduleService(state)
            scheduleServiceState.value = state
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
            materialYouState.postValue(state)
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