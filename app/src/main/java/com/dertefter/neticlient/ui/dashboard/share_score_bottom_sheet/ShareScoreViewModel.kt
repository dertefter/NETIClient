package com.dertefter.neticlient.ui.dashboard.share_score_bottom_sheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.SessiaResultsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareScoreViewModel @Inject constructor(
    private val sessiaResultsRepository: SessiaResultsRepository
): ViewModel() {


    private val shareScoreFlow = sessiaResultsRepository.getShareScoreFlow()

    val uiStateFlow = MutableStateFlow<ResponseResult>(ResponseResult(ResponseType.LOADING))

    fun updateShareScore(){
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = shareScoreFlow.first())
            sessiaResultsRepository.updateShareScore()
            val shareScore = shareScoreFlow.first()
            if (!shareScore.isNullOrEmpty()){
                uiStateFlow.value = ResponseResult(ResponseType.SUCCESS, data = shareScoreFlow.first())
            }else{
                uiStateFlow.value = ResponseResult(ResponseType.ERROR, data = shareScoreFlow.first())
            }
        }
    }

    fun replaceLink(){
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = shareScoreFlow.first())
            sessiaResultsRepository.replaceLink()
            updateShareScore()

        }
    }

}