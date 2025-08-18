package com.dertefter.neticlient.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    var scheduleServiceState: StateFlow<Boolean>  = settingsRepository.getScheduleService().stateIn(
        scope = viewModelScope,           // или любой другой CoroutineScope
        started = SharingStarted.Eagerly, // или WhileSubscribed(), Lazily()
        initialValue = true       // значение по умолчанию
    )
    var notifyFutureLessonsState = settingsRepository.getNotifyFutureLessons()
    var notifyFutureLessonsValueState = settingsRepository.getNotifyValue()
    var materialYouState = settingsRepository.getMaterialYou()

    var onBoardingState = settingsRepository.getOnBoarding()
    val isGrantedPermission: MutableStateFlow<Boolean> = MutableStateFlow(true)

    fun updateNotificationPermissionState(context: Context) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        viewModelScope.launch {
            isGrantedPermission.value = granted
            if (!granted){
                setScheduleService(false)
                setNotifyFutureLessons(false)
            }
        }

    }

    fun setOnBoarding(v: Boolean = false){
        viewModelScope.launch {
            settingsRepository.setOnBoarding(v)
        }
    }

    fun setNotifyValue(state: Int) {
        viewModelScope.launch {
            settingsRepository.setNotifyValue(state)
        }
    }

    fun setScheduleService(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setScheduleService(state)
            if (state == false){
                settingsRepository.setNotifyFutureLessons(false)
            }
        }
    }

    fun setNotifyFutureLessons(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifyFutureLessons(state)
        }
    }

    fun setMaterialYou(state: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMaterialYou(state)
        }
    }
}
