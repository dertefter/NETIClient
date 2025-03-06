package com.dertefter.neticlient.ui.sessia_results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.profile_menu.ProfileMenuItem
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.repository.SessiaResultsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessiaResultsViewModel @Inject constructor(
    private val sessiaResultsRepository: SessiaResultsRepository
): ViewModel() {

    val sessiaResultsLiveData = MutableLiveData<ResponseResult>()

    fun fetchResponseResults(){
        viewModelScope.launch {
            sessiaResultsLiveData.postValue(sessiaResultsRepository.fetchSessiaResults())
        }
    }

}