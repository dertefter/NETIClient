package com.dertefter.neticlient.ui.profile.profile_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dertefter.neticlient.data.network.model.ResponseResult
import com.dertefter.neticlient.data.network.model.ResponseType
import com.dertefter.neticlient.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {


    private val profileDetailFlow = userRepository.getProfileDetailFlow()

    val uiStateFlow: MutableStateFlow<ResponseResult> = MutableStateFlow(ResponseResult(ResponseType.LOADING))

    fun updateProfileDetails() {
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = profileDetailFlow.first())
            userRepository.updateProfileDetail()
            if (profileDetailFlow.first() != null){
                uiStateFlow.value = ResponseResult(ResponseType.SUCCESS, data = profileDetailFlow.first())
            } else{
                uiStateFlow.value = ResponseResult(ResponseType.ERROR, data = profileDetailFlow.first())
            }
        }
    }

    fun saveData(
        n_email: String,
        n_address: String,
        n_phone: String,
        n_snils: String,
        n_oms: String,
        n_vk: String,
        n_tg: String,
        n_leader: String
    ) {
        viewModelScope.launch {
            uiStateFlow.value = ResponseResult(ResponseType.LOADING, data = profileDetailFlow.first())
            userRepository.sendProfileDetails(n_email, n_address, n_phone, n_snils, n_oms, n_vk, n_tg, n_leader)
            updateProfileDetails()
        }
    }
}