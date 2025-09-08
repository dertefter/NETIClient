package com.dertefter.neticlient.ui.dashboard.sessia_results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import kotlinx.coroutines.launch

class SessiaResultsViewModel(): ViewModel() {


    val sessiaResultsFeature = NETICore.sessiaResultsFeature

    val sessiaResults = sessiaResultsFeature.sessiaResults
    val sessiaResultsMobile = sessiaResultsFeature.sessiaResultsMobile

    val status = NETICore.userDetailFeature.status
    val statusMobile = NETICore.userDetailFeature.statusMobile

    fun updateSessiaResults(){
        viewModelScope.launch {
            sessiaResultsFeature.updateSesiaResults()
        }
    }

}