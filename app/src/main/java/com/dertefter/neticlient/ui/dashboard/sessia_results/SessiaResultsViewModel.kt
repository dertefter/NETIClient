package com.dertefter.neticlient.ui.dashboard.sessia_results

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
class SessiaResultsViewModel @Inject constructor(
    private val sessiaResultsRepository: SessiaResultsRepository
): ViewModel() {


    private val sessiaResultsFlow = sessiaResultsRepository.getSessiaResultsFlow()

    val uiStateFlow = MutableStateFlow<ResponseResult>(ResponseResult(ResponseType.LOADING))

    fun updateSessiaResults(){
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = sessiaResultsFlow.first())
            sessiaResultsRepository.updateSessiaResults()
            val sessiaResults = sessiaResultsFlow.first()
            if (sessiaResults != null){
                uiStateFlow.value = ResponseResult(ResponseType.SUCCESS, data = sessiaResultsFlow.first())
            }else{
                uiStateFlow.value = ResponseResult(ResponseType.ERROR, data = sessiaResultsFlow.first())
            }
        }
    }

}