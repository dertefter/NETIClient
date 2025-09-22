package com.dertefter.neticlient.ui.dashboard.share_score_bottom_sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticore.NETICore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareScoreViewModel @Inject constructor(
    val netiCore: NETICore
): ViewModel() {


    val shareSessiaResultsFeature = netiCore.shareSessiaResultsFeature

    val status = shareSessiaResultsFeature.status

    val link = shareSessiaResultsFeature.shareScoreLink.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )


    fun updateShareScore(){
        viewModelScope.launch {
            shareSessiaResultsFeature.updateLink()
        }
    }

    fun replaceLink(){
        viewModelScope.launch {
            shareSessiaResultsFeature.requestNewLink()
        }
    }

}