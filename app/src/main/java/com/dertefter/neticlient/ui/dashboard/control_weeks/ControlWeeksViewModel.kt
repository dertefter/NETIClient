package com.dertefter.neticlient.ui.dashboard.control_weeks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.SessiaResultsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlWeeksViewModel @Inject constructor(
    private val sessiaResultsRepository: SessiaResultsRepository
): ViewModel() {


    private val controlWeeksFlow = sessiaResultsRepository.getControlWeeksFlow()

    val uiStateFlow = MutableStateFlow<ResponseResult>(ResponseResult(ResponseType.LOADING))

    fun updateControlWeeks(){
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = controlWeeksFlow.first())
            sessiaResultsRepository.updateControlWeeks()
            val controlWeeks = controlWeeksFlow.first()
            if (controlWeeks != null){
                uiStateFlow.value = ResponseResult(ResponseType.SUCCESS, data = controlWeeksFlow.first())
            }else{
                uiStateFlow.value = ResponseResult(ResponseType.ERROR, data = controlWeeksFlow.first())
            }
        }
    }

}