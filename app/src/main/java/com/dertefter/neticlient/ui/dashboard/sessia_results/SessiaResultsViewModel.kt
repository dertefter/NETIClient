package com.dertefter.neticlient.ui.dashboard.sessia_results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessiaResultsViewModel @Inject constructor(
    val netiCore: NETICore
) : ViewModel() {
    val sessiaResultsFeature = netiCore.sessiaResultsFeature

    val sessiaResults = sessiaResultsFeature.sessiaResults.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val status = sessiaResultsFeature.status

    fun updateSessiaResults(){
        viewModelScope.launch {
            sessiaResultsFeature.updateSesiaResults()
        }
    }

}