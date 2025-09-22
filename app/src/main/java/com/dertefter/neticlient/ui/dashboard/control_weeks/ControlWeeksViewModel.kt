package com.dertefter.neticlient.ui.dashboard.control_weeks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlWeeksViewModel @Inject constructor(
    val netiCore: NETICore
): ViewModel() {


    val controlWeeksFeature = netiCore.controlWeeksFeature

    val controlWeeks = controlWeeksFeature.controlWeeks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val status = controlWeeksFeature.status

    fun updateControlWeeks(){
        viewModelScope.launch {
            controlWeeksFeature.updateControlWeeks()
        }
    }

}